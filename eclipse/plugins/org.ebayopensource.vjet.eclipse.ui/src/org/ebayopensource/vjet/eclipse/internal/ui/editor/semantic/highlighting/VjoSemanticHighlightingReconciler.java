/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.ebayopensource.vjet.eclipse.internal.ui.editor.semantic.highlighting;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.JstSource;
import org.ebayopensource.dsf.jst.declaration.JstMethod;
import org.ebayopensource.vjet.eclipse.core.IVjoSourceModule;
import org.ebayopensource.vjet.eclipse.core.ts.GenericVisitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.mod.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.core.ISourceModule;
import org.eclipse.dltk.mod.internal.ui.editor.DLTKEditorMessages;
import org.eclipse.dltk.mod.internal.ui.editor.ScriptEditor;
import org.eclipse.dltk.mod.internal.ui.editor.semantic.highlighting.SemanticHighlightingPresenter;
import org.eclipse.dltk.mod.internal.ui.text.IScriptReconcilingListener;
import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
import org.eclipse.dltk.mod.ui.editor.highlighting.HighlightedPosition;
import org.eclipse.dltk.mod.ui.editor.highlighting.HighlightingStyle;
import org.eclipse.dltk.mod.ui.editor.highlighting.ISemanticHighlighter;
import org.eclipse.dltk.mod.ui.editor.highlighting.SemanticHighlighting;
import org.eclipse.dltk.mod.ui.text.ScriptTextTools;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Semantic highlighting reconciler - Background thread implementation.
 * 
 * @since 3.0
 */
public class VjoSemanticHighlightingReconciler implements
		IScriptReconcilingListener, ITextInputListener {

	/** The Java editor this semantic highlighting reconciler is installed on */
	private ScriptEditor fEditor;
	/** The source viewer this semantic highlighting reconciler is installed on */
	private ISourceViewer fSourceViewer;
	/** The semantic highlighting presenter */
	private SemanticHighlightingPresenter fPresenter;
	/** Semantic highlightings */
	private SemanticHighlighting[] fSemanticHighlightings;
	/** Highlightings */
	private HighlightingStyle[] fHighlightings;

	/** Background job */
	private Job fJob;
	/** Background job lock */
	private final Object fJobLock = new Object();
	/**
	 * Reconcile operation lock.
	 * 
	 * @since 3.2
	 */
	private final Object fReconcileLock = new Object();
	/**
	 * <code>true</code> if any thread is executing <code>reconcile</code>,
	 * <code>false</code> otherwise.
	 * 
	 * @since 3.2
	 */
	private boolean fIsReconciling = false;

	/** Background job's added highlighted positions */
	private List fAddedPositions = new ArrayList();
	/** Background job's removed highlighted positions */
	private List fRemovedPositions = new ArrayList();
	/** Number of removed positions */
	private int fNOfRemovedPositions;

	/**
	 * The semantic highlighting presenter - cache for background thread, only
	 * valid during
	 * {@link #reconciled(ModuleDeclaration, boolean, IProgressMonitor)}
	 */
	private SemanticHighlightingPresenter fJobPresenter;
	/**
	 * Semantic highlightings - cache for background thread, only valid during
	 * {@link #reconciled(ModuleDeclaration, boolean, IProgressMonitor)}
	 */
	private SemanticHighlighting[] fJobSemanticHighlightings;
	/**
	 * Highlightings - cache for background thread, only valid during
	 * {@link #reconciled(ModuleDeclaration, boolean, IProgressMonitor)}
	 */
	private HighlightingStyle[] fJobHighlightings;
	private ISemanticHighlighter positionUpdater;

	/** Position collector */
	private PositionCollector fCollector = new PositionCollector();

	/*
	 * @see org.eclipse.jdt.internal.ui.text.Script.IScriptReconcilingListener#
	 *      aboutToBeReconciled()
	 */
	public void aboutToBeReconciled() {
		// Do nothing
	}

	private class PositionCollector extends GenericVisitor {

		/** The semantic token */
		private SemanticToken fToken = new SemanticToken();

		// public boolean visit(IJstNode node) {
		//
		// if (node instanceof JstName) {
		// visitNameNode((JstName) node);
		// }
		//
		// return true;
		// }

		@Override
		public void visit(JstMethod node) {
			fToken.update(node);
			for (int i = 0, n = fJobSemanticHighlightings.length; i < n; i++) {
				VjoSemanticHighlighting semanticHighlighting = (VjoSemanticHighlighting) fJobSemanticHighlightings[i];
				if (fJobHighlightings[i].isEnabled()
						&& semanticHighlighting.consumes(fToken)) {
					JstSource source = node.getName().getSource();
					if(source==null){
						continue;
					}
					int offset = source.getStartOffSet();
					int length = source.getLength();
					if (offset > -1 && length > 0)
						addPosition(offset, length, fJobHighlightings[i]);
					break;
				}
			}
			fToken.clear();
		}

		// private boolean visitLiteral(Expression node) {
		// fToken.update(node);
		// for (int i = 0, n = fJobSemanticHighlightings.length; i < n; i++) {
		// SemanticHighlighting semanticHighlighting =
		// fJobSemanticHighlightings[i];
		// if (fJobHighlightings[i].isEnabled()
		// && semanticHighlighting.consumesLiteral(fToken)) {
		// int offset = node.getStartPosition();
		// int length = node.getLength();
		// if (offset > -1 && length > 0)
		// addPosition(offset, length, fJobHighlightings[i]);
		// break;
		// }
		// }
		// fToken.clear();
		// return false;
		// }
		//

		//
		/**
		 * Add a position with the given range and highlighting iff it does not
		 * exist already.
		 * 
		 * @param offset
		 *            The range offset
		 * @param length
		 *            The range length
		 * @param highlighting
		 *            The highlighting
		 */
		private void addPosition(int offset, int length,
				HighlightingStyle highlighting) {
			boolean isExisting = false;
			// TODO: use binary search
			for (int i = 0, n = fRemovedPositions.size(); i < n; i++) {
				HighlightedPosition position = (HighlightedPosition) fRemovedPositions
						.get(i);
				if (position == null)
					continue;
				if (position.isEqual(offset, length, highlighting)) {
					isExisting = true;
					fRemovedPositions.set(i, null);
					fNOfRemovedPositions--;
					break;
				}
			}

			if (!isExisting) {
				Position position = fJobPresenter.createHighlightedPosition(
						offset, length, highlighting);
				fAddedPositions.add(position);
			}
		}
		//
		// /**
		// * Retain the positions completely contained in the given range.
		// *
		// * @param offset
		// * The range offset
		// * @param length
		// * The range length
		// */
		// private void retainPositions(int offset, int length) {
		// // TODO: use binary search
		// for (int i = 0, n = fRemovedPositions.size(); i < n; i++) {
		// HighlightedPosition position = (HighlightedPosition)
		// fRemovedPositions
		// .get(i);
		// if (position != null && position.isContained(offset, length)) {
		// fRemovedPositions.set(i, null);
		// fNOfRemovedPositions--;
		// }
		// }
		// }

	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.Script.IScriptReconcilingListener#reconciled
	 *      (ModuleDeclaration, boolean, IProgressMonitor)
	 */
	public void reconciled(ISourceModule ast, boolean forced,
			IProgressMonitor progressMonitor) {
		// if (positionUpdater == null)
		// return;
		// // ensure at most one thread can be reconciling at any time
		// synchronized (fReconcileLock) {
		// if (fIsReconciling)
		// return;
		// else
		// fIsReconciling = true;
		// }
		// fJobPresenter = fPresenter;
		// fJobSemanticHighlightings = fSemanticHighlightings;
		// fJobHighlightings = fHighlightings;
		//
		// try {
		// // long t0 = System.currentTimeMillis();
		// if (fJobPresenter == null || fJobSemanticHighlightings == null ||
		// fJobHighlightings == null)
		// return;
		//
		// fJobPresenter.setCanceled(progressMonitor.isCanceled());
		//
		// if (ast == null || fJobPresenter.isCanceled())
		// return;
		//
		// HighlightedPosition[] added = HighlightedPosition.NO_POSITIONS;
		// HighlightedPosition[] removed = HighlightedPosition.NO_POSITIONS;
		// if (!fJobPresenter.isCanceled()) {
		// final List currentPositions = new ArrayList();
		// fJobPresenter.addAllPositions(currentPositions);
		// final UpdateResult result =
		// positionUpdater.reconcile((org.eclipse.dltk.mod.compiler.env.ISourceModule)
		// ast, currentPositions);
		// added = result.addedPositions;
		// removed = result.removedPositions;
		// }
		//			
		//			
		//			
		//			
		// //ASTNode[] subtrees= getAffectedSubtrees(ast);
		// // if (subtrees.length == 0)
		// // return;
		//			
		// startReconcilingPositions();
		//			
		// if (!fJobPresenter.isCanceled())
		// reconcilePositions(((VjoSourceModule)ast).getJstType());
		//			
		// // TextPresentation textPresentation= null;
		// // if (!fJobPresenter.isCanceled())
		// // textPresentation=
		// fJobPresenter.createPresentation(fAddedPositions, fRemovedPositions);
		// //
		// // if (!fJobPresenter.isCanceled())
		// // updatePresentation(textPresentation, fAddedPositions,
		// fRemovedPositions);
		// //
		// stopReconcilingPositions();
		//			
		//			
		//			
		//
		// if (added.length != 0 || removed.length != 0) {
		// if (!fJobPresenter.isCanceled()) {
		// final TextPresentation textPresentation =
		// fJobPresenter.createPresentation(added, removed);
		// if (!fJobPresenter.isCanceled())
		// updatePresentation(textPresentation, added, removed);
		// }
		// }
		//
		// // long t1 = System.currentTimeMillis();
		// // System.out.println(t1 - t0);
		//
		// } finally {
		// fJobPresenter = null;
		// fJobSemanticHighlightings = null;
		// fJobHighlightings = null;
		// synchronized (fReconcileLock) {
		// fIsReconciling = false;
		// }
		// }

		synchronized (fReconcileLock) {
			if (fIsReconciling)
				return;
			else
				fIsReconciling = true;
		}
		fJobPresenter = fPresenter;
		fJobSemanticHighlightings = fSemanticHighlightings;
		fJobHighlightings = fHighlightings;

		try {
			if (fJobPresenter == null || fJobSemanticHighlightings == null
					|| fJobHighlightings == null)
				return;

			fJobPresenter.setCanceled(progressMonitor.isCanceled());

			if (ast == null || fJobPresenter.isCanceled())
				return;

			// ASTNode[] subtrees= getAffectedSubtrees(ast);
			// if (subtrees.length == 0)
			// return;

			startReconcilingPositions();

			if (!fJobPresenter.isCanceled())
				reconcilePositions(((IVjoSourceModule) ast).getJstType());

			TextPresentation textPresentation = null;
			HighlightedPosition[] addedPositions = (HighlightedPosition[]) fAddedPositions
					.toArray(new HighlightedPosition[fAddedPositions.size()]);
			HighlightedPosition[] removedPositions = (HighlightedPosition[]) fRemovedPositions
					.toArray(new HighlightedPosition[fRemovedPositions.size()]);

			if (!fJobPresenter.isCanceled())
				textPresentation = fJobPresenter.createPresentation(
						addedPositions, removedPositions);

			if (!fJobPresenter.isCanceled())
				updatePresentation(textPresentation, addedPositions,
						removedPositions);

			stopReconcilingPositions();
		} finally {
			fJobPresenter = null;
			fJobSemanticHighlightings = null;
			fJobHighlightings = null;
			synchronized (fReconcileLock) {
				fIsReconciling = false;
			}
		}
	}

	/**
	 * Start reconciling positions.
	 */
	private void startReconcilingPositions() {
		fJobPresenter.addAllPositions(fRemovedPositions);
		fNOfRemovedPositions = fRemovedPositions.size();
	}

	/**
	 * Stop reconciling positions.
	 */
	private void stopReconcilingPositions() {
		fRemovedPositions.clear();
		fNOfRemovedPositions = 0;
		fAddedPositions.clear();
	}

	/**
	 * Reconcile positions based on the AST subtrees
	 * 
	 * @param subtrees
	 *            the AST subtrees
	 */
	private void reconcilePositions(IJstNode jstNode) {
		jstNode.accept(fCollector);
		List oldPositions = fRemovedPositions;
		List newPositions = new ArrayList(fNOfRemovedPositions);
		for (int i = 0, n = oldPositions.size(); i < n; i++) {
			Object current = oldPositions.get(i);
			if (current != null)
				newPositions.add(current);
		}
		fRemovedPositions = newPositions;
	}

	/**
	 * Update the presentation.
	 * 
	 * @param textPresentation
	 *            the text presentation
	 * @param addedPositions
	 *            the added positions
	 * @param removedPositions
	 *            the removed positions
	 */
	private void updatePresentation(TextPresentation textPresentation,
			HighlightedPosition[] addedPositions,
			HighlightedPosition[] removedPositions) {
		Runnable runnable = fJobPresenter.createUpdateRunnable(
				textPresentation, addedPositions, removedPositions);
		if (runnable == null)
			return;

		ScriptEditor editor = fEditor;
		if (editor == null)
			return;

		IWorkbenchPartSite site = editor.getSite();
		if (site == null)
			return;

		Shell shell = site.getShell();
		if (shell == null || shell.isDisposed())
			return;

		Display display = shell.getDisplay();
		if (display == null || display.isDisposed())
			return;

		display.asyncExec(runnable);
	}

	public void install(ScriptEditor editor, ISourceViewer sourceViewer,
			SemanticHighlightingPresenter presenter,
			SemanticHighlighting[] semanticHighlightings,
			HighlightingStyle[] highlightings) {
		fPresenter = presenter;
		fSemanticHighlightings = semanticHighlightings;
		fHighlightings = highlightings;
		ScriptTextTools textTools = editor.getTextTools();
		if (textTools != null) {
			this.positionUpdater = textTools.getSemanticPositionUpdater();
			this.positionUpdater.initialize(fPresenter, fHighlightings);
		}

		fEditor = editor;
		fSourceViewer = sourceViewer;

		// if (fEditor != null) {
		fEditor.addReconcileListener(this);
		// } else {
		// fSourceViewer.addTextInputListener(this);
		// scheduleJob();
		// }
	}

	/**
	 * Uninstall this reconciler from the editor
	 */
	public void uninstall() {
		if (fPresenter != null)
			fPresenter.setCanceled(true);

		if (fEditor != null) {
			fEditor.removeReconcileListener(this);
			fEditor = null;
		} else {
			fSourceViewer.removeTextInputListener(this);
		}

		fSourceViewer = null;
		fSemanticHighlightings = null;
		fHighlightings = null;
		fPresenter = null;
	}

	/**
	 * Schedule a background job for retrieving the AST and reconciling the
	 * Semantic Highlighting model.
	 */
	private void scheduleJob() {
		final IModelElement element = fEditor.getInputModelElement();

		synchronized (fJobLock) {
			final Job oldJob = fJob;
			if (fJob != null) {
				fJob.cancel();
				fJob = null;
			}

			if (element != null) {
				fJob = new Job(DLTKEditorMessages.SemanticHighlighting_job) {
					protected IStatus run(IProgressMonitor monitor) {
						if (oldJob != null) {
							try {
								oldJob.join();
							} catch (InterruptedException e) {
								DLTKUIPlugin.log(e);
								return Status.CANCEL_STATUS;
							}
						}
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						final ISourceModule code = DLTKUIPlugin.getDefault()
								.getWorkingCopyManager().getWorkingCopy(
										fEditor.getEditorInput());
						reconciled(code, false, monitor);
						synchronized (fJobLock) {
							// allow the job to be gc'ed
							if (fJob == this)
								fJob = null;
						}
						return Status.OK_STATUS;
					}
				};
				fJob.setSystem(true);
				fJob.setPriority(Job.DECORATE);
				fJob.schedule();
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged
	 *      (org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
	 */
	public void inputDocumentAboutToBeChanged(IDocument oldInput,
			IDocument newInput) {
		synchronized (fJobLock) {
			if (fJob != null) {
				fJob.cancel();
				fJob = null;
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse
	 *      .jface.text.IDocument, org.eclipse.jface.text.IDocument)
	 */
	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput != null)
			scheduleJob();
	}

	/**
	 * Refreshes the highlighting.
	 * 
	 * @since 3.2
	 */
	public void refresh() {
		scheduleJob();
	}
}

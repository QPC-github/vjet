/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/**
 * 
 */
package org.ebayopensource.vjet.eclipse.internal.ui.view.scriptunit;

import java.lang.reflect.Method;
import java.util.List;

import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.IScriptProblem;
import org.ebayopensource.dsf.jst.IScriptUnit;
import org.ebayopensource.dsf.jst.JstSource;
import org.ebayopensource.dsf.jst.declaration.JstBlock;
import org.ebayopensource.dsf.jst.declaration.JstMethod;
import org.ebayopensource.dsf.jst.declaration.JstProperty;
import org.ebayopensource.dsf.jst.declaration.JstType;
import org.ebayopensource.dsf.jst.term.JstIdentifier;
import org.ebayopensource.dsf.jst.term.SimpleLiteral;
import org.ebayopensource.dsf.jstojava.translator.JstUtil;
import org.ebayopensource.vjet.eclipse.core.VjetPlugin;
import org.ebayopensource.vjet.eclipse.core.parser.VjoParserToJstAndIType;
import org.ebayopensource.vjet.eclipse.internal.ui.editor.VjoEditor;
import org.ebayopensource.vjet.eclipse.internal.ui.nodeprinter.INodePrinter;
import org.ebayopensource.vjet.eclipse.internal.ui.nodeprinter.NodePrinterFactory;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.internal.core.NativeVjoSourceModule;
import org.eclipse.dltk.mod.internal.core.VjoSourceModule;
import org.eclipse.dltk.mod.internal.ui.editor.ExternalFileEditorInput;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * Script Unit View
 * 
 * 
 *
 */
public class ScriptUnitView extends ViewPart implements ISelectionListener{
	private TreeViewer viewer;
	private Label offsetLabel;

	private VjoEditor vjoEditor;  //the editor  that provide the content
	private IPartListener partListener = new PartListener();
	private IDocumentListener documentListener = new DocumentListener();
	
	private IJstType jstType;     //last jst type input
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		
		this.getSite().getPage().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
		this.getSite().getPage().getWorkbenchWindow().getPartService().addPartListener(partListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
				
		//create tree viewer
		this.viewer = new TreeViewer(parent);
		this.viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		this.viewer.setContentProvider(new ScriptUnitTreeContentProvider());
		this.viewer.setLabelProvider(new ScriptUnitTreeLabelProvider());
		
		this.viewer.addDoubleClickListener(new IDoubleClickListener() {
			/* 
			 * select source code range in editor
			 * 
			 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
			 */
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection structuredSelection = (IStructuredSelection)event.getSelection();
				if (!(structuredSelection.getFirstElement() instanceof IJstNode))
					return;
				
				IJstNode jstNode = (IJstNode)structuredSelection.getFirstElement();
				
				JstSource jstSource = jstNode.getSource();
				if (jstSource == null)
					return;
				
				vjoEditor.getScriptSourceViewer().revealRange(jstSource.getStartOffSet(), jstSource.getLength());
				vjoEditor.getScriptSourceViewer().setSelectedRange(jstSource.getStartOffSet(), jstSource.getLength());
			}
		});
		
		//offset label
		this.offsetLabel = new Label(parent, SWT.SHADOW_IN);
		this.offsetLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		//set selection provider
		this.getSite().setSelectionProvider(this.viewer);
		
		//fill action bar
		this.fillActionBar();
		
		//context menu
        this.initContextMenu();
	}
	
	private void fillActionBar() {
		IActionBars actionBars = this.getViewSite().getActionBars();
		actionBars.getToolBarManager().add(new CheckNodeAction(this.viewer));
	}

	private void initContextMenu() {
		MenuManager popupMenuManager = new MenuManager("#PopupMenu");
		
		popupMenuManager.add(new Action("Copy Structure") {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				Object selectedNode = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
				String structure = copyStructure(selectedNode, 0);
				Clipboard clipboard = new Clipboard(Display.getCurrent());
				clipboard.setContents(new Object[] {structure}, new Transfer[] {TextTransfer.getInstance()});
				clipboard.dispose();
			}
		});
		
		popupMenuManager.add(new Action("Copy Properties") {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				Object selectedNode = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
				String properties = copyProperties(selectedNode);
				Clipboard clipboard = new Clipboard(Display.getCurrent());
				clipboard.setContents(new Object[] {properties}, new Transfer[] {TextTransfer.getInstance()});
				clipboard.dispose();
			}
		});
		
		popupMenuManager.add(new Action("Copy XPath") {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.Action#run()
			 */
			public void run() {
				Object selectedNode = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
				String xpath = "";
				if (selectedNode instanceof IJstNode){
					IJstNode node = (IJstNode)selectedNode;
					ITreeContentProvider contentProvider = (ITreeContentProvider)viewer.getContentProvider();
					xpath = getXPathTestString(node, contentProvider);
					System.out.println("Here is my XPath === : \n" + xpath);
				}
				Clipboard clipboard = new Clipboard(Display.getCurrent());
				clipboard.setContents(new Object[] {xpath}, new Transfer[] {TextTransfer.getInstance()});
				clipboard.dispose();
			}
		});
		
		Menu popupMenu = popupMenuManager.createContextMenu(this.viewer.getTree());
		this.viewer.getTree().setMenu(popupMenu);
		getSite().registerContextMenu(popupMenuManager, this.viewer); 
	}
	
	private String getXPathTestString(IJstNode node, ITreeContentProvider contentProvider){
		StringBuffer testString = new StringBuffer("<testcase number='1'>\n\t");
		node.getOwnerType().getName();
		testString.append("<file>" + node.getRootType().getName()+ "</file>\n\t");
		testString.append("<jxpath>\n\t\t");
		String nodeToPut = "";
		if (getNodeName((Object)node).equals("")){
			nodeToPut = node.getClass().getSimpleName();
		} else {
			nodeToPut = node.getClass().getSimpleName()
			+ "[@Name='" + getNodeName((Object)node) + "']";
		}
		String str = getXPath(nodeToPut, node, contentProvider);
		testString.append("<pathname>"+str+"</pathname>\n\t\t");
		testString.append("<nodecount>1</nodecount>\n\t\t");
		testString.append("<node position='1' status='pass'>\n\t\t\t");
		if (node instanceof JstIdentifier){
			IJstNode bNode = ((JstIdentifier)node).getJstBinding();
			String bName = getNodeName(bNode);
			String bType = getTypeList(bNode);
			String bClass = bNode.getClass().getSimpleName();
			String bParent = getTypeList(bNode.getParentNode());
			System.out.println(bName + " ; " + bType + " ; " + bClass + " ; " + bParent);
			testString.append("<bindingnode>" + getNodeName(bNode) + "</bindingnode>\n\t\t\t");
			testString.append("<bindingtype>" + getTypeList(bNode) + "</bindingtype>\n\t\t\t");
			testString.append("<bindingclass>" + bNode.getClass().getSimpleName() + "</bindingclass>\n\t\t\t");
			testString.append("<bindingparent>" + getTypeList(bNode.getParentNode()) + "</bindingparent>\n\t\t");
		} else {
			testString.append("<bindingnode>"+ getNodeName(node) +"</bindingnode>\n\t\t\t");
			testString.append("<bindingtype>"+ getTypeList(node) +"</bindingtype>\n\t\t");
		}
		testString.append("</node>\n\t");
		testString.append("</jxpath>\n");
		testString.append("</testcase>");
		return testString.toString();
	}
	
	private String getXPath(String str, Object obj, 
			ITreeContentProvider contentProvider){
		String returnStr = "";
		if (contentProvider.getParent(obj) != null){
			Object parent = contentProvider.getParent(obj);
			String addIt = "";
			//Add node and name
			if (getNodeName(parent).equals("")){
				addIt = parent.getClass().getSimpleName();
			} else {
				addIt = parent.getClass().getSimpleName() 
					+ "[@Name='" + getNodeName(parent) + "']";
			}
			
			if(getNodeName(obj).equals("")){
				//Add position if more than 1
				if (getPosition((IJstNode)obj, (IJstNode)parent) > 1){
					if (str.indexOf("/") == -1) {
						str = str + "[@Position='" + 
							getPosition((IJstNode)obj, (IJstNode)parent) + "']";
					} else {
						str = str.replaceFirst("/", "[@Position='" + 
								getPosition((IJstNode)obj, (IJstNode)parent) + "']/");
					}
				}
			}
			
			str = addIt + "/" + str;
			if (parent.getClass().getSimpleName().equals(
					JstType.class.getSimpleName())){
				return str;
			}
			returnStr = getXPath(str, parent, contentProvider);
		}	
		return returnStr;
	}
	
	private int getPosition(IJstNode child, IJstNode parent){
		int position = 0;
		for (IJstNode node : parent.getChildren()){
			if (node.getClass().equals(child.getClass())){
				position = position + 1;
				if (getNodeName(node).equals(getNodeName(child))){
					break;
				}
			}
		}
		return position;
	}
	
	private static String getNodeName(IJstNode node) {
		String value = null;
		try {
			JstAstInfoVisitor visitor = new JstAstInfoVisitor();
			Method method = JstAstInfoVisitor.class.getMethod("visit", node
					.getClass());
			method.invoke(visitor, node);
			value = visitor.getValue();
		} catch (Exception e) {
		}
		return value;
	}
	
	private String getNodeName(Object node){
		String str = "";
		if (node instanceof JstMethod) {
			str = ((JstMethod)node).getName().getName();
		} else if (node instanceof JstProperty) {
			str = ((JstProperty)node).getName().getName();
		} else if (node instanceof JstIdentifier){
			str = ((JstIdentifier)node).getName();
		} else if (node instanceof SimpleLiteral){
			str = ((SimpleLiteral)node).getValue();
		} 
		
		return str;
	}
	
	private String getTypeList(IJstNode node){
		String retStr = "";
		try {
			JstAstInfoVisitor visitor = new JstAstInfoVisitor();
			Method method = JstAstInfoVisitor.class.getMethod("visit", node
					.getClass());
			method.invoke(visitor, node);
			List<String> actualType = visitor.getType();
			for (String s : actualType){
				if (retStr.equals(""))
					retStr = retStr + s;
				else
					retStr = retStr + "," +s;
			}
		} catch (Exception e) {}
		
		return retStr;
	}
	
	private String copyStructure(Object node, int tier) {
		ILabelProvider labelProvider = (ILabelProvider)this.viewer.getLabelProvider();
		StringBuilder structureBuilder = new StringBuilder(labelProvider.getText(node));
		
		ITreeContentProvider contentProvider = (ITreeContentProvider)this.viewer.getContentProvider();
		Object[] children = contentProvider.getChildren(node);
		++ tier;
		for (int i = 0; i < children.length; i++) {
			String childrenStructure = copyStructure(children[i], tier);
			structureBuilder.append("\n");
			for (int j = 0; j < tier; j++) {
				structureBuilder.append("\t");
			}
			structureBuilder.append(childrenStructure);
		}
		return structureBuilder.toString();
	}
	
	private String copyProperties(Object node) {
		INodePrinter nodePrinter = NodePrinterFactory.getNodePrinter(node);
		if (nodePrinter == null)
			return "";
		
		String[] names = nodePrinter.getPropertyNames(node);
		if (names.length == 0)
			return "";
		
		StringBuilder stringBuilder = new StringBuilder();
		Object[] values = nodePrinter.getPropertyValuies(node);
		for (int i = 0; i < names.length; i++) {
			stringBuilder.append(names[i] + ":" + values[i]);
			if (i != names.length - 1)
				stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(part instanceof IEditorPart))
			return;
		
		if (!(part instanceof VjoEditor)) {
			this.cleanUp();
			return;
		}
		
		if (this.vjoEditor != part) {
			/**
			 * fix bug 5832: first time open 'Array', do parsing, so the groupName of native type 'Array' will be set NULL.
			 * Note: do not use VjoEditor.getInputElement(), for native type, maybe return VjoSourceModule not NativeVjoSourceModule.
			 * For example, to native type 'Date', VjoSourceModule.getGroupname() return NULL!!!
			 */
			IModelElement modelElement = null;
			IEditorInput editorInput = ((VjoEditor)part).getEditorInput();
			if (editorInput instanceof ExternalFileEditorInput) {
				modelElement = ((ExternalFileEditorInput)editorInput).getModelElement();
			}
			else
				modelElement = ((VjoEditor)part).getInputModelElement();
			
			if (!(modelElement instanceof VjoSourceModule)) {
				this.cleanUp();
				return;
			}
			
			VjoSourceModule sourceModule = ((VjoSourceModule)modelElement);
			IScriptUnit scriptUnit = this.getScriptUnit(sourceModule, (VjoEditor)part);
			
			//add to fix NPE
			if (scriptUnit == null)
				return;
			
			//record the latest jst type
			this.jstType = scriptUnit.getType();
			
			this.vjoEditor = (VjoEditor)part;
			this.vjoEditor.getScriptSourceViewer().getDocument().addDocumentListener(this.documentListener);
			
			//update script unit
			((ScriptUnitTreeContentProvider)this.viewer.getContentProvider()).setScriptUnit(scriptUnit);
			this.viewer.setInput(new Object[]{scriptUnit});
		}
		
		if (selection instanceof ITextSelection && jstType != null) {
			ITextSelection textSelection = (ITextSelection)selection;
			
			int startOffset = textSelection.getOffset();
			int endOffset = startOffset + textSelection.getLength();
			if (textSelection.getLength() > 0)
				endOffset = startOffset + textSelection.getLength() - 1;
				
			//update offset status label
			String offsetInfo = "Offset:" + textSelection.getOffset() + "  Length:" + textSelection.getLength();
			this.offsetLabel.setText(offsetInfo);
			
			Object node = JstUtil.getLeafNode(jstType, startOffset, endOffset);
			if (node != null) {
				this.viewer.setSelection(new StructuredSelection(node), true);
			}
			else
				this.viewer.setSelection(null);
		}
	}
	
	private IJstType getJstType(VjoSourceModule sourceModule) {
		if (sourceModule instanceof NativeVjoSourceModule)
			return TypeSpaceMgr.getInstance().findType(sourceModule.getTypeName());
		else
			return sourceModule.getJstType();
	}
	
	
	/**
	 * Note: this method should not be changed, wrong parsing will interrupt jst type in TypespaceMgr
	 * 
	 * @param sourceModule
	 * @param vjoEditor
	 * @return
	 */
	private IScriptUnit getScriptUnit(VjoSourceModule sourceModule, VjoEditor vjoEditor) {
		try {
			String groupName = sourceModule.getGroupName();
			String fileName = sourceModule.getTypeName().typeName();
			System.out.println(groupName);
			if(groupName.equals(".org.eclipse.dltk.mod.core.external.folders")){
				final IJstType type = TypeSpaceMgr.getInstance().findType(sourceModule.getTypeName());
				return new IScriptUnit() {
					
					@Override
					public IJstType getType() {
						// TODO Auto-generated method stub
						return type;
					}
					
					@Override
					public JstBlock getSyntaxRoot() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public List<IScriptProblem> getProblems() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public IJstNode getNode(int startOffset) {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public List<JstBlock> getJstBlockList() {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}
			// if from lib don't parse it get type from typespace
			
			
			String source = sourceModule.getSource();
			VjoParserToJstAndIType m_parser = new VjoParserToJstAndIType();
			if(VjetPlugin.TRACE_PARSER){
				System.out.println("parsing for " + getClass().getName());
			}
			return m_parser.parse(groupName, sourceModule.getTypeName().typeName(), source);
		} catch (Exception e) {
			return null;
		}
	}
	
	private boolean needUpdate(IJstType jstType) {
		return this.jstType != jstType;
	}
	
	private void cleanUp() {
		this.viewer.setInput(new Object[0]);
		
		if (this.vjoEditor != null)
			this.vjoEditor.getScriptSourceViewer().getDocument().removeDocumentListener(this.documentListener);
		this.vjoEditor = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		if (this.vjoEditor != null)
			this.vjoEditor.getScriptSourceViewer().getDocument().removeDocumentListener(this.documentListener);
		super.dispose();
		this.getSite().getPage().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
		this.getSite().getPage().getWorkbenchWindow().getPartService().removePartListener(partListener);
	}
	
	/**
	 * vjo editor part listener
	 * 
	 * 
	 *
	 */
	private class PartListener implements IPartListener {
		public void partActivated(IWorkbenchPart part) {
		}
		
		public void partBroughtToTop(IWorkbenchPart part) {
		}
		
		/* 
		 * clean the content in script view
		 * 
		 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {
			if (vjoEditor == part) {
				cleanUp();
			}
		}
		
		public void partDeactivated(IWorkbenchPart part) {
			// TODO Auto-generated method stub
			
		}
		
		public void partOpened(IWorkbenchPart part) {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * update script unit view, when script unit changed (document changed)
	 * 
	 * 
	 *
	 */
	private class DocumentListener implements IDocumentListener {
		public void documentChanged(DocumentEvent event) {
			IModelElement element = vjoEditor.getInputModelElement();
			if (!(element instanceof VjoSourceModule)) {
				return;
			}
			VjoSourceModule sourceModule = (VjoSourceModule)element;
			IScriptUnit scriptUnit = getScriptUnit(sourceModule, vjoEditor);
			
			//record jst type
			jstType = scriptUnit.getType();
			
			//reset input
			((ScriptUnitTreeContentProvider)viewer.getContentProvider()).setScriptUnit(scriptUnit);
			viewer.setInput(new Object[]{scriptUnit});
		}
		
		public void documentAboutToBeChanged(DocumentEvent event) {
			// nothing to do
		}
	}


}

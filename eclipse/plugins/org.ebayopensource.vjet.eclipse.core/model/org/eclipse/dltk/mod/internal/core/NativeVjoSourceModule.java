/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.dltk.mod.internal.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.vjet.eclipse.core.VjoNature;
import org.ebayopensource.vjet.eclipse.internal.compiler.VjoSourceElementParser;
import org.ebayopensource.vjo.tool.typespace.SourceTypeName;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.mod.core.DLTKCore;
import org.eclipse.dltk.mod.core.ISourceElementParserExtension;
import org.eclipse.dltk.mod.core.IType;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.dltk.mod.core.SourceParserUtil;
import org.eclipse.dltk.mod.utils.CorePrinter;

public class NativeVjoSourceModule extends VjoSourceModule {

	private String group;

	private static final char[] EMPTY_CHAR = new char[0];

	public NativeVjoSourceModule(ScriptFolder folder, String group, String name) {
		super(folder, name, DefaultWorkingCopyOwner.PRIMARY);
		this.group = group;

	}

	@Override
	public char[] getContentsAsCharArray() {
		return EMPTY_CHAR;
	}

	@Override
	public SourceTypeName getTypeName() {
		return new SourceTypeName(group, getElementName());
	}

	@Override
	protected boolean buildStructure(OpenableElementInfo info,
			IProgressMonitor progressMonitor, Map newElements,
			IResource underlyingResource) throws ModelException {

		try {
			final JSSourceModuleElementInfo moduleInfo = (JSSourceModuleElementInfo) info;

			build(newElements, moduleInfo);

			if (DLTKCore.DEBUG_PRINT_MODEL) {
				System.out.println("Source Module Debug print:"); //$NON-NLS-1$

				CorePrinter printer = new CorePrinter(System.out);
				printNode(printer);
				printer.flush();
			}

			return moduleInfo.isStructureKnown();
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	@Override
	protected boolean hasBuffer() {
		return false;
	}

	@Override
	public boolean isConsistent() {
		// TODO Auto-generated method stub
		return true;// this is readonly
	}

	private void build(Map newElements,
			final JSSourceModuleElementInfo moduleInfo) throws CoreException,
			ModelException {
		// generate structure and compute syntax problems if needed
		final VjoSourceModuleStructureRequestor requestor = new VjoSourceModuleStructureRequestor(
				this, moduleInfo, newElements);

		// System.out.println("==> Parsing: " + resource.getName());
		final String natureId = VjoNature.NATURE_ID;
		if (natureId == null) {
			throw new ModelException(new ModelStatus(ModelStatus.INVALID_NAME));
		}

		final VjoSourceElementParser parser = (VjoSourceElementParser) getSourceElementParser(natureId);
		if (!isReadOnly()) {
			((ISourceElementParserExtension) parser).setScriptProject(this
					.getScriptProject());
		}

		parser.setRequestor(requestor);

		final AccumulatingProblemReporter problemReporter = getAccumulatingProblemReporter();
		parser.setReporter(problemReporter);

		SourceParserUtil.parseSourceModule(this, parser);
		if (problemReporter != null) {
			if (!problemReporter.hasErrors()) {
				StructureBuilder.build(natureId, this, problemReporter);
			}
			problemReporter.reportToRequestor();
		}

		/*
		 * ModelManager manager = ModelManager.getModelManager();
		 * manager.putInfos(this, newElements);
		 */
	}

	public void build() throws ModelException {
		try {
			build(Collections.emptyMap(),
					(JSSourceModuleElementInfo) createElementInfo());
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	@Override
	public char[] getFileName() {
		return super.getElementName().toCharArray();
	}

	@Override
	public IResource getResource() {
		if (jstType == null) {
			return null;
		} else {
			String groupName = this.jstType.getPackage().getGroupName();
			URI path =null;
			String filePath =null;
			try {
				// typespace://ExtJsTL.zip:0/?group=ExtJsTL.zip
				filePath = this.jstType.getName().replace(".", "/") + ".js";
				path = new URI("typespace://"+ groupName + ":0/" + filePath+ "?group=" + groupName);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject externalProject = ResourcesPlugin.getWorkspace().getRoot().getProject(ExternalFoldersManager.EXTERNAL_PROJECT_NAME);
			
			 IPath suffix = new Path(groupName).append(path.getPath());
//			
			 IFile f = externalProject.getFile(suffix);
			 if(f.exists()){
				 return f;
			 }
			 
			 // try finding by uri
			
			  IFile[] files = root.findFilesForLocationURI(path,
						IContainer.INCLUDE_HIDDEN);
			  if(files.length>0){
			    return files[0];
			  }
			
			// fall back to old way but this doesn't work anymore
		
			return getScriptProject().getProject().getFile(
					jstType.getName().replace(".", "/") + ".js");
		}
	}

	@Override
	public char[] getSourceAsCharArray() throws ModelException {
		return null;
	}

	public IType getVjoType() {
		IType[] types;
		try {
			types = getTypes();
			if (types.length > 0) {
				return types[0];
			} else {
				return null;
			}
		} catch (ModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.mod.internal.core.SourceModule#exists()
	 * 
	 * TODO Jack, temperly added, may cause issues, to show outline view for
	 * native type Any one can comment this method
	 */
	public boolean exists() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	private void innerRefresh() {
		try {
			ModelManager manager = ModelManager.getModelManager();
			final String natureId = getNatureId();
			final VjoSourceElementParser parser = (VjoSourceElementParser) getSourceElementParser(natureId);
			HashMap newElements = new HashMap();
			JSSourceModuleElementInfo info = (JSSourceModuleElementInfo) createElementInfo();

			final VjoSourceModuleStructureRequestor requestor = new VjoSourceModuleStructureRequestor(
					this, info, newElements);

			if (!isReadOnly()) {
				((ISourceElementParserExtension) parser).setScriptProject(this
						.getScriptProject());
			}

			parser.setRequestor(requestor);

			final AccumulatingProblemReporter problemReporter = getAccumulatingProblemReporter();
			parser.setReporter(problemReporter);

			SourceParserUtil.parseSourceModule(this, parser);

			manager.putInfos(this, newElements);

		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void setJstType(IJstType jtype) {
		if (jtype != this.jstType) {
			this.jstType = jtype;
			if (jstType != null) {
				innerRefresh();
			}
		}
	}

}

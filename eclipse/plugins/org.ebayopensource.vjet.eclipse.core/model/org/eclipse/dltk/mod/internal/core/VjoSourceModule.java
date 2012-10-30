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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.IScriptProblem;
import org.ebayopensource.dsf.jst.IScriptUnit;
import org.ebayopensource.dsf.jst.declaration.JstBlock;
import org.ebayopensource.dsf.jstojava.translator.JstUtil;
import org.ebayopensource.dsf.ts.type.TypeName;
import org.ebayopensource.vjet.eclipse.codeassist.CodeassistUtils;
import org.ebayopensource.vjet.eclipse.core.IVjoSourceModule;
import org.ebayopensource.vjet.eclipse.core.VjetPlugin;
import org.ebayopensource.vjet.eclipse.core.parser.VjoParserToJstAndIType;
import org.ebayopensource.vjet.eclipse.internal.compiler.VjoSourceElementParser;
import org.ebayopensource.vjo.tool.typespace.ITypeSpaceRunnable;
import org.ebayopensource.vjo.tool.typespace.SourceTypeName;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceListener;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.mod.core.DLTKCore;
import org.eclipse.dltk.mod.core.IBuffer;
import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.core.ISourceElementParserExtension;
import org.eclipse.dltk.mod.core.ISourceModule;
import org.eclipse.dltk.mod.core.IType;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.dltk.mod.core.SourceParserUtil;
import org.eclipse.dltk.mod.core.WorkingCopyOwner;
import org.eclipse.dltk.mod.internal.core.builder.StandardScriptBuilder;
import org.eclipse.dltk.mod.utils.CorePrinter;

public class VjoSourceModule extends JSSourceModule implements
		TypeSpaceListener, IVjoSourceModule {

	private TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
	protected IJstType jstType;

	private boolean isConsistent = false;

	private boolean m_typeProcessed;

	private List<IScriptProblem> m_probs;

	private List<JstBlock> m_blockList;

	private JstBlock m_block;
//	private IUnitSource m_unitSource = new VjoUnitSource();

	private final class SourceStructureBuilder implements ITypeSpaceRunnable {
		private final IResource underlyingResource;
		private final OpenableElementInfo info;
		private final Map newElements;
		private final IProgressMonitor progressMonitor;
		private boolean isStructureKnown = false;

		private SourceStructureBuilder(IResource underlyingResource,
				OpenableElementInfo info, Map newElements,
				IProgressMonitor progressMonitor) {

			this.underlyingResource = underlyingResource;
			this.info = info;
			this.newElements = newElements;
			this.progressMonitor = progressMonitor;
		}

		public void run() {
			try {
				isStructureKnown = doBuild(info, progressMonitor, newElements,
						underlyingResource);
			} catch (ModelException e) {
				DLTKCore.error(e.toString(), e);
			}
		}

		public boolean isStructureKnown() {
			return isStructureKnown;
		}

	}

	public VjoSourceModule(ScriptFolder parent, String name,
			WorkingCopyOwner owner) {

		super(parent, name, owner);
//		mgr.addTypeSpaceListener(this);
	}

	@Override
	protected Object openWhenClosed(Object info, IProgressMonitor monitor)
			throws ModelException {
		// TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
		return super.openWhenClosed(info, monitor);
	}

	@Override
	protected boolean buildStructure(final OpenableElementInfo info,
			final IProgressMonitor progressMonitor, final Map newElements,
			final IResource underlyingResource) throws ModelException {

		// TODO add this check back not using JstGroupCache
		// if (!mgr.existGroup(getGroupName())) {
		// return false;
		// }
		SourceStructureBuilder builder;
		builder = new SourceStructureBuilder(underlyingResource, info,
				newElements, progressMonitor);
		mgr.run(builder);

		return builder.isStructureKnown();
	}

	private boolean doBuild(OpenableElementInfo info,
			IProgressMonitor progressMonitor, Map newElements,
			IResource underlyingResource) throws ModelException {
		try {

			final JSSourceModuleElementInfo moduleInfo = (JSSourceModuleElementInfo) info;

			IBuffer buffer = null;
			// ensure buffer is opened
			if (hasBuffer()) {
				buffer = getBufferManager().getBuffer(this);
				if (buffer == null) {
					buffer = openBuffer(progressMonitor, moduleInfo);
				}
			}

			final char[] contents = (buffer == null) ? null : buffer
					.getCharacters();

			// generate structure and compute syntax problems if needed
			final VjoSourceModuleStructureRequestor requestor = new VjoSourceModuleStructureRequestor(
					this, moduleInfo, newElements);

			// System.out.println("==> Parsing: " + resource.getName());
			final String natureId = getNatureId();
			if (natureId == null) {
				throw new ModelException(new ModelStatus(
						ModelStatus.INVALID_NAME));
			}

			SourceTypeName stName = getTypeName();
			IResource resource = getResource();
			// it is not a workspace file
			// if ("".equals(stName.groupName().trim()) && (resource == null ||
			// !resource.exists())) {
			// jstType = CodeassistUtils.findNativeJstType(stName.typeName());
			// } else {
			// processType(contents);
			// }
			final VjoSourceElementParser parser = (VjoSourceElementParser) getSourceElementParser(natureId);
			if (!isReadOnly()) {
				((ISourceElementParserExtension) parser).setScriptProject(this
						.getScriptProject());
			}

			parser.setRequestor(requestor);
			final AccumulatingProblemReporter problemReporter = getAccumulatingProblemReporter();
			parser.setReporter(problemReporter);

			boolean reparsed = false;
			if (problemReporter != null) {
				if (!problemReporter.hasErrors()) {
					StructureBuilder.build(natureId, this, problemReporter);
					reparsed = true;
				}
				problemReporter.reportToRequestor();
			}
			
			if(jstType==null && isVirtualTypeResource(resource)){
				
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		    	IResource typespaceresource = root.findMember(resource.getFullPath());
		    	if (typespaceresource != null) {
		    	    URI location = typespaceresource.getLocationURI();
		    	    String typeName = location.getPath().replace("/", ".");
		    	    String groupName = location.getHost();
		    	    if(typeName.indexOf(".")==0){
		    	    	typeName = typeName.substring(1,typeName.length());
		    	    }
		    	    typeName = typeName.replace(".js", "");
				
				jstType = CodeassistUtils.findJstType(groupName, typeName);
		    	}
			}else if (jstType == null || !reparsed) {
				if ("".equals(stName.groupName().trim())
						&& (resource == null || !resource.exists())) {
					jstType = CodeassistUtils.findNativeJstType(stName
							.typeName());
				} else {
					processType(contents);
				}
			}

			// parse source module after getting the JstType
			//
			SourceParserUtil.parseSourceModule(this, parser);

			if (DLTKCore.DEBUG_PRINT_MODEL) {
				System.out.println("Source Module Debug print:"); //$NON-NLS-1$

				CorePrinter printer = new CorePrinter(System.out);
				printNode(printer);
				printer.flush();
			}

			// update timestamp (might be IResource.NULL_STAMP if original does
			// not exist)
			if (underlyingResource == null) {
				underlyingResource = getResource();
			}
			// underlying resource is null in the case of a working copy out of
			// workspace
			if (underlyingResource != null) {
				moduleInfo.setTimestamp(((IFile) underlyingResource)
						.getModificationStamp());
			}

			isConsistent = true;

			return moduleInfo.isStructureKnown();
		} catch (CoreException e) {
			throw new ModelException(e);
		}
	}

	private boolean isVirtualTypeResource(IResource fileSystemLoc) {
    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    	IResource resource = root.findMember(fileSystemLoc.getFullPath());
    	if (resource != null) {
    	    URI location = resource.getLocationURI();
        	if (location != null) {
        	    if(location.getScheme().equals("typespace")){
        	    	return true;
        	    }
        	}
    	}
		return false;
	}

	@Override
	public IModelElement[] getChildren(IProgressMonitor monitor)
			throws ModelException {

		return super.getChildren(monitor);
	}

	@Override
	public IModelElement[] getChildren() throws ModelException {
		// TODO Auto-generated method stub

		return super.getChildren();
	}

	@Override
	public boolean isConsistent() {
		return super.isConsistent() && isConsistent;
	}

	private void processType(char[] contents) {

		// fix bug 2206, add for findbugs NP warning, the contents might be
		// null.
		if (contents == null) {
			return;
		}

		m_typeProcessed = true;

		// String group = getGroupName();
		SourceTypeName typeName = getTypeName();
		String source = new String(contents);
		typeName.setSource(source);

		if (isConsistent) {
			jstType = parseAndResolve(typeName);
		} else {
			jstType = mgr.findType(typeName);
			if (jstType == null) {
				jstType = parseAndResolve(typeName);
			}
		}

	}

	private IJstType parseAndResolve(SourceTypeName typeName) {
		IJstType jstType = null;
		try {
			VjoParserToJstAndIType parser = new VjoParserToJstAndIType();
			if(VjetPlugin.TRACE_PARSER){
				System.out.println("parsing for " + getClass().getName());
			}
			IScriptUnit scriptUnit = parser.parse(typeName.groupName(),
					getTypeName().typeName(),  typeName
							.source());
			if (scriptUnit != null) {
				jstType = scriptUnit.getType();
			}
		} catch (Exception e) {
			DLTKCore.error(e.getMessage(), e);
		}
		return jstType;
	}

	public SourceTypeName getTypeName() {
		IResource path = this.getResource();
		return CodeassistUtils.getTypeName(path);
	}

	public String getGroupName() {
		IResource path = this.getResource();
		String group = path.getProject().getName();
		return group;
	}

	protected IBuffer getBuffer(IProgressMonitor progressMonitor,
			JSSourceModuleElementInfo moduleInfo) throws ModelException {
		// get buffer contents
		IBuffer buffer = getBufferManager().getBuffer(this);
		if (buffer == null) {
			buffer = openBuffer(progressMonitor, moduleInfo); // open
			// buffer
			// independently
			// from the info, since we are building the info
		}
		return buffer;
	}

	private void check(IResource underlyingResource) throws CoreException,
			ModelException {
		// check if this source module can be opened
		if (!isWorkingCopy()) // no check is done on root kind or
		{
			// exclusion
			// pattern for working copies
			IStatus status = validateSourceModule(underlyingResource);
			if (!status.isOK()) {
				throw newModelException(status);
			}
		}
		// prevents reopening of non-primary working copies (they are closed
		// when they are discarded and should not be reopened)
		if (preventReopen()) {
			// throw newNotPresentException();
		}
	}

	@Override
	public IType getType(String typeName) {
		return new VjoSourceType(this, typeName);
	}

	public void loadTypesFinished() {

		try {

			final NullProgressMonitor monitor = new NullProgressMonitor();
			isConsistent = false;
			reconcile(true, null, monitor);
			
			//added by huzhou@ebay.com
			//this logic is to find the dependents types of the saving jst type
			//and update their validations result accordingly
			try {
				if(jstType != null
						&& jstType.getPackage() != null
						&& jstType.getPackage().getGroupName() != null
						&& jstType.getName() != null){
					final List<IJstType> dependents = mgr.getTypeSpace().getAllDependents(new TypeName(jstType.getPackage().getGroupName(), jstType.getName()));
					if(dependents != null){
						final List<ISourceModule> selectedSourceModules = new LinkedList<ISourceModule>();
						final StandardScriptBuilder scriptBuild = new StandardScriptBuilder();
						final ScriptProject scriptProject = CodeassistUtils
								.getScriptProject(jstType.getPackage().getGroupName());
						VjoSourceHelper.getAllSourceModulesFromJst(selectedSourceModules, dependents, scriptProject);
						if(selectedSourceModules.size() > 0){
							scriptBuild.initialize(scriptProject);
							scriptBuild.buildModelElements(scriptProject, selectedSourceModules,
									new SubProgressMonitor(monitor, 1), 1);
						}
					}
				}
			} catch (Exception e) {
				DLTKCore.error(e.toString(), e);
			} 
			finally {
				monitor.done();
			}

		} catch (CoreException e) {
			DLTKCore.error(e.toString(), e);
		}
	}
	

	public IJstType getJstType() {
		if(jstType==null){
			jstType = mgr.getController().getJstTypeSpaceMgr().getQueryExecutor().findType(getTypeName());
		}
		return jstType;
	}

	public void refreshFinished(List<SourceTypeName> list) {
		if (list.contains(getTypeName())) {
			loadTypesFinished();
		}
	}

	public boolean refreshSourceFields() throws ModelException {
		if (jstType == null || !jstType.hasMixins()) { // only refresh types
														// with mixins
			return false;
		}

		ModelManager manager = ModelManager.getModelManager();

		// mixin types maybe changed, refresh the member fields
		try {
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

			return true;
		} catch (CoreException e) {
			throw new ModelException(e);
		}

	}
	
	public void updateScriptUnit(final JstBlock block,
			final List<JstBlock> blockList, final IJstType type,
			final List<IScriptProblem> probs) {
		this.m_block = block;
		this.m_blockList = blockList;
		this.jstType = type;
		this.m_probs = probs;
	}
	

	public IJstNode getNode(int startOffset) {
		return JstUtil.getLeafNode(jstType, startOffset, startOffset);
	}

	public JstBlock getSyntaxRoot() {
		return m_block;
	}

	public List<JstBlock> getJstBlockList() {
		return m_blockList;
	}

	public IJstType getType() {
		return jstType;
	}

	public List<IScriptProblem> getProblems() {
		return m_probs;
	}

//	@Override
//	public IUnitSource getUnitSource() {
//		return m_unitSource ;
//	}
//	
//	class VjoUnitSource implements IUnitSource {
//
//		@Override
//		public String getGroupName() {
//			return VjoSourceModule.this.getGroupName();
//		}
//
//		@Override
//		public String getSource() {
//			return VjoSourceModule.this.getSourceContents();
//		}
//
//		@Override
//		public String getUnitId() {
//			return getUnitName() + "@" + getGroupName();
//		}
//
//		@Override
//		public String getUnitName() {
//			return VjoSourceModule.this.getElementName();
//		}
//		
//	}

	

}
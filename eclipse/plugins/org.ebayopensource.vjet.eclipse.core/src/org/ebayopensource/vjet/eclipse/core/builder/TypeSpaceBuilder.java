/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.declaration.JstType;
import org.ebayopensource.dsf.jst.ts.JstTypeSpaceMgr;
import org.ebayopensource.dsf.jstojava.mixer.TypeExtensionRegistry;
import org.ebayopensource.dsf.jstojava.resolver.FunctionMetaRegistry;
import org.ebayopensource.dsf.jstojava.resolver.TypeResolverRegistry;
import org.ebayopensource.dsf.ts.event.EventListenerStatus;
import org.ebayopensource.dsf.ts.event.ISourceEventCallback;
import org.ebayopensource.dsf.ts.event.dispatch.IEventListenerHandle;
import org.ebayopensource.dsf.ts.event.group.AddGroupDependencyEvent;
import org.ebayopensource.dsf.ts.event.group.AddGroupEvent;
import org.ebayopensource.dsf.ts.event.group.BatchGroupLoadingEvent;
import org.ebayopensource.dsf.ts.event.group.IGroupEventListener;
import org.ebayopensource.dsf.ts.event.group.RemoveGroupDependencyEvent;
import org.ebayopensource.dsf.ts.event.group.RemoveGroupEvent;
import org.ebayopensource.vjet.eclipse.codeassist.CodeassistUtils;
import org.ebayopensource.vjet.eclipse.core.PiggyBackClassPathUtil;
import org.ebayopensource.vjet.eclipse.core.VjetPlugin;
import org.ebayopensource.vjet.eclipse.core.VjoLanguageToolkit;
import org.ebayopensource.vjet.eclipse.core.parser.VjoParserToJstAndIType;
import org.ebayopensource.vjet.eclipse.core.ts.EclipseTypeLoadMonitor;
import org.ebayopensource.vjet.eclipse.core.ts.TypeSpaceGroupLoadJob;
import org.ebayopensource.vjet.eclipse.core.ts.TypeSpaceRefreshJob;
import org.ebayopensource.vjet.eclipse.core.ts.TypeSpaceTracer;
import org.ebayopensource.vjo.lib.TsLibLoader;
import org.ebayopensource.vjo.tool.typespace.GroupInfo;
import org.ebayopensource.vjo.tool.typespace.SourcePathInfo;
import org.ebayopensource.vjo.tool.typespace.SourceTypeName;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.mod.core.DLTKContentTypeManager;
import org.eclipse.dltk.mod.core.DLTKCore;
import org.eclipse.dltk.mod.core.IBuildpathEntry;
import org.eclipse.dltk.mod.core.IScriptProject;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.dltk.mod.internal.core.Model;
import org.eclipse.dltk.mod.internal.core.ModelManager;
import org.eclipse.dltk.mod.internal.core.ScriptProject;

public class TypeSpaceBuilder {

	public TypeSpaceBuilder() {
	}

	public void buildProject(ScriptProject project, Map args,
			IProgressMonitor monitor, List<String> groupDepends, IBuildpathEntry bootstrapPath) {
		
		// add group event
		String name = project.getProject().getName();
		File groupPath = project.getProject().getLocation().toFile();
		SourcePathInfo sourceInfo = PiggyBackClassPathUtil
		.getProjectSrcPath_DLTK(project);
		List<String> srcPaths = sourceInfo.getSourcePaths();
		// TODO support library zip entries
		List<String> classPaths = new ArrayList<String>();
		
		List<String> bootstrapDirs = new ArrayList<String>();
		if(bootstrapPath!=null){
			String portableString = "";
			portableString = getRelativeProjectPath(project, bootstrapPath);
			bootstrapDirs.add(portableString);
		}
		List<GroupInfo> info = new ArrayList<GroupInfo>();
		processAddGroup(info, project.getProject());
		
		TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
//		// log list of groups being processed
		TypeSpaceTracer.logLoadEvent(info);
		mgr.load(new EclipseTypeLoadMonitor(monitor), info, null);

		
		
		
		
		if(VjetPlugin.TRACE_TYPESPACE){
			System.out.println("project: " + project);
			System.out.println("args: " + args);
			System.out.println("monitor: " + monitor);
			System.out.println("project buildpath: " + groupDepends);
			System.out.println("bootstrapPath: " + bootstrapPath);
		
			
		}
		
	}
	
	public void incrementalBuildProject(List<SourceTypeName> changedTypes) {

//		IResourceDelta[] resourceDeltas = delta
//				.getAffectedChildren();
//		TypeSpaceMgr tsmgr =  TypeSpaceMgr.getInstance();
//		List<GroupInfo> info = new ArrayList<GroupInfo>();
//		List<SourceTypeName> changedTypes = new ArrayList<SourceTypeName>();
//
//		for (IResourceDelta resourceDelta : resourceDeltas) {
//			IProject project = resourceDelta.getResource().getProject();
//
//			if (!project.exists() || !project.isOpen()) {
//				continue;
//			}
//
//			// process delta if project exist and type space loading is
//			// finished else create add group event.
//			if (tsmgr.existGroup(project.getName()) ) {
//				changedTypes.addAll(processDelta(resourceDelta));
//			} else if (tsmgr.isLoaded()) {
//				// for (Object o :
//				// m_tsmgr.getController().getJstTypeSpaceMgr().getTypeSpace().getGroups().keySet().toArray())
//				// {
//				// String s = (String)o;
//				// if (!s.endsWith(".jar")) {
//				// System.out.println(o);
//				// }
//				// }
//				// updateGroupDepends();
//				processAddGroup(info, project);
//			}
//
//		}
//
//		// Calls group load job if group info list in not empty else call
//		// refresh job for changes resources.
//		if (!info.isEmpty()) {
//
//			TypeSpaceGroupLoadJob groupLoadJob = new TypeSpaceGroupLoadJob(info);
//			tsmgr.setLoaded(false);
//			groupLoadJob.schedule();

		if(!changedTypes.isEmpty()) {
			TypeSpaceTracer.loadRefreshEvent(changedTypes);
			for(SourceTypeName type:changedTypes){
				TypeSpaceMgr.doProcessType(type, null);
				
			}
		}

	}
	
	public static void processAddGroup(List<GroupInfo> info, IProject project) {

		try {
			IScriptProject scriptProject;
			Model model =  ModelManager.getModelManager().getModel();
			scriptProject = model.getScriptProject(project.getName());
			if (scriptProject.exists()) {
				Map<String, List<String>> groups = new HashMap<String,List<String>>();
				createDepends(groups, scriptProject);
				populateGroupInfos(info, scriptProject, groups);
			}
		} catch (ModelException e) {
			DLTKCore.error(e.getMessage(), e);
		}
	}
	
	private static IBuildpathEntry[] createDepends(Map<String, List<String>> groups,
			IScriptProject project) throws ModelException {

		List<String> list = getDependsProjects(groups, project.getElementName());

		IBuildpathEntry[] entries = TypeSpaceBuilder.getSerFileGroupDepends(
				project, list);

		groups.put(project.getElementName(), list);

		return entries;
	}
	
	private static List<String> getDependsProjects(Map<String, List<String>> groups,
			String group) {
		List<String> list = groups.get(group);
		if (list == null) {
			list = new ArrayList<String>();
			groups.put(group, list);
		}
		return list;
	}

	private List<SourceTypeName> loadFile(IResourceDelta delta, IResource resource) {
		IFile file = (IFile) resource;
		
		SourceTypeName name = null;
		if (isValidFile(file)) {
			return loadVjoFile(delta, file);
		}
		return null;
	}
	
	private boolean isValidFile(IFile file) {
		return isValidName(file)
				&& PiggyBackClassPathUtil.isInSourceFolder(file);
	}
	
	private boolean isValidName(IFile file) {
		boolean isValid = false;
		try {
			isValid = DLTKContentTypeManager.isValidFileNameForContentType(
					VjoLanguageToolkit.getDefault(), file.getLocation());
		} catch (Exception e) {
			DLTKCore.error(e.getMessage(), e);
		}
		return isValid;
	}

	private SourceTypeName createSourceTypeName(IFile file) {
		byte[] content = SourceTypeName.EMPTY_CONTENT;

		content = getFileContent(file);

		return createSourceTypeName(file, content);
	}

	private byte[] getFileContent(IFile file) {
		byte[] bs = SourceTypeName.EMPTY_CONTENT;
		try {
			bs = doGetFileContent(file);
		} catch (IOException e) {
			logError(e);
		} catch (ResourceException e) {
			refresh(file);
			try {
				bs = doGetFileContent(file);
			} catch (Exception e1) {
				logError(e1);
			}
		} catch (CoreException e) {
			logError(e);
		}
		return bs;
	}
	
	/**
	 * Refresh file from the disk to workspace.
	 * 
	 * @param file
	 *            {@link IFile} object.
	 */
	private void refresh(IFile file) {
		try {
			file.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			DLTKCore.error(e.toString(), e);
		}
	}
	
	private void logError(Exception e1) {
		DLTKCore.error(e1.getMessage(), e1);
	}

	/**
	 * Read content of the {@link IFile} object to the bytes array.
	 * 
	 * @param file
	 *            {@link IFile}
	 * @return file content in bytes array.
	 * @throws CoreException
	 * @throws IOException
	 */
	private byte[] doGetFileContent(IFile file) throws CoreException,
			IOException {
		InputStream stream = file.getContents();
		int available = stream.available();
		byte[] bs = new byte[available];
		stream.read(bs);
		stream.close();
		return bs;
	}
	
	private SourceTypeName createSourceTypeName(IFile file, byte[] bs) {
		String group = file.getProject().getName();

		String source = new String(bs);
		String name = CodeassistUtils.getClassName(file);
		// SourceTypeName typeName = new SourceTypeName(group,
		// file.getRawLocation().toPortableString(), source);
		SourceTypeName typeName = new SourceTypeName(group, name, source);
		return typeName;
	}
	
	/**
	 * Create for {@link File} object corresponding {@link SourceTypeName}
	 * object and adding to the inner list of changed types.
	 * 
	 * @param delta
	 *            {@link IResourceDelta} object
	 * @param file
	 *            {@link File} object.
	 */
	private List<SourceTypeName> loadVjoFile(IResourceDelta delta, IFile file) {
		SourceTypeName name;
		List<SourceTypeName> changedTypes = new ArrayList<SourceTypeName>();
		TypeSpaceMgr tsmgr =  TypeSpaceMgr.getInstance();
		if (file.exists()) {
			name = createSourceTypeName(file);
		} else {
			name = createSourceTypeName(file, SourceTypeName.EMPTY_CONTENT);
		}

		String typeName = CodeassistUtils.getClassName(file);

		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			if (!tsmgr.existType(file.getProject().getName(), typeName)) {
				name.setAction(SourceTypeName.ADDED);
				changedTypes.add(name);
				if (file.exists()) {
					tsmgr.getController().parseAndResolve(name.groupName(),
							file.getLocation().toFile());
				}
			}
			break;
		case IResourceDelta.CHANGED:
			if (tsmgr.existType(file.getProject().getName(), typeName)) {
				name.setAction(SourceTypeName.CHANGED);
				changedTypes.add(name);
			}

			break;
		case IResourceDelta.REMOVED:
			if (tsmgr.existType(file.getProject().getName(), typeName)) {
				name.setAction(SourceTypeName.REMOVED);
				changedTypes.add(name);
			}
			break;
		default:
			break;
		}
		
		return changedTypes;
		// return !(delta.getKind() == IResourceDelta.ADDED &&
		// m_tsmgr.existType(file.getProject().getName(), typeName));

	}
	
	private List<SourceTypeName> processDelta(IResourceDelta delta) {
		IResource resource = delta.getResource();

		// fix typespace not updated after refactor finished
		List<SourceTypeName> changedTypes = new ArrayList<SourceTypeName>();
		if (resource instanceof IFile) {
			switch (delta.getKind()) {
			case IResourceDelta.CHANGED:
				if ((delta.getFlags() & ~(IResourceDelta.SYNC | IResourceDelta.MARKERS)) != 0) {
					changedTypes.addAll(loadFile(delta, resource));
				}
				break;
			case IResourceDelta.ADDED:
				changedTypes.addAll(loadFile(delta, resource));
				break;
			case IResourceDelta.REMOVED:
				changedTypes.addAll(loadFile(delta, resource));
				break;
			default:
				break;
			}
		} else if (PiggyBackClassPathUtil.isInSourceFolder(resource)
				|| resource instanceof IProject) {
			IResourceDelta[] deltas = delta.getAffectedChildren();
			for (IResourceDelta resourceDelta : deltas) {
				changedTypes.addAll(processDelta(resourceDelta));
			}
		}
		return changedTypes;

	}
	
	private static void populateGroupInfos(final List<GroupInfo> info,
			final IScriptProject project,
			final Map<String, List<String>> groupDependency)
			throws ModelException {
		
		
		// initialize dltk build path
		if (!PiggyBackClassPathUtil
				.ifScriptProjectInitializedFromJavaProject(project)) {
			PiggyBackClassPathUtil
					.initializeScriptProjectFromJavProject(project);
		}
		// initialize dependant jars

		List<String> classPaths = new ArrayList<String>();

		List<URL> urls = PiggyBackClassPathUtil
				.getProjectDependantJars_DLTK(project);

		List<String> groupDepends = new ArrayList<String>();
		TypeSpaceBuilder.getSerFileGroupDepends(project, groupDepends);

		IBuildpathEntry bootstrapPath = TypeSpaceBuilder
				.getBootstrapDir(project);

		for (URL u : urls) {
			java.io.File file = new java.io.File(u.getFile());
			String fileName = file.getName();
			// Skip the file which has been calculated into type space
			if (TypeSpaceMgr.getInstance().existGroup(fileName)) {
				continue;
			}
			// Check if the jar has been added into list
			if (!classPaths.contains(fileName)) {
				SourcePathInfo srcPathInfo = new SourcePathInfo();
				srcPathInfo.addSourcePath(file.getPath());
				

				info.add(new GroupInfo(file.getName(), file.getAbsolutePath(),
						srcPathInfo, null, groupDependency.get(file.getName())));
				classPaths.add(fileName);
				System.out.println("ScriptProject<" + project.getElementName()
						+ "> depends on :" + u);
			}
		}
		// initialize src path
		SourcePathInfo sourcePathInfo = PiggyBackClassPathUtil
				.getProjectSrcPath_DLTK(project);
		List<String> srcPaths = sourcePathInfo.getSourcePaths();

		String name = project.getProject().getName();
		java.io.File groupPath = project.getProject().getLocation().toFile();

		List<String> bootstrapDirs = new ArrayList<String>();
		if (bootstrapPath != null) {
			String portableString = "";
			portableString = getRelativeProjectPath(project, bootstrapPath);
			bootstrapDirs.add(portableString);
		}
		info.add(new GroupInfo(name, groupPath.getAbsolutePath(), sourcePathInfo,
				classPaths, groupDependency.get(name), bootstrapDirs));
	}

	
	private static String getRelativeProjectPath(IScriptProject project, IBuildpathEntry bootstrap) {
		String name = project.getElementName();
		String bootstrapPath = bootstrap.getPath().toPortableString();
		if (bootstrapPath.lastIndexOf(name) != -1) {
			if (bootstrapPath.equals(name)) {
				bootstrapPath = "";
			} else {
				bootstrapPath = bootstrapPath.substring(bootstrapPath
						.indexOf(name)
						+ name.length());

			}
		}
		return bootstrapPath;
	}
	
	
	public static IBuildpathEntry getBootstrapDir(IScriptProject project) throws ModelException {
		IBuildpathEntry[] entries = project.getRawBuildpath();
		for (IBuildpathEntry entry : entries) {
			if(entry.getEntryKind() == IBuildpathEntry.BPE_BOOTSTRAP){
				return entry;
			}
		}
		return null;
	}
	
	public static IBuildpathEntry[] getSerFileGroupDepends(IScriptProject project,
			List<String> list) throws ModelException {
		IBuildpathEntry[] entries = project.getRawBuildpath();
		for (IBuildpathEntry entry : entries) {
			// process only project entries
			if (entry.getEntryKind() == IBuildpathEntry.BPE_PROJECT) {
				// add first segment of the project entry - project name.
				list.add(entry.getPath().segment(0));
				// add transitive project dependencies
				
				getSerFileGroupDepends(CodeassistUtils.getScriptProject(entry.getPath().segment(0)), list);
				
				
			} else if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY){
				String id = entry.getPath().segment(entry.getPath().segmentCount()-1);
				list.add(id);
				
			} else if (entry.getEntryKind() == IBuildpathEntry.BPE_CONTAINER) {
				String id = entry.getPath().segment(0);
				
				if (org.ebayopensource.vjet.eclipse.core.VjetPlugin.VJETTL_ID.equals(id)) {
					//add built in libs
					list.add(entry.getPath().lastSegment());
				
				}
				
				
				if (org.ebayopensource.vjet.eclipse.core.VjetPlugin.VJOLIB_ID.equals(id)) {
					//add built in libs
					String[] defaultLibs = TsLibLoader.getVjoGroups();
					for (int i = 0; i < defaultLibs.length; i++) {
						list.add(defaultLibs[i]);
					}
				}
				if (org.ebayopensource.vjet.eclipse.core.VjetPlugin.BROWSERSDK_ID.equals(id)) {
					//add built in libs
					String[] defaultLibs = TsLibLoader.getBrowserGroups();
					for (int i = 0; i < defaultLibs.length; i++) {
						list.add(defaultLibs[i]);
					}
				}
				if (org.ebayopensource.vjet.eclipse.core.VjetPlugin.JSNATIVESDK_ID.equals(id)) {
					//add built in libs
					String[] defaultLibs = TsLibLoader.getJsNativeGroups();
					for (int i = 0; i < defaultLibs.length; i++) {
						list.add(defaultLibs[i]);
					}
				}
				if (org.ebayopensource.vjet.eclipse.core.VjetPlugin.SDK_CONTAINER .equals(id)) {
					//add built in libs
					String[] defaultLibs = TsLibLoader.getDefaultLibNames();
					for (int i = 0; i < defaultLibs.length; i++) {
						list.add(defaultLibs[i]);
					}
				}
			}
		}
		return entries;
	}
	
	public static void addGroupTraceEventListeners(JstTypeSpaceMgr jstTypeSpace){
		jstTypeSpace.registerSourceEventListener(new IGroupEventListener<IJstType>() {

			@Override
			public EventListenerStatus<IJstType> onBatchGroupLoaded(
					BatchGroupLoadingEvent event,
					IEventListenerHandle handle,
					ISourceEventCallback callBack) {
				System.out.println("on batch group add dependendency");
				TypeSpaceTracer.onBatchLoaded(event);
				return null;
			}

			@Override
			public EventListenerStatus onGroupAddDependency(
					AddGroupDependencyEvent event,
					IEventListenerHandle handle,
					ISourceEventCallback callBack) {
				// TODO Auto-generated method stub
				System.out.println("onGroupAddDependency for group:" + event.getGroupName());
				System.out.println("\tgroup path: " + event.getGroupPath());
				System.out.println("\tdependency list: " + event.getDependencyList());
				return null;
			}

			@Override
			public EventListenerStatus onGroupAdded(AddGroupEvent event,
					IEventListenerHandle handle,
					ISourceEventCallback callBack) {
				// TODO Auto-generated method stub
				System.out.println("on group added");
				System.out.println("bootstrap paths:" + event.getBootStrapList());
				
				System.out.println(event);
				return null;
			}

			@Override
			public EventListenerStatus onGroupRemoveDependency(
					RemoveGroupDependencyEvent event,
					IEventListenerHandle handle,
					ISourceEventCallback callBack) {
				System.out.println("on group remove dependency");
				System.out.println(event);
				return null;
			}

			@Override
			public void onGroupRemoved(RemoveGroupEvent event) {
				// TODO Auto-generated method stub
				System.out.println("on group removed");
				System.out.println(event);
			}
			
		});
	}

	public static void addGroupEventListeners(JstTypeSpaceMgr jstTypeSpaceMgr) {
		jstTypeSpaceMgr.registerSourceEventListener(new IGroupEventListener<JstType>(){

			@Override
			public EventListenerStatus<JstType> onBatchGroupLoaded(
					BatchGroupLoadingEvent event, IEventListenerHandle handle,
					ISourceEventCallback<JstType> callBack) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public EventListenerStatus<JstType> onGroupAddDependency(
					AddGroupDependencyEvent event, IEventListenerHandle handle,
					ISourceEventCallback<JstType> callBack) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public EventListenerStatus<JstType> onGroupAdded(
					AddGroupEvent event, IEventListenerHandle handle,
					ISourceEventCallback<JstType> callBack) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public EventListenerStatus<JstType> onGroupRemoveDependency(
					RemoveGroupDependencyEvent event,
					IEventListenerHandle handle,
					ISourceEventCallback<JstType> callBack) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void onGroupRemoved(RemoveGroupEvent event) {
				// remove the bootstrap entries for the group
				String groupId = event.getGroupName();
				TypeResolverRegistry.getInstance().clear(groupId);
				TypeExtensionRegistry.getInstance().clear(groupId);
				FunctionMetaRegistry.getInstance().clear(groupId);
			}
			
		});
		
		
	}

	
	
}

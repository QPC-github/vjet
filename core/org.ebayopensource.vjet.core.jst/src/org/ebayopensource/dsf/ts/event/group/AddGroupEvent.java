/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.ts.event.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ebayopensource.dsf.ts.event.ISourceEvent;
import org.ebayopensource.dsf.ts.event.ISourceEventCallback;
import org.ebayopensource.dsf.ts.event.ISourceEventListener;
import org.ebayopensource.dsf.ts.event.dispatch.IEventListenerHandle;


/**
 * Add group event. 
 * This event is fired by IDE-plugin (JST container) as response to adding new project.
 * 
 * 
 */
public final class AddGroupEvent extends GroupEvent {
	
	private final List<String> m_classPathList;
	private final List<String> m_srcPathList;
	private final List<String> m_directDependency;
	private List<String> m_bootstrapList;
	private List<String> m_srcPathExclusionPatterns;
	private List<String> m_srcPathInclusionPatterns;

	//
	// Constructors
	//
	/**
	 * Constructor
	 * @param groupName String - The project or library name
	 * @param groupPath String - The path. This is opaque string for JST and is ony interpreted by the IDE itself.
	 */
	public AddGroupEvent(String groupName, String groupPath){
		this(groupName, groupPath, null);
	}
	
	/**
	 * Constructor
	 * @param groupName String - The project or library name
	 * @param groupPath String - The project or library path
	 * @param srcPath String - src path of JS source files in this group
	 * @param classPaths List<String> - list of classpaths used by this group
	 */
	public AddGroupEvent(String groupName, String groupPath, String srcPath, List<String> classPaths){
		this(groupName, groupPath, srcPath, classPaths, null);
	}
	
	/**
	 * Constructor
	 * @param groupName String - The project or library name
	 * @param groupPath String - The project or library path
	 * @param srcPathList List<String> - list of src path of JS source files in this group
	 * @param classPaths List<String> - list of classpaths used by this group
	 */
	public AddGroupEvent(String groupName, String groupPath, List<String> srcPathList, List<String> classPaths){
		this(groupName, groupPath, srcPathList, classPaths, null);
	}
	
	/**
	 * Constructor
	 * @param groupName String - The project or library name
	 * @param groupPath String - The path. This is opaque string for JST and is ony interpreted by the IDE itself.
	 * @param directDependency - the projects/libraries the group depends on
	 */
	public AddGroupEvent(final String groupName, final String groupPath, final List<String> directDependency){
		super(groupName, groupPath);
		m_srcPathList = null;
		m_classPathList = null;
		m_directDependency = directDependency;
	}
	
	/**
	 * Constructor
	 * @param groupName String - The project or library name
	 * @param groupPath String - The project or library path
	 * @param srcPath String - src path of JS source files in this group
	 * @param classPaths List<String> - list of classpaths used by this group
	 * @param directDependency - the projects/libraries the group depends on
	 */
	public AddGroupEvent(final String groupName, final String groupPath, final String srcPath, 
			final List<String> classPaths, final List<String> directDependency){
		super(groupName, groupPath);
		m_srcPathList = new ArrayList<String>(1);
		m_srcPathList.add(srcPath);
		m_classPathList = classPaths;
		m_directDependency = directDependency;
	}
	
	/**
	 * Constructor
	 * @param groupName String - The project or library name
	 * @param groupPath String - The project or library path
	 * @param srcPathList List<String> - list of src path of JS source files in this group
	 * @param classPaths List<String> - list of classpaths used by this group
	 * @param directDependency - the projects/libraries the group depends on
	 */
	public AddGroupEvent(final String groupName, final String groupPath, final List<String> srcPathList, 
			final List<String> classPaths, final List<String> directDependency){
		super(groupName, groupPath);
		m_srcPathList = srcPathList;
		m_classPathList = classPaths;
		m_directDependency = directDependency;
	}
	
	/**
	 * Constructor
	 * @param groupName String - The project or library name
	 * @param groupPath String - The project or library path
	 * @param srcPathList List<String> - list of src path of JS source files in this group
	 * @param classPaths List<String> - list of classpaths used by this group
	 * @param directDependency - the projects/libraries the group depends on
	 * @param bootstrapList - the bootstrap source directories for adding extensions
	 * @param inclusionList - List of patterns which should be included under source paths
	 * @param exclusionList - List of patterns which should be excluded under source paths
	 * @param list2 
	 * @param list 
	 */
	public AddGroupEvent(final String groupName, final String groupPath, final List<String> srcPathList, 
			final List<String> classPaths, final List<String> directDependency, List<String> bootstrapList, List<String> inclusionList, List<String> exclusionList){
		super(groupName, groupPath);
		m_srcPathList = srcPathList;
		m_classPathList = classPaths;
		m_directDependency = directDependency;
		m_bootstrapList = bootstrapList;
		m_srcPathExclusionPatterns = exclusionList;
		m_srcPathInclusionPatterns = inclusionList;
	}
	
	public AddGroupEvent(final String groupName, final String groupPath, final List<String> srcPathList, 
			final List<String> classPaths, final List<String> directDependency, List<String> bootstrapList){
		this(groupName,groupPath,srcPathList,classPaths,directDependency,bootstrapList,null,null);
	}
	
	/**
	 * Add a single source folder to the list of src paths
	 * @param srcPath
	 */
	public boolean addSrcPath(String srcPath) {
		if (m_srcPathList != null) {
			return m_srcPathList.add(srcPath);
		}
		
		return false;		
	}
	
	public boolean addSrcPathExclusionRule(String excludePattern){
		if(m_srcPathExclusionPatterns!=null){
			return m_srcPathExclusionPatterns.add(excludePattern);
		}
		return false;
	}
	
	/**
	 * Add a single class path to the list of class paths
	 * @param srcPath
	 * @return
	 */
	public boolean addClassPath(String classPath) {
		if (m_classPathList != null) {
			return m_classPathList.add(classPath);
		}
		
		return false;
		
	}
	//
	// Satisfy IJstEvent
	//
	/**
	 * @see ISourceEvent#dispatch(ISourceEventListener)
	 */
	public void dispatch(ISourceEventListener listener){
		if (listener == null){
			return;
		}
		((IGroupEventListener)listener).onGroupAdded(this, null, null);
	}
	
	/**
	 * @see ISourceEvent#dispatch(ISourceEventListener,IEventListenerHandle,ISourceEventCallback)
	 */
	public void dispatch(ISourceEventListener listener, IEventListenerHandle handle, ISourceEventCallback callback){
		if (listener == null){
			return;
		}
		((IGroupEventListener)listener).onGroupAdded(this, handle, callback);
	}
	
	/**
	 * Answer the path of the group source folder where JS files are under
	 * @return String
	 */
	public String getSourcePath(){
		if (m_srcPathList != null && m_srcPathList.size() > 0) {
			return m_srcPathList.get(0);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Answer the list of paths of the group source folders where JS files are under
	 * @return String
	 */
	public List<String> getSourcePathList(){
		return m_srcPathList;
	}
	
	
	/**
	 * Answer the list of paths of the group bootstrap folders where special bootstrap extensions exist
	 * @return String
	 */
	public List<String> getBootStrapList(){
		return m_bootstrapList;
	}
	
	/**
	 * Answer the class path where dependencies are defined for the group
	 * @return String
	 */
	public List<String> getClassPath(){
		return m_classPathList;
	}
	
	/**
	 * Answer an unmodifiable list of direct dependency
	 * @return List<String>
	 */
	public List<String> getDirectDependency(){
		if (m_directDependency == null){
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(m_directDependency);
	}
	
	public boolean shouldLock() { return false; }

	public List<String> getSrcPathInclusionPatterns() {
		return m_srcPathInclusionPatterns;
	}

	public List<String> getSrcPathExclusionPatterns() {
		return m_srcPathExclusionPatterns;
	}
	public void setSrcPathInclusionPatterns(
			List<String> m_srcPathInclusionPatterns) {
		this.m_srcPathInclusionPatterns = m_srcPathInclusionPatterns;
	}
}

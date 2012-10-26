/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjo.tool.typespace;

import java.util.Collections;
import java.util.List;

/**
 * This class contains information about group : name, source path, class path
 * and group path.
 * 
 * 
 * 
 */
public class GroupInfo {

	private String m_groupName;
	private SourcePathInfo m_srcPath;
	private List<String> m_classPath;
	private String m_groupPath;
	private List<String> m_directDependency;
	private List<String> m_bootstrapPath;
	
	public GroupInfo(String grpName, String groupPath, SourcePathInfo srcPath,
			List<String> classPath, final List<String> directDependency) {
		this(grpName, groupPath,  srcPath,
			 classPath,  directDependency, null);
	}
	public GroupInfo(String grpName, String groupPath, SourcePathInfo srcPath,
			List<String> classPath, final List<String> directDependency, List<String> bootStrapPath) {
		m_groupName = grpName;
		m_srcPath = srcPath;
		m_classPath = classPath;
		m_groupPath = groupPath;
		m_directDependency = directDependency;
		m_bootstrapPath = bootStrapPath;
	}

	public String getGroupName() {
		return m_groupName;
	}

	public void setGroupName(String groupName) {
		m_groupName = groupName;
	}

	public SourcePathInfo getSrcPath() {
		return m_srcPath;
	}

	public void setSrcPath(SourcePathInfo srcPath) {
		m_srcPath = srcPath;
	}

	public List<String> getClassPath() {
		return m_classPath;
	}

	public void setClassPath(List<String> classPath) {
		m_classPath = classPath;
	}

	public String getGroupPath() {
		return m_groupPath;
	}

	public void setGroupPath(String groupPath) {
		m_groupPath = groupPath;
	}
	
	public List<String> getBootstrapPath() {
		return m_bootstrapPath;
	}
	public void setBootstrapPath(List<String> bootstrapPath) {
		m_bootstrapPath = bootstrapPath;
	}
	
	/**
	 * Answer an unmodifiable list of direct dependency.
	 * @return List<String>
	 */
	public List<String> getDirectDependency(){
		if (m_directDependency == null){
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(m_directDependency);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("GroupInfo[");
		buffer.append("m_groupName = ").append(m_groupName);
		buffer.append(" m_groupPath = ").append(m_groupPath);
		buffer.append(" m_srcPath = ").append(m_srcPath);
		if (m_directDependency != null && !m_directDependency.isEmpty()){
			buffer.append(" directDependency = [");
			for (String d: m_directDependency){
				buffer.append(d).append(",");
			}
			buffer.append("]");
		}
		buffer.append("]");
		return buffer.toString();
	}

}

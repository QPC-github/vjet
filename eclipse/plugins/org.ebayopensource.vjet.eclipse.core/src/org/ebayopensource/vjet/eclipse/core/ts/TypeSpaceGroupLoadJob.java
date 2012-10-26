/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.ts;

import java.util.List;

import org.ebayopensource.vjo.tool.typespace.GroupInfo;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Job for loading group to type space.
 * 
 * 
 * 
 */
public class TypeSpaceGroupLoadJob extends WorkspaceJob {


	private static final String TYPE_SPACE_GROUP_LOADING = "Type Space group loading";

	private List<GroupInfo> m_list;

	public TypeSpaceGroupLoadJob(List<GroupInfo> list) {
		super(TYPE_SPACE_GROUP_LOADING);
		this.m_list = list;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
//		TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
		// log list of groups being processed
//		TypeSpaceTracer.logLoadEvent(m_list);
//		mgr.load(new EclipseTypeLoadMonitor(monitor), m_list, null);
		return Status.OK_STATUS;
	}

	

}

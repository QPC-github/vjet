/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.ts;

import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.mod.core.DLTKCore;

/**
 * Job to reloading all types to the type space
 * 
 * 
 *
 */
public class TypeSpaceReloadJob extends WorkspaceJob {

	private static final String TYPE_SPACE_RELOAD = "type space reload";
	private static final String TYPE_SPACE_UPDATE = "type space update project";
	private IProject m_project;

	public TypeSpaceReloadJob() {
		super(TYPE_SPACE_RELOAD);
	}

	public TypeSpaceReloadJob(IProject project) {
		super(TYPE_SPACE_UPDATE);
		m_project = project;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
//		TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
//		try {
//			TypeSpaceTracer.loadReloadEvent(mgr);
//			if(m_project!=null){
//				mgr.reloadGroup(new EclipseTypeLoadMonitor(monitor), m_project.getName(), null);
//				m_project.build(IncrementalProjectBuilder.FULL_BUILD, new SubProgressMonitor(monitor,1));
//			}else{
//				mgr.reload(new EclipseTypeLoadMonitor(monitor), null);
//			}
//			
//		} catch (Exception e) {
//			DLTKCore.error(e.getMessage(), e);
//		}
		return Status.OK_STATUS;
	}

}

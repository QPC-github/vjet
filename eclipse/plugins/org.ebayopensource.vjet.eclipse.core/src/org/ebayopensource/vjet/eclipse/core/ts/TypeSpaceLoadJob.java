/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.ts;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.ts.event.EventListenerStatus;
import org.ebayopensource.dsf.ts.event.ISourceEventCallback;
import org.ebayopensource.vjet.eclipse.core.VjetPlugin;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceLoadEvent;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.mod.core.DLTKCore;

/**
 * Job to loading all types of the workspace to type space.
 * 
 * 
 * 
 */
public class TypeSpaceLoadJob extends WorkspaceJob {

	private static final String TYPE_SPACE_LOADING = "Type Space loading";

	public TypeSpaceLoadJob() {
		super(TYPE_SPACE_LOADING);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
//		TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
//		try {
//			mgr
//					.load(new EclipseTypeLoadMonitor(monitor),
//							getErrorLogCallback());
//		} catch (Exception e) {
//			e.printStackTrace();
//			DLTKCore.error(e.getMessage(), e);
//		}
		return Status.OK_STATUS;
	}

	private ISourceEventCallback<IJstType> getErrorLogCallback() {
		
		return InnerErrorLogCallBack.getInstance();
	}

}

class InnerErrorLogCallBack implements ISourceEventCallback<IJstType> {
	
	private static ISourceEventCallback<IJstType> m_instance;

	public static ISourceEventCallback<IJstType> getInstance() {
		if (m_instance == null) {
			m_instance = new InnerErrorLogCallBack();
		}
		return m_instance;
	}

	public void onComplete(EventListenerStatus<IJstType> status) {
		  if (status.getCode() == EventListenerStatus.Code.Failed
		    || status.getCode() == EventListenerStatus.Code.Exception) {
		   String messagesDetail = TypeSpaceLoadEvent.getErrorString(status);
		   String message = status.getMsg();
		   if (message == null) {
		    message = "";
		   }
		   if (messagesDetail == null) {
		    messagesDetail = "";
		   }
		   VjetPlugin.error("Encounted exception when loading type space: "
		     + message + "{" + messagesDetail + "}", IStatus.WARNING);
		  }
		 }

	@Override
	public void onProgress(float percentage) {
		// do nothing
	}

}

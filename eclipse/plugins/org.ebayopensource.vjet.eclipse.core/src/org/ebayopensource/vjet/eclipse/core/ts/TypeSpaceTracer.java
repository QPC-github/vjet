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

import org.ebayopensource.dsf.ts.event.group.BatchGroupLoadingEvent;
import org.ebayopensource.vjet.eclipse.core.VjetPlugin;
import org.ebayopensource.vjo.tool.typespace.GroupInfo;
import org.ebayopensource.vjo.tool.typespace.SourceTypeName;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class TypeSpaceTracer {

	private static final boolean DEBUG_TS = "true"
		.equalsIgnoreCase(Platform
				.getDebugOption("org.ebayopensource.vjet.eclipse.core/typespace"));

	
	public static void logLoadEvent(List<GroupInfo> groups) {
		
		if(!DEBUG_TS){
			return;
		}
		
		StringBuilder listOfProjects = new StringBuilder();
		System.out.println("==================");
		System.out.println("Loading Groups:");
		for(GroupInfo p:groups){
			println(p.getGroupName());
			print("\t");
			println("bootstrap path:"+ p.getBootstrapPath());
			print("\t");
			println("direct group dependencies: " );
			for(String dd:p.getDirectDependency()){
				print("\t");
				println(dd);
			}
				
			listOfProjects.append(p.getGroupName()).append(" ");
			
		}
		System.out.println("==================");
		
		VjetPlugin.getDefault().getLog().log(new Status(IStatus.INFO, VjetPlugin.PLUGIN_ID,
				IStatus.INFO, TypeSpaceGroupLoadJob.class.getName() + " List of projects: " + listOfProjects, null));
		
	}
	
	static void print(Object obj){
		System.out.print(obj);
	}
	static void println(Object obj){
		System.out.println(obj);
	}

	public static void loadRefreshEvent(List<SourceTypeName> changedTypes) {
	
		if(!DEBUG_TS){
			return;
		}
		System.out.println("==================");
		System.out.println("Refreshing Types:");
		for(SourceTypeName type:changedTypes){
			println(type);
			
		}
		System.out.println("==================");
		
	}

	public static void onBatchLoaded(BatchGroupLoadingEvent event) {
		if(!DEBUG_TS){
			return;
		}
		
		System.out.println("onBatchLoaded for groups" + event.getAllGroups());
	}

	public static void loadReloadEvent(TypeSpaceMgr mgr) {
		if(!DEBUG_TS){
			return;
		}
		System.out.println("==================");
		System.out.println("loadReloadEvent:");
//		for(SourceTypeName type:changedTypes){
//			println(type);
//			
//		}
		System.out.println("==================");
		
	}
	
}

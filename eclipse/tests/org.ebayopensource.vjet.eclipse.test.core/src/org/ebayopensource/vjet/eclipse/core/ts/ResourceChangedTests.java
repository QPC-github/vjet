/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.ts;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.ts.type.TypeName;
import org.ebayopensource.vjet.eclipse.core.test.parser.AbstractVjoModelTests;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;

public class ResourceChangedTests extends AbstractVjoModelTests {

	
	public void setUpSuite() {
		
		IProject project = getWorkspaceRoot().getProject(getTestProjectName());
		if (!project.exists()) {
			super.setUpSuite();
		}
		
		TypeSpaceMgr.getInstance().setAllowChanges(true);
	}
	
	public void testDeleteFile() throws CoreException {
		
		changeResource("File","Delete","src/test/TestImps.js");
		
	}
	
	public void testDeleteFolder() throws CoreException {
		
		changeResource("Folder","Delete","src/test");
		
	}
	
	
	private void changeResource(String target,String operation, String res) throws CoreException {
		try {
			setUpSuite();
			IProject iProj = getProject(getTestProjectName());
			//IProject iProj = setUpProjectTo("TestProject", "TestProject");
			//sleep to wait for the group to be loaded into type space
			 buildAndWaitForEnd();
			
			TypeName typeName = new TypeName(getTestProjectName(), "test.TestImps");
			//check if the type exists in type space before the deletion
			IJstType removedType = TypeSpaceMgr.getInstance().findType(typeName);
		
			assertTrue(removedType != null);
			
			//change the resource
			TargetResourceSelectionHandler targetHandler = TARGET_REGISTRY.get(target);
			IResource resource = targetHandler.getResource(iProj, res);
			
			ResourceChangeOperationHandler operationHandler =  OPERATION_REGISTRY.get(operation);
			operationHandler.change(resource);
			//sleep to wait for the deletion from type space to complete @see JstEventListener#onTypeRemoved()
		   incrementalBuildAndWaitForEnd();
			
			
			//check if the type is removed from type space after the deletion
			removedType = TypeSpaceMgr.getInstance().findType(typeName);
			assertTrue(removedType == null);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally{
			deleteProject(getTestProjectName());
		}
	}

	//------------------------Target Resource Selection--------------------------
	interface TargetResourceSelectionHandler{
		
		public IResource getResource(IProject proj, String res);
	}
	
	static class FolderChangeHandler implements TargetResourceSelectionHandler{
		
		public IResource getResource(IProject proj, String res){
			return proj.getFolder(res);
		}
	}
	
	static class FileChangeHandler implements TargetResourceSelectionHandler{
		public IResource getResource(IProject proj, String res){
			return proj.getFile(res);
		}
	}
	
	//TARGET_REGISTRY
	private static Map<String,TargetResourceSelectionHandler> TARGET_REGISTRY = new HashMap<String,TargetResourceSelectionHandler>();
	static{
		TARGET_REGISTRY.put("File", new FileChangeHandler());
		TARGET_REGISTRY.put("Folder", new FolderChangeHandler());
	}
	//------------------------Resource Change Operation--------------------------
	interface ResourceChangeOperationHandler{
		
		public void change(IResource resource) throws CoreException;
	}
	
	private static Map<String,ResourceChangeOperationHandler> OPERATION_REGISTRY = new HashMap<String,ResourceChangeOperationHandler>();
	static{
		OPERATION_REGISTRY.put("Delete", new DeleteHandler());
	}
	
	static class DeleteHandler  implements ResourceChangeOperationHandler {
		
		public void change(IResource resource) throws CoreException{
			
			resource.delete(true, null);
		}
	}
	
}

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

import org.ebayopensource.dsf.jst.BaseJstNode;
import org.ebayopensource.dsf.jst.IJstMethod;
import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.IJstProperty;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.expr.FieldAccessExpr;
import org.ebayopensource.dsf.jst.expr.MtdInvocationExpr;
import org.ebayopensource.dsf.jst.stmt.ExprStmt;
import org.ebayopensource.dsf.jst.term.JstIdentifier;
import org.ebayopensource.dsf.ts.type.TypeName;
import org.ebayopensource.vjet.eclipse.core.test.parser.AbstractVjoModelTests;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;

public class ExpressionTypeLinkerTests extends AbstractVjoModelTests {
	
	private static boolean isFirstRun = true;
	
	private IJstType linkerC;
	private IJstType linkerB;
	private IJstType linkerA;
	
	private TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
	
	 @Before
	public void setUp() {
		setWorkspaceSufix("1");
		IProject project = getWorkspaceRoot().getProject(getTestProjectName());

		if (isFirstRun) {
			try {
				super.deleteResource(project);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			super.setUpSuite();			
			isFirstRun = false;
		}
		TypeName tn = new TypeName(getTestProjectName(), "ETLinkerB.LinkerB");
		linkerB = mgr.findType(tn);
		tn = new TypeName(getTestProjectName(), "ETLinkerA.LinkerA");
		linkerA = mgr.findType(tn);
		tn = new TypeName(getTestProjectName(), "ETLinkerB.LinkerC");
		linkerC = mgr.findType(tn);
		
		try {
			super.setUp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testThisField() {
		IJstProperty s = this.linkerB.getProperty("s", false);
		IJstMethod func = this.linkerB.getInstanceMethod("func");
		List<BaseJstNode> children = func.getBlock().getChildren();
		ExprStmt expr = (ExprStmt) children.get(1);
		FieldAccessExpr thisS = (FieldAccessExpr) expr.getChildren().get(0);
		assertEquals(s, thisS.getName().getJstBinding());
	}
	
	public void testThisMethod() {
		IJstMethod func = this.linkerB.getInstanceMethod("func");
		List<BaseJstNode> children = func.getBlock().getChildren();
		MtdInvocationExpr thisFunc = (MtdInvocationExpr) children.get(2);
		IJstNode binding = ((JstIdentifier) thisFunc.getMethodIdentifier()).getJstBinding();
		assertEquals(func, binding);
	}
	
	public void testThisBaseField() {		
		//mgr.reload(null);
		IJstProperty s = this.linkerA.getProperty("s", false);
		IJstMethod func = this.linkerB.getInstanceMethod("func");
		List<BaseJstNode> children = func.getBlock().getChildren();
		
		ExprStmt expr = (ExprStmt) children.get(3);
		FieldAccessExpr baseS = (FieldAccessExpr) expr.getChildren().get(0);		
		assertEquals(s, baseS.getName().getJstBinding());
	}
	
	public void testThisBaseMethod() {
		IJstMethod func = this.linkerB.getInstanceMethod("func");
		IJstMethod funcA = this.linkerA.getInstanceMethod("func");
		List<BaseJstNode> children = func.getBlock().getChildren();
		MtdInvocationExpr baseFunc = (MtdInvocationExpr) children.get(4);
		IJstNode binding = ((JstIdentifier) baseFunc.getMethodIdentifier()).getJstBinding();
		assertEquals(funcA, binding);
	}
	
	public void testMethodReturnType() {
		IJstMethod func = this.linkerB.getInstanceMethod("func");
		IJstProperty c = this.linkerC.getProperty("c", false);
		List<BaseJstNode> children = func.getBlock().getChildren();
		ExprStmt expr = (ExprStmt) children.get(5);
		FieldAccessExpr cc = (FieldAccessExpr) expr.getChildren().get(0);
		assertEquals(c, cc.getName().getJstBinding());
	}
	
	public void testFieldType() {
		IJstMethod func = this.linkerB.getInstanceMethod("func");
		IJstProperty c = this.linkerC.getProperty("c", false);
		List<BaseJstNode> children = func.getBlock().getChildren();
		
		ExprStmt expr = (ExprStmt) children.get(5);
		FieldAccessExpr fc = (FieldAccessExpr) expr.getChildren().get(0);		
		assertEquals(c, fc.getName().getJstBinding());
	}
	

}

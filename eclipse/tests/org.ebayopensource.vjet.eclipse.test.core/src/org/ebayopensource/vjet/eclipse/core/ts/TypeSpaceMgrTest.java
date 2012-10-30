/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.ts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.ebayopensource.dsf.jsgen.shared.generate.CodeStyle;
import org.ebayopensource.dsf.jsgen.shared.vjo.GeneratorCtx;
import org.ebayopensource.dsf.jsgen.shared.vjo.VjoGenerator;
import org.ebayopensource.dsf.jst.IJstMethod;
import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jstojava.parser.SyntaxTreeFactory2;
import org.ebayopensource.dsf.ts.event.EventListenerStatus;
import org.ebayopensource.dsf.ts.event.ISourceEvent;
import org.ebayopensource.dsf.ts.method.MethodName;
import org.ebayopensource.dsf.ts.property.PropertyName;
import org.ebayopensource.dsf.ts.type.TypeName;
import org.ebayopensource.vjet.eclipse.codeassist.CodeassistUtils;
import org.ebayopensource.vjet.eclipse.core.search.TypeSearcher;
import org.ebayopensource.vjet.eclipse.core.test.parser.AbstractVjoModelTests;
import org.ebayopensource.vjo.tool.typespace.SourceTypeName;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceListener;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.mod.core.IBuildpathAttribute;
import org.eclipse.dltk.mod.core.IBuildpathEntry;
import org.eclipse.dltk.mod.core.IProjectFragment;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.dltk.mod.internal.core.BuildpathEntry;
import org.eclipse.dltk.mod.internal.core.ModelManager;
import org.eclipse.dltk.mod.internal.core.ScriptProject;
import org.eclipse.mod.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;


public class TypeSpaceMgrTest extends AbstractVjoModelTests implements TypeSpaceListener {

	private static final Path[] EMPTY_PATH = new Path[0];

	private static final String TEST_B = "TestB";

	private static final String TEST_A = "TestA";

	private static final String TEST_D = "TestD";

	private static boolean isFirstRun = true;

	private TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();
	
	//private VjetWorkspace vjetWorkspace = VjetWorkspace.getInstance();
	
	private IProject projectC;

	//private static EclipseTypeSpaceLoader loader = protnew EclipseTypeSpaceLoader();

	private boolean isFinished;

	private ModelManager manager = ModelManager.getModelManager();

	public void setUp(){	
		mgr.addTypeSpaceListener(this);
		mgr.setAllowChanges(false);
		setWorkspaceSufix("TS");
		IProject projectA = getWorkspaceRoot().getProject(TEST_A);
		IProject projectB = getWorkspaceRoot().getProject(TEST_B);
		projectC = getWorkspaceRoot().getProject(TEST_D);
		IProject project = getWorkspaceRoot().getProject(getTestProjectName());
		isFinished = false;


			try {
				super.deleteResource(projectA);
				super.deleteResource(projectB);
				super.deleteResource(projectC);				
				super.deleteResource(project);
				copyProjects(TEST_A, TEST_B, TEST_D);
//				ModelManager manager = ModelManager.getModelManager();
				manager.shutdown();
				manager.startup();										
				mgr.reload(this);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			waitTypeSpaceLoaded(); 

		
	}

	private void copyProjects(String... names) throws CoreException,
			IOException {
		for (String name : names) {
			setUpProject(name);
		}
	}

	//@Test
	public void testFindSatisfies() {
		TypeName name = getTypeName("ts.I");
		List<IJstType> list = mgr.findSatisfiers(name);
		assertTrue(list.size() > 0);
		assertEquals(list.get(0).getName(), "ts.B");
	}
	
//	//@Test
//	public void testGroupDepends() {
//		//TestA depends on 4 default libraries @see TsLibLoader#getDefaultLibNames()
//		// and one zip library 
//		List<String> list = mgr.getGroupDepends(TEST_A);
//		assertEquals(5,list.size() );
//		
//		//TestB depends on TestA and 4 default libraries
//		// TODO this is 10 but there are duplicate groups in group dependencies
//		// need to look into this issue
//		list = mgr.getGroupDepends(TEST_B);
//		assertEquals(10,list.size());
//		//assertEquals(TEST_A,list.get(0));
//		//assertEquals(JstTypeSpaceMgr.JS_NATIVE_GRP,list.get(1));
//	}
	

	//@Test
	public void testFindSubTypes() {
		String n = "ts.A";
		TypeName name = getTypeName(n);
		assertNotNull(mgr.findType(name));
		List<IJstType> subTypes = mgr.findSubTypes(name);
		assertTrue(subTypes.size() > 0);
		IJstNode typeB = findOwnerType(subTypes, "ts.B");
		assertNotNull(typeB);
	}

	private TypeName getTypeName(String n) {
		return new TypeName(TEST_A, n);
	}

	//@Test
	public void testPropertyDepends() throws IOException {
		InputStream stream = getClass().getResourceAsStream("A.js");
		byte[] bs = new byte[stream.available()];
		stream.read(bs);
		char[] s = new String(bs).toCharArray();
		CompilationUnitDeclaration ast = SyntaxTreeFactory2.
			createASTCompilationResult(null, s, "ts.A", null)
			.getCompilationUnitDeclaration();
		
		System.out.println("TypeSpaceMgrts.testPropertyDepends()"+ast);
		
		IJstType typeH = findType("ts.H");

		TypeName typeName = new TypeName(TEST_B, "ts.G");
		IJstType typeG = mgr.findType(typeName);
		
		assertEquals(typeG.getExtend(), typeH);

		PropertyName name = new PropertyName(getTypeName("ts.H"), "b");
		List<IJstNode> list = mgr.getPropertyDependents(name);
		assertEquals(1,list.size());
		IJstMethod method = findMethod(list.get(0));
		IJstMethod typeMethod = typeG.getMethod("testMethod");
		assertEquals(typeMethod.toString(), method.toString());
	}

	//@Test
	public void testMethodDepends() {

		MethodName name = new MethodName(getTypeName("ts.A"),"gh");	
		List<IJstNode> list = mgr.getMethodDependents(name);

		IJstNode node = list.get(0);
		IJstMethod method = findMethod(node);
		assertNotNull(method);

		IJstType typeB = findType("ts.B");
		IJstMethod typeMethod = typeB.getMethod(method.getOriginalName());
		assertNotNull(typeMethod);

		assertEquals(method.toString(), typeMethod.toString());
	}

	private IJstMethod findMethod(IJstNode node) {
		IJstMethod method = null;
		while (node != null) {
			if (node instanceof IJstMethod) {
				method = (IJstMethod) node;
				break;
			}
			node = node.getParentNode();
		}
		return method;
	}

	private IJstNode findOwnerType(List<IJstType> list, String string) {
		IJstNode index = null;

		for (IJstNode jstNode : list) {
			if (jstNode.getOwnerType().getName().equals(string)) {
				index = jstNode;
				break;
			}
		}

		return index;
	}

	//@Test
	public void testExtends() {
		IJstType typeA = findType("ts.A");
		IJstType typeB = findType("ts.B");
		assertEquals(typeB.getExtend(), typeA);
		}

	private IJstType findType(String string) {
		TypeName nameA = getTypeName(string);
		IJstType type = mgr.findType(nameA);
		return type;
	}

	
	//@Test
	public void _testChangeBuildPath() throws ModelException {
		//System.out.println("TypeSpaceMgrts.testChangeBuildPath() start");
		TypeName typeNameC = new TypeName(TEST_D, "ts.C");
		IJstType typeC = mgr.findType(typeNameC);
		
		assertNotNull(typeC);		
		assertNotNull(typeC.getExtend());
		assertEquals(typeC.getExtend().getName(),"ts.A");
		
		TypeName typeNameA = new TypeName(TEST_A, "ts.A");
		IJstType typeA = mgr.findType(typeNameA);
		assertNotNull(typeA);
		
		//assertNotSame(typeC.getExtend(), typeA);
		
		ScriptProject project = (ScriptProject) manager.getModel()
				.getScriptProject(TEST_D);
		assertTrue(projectC.isAccessible());
		IBuildpathEntry[] entries = project.getResolvedBuildpath(true);
		IBuildpathEntry[] newEntries = new BuildpathEntry[entries.length + 1];
		System.arraycopy(entries, 0, newEntries, 0, entries.length);
		Path path = new Path("/"+TEST_A);
		newEntries[entries.length] = new BuildpathEntry(
				IProjectFragment.K_SOURCE, BuildpathEntry.BPE_PROJECT, path,
				false, EMPTY_PATH, EMPTY_PATH, null, false,
				new IBuildpathAttribute[0], false);
		
		
		project.saveBuildpath(newEntries);		
		//System.out.println("TypeSpaceMgrts.testChangeBuildPath() build path saved");
		waitTypeSpaceLoaded();				
		
		//System.out.println("TypeSpaceMgrts.testChangeBuildPath() job finished");
		
		typeNameC = new TypeName(TEST_D, "ts.C");
		typeC = mgr.findType(typeNameC);
		//System.out.println("TypeSpaceMgrts.testChangeBuildPath()"+typeC);
		assertNotNull(typeC);
		assertNotNull(typeC.getExtend());
		assertEquals(typeC.getExtend().getName(),"ts.A");
		
		typeNameA = new TypeName(TEST_A, "ts.A");
		typeA = mgr.findType(typeNameA);
		
		assertNotNull(typeA);
				
		assertEquals(typeC.getExtend().hashCode(), typeA.hashCode());
		//System.out.println("TypeSpaceMgrts.testChangeBuildPath() finished");
	}

	//@Test
	public void _testCloseOpenProject() throws CoreException {
		TypeName typeName = new TypeName(TEST_D, "ts.C");
		IJstType typeC = mgr.findType(typeName);
		assertNotNull(typeC);
		mgr.setAllowChanges(true);
		projectC.close(new NullProgressMonitor());
		
		waitTypeSpaceLoaded();
		assertFalse(mgr.existGroup(TEST_D));
	    typeC = mgr.findType(typeName);
		assertNull(typeC);		
		projectC.open(new NullProgressMonitor());
		

		waitTypeSpaceLoaded();
		mgr.setAllowChanges(false);
	}

	//@Test
	public void testTypeSearcher() throws IOException {
		TypeSearcher searcher = TypeSearcher.getInstance();
		List<IJstType> results = searcher.search("ts.C");
		assertTrue(results.size() > 0);
		final IJstType type = results.get(0);
		assertTrue("C".equals(type.getSimpleName()));
	}
	
//	public void waitTypeSpaceLoaded() {
//		while (!isFinished);
//		isFinished = false;
//
//	}

	public void loadTypesFinished() {
		isFinished = true;		
	}

	public void onComplete(EventListenerStatus<IJstType> arg0) {
		isFinished = true;	
	}

	public void processEventFinished(ISourceEvent event) {
		isFinished = true;		
	}

	public void refreshFinished(List<SourceTypeName> list) {
		// TODO Auto-generated method stub
		
	}
	
	public void testNativeTypeToJs() {
		GeneratorCtx m_generatorCtx = new GeneratorCtx(CodeStyle.PRETTY);
		Iterator<String> s = TypeSpaceMgr.NATIVE_GLOBAL_OBJECTS.iterator();
		while (s.hasNext()) {
			String name = s.next();
			IJstType type = CodeassistUtils.findNativeJstType(name);
			VjoGenerator writer = m_generatorCtx.getProvider()
					.getTypeGenerator();
				assertTrue(writer.writeVjo(type).getGeneratedText() != null);
		}

	}

		
}
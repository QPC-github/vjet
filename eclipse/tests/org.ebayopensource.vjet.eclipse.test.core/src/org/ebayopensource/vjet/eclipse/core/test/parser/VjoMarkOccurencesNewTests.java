/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.test.parser;

import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.jface.text.Region;

import org.ebayopensource.vjet.eclipse.core.IJSSourceModule;
import org.ebayopensource.vjet.eclipse.core.test.FixtureUtils;
import org.ebayopensource.vjet.testframework.fixture.FixtureManager;

public class VjoMarkOccurencesNewTests extends AbstractMarkOccurencesTests {

	public void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * test on type
	 * 
	 * @throws ModelException
	 */
	public void testOnType() throws ModelException {
		simplyTest("MyTest", "MyTest.InstanceInnerType", "var a = new this.vj$.<cursor>MyTest(); //<MyTest");//var a = new this.vj$.<cursor>MyTest();
	}

	public void testOnMethodEnd() throws ModelException {
		simplyTest("foo", ">public String foo", "foo<cursor> : function (a) {");//var a = new this.vj$.<cursor>MyTest();
	}
	
	
	public void testOnVJO() throws ModelException {
		simplyTest("vjo", "vjo.ctype", "<cursor>vjo.syserr.print('a');");//<cursor>vjo.syserr.print('a');
	}

	public void testOnLocal() throws ModelException {
		simplyTest("outerType", "var <cursor>outerType = new this.vj$.MyTest()"); // var <cursor>outerType = new this.vj$.MyTest()
	}
	
	public void testOnLocal2() throws ModelException {
		simplyTest("outerType", "var innerType = new <cursor>outerType.InstanceInnerType()"); // var innerType = new <cursor>outerType.InstanceInnerType() ;
	}

	public void testOnArg() throws ModelException {
		String[] ex = new String[2];
		ex[0] = "public void zoo(int x)";
		ex[1] = "ax";
		simplyTest("x", ex, "var c = this.foo(<cursor>x).length"); // var c = foo(<cursor>x).length;
	}
	
/*	public void testOnInnerTypeName() throws ModelException {
		simplyTest("InstanceInnerType", "new outerType.<cursor>InstanceInnerType()"); // new outerType.<cursor>InstanceInnerType()
	}
	
	public void testOnInnerTypeName2() throws ModelException {
		simplyTest("InstanceInnerType", "<cursor>InstanceInnerType : vjo.ctype()"); // <cursor>InstanceInnerType : vjo.ctype()
	}
*/
	public void testOnInnerTypeProperty() throws ModelException {
		simplyTest("z", "zoo", "<cursor>z: 2, //<int"); // <cursor>z: 2, //<int
	}
	
	public void testOnInnerTypeProperty2() throws ModelException {
		simplyTest("z", "zoo", "this.<cursor>z++;"); // this.<cursor>z++;
	}

	public void testOnInnerTypeMethod() throws ModelException {
		simplyTest("innerFunc",  "innerType.<cursor>innerFunc();"); // innerType.<cursor>innerFunc();
	}
	
	public void testOnInnerTypeMethod2() throws ModelException {
		simplyTest("innerFunc",  "<cursor>innerFunc : function() {"); // <cursor>innerFunc : function() {
	}
	
	public void testOnInnerTypeLocalVar() throws ModelException {
		simplyTest("date",  "var <cursor>date = new Date();"); // var <cursor>date = new Date(); //< Date

	}

	public void testOnAlert() throws ModelException {
		simplyTest("alert",  "window.<cursor>alert('hi');"); // <cursor>date.getDate();
	}
	
	
	public void testOnInnerTypeLocalVar2() throws ModelException {
		simplyTest("date",  "<cursor>date.getDate();"); // <cursor>date.getDate();
	}
	
/*	public void testOnNativeMethod() throws ModelException {
		simplyTest("println",  "vjo.sysout.<cursor>println('InstanceInnerType function called');"); // vjo.sysout.<cursor>println('InstanceInnerType function called');      

	}
	
	public void testOnNativeMethod2() throws ModelException {
		simplyTest("println",  "vjo.sysout.<cursor>println('OuterType function called');"); // vjo.sysout.<cursor>println('OuterType function called');    
	}
*/	
	private void simplyTest(String occurrenceName, String[] excludedName, String sentence)throws ModelException  {
		String js = "selection/MyTest.js";
		FixtureManager m_fixtureManager = FixtureUtils.setUpFixture(this, js);
		
		try {
			IJSSourceModule module = (IJSSourceModule) getSourceModule(
					getProjectName(), "src", new Path(js));
			String name = occurrenceName;

			List<Region> matches = super.getPositions(module.getSource(), name, excludedName);
			
			assertNotNull("Cant find position in file", matches);
			assertNotSame(0, matches.size());

			Region position = this.getRegion(getOffset(module, sentence), name); 
			basicTest(module, position, matches);
		} finally {
			FixtureUtils.tearDownFixture(m_fixtureManager);
		}
		
	}

	private void simplyTest(String occurrenceName, String excludedName, String sentence)throws ModelException  {
		String js = "selection/MyTest.js";
		FixtureManager m_fixtureManager = FixtureUtils.setUpFixture(this, js);
		buildAndWaitForEnd();
		try {
			IJSSourceModule module = (IJSSourceModule) getSourceModule(
					getProjectName(), "src", new Path(js));
			String name = occurrenceName;

			String[] excludedNames = new String[1];
			excludedNames[0] = excludedName;
			List<Region> matches = super.getPositions(module.getSource(), name, excludedNames);
			
			assertNotNull("Cant find position in file", matches);
			assertNotSame(0, matches.size());

			Region position = this.getRegion(getOffset(module, sentence), name); 
			basicTest(module, position, matches);
		} finally {
			FixtureUtils.tearDownFixture(m_fixtureManager);
		}
		
	}

	private void simplyTest(String occurrenceName, String sentence)throws ModelException  {
		String js = "selection/MyTest.js";
		FixtureManager m_fixtureManager = FixtureUtils.setUpFixture(this, js);
		
		try {
			IJSSourceModule module = (IJSSourceModule) getSourceModule(
					getProjectName(), "src", new Path(js));
			String name = occurrenceName;

			List<Region> matches = super.getPositions(module.getSource(), name);
			
			assertNotNull("Cant find position in file", matches);
			assertNotSame(0, matches.size());

			Region position = this.getRegion(getOffset(module, sentence), name); 
			basicTest(module, position, matches);
		} finally {
			FixtureUtils.tearDownFixture(m_fixtureManager);
		}
		
	}
	
	private Region getRegion(int position, String name) {
		return new Region(position, name.length());
	}
}

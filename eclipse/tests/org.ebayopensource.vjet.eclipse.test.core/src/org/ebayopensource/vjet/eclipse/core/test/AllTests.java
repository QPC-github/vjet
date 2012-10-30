/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ebayopensource.vjet.eclipse.core.test.bug.BugVerifyTests;
import org.ebayopensource.vjet.eclipse.core.test.contentassist.BaseTemplateTests;
import org.ebayopensource.vjet.eclipse.core.test.contentassist.VjoSteppingCodeCompletionTest;
import org.ebayopensource.vjet.eclipse.core.test.debug.DebuggerTest;
import org.ebayopensource.vjet.eclipse.core.test.debug.StepFilteringPatternTest;
import org.ebayopensource.vjet.eclipse.core.test.parser.VJOSelectionNewTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoCallHierarchyTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoMarkOccurencesNewTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoMarkOccurencesRefPrjsTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoMarkOccurencesTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoSearchRefPrjsTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoSearchTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoSelectionRefPrjsTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoSelectionTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoSourceElementParserTests;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoSteppingSelectionTest;
import org.ebayopensource.vjet.eclipse.core.test.parser.VjoTypeHierarchyTests;
import org.ebayopensource.vjet.eclipse.core.test.ted.TedTypespaceLoadTest;
import org.ebayopensource.vjet.eclipse.core.test.ui.JsPerspectiveTests;
import org.ebayopensource.vjet.eclipse.core.test.ui.TypeSpaceCleanTester;
import org.ebayopensource.vjet.eclipse.core.test.ui.VjoAutoEditorStrategyTester;
import org.ebayopensource.vjet.eclipse.core.test.ui.VjoWizardTests;
import org.ebayopensource.vjet.eclipse.core.test.validation.DefaultValueTester;
import org.ebayopensource.vjet.eclipse.core.test.validation.VjoValidationPolicy;
import org.ebayopensource.vjet.eclipse.core.ts.ExpressionTypeLinkerTests;
import org.ebayopensource.vjet.eclipse.core.ts.ResourceChangedTests;
import org.ebayopensource.vjet.eclipse.core.ts.TypeSpaceMgrTest;

public class AllTests extends TestSuite {

	public static Test suite() {
		// InitializationList list = new InitializationList();
		/*
		 * list.addChild(TransportServiceFactory.class.getName());
		 * 
		 * final BaseKernelTestSuite suite = new BaseKernelTestSuite(
		 * "All Kernel Util XSL tests.", null, list);
		 */
		final TestSuite suite = new TestSuite("All VJET2 Tests");

		// //////////////////////////////////////
		// Add any new tests here
		// //////////////////////////////////////

		// Bug 4722 Tester, Modify by Eric.Ma 2009.06.23//
		suite.addTestSuite(TypeSpaceCleanTester.class);
		// add bug verify test suite
		suite.addTestSuite(BugVerifyTests.class);
		// End of modification.
		// suite.addTestSuite(DefectCodeCompletionTests.class);
		suite.addTestSuite(VjoSourceElementParserTests.class);
		suite.addTestSuite(VjoTypeHierarchyTests.class);
		suite.addTestSuite(VjoCallHierarchyTests.class);
		// suite.addTestSuite(CodeCompletionTests.class);
		// suite.addTestSuite(VjoUtilityCompletionTests.class);
		// suite.addTestSuite(ThisCompletionTests.class);
		suite.addTestSuite(TypeSpaceMgrTest.class);
		suite.addTestSuite(ResourceChangedTests.class);
		// suite.addTestSuite(MixinCompletionTests.class);
		// suite.addTestSuite(CyclicDependencyTest.class);
		// suite.addTestSuite(ThisCompletionTests2.class) ;
		// suite.addTestSuite(CodeCompletionTests2.class) ;
		// suite.addTestSuite(CompletionTestsForUnexpectedPosition.class);
		// suite.addTestSuite(NativeCompletionTest.class);
		// suite.addTestSuite(TypeCompletionTests.class);
		suite.addTestSuite(ExpressionTypeLinkerTests.class);
		suite.addTestSuite(VjoMarkOccurencesTests.class);
		suite.addTestSuite(VjoMarkOccurencesRefPrjsTests.class);
		suite.addTestSuite(VjoSelectionTests.class);
		suite.addTestSuite(VjoSelectionRefPrjsTests.class);
		suite.addTestSuite(VjoSearchTests.class);
		suite.addTestSuite(VjoSearchRefPrjsTests.class);
		// suite.addTestSuite(TemplateCompletionTests.class);
		suite.addTestSuite(VjoWizardTests.class);
		suite.addTestSuite(VjoSteppingCodeCompletionTest.class);
		suite.addTestSuite(VjoSteppingSelectionTest.class);
		// suite.addTestSuite(PartialCodeCompletionTests.class);
		suite.addTestSuite(JsPerspectiveTests.class);
		suite.addTestSuite(BaseTemplateTests.class);
		// TODO revisit these test cases VjoAutoEditorStrategyTester
//		suite.addTestSuite(VjoAutoEditorStrategyTester.class);
		suite.addTestSuite(VJOSelectionNewTests.class);
		suite.addTestSuite(VjoMarkOccurencesNewTests.class);
		// Return the test suite

//		suite.addTestSuite(CodeCompletionUITests.class);
		suite.addTestSuite(DefaultValueTester.class);
//		suite.addTestSuite(VjoValidationPolicy.class);
		suite.addTestSuite(DebuggerTest.class);
		suite.addTestSuite(StepFilteringPatternTest.class);
//		suite.addTestSuite(OpenMarkerTest.class);
		// TED test suit
		suite.addTestSuite(TedTypespaceLoadTest.class);
		return suite;
	}

}

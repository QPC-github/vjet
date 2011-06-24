/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/**
 * 
 */
package org.ebayopensource.dsf.jst.validation.vjo.syntax.generic;

import static com.ebay.junitnexgen.category.Category.Groups.FAST;
import static com.ebay.junitnexgen.category.Category.Groups.P1;
import static com.ebay.junitnexgen.category.Category.Groups.UNIT;

import java.util.List;

import org.ebayopensource.dsf.jsgen.shared.validation.vjo.VjoSemanticProblem;
import org.ebayopensource.dsf.jst.validation.vjo.VjoValidationBaseTester;
import org.junit.Before;
import org.junit.Test;

import com.ebay.junitnexgen.category.Category;
import com.ebay.junitnexgen.category.Description;

/**
 * 
 * 
 */
@Category( { P1, FAST, UNIT })
public class DeclaredGeneric6Tester extends VjoValidationBaseTester {
    @Before
    public void setUp() {
        expectProblems.clear();
    }

    @Test
    @Category( { P1, FAST, UNIT })
    @Description("Test parameter as generic")
    public void testNestedTypes() {
        List<VjoSemanticProblem> problems = getVjoSemanticProblem(
                "syntax.generic.", "GenericIType.js", this.getClass());
        assertProblemEquals(expectProblems, problems);
    }
}

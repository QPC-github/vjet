/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/* 
 * $Id: FileOperatorTester.java, Dec 30, 2009, 12:10:30 AM, liama. Exp$
 *
 * Copyright (c) 2006-2009 Ebay Technologies. All Rights Reserved.
 * This software program and documentation are copyrighted by Ebay 
 * Technologies.
 */
package org.ebayopensource.dsf.cml.vjetv.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.ebayopensource.dsf.jstojava.cml.vjetv.util.FileOperator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Class/Interface description
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */

@Ignore
public class FileOperatorTester {

    private static String viewroot = "";

    private static String currentPath = System.getProperty("user.dir");

    @Before
    public void setUp() {
        FileOperatorTester t = new FileOperatorTester();
        File f = new File(t.getClass().getProtectionDomain().getCodeSource()
                .getLocation().getFile());
        String path = f.getAbsolutePath();
        path = path.substring(0, path.indexOf("v4darwin"));
        viewroot = path;
    }

    @Test
    public void testGetAllJSFiles() {
        LinkedHashSet<File> sets = new LinkedHashSet<File>();
        String testFolder1 = viewroot
                + "/v4darwin/DSFJsToJavaTests/vjLibTestFiles/dox/ebay/vjoPro/vjoPro4javadev/samples/vjlib/windows";
        FileOperator.getAllJSFiles(new File(testFolder1), sets, null);
        Assert.assertEquals(1, sets.size());
    }

    @Test
    public void testGetAllJSFiles2() {
        LinkedHashSet<File> sets = new LinkedHashSet<File>();
        String testFolder1 = viewroot
                + "/v4darwin/DSFJsToJavaTests/vjLibTestFiles/dox/ebay/vjoPro/vjoPro4javadev/samples/vjlib";
        FileOperator.getAllJSFiles(new File(testFolder1), sets, null);
        Assert.assertEquals(20, sets.size());
    }

    @Test
    public void testGetAllJSFiles3() {
        LinkedHashSet<File> sets = new LinkedHashSet<File>();
        String testFolder1 = viewroot
                + "/v4darwin/DSFJsToJavaTests/vjLibTestFiles/dox/ebay/vjoPro/vjoPro4javadev/samples/sample1";
        FileOperator.getAllJSFiles(new File(testFolder1), sets, null);
        Assert.assertEquals(3, sets.size());
    }

    @Test
    public void testGetAllJSFiles4() {
        LinkedHashSet<File> sets = new LinkedHashSet<File>();
        String testFolder1 = viewroot
                + "/v4darwin/DSFJsToJavaTests/vjLibTestFiles/dox/ebay/vjoPro/vjoPro4javadev/samples/";
        FileOperator.getAllJSFiles(new File(testFolder1), sets, null);
        Assert.assertEquals(38, sets.size());
    }

    @Test
    public void testIsJarJSPathValid1() {
        ArrayList<File> lists = new ArrayList<File>();
        lists.add(new File(currentPath + "/testBp/testJS.jar"));
        Assert.assertEquals(true, FileOperator.isJarJsPathValid(
                "access/inherits", lists, false));
    }

    @Test
    public void testIsJarJSPathValid2() {
        ArrayList<File> lists = new ArrayList<File>();
        lists.add(new File(currentPath + "/testBp/testJS.jar"));
        Assert.assertEquals(false, FileOperator.isJarJsPathValid(
                "access/inherits/false", lists, false));
    }

    @Test
    public void testIsJarJSPathValid3() {
        ArrayList<File> lists = new ArrayList<File>();
        lists.add(new File(currentPath + "/testBp/testJS.jar"));
        Assert.assertEquals(true, FileOperator.isJarJsPathValid(
                "access/inherits/Person.js", lists, false));
    }

    @Test
    public void testIsJarJSPathValid4() {
        ArrayList<File> lists = new ArrayList<File>();
        lists.add(new File(currentPath + "/testBp/testJS.jar"));
        Assert.assertEquals(true, FileOperator.isJarJsPathValid(
                "access.inherits", lists, false));
    }

    @Test
    public void testIsJarJSPathValid5() {
        ArrayList<File> lists = new ArrayList<File>();
        lists.add(new File(currentPath + "/testBp/testJS.jar"));
        Assert.assertEquals(true, FileOperator.isJarJsPathValid(
                "access" + File.separator + "inherits", lists, false));
    }

    @Test
    public void testIsJarJSPathValid6() {
        ArrayList<File> lists = new ArrayList<File>();
        lists.add(new File(currentPath + "/testBp/testJS.jar"));
        Assert.assertEquals(true, FileOperator.isJarJsPathValid(
                "access.inherits.Person.js", lists, false));
    }

    @Test
    public void testGatherJSFiles() {
        ArrayList<File> lists = new ArrayList<File>();
        lists.add(new File(currentPath + "/testBp/testJS.jar"));
        FileOperator.gatherSpecifiedFiles("access.inherits.Person.js", lists);
        File f = new File(FileOperator.TEMPFOLDER);
        FileOperator.delFolder(f.getAbsolutePath());
    }

    @Test
    public void testGetSourceLine() {
        File f = new File(currentPath + "/testBp/ArgFile5.txt");
        String source = FileOperator.getSourceFromFile(f);
        source = FileOperator.getSourceLineFromFile(source, 10, 21);
        System.out.println(source);
    }
}

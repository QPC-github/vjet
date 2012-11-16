/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/* 
 * $Id: FileOperator.java,v 1.7 2008/10/09 12:21:34 eric Exp $
 *
 * Copyright (c) 2006-2007 Wipro Technologies. All Rights Reserved.
 * This software program and documentation are copyrighted by Wipro 
 * Technologies.
 */
package org.ebayopensource.dsf.jstojava.cml.vjetv.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.ebayopensource.dsf.jstojava.loader.OnDemandAllTypeLoader;

/**
 * utility class for ebay vjet validation service, used to operator file in
 * local disk.
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */
public class FileOperator {

    /**
     * constant for lib.
     */
    private static final String LIB = "lib";

    /**
     * current user directory.
     */
    public static final String USER_DIR = "user.dir";

    /**
     * bin folder in eclipse style.
     */
    private static final String BIN = "bin";

    /**
     * target folder in maven style.
     */
    private static final String TARGET = "target";

    /**
     * classes folder in maven style.
     */
    public static final String CLASSES = "classes";

    /**
     * jar package suffix.
     */
    public static final String JAR_SUFFIX = ".jar";

    /**
     * jar package suffix.
     */
    public static final String JAR_FOLDER_SUFFIX = File.separatorChar + "*.jar";

    public static final String TEMPFOLDER = System.getProperty("user.dir")
            + File.separatorChar + "VjetTempFolder";

    /**
     * dot used in full class name.
     */
    public static final char DOT = '.';

    /**
     * constant for [.].
     */
    private static final String DOT_PATTERN = "[.]";

    /**
     * one.
     */
    private static final int ONE = 1;

    /**
     * zero.
     */
    private static final int ZERO = 0;

    /**
     * suffix of JS file.
     */
    public static final String JS_SUFFIX = ".js";

    /**
     * suffix of All JS Folder
     */
    public static final String JS_FOLDER_SUFFIX = File.separatorChar + "*"
            + JS_SUFFIX;

    /**
     * separator in class path.
     */
    public static final String SEPARATOR = File.pathSeparator;

    /**
     * The value is used for character storage.
     */
    public static final String JDTCOMPILER = "org.eclipse.jdt.core";

    /**
     * The value is used for character storage.
     */
    public static final String JAVASOURCEPATH = "java.source.path";

    /**
     * bind folder name.
     * 
     * @param mainFolder
     *            the home directory.
     * @param fileName
     *            the folder name
     * @return String
     */
    public static String bindFileName(String mainFolder, final String fileName) {
        StringBuffer sb = new StringBuffer();
        sb.append(mainFolder);
        sb.append(File.separatorChar);
        sb.append(fileName);
        sb.append(File.separatorChar);
        return sb.toString();
    }

    /**
     * gets classes directory. System will find classes folder in eclipse style
     * and maven style. it will return null if can't find classes folder in
     * these two style.
     * 
     * @param home
     *            File project directory.
     * @return String classes folder absolute path if find.
     */
    public static String getClassesDir(File home) {
        File[] list = home.listFiles();
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File temp = list[i];
                final String fn = temp.getName();
                // try to find test case folder in eclipse style.
                if (fn.equals(BIN)) {
                    return BIN;
                }
                // try to find test case folder in maven style.
                if (fn.equals(TARGET)) {
                    File[] files = temp.listFiles();
                    for (int j = 0; j < files.length; j++) {
                        final String fileName = files[j].getName();
                        if (fileName.equals(CLASSES)) {
                            StringBuffer sb = new StringBuffer();
                            sb.append(TARGET);
                            sb.append(File.separator);
                            sb.append(CLASSES);
                            return sb.toString();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * gets class folder with the specific package name.
     * 
     * @param files
     *            File[] files in the folder.
     * @param pns
     *            package name array.
     * @return File class folder.
     */
    protected static File getClassFolderWithPackageName(File[] files,
            String[] pns) {
        final int len = pns.length;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < files.length; j++) {
                if (files[j].getName().equals(pns[i])) {
                    if (len == ONE) {
                        return files[j];
                    }
                    File[] fs = files[j].listFiles();
                    String[] temp = new String[pns.length - i - ONE];
                    System.arraycopy(pns, i + ONE, temp, ZERO, temp.length);
                    return getClassFolderWithPackageName(fs, temp);
                }
            }
        }
        return null;
    }

    /**
     * Get all JS files from user specify path
     * 
     * @param file
     *            {@link File}
     * @param lists
     *            {@link ArrayList}
     * @param exclusionRules 
     */
    public static void getAllJSFiles(File file, LinkedHashSet<File> lists, char[][] exclusionRules) {
        if (file.isDirectory() && file.canRead()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile() && files[i].canRead()) {
                    if (isJSFiles(files[i]) && !OnDemandAllTypeLoader.isExcluded(files[i].getAbsolutePath().toCharArray(),null,exclusionRules,false)) {
                        lists.add(files[i]);
                    }
                } else if (files[i].isDirectory() && files[i].canRead()) {
                    getAllJSFiles(files[i], lists, exclusionRules);
                }
            }
        }
        if (file.isFile() && file.canRead() && isJSFiles(file)) {
            lists.add(file);
        }
    }

    /**
     * Judge file is end with JS file
     * 
     * @param file
     *            {@link File}
     * @return boolean
     */
    private static boolean isJSFiles(File file) {
        if (file.getAbsolutePath().toLowerCase().endsWith(JS_SUFFIX))
            return true;
        return false;
    }

    /**
     * Get source from file
     * 
     * @param f
     *            {@link File}
     * @return String
     */
    public static String getSourceFromFile(File f) {
        byte[] bytes = new byte[1024];
        StringBuffer sb = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(f);
            while (fis.read(bytes) != -1) {
                sb.append(new String(bytes));
                bytes = new byte[1024];
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Get line source from file
     * 
     * @param f
     *            {@link File}
     * @return String
     */
    public static String getSourceLineFromFile(String source, int start, int end) {
        String endString = source.substring(end);
        int newline = endString.indexOf("\n");
        int newEnd = newline == -1 ? source.length() : newline + end;

        int newStart = getNewStringPosition(source, start);
        try {
            return source.substring(newStart, newEnd);
        } catch (Exception e) {
            return source.substring(start, end);
        }
    }

    /**
     * @param source
     * @param start
     * @return int
     */
    public static int getNewStringPosition(String source, int start) {
        String startString = source.substring(0, start);
        int lastline = startString.lastIndexOf("\n");
        int newStart = lastline == -1 ? 0 : lastline + 1;
        return newStart;
    }

    /**
     * Get all JS files from user specify path
     * 
     * @param file
     *            {@link File}
     * @param lists
     *            {@link ArrayList}
     */
    public static void getJarsFiles(File file, HashSet<File> lists) {
        if (file.isDirectory() && file.canRead()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile() && files[i].canRead()) {
                    if (isJarFiles(files[i])) {
                        lists.add(files[i]);
                    }
                }
            }
        }
    }

    public static boolean isJarJsPathValid(String packageName,
            List<File> buildJars, boolean exist) {

        packageName = replaceSlash2ZipSlash(packageName);

        ZipEntry result = null;
        for (File file : buildJars) {
            ZipFile zip = null;
            try {
                zip = new ZipFile(file);
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (packageName != null) {
                result = zip.getEntry(packageName);
            }
            if (result != null) {
                return true;
            }
        }
        if (exist) {
            ParserHelper.exitSystem("Please check JS path : " + packageName
                    + "Or exist in buildpath jars!");
        }
        return false;
    }

    /**
     * @param packageName
     * @return String
     */
    private static String replaceDot2Slash(String packageName) {
        if (packageName == null) {
            ParserHelper.exitSystem("Please check JS path!");
        }
        if (packageName.equalsIgnoreCase("")) {
            ParserHelper.exitSystem("Please check JS path : " + packageName
                    + "Or exist in buildpath jars!");
        }
        if (packageName.endsWith(FileOperator.JS_FOLDER_SUFFIX)) {
            packageName = packageName.substring(0, packageName
                    .lastIndexOf(File.separatorChar));
        }
        if (packageName.contains(".")) {
            if (packageName.endsWith(JS_SUFFIX)) {
                packageName = packageName.substring(0, packageName
                        .indexOf(JS_SUFFIX));
                packageName = packageName.replace('.', File.separatorChar);
                packageName = packageName + JS_SUFFIX;
            } else {
                packageName = packageName.replace('.', File.separatorChar);
            }
        }
        return packageName;
    }

    /**
     * @param packageName
     * @return String
     */
    private static String replaceSlash2ZipSlash(String packageName) {
        packageName = replaceDot2Slash(packageName);
        if (packageName.contains("\\")) {
            packageName = packageName.replace('\\', '/');
        }
        return packageName;
    }

    public static boolean isJarJsPathValid(String packageName,
            List<File> buildJars) {
        return isJarJsPathValid(packageName, buildJars, true);
    }

    /**
     * Judge file is end with JS file
     * 
     * @param file
     *            {@link File}
     * @return boolean
     */
    public static boolean isJarFiles(File file) {
        if (file.getAbsolutePath().toLowerCase().endsWith(JAR_SUFFIX))
            return true;
        return false;
    }

    public static void gatherSpecifiedFiles(String specifiedpath,
            List<File> buildJars) {
        specifiedpath = replaceSlash2ZipSlash(specifiedpath);
        for (File buildFile : buildJars) {
            try {
                ZipFile zip = new ZipFile(buildFile);
                Enumeration en = zip.entries();
                ZipEntry entry = null;
                InputStream input = null;
                BufferedOutputStream bos = null;
                File file = null;
                byte[] buffer = new byte[8192];
                int length = -1;
                while (en.hasMoreElements()) {
                    entry = (ZipEntry) en.nextElement();
                    if (entry.isDirectory()
                            || !entry.getName().startsWith(specifiedpath)) {
                        continue;
                    }
                    if (entry.getName().endsWith(JS_SUFFIX)) {
                        input = zip.getInputStream(entry);
                        file = new File(TEMPFOLDER, entry.getName());
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        bos = new BufferedOutputStream(new FileOutputStream(
                                file));

                        while (true) {
                            length = input.read(buffer);
                            if (length == -1)
                                break;
                            bos.write(buffer, 0, length);
                        }
                        bos.close();
                        input.close();
                    }
                }
                zip.close();
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Del a folder with all children
     * 
     * @param folderPath
     *            String
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath);
            new File(folderPath).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Del all files under a folder
     * 
     * @param path
     * @return boolean
     */
    private static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);
                delFolder(path + "/" + tempList[i]);
                flag = true;
            }
        }
        return flag;
    }



}

/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jstojava.cml.vjetv.core.impl;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.ebayopensource.dsf.jstojava.cml.vjetv.core.IHeadLessLauncher;
import org.ebayopensource.dsf.jstojava.cml.vjetv.core.LauncherFactory;
import org.ebayopensource.dsf.jstojava.cml.vjetv.model.IHeadlessLauncherConfigure;
import org.ebayopensource.dsf.jstojava.cml.vjetv.model.impl.VjetvHeadlessConfigure;
import org.ebayopensource.dsf.jstojava.cml.vjetv.util.ClassLoaderUtil;
import org.ebayopensource.dsf.jstojava.cml.vjetv.util.FileOperator;

/**
 * Class/Interface description
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */
public class EVHeadleassTask extends Task {

    /**
     * The value is used for character storage.
     */
    private final static String PATHSEPARATORCHAR = String
            .valueOf(File.pathSeparatorChar);

    /**
     * whether the path of class folder is absolute.
     * 
     * @param tcDir
     *            String class folder of target project.
     * @return It will return true if the tcDir is absolute.
     */
    private static boolean isAbsolutePath(String tcDir) {
        return new File(tcDir).isAbsolute();
    }

    /**
     * Judge user specify path is valid
     * 
     * @param path
     *            String
     * @return boolean
     */
    private static File isPathValid(String path) {
        if (null == path) {
            return null;
        }
        path = path.trim();
        File file = new File(path);
        if (file.exists() && file.canRead()) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * Headless ebay validation task entry Arg 0 , validated projeceName, Arg 1 ,
     * validated source locations, Arg 2 , validated class path. Arg 3 ,
     * validated report destn location.
     * 
     * @param args
     */
    public static void main(String[] args) {
        EVHeadleassTask task = new EVHeadleassTask();
        task.m_validateProjectName = args[0];
        task.m_sourceLocation = args[1];
        task.m_classPath = args[2];
        task.execute();
    }

    /**
     * The value is used for character storage.
     * 
     */
    private String m_validateProjectName;

    /**
     * The value is used for character storage.
     * 
     */
    private String[] m_dynamicClassPath = new String[] {};

    /**
     * The value is used for character storage.
     * 
     */
    private String m_classPath;

    /**
     * The value is used for character storage.
     * 
     */
    private String m_sourceLocation;

    /**
     * The value is used for character storage.
     */
    private boolean m_isVerbose;

    /**
     * The value is used for character storage.
     * 
     */
    HashSet<String> m_classPathSet = new HashSet<String>();

    /**
     * The value is used for character storage.
     */
    LinkedHashSet<File> m_jsFiles = new LinkedHashSet<File>();

    /**
     * 
     * Add path to source path which will be used to add onDemandLoader
     * 
     * @param bins
     *            {@link String}
     */
    private void addSourcePath(String[] bins) {
        StringBuffer sb = new StringBuffer();
        if (null != System.getProperty("java.source.path")) {
            sb.append(System.getProperty("java.source.path"));
        }
        for (int i = 0; i < bins.length; i++) {
            sb.append(File.pathSeparatorChar + bins[i]);
        }
        System.setProperty("java.source.path", sb.toString());
    }

    /**
     * Calculate source folder path
     * 
     * @return {@link HashSet}
     */
    private HashSet<String> calculateSourceFolderPath() {
        HashSet<String> sourceFolders = new HashSet<String>();
        if (m_sourceLocation != null) {
            // SourceLocation are relative path
            String[] sourceLocations = m_sourceLocation
                    .split(PATHSEPARATORCHAR);
            String userDir = System.getProperty("user.dir");
            String sourceFolder;
            for (int i = 0; i < sourceLocations.length; i++) {
                sourceFolder = sourceLocations[i].trim();
                if (!sourceFolder.equals("")) {
                    // Change relative path to absolute path
                    sourceFolders.add(userDir + File.separatorChar
                            + sourceFolder);
                }
            }
        }
        return sourceFolders;
    }

    /**
     * Convert properties to vjetv configure
     */
    private VjetvHeadlessConfigure convertProperties() {

        // Step1 calculate source folder path
        HashSet<String> sourceFolders = calculateSourceFolderPath();

        // Step2 get runtime class path
        if (m_classPath != null) {
            m_dynamicClassPath = m_classPath.split(PATHSEPARATORCHAR);
        }

        // Step3 add to class path
        m_classPathSet.clear();
        m_classPathSet.addAll(Arrays.asList(m_dynamicClassPath));
        m_classPathSet.addAll(sourceFolders);
        for (int i = 0; i < m_dynamicClassPath.length; i++) {
            if (!isAbsolutePath(m_dynamicClassPath[i])) {
                System.out.println(m_dynamicClassPath[i]);
                // throw new IllegalArgumentException();
            }
        }

        // Step4 Calculate all JS Files
        m_jsFiles.clear();
        File sourceFolderFile = null;
        for (Iterator<String> iterator = sourceFolders.iterator(); iterator
                .hasNext();) {
            sourceFolderFile = isPathValid(iterator.next());
            if (sourceFolderFile != null) {
            	
                FileOperator.getAllJSFiles(sourceFolderFile, m_jsFiles,null);
            }
        }

        // Step5 create conf object, give default properties value
        VjetvHeadlessConfigure conf = new VjetvHeadlessConfigure();
        conf.setReportLevel("ALL");
        conf.setReportType("txt");
        conf.setValidatedJSFiles(m_jsFiles);
        conf.setReportPath(null);
        conf.setFailBuild(true);
        conf.setVerbose(m_isVerbose);
        return conf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {

        // Step1: convert properties to conf object
        IHeadlessLauncherConfigure conf = convertProperties();

        // Step2: dynamic add realted js to classloader
        initEnv();

        // Step3: do valdiate
        IHeadLessLauncher launcher = LauncherFactory
                .getVjetValidationLauncher();
        launcher.launch(conf);
    }

    /**
     * @return the classPath
     */
    public String getClassPath() {
        return m_classPath;
    }

    /**
     * @return the validateProjectName
     */
    public String getValidateProjectName() {
        return m_validateProjectName;
    }

    /**
     * Init current env Involving dynamic add class path and add to source path
     */
    private void initEnv() {
        Object o = EVHeadleassTask.class.getClassLoader();
        String classPath = null;
        for (Iterator<String> iterator = m_classPathSet.iterator(); iterator
                .hasNext();) {
            classPath = iterator.next();
            try {
                if (o instanceof AntClassLoader) {
                    ((AntClassLoader) o).addPathElement(classPath);
                } else {
                    ClassLoaderUtil.addClassPath2ClassLoader(
                            EVHeadleassTask.class.getClassLoader(), classPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        addSourcePath(m_classPathSet.toArray(new String[] {}));
    }

    /**
     * @param classPath
     *            the classPath to set
     */
    public void setClassPath(String classPath) {
        this.m_classPath = classPath;
    }

    /**
     * @param sourceLocation
     *            the sourceLocation to set
     */
    public void setSourceLocation(String sourceLocation) {
        this.m_sourceLocation = sourceLocation;
    }

    /**
     * @param validateProjectName
     *            the validateProjectName to set
     */
    public void setValidateProjectName(String validateProjectName) {
        this.m_validateProjectName = validateProjectName;
    }

    /**
     * This method use to set isVerbose value
     * 
     * @param isVerbose
     *            the isVerbose to set
     */
    public void setVerbose(boolean isVerbose) {
        this.m_isVerbose = isVerbose;
    }
}

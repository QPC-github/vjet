/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jstojava.cml.vjetv.model.impl;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;

import org.ebayopensource.dsf.jstojava.cml.vjetv.model.IHeadlessLauncherConfigure;

/**
 * Class/Interface description
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */
public class VjetvHeadlessConfigure extends IHeadlessLauncherConfigure {

    /**
     * The value is used for character storage.
     */
    private LinkedHashSet<File> m_validatedJSFiles = null;

    /**
     * The value is used for character storage.
     */
    private String m_reportLevel = null;

    /**
     * The value is used for character storage.
     */
    private boolean m_isBuild = false;

    private boolean m_verbose;

	private List<String> m_librariesToLoad;

	private List<String> m_exclusionPatterns;

    /**
     * @return the isBuild
     */
    public boolean isFailBuild() {
        return m_isBuild;
    }

    /**
     * @param isBuild
     *            the isBuild to set
     */
    public void setFailBuild(boolean isBuild) {
        this.m_isBuild = isBuild;
    }

    /**
     * @return the reportLevel
     */
    public String getReportLevel() {
        return m_reportLevel;
    }

    /**
     * @param reportLevel
     *            the reportLevel to set
     */
    public void setReportLevel(String reportLevel) {
        this.m_reportLevel = reportLevel;
    }

 
    public String getReportPath() {
        return m_reportPath;
    }

    
    public String getReportType() {
        return m_reportType;
    }

    /**
     * @param reportPath
     *            the reportPath to set
     */
    public void setReportPath(String reportPath) {
        this.m_reportPath = reportPath;
    }

    /**
     * @param reportType
     *            the reportType to set
     */
    public void setReportType(String reportType) {
        this.m_reportType = reportType;
    }

    /**
     * @return the validatedJSFiles
     */
    public LinkedHashSet<File> getValidatedJSFiles() {
        return m_validatedJSFiles;
    }

    /**
     * @param validatedJSFiles
     *            the validatedJSFiles to set
     */
    public void setValidatedJSFiles(LinkedHashSet<File> validatedJSFiles) {
        this.m_validatedJSFiles = validatedJSFiles;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ebayopensource.dsf.jstojava.cml.vjetv.model.IHeadlessLauncherConfigure#isVerbose()
     */
    @Override
    public boolean isVerbose() {
        return m_verbose;
    }

    /**
     * Set verbose
     * 
     * @param verbose
     *            boolean
     */
    public void setVerbose(boolean verbose) {
        m_verbose = verbose;
    }

	public void setLibrariesToLoad(List<String> findLibraries) {
		m_librariesToLoad= findLibraries;
		
	}
	
	public List<String> getLibrariesToLoad(){
		return m_librariesToLoad;
	}

	@Override
	public List<String> getExclusionPatterns() {
		return m_exclusionPatterns;
	}
	
	public void setExclusionPatterns(List<String> exclusionPatterns) {
		m_exclusionPatterns= exclusionPatterns;
	}
}

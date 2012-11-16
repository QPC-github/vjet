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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.ebayopensource.dsf.jstojava.cml.vjetv.model.IHeadlessParserConfigure;

/**
 * Class/Interface description
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */
public class HeadlessParserConfigure extends IHeadlessParserConfigure {

    /**
     * The value is used for character storage.
     */
    private final LinkedHashSet<File> m_validatedJSFiles = new LinkedHashSet<File>();

    /**
     * The value is used for store verbose value
     */
    private boolean m_verbose = false;
    
    /**
     * The value is used for store verbose value
     */
    private String m_policyPath = null;

    /**
     * The value is used for store verbose value
     */
    private boolean m_isFailBuild = false;

	private List<String> m_exclusionPtterns;

    /**
     * This method use to get isBuild value
     * 
     * @return the isBuild
     */
    public boolean isFailBuild() {
        return m_isFailBuild;
    }

    /**
     * This method use to set isBuild value
     * 
     * @param isBuild
     *            the isBuild to set
     */
    public void setFailBuild(boolean isBuild) {
        this.m_isFailBuild = isBuild;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ebayopensource.vjo.tool.model.IHeadlessParserConfigure#getBuildPath()
     */
    @Override
    public HashSet<File> getBuildPath() {
        return this.m_buildPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ebayopensource.vjo.tool.model.IHeadlessParserConfigure#getBootPath()
     */
    @Override
    public HashSet<File> getBootPath() {
        return this.m_bootPath;
    }

    /**
     * @return the reportLevel
     */
    @Override
    public String getReportLevel() {
        return m_reportLevel;
    }

    @Override
    public String getReportPath() {
        return m_reportPath;
    }


    @Override
    public String getReportType() {
        return m_reportType;
    }


    @Override
    public HashSet<File> getSourceLocation() {
        return m_sourceLocation;
    }

    /**
     * @return the validatedJSFiles
     */
    @Override
    public LinkedHashSet<File> getValidatedJSFiles() {
        return m_validatedJSFiles;
    }


    @Override
    public void init() {
    }

    /**
     * @param buildpath
     *            {@link HashSet}
     */
    public void appendBuildPath(HashSet<File> buildPath) {
        this.m_buildPath.addAll(buildPath);
    }

    /**
     * @param bootPath
     *            {@link HashSet}
     */
    public void appendBootPath(HashSet<File> bootPath) {
        this.m_bootPath.addAll(bootPath);
    }

    /**
     * @param reportLevel
     *            the reportLevel to set
     */
    public void setReportLevel(String reportLevel) {
        this.m_reportLevel = reportLevel;
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
     * Set source location
     * 
     * @param sourceLoacation
     *            {@link String}
     */
    public void appendSourceLoacation(HashSet<File> sourceLoacation) {
        this.m_sourceLocation.addAll(sourceLoacation);
    }

    /**
     * @param validatedJSFiles
     *            the validatedJSFiles to set
     */
    public void appendValidatedJSFiles(LinkedHashSet<File> validatedJSFiles) {
        this.m_validatedJSFiles.addAll(validatedJSFiles);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ebayopensource.vjo.tool.model.IHeadlessParserConfigure#isVerbose()
     */
    @Override
    public boolean isVerbose() {
        return m_verbose;
    }

    /**
     * Set verbose value
     * 
     * @param verbose
     *            {@link Boolean}
     */
    public void setVerbose(boolean verbose) {
        this.m_verbose = verbose;
    }

    @Override
    public String getPolicyFilePath() {
        return m_policyPath;
    }
    
    /**
     * Set policy path
     * @param path
     */
    public void setPolicyFilePath(String path){
        this.m_policyPath = path;
    }

	@Override
	public List<String> getExclusionPatterns() {
		return m_exclusionPtterns;
	}
	
	public void setExclusionPatterns(List<String> exclusionPatterns){
		m_exclusionPtterns = exclusionPatterns;
	}
	
}

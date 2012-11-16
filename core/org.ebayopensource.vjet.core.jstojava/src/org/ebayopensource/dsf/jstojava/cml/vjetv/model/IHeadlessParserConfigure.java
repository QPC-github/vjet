/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/* 
 * $Id: IHandlessParserConfigure.java,v 1.6 2008/10/29 10:44:47 green Exp $
 *
 * Copyright (c) 2006-2007 Wipro Technologies. All Rights Reserved.
 * This software program and documentation are copyrighted by Wipro 
 * Technologies.
 */
package org.ebayopensource.dsf.jstojava.cml.vjetv.model;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The interface used to implement launch configures, Include detail informaton.
 * can be used for eclipse launch configure data and command line mode
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */
public abstract class IHeadlessParserConfigure {

    /**
     * The value is used for store report path
     */
    protected String m_reportPath;

    /**
     * The value is used for store view root path
     */
    protected String m_viewRootPath;

    /**
     * The value is used for store user specify source jars path.
     */
    protected HashSet<File> m_buildPath = new HashSet<File>();

    /**
     * The value is used for store user specify source jars path.
     */
    protected HashSet<File> m_bootPath = new HashSet<File>();

    /**
     * The value is used for store report type
     */
    protected String m_reportType;

    /**
     * The value is used for store source location
     */
    protected HashSet<File> m_sourceLocation = new HashSet<File>();

    /**
     * The value is used for store report level.
     */
    protected String m_reportLevel;

    /**
     * The value is used for character storage.
     * 
     */
    public final static String ALL = "ALL";

    /**
     * The value is used for character storage.
     * 
     */
    public final static String ERROR = "ERROR";

    /**
     * The value is used for character storage.
     * 
     */
    public final static String WARNING = "WARNING";

    /**
     * Export txt type
     */
    public final static int TXT = 0;

    /**
     * Export xml type
     */
    public final static int XML = 4;

    /**
     * Exprot HTML type
     */
    public final static int HTML = 1;

    /**
     * Exprot PDF type
     */
    public final static int PDF = 2;

    /**
     * This method is used to inital different launcher configure object
     */
    public abstract void init();

    /**
     * Get report type
     * 
     * @return int
     */
    public abstract String getReportType();

    /**
     * Get genered report path
     * 
     * @return String
     */
    public abstract String getReportPath();

    /**
     * @return the sourceLocation
     */
    public abstract HashSet<File> getSourceLocation();

    /**
     * @return the validatedJSFiles
     */
    public abstract LinkedHashSet<File> getValidatedJSFiles();

    /**
     * @return the build path
     */
    public abstract HashSet<File> getBuildPath();

    /**
     * @return the boot path
     */
    public abstract HashSet<File> getBootPath();

    /**
     * @return the reportLevel
     */
    public abstract String getReportLevel();

    /**
     * @return boolean is verbose show problem information
     */
    public abstract boolean isVerbose();

    /**
     * @return boolean is build mode
     */
    public abstract boolean isFailBuild();
    
    
    /**
     * @return String get policy file path.
     */
    public abstract String getPolicyFilePath();

	public abstract List<String> getExclusionPatterns();


}

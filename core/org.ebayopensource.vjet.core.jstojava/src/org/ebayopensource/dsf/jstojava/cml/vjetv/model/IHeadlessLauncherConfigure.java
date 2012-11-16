/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/* 
 * $Id: IHeadlessLauncherConfigure.java,v 1.6 2008/10/29 10:44:47 green Exp $
 *
 * Copyright (c) 2006-2007 Wipro Technologies. All Rights Reserved.
 * This software program and documentation are copyrighted by Wipro 
 * Technologies.
 */
package org.ebayopensource.dsf.jstojava.cml.vjetv.model;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The interface used to implement launch configures, Include detail informaton.
 * can be used for eclipse launch configure data and command line mode
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */
public abstract class IHeadlessLauncherConfigure {

    /**
     * Export txt type
     */
    public final static String TXT = "TXT";

    /**
     * Export xml type
     */
    public final static String XML = "XML";

    /**
     * Exprot HTML type
     */
    public final static String HTML = "HTML";

    /**
     * Exprot PDF type
     */
    public final static String PDF = "PDF";

    /**
     * The value is used for store report path
     */
    protected String m_reportPath;

    /**
     * The value is used for store report type
     */
    protected String m_reportType;

    /**
     * The value is used for store source location
     */
    protected String[] m_sourceLocation;

    /**
     * Get genered report path
     * 
     * @return String
     */
    public abstract String getReportPath();

    /**
     * Get report type
     * 
     * @return int
     */
    public abstract String getReportType();

    /**
     * Report verbose
     * 
     * @return boolean
     */
    public abstract boolean isVerbose();

    /**
     * @return the validatedJSFiles
     */
    public abstract LinkedHashSet<File> getValidatedJSFiles();

    /**
     * @return boolean
     */
    public abstract boolean isFailBuild();

    /**
     * @return String
     */
    public abstract String getReportLevel();
    
    public abstract List<String> getLibrariesToLoad();

	public abstract List<String> getExclusionPatterns();
}

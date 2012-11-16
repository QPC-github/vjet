/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.ebayopensource.dsf.jstojava.cml.vjetv.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.ebayopensource.dsf.jstojava.cml.vjetv.messages.Messages;
import org.ebayopensource.dsf.jstojava.cml.vjetv.model.IHeadLessLauncherResult;
import org.ebayopensource.dsf.jstojava.cml.vjetv.model.IHeadlessLauncherConfigure;
import org.ebayopensource.dsf.jstojava.cml.vjetv.model.IHeadlessParserConfigure;
import org.ebayopensource.dsf.jstojava.cml.vjetv.model.impl.HeadlessParserConfigure;
import org.ebayopensource.dsf.jstojava.cml.vjetv.model.impl.VjetvHeadlessConfigure;
import org.ebayopensource.dsf.jstojava.cml.vjetv.util.ParserHelper;

/**
 * Class/Interface description
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */
public class HeadLessValidationEntry {
    /**
     * Convert command parser configuration to vjetv configuration
     * 
     * @param parserConf
     *            {@link IHeadlessParserConfigure}
     * @return vjetv headless configuration {@link VjetvHeadlessConfigure}
     */
    private static VjetvHeadlessConfigure convert2VjetvConfig(
            IHeadlessParserConfigure parserConf) {
        VjetvHeadlessConfigure vjetvConf = new VjetvHeadlessConfigure();
        vjetvConf.setLibrariesToLoad(findLibraries(parserConf.getBuildPath()));
        vjetvConf.setExclusionPatterns(parserConf.getExclusionPatterns());
        vjetvConf.setReportType(parserConf.getReportType());
        vjetvConf.setValidatedJSFiles(parserConf.getValidatedJSFiles());
        vjetvConf.setReportLevel(parserConf.getReportLevel());
        vjetvConf.setReportPath(parserConf.getReportPath());
        vjetvConf.setReportType(parserConf.getReportType());
        vjetvConf.setVerbose(parserConf.isVerbose());
        vjetvConf.setFailBuild(parserConf.isFailBuild());
        return vjetvConf;
    }

    private static List<String> findLibraries(HashSet<File> buildPath) {
		List<String> libs = new ArrayList<String>();
		for (File path : buildPath) {
			if(path.getAbsolutePath().endsWith(".zip")){
				libs.add(path.getAbsolutePath());
			}
		}

		return libs;
	}

	/**
     * VjetV entry
     * 
     * @param args
     */
    public static void main(String[] args) {

        if (args.length != 0) {
            runVjetv(args);
        } else {
            ParserHelper.printOptionsHelp(ArgumentsParser.getEVOptions(),
                    Messages.getString("VjetVEntry.ADDCOMMAND")); //$NON-NLS-1$
            System.exit(0);
        }
    }

    /**
     * Run vjetv with String args
     * 
     * @param args
     *            String[]
     * @return {@link IHeadLessLauncherResult}
     */
    public static IHeadLessLauncherResult runVjetv(String[] args) {
        ArgumentsParser parser = new ArgumentsParser();
        HeadlessParserConfigure parserConf = new HeadlessParserConfigure();
        parser.parser(args, parserConf);
        parser.initEnv(parserConf);
        IHeadlessLauncherConfigure vjetvConf = convert2VjetvConfig(parserConf);
        IHeadLessLauncherResult result = LauncherFactory.getVjetValidationLauncher().launch(vjetvConf);
        if (result.getErrorNumber() > 0 && parserConf.isFailBuild()) {
            throw new RuntimeException("There are  "
                    + result.getErrorNumber()
                    + " validation errors, please fix it");
        }
        return result; 
    }
}

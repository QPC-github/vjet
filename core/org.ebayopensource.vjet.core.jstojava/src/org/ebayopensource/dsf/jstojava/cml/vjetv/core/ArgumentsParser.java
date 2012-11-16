/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jstojava.cml.vjetv.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import org.ebayopensource.dsf.jsgen.shared.validation.vjo.IVjoSemanticRule;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.IVjoSemanticRuleSet;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.VjoSemanticRulePolicy;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.semantic.rules.VjoSemanticRuleRepo;
import org.ebayopensource.dsf.jstojava.cml.vjetv.messages.Messages;
import org.ebayopensource.dsf.jstojava.cml.vjetv.model.IHeadlessParserConfigure;
import org.ebayopensource.dsf.jstojava.cml.vjetv.model.impl.HeadlessParserConfigure;
import org.ebayopensource.dsf.jstojava.cml.vjetv.parser.BasicParser;
import org.ebayopensource.dsf.jstojava.cml.vjetv.parser.CommandLine;
import org.ebayopensource.dsf.jstojava.cml.vjetv.parser.OptionGroup;
import org.ebayopensource.dsf.jstojava.cml.vjetv.parser.Options;
import org.ebayopensource.dsf.jstojava.cml.vjetv.parser.ParseException;
import org.ebayopensource.dsf.jstojava.cml.vjetv.util.FileOperator;
import org.ebayopensource.dsf.jstojava.cml.vjetv.util.ParserHelper;
import org.ebayopensource.dsf.jstojava.loader.OnDemandAllTypeLoader;

/**
 * This class is used to parse command from end user when the service target is
 * -t or -tc.
 * 
 * @author <a href="mailto:liama@ebay.com">liama</a>
 * @since JDK 1.5
 */
public class ArgumentsParser {

    /**
     * Short single-character for store blank char
     */
    public final static String BLANK = " "; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String ARGFILE_PREFIX = "@"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String BUILD_PATH = "bp"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String BOOT_PATH = "bootp"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String SOURCE_PATH = "sp"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String REPORT_PATH = "d"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String IMPORT_POLICY = "policy"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String EXPORT_POLICY = "ep"; //$NON-NLS-1$
    
	private static final String EXCLUSION_RULE = "exclude";

    /**
     * Short single-character name of the option.
     */
    private static final String REPORT_TYPE = "rt"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String NO_WARN = "nw"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String VERBOSE = "vb"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String BUILD = "b"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final int SHORT_OPTION = 0;

    /**
     * longOpt Long multi-character name of the option
     */
    private static final int LONG_OPTION = 1;

    /**
     * description Self-documenting description
     */
    private static final int OPTION_DESC = 2;

    /**
     * Short single-character name of the option.
     */
    private static final String ARGFILE = "ARGFILE"; //$NON-NLS-1$

    /**
     * Short single-character name of the option.
     */
    private static final String SOURCEFILE = "SOURCEFILE"; //$NON-NLS-1$

    /**
     * command line options. Function target option string array should be
     * register here. first element of this array is short representation of
     * option, second element is the long representation of option, and the
     * third element is the description of the option.
     */
    final static String[][] OPTIONARAAY = new String[][] {
            new String[] { "h", //$NON-NLS-1$
                    "help", //$NON-NLS-1$
                    "Print a synopsis of standard options" }, //$NON-NLS-1$
            new String[] { BUILD, //$NON-NLS-1$
                    "build", //$NON-NLS-1$
                    "Validator will throw exception when error problems exist" },
            new String[] { "v", "version", "Display VJET headless version" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            new String[] { NO_WARN, "nowarn", "Disable warning problems" }, //$NON-NLS-1$
            new String[] {
                    VERBOSE,
                    "verbose", "Verbose output. This includes information about each JS validated" }, //$NON-NLS-1$ //$NON-NLS-2$
            new String[] { BUILD_PATH,
                    "buildPath", //$NON-NLS-1$
                    "Specify the source code path to search for JS files, Source path entries are separated by semicolons(;) "
                            + "and can be directories, JAR archives." }, //$NON-NLS-1$
            new String[] { BOOT_PATH, "bootPath", "Boot class path" },
            new String[] { REPORT_PATH, "reportPath", "report path" },
            new String[] { IMPORT_POLICY, "importPolicy",
                    "Import validation policy" },
            new String[] { EXPORT_POLICY, "exportPolicy",
                    "Export valdiation policy" },
            new String[] { "excl", //$NON-NLS-1$
            		EXCLUSION_RULE, //$NON-NLS-1$
                            "Excludes directories or files matching pattern ie *mypath/*" } //$NON-NLS-1$
    // new String[] { REPORT_TYPE, "reportType", "report file type" },

    }; //$NON-NLS-1$



    /**
     * Calculate all env
     * 
     * @param conf
     *            {@link IHeadlessParserConfigure}
     * @param buildPathSets
     */
    private static void calculateAllEnv(IHeadlessParserConfigure conf,
            HashSet<File> buildPathSets) {
        HashSet<File> buildPath = conf.getBuildPath();
        HashSet<File> sourceLocation = conf.getSourceLocation();

        // Step1: Get all validated JS files from user specify source location
        handleValidatedJSFiles((HeadlessParserConfigure) conf);

        // Step2: Add user specify source location to build path
        File sourceFolder = null;
        for (Iterator<File> iterator = sourceLocation.iterator(); iterator
                .hasNext();) {
            sourceFolder = iterator.next();
            if (sourceFolder.isDirectory() && sourceFolder.exists()) {
                buildPathSets.add(sourceFolder);
            }
        }
        
        // Step3: Add user specify build path
        if (buildPath.size() > 0) {
            buildPathSets.addAll(buildPath);
        }


    }

    /**
     * Create launch class path. include DSFPrebuild.jar, DSFAllJar,
     * eclipse.core.jdt.jar and depend project class apth.
     * 
     * @param buildPathSets
     *            {@link HashSet}
     */
    private static void createLaunchClassPath(HeadlessParserConfigure conf,
            HashSet<File> buildPathSets) {
        ValidateClassLoader loader = new ValidateClassLoader(
                convertFileToAbPath(conf.getBootPath())
                        .toArray(new String[] {}), new String[] {});
        loader.addSourcePath(convertFileToAbPath(buildPathSets).toArray(
                new String[] {}));
    }

    /**
     * Convert file to absolute path
     * 
     * @param files
     *            {@link HashSet}
     * @return HashSet<String>
     */
    private static HashSet<String> convertFileToAbPath(HashSet<File> files) {
        HashSet<String> paths = new LinkedHashSet<String>();
        File file = null;
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
            file = iterator.next();
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    /**
     * create command line options.
     * 
     * @return Options
     */
    private static Options createOptions() {
        Options opts = new Options();
        for (int i = 0; i < OPTIONARAAY.length; i++) {
            opts.addOption(OPTIONARAAY[i][SHORT_OPTION],
                    OPTIONARAAY[i][LONG_OPTION], i > 4 ? true : false,
                    OPTIONARAAY[i][OPTION_DESC]);

        }
        OptionGroup og = new OptionGroup();
        og.addOption(opts.getOption("h")); //$NON-NLS-1$
        og.addOption(opts.getOption("v")); //$NON-NLS-1$
        OptionGroup og1 = new OptionGroup();
        og1.addOption(opts.getOption(IMPORT_POLICY)); //$NON-NLS-1$
        og1.addOption(opts.getOption(EXPORT_POLICY)); //$NON-NLS-1$
        opts.addOptionGroup(og);
        opts.addOptionGroup(og1);

        return opts;
    }

    /**
     * Get all validate JSF files from user specify source location
     * 
     * @param conf
     *            {@link IHeadlessParserConfigure}
     * @return {@link LinkedHashSet}
     */
    private static LinkedHashSet<File> getAllValidateJSFiles(
            HeadlessParserConfigure conf) {
        LinkedHashSet<File> lists = new LinkedHashSet<File>();
        File rootFile;
        String sourcePath;
        HashSet<File> sourcePaths = conf.getSourceLocation();
        for (Iterator<File> iterator = sourcePaths.iterator(); iterator
                .hasNext();) {
            rootFile = iterator.next();
            sourcePath = rootFile.getAbsolutePath().trim();
            if (sourcePath.endsWith(FileOperator.JS_FOLDER_SUFFIX)) {
                sourcePath = sourcePath.substring(0, sourcePath
                        .lastIndexOf(File.separatorChar));
                rootFile = new File(sourcePath);
            }
            if (!isPathValid(rootFile, true)) {
                continue;
            }
            char[][] exclusionPatternsChar = new char[][]{};
			exclusionPatternsChar = OnDemandAllTypeLoader.processPatterns(
					conf.getExclusionPatterns());
            FileOperator.getAllJSFiles(rootFile, lists, exclusionPatternsChar);

            // Add tempfolder's js files
            rootFile = new File(FileOperator.TEMPFOLDER);
            if (isPathValid(rootFile, false)) {
                FileOperator.getAllJSFiles(rootFile, lists,exclusionPatternsChar);
            }
            
            
 
            
        }
        return lists;
    }

    /**
     * get Depend jars path
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @return HashSet<String>
     */
    private static HashSet<File> getBuildPath(CommandLine commandLine) {
        String classPaths = commandLine.getOptionValue(BUILD_PATH);
        return createPath(classPaths);
    }

    /**
     * @param classPaths
     * @return HashSet<String>
     */
    private static HashSet<File> createPath(String classPaths) {
        if (null == classPaths) {
            ParserHelper.exitSystem(Messages
                    .getString("ArgumentsParser.NEEDCLASSPATH")); //$NON-NLS-1$
        }
        String[] paths = classPaths.split(FileOperator.SEPARATOR);
        String path = null;
        HashSet<File> al = new LinkedHashSet<File>();
        File file = null;
        for (int i = 0; i < paths.length; i++) {
            path = paths[i].trim();
            file = new File(path);
            path = file.getAbsolutePath();
            if (path.endsWith(FileOperator.JAR_FOLDER_SUFFIX)) {
                path = path.trim().substring(0,
                        path.indexOf(FileOperator.JAR_FOLDER_SUFFIX));
                file = new File(path);
                if (isPathValid(file, true)) {
                    FileOperator.getJarsFiles(file, al);
                }
            }
            if (!path.equals("") && isPathValid(file, true)) { //$NON-NLS-1$
                al.add(file);
            }
        }
        return al;
    }

    /**
     * get Depend jars path
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @return HashSet<File>
     */
    private static HashSet<File> getBootPath(CommandLine commandLine) {
        String classPaths = commandLine.getOptionValue(BOOT_PATH);
        return createPath(classPaths);
    }

    /**
     * parse command line from end user and return configuration valu
     * 
     * @param commandLine
     *            <code>CommandLine</code> command line from end user.
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static IHeadlessParserConfigure getConfiguration(
            CommandLine commandLine, HeadlessParserConfigure conf) {

        // Stpe1: handle -v or -h arg
        if (commandLine.hasOption('v')) {
            System.out.println(Messages
                    .getString("ArgumentsParser.VJETVERSION")); //$NON-NLS-1$
            ParserHelper.exitSystem();
        } else if (commandLine.hasOption('h')) {
            printHelp();
        }

        // Step2 handle arg files
        handleArgFiles(commandLine, conf);

        // Step3: handle source location and depend project relation
        handleSourceLoacations(commandLine, conf);

        // Step4: handle build path
        handleBuildPath(commandLine, conf);
        

        // Step5: handle bootstrap path
        handleBootPath(commandLine, conf);

        // Step6: Handle report path default is user dir
        handleReportPath(commandLine, conf);

        // Step7: Handle report type default is txt
        handleReportType(commandLine, conf);

        // Step9: Get report level, For example error, warning, all
        handleReportLevel(commandLine, conf);

        // Step10; get verbose arg
        handleVerbose(commandLine, conf);

        // Step11: handle build mode
        handleBuildMode(commandLine, conf);

        // Step12: handle import function
        handleImportFunction(commandLine, conf);

        // Step13: handle export function
        handleExportFunction(commandLine, conf);
        
        // Step 14 handle exclusion rules
        handleExclusionRules(commandLine, conf);
        
        return conf;
    }

    private static void handleExclusionRules(CommandLine commandLine,
			HeadlessParserConfigure conf) {
    	  String value = commandLine.getOptionValue(EXCLUSION_RULE);
          if (value == null)
              return;
          String[] rules = value.split(",");
          List<String> ruleList = new ArrayList<String>(rules.length);
          for(String rule :rules){
        	 ruleList.add(rule);
      
          }
          conf.setExclusionPatterns(ruleList);
	}

	/**
     * Handle import validation policy function.
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static void handleImportFunction(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        String path = commandLine.getOptionValue(IMPORT_POLICY);
        if (path == null)
            return;
        if (isPathValid(path, true)) {
            conf.setPolicyFilePath(path);
        }
    }

    /**
     * Handle export validation policy function.
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static void handleExportFunction(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        String path = commandLine.getOptionValue(EXPORT_POLICY);
        if (path == null)
            return;
        exportPolicy2File(path);
    }

    /**
     * Export policy to user specify file
     * 
     * @param path
     */
    private static void exportPolicy2File(String path) {
        File exportFile = new File(path);
        if (isPathValid(exportFile, true)) {
            Properties pro = new Properties();
            String ruleSetName = "";
            VjoSemanticRuleRepo ruleRepo = VjoSemanticRuleRepo.getInstance();
            for (IVjoSemanticRuleSet ruleSet : ruleRepo.getRuleSets()) {
                ruleSetName = ruleSet.getRuleSetName();
                for (IVjoSemanticRule<?> rule : ruleSet.getRules()) {
                    pro.put(ruleSetName + "." + rule.getRuleName(), rule
                            .getGlobalRulePolicy().getProblemSeverity(null)
                            .toString());
                }
            }
            try {
                if (exportFile.canWrite()) {
                    pro.store(new FileOutputStream(path), "");
                    ParserHelper
                            .exitSystem("Successful export validation policy to :"
                                    + path);
                } else {
                    System.out.println("Can't write File :" + path);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle build mode
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static void handleBuildMode(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        if (commandLine.hasOption(BUILD)) {
            conf.setFailBuild(true);
        }
    }

    /**
     * Handle verbose value
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static void handleVerbose(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        if (commandLine.hasOption(VERBOSE)) {
            conf.setVerbose(true);
        } else {
            conf.setVerbose(false);
        }
    }

    /**
     * Handle arg files, allow user specify multi_arg files
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static void handleArgFiles(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        HashSet<File> argFiles = getNoRecognizedValue(commandLine, ARGFILE);

        File argFile = null;
        String args = null;
        for (Iterator<File> iterator = argFiles.iterator(); iterator.hasNext();) {
            argFile = iterator.next();
            args = readArgFile(argFile);
            ArgumentsParser parser = new ArgumentsParser();
            parser.parser(args.split(BLANK), conf);
        }
    }

    /**
     * Read arg file to args
     * 
     * @param f
     *            {@link File} argFile
     * @return args
     */
    public static String readArgFile(File f) {
        StringBuffer sb = new StringBuffer();
        try {
            InputStreamReader isr = new InputStreamReader(
                    new FileInputStream(f));
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            if (line != null) {
                line = line.trim();
            }
            while (line != null) {
                if (line.endsWith(FileOperator.SEPARATOR)) {
                    sb.append(line);
                } else {
                    sb.append(line + BLANK);
                }
                line = br.readLine();
                if (line != null) {
                    line = line.trim();
                } else {
                    break;
                }
                while (line.equals("") || line.equals(BLANK)) { //$NON-NLS-1$
                    line = br.readLine();
                    if (line != null) {
                        line = line.trim();
                    } else {
                        break;
                    }
                }
            }
            isr.close();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Calculate validated js files
     * 
     * @param conf
     *            void
     */
    private static void handleValidatedJSFiles(HeadlessParserConfigure conf) {
        conf.appendValidatedJSFiles(getAllValidateJSFiles(conf));
        
        
        
    }

    /**
     * Handle reprot type
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static void handleReportType(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        conf.setReportType(getReportType(commandLine));
    }

    /**
     * Handle report path default is user dir
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static void handleReportPath(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        conf.setReportPath(getReportPath(commandLine));
    }

    /**
     * Handle boot path
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static void handleBootPath(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        if (commandLine.hasOption(BOOT_PATH)) {
            conf.appendBootPath(getBootPath(commandLine));
        }
    }

    /**
     * Handle source locations which user speicified. Include: absolute path.
     * relative path. *.js A.js
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     * 
     */
    private static void handleSourceLoacations(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        conf
                .appendSourceLoacation(getNoRecognizedValue(commandLine,
                        SOURCEFILE));
    }

    /**
     * Get source location args from commandline object
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @return {@link HashSet}
     */
    private static HashSet<File> getNoRecognizedValue(CommandLine commandLine,
            String type) {
        List<String> allArgs = commandLine.getArgList();
        HashSet<File> argFiles = new HashSet<File>();
        if (allArgs.size() == 0)
            return argFiles;
        String arg = null;
        File file = null;
        for (Iterator<String> iterator = allArgs.iterator(); iterator.hasNext();) {
            arg = iterator.next().trim();
            String[] args = arg.split(FileOperator.SEPARATOR);
            String tempString = null;
            for (int i = 0; i < args.length; i++) {
                tempString = args[i].trim();
                if (tempString.equals("")) //$NON-NLS-1$
                    continue;
                if (type.equalsIgnoreCase(ARGFILE)) {
                    addArgFiles(argFiles, file, tempString);
                } else if (type.equalsIgnoreCase(SOURCEFILE)) {
                    if (tempString.startsWith(ARGFILE_PREFIX))
                        continue;
                    addSourceFolder(argFiles, tempString);
                }
            }
        }
        return argFiles;
    }

    /**
     * Add source folder to collection
     * 
     * @param argFiles
     *            {@link HashSet}
     * @param tempString
     *            {@link String}
     */
    private static void addSourceFolder(HashSet<File> argFiles,
            String tempString) {
        tempString = tempString.trim();
        File file;
        if (tempString.endsWith(FileOperator.JS_FOLDER_SUFFIX)) {
            tempString = tempString.substring(0, tempString
                    .lastIndexOf(FileOperator.JS_FOLDER_SUFFIX));
        }
        file = new File(tempString);
        if (!tempString.equals("") && isPathValid(tempString)) {
            argFiles.add(file);
        }
    }

    /**
     * Add arg files to collection
     * 
     * @param argFiles
     *            {@link HashSet}
     * @param file
     *            {@link File}
     * @param tempString
     *            {@link String}
     */
    private static void addArgFiles(HashSet<File> argFiles, File file,
            String tempString) {
        if (tempString.startsWith(ARGFILE_PREFIX)) {
            tempString = tempString.trim();
            if (!(tempString.length() > 1)) {
                ParserHelper.exitSystem(Messages
                        .getString("ArgumentsParser.CHECKARGPATH") //$NON-NLS-1$
                        + tempString);
            }
            tempString = tempString.substring(1);
            file = new File(tempString);
            if (!tempString.equals("") && isPathValid(file, true)) { //$NON-NLS-1$
                argFiles.add(file);
            }
        }
    }

    /**
     * gets the command line options for ebay headless validation
     * 
     * @return the wtOptions
     */
    public static Options getEVOptions() {
        return createOptions();
    }

    /**
     * get report level
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     * @return String
     */
    private static void handleReportLevel(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        if (commandLine.hasOption(NO_WARN)) {
            conf.setReportLevel(IHeadlessParserConfigure.ERROR);
        } else {
            conf.setReportLevel(IHeadlessParserConfigure.ALL);
        }
    }

    /**
     * Get report path, if not specify it.current path as default value
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @return String
     */
    private static String getReportPath(CommandLine commandLine) {
        return commandLine.getOptionValue(REPORT_PATH);
    }

    /**
     * Get report type
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @return String
     */
    private static String getReportType(CommandLine commandLine) {
        String reportFileType = commandLine.getOptionValue(REPORT_TYPE,
                ParserHelper.XML);
        if (reportFileType.equalsIgnoreCase(ParserHelper.TXT)
        // || reportFileType.equalsIgnoreCase(ParserHelper.HTML)
                // || reportFileType.equalsIgnoreCase(ParserHelper.PDF)
                || reportFileType.equalsIgnoreCase(ParserHelper.XML)) {
            return reportFileType;
        } else {
            return ParserHelper.XML;
        }
    }

    /**
     * handle specify jars
     * 
     * @param commandLine
     *            {@link CommandLine}
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    private static void handleBuildPath(CommandLine commandLine,
            HeadlessParserConfigure conf) {
        if (commandLine.hasOption(BUILD_PATH)) {
            conf.appendBuildPath(getBuildPath(commandLine));
        }
    }

    /**
     * Initilize laucher env
     * 
     * @param conf
     *            {@link HeadlessParserConfigure}
     */
    public void initEnv(HeadlessParserConfigure conf) {
        HashSet<File> buildPathSets = new LinkedHashSet<File>();

        // Step1 : Get all env path
        calculateAllEnv(conf, buildPathSets);

        // Step2 : Add new class paths to current class loader
        createLaunchClassPath(conf, buildPathSets);

        // Step3 : Import user specify policy
        importPolicy(conf);

    }

    /**
     * Import validation policy to current validation rule's repo
     * @param conf
     */
    private void importPolicy(HeadlessParserConfigure conf) {
        String path = conf.getPolicyFilePath();
        if(path == null)
            return;
        if (isFilePathValid(new File(path))) {
            if (path != null) {
                Properties pro = new Properties();
                FileInputStream fi = null;
                try {
                    fi = new FileInputStream(path);
                    pro.load(fi);
                    String ruleSetName = "";
                    String proKey = "";
                    VjoSemanticRuleRepo ruleRepo = VjoSemanticRuleRepo
                            .getInstance();
                    for (IVjoSemanticRuleSet ruleSet : ruleRepo.getRuleSets()) {
                        ruleSetName = ruleSet.getRuleSetName();
                        for (IVjoSemanticRule<?> rule : ruleSet.getRules()) {
                            proKey = ruleSetName + "." + rule.getRuleName();
                            rule.setGlobalPolicy(getRulePolicy(pro
                                    .getProperty(proKey)));
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fi != null) {
                            fi.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    /**
     * Judge user specify jar path is valid
     * 
     * @param path
     *            String
     * @return boolean
     */
    public static boolean isJarPathValid(String path) {
        File file = new File(path);
        if (file.exists() && file.canRead()
                && file.getAbsolutePath().endsWith(FileOperator.JAR_SUFFIX)) {
            return true;
        } else {
            System.out.println(Messages
                    .getString("ArgumentsParser.CHECKJARPATH") //$NON-NLS-1$
                    + file.getAbsolutePath());
            ParserHelper.exitSystem();
            return false;
        }
    }

    /**
     * Judge user specify path is valid
     * 
     * @param path
     *            String
     * @return boolean
     */
    public static boolean isPathValid(String path) {
        return isPathValid(path, true);
    }

    /**
     * Judge user specify path is valid
     * 
     * @param path
     *            String
     * @param exist
     *            boolean specify if path is invalid. exist system
     * @return boolean
     */
    public static boolean isPathValid(String path, boolean exist) {
        if (null == path) {
            ParserHelper.exitSystem(Messages
                    .getString("ArgumentsParser.CHECKPATH")); //$NON-NLS-1$
        } else {
            path = path.trim();
        }
        File pathFile = new File(path);
        return isPathValid(pathFile, exist);
    }

    /**
     * Judge user specify path is valid
     * 
     * @param path
     *            File
     * @param exist
     *            boolean specify if path is invalid. exist system
     * @return boolean
     */
    private static boolean isPathValid(File pathFile, boolean exist) {
        if (pathFile.exists() && pathFile.canRead()) {
            return true;
        } else {
            if (exist) {
                ParserHelper.exitSystem(Messages
                        .getString("ArgumentsParser.CHECKPATH") //$NON-NLS-1$
                        + pathFile.getAbsolutePath());
            }
            return false;
        }
    }
    
    /**
     * Judge user specify file path is valid
     * 
     * @param path
     *            File
     * @param exist
     *            boolean specify if path is invalid. exist system
     * @return boolean
     */
    private static boolean isFilePathValid(File file) {
        if(isPathValid(file, true)){
            if(file.isFile()){
                return true;
            }
        }
        ParserHelper.exitSystem(Messages
                .getString("ArgumentsParser.CHECKPATH") //$NON-NLS-1$
                + file.getAbsolutePath());
        return false; 
    }

    /**
     * print command line help and exit the system.
     * 
     */
    private static void printHelp(String message) {
        ParserHelper.printOptionsHelp(getEVOptions(), message);
        ParserHelper.exitSystem();
    }

    /**
     * print command line help and exit the system.
     * 
     */
    private static void printHelp() {
        printHelp("vjetv <options> <source files> \n"
                + "Where possible options include: \n");
    }

    /**
     * parse command line from end user and generate configuration for Test Case
     * Reporting.
     * 
     * @param args
     *            String[] command line from end user.
     * @param conf
     *            {@link HeadlessParserConfigure}
     * @return {@link JunitConfiguration} configuration for Test Case Reporting.
     */
    public final IHeadlessParserConfigure parser(String[] args,
            HeadlessParserConfigure conf) {
        CommandLine commandLine = null;
        BasicParser parser = new BasicParser();
        try {
            commandLine = parser.parse(getEVOptions(), args);
        } catch (ParseException e) {
            printHelp(e.getMessage());
        }
        if (conf != null) {
            return getConfiguration(commandLine, conf);
        } else {
            return null;
        }
    }

    private VjoSemanticRulePolicy getRulePolicy(String severity) {
        if ("error".equalsIgnoreCase(severity)) {
            return VjoSemanticRulePolicy.GLOBAL_ERROR_POLICY;
        } else if ("warning".equalsIgnoreCase(severity)) {
            return VjoSemanticRulePolicy.GLOBAL_WARNING_POLICY;
        } else if ("ignore".equalsIgnoreCase(severity)) {
            return VjoSemanticRulePolicy.GLOBAL_IGNORE_POLICY;
        }
        return VjoSemanticRulePolicy.GLOBAL_ERROR_POLICY;
    }

}

/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jstojava.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.ebayopensource.dsf.jsgroup.bootstrap.JsLibBootstrapLoader;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.IJstTypeReference;
import org.ebayopensource.dsf.jst.SimpleBinding;
import org.ebayopensource.dsf.jst.declaration.JstCache;
import org.ebayopensource.dsf.jst.declaration.JstObjectLiteralType;
import org.ebayopensource.dsf.jst.declaration.JstType;
import org.ebayopensource.dsf.jst.ts.IJstTypeLoader;
import org.ebayopensource.dsf.jst.ts.util.JstRefTypeDependencyCollector;
import org.ebayopensource.dsf.jst.ts.util.JstTypeSerializer;
import org.ebayopensource.dsf.jstojava.parser.VjoParser;
import org.ebayopensource.dsf.jstojava.translator.TranslateConfig;
import org.ebayopensource.dsf.ts.event.group.AddGroupEvent;
import org.ebayopensource.dsf.util.JavaSourceLocator;


public class OnDemandAllTypeLoader implements IJstTypeLoader {

    private static final String ONDEMAND = "ONDEMAND";

    private static final String JS = ".js";

	private static final String SUFFIX_ZIP = ".zip";

	private static final String SUFFIX_JAR = "jar";

    private IJstType m_type;

    private String m_group = ONDEMAND;

    private List<SourceType> m_sources;

    private Map<String, String> m_lookedup = new HashMap<String, String>();

	private List<String> m_exclusionRules;

    public OnDemandAllTypeLoader(IJstType type) {
        m_type = type;
    }

    public OnDemandAllTypeLoader(String group, IJstType type) {
        m_type = type;
        m_group = group;
    }
    
    public OnDemandAllTypeLoader(String group, IJstType type, List<String> exclusionRules) {
        m_type = type;
        m_group = group;
        m_exclusionRules = exclusionRules;
    }

    protected void findSources(IJstType type) {
        JavaSourceLocator locator = JavaSourceLocator.getInstance();
        // Currently, only inactive needs, and mixins are required dependencies
        // for generation

        for (IJstTypeReference ineed : JstRefTypeDependencyCollector
                .getDependency(type).values()) {
            addSource(locator, ineed.getReferencedType());
            findSources(ineed.getReferencedType().getName()); // RECURSIVE
        }

        for (IJstType ineed : type.getImports()) {
            if (ineed.getAlias() != null) {
                addSource(locator, ineed);
                findSources(ineed.getName());
            }
        }

    }

    public List<SourceType> loadJstTypes(List<AddGroupEvent> groupList) {
    	
    	
    	
        m_sources = new ArrayList<SourceType>(5);
        JavaSourceLocator locator = JavaSourceLocator.getInstance();
        if(m_type.getName()!=null){
        	addSource(locator, m_type);
            findSources(m_type);
            ArrayList<SourceType> rev = new ArrayList<SourceType>();
            for (int i = m_sources.size() - 1; i >= 0; i--) {
                rev.add(m_sources.get(i));
            }
            return rev;
        }else{
        	// check if group is library
        	for(AddGroupEvent event: groupList){
        		if(event.getGroupPath()!=null && event.getGroupPath().endsWith(SUFFIX_ZIP)){
        			return loadJstTypesFromLibrary(event.getGroupName(), new File(event.getGroupPath()));
        		}
        	}
        }
        return Collections.EMPTY_LIST;
        
    }
    
	public static boolean isBinaryPath(String path) {
		return path.endsWith(SUFFIX_ZIP) || path.endsWith(SUFFIX_JAR);
	}
    
	protected List<SourceType> loadJstTypesFromLibrary(String groupName,
			File libFile) {
		List<SourceType> typeList = new ArrayList<SourceType>();

		String libFileName = libFile.getName().toLowerCase();

		if (isBinaryPath(libFileName)) {
			ZipFile jarFile = null;
			try {
				jarFile = new ZipFile(libFile);
				
				// load in bootstrap.js first
				ZipEntry bootstrapEntry = jarFile.getEntry("bootstrap.js");
				if(bootstrapEntry!=null){
					InputStream stream = jarFile.getInputStream(bootstrapEntry);
					JsLibBootstrapLoader.load(VjoParser.load(stream, "bootstrap.js"), groupName);
				}
				
				Enumeration<? extends ZipEntry> enumeration = jarFile.entries();

				while (enumeration.hasMoreElements()) {

					ZipEntry elem = enumeration.nextElement();
					
					if (elem.getName().endsWith(".ser")) {
						typeList.addAll(loadAllTypes(groupName, jarFile, elem));
					}
					else if(!elem.getName().contains("bootstrap.js")) {
						typeList.add(createType(groupName, jarFile, elem));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					jarFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}

		return typeList;
	}
	
	protected SourceType createType(String groupName, ZipFile jarFile,
			ZipEntry elem) throws IOException {

		String typeName = elem.getName();
		if (!typeName.endsWith(JS)) {
			return null;
		}
		int end = typeName.lastIndexOf(JS); // remove .js


		typeName = typeName.substring(0, end);
		typeName = typeName.replace("\\", ".");
		typeName = typeName.replace("/", ".");

		InputStream stream = jarFile.getInputStream(elem);
		byte[] bs = new byte[stream.available()];
		stream.read(bs);
		stream.close();

		String source = new String(bs);

		File f = null;
		f = new File(jarFile.getName() + "!"
				+ elem.getName());
		
		SourceType srcType = new SourceType(groupName, typeName, source, f);

		return srcType;

	}
	
	protected List<SourceType> loadAllTypes(String groupName, ZipFile jarFile, ZipEntry elem) throws IOException {
		
		InputStream stream = jarFile.getInputStream(elem);
		
		List<IJstType> jstTypes = JstTypeSerializer.getInstance().deserialize(stream);
		
		List<SourceType> srcTypes = new ArrayList<SourceType>();
		
		for (IJstType type : jstTypes) {
			if(JstCache.getInstance().getType(type.getName())==null){
				JstCache.getInstance().addType((JstType)type);
				if(type.getAliasTypeName()!=null && type instanceof JstObjectLiteralType){
					JstCache.getInstance().addAliasType(type.getAliasTypeName(), (JstObjectLiteralType)type);
				}
				
			}
			srcTypes.add(new SourceType(groupName, type));			
		}
		
		return srcTypes;		
	}	

	

    protected void findSources(String typeName) {
        if (alreadyLookedup(typeName)) {
            return;
        }

        JavaSourceLocator locator = JavaSourceLocator.getInstance();
        URL url = locator.getSourceUrl(typeName, ".js");
        if (url == null) {
            url = locator.getSourceUrl(typeName, ".vjo");
        }
        if (url != null) {
            TranslateConfig cfg = new TranslateConfig();
            cfg.setSkiptImplementation(true);
            VjoParser p = new VjoParser(cfg);
            // System.out.println("url: "+url);
            IJstType type = p.parse(m_group, url).getType();
            if (type != null) {
                findSources(type);
            }
        }
    }

    protected boolean alreadyLookedup(String typeName) {
        if (m_lookedup.get(typeName) != null) {
            return true;
        }

        m_lookedup.put(typeName, "");
        return false;
    }

    protected void addSource(JavaSourceLocator locator, IJstType ineed) {
        URL url = locator.getSourceUrl(ineed.getName(), JS);

        if (url == null && ineed.getSource() != null
                && ineed.getSource().getBinding() != null
                && ineed.getSource().getBinding() instanceof SimpleBinding) {
            try {
                File f = new File(((SimpleBinding) ineed.getSource()
                        .getBinding()).getName());
                url = f.toURI().toURL();
            } catch (MalformedURLException e) {
                // ignore error

            }
        }
        if (url != null) {
            String src = locator.getSource(url);
            File file = new File(url.getFile());
            if(!isExcluded(file,null, m_exclusionRules)){
				m_sources.add(new SourceType(m_group, ineed.getName(), src,
	                    file));
            }
        }
    }
    
	public boolean isExcluded(File file, List<String> inclusionPatterns,
			List<String> exclusionPatterns) {
		if( inclusionPatterns.size()==0 && exclusionPatterns.size()==0){
			return false;
		}
		
		char[][] inclusionPatternsChar = new char[][]{};
	
			inclusionPatternsChar = processPatterns(
					inclusionPatterns);
		
		char[][] exclusionPatternsChar = new char[][]{};
			exclusionPatternsChar = processPatterns(
					exclusionPatterns);
			
		boolean excluded = isExcluded(file.toString().toCharArray(), inclusionPatternsChar,
				exclusionPatternsChar, file.isDirectory());
		if(excluded){
			System.out.println("file excluded :" + file);
		}
		return excluded;
		
	
	}
	
	public final static boolean isExcluded(char[] path,
			char[][] inclusionPatterns, char[][] exclusionPatterns,
			boolean isFolderPath) {
		if (inclusionPatterns == null && exclusionPatterns == null)
			return false;

		inclusionCheck: if (inclusionPatterns != null) {
			for (int i = 0, length = inclusionPatterns.length; i < length; i++) {
				char[] pattern = inclusionPatterns[i];
				char[] folderPattern = pattern;
				if (isFolderPath) {
					int lastSlash = CharOperation.lastIndexOf('/', pattern);
					if (lastSlash != -1 && lastSlash != pattern.length - 1) { // trailing
						// slash
						// ->
						// adds
						// ' **'
						// for
						// free
						// (see
						// http://ant.apache.org/manual/dirtasks.html)
						int star = CharOperation.indexOf('*', pattern,
								lastSlash);
						if ((star == -1 || star >= pattern.length - 1 || pattern[star + 1] != '*')) {
							folderPattern = CharOperation.subarray(pattern, 0,
									lastSlash);
						}
					}
				}
				if (CharOperation.pathMatch(folderPattern, path, true, '/')) {
					break inclusionCheck;
				}
			}
			return true; // never included
		}
		if (isFolderPath) {
			path = CharOperation.concat(path, new char[] { '*' }, '/');
		}
		if (exclusionPatterns != null) {
			for (int i = 0, length = exclusionPatterns.length; i < length; i++) {
				if (CharOperation.pathMatch(exclusionPatterns[i], path, true,
						'/')) {
					return true;
				}
			}
		}
		return false;
	}

	

	public static char[][] processPatterns(
			List<String> patterns) {
		char[][] patternsChar;
		int length = patterns.size();
		patternsChar = new char[length][];
		for (int i = 0; i < length; i++) {
			patternsChar[i] = patterns.get(i).toCharArray();
		}
		return patternsChar;
	}

}

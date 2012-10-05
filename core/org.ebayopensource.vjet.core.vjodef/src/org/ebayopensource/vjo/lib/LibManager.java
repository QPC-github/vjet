/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjo.lib;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.ebayopensource.dsf.jst.IJstLib;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.JstLib;
import org.ebayopensource.dsf.jst.datatype.JstReservedTypes;
import org.ebayopensource.dsf.jst.declaration.JstRefType;
import org.ebayopensource.dsf.jst.declaration.JstType;
import org.ebayopensource.dsf.jst.ts.util.JstTypeSerializer;
import org.ebayopensource.dsf.jst.IJstSerializer;

public final class LibManager {
	
	private Map<String, JstLib> m_libs = new HashMap<String, JstLib>();
	private IResourceResolver m_resourceResolver = ResourceHelper.getInstance();
	private IJstSerializer m_jstSerializer = JstTypeSerializer.getInstance();
	
	public static final String JS_NATIVE_LIB_NAME = "JsNativeLib";
	public static final String JS_NATIVE_GLOBAL_LIB_NAME = "JsNativeGlobal";
	public static final String JAVA_PRIMITIVE_LIB_NAME = "JavaPrimitive";
	public static final String VJO_JAVA_LIB_NAME = "VjoJavaLib";
	public static final String VJO_BASE_LIB_NAME = "VjoBaseLib";
	public static final String VJO_LIB_SUFFIX = ".jar";
	public static final String VJO_SER_SUFFIX = ".ser";

	public static final String VJO_SELF_DESCRIBED = "VjoSelfDescribed";

//	public static final String VJO_SELF_DESCRIBED = null;
	
	//
	// Singleton
	//
	private static LibManager s_instance = new LibManager();
	private LibManager(){};
	
	public static LibManager getInstance(){
		return s_instance;
	}
	
	//
	// API
	//
	/**
	 * Set resource resolver for serialized JST libs. 
	 * @param resourceResolver IResourceResolver
	 * @return LibManager
	 */
	public LibManager setResourceResolver(final IResourceResolver resourceResolver){
		if (m_resourceResolver == null){
			throw new RuntimeException("resourceResolver cannot be null");
		}
		m_resourceResolver = resourceResolver;
		if (m_resourceResolver.getJstSerializer() != null){
			m_jstSerializer = m_resourceResolver.getJstSerializer();
		}
		return this;
	}
	
	/**
	 * Answer JST lib registered with name as defined in LibManager.JAVA_PRIMITIVE_LIB_NAME
	 * @return IJstLib
	 */
	public IJstLib getJavaPrimitiveLib() {
		JstLib lib = m_libs.get(JAVA_PRIMITIVE_LIB_NAME);
		if (lib != null) {
			return lib;
		}
		lib = new JstLib(JAVA_PRIMITIVE_LIB_NAME);
		for (JstRefType type : JstReservedTypes.JavaPrimitive.ALL) {
			lib.addType(type);
		}
		lib.addType(JstReservedTypes.Other.VOID);
		synchronized (LibManager.class) {
			m_libs.put(JAVA_PRIMITIVE_LIB_NAME, lib);
		}
		return lib;
	}
	
	/**
	 * Answers VJO types base types (vjo, vjo utils, vjo.ctype, type cascades)
	 * library
	 * 
	 * Answers VJO language base types (vjo.Object, vjo.Class & vjo.Enum)
	 * library
	 * 
	 * @return IJstLib or <code>null</code> if library could not be loaded
	 */
	public IJstLib getVjoSelfDescLib() {
		JstLib lib = m_libs.get(VJO_SELF_DESCRIBED);
		if (lib != null) {
			return lib;
		}
		List<IJstType> jstTypes = null;
		try {
			VjoSelfDescribedLib.getInstance().setResourceResolver(m_resourceResolver);
			jstTypes = VjoSelfDescribedLib.getInstance().getTypes();
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		lib = new JstLib(VJO_SELF_DESCRIBED);
		lib.addTypes(jstTypes);
		
		synchronized (LibManager.class) {
			m_libs.put(VJO_SELF_DESCRIBED, lib);
		}
		return lib;
	}
	
	/**
	 * Answers browser's implicit types (e.g. client-side, DOM, Events, and JS global types)
	 * library
	 * @return IJstLib or <code>null</code> if library could not be loaded
	 */
	public IJstLib getBrowserTypesLib() {
		JstLib lib = m_libs.get(JS_NATIVE_LIB_NAME);
		if (lib != null) {
			return lib;
		}
		List<IJstType> jstTypes = null;
		try {
			jstTypes = m_jstSerializer.deserialize(
					m_resourceResolver.getJsNativeSerializedStream());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (jstTypes == null) {
			return null;
		}
		lib = new JstLib(JS_NATIVE_LIB_NAME);
		lib.addTypes(jstTypes);
		synchronized (LibManager.class) {
			m_libs.put(JS_NATIVE_LIB_NAME, lib);
		}
		return lib;
	}
	
	/**
	 * Answers JavaScript implicit types (e.g. Array, Boolean, Number, etc) library
	 * @return IJstLib or <code>null</code> if library could not be loaded
	 */
	public IJstLib getJsNativeGlobalLib() {
		JstLib lib = m_libs.get(JS_NATIVE_GLOBAL_LIB_NAME);
		if (lib != null) {
			return lib;
		}
		List<IJstType> jstTypes = null;
		try {
			jstTypes = m_jstSerializer.deserialize(
					m_resourceResolver.getJsNativeGlobalSerializedStream());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (jstTypes == null) {
			return null;
		}
		lib = new JstLib(JS_NATIVE_GLOBAL_LIB_NAME);
		lib.addTypes(jstTypes);
		synchronized (LibManager.class) {
			m_libs.put(JS_NATIVE_GLOBAL_LIB_NAME, lib);
		}
		return lib;
	}
	
	/**
	 * Answers VJO Java library types. These JstTypes will be loaded from
	 * a library jar that contains the serialized JstTypes
	 * @return IJstLib or <code>null</code> if library could not be loaded
	 */
	public IJstLib getVjoJavaLib() {
		JstLib lib = m_libs.get(VJO_JAVA_LIB_NAME);
		if (lib != null) {
			return lib;
		}
		List<IJstType> jstTypes = null;
		try {
			jstTypes = m_jstSerializer.deserialize(
					m_resourceResolver.getVjoJavaLibSerializedStream());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (jstTypes == null) {
			return null;
		}
		lib = new JstLib(VJO_JAVA_LIB_NAME);
		lib.addTypes(jstTypes);
		synchronized (LibManager.class) {
			m_libs.put(VJO_JAVA_LIB_NAME, lib);
		}
		return lib;
	}
	
	/**
	 *  Loads the JstTypes from the library designated by the lib path and lib name
	 * @param libPath - the directory path to the vjo library jar file containing the .ser file
	 * @param libName - the name of the vjo library (.jar and .ser) without the suffix
	 * @return
	 */
	public IJstLib getVjoJstLib(String libPath, String libName) {
		String vjoLibName = libName;
		
		if (libName.endsWith(VJO_LIB_SUFFIX)) {		
			vjoLibName = libName.substring(0, libName.lastIndexOf(VJO_LIB_SUFFIX));
		}
		else if (vjoLibName.endsWith(VJO_SER_SUFFIX)) {		
			vjoLibName = libName.substring(0, libName.lastIndexOf(VJO_SER_SUFFIX));
		}
		
		JstLib lib = m_libs.get(vjoLibName);
		if (lib != null) {
			return lib;
		}
		
		List<IJstType> jstTypes = null;
		Manifest mf = null;
		try {		
			String vjoLibFilePath = libPath + File.separatorChar + vjoLibName + VJO_LIB_SUFFIX;
			
			mf = getVjoJstLibManifest(vjoLibFilePath);
				
			jstTypes = m_jstSerializer.deserialize(
						getVjoJstLibSerializedStream(vjoLibFilePath, vjoLibName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (jstTypes == null) {
			return null;
		}
		lib = new JstLib(vjoLibName, mf);
		lib.addTypes(jstTypes);
		synchronized (LibManager.class) {
			m_libs.put(vjoLibName, lib);
		}
		return lib;
	}
	
	private InputStream getVjoJstLibSerializedStream(String libFilePath, String libName) {
		
		try {
			URL libUrl = new File(libFilePath).toURL();

			URLClassLoader classLoader = new URLClassLoader(new URL[] { libUrl });
			
			if (!libName.endsWith(VJO_SER_SUFFIX)) {
				libName = libName + VJO_SER_SUFFIX;
			}
			URL serFile = classLoader.findResource(libName);
			return serFile.openStream();
		} catch (Exception e) {
			throw new RuntimeException("Could not load Vjo library " + libName + "in " + libFilePath);
		}
	}
	
	private Manifest getVjoJstLibManifest(String libFilePath) {
		
		try {
			JarFile jar = new JarFile(libFilePath);
			return jar.getManifest();
		} catch (Exception e) {
			throw new RuntimeException("Could not find Vjo library manifest.mf in " + libFilePath);
		}
	}
	
	public boolean hasType(String name) {
		for (JstLib lib : m_libs.values()) {
			for (JstType type : lib.getAllTypes(true)) {
				if (type.getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void clear(){
		m_libs.clear();
	}
}

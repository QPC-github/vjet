/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jstojava.resolver;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.dsf.jstojava.resolver.FunctionMetaMapping.MetaExtension;

/**
 * Registry of function arg meta mapping at Lib/Group level from meta bootstrap.
 * The argument types of 2nd to N arguments will be resolved to a typed function based on first
 * argument value (JstIdentifier or literal). 
 * 
 * TODO look at replacing FunctionMetaRegistry with this registry
 * 
 */
public class FunctionParamsMetaRegistry {
	
	private static FunctionParamsMetaRegistry s_instance = new FunctionParamsMetaRegistry();
	private Map<String, IFunctionMetaMapping> m_funcMetaMappings = new LinkedHashMap<String, IFunctionMetaMapping>();
	private Set<String> m_tergetFuncs = new HashSet<String>();
	
	public static FunctionParamsMetaRegistry getInstance() {
		return s_instance;
	}
	
	public void addMapping(IFunctionMetaMapping mapping) {
		for(String grp: mapping.getGroupIds()){
			m_funcMetaMappings.put(grp, mapping);
		}
		m_tergetFuncs.addAll(mapping.getSupportedTargetFuncs());
	}
	
	public boolean isFuncMetaMappingSupported(String targetFunc) {
		return m_tergetFuncs.contains(targetFunc);
	}
	
	public IMetaExtension getExtentedArgBinding(
		String targetFunc, String key, String groupId, List<String> dependentGroupIds) {
		
		IMetaExtension method = getExtentedArgBinding(targetFunc, key, groupId);
		if (method == null && dependentGroupIds != null) {
			for (int i = 0; i < dependentGroupIds.size(); i++) {
				method = getExtentedArgBinding(targetFunc, key, dependentGroupIds.get(i));
				if (method != null) {
					break;
				}
			}
		}		
		return method;
	}
		
	public void clear(String groupId) {
		m_funcMetaMappings.remove(groupId);
		m_tergetFuncs.clear();
		for (IFunctionMetaMapping mapping : m_funcMetaMappings.values()) {
			m_tergetFuncs.addAll(mapping.getSupportedTargetFuncs());
		}
	}
	
	public void clearAll() {
		m_funcMetaMappings.clear();
		m_tergetFuncs.clear();
	}
	
	private IMetaExtension getExtentedArgBinding(
		String targetFunc, String key, String groupId) {
		// look though the dependencies look for names check start with
		for(String group: m_funcMetaMappings.keySet()){
			if(groupId.startsWith(group)){
				IFunctionMetaMapping mapping = m_funcMetaMappings.get(group);
				return (mapping == null) ? null : mapping.getExtentedArgBinding(targetFunc, key);
			}
		}
		return null;
		
	}

	public boolean isFirstArgumentType(String targetFunc, String groupId) {
		IFunctionMetaMapping mapping = m_funcMetaMappings.get(groupId);
		if(mapping!=null){
			return mapping.isFirstArgumentType(targetFunc);
		}
		return false;
	}
}

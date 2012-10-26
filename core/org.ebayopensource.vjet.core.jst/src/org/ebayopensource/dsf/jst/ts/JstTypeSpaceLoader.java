/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jst.ts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ebayopensource.dsf.jst.FileBinding;
import org.ebayopensource.dsf.jst.IJstParseController;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.JstSource;
import org.ebayopensource.dsf.jst.declaration.JstType;
import org.ebayopensource.dsf.jst.ts.IJstTypeLoader.SourceType;
import org.ebayopensource.dsf.ts.TypeSpace;
import org.ebayopensource.dsf.ts.event.EventListenerStatus;
import org.ebayopensource.dsf.ts.event.ISourceEventCallback;
import org.ebayopensource.dsf.ts.event.group.AddGroupEvent;
import org.ebayopensource.dsf.ts.group.IGroup;
import org.ebayopensource.dsf.ts.type.TypeName;

/**
 * JstType loading steps:
 * 1. Create empty JST types
 * 2. Parse all JS source
 * 3. Resolved all expression bindings between JstTypes
 * 4. Load all JstTypes into TS 
 *  
 *
 */
public class JstTypeSpaceLoader implements ITypeSpaceLoader {
		
	private JstTypeSpaceMgr m_tsMgr;
	private IJstTypeLoader m_typeLoader;
	
	public Map<String, Map<String, IJstType>> m_jstGroupTypes = new LinkedHashMap<String, Map<String, IJstType>>();
	
	public JstTypeSpaceLoader(JstTypeSpaceMgr tsMgr, IJstTypeLoader loader) {
		assert tsMgr != null : "tsMgr cannot be null";
		m_tsMgr = tsMgr;
		m_typeLoader = loader;
	}
	
	private synchronized boolean addType(String group, IJstType type) {
		Map<String, IJstType> typeMap = getGroupMap(group);
		
		String typeName = type.getName();
		
		IJstType oldType = typeMap.get(typeName);
		
		if (oldType != null) {
			if (oldType != type) { // duplicate, keep old type
				return false;
			}
			else { // same type
				return true;
			}
		}
		else {		
			typeMap.put(typeName, type);
			return true;
		}
		
	}
	
	private IJstType getType(String group, String typeName) {
		Map<String, IJstType> typeMap = getGroupMap(group);
		
		return typeMap.get(typeName);
	}
	
	private Map<String, IJstType> getGroupMap(String group) {
		Map<String, IJstType> typeMap = m_jstGroupTypes.get(group);
		
		if (typeMap == null) {
			typeMap = new LinkedHashMap<String, IJstType>();
			m_jstGroupTypes.put(group, typeMap);
		}
		
		return typeMap;
	}

	/**
	 * Load all types defined in the group into type space
	 * @param group
	 */
	public EventListenerStatus<IJstType> loadJstTypesIntoTS(List<AddGroupEvent> groupList,
															ISourceEventCallback<IJstType> callback) {
				
		EventListenerStatus<IJstType> successStatus = 
			new EventListenerStatus<IJstType>(EventListenerStatus.Code.Successful);
		
		EventListenerStatus<IJstType> failedStatus = 
			new EventListenerStatus<IJstType>(EventListenerStatus.Code.Failed);
			
		// load all types from each group by loading all sources
		List<SourceType> jstTypeSrcList = m_typeLoader.loadJstTypes(groupList);
		
		// create all JstTypes from the source type
		/*
		for (SourceType srcType: jstTypeSrcList) {
			
			if (getType(srcType.getGroupName(), srcType.getTypeName()) == null) {
				// create empty JstType in JstCache
				IJstType type = JstFactory.getInstance().createJstType(srcType.getTypeName(), true);
			}
		}
		*/
		
		ArrayList<IJstType> typeList = new ArrayList<IJstType>(); // temp holder of all jst types
		IJstParseController controller = m_tsMgr.getJstParseController();
		
		float progressPercent = 0;
		int total = jstTypeSrcList.size();
		
		boolean hasError = false;
		
		m_jstGroupTypes.clear(); // remove jst types from previous load
		
		// add all groups into type space
		//m_tsMgr.getGroupMgr().addGroups(groupList);
		for (AddGroupEvent event : groupList) {
			try {
				m_tsMgr.getGroupMgr().addGroup(event.getGroupName(), event.getGroupPath(), event.getSourcePath(), event.getClassPath());
			}
			catch (Throwable e) {
				hasError = true;
				failedStatus.addErrorSource(new EventListenerStatus.ErrorSource(event.getGroupName(), "Add group error", e));				
			}
		}
		
		TypeSpace ts = (TypeSpace)m_tsMgr.getTypeSpace();
		
		// Setup group dependencies
		IGroup<IJstType> group;
		for (AddGroupEvent event : groupList) {
			group = ts.getGroup(event.getGroupName());
			for (String d: event.getDirectDependency()){
				group.addGroupDependency(ts.getGroup(d));
			}
		}
					
		// translate all jst types from source
		for (SourceType srcType: jstTypeSrcList) {
			if(srcType==null){
				continue;
			}
//			System.out.println(srcType.getFile());
			
			boolean isSerialized = false;
			
			try {				
				IJstType type = srcType.getJstType(); // check if jsttype is deserialized
				
				if (type != null) {
					isSerialized = true;
				}				
				else if (type == null && srcType.getSource() != null) {	
					isSerialized = false;
					// TODO show better progress message here not percentage
					type = controller.parse(srcType.getGroupName(), srcType.getFileName(), srcType.getSource()).getType();
				}
				
				if (type != null) {
					JstType jstType = (JstType)type;
					JstSource oldSource = jstType.getSource();
					
					if (oldSource != null) {
						oldSource.setBinding(new FileBinding(srcType.getFile()));
					}
					else {
						JstSource source = new JstSource(new FileBinding(srcType.getFile()));
						jstType.setSource(source);
					}
					
					if (type.getName() == null || srcType.getFileName() == null) {
						hasError = true;
						failedStatus.addErrorSource(new EventListenerStatus.ErrorSource(srcType.getFileName(), "JstType name is null ", null));		
					}
					else {
						typeList.add(type);
						
						if (!addType(srcType.getGroupName(), type)) { // add to group map to be added to TS later
							hasError = true;
							failedStatus.addErrorSource(new EventListenerStatus.ErrorSource(type.getName(), "Duplicate JstType name at path of " + srcType.getFileName(), null));		
						}
						else {
							try {
								ts.getLocker().lockExclusive();
								m_tsMgr.getTypeDependencyMgr().addTypeNoDependency(new TypeName(srcType.getGroupName(), type.getName()), type);
							}
							catch (Throwable e) {
								hasError = true;
								failedStatus.addErrorSource(new EventListenerStatus.ErrorSource(srcType.getGroupName() + ": " + type.getName(), "type space error", e));				
							}
							finally {
								ts.getLocker().releaseExclusive();
							}
						}
						
						if (!srcType.getFileName().equalsIgnoreCase(type.getName())) {
							hasError = true;
							failedStatus.addErrorSource(new EventListenerStatus.ErrorSource(srcType.getFileName(), "JS file path doesn't match type name "  + type.getName(), null));		
						}
					}
				}
				else {
					hasError = true;
					failedStatus.addErrorSource(new EventListenerStatus.ErrorSource(srcType.getFileName(), "VJO parse error", null));					
				}
			}
			catch (Throwable e) {
				hasError = true;
				failedStatus.addErrorSource(new EventListenerStatus.ErrorSource(srcType.getGroupName() + ": " + srcType.getFileName(), "VJO parse error", e));				
			}
			
			if (isSerialized) {
				progressPercent += 0.05 / (float)total;
			}
			else {
				progressPercent += 0.40 / (float)total;
			}
			
			notifyProgress(callback, progressPercent);
		}
		
		float remainingPercent = (float)1.0 - progressPercent;
		
		total = typeList.size(); // recalculate total
		
		// resolve all type bindings
		for (IJstType type: typeList) {
			try {
//				System.out.println("RESOLVING =" + type.getName());
				
				controller.resolve(type);
			}
			catch (Throwable e) {
				hasError = true;
				String groupName="";
				if (type.getPackage() != null) {
					groupName = type.getPackage().getGroupName();
				}
				failedStatus.addErrorSource(new EventListenerStatus.ErrorSource(groupName + ": " + type.getSimpleName(), "resolve error", e));				
			}
			
			progressPercent += ((float)0.4 * remainingPercent) / (float)total;
			
			notifyProgress(callback, progressPercent);
		}
		
		// load all types into type space
		for (String groupName : m_jstGroupTypes.keySet()) {
			Map<String, IJstType> typeMap = getGroupMap(groupName);
			
			for (String typeName : typeMap.keySet()) {
				IJstType type = typeMap.get(typeName);
				String typeParsedName = (type.getSimpleName() == null) ?  typeName : type.getName();
				
				if (typeParsedName != null) {
					try {
						ts.getLocker().lockExclusive();
						m_tsMgr.getTypeDependencyMgr().addType(new TypeName(groupName, typeParsedName), type);
						
					}
					catch (Throwable e) {
						hasError = true;
						failedStatus.addErrorSource(new EventListenerStatus.ErrorSource(groupName + ": " + typeParsedName, "type space error", e));				
					}
					finally {
						ts.getLocker().releaseExclusive();
					}
				}
				
				progressPercent += ((float)0.6 * remainingPercent) / (float)total;
				
				notifyProgress(callback, progressPercent);
			}
		}
		
		if (hasError) {
			return failedStatus;
		}
		else {
			return successStatus;
		}
		
	}
	
	public void setJstTypeLoader(IJstTypeLoader loader) {
		m_typeLoader = loader;
	}
	
	private void notifyProgress(ISourceEventCallback<IJstType> callback, float percentage) {
		if (callback == null){
			return;
		}
		
		int percent = (int)(percentage * 100.0 + 0.5);
		
		if (percent > 0) {
			callback.onProgress(percent);
		}
	}
	
}

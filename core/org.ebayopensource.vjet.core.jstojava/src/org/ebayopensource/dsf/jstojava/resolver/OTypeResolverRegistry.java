/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jstojava.resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.meta.JsTypingMeta;
import org.ebayopensource.dsf.jst.term.NV;
import org.ebayopensource.dsf.jstojava.parser.comments.JsCommentMeta;
import org.ebayopensource.dsf.jstojava.parser.comments.ParseException;
import org.ebayopensource.dsf.jstojava.parser.comments.TokenMgrError;
import org.ebayopensource.dsf.jstojava.parser.comments.VjComment;
import org.ebayopensource.dsf.jstojava.report.ErrorReporter;
import org.ebayopensource.dsf.jstojava.translator.IFindTypeSupport;
import org.ebayopensource.dsf.jstojava.translator.TranslateHelper;

/**
 * Registry of otype resolver. Take an object literal and resolve to an otype 
 * based on field name such as type.
 * 
 * This resolver will take an object literal passed into a method invocation expression
 * and resolve to an otype. 
 * 
 * doIt({
 *  type: 'foo'
 *  
 * })
 * 
 * The object literal will be resolved based on the literal field key (in the example above the key is type)
 * 
 * This will able validation and code assist for the object literal. 
 *
 *
 */
public class OTypeResolverRegistry {
	
	private static final OTypeResolverRegistry s_instance = new OTypeResolverRegistry();
	private Map<String, List<IOTypeResolver>> m_resolvers = new HashMap<String, List<IOTypeResolver>>();
	
	public static OTypeResolverRegistry getInstance() {
		return s_instance;
	}
	
	public OTypeResolverRegistry addResolver(String key, IOTypeResolver resolver) {
		List<IOTypeResolver> resolverList = m_resolvers.get(key);
		if (resolverList == null) {
			resolverList = new ArrayList<IOTypeResolver>(1);
			m_resolvers.put(key, resolverList);
		}
		resolverList.add(resolver);
		return this;
	}
	
	public IJstType resolve(String key, NV args) {
		List<IOTypeResolver> resolverList = m_resolvers.get(key);
		if (resolverList == null) {
			return null;
		}
		for (int i = 0; i < resolverList.size(); i++) {
			IOTypeResolver resolver = resolverList.get(i);
			String typeName = resolver.resolve(args);
			
			if(typeName.contains("org.mozilla.mod.javascript.Undefined")){
				return null;
			}
			
			try {
				final JsCommentMeta commentMeta = VjComment.parse("//> " + typeName);;
				final JsTypingMeta typingMeta = commentMeta.getTyping();
				IFindTypeSupport findSupport = new IFindTypeSupport() {
					
					@Override
					public char[] getOriginalSource() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public ILineInfoProvider getLineInfoProvider() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public ErrorReporter getErrorReporter() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public IJstType getCurrentType() {
						// TODO Auto-generated method stub
						return null;
					}
					
					@Override
					public IJstType findTypeByName(String name) {
						// TODO Auto-generated method stub
						return null;
					}
				};;;
				return TranslateHelper.findType(findSupport, typingMeta, commentMeta);
				
			} catch (ParseException e) {
				// do nothing will validate later
				
			} catch(TokenMgrError e){
				// do nothing will validate later
			}
			
		
		}
		return null;
	}
	
	public boolean hasResolver(String key) {
		return m_resolvers.containsKey(key);
	}
	
	public boolean hasResolvers(){
		return !m_resolvers.isEmpty();
	}
	
	public void clear(String groupId) {
		for (List<IOTypeResolver> resolverList : m_resolvers.values()) {
			for (int i = resolverList.size() - 1; i >=0; i--) {
				IOTypeResolver resolver = resolverList.get(i);
				for(String group: resolver.getGroupIds()){
					if (groupId.endsWith(group)) {
						resolverList.remove(resolver);
					}
				}
				
			}
		}
	}
	
	public void clearAll() {
		m_resolvers.clear();
	}

	public Set<String> getKeys() {
		return m_resolvers.keySet();
	}
}

/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jst.term;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.declaration.SynthOlType;
import org.ebayopensource.dsf.jst.token.IExpr;
import org.ebayopensource.dsf.jst.traversal.IJstNodeVisitor;

public class ObjLiteral extends JstLiteral {
	
	private static final long serialVersionUID = 1L;
	
	private Map<String,NV> m_nvs = new LinkedHashMap<String,NV>(2);
	private IJstType m_jstType;
	
	public ObjLiteral() {
		m_jstType = new SynthOlType(this);
	}
	
	public void setJstType(IJstType type){
		m_jstType = type;
	}
	

	private void readObject(ObjectInputStream in)  
            throws IOException, ClassNotFoundException {
            Object obj = in.readObject();
            if(obj instanceof IJstType){
            	m_jstType = (IJstType)obj;
            }
            obj = in.readObject();
            // support backward compatibility of older serialized files
            if (obj instanceof List) {
            	m_nvs = new LinkedHashMap<String, NV>();
               List<NV> list = (List<NV>)obj;
               for (NV object : list) {
            	   if(object !=  null && object.getName()!=null){
            		   m_nvs.put(object.getName(), object);
            	   }
               }
			}else {
            	m_nvs = (Map) obj;
            }
        }
	
	//
	// Satisfy ILiteral
	//
	public String toValueText(){
		StringBuilder sb = new StringBuilder("{");
		List<NV> nvs = getNVs();
		for (int i=0; i<nvs.size(); i++){
			if (i > 0){
				sb.append(",");
			}
			sb.append(nvs.get(i));
		}
		sb.append("}");
		
		return sb.toString();
	}
	
	
	
	@Override
	public IJstType getResultType(){
		return m_jstType;
	}
	
	public String toParamText(){
		return toValueText();
	}
	
	public String toTermText(){
		return toValueText();
	}
	
	public String toSimpleTermText(){
		return toValueText();
	}
	
	public String toRHSText(){
		return toValueText();
	}
	
	public String toExprText(){
		return toValueText();
	}
	
	//
	// API
	//
	public ObjLiteral add(final String name, final String value){
		return add(new NV(name, SimpleLiteral.getStringLiteral(value)));
	}
	
	public ObjLiteral add(final String name, final boolean value){
		return add(new NV(name, SimpleLiteral.getBooleanLiteral(value)));
	}
	
	public ObjLiteral add(final String name, final int value){
		return add(new NV(name, SimpleLiteral.getIntLiteral(value)));
	}
	
	public ObjLiteral add(final String name, final IExpr value){
		return add(new NV(name, value));
	}
	
	public ObjLiteral add(final NV nv){
		assert nv != null : "nv is null";
		m_nvs.put(nv.getName(),nv);
		addChild(nv);
		return this;
	}
	
	public List<NV> getNVs(){
		List<NV> list = new ArrayList<NV>(m_nvs.values());
		return Collections.unmodifiableList(list);
	}
	
	public NV getNV(String field){
		return m_nvs.get(field);
	}

	@Override
	public void accept(IJstNodeVisitor visitor){
		visitor.visit(this);
	}
	
	@Override
	public String toString(){
		return toValueText();
	}


}

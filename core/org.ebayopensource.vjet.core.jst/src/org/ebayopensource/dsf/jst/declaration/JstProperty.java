/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jst.declaration;

import org.ebayopensource.dsf.jst.BaseJstNode;
import org.ebayopensource.dsf.jst.IJstDoc;
import org.ebayopensource.dsf.jst.IJstProperty;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.IJstTypeReference;
import org.ebayopensource.dsf.jst.term.JstIdentifier;
import org.ebayopensource.dsf.jst.term.JstLiteral;
import org.ebayopensource.dsf.jst.token.IExpr;
import org.ebayopensource.dsf.jst.token.ISimpleTerm;
import org.ebayopensource.dsf.jst.traversal.IJstNodeVisitor;
import org.ebayopensource.dsf.jst.util.DataTypeHelper;
import org.ebayopensource.dsf.common.Z;

/**
 * Implementation of IJstProperty
 * 
 */
public class JstProperty extends BaseJstNode implements IJstProperty {

	private static final long serialVersionUID = 1L;
	
	private final JstTypeReference m_type;
	private JstName m_name;
	private JstModifiers m_modifiers;
	private ISimpleTerm m_value;
	private IExpr m_initializer;
	private IJstDoc m_doc;

	//
	// Constructor
	//
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 */
	public JstProperty(final IJstType type, final String name) {
		this(type, name, (JstIdentifier)null, new JstModifiers());
	}

	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param value ISimpleTerm
	 */
	public JstProperty(final IJstType type, final String name, final JstIdentifier value) {
		this(type, name, value, new JstModifiers());
	}

	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param value JstLiteral
	 */
	public JstProperty(final IJstType type, final String name, final JstLiteral value) {
		this(type, name, value, new JstModifiers());
	}

	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param value JstIdentifier
	 * @param modifiers JstModifiers
	 */
	public JstProperty(final IJstType type, final String name, final JstModifiers modifiers) {
		this(type, new JstName(name), (JstIdentifier)null, modifiers);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param value JstIdentifier
	 * @param modifiers JstModifiers
	 */
	public JstProperty(final IJstType type, final String name, final JstIdentifier value, final JstModifiers modifiers) {
		this(type, new JstName(name), value, modifiers);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name JstName
	 * @param value JstIdentifier
	 * @param modifiers JstModifiers
	 */
	public JstProperty(final IJstType type, final JstName name, final JstIdentifier value, final JstModifiers modifiers) {
		this(type,name,modifiers,value,null);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param value JstLiteral
	 * @param modifiers JstModifiers
	 */
	public JstProperty(final IJstType type, final String name, final JstLiteral value, final JstModifiers modifiers) {
		this(type, new JstName(name), value, modifiers);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name JstName
	 * @param value JstLiteral
	 * @param modifiers JstModifiers
	 */
	public JstProperty(final IJstType type, final JstName name, final JstLiteral value, final JstModifiers modifiers) {
		this(type,name,modifiers,value,null);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param initializer IExpr
	 * @param modifiers JstModifiers
	 */
	public JstProperty(final IJstType type, final String name, final IExpr initializer, final JstModifiers modifiers) {
		this(type, new JstName(name), initializer, modifiers);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name JstName
	 * @param initializer IExpr
	 * @param modifiers JstModifiers
	 */
	public JstProperty(final IJstType type, final JstName name, final IExpr initializer, final JstModifiers modifiers) {
		this(type,name,modifiers,null,initializer);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name JstName
	 * @param initializer IExpr
	 * @param modifiers JstModifiers
	 */
	protected JstProperty(final IJstType type, 
			final JstName name, 			
			final JstModifiers modifiers,
			final ISimpleTerm value, 
			final IExpr expr
			) {
		assert type != null : "type cannot be null";
		assert name != null : "name cannot be null";
		assert modifiers != null : "modifiers cannot be null";
		m_type = new JstTypeReference(type);
		m_name = name;
		m_value = value;
		m_initializer = expr;
		m_modifiers = modifiers;
		addChild(name);
		if (value != null)
			addChild(value);
		if (expr != null)
			addChild(expr);
		addChild(m_type);
	}
	
	//
	// Satisfy IJstProperty
	//
	/**
	 * @see IJstProperty#getType()
	 */
	public IJstType getType() {
		return (m_type == null)? null: m_type.getReferencedType();
	}
	
	/**
	 * @see IJstProperty#getTypeRef()
	 */
	public IJstTypeReference getTypeRef() {
		return m_type;
	}
	
	/**
	 * @see IJstProperty#getName()
	 */
	public JstName getName() {
		return m_name;
	}

	/**
	 * @see IJstProperty#getModifiers()
	 */
	public JstModifiers getModifiers() {
		return m_modifiers;
	}
	
	/**
	 * @see IJstProperty#getModifiers()
	 */
	public void setModifiers(JstModifiers modifiers) {
		m_modifiers = modifiers;
	}

	/**
	 * @see IJstProperty#isPublic()
	 */
	public boolean isPublic() {
		return m_modifiers.isPublic();
	}
	
	/**
	 * @see IJstProperty#isProtected()
	 */
	public boolean isProtected() {
		return m_modifiers.isProtected();
	}

	/**
	 * @see IJstProperty#isInternal()
	 */
	public boolean isInternal() {
		return m_modifiers.isInternal();
	}

	/**
	 * @see IJstProperty#isPrivate()
	 */
	public boolean isPrivate() {
		return m_modifiers.isPrivate();
	}
	
	/**
	 * @see IJstProperty#isStatic()
	 */
	public boolean isStatic() {
		return m_modifiers.isStatic();
	}

	/**
	 * @see IJstProperty#isFinal()
	 */
	public boolean isFinal() {
		return m_modifiers.isFinal();
	}
	
	/**
	 * @see IJstProperty#getValue()
	 */
	public ISimpleTerm getValue() {
		return m_value;
	}
	
	/**
	 * @see IJstProperty#getInitializer()
	 */
	public IExpr getInitializer(){
		return m_initializer;
	}
	
	//
	// API
	//
	/**
	 * Set name for the property
	 * @param name String
	 */
	public void setName(String name){
		setName(new JstName(name));
	}

	/**
	 * Set name for the property
	 * @param name String
	 */
	public void setName(JstName name){
		assert name != null : "name cannot be null";
		// FIXME: need to replace the child?
		m_name = name;
	}
	
	/**
	 * Set type for the property
	 * @param type IJstType
	 */
	public void setType(IJstType type){
		m_type.setReferencedType(type);
	}

	/**
	 * Set value for the property
	 * @param value ISimpleTerm
	 */
	public void setValue(final ISimpleTerm value) {
		m_value = value;
		addChild(value);
	}
	
	/**
	 * Set the initializer for this property. 
	 * It is a child node of constructor or init block,
	 * depending on whether it's instance or static property
	 * @param initializer IExpr
	 */
	public void setInitializer(IExpr initializer){
		m_initializer = initializer;
	}
	
	/**
	 * Answer the access scope
	 * @return String
	 */
	public String getAccessScope() {
		return m_modifiers.getAccessScope();
	}

	/**
	 * Answer formatted property name:value
	 * @return String
	 */
	public String toNVText() {
		if (m_value == null) {
			String defaultValue = null;
			IJstType elementType = getType();
			defaultValue = DataTypeHelper.getDefaultValue(elementType.getName());
			return m_name + ":" + defaultValue;
		}
		return m_name + ":" + m_value.toSimpleTermText();
	}
	
	/**
	 * {@link IJstProperty#getDoc()}
	 */
	public IJstDoc getDoc() {
		return m_doc;
	}
	
	/**
	 * Set JstDoc for this property
	 * @param jstDoc IJstDoc
	 */
	public void setDoc(IJstDoc jstDoc) {
		m_doc = jstDoc;
	}

	@Override
	public void accept(IJstNodeVisitor visitor){
		visitor.visit(this);
	}
	
	@Override
	public String toString() {
		Z z = new Z();
		z.format("m_name", m_name == null ? null : m_name.getName());
		z.format("m_value", m_value);
		z.format("m_modifiers", m_modifiers);
		z.format("m_type", m_type);

		return z.toString();
	}
	
	@Deprecated // TODO move it out
	public String getProcessedValue() {
		if (m_value != null) {
			char[] val = m_value.toSimpleTermText().toCharArray();
			if (val.length > 0) {
				if (val[0] == '"' && val[val.length - 1] == '"'
						|| val[0] == '\'' && val[val.length - 1] == '\'') {
					return new String(val, 1, val.length - 2);
				} else {
					return m_value.toSimpleTermText();
				}
			}
		}
		return null;
	}

}

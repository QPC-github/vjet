/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jst.declaration;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.ISynthesized;
import org.ebayopensource.dsf.jst.term.JstIdentifier;
import org.ebayopensource.dsf.jst.term.JstLiteral;
import org.ebayopensource.dsf.jst.token.IExpr;

public class JstSynthesizedProperty extends JstProperty implements ISynthesized{
	private static final long serialVersionUID = 1L;
	
	public JstSynthesizedProperty(final IJstType type, final String name, JstIdentifier identifier, JstModifiers modifier) {
		super(type, name, identifier, (modifier==null)?new JstModifiers():modifier);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 */
	public JstSynthesizedProperty(final IJstType type, final String name) {
		super(type, name, (JstIdentifier)null, new JstModifiers());
	}

	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param value ISimpleTerm
	 */
	public JstSynthesizedProperty(final IJstType type, final String name, final JstIdentifier value) {
		super(type, name, value, new JstModifiers());
	}

	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param value JstLiteral
	 */
	public JstSynthesizedProperty(final IJstType type, final String name, final JstLiteral value) {
		super(type, name, value, new JstModifiers());
	}

	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param value JstIdentifier
	 * @param modifiers JstModifiers
	 */
	public JstSynthesizedProperty(final IJstType type, final String name, final JstModifiers modifiers) {
		super(type, new JstName(name), (JstIdentifier)null, modifiers);
	}
	

	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name JstName
	 * @param value JstIdentifier
	 * @param modifiers JstModifiers
	 */
	public JstSynthesizedProperty(final IJstType type, final JstName name, final JstIdentifier value, final JstModifiers modifiers) {
		super(type,name,modifiers,value,null);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param value JstLiteral
	 * @param modifiers JstModifiers
	 */
	public JstSynthesizedProperty(final IJstType type, final String name, final JstLiteral value, final JstModifiers modifiers) {
		super(type, new JstName(name), value, modifiers);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name JstName
	 * @param value JstLiteral
	 * @param modifiers JstModifiers
	 */
	public JstSynthesizedProperty(final IJstType type, final JstName name, final JstLiteral value, final JstModifiers modifiers) {
		super(type,name,modifiers,value,null);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name String
	 * @param initializer IExpr
	 * @param modifiers JstModifiers
	 */
	public JstSynthesizedProperty(final IJstType type, final String name, final IExpr initializer, final JstModifiers modifiers) {
		super(type, new JstName(name), initializer, modifiers);
	}
	
	/**
	 * Constructor
	 * @param type JstType type of the property
	 * @param name JstName
	 * @param initializer IExpr
	 * @param modifiers JstModifiers
	 */
	public JstSynthesizedProperty(final IJstType type, final JstName name, final IExpr initializer, final JstModifiers modifiers) {
		super(type,name,modifiers,null,initializer);
	}
	
}

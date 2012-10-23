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

public class JstSynthesizedMethod extends JstMethod implements ISynthesized {
	private static final long serialVersionUID = 1L;

	public JstSynthesizedMethod(final String name,
			final JstModifiers modifiers, final IJstType rtnType,
			final JstArg... args) {
		super(new JstName(name), modifiers, rtnType, args);
	}

	/**
	 * Constructor
	 * 
	 * @param args
	 *            JstArg...
	 */
	public JstSynthesizedMethod(final JstArg... args) {
		super((JstName) null, new JstModifiers(), null, args);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            String
	 * @param args
	 *            JstArg...
	 */
	public JstSynthesizedMethod(final String name, final JstArg... args) {
		super(name, new JstModifiers(), null, args);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            String
	 * @param modifiers
	 *            JstModifiers
	 * @param args
	 *            JstArg...
	 */
	public JstSynthesizedMethod(final String name,
			final JstModifiers modifiers, final JstArg... args) {
		this(name, modifiers, null, args);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            String
	 * @param rtnType
	 *            JstType
	 * @param args
	 *            JstArg...
	 */
	public JstSynthesizedMethod(final String name, final IJstType rtnType,
			final JstArg... args) {
		this(name, new JstModifiers(), rtnType, args);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            JstName
	 * @param modifiers
	 *            JstModifiers
	 * @param rtnType
	 *            JstType
	 * @param args
	 *            JstArg...
	 */
	public JstSynthesizedMethod(final JstName name,
			final JstModifiers modifiers, final IJstType rtnType,
			final JstArg... args) {

		super(name, modifiers, rtnType, args);
	}

}

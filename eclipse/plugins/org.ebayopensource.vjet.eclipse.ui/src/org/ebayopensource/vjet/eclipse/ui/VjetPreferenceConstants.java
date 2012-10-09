/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.ui;

import org.ebayopensource.vjet.eclipse.internal.ui.text.VjetColorConstants;
import org.eclipse.dltk.mod.ui.CodeFormatterConstants;
import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
import org.eclipse.dltk.mod.ui.PreferenceConstants;
import org.eclipse.dltk.mod.ui.preferences.NewScriptProjectPreferencePage;
import org.eclipse.dltk.mod.ui.text.DLTKColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

public class VjetPreferenceConstants extends PreferenceConstants {

	/*
	 * Single line comment
	 */
	/**
	 * A named preference that controls whether the 'close comments' feature is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 */
	public final static String EDITOR_CLOSE_COMMENTS = "closeComments"; //$NON-NLS-1$
	
	/**
	 * A named preference that controls whether occurrences are marked in the editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.0
	 */	
	public static final String EDITOR_MARK_OCCURRENCES= "markOccurrences"; //$NON-NLS-1$
	
	/**
	 * A named preference that controls whether occurrences are sticky in the editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 *
	 * @since 3.0
	 */	
	public static final String EDITOR_STICKY_OCCURRENCES= "stickyOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether method occurrences are marked.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_METHOD_OCCURRENCES= "markMethodOccurrences"; //$NON-NLS-1$
	/**
	 * A named preference that controls whether non-constant field occurrences are marked.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_FIELD_OCCURRENCES= "markFieldOccurrences"; //$NON-NLS-1$
	
	/**
	 * A named preference that controls whether local variable occurrences are marked.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES= "markLocalVariableOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that holds the color used to render single line
	 * comments.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_COLOR = VjetColorConstants.VJET_SINGLE_LINE_COMMENT;

	public final static String EDITOR_REGEXP_CORE_COLOR = VjetColorConstants.VJET_REGEXP_CORE;

	public final static String EDITOR_TASK_TAGS_COMMENT_COLOR = VjetColorConstants.VJET_TASK_TAGS_COMMENT;
	public final static String EDITOR_TASK_TAGS_COMMENT_BOLD= VjetColorConstants.VJET_TASK_TAGS_COMMENT + EDITOR_BOLD_SUFFIX;
	
	public final static String EDITOR_MULTI_LINE_COMMENT_COLOR = VjetColorConstants.VJET_MULTI_LINE_COMMENT;

	public final static String EDITOR_JAVADOC_HTML_MARKUP_COLOR = VjetColorConstants.VJET_JAVADOC_HTML_MARKUP;
	public final static String EDITOR_JAVADOC_LINKS_COLOR = VjetColorConstants.VJET_JAVADOC_LINKS;
	public final static String EDITOR_JAVADOC_OTHERS_COLOR = VjetColorConstants.VJET_JAVADOC_OTHERS;
	public final static String EDITOR_JAVADOC_TAGS_COLOR = VjetColorConstants.VJET_JAVADOC_TAGS;
	public final static String EDITOR_JAVADOC_TAGS_BOLD = VjetColorConstants.VJET_JAVADOC_TAGS + EDITOR_BOLD_SUFFIX;
	public final static String EDITOR_JAVADOC_HTML_MARKUP_BOLD= VjetColorConstants.VJET_JAVADOC_HTML_MARKUP + EDITOR_BOLD_SUFFIX;
	
	

	

	
	/**
	 * A named preference that controls whether single line comments are
	 * rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in bold. If <code>false</code> the are
	 * rendered using no font style attribute.
	 * </p>
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_BOLD = VjetColorConstants.VJET_SINGLE_LINE_COMMENT
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether single line comments are
	 * rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in italic. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_ITALIC = VjetColorConstants.VJET_SINGLE_LINE_COMMENT
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that controls whether single line comments are
	 * rendered in strikethrough.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in strikethrough. If <code>false</code> the
	 * are rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_STRIKETHROUGH = VjetColorConstants.VJET_SINGLE_LINE_COMMENT
			+ EDITOR_STRIKETHROUGH_SUFFIX;

	/**
	 * A named preference that controls whether single line comments are
	 * rendered in underline.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in underline. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 * 
	 * 
	 */
	public final static String EDITOR_SINGLE_LINE_COMMENT_UNDERLINE = VjetColorConstants.VJET_SINGLE_LINE_COMMENT
			+ EDITOR_UNDERLINE_SUFFIX;

	/*
	 * Key worlds
	 */
	/**
	 * A named preference that holds the color used to render keyword.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_KEYWORD_COLOR = VjetColorConstants.VJET_KEYWORD;

	/**
	 * A named preference that controls whether kwyword are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in bold. If <code>false</code> the are
	 * rendered using no font style attribute.
	 * </p>
	 */
	public final static String EDITOR_KEYWORD_BOLD = VjetColorConstants.VJET_KEYWORD
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether keyword are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in italic. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_KEYWORD_ITALIC = VjetColorConstants.VJET_KEYWORD
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that controls whether single line comments are
	 * rendered in strikethrough.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in strikethrough. If <code>false</code> the
	 * are rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_KEYWORD_STRIKETHROUGH = VjetColorConstants.VJET_KEYWORD
			+ EDITOR_STRIKETHROUGH_SUFFIX;

	/**
	 * A named preference that controls whether keyword are rendered in
	 * underline.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in underline. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 * 
	 * 
	 */
	public final static String EDITOR_KEYWORD_UNDERLINE = VjetColorConstants.VJET_KEYWORD
			+ EDITOR_UNDERLINE_SUFFIX;
	/*
	 * keyword return color
	 */
	/**
	 * A named preference that holds the color used to render keyword.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_KEYWORD_RETURN_COLOR = VjetColorConstants.VJET_KEYWORD_RETURN;

	/**
	 * A named preference that controls whether kwyword are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in bold. If <code>false</code> the are
	 * rendered using no font style attribute.
	 * </p>
	 */
	public final static String EDITOR_KEYWORD_RETURN_BOLD = VjetColorConstants.VJET_KEYWORD_RETURN
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether keyword are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in italic. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_KEYWORD_RETURN_ITALIC = VjetColorConstants.VJET_KEYWORD_RETURN
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that controls whether single line comments are
	 * rendered in strikethrough.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in strikethrough. If <code>false</code> the
	 * are rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_KEYWORD_RETURN_STRIKETHROUGH = VjetColorConstants.VJET_KEYWORD_RETURN
			+ EDITOR_STRIKETHROUGH_SUFFIX;

	/**
	 * A named preference that controls whether keyword are rendered in
	 * underline.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in underline. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 * 
	 * 
	 */
	public final static String EDITOR_KEYWORD_RETURN_UNDERLINE = VjetColorConstants.VJET_KEYWORD_RETURN
			+ EDITOR_UNDERLINE_SUFFIX;

	/*
	 * Numbers
	 */
	/**
	 * A named preference that holds the color used to render NUMBER.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_NUMBER_COLOR = VjetColorConstants.VJET_NUMBER;

	/**
	 * A named preference that controls whether number are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in bold. If <code>false</code> the are
	 * rendered using no font style attribute.
	 * </p>
	 */
	public final static String EDITOR_NUMBER_BOLD = VjetColorConstants.VJET_NUMBER
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether NUMBER are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in italic. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_NUMBER_ITALIC = VjetColorConstants.VJET_NUMBER
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that controls whether single line comments are
	 * rendered in strikethrough.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in strikethrough. If <code>false</code> the
	 * are rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_NUMBER_STRIKETHROUGH = VjetColorConstants.VJET_NUMBER
			+ EDITOR_STRIKETHROUGH_SUFFIX;

	/**
	 * A named preference that controls whether NUMBER are rendered in
	 * underline.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in underline. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 * 
	 * 
	 */

	public final static String EDITOR_NUMBER_UNDERLINE = VjetColorConstants.VJET_NUMBER
			+ EDITOR_UNDERLINE_SUFFIX;

	/*
	 * Strings
	 */
	/**
	 * A named preference that holds the color used to render STRING.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 */
	public final static String EDITOR_STRING_COLOR = VjetColorConstants.VJET_STRING;

	/**
	 * A named preference that controls whether STRING are rendered in bold.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in bold. If <code>false</code> the are
	 * rendered using no font style attribute.
	 * </p>
	 */
	public final static String EDITOR_STRING_BOLD = VjetColorConstants.VJET_STRING
			+ EDITOR_BOLD_SUFFIX;

	/**
	 * A named preference that controls whether STRING are rendered in italic.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in italic. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_STRING_ITALIC = VjetColorConstants.VJET_STRING
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that controls whether single line comments are
	 * rendered in strikethrough.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in strikethrough. If <code>false</code> the
	 * are rendered using no italic font style attribute.
	 * </p>
	 */
	public final static String EDITOR_STRING_STRIKETHROUGH = VjetColorConstants.VJET_STRING
			+ EDITOR_STRIKETHROUGH_SUFFIX;

	/**
	 * A named preference that controls whether STRING are rendered in
	 * underline.
	 * <p>
	 * Value is of type <code>Boolean</code>. If <code>true</code> single
	 * line comments are rendered in underline. If <code>false</code> the are
	 * rendered using no italic font style attribute.
	 * </p>
	 * 
	 * 
	 */

	public final static String EDITOR_STRING_UNDERLINE = VjetColorConstants.VJET_STRING
			+ EDITOR_UNDERLINE_SUFFIX;

//	public final static String EDITOR_FUNCTION_DEFINITION_COLOR = VjetColorConstants.VJET_FUNCTION_DEFINITION;

	public static final String EDITOR_XML_TAG_NAME_COLOR = VjetColorConstants.VJET_XML_TAG_NAME;

	public static final String EDITOR_XML_COMMENT_COLOR = VjetColorConstants.VJET_XML_COMMENT_NAME;

	public static final String EDITOR_XML_BODY_ALL = VjetColorConstants.VJET_XML_ALL;

	public static final String EDITOR_XML_ATTR_NAME_COLOR = VjetColorConstants.VJET_XML_ATTR_NAME;

	private static final String EDITOR_XML_TAG_NAME_BOLD = VjetColorConstants.VJET_XML_TAG_NAME
			+ EDITOR_BOLD_SUFFIX;

	private static final String EDITOR_XML_ATTR_NAME_ITALIC = VjetColorConstants.VJET_XML_ATTR_NAME
			+ EDITOR_ITALIC_SUFFIX;

	/**
	 * A named preference that stores the configured folding provider.
	 * <p>
	 * Value is of type <code>String</code>.
	 */
	public static final String EDITOR_FOLDING_PROVIDER= "editor_folding_provider"; //$NON-NLS-1$
	
	/**
	 * A named preference that stores the value for Javadoc folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_FOLDING_JAVADOC= "editor_folding_default_javadoc"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for inner type folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 */
	public static final String EDITOR_FOLDING_INNERTYPES= "editor_folding_default_innertypes"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for method folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 */
	public static final String EDITOR_FOLDING_METHODS= "editor_folding_default_methods"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for imports folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 */
	public static final String EDITOR_FOLDING_IMPORTS= "editor_folding_default_imports"; //$NON-NLS-1$

	/**
	 * A named preference that stores the value for header comment folding for the default folding provider.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 */
	public static final String EDITOR_FOLDING_HEADERS= "editor_folding_default_headers"; //$NON-NLS-1$
	
	/**
	 * A named preference that defines how member elements are ordered by the
	 * Java views using the <code>JavaElementSorter</code>.
	 * <p>
	 * Value is of type <code>String</code>: A comma separated list of the
	 * following entries. Each entry must be in the list, no duplication. List
	 * order defines the sort order.
	 * <ul>
	 * <li><b>T</b>: Types</li>
	 * <li><b>C</b>: Constructors</li>
	 * <li><b>I</b>: Initializers</li>
	 * <li><b>M</b>: Methods</li>
	 * <li><b>F</b>: Fields</li>
	 * <li><b>SI</b>: Static Initializers</li>
	 * <li><b>SM</b>: Static Methods</li>
	 * <li><b>SF</b>: Static Fields</li>
	 * </ul>
	 * </p>
	 * @since 2.1
	 */
	public static final String APPEARANCE_MEMBER_SORT_ORDER= "outlinesortoption"; //$NON-NLS-1$
	
	/**
	 * A named preference that defines how member elements are ordered by visibility in the
	 * Java views using the <code>JavaElementSorter</code>.
	 * <p>
	 * Value is of type <code>String</code>: A comma separated list of the
	 * following entries. Each entry must be in the list, no duplication. List
	 * order defines the sort order.
	 * <ul>
	 * <li><b>B</b>: Public</li>
	 * <li><b>V</b>: Private</li>
	 * <li><b>R</b>: Protected</li>
	 * <li><b>D</b>: Default</li>
	 * </ul>
	 * </p>
	 * @since 3.0
	 */
	public static final String APPEARANCE_VISIBILITY_SORT_ORDER= "org.eclipse.jdt.ui.visibility.order"; //$NON-NLS-1$
	
	/**
	 * A named preferences that controls if Java elements are also sorted by 
	 * visibility.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * @since 3.0
	 */
	public static final String APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER= "org.eclipse.jdt.ui.enable.visibility.order"; //$NON-NLS-1$
	

	public static String TOGGLE_PRESENTATION_ACTION;
	
	public static void initializeDefaultValues(IPreferenceStore store) {
		
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR,
				new RGB(63, 127, 95));
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_MULTI_LINE_COMMENT_COLOR,
				new RGB(63, 127, 95));
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_KEYWORD_COLOR, new RGB(127, 0,
						85));
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_KEYWORD_RETURN_COLOR, new RGB(
						127, 0, 85));
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_STRING_COLOR,
				new RGB(42, 0, 255));
//		PreferenceConverter
//				.setDefault(store, VjetPreferenceConstants.EDITOR_NUMBER_COLOR,
//						new RGB(128, 0, 0));
//		PreferenceConverter.setDefault(store,
//				VjetPreferenceConstants.EDITOR_FUNCTION_DEFINITION_COLOR,
//				new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_XML_COMMENT_COLOR, new RGB(170,
						200, 200));
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_XML_BODY_ALL, new RGB(240, 240,
						240));
		store.setDefault(
				VjetPreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_BOLD, false);
		store.setDefault(
				VjetPreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_ITALIC,
				false);

		store.setDefault(VjetPreferenceConstants.EDITOR_KEYWORD_BOLD, true);
		store.setDefault(VjetPreferenceConstants.EDITOR_KEYWORD_ITALIC, false);
		store
				.setDefault(VjetPreferenceConstants.EDITOR_XML_TAG_NAME_BOLD,
						true);
		store.setDefault(VjetPreferenceConstants.EDITOR_XML_ATTR_NAME_ITALIC,
				true);
		store.setDefault(VjetPreferenceConstants.EDITOR_KEYWORD_RETURN_BOLD,
				true);
		store.setDefault(VjetPreferenceConstants.EDITOR_KEYWORD_RETURN_ITALIC,
				false);

		store.setDefault(PreferenceConstants.EDITOR_SMART_INDENT, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_STRINGS, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACKETS, true);
		store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACES, true);
		store.setDefault(VjetPreferenceConstants.EDITOR_CLOSE_COMMENTS, true);
		store.setDefault(PreferenceConstants.EDITOR_SMART_TAB, true);
		store.setDefault(PreferenceConstants.EDITOR_SMART_PASTE, true);
		store.setDefault(PreferenceConstants.EDITOR_SMART_HOME_END, true);
		store.setDefault(PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION, true);
		store.setDefault(PreferenceConstants.EDITOR_TAB_WIDTH, 8);
		store.setDefault(
				PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE, true);

		// folding
		store.setDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
		store.setDefault(VjetPreferenceConstants.EDITOR_FOLDING_INNERTYPES,	false);
		store.setDefault(VjetPreferenceConstants.EDITOR_FOLDING_METHODS, false);
		store.setDefault(VjetPreferenceConstants.EDITOR_FOLDING_IMPORTS, true);
		store.setDefault(VjetPreferenceConstants.EDITOR_FOLDING_HEADERS, true);

		store.setDefault(CodeFormatterConstants.FORMATTER_TAB_CHAR,
				CodeFormatterConstants.TAB);
		store.setDefault(CodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		store.setDefault(CodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "4");

		NewScriptProjectPreferencePage.initDefaults(store);

		store.setDefault(PreferenceConstants.APPEARANCE_COMPRESS_PACKAGE_NAMES,
				false);
		store.setDefault(PreferenceConstants.APPEARANCE_METHOD_RETURNTYPE,
				false);
		store.setDefault(PreferenceConstants.APPEARANCE_METHOD_TYPEPARAMETERS,
				true);
		store.setDefault(
				PreferenceConstants.APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW,
				""); //$NON-NLS-1$

		store.setDefault(PreferenceConstants.SHOW_SOURCE_MODULE_CHILDREN, true);

		store.setDefault(
				PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS, ".");
		
		PreferenceConstants.initializeDefaultValues(store);

		//Code assist
		store.setDefault(VjetPreferenceConstants.CODEASSIST_USETHISVJ$, false);
		
		// mark occurrences
		store.setDefault(VjetPreferenceConstants.EDITOR_MARK_OCCURRENCES, true);
		store.setDefault(VjetPreferenceConstants.EDITOR_STICKY_OCCURRENCES, true);
		store.setDefault(VjetPreferenceConstants.EDITOR_MARK_METHOD_OCCURRENCES, true);
		store.setDefault(VjetPreferenceConstants.EDITOR_MARK_FIELD_OCCURRENCES, true);
		store.setDefault(VjetPreferenceConstants.EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES, true);
		
		//new VJO project(build path)
		store.setDefault(VjetPreferenceConstants.SRCBIN_FOLDERS_IN_NEWPROJ, true);
		store.setDefault(VjetPreferenceConstants.SRC_SRCNAME, "src");
		
		
		// MembersOrderPreferencePage, use dltk ui store.
		DLTKUIPlugin.getDefault().getPreferenceStore().setDefault(VjetPreferenceConstants.APPEARANCE_MEMBER_SORT_ORDER, "T,SF,SI,SM,F,I,C,M"); //$NON-NLS-1$
		DLTKUIPlugin.getDefault().getPreferenceStore().setDefault(VjetPreferenceConstants.APPEARANCE_VISIBILITY_SORT_ORDER, "B,V,R,D"); //$NON-NLS-1$
		DLTKUIPlugin.getDefault().getPreferenceStore().setDefault(VjetPreferenceConstants.APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER, false);
		
		
		
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_TASK_TAGS_COMMENT_COLOR,  new RGB(127, 159, 191));	
		store.setDefault(VjetPreferenceConstants.EDITOR_TASK_TAGS_COMMENT_BOLD, true);
	    //store.setDefault(VjetPreferenceConstants.EDITOR_TASK_TAG_ITALIC, false);

		
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_JAVADOC_HTML_MARKUP_COLOR,   new RGB(127, 127, 159));
		store.setDefault(VjetPreferenceConstants.EDITOR_JAVADOC_HTML_MARKUP_BOLD, false);
		//store.setDefault(VjetPreferenceConstants.EDITOR_JAVADOC_KEYWORD_ITALIC, false);

		
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_JAVADOC_TAGS_COLOR,  new RGB(127, 127, 159));
		store.setDefault(VjetPreferenceConstants.EDITOR_JAVADOC_TAGS_BOLD, true);
//		store.setDefault(VjetPreferenceConstants.EDITOR_JAVADOC_TAG_ITALIC, false);

		
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_JAVADOC_LINKS_COLOR,  new RGB(63, 63, 191));
//		store.setDefault(VjetPreferenceConstants.EDITOR_JAVADOC_LINKS_BOLD, false);
//		store.setDefault(VjetPreferenceConstants.EDITOR_JAVADOC_LINKS_ITALIC, false);

		
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_JAVADOC_OTHERS_COLOR,  new RGB(63, 95, 191));
//		store.setDefault(VjetPreferenceConstants.EDITOR_JAVADOC_DEFAULT_BOLD, false);
//		store.setDefault(VjetPreferenceConstants.EDITOR_JAVADOC_DEFAULT_ITALIC, false);
		
		PreferenceConverter.setDefault(store,
				VjetPreferenceConstants.EDITOR_REGEXP_CORE_COLOR,  new RGB(63, 95, 191));
		store.setDefault(
				SHOW_UNKNOWN_CONTENT_TYPE_MSG,  true);
		//VjetPreferenceConstants.EDITOR_REGEXP_CORE_COLOR
		
	}
	
	/**
	 * A preference that controls the selected formatter.
	 */
	public static final String FORMATTER_ID = "formatterId"; //$NON-NLS-1$
	
	public static final String TOGGLE_MARK_OCCURRENCES_ACTION = "org.ebayopensource.vjet.eclipse.ui.toggle_mark_occurrences_action_context"; //$NON-NLS-1$

	public static final String SHOW_UNKNOWN_CONTENT_TYPE_MSG = "showUnknownContentTypeMsg";

	public static final String CODEASSIST_USETHISVJ$ = "use_this.vj$";
	public static final String CODEASSIST_APPENDCOMMENT = "append_annotion";
	public static final String CODEASSIST_CAMEL_MATCH = "camel_match";

	public static final String EDITOR_DEFAULT_COLOR = DLTKColorConstants.DLTK_DEFAULT;
	
	
}

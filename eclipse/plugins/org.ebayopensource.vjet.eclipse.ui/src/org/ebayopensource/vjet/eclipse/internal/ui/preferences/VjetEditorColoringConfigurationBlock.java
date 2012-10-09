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
package org.ebayopensource.vjet.eclipse.internal.ui.preferences;

import java.io.InputStream;

import org.ebayopensource.vjet.eclipse.internal.ui.editor.semantic.highlighting.VjoSemanticHighlighting;
import org.ebayopensource.vjet.eclipse.internal.ui.editor.semantic.highlighting.VjoSemanticHighlightingManager;
import org.ebayopensource.vjet.eclipse.internal.ui.editor.semantic.highlighting.VjoSemanticHighlightingManager.HighlightedRange;
import org.ebayopensource.vjet.eclipse.internal.ui.editor.semantic.highlighting.VjoSemanticHighlightings;
import org.ebayopensource.vjet.eclipse.internal.ui.text.SimpleVjoSourceViewerConfiguration;
import org.ebayopensource.vjet.eclipse.ui.VjetPreferenceConstants;
import org.ebayopensource.vjet.eclipse.ui.VjetUIPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.dltk.mod.internal.ui.editor.ScriptSourceViewer;
import org.eclipse.dltk.mod.javascript.internal.ui.editor.JavaScriptDocumentSetupParticipant;
import org.eclipse.dltk.mod.javascript.ui.text.IJavaScriptPartitions;
import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
import org.eclipse.dltk.mod.ui.editor.highlighting.SemanticHighlighting;
import org.eclipse.dltk.mod.ui.preferences.AbstractScriptEditorColoringConfigurationBlock;
import org.eclipse.dltk.mod.ui.preferences.IPreferenceConfigurationBlock;
import org.eclipse.dltk.mod.ui.preferences.OverlayPreferenceStore;
import org.eclipse.dltk.mod.ui.preferences.PreferencesMessages;
import org.eclipse.dltk.mod.ui.text.IColorManager;
import org.eclipse.dltk.mod.ui.text.ScriptSourceViewerConfiguration;
import org.eclipse.dltk.mod.ui.text.ScriptTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class is representation of the vjet/editor/syntax coloring configuration block.
 *  
 * 
 *
 */
public class VjetEditorColoringConfigurationBlock extends
		AbstractScriptEditorColoringConfigurationBlock implements
		IPreferenceConfigurationBlock {

	public static final String JS_DOC = "Jsdoc";

	private static final String[][] fSyntaxColorListModel = new String[][] {
			{ PreferencesMessages.DLTKEditorPreferencePage_singleLineComment,
					VjetPreferenceConstants.EDITOR_SINGLE_LINE_COMMENT_COLOR,
					sCommentsCategory },
			
			{ "Multi-line comment",
						VjetPreferenceConstants.EDITOR_MULTI_LINE_COMMENT_COLOR,
						sCommentsCategory },
			{ "Task Tags",
						VjetPreferenceConstants.EDITOR_TASK_TAGS_COMMENT_COLOR,
						sCommentsCategory },
						
			{ PreferencesMessages.DLTKEditorPreferencePage_keywords,
					VjetPreferenceConstants.EDITOR_KEYWORD_COLOR, sCoreCategory },

			{ PreferencesMessages.DLTKEditorPreferencePage_strings,
					VjetPreferenceConstants.EDITOR_STRING_COLOR, sCoreCategory },

			{ PreferencesMessages.DLTKEditorPreferencePage_regexps,
					VjetPreferenceConstants.EDITOR_REGEXP_CORE_COLOR,
					sCoreCategory },
					
			{ PreferencesMessages.DLTKEditorPreferencePage_default,
						VjetPreferenceConstants.EDITOR_DEFAULT_COLOR, sCoreCategory },
						

			{ PreferencesMessages.DLTKEditorPreferencePage_numbers,
					VjetPreferenceConstants.EDITOR_NUMBER_COLOR, sCoreCategory },

//			{ PreferencesMessages.DLTKEditorPreferencePage_function_colors,
//					VjetPreferenceConstants.EDITOR_FUNCTION_DEFINITION_COLOR,
//					sCoreCategory },

			{ "HTML Markup",
					VjetPreferenceConstants.EDITOR_JAVADOC_HTML_MARKUP_COLOR,
					JS_DOC },

			{ "Links",
					VjetPreferenceConstants.EDITOR_JAVADOC_LINKS_COLOR,
					JS_DOC },

			{ "Others",
					VjetPreferenceConstants.EDITOR_JAVADOC_OTHERS_COLOR,
					JS_DOC },

			{ "Tags",
					VjetPreferenceConstants.EDITOR_JAVADOC_TAGS_COLOR,
					JS_DOC },
					
			/*
			 * { "XML Tag Name",
			 * VjetPreferenceConstants.EDITOR_XML_TAG_NAME_COLOR, "XML" }, {
			 * "XML Attribute Name",
			 * VjetPreferenceConstants.EDITOR_XML_ATTR_NAME_COLOR, "XML" }, {
			 * "XML Comment", VjetPreferenceConstants.EDITOR_XML_COMMENT_COLOR,
			 * "XML" },
			 */
	// { "XML Area Background
	// color",JavascriptPreferenceConstants.EDITOR_XML_BODY_ALL
	// , "XML"},
	};

	private static final String PREVIEW_FILE_NAME = "PreviewFile.txt";

	public VjetEditorColoringConfigurationBlock(OverlayPreferenceStore store) {
		super(store);
	}
	
	
	

	@Override
	protected SemanticHighlightingColorListItem createSemanticHighlightingItem(SemanticHighlighting highLighting) {
		if(highLighting instanceof VjoSemanticHighlighting){
			VjoSemanticHighlighting vjoSemanticHighlighting=(VjoSemanticHighlighting)highLighting;
			return new SemanticHighlightingColorListItem(
					vjoSemanticHighlighting.getDisplayName(), 
					VjoSemanticHighlightings.getColorPreferenceKey(vjoSemanticHighlighting),
					VjoSemanticHighlightings.getBoldPreferenceKey(vjoSemanticHighlighting),
					VjoSemanticHighlightings.getItalicPreferenceKey(vjoSemanticHighlighting),
					VjoSemanticHighlightings.getStrikethroughPreferenceKey(vjoSemanticHighlighting),
					VjoSemanticHighlightings.getUnderlinePreferenceKey(vjoSemanticHighlighting),sCoreCategory,
					VjoSemanticHighlightings.getEnabledPreferenceKey(vjoSemanticHighlighting)
			);
		}else{
			return super.createSemanticHighlightingItem(highLighting);
		}
	}




	@Override
	protected ProjectionViewer createPreviewViewer(Composite parent,
			IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
			boolean showAnnotationsOverview, int styles, IPreferenceStore store) {
		return new ScriptSourceViewer(parent, verticalRuler, overviewRuler,
				showAnnotationsOverview, styles, store);
	}

	@Override
	protected ScriptSourceViewerConfiguration createSimpleSourceViewerConfiguration(
			IColorManager colorManager, IPreferenceStore preferenceStore,
			ITextEditor editor, boolean configureFormatter) {
		return new SimpleVjoSourceViewerConfiguration(colorManager,
				preferenceStore, editor, IJavaScriptPartitions.JS_PARTITIONING,
				configureFormatter);
	}

	@Override
	protected String[] getCategories() {
		return new String[] { sCoreCategory, sDocumentationCategory,
				sCommentsCategory, JS_DOC };
	}

	@Override
	protected InputStream getPreviewContentReader() {
		return getClass().getResourceAsStream(PREVIEW_FILE_NAME);
	}

	@Override
	protected String[][] getSyntaxColorListModel() {
		return fSyntaxColorListModel;
	}

	@Override
	protected void setDocumentPartitioning(IDocument document) {
		// TODO replace by appropriate VJO class
		JavaScriptDocumentSetupParticipant participant = new JavaScriptDocumentSetupParticipant();
		participant.setup(document);
	}

	@Override
	protected Preferences createTemporaryCorePreferenceStore() {
		// TODO Auto-generated method stub
		return VjetUIPlugin.getDefault().getPluginPreferences();
	}
	

	@Override
	protected ScriptTextTools getTextTools() {
		return VjetUIPlugin.getDefault().getTextTools();
	}
	
	/**
	 * Semantic highlighting manager
	 */
	protected VjoSemanticHighlightingManager fSemanticHighlightingManager;
	
	/**
	 * Install Semantic Highlighting on the previewer
	 */
	protected void installSemanticHighlighting() {
		// eBay modify, change to protected
		final ScriptTextTools textTools = getTextTools();
		if (fSemanticHighlightingManager == null && textTools != null) {
			fSemanticHighlightingManager = new VjoSemanticHighlightingManager() {
				protected ScriptTextTools getTextTools() {
					return textTools;
				}
			};
			fSemanticHighlightingManager.install(
					(ScriptSourceViewer) fPreviewViewer, fColorManager,
					getPreferenceStore(),createPreviewerRanges());
		}
	}
	
	/**
	 * Create a highlighted range on the previewers document with the given line, column, length and key.
	 * 
	 * @param line the line
	 * @param column the column
	 * @param length the length
	 * @param key the key
	 * @return the highlighted range
	 * @since 3.0
	 */
	private HighlightedRange createHighlightedRange(int line, int column, int length, String key) {
		try {
			IDocument document= fPreviewViewer.getDocument();		
			int offset= document.getLineOffset(line) + column;
			return new HighlightedRange(offset, length, key);
		} catch (BadLocationException x) {
			DLTKUIPlugin.log(x);
		}
		return null;
	}

	
	protected int getLine(String text) throws BadLocationException{
		IDocument document= fPreviewViewer.getDocument();
		int index=document.get().indexOf(text);
		return document.getLineOfOffset(index);
	}
	
    protected int getColumn(int line, String text) throws BadLocationException{
    	IDocument document= fPreviewViewer.getDocument();	
    	int index=document.get().indexOf(text);
		int column= index - document.getLineOffset(line);
		return column;
	}
	
	/**
	 * Create the hard coded previewer ranges
	 * 
	 * @return the hard coded previewer ranges
	 * @since 3.0
	 */
	private HighlightedRange[][] createPreviewerRanges() {
		/*
		 * count number, change previewFile.txt must change this file. use below code to get actually line and column
		 */ 
//		int line=0;
//		int column=0;
//		try {
//			line = getLine("foobar :");
//			column=getColumn(line,"foobar :");
//			
//			line=getLine("foo :");
//			column=getColumn(line,"foo :");
//
//			line=getLine("bar : function (name)");
//			column=getColumn(line,"bar : function (name)");
//	
//		} catch (BadLocationException e) {
//		}
		
		return new HighlightedRange[][] {
				{ createHighlightedRange(18, 2, 6, VjoSemanticHighlightings.METHOD_DECLARATION) },
				{ createHighlightedRange(25, 5, 3, VjoSemanticHighlightings.METHOD_DECLARATION) },
				{ createHighlightedRange(35, 5, 3, VjoSemanticHighlightings.METHOD_DECLARATION) },
				};
	}
	
	
}

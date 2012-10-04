/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/**
 * 
 */
package org.ebayopensource.vjet.eclipse.core;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.dltk.mod.core.AbstractLanguageToolkit;
import org.eclipse.dltk.mod.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.mod.core.environment.IFileHandle;

/**
 * 
 * 
 */
public class VjoLanguageToolkit extends AbstractLanguageToolkit {
	private static final String LANGUAGE_NAME = "VJO (JavaScript)";

	private static VjoLanguageToolkit s_instance = new VjoLanguageToolkit();

	public static final String VJET_CONTENT_TYPE_ID = "org.eclipse.dltk.mod.vjoContentType";
	
	public static IDLTKLanguageToolkit getDefault() {
		return s_instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.mod.core.IDLTKLanguageToolkit#getLanguageContentType()
	 */
	public String getLanguageContentType() {
		return VJET_CONTENT_TYPE_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.mod.core.IDLTKLanguageToolkit#getLanguageName()
	 */
	public String getLanguageName() {
		return LANGUAGE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.mod.core.IDLTKLanguageToolkit#getNatureId()
	 */
	public String getNatureId() {
		return VjoNature.NATURE_ID;
	}

	@Override
	public boolean languageSupportZIPBuildpath() {
		return true;
	}
	
	/**
	 * Check if the file is VJET content type 
	 * @param file
	 * @return
	 */
	public static boolean isVjetContentType(IFile file) {
		IContentType type1 = getVjetContentType();
		IContentType[] type = Platform.getContentTypeManager().findContentTypesFor(file.getName());
		
		boolean isKind = false;
		for (IContentType iContentType : type) {
			if(iContentType.isKindOf(type1)){
				isKind = true;
				break;
			}
		}
		
		return type != null && isKind;
	}
	
	// add by patrick
	public static IContentType getVjetContentType() {
		return Platform.getContentTypeManager().getContentType(
				VJET_CONTENT_TYPE_ID);
	}

	@Override
	public boolean canValidateContent(File file) {
		IContentType contentType = getVjetContentType();
		return contentType.isAssociatedWith(file.getName());
	}

	@Override
	public boolean canValidateContent(IFileHandle file) {
		IContentType contentType = getVjetContentType();
		return contentType.isAssociatedWith(file.getName());
	}

	@Override
	public boolean canValidateContent(IResource resource) {
		if (IResource.FILE == resource.getType()) {
			return isVjetContentType((IFile) resource);
		}
		return false;
	}
	// end add
}

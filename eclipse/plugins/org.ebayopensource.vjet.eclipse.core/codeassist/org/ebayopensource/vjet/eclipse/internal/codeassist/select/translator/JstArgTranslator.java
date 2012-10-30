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
package org.ebayopensource.vjet.eclipse.internal.codeassist.select.translator;

import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.declaration.JstArg;
import org.ebayopensource.vjet.eclipse.core.IVjoSourceModule;
import org.ebayopensource.vjet.eclipse.core.VjetPlugin;
import org.ebayopensource.vjet.eclipse.internal.codeassist.select.JstNodeDLTKElementResolver;
import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.core.IType;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.dltk.mod.internal.core.JSSourceMethod;

/**
 * 
 * 
 */
public class JstArgTranslator extends DefaultNodeTranslator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.vjet.eclipse.internal.codeassist.select.translator.DefaultNodeTranslator#convert(org.ebayopensource.dsf.jst.IJstNode,
	 *      org.eclipse.dltk.mod.core.ISourceModule)
	 */
	public IModelElement[] convert(IVjoSourceModule module, IJstNode jstNode) {
		JstArg jstArg = (JstArg) jstNode;

		// directly to find method argument
		
		try {
			IModelElement[] convert = JstNodeDLTKElementResolver.convert(module, jstNode.getRootType());
			if(convert==null || convert.length==0){
				return null;
			}
			IType dltkType = (IType) convert[0];
			IModelElement modelElement = dltkType.getSourceModule().getElementAt(jstArg
					.getSource().getStartOffSet());
			if (modelElement instanceof JSSourceMethod) {
				JSSourceMethod sourceMethod = (JSSourceMethod) modelElement;
				String argName = jstArg.getName();

				IModelElement[] children = sourceMethod.getChildren();
				for (int i = 0; i < children.length; i++) {
					if (children[i].getElementType() == IModelElement.FIELD
							&& children[i].getElementName().equals(argName)) {
						return new IModelElement[]{children[i]};
					}
				}
			}
		} catch (ModelException e) {
			VjetPlugin.error(e.getLocalizedMessage(), e);
		}

		return null;
	}
}

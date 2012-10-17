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
package org.ebayopensource.vjet.eclipse.internal.ui.view.scriptunit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ebayopensource.dsf.jst.BaseJstNode;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.IScriptUnit;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 *
 */
class ScriptUnitTreeContentProvider implements ITreeContentProvider {
	private IScriptUnit scriptUnit;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement.getClass().isArray()) {
			return (Object[])parentElement;
		}
		
		if (parentElement instanceof IScriptUnit) {
			return this.getScriptUnitChildren((IScriptUnit)parentElement);
		}
		
		if (parentElement instanceof IJstType) {
			IJstType jstType = (IJstType)parentElement;
			
			List children = new ArrayList();
			
			//add embeded types
			if(jstType.getEmbededTypes() != null) {
				Collections.addAll(children, jstType.getEmbededTypes().toArray());
			}
			
			//add children 
			Collections.addAll(children, jstType.getChildren().toArray());
			
			return children.toArray();
		}
			
		if (parentElement instanceof BaseJstNode) {
			List<BaseJstNode> children = ((BaseJstNode)parentElement).getChildren();
			return children.toArray(new BaseJstNode[children.size()]);
		}
		
		return new Object[0];
	}

	private Object[] getScriptUnitChildren(IScriptUnit scriptUnit) {
		List children = new ArrayList();
		
		//add jst type
		if (scriptUnit.getType() != null)
			children.add(scriptUnit.getType());
		
//		//syntax root
//		if (scriptUnit.getSyntaxRoot() != null)
//			children.add(scriptUnit.getSyntaxRoot());
		
		//jst block list
		if (scriptUnit.getJstBlockList()!=null && scriptUnit.getJstBlockList().size() > 0)
			children.addAll(scriptUnit.getJstBlockList());
		
//		//problem
//		if (scriptUnit.getProblems().size() > 0)
//			children.add(scriptUnit.getProblems());
		
		return children.toArray();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof IJstType) {
			if (((IJstType)element).getOuterType() != null)
				return ((IJstType)element).getOuterType();
			
			return this.scriptUnit;
		}
		
		if (element instanceof BaseJstNode)
			return ((BaseJstNode)element).getParentNode();
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return this.getChildren(element).length > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return this.getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param scriptUnit the scriptUnit to set
	 */
	public void setScriptUnit(IScriptUnit scriptUnit) {
		this.scriptUnit = scriptUnit;
	}

}

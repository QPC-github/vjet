/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.search;

import java.util.Iterator;
import java.util.List;

import org.ebayopensource.dsf.jst.IJstGlobalVar;
import org.ebayopensource.dsf.jst.IJstMethod;
import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.JstSource;
import org.ebayopensource.dsf.ts.method.MethodName;
import org.ebayopensource.dsf.ts.type.TypeName;
import org.ebayopensource.vjet.eclipse.codeassist.CodeassistUtils;
import org.ebayopensource.vjet.eclipse.core.IVjoSourceModule;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;
import org.eclipse.dltk.mod.core.Flags;
import org.eclipse.dltk.mod.core.IMethod;
import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.core.ISourceRange;
import org.eclipse.dltk.mod.core.IType;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.dltk.mod.core.search.SearchPattern;
import org.eclipse.dltk.mod.internal.core.JSSourceMethod;
import org.eclipse.dltk.mod.internal.core.NativeVjoSourceModule;
import org.eclipse.dltk.mod.internal.core.search.matching.MethodPattern;

/**
 * Search declaration and references for method.
 * 
 * 
 * 
 */
public class VjoMethodSearcher extends AbstractVjoElementSearcher {

	private static final String ENCLOSING_TYPE_SEPARATOR = ".";

	public Class<? extends SearchPattern> getSearchPatternClass() {
		return MethodPattern.class;
	}

	@Override
	protected void searchDeclarations(SearchQueryParameters params,
			List<VjoMatch> result) {
		JSSourceMethod method = (JSSourceMethod) params.getElement();
		IVjoSourceModule module = (IVjoSourceModule) method.getSourceModule();
		TypeName typeName = module.getTypeName();

		IType type = CodeassistUtils.findType(module, typeName.typeName());
		if (isInScope(type)) {
			try {
				ISourceRange nameRange = method.getNameRange();
				VjoMatch match = VjoMatchFactory.createMethodMatch(type,
						nameRange.getOffset(), nameRange.getLength());

				// Add by Oliver.2009-06-25.
				try {
					match.setIsPublic(Flags.isPublic(method.getFlags()));
					match.setIsStatic(Flags.isStatic(method.getFlags()));
				} catch (ModelException e) {
				}

				result.add(match);
			} catch (ModelException e) {
				// VjetPlugin.getDefault().getLog().log(
				// new Status(IStatus.ERROR, VjetPlugin.PLUGIN_ID,
				// IStatus.ERROR, "Wrong name range", e));
			}
		}
	}

	@Override
	protected void searchReferences(SearchQueryParameters params,
			List<VjoMatch> result) {
		IMethod method = (IMethod) params.getElement();

		if (method.getSourceModule() instanceof NativeVjoSourceModule)
			this.processNativeMethodReference(method, result);
		else
			this.processSourceMethodReference(method, result);
	}

	// process source method reference: including source method in inner type
	private void processSourceMethodReference(IMethod method,
			List<VjoMatch> result) {
		IVjoSourceModule module = (IVjoSourceModule) method.getSourceModule();
		IJstType jstType = TypeSpaceMgr.findType(module.getTypeName()
				.groupName(), module.getTypeName().typeName());
		String dltkTypeName = ((IType) method.getParent())
				.getFullyQualifiedName(ENCLOSING_TYPE_SEPARATOR);

		IJstMethod jstMethod = this.getJstMethod(jstType, dltkTypeName, method
				.getElementName(), true);
		if (jstMethod == null)
			return;

		
		
		// work out PropertyName and find referenced nodes from TypeSpaceMgr
		String grouName = jstMethod.getOwnerType().getPackage().getGroupName();
		String typeName = jstMethod.getOwnerType().getName();
		MethodName methodName = new MethodName(
				new TypeName(grouName, typeName), method.getElementName());

		// find reference nodes
		List<IJstNode> list = mgr.getMethodDependents(methodName);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			IJstNode jstNode = (IJstNode) iterator.next();
			this.createMatch(jstNode, method, result);
		}
	}

	// process native method reference: because currently, native type name does
	// not match IJstType.getAlias
	private void processNativeMethodReference(IMethod method,
			List<VjoMatch> result) {
		NativeVjoSourceModule nativeVjoSourceModule = (NativeVjoSourceModule) method
				.getSourceModule();

		String groupName = nativeVjoSourceModule.getTypeName().groupName();
		String typeName = nativeVjoSourceModule.getType().getName();
		IModelElement parent = method.getParent();
//		if (parent instanceof IType) {
//			typeName = ((IType) parent)
//					.getTypeQualifiedName(ENCLOSING_TYPE_SEPARATOR);
//		}
		MethodName methodNameTest = new MethodName(new TypeName(groupName,
				typeName), method.getElementName());

		List<IJstNode> list = mgr.getMethodDependents(methodNameTest);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			IJstNode jstNode = (IJstNode) iterator.next();
			this.createMatch(jstNode, method, result);
		}
	}

	// currently, not support anoymous inner type
	private IJstMethod getJstMethod(IJstType type, String typeName,
			String methodName, boolean alias) {
		if(type==null){
			return null;
		}
		
		if ((alias)
				|| (!alias && type.getName().equals(typeName))) {
			IJstMethod jstMethod = type.getMethod(methodName);
			
			if (jstMethod == null && "constructs".equals(methodName))
				jstMethod = type.getConstructor();
			if (jstMethod == null) {
				IJstGlobalVar gvar = type.getGlobalVar(methodName);
				if(gvar!=null){
					jstMethod = gvar.getFunction();
				}
			}
			return jstMethod;
		} else {
			// iterate inner types...
			for (Iterator iterator = type.getEmbededTypes().iterator(); iterator
					.hasNext();) {
				IJstType innerType = (IJstType) iterator.next();
				IJstMethod jstMethod = this.getJstMethod(innerType, typeName,
						methodName, alias);
				if (jstMethod != null)
					return jstMethod;
			}
		}
		return null;
	}

	private void createMatch(IJstNode jstNode, IMethod method,
			List<VjoMatch> result) {
		//if (!isValidNode(jstNode))
			//return;

		IType dltkType = CodeassistUtils.findType(jstNode.getRootType());
		if (dltkType == null)
			return;

		//Check if the file is visible in the result project's build path
//		IType declareType = CodeassistUtils.findType((ScriptProject)dltkType.getScriptProject(), method.getDeclaringType().getFullyQualifiedName("."));
//		if (declareType == null) {
//			return;
//		}
		JstSource jstSource = jstNode.getSource();
		int length = jstSource.getEndOffSet() - jstSource.getStartOffSet() + 1;
		VjoMatch match = VjoMatchFactory.createTypeMatch(dltkType, jstSource
				.getStartOffSet(), length);
		result.add(match);
	}

	/**
	 * filer the temp method created by back-end.
	 * 
	 * @param node
	 * @return
	 */
	private boolean isValidNode(IJstNode node) {
		IJstMethod jstMethod = CodeassistUtils.findDeclaringMethod(node);
		if (jstMethod == null) {
			return true;
		}
		if (jstMethod.isConstructor()) {
			return jstMethod.getOwnerType().getConstructor() != null;
		}
		IJstMethod validJstMethod = jstMethod.getOwnerType().getMethod(
				jstMethod.getName().getName());
		// if (jstMethod.equals(validJstMethod))
		// if (jstMethod == validJstMethod)
		// return false;

		return validJstMethod != null;
	}

	// add by patrick
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ebayopensource.vjet.eclipse.core.search.AbstractVjoElementSearcher#findOccurrence
	 * (org.ebayopensource.dsf.jst.IJstNode, org.ebayopensource.dsf.jst.IJstNode)
	 */
	public List<VjoMatch> findOccurrence(IJstNode searchedJstNode,
			IJstNode searchedTree) {
		
		if(searchedJstNode.getParentNode() instanceof IJstMethod){
			searchedJstNode = (IJstMethod)searchedJstNode.getParentNode();
		}
		
		VjoMehtodOccurrenceVisitor visitor = new VjoMehtodOccurrenceVisitor(
				(IJstMethod) searchedJstNode);
		searchedTree.accept(visitor);
		return visitor.getMatches();
	}

	// end add
}

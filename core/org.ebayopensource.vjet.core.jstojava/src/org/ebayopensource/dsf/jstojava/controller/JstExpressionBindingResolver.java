/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jstojava.controller;

import java.util.List;

import org.ebayopensource.dsf.jsgen.shared.jstvalidator.DefaultJstProblem;
import org.ebayopensource.dsf.jst.IJstGlobalFunc;
import org.ebayopensource.dsf.jst.IJstGlobalProp;
import org.ebayopensource.dsf.jst.IJstGlobalVar;
import org.ebayopensource.dsf.jst.IJstMethod;
import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.IJstOType;
import org.ebayopensource.dsf.jst.IJstProperty;
import org.ebayopensource.dsf.jst.IJstRefResolver;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.IScriptProblem;
import org.ebayopensource.dsf.jst.JstSource.IBinding;
import org.ebayopensource.dsf.jst.ProblemSeverity;
import org.ebayopensource.dsf.jst.ResolutionResult;
import org.ebayopensource.dsf.jst.declaration.JstAttributedType;
import org.ebayopensource.dsf.jst.declaration.JstFuncType;
import org.ebayopensource.dsf.jst.declaration.JstGlobalProp;
import org.ebayopensource.dsf.jst.declaration.JstSynthesizedProperty;
import org.ebayopensource.dsf.jst.declaration.JstType;
import org.ebayopensource.dsf.jst.term.JstIdentifier;
import org.ebayopensource.dsf.jst.ts.JstTypeSpaceMgr;
import org.ebayopensource.dsf.jstojava.report.DefaultErrorReporter;
import org.ebayopensource.dsf.jstojava.report.ErrorReporter;
import org.ebayopensource.dsf.jstojava.translator.TranslateHelper.RenameableSynthJstProxyProp;
import org.ebayopensource.dsf.ts.ITypeSpace;

public class JstExpressionBindingResolver implements IJstRefResolver {

	private static final boolean DEBUG = false;

	private final JstParseController m_controller;

	private final JstExpressionTypeLinker m_typeLinkerVisitor;

	private ErrorReporter m_errorReporter = new DefaultErrorReporter();


	public JstExpressionBindingResolver(JstParseController controller) {
		m_controller = controller;
		m_typeLinkerVisitor = new JstExpressionTypeLinker(this);
	}

	public synchronized ResolutionResult resolve(IJstType type) {

		ResolutionResult rr = new ResolutionResult();
		resolveTypeInternal(type, rr);
		return rr;
		// processMixins(type);
	}

	private void resolveTypeInternal(IJstType type, ResolutionResult rr) {
		promoteGlobals(type, m_controller.getJstTypeSpaceMgr(), rr);

		m_typeLinkerVisitor.setCurrentType(type);
		m_typeLinkerVisitor.setGroupName(type.getPackage().getGroupName());
		JstExpressionTypeLinkerTraversal.accept(type, m_typeLinkerVisitor);
		// hijack type with factory created argument
		if (m_typeLinkerVisitor.getTypeConstructedDuringLink()) {
			JstExpressionTypeLinkerTraversal.accept(m_typeLinkerVisitor.getType(), m_typeLinkerVisitor);
			type = m_typeLinkerVisitor.getType();
		}
		if (type instanceof JstType) {
			JstType type2 = (JstType) type;
			setResolutionStatus(type2);
			rr.setType(type2);
			
		}
	}
	


	private void promoteGlobals(IJstType type, JstTypeSpaceMgr mgr,
			ResolutionResult rr) {
		if (type.hasGlobalVars()) {
			ITypeSpace<IJstType, IJstNode> typeSpace = mgr.getTypeSpace();
			boolean error = false;
			if (type instanceof JstType) {
				JstType type2 = (JstType) type;
				if (!type2.getStatus().areGlobalsPromoted()) {

					typeSpace.removeGlobalsFromType(type.getPackage()
							.getGroupName(), type.getName());

					error = validateGlobal(type, mgr, rr, error);
					if (!error) {
						for (IJstGlobalVar gvar : type.getGlobalVars()) {
							final String groupName = type.getPackage().getGroupName();
							final IJstNode globalBinding = JstExpressionTypeLinkerHelper.look4ActualBinding(this, gvar.getType(), new GroupInfo(groupName, null));
							
							if(gvar.getType() instanceof JstAttributedType){
								if(!gvar.isFunc()){
									final IJstGlobalProp globalPty = gvar.getProperty();
									if(globalPty instanceof JstGlobalProp){
										if(globalBinding instanceof IJstProperty){
											((JstGlobalProp)globalPty)
												.setProperty(new RenameableSynthJstProxyProp((IJstProperty)globalBinding, globalPty.getName().getName()));
										}
										else if(globalBinding instanceof IJstMethod){
											((JstGlobalProp)globalPty)
												.setProperty(new JstSynthesizedProperty(new JstFuncType((IJstMethod)globalBinding), globalPty.getName().getName(), (JstIdentifier)null, null));
										}
									}
								}
							}
							
							
							//TODO replace gvar symbol with attributed type bound updates
							typeSpace.addToGlobalSymbolMap(groupName,
									gvar.getName().getName(), gvar.getOwnerType().getName(), gvar);
						}
					}
				}
			}
		}
	}

	private boolean validateGlobal(IJstType type, JstTypeSpaceMgr mgr,
			ResolutionResult rr, boolean error) {
		for (IJstGlobalVar var : type.getGlobalVars()) {
			
			String glbScope = var.getScopeForGlobal();
			if (glbScope != null) {
				//TODO validate conflict under given global scope
				continue;
			}
			
			String name = var.getName().getName();
			String groupName = type.getPackage().getGroupName();
			IJstNode findGlobal = mgr.getQueryExecutor().findGlobalVar(groupName, name, true);
			
			if (findGlobal == null) {
				continue;
			}
			
			// determine if global conflicts with global type or type without
			// package
			List<IJstType> list = mgr.getTypeSpace().getType(name);
			if (!list.isEmpty()) {
				IScriptProblem prblm = reportError(type, var, name,
						findGlobal, "global " + name
								+ " has already been defined in type: " + list.get(0).getName());
				rr.addProblem(prblm);
				error = true;
				continue;
			}
			
			
			// determine if global property or method exists
			if ((findGlobal instanceof IJstGlobalProp  || 
					findGlobal instanceof IJstGlobalFunc) && !findGlobal.getOwnerType().equals(type)) {

				IScriptProblem prblm = reportError(type, var, name,
						findGlobal, "global " + name
								+ " has already been defined in type: "
								+ type.getName());
				rr.addProblem(prblm);

				// TODO should we not promote all?
				error =  true;
				continue;
			}
		}
		return error;
	}

	private IScriptProblem reportError(IJstType type, IJstGlobalVar vars,
			String name, IJstNode findGlobal, String msg) {
		IBinding binding = type.getSource().getBinding();
		
		IScriptProblem prblm = new DefaultJstProblem(null, null, msg,
				(binding).getName()
						.toCharArray(), vars.getSource().getStartOffSet(), vars
						.getSource().getEndOffSet(), vars.getSource().getRow(),
				vars.getSource().getColumn(), ProblemSeverity.error);
		return prblm;
	}

	// set this type and all inner types and oTypes to resolved status
	//
	private void setResolutionStatus(JstType type) {
		type.getStatus().setHasResolution();

		List<JstType> innerTypes = type.getEmbededTypes();

		if (innerTypes != null && !innerTypes.isEmpty()) {
			for (JstType innerType : innerTypes) {
				innerType.getStatus().setHasResolution();
			}
		}

		List<IJstOType> oTypes = type.getOTypes();

		if (oTypes != null && !oTypes.isEmpty()) {
			for (IJstOType oType : oTypes) {
				if (oType instanceof JstType) {
					JstType oJstType = (JstType) oType;
					oJstType.getStatus().setHasResolution();
				}
			}
		}
	}

	// private IJstType processMixins(IJstType jstType) {
	// List<? extends IJstTypeReference> mixins = jstType.getMixins();
	// JstType type = (JstType) jstType;
	//
	// for (IJstTypeReference m : mixins) {
	// IJstType mtype = m.getReferencedType();
	// for (IJstMethod method : mtype.getInstanceMethods()) {
	// type.addMethod(method);
	// }
	// for (IJstMethod smethod : mtype.getStaticMethods()) {
	// type.addMethod(smethod);
	// }
	// for (IJstProperty prop : mtype.getInstanceProperties()) {
	// type.addProperty(prop);
	// }
	// for (IJstProperty sprop : mtype.getStaticProperties()) {
	// type.addProperty(sprop);
	// }
	// }
	// return (IJstType) type;
	// }

	public synchronized ResolutionResult resolve(String groupName, IJstType type) {
		ResolutionResult rr = new ResolutionResult();
		promoteGlobals(type, m_controller.getJstTypeSpaceMgr(), rr);
		m_typeLinkerVisitor.setGroupName(groupName);
		return resolve(type);

	}

	public synchronized ResolutionResult resolve(IJstProperty property) {
		return resolvePropertyInternal(property, new ResolutionResult());
	}

	private ResolutionResult resolvePropertyInternal(IJstProperty property,
			ResolutionResult rr) {
		IJstNode node = property.getInitializer();

		if (node == null) {
			node = property;
		}

		m_typeLinkerVisitor.setCurrentType(property.getOwnerType());
		JstExpressionTypeLinkerTraversal.accept(node, m_typeLinkerVisitor);
		return rr;
	}

	public synchronized ResolutionResult resolve(String groupName,
			IJstProperty property) {
		m_typeLinkerVisitor.setGroupName(groupName);
		return resolvePropertyInternal(property, new ResolutionResult());
	}

	public synchronized ResolutionResult resolve(IJstMethod method) {
		ResolutionResult rr = new ResolutionResult();

		m_typeLinkerVisitor.setCurrentType(method.getOwnerType());
		JstExpressionTypeLinkerTraversal.accept(method, m_typeLinkerVisitor);

		return rr;
	}

	public synchronized ResolutionResult resolve(String groupName,
			IJstMethod method) {
		m_typeLinkerVisitor.setGroupName(groupName);
		return resolve(method);
	}

	public JstParseController getController() {
		return m_controller;
	}

	@Deprecated
	public ErrorReporter getErrorReporter() {
		if (m_errorReporter == null) {
			m_errorReporter = new DefaultErrorReporter();
		}
		return m_errorReporter;
	}

	@Deprecated
	public void setErrorReporter(ErrorReporter reporter) {
		m_errorReporter = reporter;
	}

	@Deprecated
	public void error(String message, String resource, int start, int end,
			int line, int column) {
		if(DEBUG){
			System.err.println("message: " + message + " , resource: " + resource);
		}
//		getErrorReporter().error(message, resource, start, end, line, column);
	}

	@Deprecated
	public void warning(String message, String resource, int start, int end,
			int line, int column) {
		if(DEBUG){
			System.err.println("message: " + message + " , resource: " + resource);
		}
//		getErrorReporter().warning(message, resource, start, end, line, column);
	}

	public synchronized ResolutionResult resolve(IJstType ownerType,
			IJstNode node) {
		ResolutionResult rr = new ResolutionResult();
		m_typeLinkerVisitor.setCurrentType(ownerType);
		JstExpressionTypeLinkerTraversal.accept(node, m_typeLinkerVisitor);
		return rr;
	}

}

/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jsgen.shared.validation.vjo.semantic.validator;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.dsf.jsgen.shared.validation.vjo.VjoSemanticValidator;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.VjoValidationCtx;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.semantic.rules.VjoSemanticRuleRepo;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.BaseVjoSemanticRuleCtx;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.InvalidIdentifierNameWithKeywordRuleCtx;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.semantic.rules.util.TypeCheckUtil;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationPostAllChildrenListener;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationPreAllChildrenListener;
import org.ebayopensource.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationVisitorEvent;
import org.ebayopensource.dsf.jst.IInferred;
import org.ebayopensource.dsf.jst.IJstNode;
import org.ebayopensource.dsf.jst.IJstProperty;
import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.jst.declaration.JstArray;
import org.ebayopensource.dsf.jst.declaration.JstProperty;
import org.ebayopensource.dsf.jst.declaration.SynthOlType;
import org.ebayopensource.dsf.jst.expr.JstArrayInitializer;
import org.ebayopensource.dsf.jst.meta.BaseJsCommentMetaNode;
import org.ebayopensource.dsf.jst.term.SimpleLiteral;
import org.ebayopensource.dsf.jst.token.IExpr;
import org.ebayopensource.vjo.lib.LibManager;

public class VjoJstPropertyValidator 
	extends VjoSemanticValidator
	implements IVjoValidationPreAllChildrenListener,
		IVjoValidationPostAllChildrenListener {

	private static List<Class<? extends IJstNode>> s_targetTypes;
	
	static{
		s_targetTypes = new ArrayList<Class<? extends IJstNode>>();
		s_targetTypes.add(JstProperty.class);
	}

	@Override
	public List<Class<? extends IJstNode>> getTargetNodeTypes() {
		return s_targetTypes;
	}

	@Override
	public void onPreAllChildrenEvent(final IVjoValidationVisitorEvent event){
		final VjoValidationCtx ctx = event.getValidationCtx();
		final IJstNode jstNode = event.getVisitNode();
		if(!(jstNode instanceof IJstProperty)){
			return;
		}
		final IJstProperty jstProperty = (IJstProperty)jstNode;
		if(jstProperty.getName() != null){
			//check property name
			final String jstPropertyName = jstProperty.getName().getName();
			final InvalidIdentifierNameWithKeywordRuleCtx idRuleCtx = new InvalidIdentifierNameWithKeywordRuleCtx(
					jstProperty.getName(), 
					ctx.getGroupId(), 
					new String[]{jstPropertyName, jstPropertyName}, 
					jstPropertyName, 
					jstProperty.getOwnerType() != null && jstProperty.getOwnerType().isEnum());
			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().INVALID_IDENTIFIER_WITH_KEYWORD, idRuleCtx);
		}
		
		//bugfix 6759 & 6760
		//method couldn't be both abstract and static or both abstract and private
		if(jstProperty.getModifiers().isAbstract()){
			if(jstProperty.getModifiers().isStatic()){
				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstProperty.getName(), ctx.getGroupId(), new String[]{jstProperty.getName().getName()});
				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_STATIC_AND_ABSTRACT, ruleCtx);
			}
			if(jstProperty.getModifiers().isPrivate()){
				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstProperty.getName(), ctx.getGroupId(), new String[]{jstProperty.getName().getName()});
				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().METHOD_OR_PROPERTY_SHOULD_NOT_BE_BOTH_PRIVATE_AND_ABSTRACT, ruleCtx);
			}
		}
		
		//Field Hiding Field
		final IJstType ownerType = jstProperty.getOwnerType();
		if(!jstProperty.isStatic() 
				&& ownerType.isClass()
				&& jstProperty.getName() != null){//only ctype has inheritance which could hide super field
			final String propertyName = jstProperty.getName().getName();
			IJstType extType = ownerType.getExtend();
			if(propertyName  != null){
				while(extType != null){
					if(extType.getProperty(propertyName, false, false) != null){
						//report problem
						final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(jstProperty.getName(), ctx.getGroupId(), new String[]{propertyName, extType.getName()});
						satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().PROPERTY_SHOULD_NOT_HIDE_PARENT_PROPERTY, ruleCtx);
						break;
					}
					extType = extType.getExtend();
				}
			}
		}
	}
	
	@Override
	public void onPostAllChildrenEvent(final IVjoValidationVisitorEvent event){
		final VjoValidationCtx ctx = event.getValidationCtx();
		final IJstNode jstNode = event.getVisitNode();
		if(!(jstNode instanceof IJstProperty)){
			return;
		}
		final IJstProperty jstProperty = (IJstProperty)jstNode;
		final IJstType jstType = ctx.getScope().getClosestTypeScopeNode();
		
		validatePropertyType(ctx, jstProperty, jstType);
		
		final IExpr initExpr = jstProperty.getInitializer();
		if(initExpr != null){
			validatePropertyExpr(ctx, jstProperty, initExpr);
		}
		//bugfix 5297, objLiteral is a value instead of an initializer expr
		else if(!jstType.getEnumValues().contains(jstProperty)
				&& jstProperty.getValue() != null){
			if(jstProperty.getValue() instanceof IExpr){
				validatePropertyExpr(ctx, jstProperty, (IExpr)jstProperty.getValue());
			}
		}
	}

	private void validatePropertyExpr(final VjoValidationCtx ctx,
			final IJstProperty jstProperty, final IExpr propertyExpr) {
		if(jstProperty == null 
				|| propertyExpr == null){
			return;
		}
		
		final IJstType initExprValue = propertyExpr.getResultType();
		if(initExprValue != null){
			//property symbol used just for assigned state tracking
			if(!"Object".equals(initExprValue.getName()) 
				|| !(propertyExpr instanceof SimpleLiteral)){//bugfix new Object() case; 8311
				
				ctx.getPropertyStatesTable().assign(jstProperty);
			}
			
			//enum properties are not actual jst properties, skip its initialization validation
			if(jstProperty.getOwnerType() != null 
					&& jstProperty.getOwnerType().getAllPossibleProperties(jstProperty.isStatic(), true).contains(jstProperty)){
				validatePropertyExprInitialization(ctx, jstProperty, propertyExpr);
			}
			
			if(jstProperty.getType() != null
					&& !TypeCheckUtil.isAssignable(jstProperty.getType(), initExprValue)) {
				String[] arguments = new String[3];
				arguments[0] = jstProperty.getType().getSimpleName();
				arguments[1] = initExprValue.getSimpleName();
				arguments[2] = propertyExpr.toExprText();
				
				final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(propertyExpr, ctx.getGroupId(), arguments);
				satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ASSIGNABLE, ruleCtx);
				return;//skip symbol assigned type update
			}
		}
	}
	
	/**
	 * <p>
	 * 	this validation method aims to report initialization issues which property initialization value depends on uninitialized type
	 *  this validation logic used to be separated into FieldAccessExpr, MtdInvocationExpr validators, now it's moved into Property validator
	 *  
	 *  the output is initialization successfulness, it fails on the 1st left most child expr which has an uninitialized type in use
	 *  the visit order is depth-first, visits stops on 1st error
	 * </p>
	 * @param ctx
	 * @param property
	 * @param propertyExpr
	 * @return
	 */
	private boolean validatePropertyExprInitialization(final VjoValidationCtx ctx,
			final IJstProperty property,
			final IExpr propertyExpr){
		//safe check
		if(propertyExpr == null){
			return true;
		}
		
		//cares all child expressions, depth 1st, left 1st
		final List<IExpr> subExprs = new ArrayList<IExpr>();
		for(IJstNode child: propertyExpr.getChildren()){
			if(child != null && child instanceof IExpr && !(child instanceof BaseJsCommentMetaNode<?>)){//only IExpr matters
				subExprs.add((IExpr)child);
			}
		}

		//depth-1st
		for(IExpr subExpr: subExprs){
			if(!validatePropertyExprInitialization(ctx, property, subExpr)){
				return false;
			}
		}
		
		//check if expr's value type is ready to use
		//actually, only js native types could be used here
		final IJstType propertyExprResultType = propertyExpr.getResultType();
		if(!isNative(propertyExprResultType) || propertyExprResultType == null){
		
//        if(ctx.getUninitializedTypes().contains(propertyExprResultType) || propertyExprResultType == null){
            final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(property, ctx.getGroupId(), new String[]{property.getName().getName(),
                propertyExprResultType != null ? propertyExprResultType.getName() : property.getInitializer().toExprText()});
            satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().CANNOT_USE_UNINTIALIZED_TYPE, ruleCtx);
            return false;
        }
		
		return true;
	}

	private boolean isNative(IJstType propertyExprResultType) {
		if(propertyExprResultType instanceof SynthOlType){
			return true;
		}
		if(propertyExprResultType instanceof JstArrayInitializer || propertyExprResultType instanceof JstArray){
			return true;
		}
		if(propertyExprResultType==null || propertyExprResultType.getPackage()==null){
			return false;
		}
		String groupName = propertyExprResultType.getPackage().getGroupName();

		if(groupName.equals(LibManager.JS_NATIVE_GLOBAL_LIB_NAME) || 
				groupName.equals("JsBrowserLib") ||
				groupName.equals(LibManager.JS_NATIVE_LIB_NAME)||
				groupName.equals(LibManager.VJO_SELF_DESCRIBED)||
				groupName.equals(LibManager.JAVA_PRIMITIVE_LIB_NAME)){
			return true;
		}
		
		return false;
	}

	protected void validatePropertyType(final VjoValidationCtx ctx,
			final IJstProperty jstProperty, final IJstType jstType) {
		
		final IJstType propertyJstType = jstProperty.getType();
		if(jstType.isEnum() && jstType.getEnumValues().contains(jstProperty)){
			//skipped
		}
		else if (propertyJstType instanceof IInferred) {
			return; // skip inferred type
		}
		validateComplexType(ctx, jstProperty, jstProperty.getName().getName(), propertyJstType);
	}
}

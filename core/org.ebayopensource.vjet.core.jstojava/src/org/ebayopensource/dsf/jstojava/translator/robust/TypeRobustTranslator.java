/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jstojava.translator.robust;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.ebayopensource.dsf.jst.declaration.JstCache;
import org.ebayopensource.dsf.jst.declaration.JstModifiers;
import org.ebayopensource.dsf.jst.declaration.JstSynthesizedProperty;
import org.ebayopensource.dsf.jst.declaration.JstType;
import org.ebayopensource.dsf.jst.declaration.JstVjoBaseProperty;
import org.ebayopensource.dsf.jst.declaration.JstVjoProperty;
import org.ebayopensource.dsf.jst.meta.IJsCommentMeta;
import org.ebayopensource.dsf.jst.meta.JsType;
import org.ebayopensource.dsf.jst.term.JstIdentifier;
import org.ebayopensource.dsf.jstojava.translator.JsDocHelper;
import org.ebayopensource.dsf.jstojava.translator.TranslateCtx;
import org.ebayopensource.dsf.jstojava.translator.TranslateHelper;
import org.eclipse.mod.wst.jsdt.core.ast.IExpression;
import org.eclipse.mod.wst.jsdt.core.ast.IProgramElement;
import org.eclipse.mod.wst.jsdt.internal.compiler.ast.FieldReference;
import org.eclipse.mod.wst.jsdt.internal.compiler.ast.MessageSend;
import org.ebayopensource.vjo.meta.VjoKeywords;

public class TypeRobustTranslator extends CompletionsFilteredRobustTranslator
		implements ICompletionsFilter {

	public String[] filter(String[] completions) {
		return excludeCompletions(completions);
	}

	private static final List<String> NON_REPEATABLE_KEYWORDS_LIST = new ArrayList<String>();

	private String[] excludeCompletions(String[] completions) {
		completions = excludeRepetition(completions);
		completions = excludeProcessedSingletons(completions);

		return completions;
	}

	private String[] excludeProcessedSingletons(String[] completions) {

		List<String> filtered = new ArrayList<String>();

		for (String completion : completions) {

			boolean inheritsProcessed = completion.equals(VjoKeywords.INHERITS)
					&& jst.getExtend() != null;

			boolean finalProcessed = completion.equals(VjoKeywords.MAKE_FINAL)
					&& jst.getModifiers().isFinal();

			boolean valuesProcessed = (completion.equals(VjoKeywords.VALUES)
					&& jst.isEnum()) || !completion.equals(VjoKeywords.VALUES);
			
			if (!inheritsProcessed && !finalProcessed && valuesProcessed) {
				filtered.add(completion);
			}

		}

		return filtered.toArray(new String[] {});
	}

	private String[] excludeRepetition(String[] completions) {
		IExpression receiver = null;

		List<String> filtered = new ArrayList<String>();
		filtered.addAll(Arrays.asList(completions));

		for(Iterator<IProgramElement> it = astElements.iterator(); it.hasNext() ; ) {
			IProgramElement item = it.next();
			if (item instanceof FieldReference)
				receiver = ((FieldReference)item).getReceiver();
	
			if (item instanceof MessageSend) {
				receiver = ((MessageSend) item);
	
				// check if non-reccurent keyword has already been processed
				while (receiver != null && receiver instanceof MessageSend) {
					String token = TranslateHelper.getStringToken(receiver);
					if (filtered.contains(token)
							&& NON_REPEATABLE_KEYWORDS_LIST.contains(token)) {
						filtered.remove(token);
						ALLOWED_KEYWORDS.add(token);
					}
					receiver = ((MessageSend) receiver).getReceiver();
				}
			}
		}

		return filtered.toArray(new String[] {});
	}


	static {
		NON_REPEATABLE_KEYWORDS_LIST.add(VjoKeywords.PROPS);
		NON_REPEATABLE_KEYWORDS_LIST.add(VjoKeywords.PROTOS);
		NON_REPEATABLE_KEYWORDS_LIST.add(VjoKeywords.ENDTYPE);
	}
	public TypeRobustTranslator(TranslateCtx ctx) {
		super(ctx);
		filter = this;
	}


	protected void transformType() {

		weakTranslator.getProvider().getTypeTranslator().processCType(
				(MessageSend) current, jst);
		this.weakTranslator.getCtx().setCurrentType(jst);
	}

	public boolean transform() {

		if(astElements.size()>0){
			current = astElements.pop();
		}
		
		transformType();
		
		TranslateCtx ctx = weakTranslator.getCtx();
		if(!(current instanceof MessageSend)){
			return false;
		}
		int next = ((MessageSend)current).sourceEnd; 
		if (astElements.size()>0) {
			
			IProgramElement ne = astElements.peek();
			if (ne instanceof MessageSend) {
				char[] selector = ((MessageSend)ne).selector;
				String src = ctx.getOriginalSourceAsString();
				if (selector !=null && src !=null) {
					next = src.indexOf(String.valueOf(selector),next);
				}
			}
		}
		
		
		List<IJsCommentMeta> metaArr = ctx.getCommentCollector()
				.getCommentMeta(current.sourceStart(),
						ctx.getPreviousNodeSourceEnd(), next);
		if (metaArr!=null) {
			if(metaArr.size()>0){
				JsDocHelper.addJsDoc(metaArr.get(0), ctx.getCurrentType());
			}
			JstModifiers modifiers = ctx.getCurrentType().getModifiers();
			for(IJsCommentMeta meta :  metaArr){
				JstModifiers mods = meta.getModifiers();
				if (mods.isAbstract())
					modifiers.setAbstract();
				if (mods.isFinal())
					modifiers.setFinal();
				if (mods.isPublic())
					modifiers.setPublic();
				if (mods.isPrivate())
					modifiers.setPrivate();
				if (mods.isProtected())
					modifiers.setProtected();
				if (mods.isDynamic())
					modifiers.setDynamic();
				// for inner type generic support
				if (meta.getTyping() instanceof JsType) {
					JsType jsType = (JsType) meta.getTyping();
					if (jsType.getArgs().size()>0) {
						TranslateHelper.addParamsToType(ctx, ctx.getCurrentType(), jsType);
					}
				}
				
			}
		}
		
		//Add vj$, base etc
		addVjoSynthesizedMembers(ctx.getCurrentType());
		
		// lookup possible empty completions
		lookupEmptyCompletion();

		// move to the next iteration
		return super.transform();

	}

	private void addVjoSynthesizedMembers(JstType currentType) {
		if (currentType.isOType()) {
			return;
		}
		
		boolean isInstanceInner = currentType.isEmbededType() && !currentType.getModifiers().isStatic();
		if (!isInstanceInner) {
			//Static vj$
			JstVjoProperty staticVj = new JstVjoProperty(VjoKeywords.VJ$, currentType);
			staticVj.getModifiers().setPrivate().setStatic(true).setFinal();
			staticVj.setParent(currentType);
			currentType.addProperty(staticVj); 
		}
		
		if (!currentType.isInterface()) {
			//Instance vj$
			JstVjoProperty instanceVj = 
				new JstVjoProperty(VjoKeywords.VJ$, currentType, true);
			instanceVj.getModifiers().setPrivate().setStatic(false).setFinal();
			instanceVj.setParent(currentType);
			currentType.addProperty(instanceVj);

			if (currentType.isClass()) {
				//base property
				JstVjoBaseProperty baseProp = 
					new JstVjoBaseProperty(VjoKeywords.BASE, currentType);
				baseProp.getModifiers().setPrivate().setStatic(false).setFinal();
				baseProp.setParent(currentType);
				currentType.addProperty(baseProp);

				//TODO - Do we need to filter this for instance inner?
				/*
				if (!isInstanceInner) {
					//prototype property
					JstSynthesizedProperty protoProp = 
						new JstSynthesizedProperty(JstCache.getInstance().getType("Object"), 
								"prototype", null, new JstModifiers());
					protoProp.getModifiers().setPublic().setStatic(true);
					protoProp.setParent(currentType);
					currentType.addProperty(protoProp);
				}
				*/
			}
		} else {
			JstSynthesizedProperty clazzProp = 
				new JstSynthesizedProperty( 
					JstCache.getInstance().getType("vjo.Class"), VjoKeywords.CLASS, (JstIdentifier)null, null);
			clazzProp.getModifiers().setPublic().setFinal().setStatic(true);
			clazzProp.setParent(currentType);
			currentType.addProperty(clazzProp);
		}
		
		//Add synthesized methods:
		/*
		// isInstance on Type is deprecated!
		if (!currentType.isMixin()) {
			//isInstance:
			IJstType objectType = JstCache.getInstance().getType(org.ebayopensource.dsf.jsnative.global.Object.class.getSimpleName());
			JstArg arg = new JstArg(objectType, "o", false);
			
			IJstType boolType = JstCache.getInstance().getType("boolean");
			JstSynthesizedMethod isInstanceMethod = 
				new JstSynthesizedMethod(VjoKeywords.IS_INSTANCE, new JstModifiers(), boolType, arg);
			isInstanceMethod.getModifiers()
				.setPublic().setStatic(true).setFinal();
			
			isInstanceMethod.setBlock(new JstBlock());
			isInstanceMethod.getBlock().addStmt(new RtnStmt(new SimpleLiteral(null, boolType, "true")));
			currentType.addMethod(isInstanceMethod);
		}
		*/
	}

	public static IntegerHolder getIntegerHolder(int i) {
		return new IntegerHolder(i);
	}
}

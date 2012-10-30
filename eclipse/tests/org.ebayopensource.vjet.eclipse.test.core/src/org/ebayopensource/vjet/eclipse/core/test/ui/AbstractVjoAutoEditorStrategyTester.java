/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core.test.ui;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.ui.IEditorPart;

import org.ebayopensource.vjet.eclipse.core.IJSSourceModule;
import org.ebayopensource.vjet.eclipse.core.test.contentassist.TestConstants;
import org.ebayopensource.vjet.eclipse.core.test.parser.AbstractVjoModelTests;
import org.ebayopensource.vjet.eclipse.internal.ui.text.VjoAutoEditStrategy;

public class AbstractVjoAutoEditorStrategyTester extends AbstractVjoModelTests {

	private DocumentCommand createCmd(int position, int length, String text) {
		DocumentCommand cmd = new DocumentCommand() {
		};

		cmd.offset = position;
		cmd.length = length;
		cmd.text = text;
		cmd.doit = true;
		cmd.shiftsCaret = true;
		cmd.caretOffset = -1;

		return cmd;
	}

	private DocumentCommand createNewlineCmd(int position) {
		return createCmd(position, 0, "\r\n");
	}
	
	private DocumentCommand createParentheseCmd(int position) {
		return createCmd(position, 0, "}");
	}
	
	private DocumentCommand createTabCmd(int position) {
		return createCmd(position, 0, "\t");
	}

	protected void basicTest(IJSSourceModule module, int position, String text,
			IJSSourceModule goldenModule) throws ModelException {
		this.basicTest(module, position, 0, text, goldenModule);
	}

	protected void basicTest(IJSSourceModule module, int position, int offset, String text,
			IJSSourceModule goldenModule) throws ModelException {
		VjoAutoEditStrategy strategy = new VjoAutoEditStrategy(
				IDocumentExtension3.DEFAULT_PARTITIONING, null);

		IEditorPart part = super.getEditor(module);
		DocumentCommand cmd = createCmd(position, offset, text);

		strategy
				.customizeDocumentCommand(new Document(module.getSource()), cmd);

		String newOutput = module.getSource().substring(0, cmd.offset) + cmd.text
				+ module.getSource().substring(cmd.offset + cmd.length);

		try {
			assertEquals(newOutput, goldenModule.getSource());
		} finally {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}
	}
	
	private boolean enableAutoBuild(boolean state) throws Exception{
		  IWorkspace workspace = ResourcesPlugin.getWorkspace();
	        IWorkspaceDescription desc = workspace.getDescription();
	        boolean isAutoBuilding = desc.isAutoBuilding();
	        if (isAutoBuilding != state) {
	            desc.setAutoBuilding(state);
	            workspace.setDescription(desc);
	        }
	        return isAutoBuilding;
	}
	
	
	protected void basicTest(IJSSourceModule module, int position,
			IJSSourceModule goldenModule) throws Exception {
		
		enableAutoBuild(false);
		
		
		VjoAutoEditStrategy strategy = new VjoAutoEditStrategy(
				IDocumentExtension3.DEFAULT_PARTITIONING, null);

		IEditorPart part = super.getEditor(module);
		DocumentCommand cmd = createNewlineCmd(position);

		strategy
				.customizeDocumentCommand(new Document(module.getSource()), cmd);

		String newOutput = module.getSource().substring(0, cmd.offset) + cmd.text
		+ module.getSource().substring(cmd.offset + cmd.length);

		try {
			assertEquals(goldenModule.getSource(), newOutput );
		} finally {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}
		
		enableAutoBuild(true);
		
	}
	
	protected void indentTest(IJSSourceModule module, int position,
			IJSSourceModule goldenModule) throws ModelException {
		VjoAutoEditStrategy strategy = new VjoAutoEditStrategy(
				IDocumentExtension3.DEFAULT_PARTITIONING, null);

		IEditorPart part = super.getEditor(module);
		DocumentCommand cmd = createParentheseCmd(position);

		strategy
				.customizeDocumentCommand(new Document(module.getSource()), cmd);

		String newOutput = module.getSource().substring(0, cmd.offset) + cmd.text
		+ module.getSource().substring(cmd.offset + cmd.length);

		try {
			assertEquals(newOutput, goldenModule.getSource());
		} finally {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}
	}

	protected void tabTest(IJSSourceModule module, int position,
			IJSSourceModule goldenModule) throws ModelException {
		VjoAutoEditStrategy strategy = new VjoAutoEditStrategy(
				IDocumentExtension3.DEFAULT_PARTITIONING, null);

		IEditorPart part = super.getEditor(module);
		DocumentCommand cmd = createTabCmd(position);

		strategy
				.customizeDocumentCommand(new Document(module.getSource()), cmd);

		String newOutput = module.getSource().substring(0, cmd.offset) + cmd.text
		+ module.getSource().substring(cmd.offset + cmd.length);

		try {
			assertEquals(newOutput, goldenModule.getSource());
		} finally {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}
	}
	protected String getProjectName() {
		return TestConstants.PROJECT_NAME_VJETPROJECT;
	}

}

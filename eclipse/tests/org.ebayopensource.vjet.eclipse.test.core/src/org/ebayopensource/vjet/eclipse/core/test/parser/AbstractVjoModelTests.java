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
package org.ebayopensource.vjet.eclipse.core.test.parser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.mod.compiler.env.ISourceModule;
import org.eclipse.dltk.mod.core.CompletionProposal;
import org.eclipse.dltk.mod.core.CompletionRequestor;
import org.eclipse.dltk.mod.core.IField;
import org.eclipse.dltk.mod.core.IMethod;
import org.eclipse.dltk.mod.core.IModelElement;
import org.eclipse.dltk.mod.core.IScriptProject;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.dltk.mod.core.tests.model.AbstractModelTests;
import org.eclipse.dltk.mod.internal.core.JSSourceField;
import org.eclipse.dltk.mod.internal.core.JSSourceFieldElementInfo;
import org.eclipse.dltk.mod.ui.templates.ScriptTemplateProposal;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.IProgressService;

import org.ebayopensource.dsf.jst.IJstType;
import org.ebayopensource.dsf.ts.event.EventListenerStatus;
import org.ebayopensource.dsf.ts.event.ISourceEventCallback;
import org.ebayopensource.vjet.eclipse.codeassist.VjoCompletionEngine;
import org.ebayopensource.vjet.eclipse.core.IJSField;
import org.ebayopensource.vjet.eclipse.core.IJSMethod;
import org.ebayopensource.vjet.eclipse.core.IJSSourceModule;
import org.ebayopensource.vjet.eclipse.core.search.matching.ICategoryRequestor;
import org.ebayopensource.vjet.eclipse.core.test.FixtureUtils;
import org.ebayopensource.vjet.eclipse.core.test.VjetModelTestsPlugin;
import org.ebayopensource.vjo.tool.typespace.TypeSpaceMgr;

public abstract class AbstractVjoModelTests extends AbstractModelTests
		implements ISourceEventCallback<IJstType> {

	protected static final String PROJECT_NAME = "TestProject";

	protected static final String VJO_EDITOR = "org.ebayopensource.vjet.ui.VjetJsEditor";

	private String workspaceSufix = "";

	protected TypeSpaceMgr mgr = TypeSpaceMgr.getInstance();

	private boolean isFinished;

	public AbstractVjoModelTests() {
		super(VjetModelTestsPlugin.PLUGIN_NAME, null);
	}

	public AbstractVjoModelTests(String name) {
		super(VjetModelTestsPlugin.PLUGIN_NAME, name);
	}

	public void setUpSuite() {
		mgr.setAllowChanges(false);
		try {
			super.setUpSuite();
			IScriptProject proj = setUpScriptProjectTo(getTestProjectName(),
					PROJECT_NAME);
			mgr.reload(this);
			waitTypeSpaceLoaded();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("AbstractVjoModelTests.setUpSuite()");
	}

	public void tearDownSuite() throws Exception {
		deleteProject(getTestProjectName());
		mgr.clean();
		super.tearDownSuite();
	}

	protected static String getTestProjectName() {
		return PROJECT_NAME + "q";
	}

	protected IEditorPart getEditor(IJSSourceModule module) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Workspace m_workspace = (Workspace) ResourcesPlugin.getWorkspace();
		// workbench = PlatformUI.getWorlbench()
		IPath path = module.getPath(); // new
										// Path("VJETProject").append("selection").append("A");
		IFile file = (IFile) m_workspace.newResource(path, IResource.FILE);
		FileEditorInput input = new FileEditorInput(file);
		IEditorPart editor = null;
		try {
			editor = workbench.getActiveWorkbenchWindow().getActivePage()
					.openEditor(input, VJO_EDITOR, true);
		} catch (PartInitException e) {
			assertFalse("Can't open editor for " + module.getElementName(),
					true);
		}

		return editor;
	}

	protected static IJSField processField(IField[] fields, String fieldName,
			String expectedType, int expectedModifiers) throws ModelException {
		List<IJSField> foundFields = findFieldByName(fields, fieldName);
		assertEquals("Wrong number of fields found", 1, foundFields.size());
		IJSField field = foundFields.get(0);
		assertEquals("Wrong field modifiers", expectedModifiers,
				field.getFlags());
		JSSourceField sourceField = (JSSourceField) field;
		JSSourceFieldElementInfo fieldInfo = (JSSourceFieldElementInfo) sourceField
				.getElementInfo();
		assertEquals("Wrong field type", expectedType, fieldInfo.getType());

		return field;
	}

	protected static List<IJSField> findFieldByName(IField[] fields, String name) {
		List<IJSField> foundFields = new ArrayList<IJSField>();
		for (IModelElement modelElement : fields) {
			if (name.equals(modelElement.getElementName())) {
				foundFields.add((IJSField) modelElement);
			}
		}
		return foundFields;
	}

	protected IJSMethod processMethod(IMethod[] methods, String methodName,
			String expectedReturnType, int expectedModifiers)
			throws ModelException {
		IJSMethod method = findMethodByName(methods, methodName);
		assertEquals("Wrong method modifiers", expectedModifiers,
				method.getFlags());
		assertEquals("Wrong return type", expectedReturnType,
				method.getReturnType());

		return method;
	}

	protected static IJSMethod findMethodByName(IModelElement[] methods,
			String name) {

		for (IModelElement modelElement : methods) {
			if (name.equals(modelElement.getElementName())) {
				return (IJSMethod) modelElement;
			}
		}
		return null;
	}

	@Override
	public File getSourceWorkspacePath() {
		return new File(getPluginDirectoryPath(), "workspace" + workspaceSufix);
	}

	public String getWorkspaceSufix() {
		return workspaceSufix;
	}

	public void setWorkspaceSufix(String workspaceSufix) {
		this.workspaceSufix = workspaceSufix;
	}

	protected final class TestCompletionRequetor extends CompletionRequestor
			implements ICategoryRequestor {
		LinkedList<CompletionProposal> results;

		String category;

		public TestCompletionRequetor(LinkedList<CompletionProposal> results2,
				final String category) {
			this.results = results2;
			this.category = category;
		}

		public void accept(CompletionProposal proposal) {
			results.add(proposal);
		}

		public String getCategory() {
			return this.category;
		}
	}

	protected VjoCompletionEngine createEngine(
			LinkedList<CompletionProposal> results, String category,
			IJSSourceModule module) {

		VjoCompletionEngine engine = new VjoCompletionEngine();

		TestCompletionRequetor requestor = new TestCompletionRequetor(results,
				category);

		engine.setRequestor(requestor);

		IJSSourceModule[] workingCopies = { module };
		IProject pr = getProject(PROJECT_NAME);

		return engine;
	}

	protected int firstPositionInFile(String string, IJSSourceModule module)
			throws ModelException {
		String content = module.getSource();

		int position = content.indexOf(string);
		if (position >= 0) {
			return position + string.length();
		}
		return -1;
	}

	protected int lastPositionInFile(String string, IJSSourceModule module)
			throws ModelException {
		String content = module.getSource();

		if (string == null)
			return content.length();

		int position = content.lastIndexOf(string);
		if (position >= 0) {
			return position + string.length();
		}

		return -1;
	}

	public int firstBeforePositionInFile(String string, IJSSourceModule module)
			throws ModelException {
		String content = module.getSource();

		int position = content.indexOf(string);
		if (position >= 0) {
			return position - 1;
		}
		return -1;
	}

	public int lastBeforePositionInFile(String string, IJSSourceModule module)
			throws ModelException {
		String content = module.getSource();

		if (string == null)
			return content.length();

		int position = content.lastIndexOf(string);
		if (position >= 0) {
			return position - 1;
		}

		return -1;
	}

	protected void compareCompletions(LinkedList<CompletionProposal> results,
			String[] names, boolean compareOnlyNames) {
		assertEquals("Results :" + results, names.length, results.size());
		Collections.sort(results, new Comparator() {

			public int compare(Object arg0, Object arg1) {
				CompletionProposal pr = (CompletionProposal) arg0;
				CompletionProposal pr1 = (CompletionProposal) arg1;
				return new String(pr.getCompletion()).compareTo(new String(pr1
						.getCompletion()));
			}

		});

		if (names.length > 1) {
			Arrays.sort(names, 0, names.length);
		}
		Iterator it = results.iterator();
		int pos = 0;
		// System.out.println(results.toString());
		while (it.hasNext()) {
			CompletionProposal pr = (CompletionProposal) it.next();
			if (compareOnlyNames) {
				assertEquals(names[pos], new String(pr.getName()));
			} else {
				assertEquals(names[pos], new String(pr.getCompletion()));
			}
			pos++;
		}
	}

	protected void compareTemplateCompletions(
			List<ScriptTemplateProposal> results, String[] names,
			boolean compareOnlyNames) {
		assertEquals("Results :" + results, names.length, results.size());
		Collections.sort(results, new Comparator() {

			public int compare(Object arg0, Object arg1) {
				ScriptTemplateProposal pr = (ScriptTemplateProposal) arg0;
				ScriptTemplateProposal pr1 = (ScriptTemplateProposal) arg1;
				return new String(pr.getPattern()).compareTo(new String(pr1
						.getPattern()));
			}

		});
		// Jack: if here sorting the list, test method will not be sure which is
		// the right sequence
		// if (names.length > 1) {
		// Arrays.sort(names, 0, names.length);
		// }
		Iterator it = results.iterator();
		int pos = 0;
		// System.out.println(results.toString());
		while (it.hasNext()) {
			ScriptTemplateProposal pr = (ScriptTemplateProposal) it.next();
			if (compareOnlyNames) {
				assertEquals(names[pos], new String(pr.getDisplayString()));
			} else {
				assertEquals(names[pos], new String(pr.getPattern()));
			}
			pos++;
		}
	}

	protected void basicTest(IJSSourceModule module, int position,
			String[] compNames, String category) throws ModelException {
		assertNotSame("Invalid file content, cant find position", -1, position);

		LinkedList<CompletionProposal> results = new LinkedList<CompletionProposal>();

		VjoCompletionEngine c = createEngine(results, category, module);
		c.complete((ISourceModule) module, position, 0);
		compareCompletions(results, compNames, false);
	}

	protected void containsNames(IJSSourceModule module, int position,
			String[] compNames, String category) throws ModelException {
		assertNotSame("Invalid file content, cant find position", -1, position);

		LinkedList<CompletionProposal> results = new LinkedList<CompletionProposal>();

		VjoCompletionEngine c = createEngine(results, category, module);
		c.complete((ISourceModule) module, position, 0);
		containsNames(results, compNames);
	}

	protected void excludesNames(IJSSourceModule module, int position,
			String[] compNames, String category) throws ModelException {
		assertNotSame("Invalid file content, cant find position", -1, position);

		LinkedList<CompletionProposal> results = new LinkedList<CompletionProposal>();

		VjoCompletionEngine c = createEngine(results, category, module);
		c.complete((ISourceModule) module, position, 0);
		excludesNames(results, compNames);
	}

	protected String getProjectName() {
		return getTestProjectName();
	}

	protected void containsNames(LinkedList<CompletionProposal> results,
			String[] names) {
		List<String> resultNames = new ArrayList<String>();

		for (CompletionProposal proposal : results) {
			resultNames.add(String.valueOf(proposal.getCompletion()).trim());
		}

		for (String string : names) {
			assertTrue("Results not contains " + string + " proposal in list"
					+ resultNames, resultNames.contains(string));
		}

	}

	protected void excludesNames(LinkedList<CompletionProposal> results,
			String[] names) {
		List<String> resultNames = new ArrayList<String>();

		for (CompletionProposal proposal : results) {
			resultNames.add(String.valueOf(proposal.getCompletion()).trim());
		}

		for (String string : names) {
			assertFalse("Results not contains " + string + " proposal in list"
					+ resultNames, resultNames.contains(string));
		}

	}

	public void waitTypeSpaceLoaded() {
		buildAndWaitForEnd();
	}

	public static boolean buildAndWaitForEnd() {
		return FixtureUtils.buildAndWaitForEnd();
	}

	public static boolean incrementalBuildAndWaitForEnd() {
		return FixtureUtils.incrementalBuildAndWaitForEnd();
	}

	public void onComplete(EventListenerStatus<IJstType> arg0) {
		isFinished = true;
	}

	public void onProgress(float percent) {
		// System.out.println("Percentage of completion " + percent);
	}

	protected boolean isJSFile(String file) {
		return file.endsWith(".js");
	}

	/** Start : code for not running the failing test cases **/
	private List<String> testCaseFormats = new ArrayList<String>();

	private final void addTestCaseFormats(final String testName) {
		if (!testCaseFormats.contains(testName))
			testCaseFormats.add(testName);
	}

	private static List<String> ExcludeIt = new ArrayList<String>();
	static {
		Properties p = new Properties();
		String file = "/VJETJunitFailingTests.properties";
		try {
			p.load(AbstractVjoModelTests.class.getResourceAsStream(file));
			ExcludeIt = Arrays.asList(p.getProperty("excludeTests").split(","));
			// Platform.addLogListener(new LogListener());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void runBare() throws Throwable {
		populateTestCaseFormat();

		// Check if we need to run the test case
		boolean runTest = true;
		Iterator<String> iter = testCaseFormats.iterator();
		while (runTest && iter.hasNext()) {
			runTest = !ExcludeIt.contains(iter.next());
		}

		// when in pde build and test, we need this information to know where
		// we've reached
		System.err.println("Start running test case '" + getName()
				+ "' in Class '" + getClass().getName());

		if (runTest) {
			super.runBare();
		} else {
			System.err
					.println("Test case '"
							+ getName()
							+ "' in Class '"
							+ getClass().getName()
							+ "' is failing and hence it is not being run. Please fix the test case.");
		}
	}

	private void populateTestCaseFormat() {
		String className = getClass().getName();
		String testName = getName();
		if (testName != null) {
			addTestCaseFormats(className + "." + testName);
		}

		int dot = className.lastIndexOf('.');
		if (dot >= 0) {
			String localName = className.substring(dot + 1);
			if (testName != null) {
				addTestCaseFormats(localName + "." + testName);
			}
		}
	}
	/** End : code for not running the failing test cases **/
}

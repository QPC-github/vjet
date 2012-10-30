/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *

 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.internal.launching;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.mod.core.BuildpathContainerInitializer;
import org.eclipse.dltk.mod.core.DLTKCore;
import org.eclipse.dltk.mod.core.DLTKLanguageManager;
import org.eclipse.dltk.mod.core.IAccessRule;
import org.eclipse.dltk.mod.core.IBuildpathAttribute;
import org.eclipse.dltk.mod.core.IBuildpathContainer;
import org.eclipse.dltk.mod.core.IBuildpathEntry;
import org.eclipse.dltk.mod.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.mod.core.IProjectFragment;
import org.eclipse.dltk.mod.core.IScriptProject;
import org.eclipse.dltk.mod.core.environment.EnvironmentManager;
import org.eclipse.dltk.mod.core.environment.IEnvironment;
import org.eclipse.dltk.mod.core.internal.environment.LocalEnvironment;
import org.eclipse.dltk.mod.internal.core.BuildpathEntry;
import org.eclipse.dltk.mod.internal.core.ExternalFoldersManager;
import org.eclipse.dltk.mod.internal.core.ModelManager;
import org.eclipse.dltk.mod.internal.core.ScriptProject;
import org.eclipse.dltk.mod.internal.launching.DLTKLaunchingPlugin;
import org.eclipse.dltk.mod.launching.IInterpreterInstall;
import org.eclipse.dltk.mod.launching.IInterpreterInstallType;
import org.eclipse.dltk.mod.launching.InterpreterStandin;
import org.eclipse.dltk.mod.launching.LaunchingMessages;
import org.eclipse.dltk.mod.launching.LibraryLocation;
import org.eclipse.dltk.mod.launching.ScriptLaunchConfigurationConstants;
import org.eclipse.dltk.mod.launching.ScriptRuntime;
import org.eclipse.dltk.mod.launching.ScriptRuntime.DefaultInterpreterEntry;

import org.ebayopensource.vjet.eclipse.core.VjetPlugin;
import org.ebayopensource.vjo.lib.TsLibLoader;

/**
 * Resolves a container for a InterpreterEnvironment buildpath container entry.
 */
public class VjoContainerInitializer extends BuildpathContainerInitializer {

	private static IAccessRule[] EMPTY_RULES = new IAccessRule[0];

	public void initialize(final IPath containerPath,
			final IScriptProject project) throws CoreException {
		int size = containerPath.segmentCount();
		if (size > 0) {
			if (containerPath.segment(0).equals(VjetPlugin.VJOLIB_ID)) {

				try {
					IInterpreterInstall interp = resolveInterpreter(
							getNatureFromProject(project),
							getEnvironmentFromProject(project), containerPath);
					VjoSdkBuildpathContainer container = null;

					BuildPathUtils.addLinkForGroup("VjoSelfDescribed");

					container = new VjoSdkBuildpathContainer(interp,
							containerPath);
					container.setEntries(createEntries());
					DLTKCore.setBuildpathContainer(containerPath,
							new IScriptProject[] { project },
							new IBuildpathContainer[] { container }, null);

				} catch (CoreException e) {
					// do nothing link will 
					//e.printStackTrace();
				}

			}

		}
	}

	protected List createEntries() {
		List entries = new ArrayList(1);

		/*
		 * if (!entryPath.isAbsolute()) Assert.isTrue(false, "Path for
		 * IBuildpathEntry must be absolute"); //$NON-NLS-1$
		 */
		IBuildpathAttribute[] attributes = new IBuildpathAttribute[0];
		ArrayList excluded = new ArrayList(); // paths to exclude
		IEnvironment env = LocalEnvironment.getInstance();

		entries.add(new BuildpathEntry(IProjectFragment.K_BINARY,
				IBuildpathEntry.BPE_LIBRARY, ScriptProject
						.canonicalizedPath(BuildPathUtils
								.createPathForGroup("VjoSelfDescribed")),
				false, BuildpathEntry.INCLUDE_ALL, (IPath[]) excluded
						.toArray(new IPath[excluded.size()]), EMPTY_RULES,
				false, attributes, false));

		return entries;
	}

	/**
	 * Returns the Interpreter install associated with the container path, or
	 * <code>null</code> if it does not exist.
	 * 
	 * @throws CoreException
	 *             if cannt resolve interpreter, for example no default
	 *             interpreter is specified
	 */
	public static IInterpreterInstall resolveInterpreter(String natureId,
			String environment, IPath containerPath) throws CoreException {
		if (containerPath.segmentCount() > 1) {
			String typeId = getInterpreterTypeId(containerPath);
			IInterpreterInstallType installType = ScriptRuntime
					.getInterpreterInstallType(typeId);
			if (installType != null) {
				String name = getInterpreterName(containerPath);
				return installType.findInterpreterInstallByName(name);
			}
		}
		return ScriptRuntime
				.getDefaultInterpreterInstall(new DefaultInterpreterEntry(
						natureId, environment));
	}

	/**
	 * Returns the Interpreter type identifier from the given container ID path.
	 * 
	 * @return the Interpreter type identifier from the given container ID path
	 */
	public static String getInterpreterTypeId(IPath path) {
		return path.segment(1);
	}

	/**
	 * Returns the Interpreter name from the given container ID path.
	 * 
	 * @return the Interpreter name from the given container ID path
	 */
	public static String getInterpreterName(IPath path) {
		return path.segment(2).replaceAll("%2F", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * The container can be updated if it refers to an existing Interpreter.
	 * 
	 */
	public boolean canUpdateBuildpathContainer(IPath containerPath,
			IScriptProject project) {
		if (containerPath != null && containerPath.segmentCount() > 0) {
			if (ScriptRuntime.INTERPRETER_CONTAINER.equals(containerPath
					.segment(0))) {
				try {
					return resolveInterpreter(getNatureFromProject(project),
							getEnvironmentFromProject(project), containerPath) != null;
				} catch (CoreException e) {
					return false;
				}
			}
		}
		return false;
	}

	private String getNatureFromProject(IScriptProject project) {
		IDLTKLanguageToolkit languageToolkit = DLTKLanguageManager
				.getLanguageToolkit(project);
		if (languageToolkit != null) {
			return languageToolkit.getNatureId();
		}
		return null;
	}

	private String getEnvironmentFromProject(IScriptProject project) {
		IEnvironment environment = EnvironmentManager.getEnvironment(project);
		if (environment != null) {
			return environment.getId();
		}
		return null;
	}

	public void requestBuildpathContainerUpdate(IPath containerPath,
			IScriptProject project, IBuildpathContainer containerSuggestion)
			throws CoreException {
		IInterpreterInstall interpreter = resolveInterpreter(
				getNatureFromProject(project),
				getEnvironmentFromProject(project), containerPath);
		if (interpreter == null) {
			IStatus status = new Status(
					IStatus.ERROR,
					DLTKLaunchingPlugin.getUniqueIdentifier(),
					ScriptLaunchConfigurationConstants.ERR_INTERPRETER_INSTALL_DOES_NOT_EXIST,
					MessageFormat
							.format(LaunchingMessages.InterpreterEnvironmentContainerInitializer_InterpreterEnvironment_referenced_by_classpath_container__0__does_not_exist__1,
									new String[] { containerPath.toString() }),
					null);
			throw new CoreException(status);
		}
		// update of the interpreter with new library locations
		IBuildpathEntry[] entries = containerSuggestion
				.getBuildpathEntries(project);
		LibraryLocation[] libs = new LibraryLocation[entries.length];
		for (int i = 0; i < entries.length; i++) {
			IBuildpathEntry entry = entries[i];
			if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
				IPath path = entry.getPath();
				File lib = path.toFile();
				if (lib.exists()) {
					libs[i] = new LibraryLocation(path);
				} else {
					IStatus status = new Status(
							IStatus.ERROR,
							DLTKLaunchingPlugin.getUniqueIdentifier(),
							ScriptLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
							MessageFormat
									.format(LaunchingMessages.InterpreterEnvironmentContainerInitializer_Buildpath_entry__0__does_not_refer_to_an_existing_library__2,
											new String[] { entry.getPath()
													.toString() }), null);
					throw new CoreException(status);
				}
			} else {
				IStatus status = new Status(
						IStatus.ERROR,
						DLTKLaunchingPlugin.getUniqueIdentifier(),
						ScriptLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
						MessageFormat
								.format(LaunchingMessages.InterpreterEnvironmentContainerInitializer_Buildpath_entry__0__does_not_refer_to_a_library__3,
										new String[] { entry.getPath()
												.toString() }), null);
				throw new CoreException(status);
			}
		}
		InterpreterStandin standin = new InterpreterStandin(interpreter);
		standin.setLibraryLocations(libs);
		standin.convertToRealInterpreter();
		ScriptRuntime.saveInterpreterConfiguration();
	}

	public String getDescription(IPath containerPath, IScriptProject project) {
		String tag = null;
		if (containerPath.segmentCount() > 2) {
			tag = getInterpreterName(containerPath);
		}
		if (tag != null) {
			return MessageFormat
					.format(LaunchingMessages.InterpreterEnvironmentContainer_InterpreterEnvironment_System_Library_1,
							new String[] { tag });
		}
		return LaunchingMessages.InterpreterEnvironmentContainerInitializer_Default_System_Library_1;
	}
}

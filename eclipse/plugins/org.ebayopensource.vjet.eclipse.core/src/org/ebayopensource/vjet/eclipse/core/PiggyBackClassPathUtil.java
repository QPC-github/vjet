/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.ebayopensource.dsf.jst.ts.util.ISdkEnvironment;
import org.ebayopensource.vjet.eclipse.codeassist.CodeassistUtils;
import org.ebayopensource.vjet.eclipse.core.sdk.VJetSdkEnvironment;
import org.ebayopensource.vjo.tool.typespace.SourcePathInfo;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.dltk.mod.core.DLTKCore;
import org.eclipse.dltk.mod.core.IBuildpathEntry;
import org.eclipse.dltk.mod.core.IScriptProject;
import org.eclipse.dltk.mod.core.ModelException;
import org.eclipse.dltk.mod.core.environment.EnvironmentPathUtils;
import org.eclipse.dltk.mod.core.environment.IEnvironment;
import org.eclipse.dltk.mod.core.internal.environment.LocalEnvironment;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.service.prefs.BackingStoreException;

public class PiggyBackClassPathUtil {
	public static final String PREFERENCE_SCRIPTPROJECT_INITIALIZED = "initialized_project_from_v4classpath";
	private static List<String> noneVjoFileList = new ArrayList<String>();
	private static List<String> vjoFileList = new ArrayList<String>();

	public static List<URL> getProjectDependencyUrls_bak(
			IJavaProject javaProject) throws JavaModelException,
			MalformedURLException {
		URL[] urls = getProjectDependencyUrlsInternal(javaProject);
		List<URL> urlsString = new ArrayList<URL>(urls.length);
		for (URL u : urls) {
			urlsString.add(u);
		}
		return urlsString;
	}

	private static URL[] getProjectDependencyUrlsInternal(
			IJavaProject javaProject) throws JavaModelException,
			MalformedURLException {
		List<IJavaProject> jps = new ArrayList<IJavaProject>(1);
		jps.add(javaProject);

		return getProjectDependencyUrls_bak(jps, null,
				new HashMap<String, String>());
	}

	public static URL[] getProjectDependencyUrls_bak(
			List<IJavaProject> javaProjectList, List<URL> currentUrlList,
			HashMap<String, String> projectMap) throws JavaModelException,
			MalformedURLException {

		List<URL> projectDependencyUrlList = currentUrlList;

		if (projectDependencyUrlList == null) {
			projectDependencyUrlList = new ArrayList<URL>();
		}

		for (IJavaProject project : javaProjectList) {

			if (projectMap.containsKey(project.getElementName()) == true) {
				continue;
			} else {
				projectMap.put(project.getElementName(), project
						.getElementName());
			}

			// Add the dependencies to the URL list
			// IClasspathEntry[] entries;
			// entries = project.getResolvedClasspath(true);

			IClasspathEntry[] entries2;
			entries2 = project.getRawClasspath();

			for (IClasspathEntry entry : entries2) {

				IPath path = entry.getPath();
				File f = path.toFile();
				URL entryUrl;
				entryUrl = f.toURL();
				switch (entry.getEntryKind()) {

				case IClasspathEntry.CPE_VARIABLE:
				case IClasspathEntry.CPE_LIBRARY:
					addEntryToList(entry, projectDependencyUrlList);
				case IClasspathEntry.CPE_PROJECT:
					List<IJavaProject> subjavaProjectList = new ArrayList<IJavaProject>();
					IResource subResource = ResourcesPlugin.getWorkspace()
							.getRoot().findMember(entry.getPath());
					if (subResource == null) {
						String projectName = entry.getPath().toString();
						String parentProjectName = project.getElementName();
					}
					if (subResource != null
							&& subResource.getType() == IResource.PROJECT) {
						IProject subProject = (IProject) subResource;
						IJavaProject subJavaProject = JavaCore
								.create(subProject);
						if (subJavaProject != null && subJavaProject.exists()) {
							subjavaProjectList.add(subJavaProject);

							// Recursively call our selves to populate the
							// project
							// dependency's for the sub projects.
							getProjectDependencyUrls_bak(subjavaProjectList,
									projectDependencyUrlList, projectMap);
						}

					}
					break;
				case IClasspathEntry.CPE_CONTAINER:
					if (!JavaRuntime.JRE_CONTAINER.equals(path.toOSString())) {
						IClasspathContainer container = JavaCore
								.getClasspathContainer(entry.getPath(), project);
						if (container != null) {
							IClasspathEntry[] entries = container
									.getClasspathEntries();
							for (int i = 0; i < entries.length; i++) {
								addEntryToList(entries[i],
										projectDependencyUrlList);
							}
						}
					}
					break;
				default:
					break;
				}
			}
			//
			// IPath path = project.getOutputLocation();
			// IPath projectResourceLocation =
			// project.getResource().getLocation();
			// File projectFilePath = projectResourceLocation.append(
			// path.removeFirstSegments(1)).toFile();
			// URL projectOutputUrl;
			// projectOutputUrl = projectFilePath.toURL();
			//
			// if (projectDependencyUrlList.contains(projectOutputUrl) == false)
			// {
			// projectDependencyUrlList.add(projectOutputUrl);
			// }
		}

		URL[] arrayList = new URL[projectDependencyUrlList.size()];
		URL[] returnURLArray = projectDependencyUrlList.toArray(arrayList);

		return returnURLArray;

	}

	private static void addEntryToList(IClasspathEntry entry,
			List<URL> projectDependencyUrlList) {
		try {
			IClasspathEntry resolvedEntry = JavaCore
					.getResolvedClasspathEntry(entry);
			if (resolvedEntry != null) {
				File f = resolvedEntry.getPath().toFile();
				if (!f.exists() || !isVjoJar(f)) {
					return;
				}
				URL entryUrl = f.toURL();
				if (!projectDependencyUrlList.contains(entryUrl)) {
					projectDependencyUrlList.add(entryUrl);
				}
			}
		} catch (AssertionFailedException e) {
			// Catch the assertion failure
			// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55992
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	private static IBuildpathEntry[] getResolvedBuildpath(
			IScriptProject vProject) {
		try {
			return vProject.getResolvedBuildpath(true);
		} catch (ModelException e) {
			VjetPlugin.error("Failed to get build path from <"
					+ vProject.getElementName() + ">", e, IStatus.WARNING);
			return new IBuildpathEntry[0];
		}

	}

	public static SourcePathInfo getProjectSrcPath_DLTK(IScriptProject vProject) {
		SourcePathInfo srcPaths = new SourcePathInfo();
		String name = vProject.getProject().getName();
		IBuildpathEntry[] entries = getResolvedBuildpath(vProject);

		for (IBuildpathEntry entry : entries) {
			if (entry.getEntryKind() != IBuildpathEntry.BPE_SOURCE) {
				continue;
			}
			
			for(IPath inclusion : entry.getInclusionPatterns()){
				srcPaths.addInclusionRule(inclusion.toPortableString());
			}
			for(IPath exclusion : entry.getExclusionPatterns()){
				srcPaths.addExclusionRule(exclusion.toPortableString());
			}
			
			String portableString = entry.getPath().toPortableString();

			if (portableString.lastIndexOf(name) != -1) {
				if (portableString.equals(name)) {
					portableString = "";
				} else {
					portableString = portableString.substring(portableString
							.indexOf(name)
							+ name.length());

				}
			}
			srcPaths.addSourcePath(portableString);
		}
		return srcPaths;
	}

	public static List<URL> getProjectDependantJars_DLTK(IScriptProject vProject) {
		IBuildpathEntry[] entries = getResolvedBuildpath(vProject);
		List<URL> urlsString = new ArrayList<URL>(entries.length);
		for (int i = 0; i < entries.length; i++) {
			IBuildpathEntry entry = entries[i];
			if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
				IPath path = entry.getPath();
			
				File file = getFile(path);
				if (file!=null && file.exists() && file.isFile()) {
					URL url = getURL(file);
					if (url != null) {
						urlsString.add(url);
					}
				}
			}
		}
		return urlsString;
	}
	
	/**
	 * @param path
	 * @return
	 */
	private static File getFile(IPath path) {
		path = EnvironmentPathUtils.getLocalPath(path);
		File file = path.toFile();
		if (!file.exists()) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource member = root.findMember(path);
			if (member != null && member.exists() && member.getLocation()!=null) {
				file = member.getLocation().toFile();
			}else if(member != null && member.exists() && member.getLocation()==null){
				if(member.isLinked()){
					return null;
				}
						
			}
			
			
		}
		return file;
	}

	private static URL getURL(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void addEntryToVJETEntry(IClasspathEntry entry,
			List<IBuildpathEntry> vEntries, List<String> duplicateChecker) {
		try {
			IClasspathEntry resolvedEntry = JavaCore
					.getResolvedClasspathEntry(entry);
			if (resolvedEntry != null) {
				File f = resolvedEntry.getPath().toFile();
				IFile ifile = null;
				if (!f.exists()) {
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					ifile = workspace.getRoot().getFile(resolvedEntry.getPath());
					if (ifile.exists()) {
						f = ifile.getLocation().toFile();
					}
					if (!f.exists()) {
						return;
					}
				}
				String s = f.toString();
				if (duplicateChecker.contains(s)) {
					return;
				} else {
					duplicateChecker.add(s);
				}
				IBuildpathEntry eEntry = null;
				if (!isVjoJar(f)) {
					// The file is not a vjo file
					return;
				}
				if (ifile != null) {
					eEntry = DLTKCore
					.newLibraryEntry(resolvedEntry.getPath());
				} else {
					IEnvironment env = LocalEnvironment.getInstance();
					 eEntry = DLTKCore
					.newExtLibraryEntry(EnvironmentPathUtils.getFullPath(
							env, new Path(f.toURI().toURL().getFile())));
				}
				// IBuildpathEntry eEntry = DLTKCore.newLibraryEntry(new
				// Path(LocalEnvironment.ENVIRONMENT_ID, "/" + s), true, true);
				vEntries.add(eEntry);
			}
		} catch (Exception e) {
			// Catch the assertion failure
			// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55992
			e.printStackTrace();
		}

	}

	public static boolean isVjoJar(File libFile) {
		String toString = libFile.toString();
		if (noneVjoFileList.contains(toString)) {
			return false;
		} else if (vjoFileList.contains(toString)) {
			return true;
		}
		String libFileName = libFile.getName().toLowerCase();
		if (CodeassistUtils.isBinaryPath(libFileName)) {
			ZipFile jarFile = null;
			try {
				jarFile = new ZipFile(libFile);
				Enumeration<? extends ZipEntry> enumeration = jarFile.entries();

				while (enumeration.hasMoreElements()) {

					ZipEntry elem = enumeration.nextElement();

					if (!elem.isDirectory()) {
						String typeName = elem.getName();
						if (CodeassistUtils.isVjetFileName(typeName)) {
							vjoFileList.add(toString);
							return true;
						}
					}
				}
			} catch (IOException e) {

			} finally {
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		noneVjoFileList.add(toString);
		return false;
	}

	/**
	 * Check if the script project has been initialized from java project
	 * 
	 * @see org.ebayopensource.vjet.eclipse.core.PiggyBackClassPathUtil#initializeScriptProjectFromJavProject(IScriptProject
	 *      project)
	 * @param project
	 * @return
	 */
	public static boolean ifScriptProjectInitializedFromJavaProject(
			IScriptProject vProject) {
		IProject project = vProject.getProject();
		IEclipsePreferences prefs = getProjectPreference(project,
				VjetPlugin.PLUGIN_ID);
		if (prefs == null) {
			return false;
		}
		return prefs.getBoolean(PREFERENCE_SCRIPTPROJECT_INITIALIZED, false);
	}

	private static IEclipsePreferences getProjectPreference(IProject project,
			String pluginId) {
		IScopeContext projectContext = new ProjectScope(project);
		// if (!projectContext.getLocation().toFile().exists()) {
		// return null;
		// }
		IEclipsePreferences prefs = projectContext
				.getNode(VjetPlugin.PLUGIN_ID);
		return prefs;
	}

	/**
	 * Way to initialize ScriptProject from Java. "run setup option" in EDE only
	 * generates java build path. VJET need analyze Java class path and build up
	 * it own path, in this way, VJET can kick off many none DLTK related paths.
	 * 
	 * @param project
	 */
	public static void initializeScriptProjectFromJavProject(
			IScriptProject project) {
		IJavaProject javaProject = JavaCore.create(project.getProject());
		if (!javaProject.exists()) {
			return;
		}
		List<IBuildpathEntry> vEntries = fetchBuildEntryFromJavaProject(javaProject);
		savaVJetEntries(vEntries, project);

	}

	public static List<IBuildpathEntry> fetchBuildEntryFromJavaProject(
			IJavaProject javaProject) {
		// Project entry
		List<IBuildpathEntry> vEntries = new ArrayList<IBuildpathEntry>();
		List<String> duplicateChecker = new ArrayList<String>();
		IClasspathEntry[] entries2;
		try {
			entries2 = javaProject.getRawClasspath();
		} catch (JavaModelException e) {
			VjetPlugin.error("Failed to resolve Java prjoect classpath: "
					+ javaProject.getElementName(), e, IStatus.WARNING);
			return Collections.emptyList();
		}

		for (IClasspathEntry entry : entries2) {

			IPath path = entry.getPath();
			String sPath = path.toString();
			switch (entry.getEntryKind()) {

			case IClasspathEntry.CPE_VARIABLE:
			case IClasspathEntry.CPE_LIBRARY:
				addEntryToVJETEntry(entry, vEntries, duplicateChecker);
			case IClasspathEntry.CPE_PROJECT:
				IResource subResource = ResourcesPlugin.getWorkspace()
						.getRoot().findMember(entry.getPath());
				if (subResource != null
						&& subResource.getType() == IResource.PROJECT) {
					addProjectEntry(entry.getPath(), vEntries, duplicateChecker);
				}
				break;
			case IClasspathEntry.CPE_CONTAINER:
				if (!JavaRuntime.JRE_CONTAINER.equals(path.segment(0))) {

					IClasspathContainer container;
					try {
						container = JavaCore.getClasspathContainer(path,
								javaProject);
						if (container != null) {
							IClasspathEntry[] entries = container
									.getClasspathEntries();
							for (int i = 0; i < entries.length; i++) {
								addEntryToVJETEntry(entries[i], vEntries,
										duplicateChecker);
							}
						}
					} catch (JavaModelException e) {
						VjetPlugin.error(
								"Failed to resolve Java classpath container: "
										+ sPath, e, IStatus.WARNING);
					}
				}
				break;
			default:
				break;
			}
		}
		return vEntries;
	}

	/**
	 * TODO, save entries into .buildpath Save new entry into Vjet project
	 * 
	 * @param entries
	 * @param project
	 */
	private static void savaVJetEntries(List<IBuildpathEntry> entries,
			IScriptProject project) {
		try {
			IBuildpathEntry[] oEntries = project.getRawBuildpath();
			for (int i = 0; i < oEntries.length; i++) {
				if (oEntries[i].getEntryKind() != IBuildpathEntry.BPE_LIBRARY
						&& oEntries[i].getEntryKind() != IBuildpathEntry.BPE_PROJECT) {
					entries.add(0, oEntries[i]);
				}
			}
			project.setRawBuildpath(entries.toArray(new IBuildpathEntry[] {}),
					null);
			setProjectInitialized(project.getProject());
		} catch (ModelException e) {
			e.printStackTrace();
			VjetPlugin.getDefault().error("Failed to update project build path.", e);
			//Do nothing and skip
		}
	}

	/**
	 * Set that the VJet project has been initialized from java project
	 * 
	 * @param vProject
	 */
	public static void setProjectInitialized(IProject project) {
		IEclipsePreferences prefs = getProjectPreference(project,
				VjetPlugin.PLUGIN_ID);
		if (prefs == null) {
			return;
		}
		prefs.putBoolean(PREFERENCE_SCRIPTPROJECT_INITIALIZED, true);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			VjetPlugin.error("Failed to save preference for "
					+ project.getName(), e, IStatus.WARNING);
		}
	}

	/**
	 * Add project dependency as entry
	 * 
	 * @param path
	 * @param vEntries
	 * @param duplicateChecker
	 */
	private static void addProjectEntry(IPath path,
			List<IBuildpathEntry> vEntries, List<String> duplicateChecker) {
		String str = path.toString();
		if (duplicateChecker.contains(str)) {
			// exists, do nothing
			return;
		} else {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(str);
			IScriptProject vProject = getScriptProject(project);
			if (project.exists() && vProject.exists()) {
				duplicateChecker.add(str);
				IBuildpathEntry pEntry = DLTKCore.newProjectEntry(path, true);
				vEntries.add(pEntry);
			}
		}
	}

	private static IScriptProject getScriptProject(IProject project) {
		IScriptProject vProject = DLTKCore.create(project);
		return vProject;
	}

	/*public static boolean ifContainSourceFolder(IResource resource) {
		IScriptProject vProject = getScriptProject(resource.getProject());
		List<String> list = getProjectSrcPath_DLTK(vProject);

		if (list != null) {
			for (String path : list) {

				// check that
                if (resource.getProjectRelativePath().toOSString().startsWith(path.substring(1))) {
					return true;
				}
			}
		}
		return false;
	}*/

	/**
	 * Returns true if file is in source folder.
	 * 
	 * @param file
	 *            {@link IFile} object.
	 * @return true if file is in source folder.
	 */
	public static boolean isInSourceFolder(IResource resource) {
		List<String> list = PiggyBackClassPathUtil
				.getProjectSrcPath_DLTK(getScriptProject(resource.getProject())).getSourcePaths();

		boolean isInSourceFolder = false;

		if (list != null) {
			for (String path : list) {
				if(path == null || path.equalsIgnoreCase(""))
					continue;

				// check that
                if (resource.getProjectRelativePath().toOSString().startsWith(path.substring(1))) {
					isInSourceFolder = true;
					break;
				}
			}
		}
		return isInSourceFolder;
	}

	public static ISdkEnvironment getSdkEnvironment() {
		return new VJetSdkEnvironment(new String[0], "DefaultSdk");
	}

}

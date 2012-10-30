package org.ebayopensource.vjet.eclipse.internal.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.mod.core.BuildpathContainerInitializer;
import org.eclipse.dltk.mod.core.DLTKCore;
import org.eclipse.dltk.mod.core.IAccessRule;
import org.eclipse.dltk.mod.core.IBuildpathAttribute;
import org.eclipse.dltk.mod.core.IBuildpathContainer;
import org.eclipse.dltk.mod.core.IBuildpathEntry;
import org.eclipse.dltk.mod.core.IProjectFragment;
import org.eclipse.dltk.mod.core.IScriptProject;
import org.eclipse.dltk.mod.internal.core.BuildpathEntry;
import org.eclipse.dltk.mod.internal.core.ScriptProject;

public class VJETZipContainerInitializer extends BuildpathContainerInitializer {

	private static IAccessRule[] EMPTY_RULES = new IAccessRule[0];

	public void initialize(final IPath containerPath,
			final IScriptProject project) throws CoreException {

		IFile getZipFile = project.getProject().getFile(
				containerPath.lastSegment());
		// group name
		String groupName = getZipFile.getFullPath().lastSegment();
		IFile zipfile = project.getProject().getFile(groupName);
		// need to add type before referencing... chicken and egg
		// issue
		// need to have manifest of types to prevent this issue
		// requires zip file to contain types.txt inside
		BuildPathUtils.initGroupWithTypeList(groupName, zipfile);

		BuildPathUtils.addLinkForGroup(groupName, groupName);
		// System.out.println(containerPath);
		// System.out.println(project);

		VJETZipContainer container = new VJETZipContainer(groupName,
				zipfile.getFullPath());
		container
				.setEntries(createEntries(groupName, getZipFile.getFullPath()));
		DLTKCore.setBuildpathContainer(containerPath,
				new IScriptProject[] { project },
				new IBuildpathContainer[] { container }, null);

	}

	private List createEntries(String groupName, IPath containerPath) {
		List entries = new ArrayList(1);

		IBuildpathAttribute[] attributes = new IBuildpathAttribute[0];
		ArrayList excluded = new ArrayList(); // paths to exclude
		// TODO add zip to build path rather than require seperate entry
		// TODO add to type space
		entries.add(new BuildpathEntry(IProjectFragment.K_BINARY,
				IBuildpathEntry.BPE_LIBRARY, ScriptProject
						.canonicalizedPath(containerPath), false,
				BuildpathEntry.INCLUDE_ALL, (IPath[]) excluded
						.toArray(new IPath[excluded.size()]), EMPTY_RULES,
				false, attributes, false));
		entries.add(new BuildpathEntry(IProjectFragment.K_BINARY,
				IBuildpathEntry.BPE_LIBRARY, ScriptProject
						.canonicalizedPath(BuildPathUtils
								.createPathForGroup(groupName)), false,
				BuildpathEntry.INCLUDE_ALL, (IPath[]) excluded
						.toArray(new IPath[excluded.size()]), EMPTY_RULES,
				false, attributes, false));

		return entries;

	}

}

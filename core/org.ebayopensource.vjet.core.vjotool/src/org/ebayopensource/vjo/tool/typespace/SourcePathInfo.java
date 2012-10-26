package org.ebayopensource.vjo.tool.typespace;

import java.util.ArrayList;
import java.util.List;

public class SourcePathInfo {

	private List<String> sourcePaths = new ArrayList<String>();
	private List<String> inclusionRules = null;
	private List<String> exclusionRules = null;

	public List<String> getSourcePaths() {
		return sourcePaths;
	}

	public void setSourcePaths(List<String> sourcePaths) {
		this.sourcePaths = sourcePaths;
	}

	public void addSourcePath(String path) {
		sourcePaths.add(path);
	}

	public List<String> getInclusionRules() {
		if(inclusionRules==null){
			inclusionRules = new ArrayList<String>();
		}
		return inclusionRules;
	}

	public void setInclusionRules(List<String> inclusionRules) {
		this.inclusionRules = inclusionRules;
	}

	public void addInclusionRule(String pattern) {
		getInclusionRules().add(pattern);
	}

	public List<String> getExclusionRules() {
		if(exclusionRules==null){
			exclusionRules = new ArrayList<String>();
		}
		return exclusionRules;
	}

	public void setExclusionRules(List<String> exclusionRules) {
		this.exclusionRules = exclusionRules;
	}

	public void addExclusionRule(String exclusionRule) {
		getExclusionRules().add(exclusionRule);
	}

}

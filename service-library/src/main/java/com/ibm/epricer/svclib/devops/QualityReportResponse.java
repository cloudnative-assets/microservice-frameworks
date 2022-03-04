package com.ibm.epricer.svclib.devops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QualityReportResponse {

	private ProjectStatus projectStatus;

	public ProjectStatus getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(ProjectStatus projectStatus) {
		this.projectStatus = projectStatus;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("QualityReportResponse [projectStatus=").append(projectStatus).append("]");
		return builder.toString();
	}

}

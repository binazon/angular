package com.orange.srs.refreport.model.TO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import com.orange.srs.refreport.model.ReportingGroup;

public class ReportUserTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8668402926370709773L;
	@XmlTransient
	private Long reportUserPk;

	@XmlElementWrapper(name = "workingGroupList")
	@XmlElement(name = "workingGroup")
	private List<ReportingGroup> workingGroups = new ArrayList<ReportingGroup>();

	public ReportUserTO() {
		super();
	}

	public ReportUserTO(Long reportUserPk, List<ReportingGroup> workingGroupList) {
		this.reportUserPk = reportUserPk;
		this.workingGroups = workingGroupList;

	}

	public Long getReportUserPk() {
		return reportUserPk;
	}

	public void setReportUserPk(Long reportUserPk) {
		this.reportUserPk = reportUserPk;
	}

	public List<ReportingGroup> getWorkingGroups() {
		return workingGroups;
	}

	public void setWorkingGroups(List<ReportingGroup> workingGroups) {
		this.workingGroups = workingGroups;
	}

}

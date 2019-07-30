package com.orange.srs.refreport.model.TO;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "reportingGroupList")
public class ReportingGroupTOList {

	@XmlElement(name = "reportingGroup")
	public List<ReportingGroupTO> reportingGroupTOs = new ArrayList<>();
}

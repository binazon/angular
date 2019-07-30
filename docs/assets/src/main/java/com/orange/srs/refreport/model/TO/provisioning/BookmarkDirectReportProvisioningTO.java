package com.orange.srs.refreport.model.TO.provisioning;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlRootElement(name = "bookmarkDirectReport")
public class BookmarkDirectReportProvisioningTO {

	@XmlAttribute(required = true)
	public String label;
	@XmlElement(required = true)
	public String indicatorId;
	@XmlElement(required = true)
	public String offerOptionAlias;
	@XmlElement(required = true)
	public String paramTypeAlias;
	@XmlElement(required = false)
	public String additionalParamTypeAlias;
	@XmlElement(name = "hierarchy", required = false)
	public HierarchyProvisioningTO hierarchy;

	@XmlAttribute(required = false)
	public Boolean suppress;

	public BookmarkDirectReportProvisioningTO() {
	}

	public BookmarkDirectReportProvisioningTO(String label, String indicatorId, String offerOptionAlias,
			String paramTypeAlias, String additionalParamTypeAlias, String hierarchy) {
		this.label = label;
		this.indicatorId = indicatorId;
		this.offerOptionAlias = offerOptionAlias;
		this.paramTypeAlias = paramTypeAlias;
		this.additionalParamTypeAlias = additionalParamTypeAlias;
		String[] hierarchyMinAndMax = StringUtils.splitPreserveAllTokens(hierarchy, ';');
		this.hierarchy = new HierarchyProvisioningTO(hierarchyMinAndMax[0], hierarchyMinAndMax[1]);
	}
}

package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listBookmarkDirectReport")
public class BookmarkDirectReportListProvisioningTO {

	@XmlElement(name = "bookmarkDirectReport")
	public List<BookmarkDirectReportProvisioningTO> bookmarkDirectReportProvisioningTOs = new ArrayList<BookmarkDirectReportProvisioningTO>();
}

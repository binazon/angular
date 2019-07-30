package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.enums.FilterTypeEnum;

@XmlRootElement(name = "filter")
public class FilterProvisioningTO {

	@XmlAttribute(required = true)
	public String id;
	@XmlElement(required = true)
	public String uri;
	@XmlElement(required = true)
	public String type;
	@XmlElement(required = true)
	public String name;
	@XmlElement(required = true)
	public String comments;
	@XmlElement(required = true)
	public String value;

	@XmlAttribute(required = false)
	public Boolean suppress;

	@XmlElementWrapper(name = "listOfferOption")
	@XmlElement(name = "offerOption")
	public List<OfferOptionLinkProvisioningTO> offerOptionAliasProvisioningTOs = new ArrayList<>();

	public FilterProvisioningTO() {
	}

	public FilterProvisioningTO(String id, String uri, FilterTypeEnum type, String name, String comments,
			String value) {
		this.id = id;
		this.uri = uri;
		this.type = type.getValue();
		this.name = name;
		this.comments = comments;
		this.value = value;
	}
}

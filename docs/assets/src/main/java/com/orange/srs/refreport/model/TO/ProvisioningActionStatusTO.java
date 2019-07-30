package com.orange.srs.refreport.model.TO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ProvisioningActionStatusTO implements Serializable {

	private static final long serialVersionUID = -4483872523394936822L;

	@XmlAttribute(name = "name")
	public String name;
	@XmlAttribute(name = "duration")
	public Long duration;
	@XmlAttribute(name = "error")
	public Boolean error = false;
	@XmlAttribute(name = "comment")
	public String comment;

	@XmlElement(name = "file")
	public String file;

	@XmlElementWrapper(name = "infos")
	@XmlElement(name = "info")
	public List<InfoStatusTO> infos = new ArrayList<>();

	public ProvisioningActionStatusTO() {
		super();
	}

	public ProvisioningActionStatusTO(String name) {
		super();
		this.name = name;
	}

	public void addInfo(String name, Integer value) {
		infos.add(new InfoStatusTO(name, value));
	}

	public void copyInfosFrom(ProvisioningActionStatusTO to) {
		infos.addAll(to.infos);
	}
}

package com.orange.srs.refreport.model.TO.provisioning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.refreport.model.TO.ProvisioningActionStatusTO;

@XmlRootElement
public class ProvisioningStatusTO implements Serializable {

	private static final long serialVersionUID = 7463524785938474802L;

	@XmlAttribute(name = "name")
	public String name;
	@XmlAttribute(name = "duration")
	public long duration = 0;

	@XmlElement(name = "action")
	public List<ProvisioningActionStatusTO> actions = new ArrayList<>();

	public ProvisioningStatusTO() {
		super();
	}

	public ProvisioningStatusTO(String name) {
		super();
		this.name = name;
	}

	public void addAction(ProvisioningActionStatusTO action) {
		actions.add(action);
	}
}

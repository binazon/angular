package com.orange.srs.refreport.model.TO;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AttributesProcessStatus")
public class AttributesProcessStatusTOList {
	@XmlElementWrapper(name = "updated")
	@XmlElement(name = "attribute")
	public List<AttributeProcessStatusTO> updatedAttributeTOs = new ArrayList<AttributeProcessStatusTO>();
	@XmlElementWrapper(name = "errors")
	@XmlElement(name = "attribute")
	public List<AttributeProcessStatusTO> errorAttributeTOs = new ArrayList<AttributeProcessStatusTO>();
	@XmlElementWrapper(name = "created")
	@XmlElement(name = "attribute")
	public List<AttributeProcessStatusTO> createdAttributeTOs = new ArrayList<AttributeProcessStatusTO>();
}

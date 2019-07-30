package com.orange.srs.refreport.model.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "groupEntityAttributeList")
public class GroupEntityAttributeParameterList implements Serializable {

	private static final long serialVersionUID = -1842725090114755213L;

	@XmlElement(name = "groupEntityAttribute")
	public List<GroupEntityAttributeParameter> groupEntityAttributes = new ArrayList<GroupEntityAttributeParameter>();
}

package com.orange.srs.refreport.model.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.parameter.GroupAttributeParameterList;

@XmlRootElement(name = "groupAttributesParameter")
public class GroupAttributesParameter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3831062417099809298L;

	@XmlElement(name = "groupAttributeList")
	public List<GroupAttributeParameterList> groupAttributeLists = new ArrayList<GroupAttributeParameterList>();

}

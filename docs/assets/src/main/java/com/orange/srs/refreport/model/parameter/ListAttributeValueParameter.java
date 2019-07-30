package com.orange.srs.refreport.model.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "attribut")
public class ListAttributeValueParameter implements Serializable {

	private static final long serialVersionUID = -2455949518983470876L;

	public List<ListAttributeElementParameter> element;

	public ListAttributeValueParameter() {
		element = new ArrayList<ListAttributeElementParameter>();
	}

	public void addElement(ListAttributeElementParameter elt) {
		element.add(elt);
	}
}

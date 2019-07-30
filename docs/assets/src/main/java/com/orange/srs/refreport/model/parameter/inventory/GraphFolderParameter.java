package com.orange.srs.refreport.model.parameter.inventory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.parameter.SOAParameter;

@XmlRootElement
public class GraphFolderParameter extends SOAParameter {
	@XmlElement
	public String graphFolder;
}

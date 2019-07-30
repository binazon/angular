package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.TO.InputSourceProxyProvisioningTO;

@XmlRootElement(name = "listSourceProxy")
public class SourceProxyListProvisioningTO {

	@XmlElement(name = "inputSource")
	public List<InputSourceProxyProvisioningTO> inputSourceProxyProvisioningTOs = new ArrayList<InputSourceProxyProvisioningTO>();
}

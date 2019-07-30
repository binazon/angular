package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listProxy")
public class ProxyListProvisioningTO {

	@XmlElement(name = "proxy")
	public List<ProxyProvisioningTO> proxyProvisioningTOs = new ArrayList<ProxyProvisioningTO>();
}

package com.orange.srs.refreport.model.TO.provisioning;

import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.refreport.technical.FSTSerializer;
import com.orange.srs.statcommon.model.TO.report.ProxyTO;

@XmlRootElement(name = "proxy")
public class ProxyProvisioningTO extends ProxyTO {

	private static final long serialVersionUID = -500127150972573756L;

	@XmlAttribute(required = false)
	public Boolean suppress;

	public ProxyProvisioningTO() {
	}

	public ProxyProvisioningTO(byte[] uri, String name, String version, boolean isSsl) {
		try {
			this.uri = (URI) FSTSerializer.unserialize(uri);
		} catch (Exception e) {
			throw new RuntimeException("Unable to unserialized URI: " + e.getMessage(), e);
		}
		this.name = name;
		this.version = version;
		this.isSsl = isSsl;
	}
}

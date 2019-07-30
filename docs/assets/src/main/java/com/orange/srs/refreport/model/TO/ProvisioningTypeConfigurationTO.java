package com.orange.srs.refreport.model.TO;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.TO.ProvisioningSourceTO;

@XmlRootElement(name = "provisioningType")
public class ProvisioningTypeConfigurationTO implements Serializable {

	private static final long serialVersionUID = 8134986426851546291L;

	private String type;
	private List<ProvisioningSourceTO> listProvisioningSourceTO;

	@XmlAttribute
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ProvisioningSourceTO> getListProvisioningSourceTO() {
		return listProvisioningSourceTO;
	}

	public void getListProvisioningSourceTO(List<ProvisioningSourceTO> listProvisioningSourceTO) {
		this.listProvisioningSourceTO = listProvisioningSourceTO;
	}

	public void addProvisioningSourceTO(ProvisioningSourceTO ProvisioningSourceTO) {
		this.listProvisioningSourceTO.add(ProvisioningSourceTO);
	}
}

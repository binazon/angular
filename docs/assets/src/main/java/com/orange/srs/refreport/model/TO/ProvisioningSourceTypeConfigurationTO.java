package com.orange.srs.refreport.model.TO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.orange.srs.statcommon.model.TO.ProvisioningSourceTO;

@XmlRootElement(name = "provisioningSourceType")
public class ProvisioningSourceTypeConfigurationTO implements Serializable {

	private static final long serialVersionUID = 8134986426851546291L;

	private String sourceType;
	private List<ProvisioningSourceTO> listProvisioningSourceTO;

	public ProvisioningSourceTypeConfigurationTO() {
		listProvisioningSourceTO = new ArrayList<ProvisioningSourceTO>();
	}

	public void finalize() {
		listProvisioningSourceTO.removeAll(listProvisioningSourceTO);
		listProvisioningSourceTO = null;
		;
	}

	@XmlAttribute
	public String getSource() {
		return sourceType;
	}

	public void setSource(String sourceType) {
		this.sourceType = sourceType;
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

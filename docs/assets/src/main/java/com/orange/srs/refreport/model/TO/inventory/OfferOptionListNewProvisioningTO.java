package com.orange.srs.refreport.model.TO.inventory;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listOfferOption")
public class OfferOptionListNewProvisioningTO {

	@XmlElement(name = "offerOption")
	public List<OfferOptionNewProvisioningTO> offerOptionNewProvisioningTOs = new ArrayList<OfferOptionNewProvisioningTO>();
}

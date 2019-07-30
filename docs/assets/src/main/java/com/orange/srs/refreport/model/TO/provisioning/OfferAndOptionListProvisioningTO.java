package com.orange.srs.refreport.model.TO.provisioning;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "listOffer")
public class OfferAndOptionListProvisioningTO {

	@XmlElement(name = "offer")
	public List<OfferAndOptionProvisioningTO> offerAndOptionProvisioningTOs = new ArrayList<>();
}

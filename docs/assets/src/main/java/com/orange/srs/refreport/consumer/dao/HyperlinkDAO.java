package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.Hyperlink;
import com.orange.srs.refreport.model.TO.provisioning.HyperlinkProvisioningTO;

public interface HyperlinkDAO extends Dao<Hyperlink, String> {

	public List<HyperlinkProvisioningTO> findAllHyperlinkProvisioningTO();

}

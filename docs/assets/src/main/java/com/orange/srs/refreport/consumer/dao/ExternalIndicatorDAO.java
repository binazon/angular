package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.ExternalIndicator;
import com.orange.srs.refreport.model.TO.provisioning.ExternalIndicatorProvisioningTO;
import com.orange.srs.statcommon.model.TO.rest.ExternalIndicatorTO;

public interface ExternalIndicatorDAO extends Dao<ExternalIndicator, String> {

	public List<ExternalIndicatorProvisioningTO> findAllExternalIndicatorProvisioningTO();

	public List<ExternalIndicatorTO> getExternalIndicatorTOForLabel(String label);

}

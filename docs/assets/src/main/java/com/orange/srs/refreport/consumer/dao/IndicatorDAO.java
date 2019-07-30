package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorProvisioningTO;

public interface IndicatorDAO extends Dao<Indicator, Long> {

	public List<IndicatorProvisioningTO> findAllIndicatorProvisioningTO();

}

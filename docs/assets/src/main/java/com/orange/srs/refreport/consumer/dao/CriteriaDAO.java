package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.Criteria;
import com.orange.srs.refreport.model.CriteriaId;
import com.orange.srs.refreport.model.TO.provisioning.CriteriaProvisioningTO;

public interface CriteriaDAO extends Dao<Criteria, CriteriaId> {

	public List<CriteriaProvisioningTO> findAllCriteriaProvisioningTO();

}

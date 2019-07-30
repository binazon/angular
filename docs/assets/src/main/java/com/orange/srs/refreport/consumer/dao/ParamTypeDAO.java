package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeProvisioningTO;
import com.orange.srs.statcommon.model.TO.ParamTypeTO;

public interface ParamTypeDAO extends Dao<ParamType, Long> {

	public List<ParamTypeTO> findAllParamTypeTO();

	public List<ParamTypeProvisioningTO> findAllParamTypeProvisioningTO();
}

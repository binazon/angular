package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.InputColumn;
import com.orange.srs.refreport.model.TO.provisioning.InputColumnProvisioningTO;

public interface InputColumnDAO extends Dao<InputColumn, Long> {

	public List<InputColumnProvisioningTO> findAllInputColumnProvisioningTOForInputFormat(String inputFormatType);
}

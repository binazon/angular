package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.TO.provisioning.FilterProvisioningTO;
import com.orange.srs.statcommon.model.TO.FilterTO;

public interface FilterDAO extends Dao<Filter, Long> {

	public List<FilterTO> findAllFilterTO();

	public List<FilterProvisioningTO> findAllFilterProvisioningTO();

}

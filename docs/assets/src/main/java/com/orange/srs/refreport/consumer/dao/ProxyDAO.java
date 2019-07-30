package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.Proxy;
import com.orange.srs.refreport.model.TO.provisioning.ProxyProvisioningTO;

public interface ProxyDAO extends Dao<Proxy, Long> {

	public List<ProxyProvisioningTO> findAllProxyProvisioningTO();
}

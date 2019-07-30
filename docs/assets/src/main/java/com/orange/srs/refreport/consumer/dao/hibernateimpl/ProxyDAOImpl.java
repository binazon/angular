package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.consumer.dao.ProxyDAO;
import com.orange.srs.refreport.model.Proxy;
import com.orange.srs.refreport.model.TO.provisioning.ProxyProvisioningTO;

@Stateless
public class ProxyDAOImpl extends AbstractJpaDao<Proxy, Long> implements ProxyDAO {

	@Override
	public List<ProxyProvisioningTO> findAllProxyProvisioningTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.proxyProvisioningTOBuilder("p") + " FROM "
				+ getEntityName() + " p";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}

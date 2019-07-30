package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.consumer.dao.ParamTypeDAO;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeProvisioningTO;
import com.orange.srs.statcommon.model.TO.ParamTypeTO;

@Stateless
public class ParamTypeDAOImpl extends AbstractJpaDao<ParamType, Long> implements ParamTypeDAO {

	@Override
	public List<ParamTypeTO> findAllParamTypeTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.paramTypeTOBuilder("pt") + " FROM " + getEntityName()
				+ " pt";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ParamTypeProvisioningTO> findAllParamTypeProvisioningTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.paramTypeProvisioningTOBuilder("pt") + " FROM "
				+ getEntityName() + " pt";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}

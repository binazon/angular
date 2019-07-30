package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.CriteriaDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.Criteria;
import com.orange.srs.refreport.model.CriteriaId;
import com.orange.srs.refreport.model.TO.provisioning.CriteriaProvisioningTO;

@Stateless
public class CriteriaDAOImpl extends AbstractJpaDao<Criteria, CriteriaId> implements CriteriaDAO {

	@Override
	public List<CriteriaProvisioningTO> findAllCriteriaProvisioningTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.criteriaProvisioningTOBuilder("c") + " FROM "
				+ getEntityName() + " c";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}

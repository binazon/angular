package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.ExternalIndicatorDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.ExternalIndicator;
import com.orange.srs.refreport.model.TO.provisioning.ExternalIndicatorProvisioningTO;
import com.orange.srs.statcommon.model.TO.rest.ExternalIndicatorTO;

@Stateless
public class ExternalIndicatorDAOImpl extends AbstractJpaDao<ExternalIndicator, String>
		implements ExternalIndicatorDAO {

	@Override
	public List<ExternalIndicatorProvisioningTO> findAllExternalIndicatorProvisioningTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.externalIndicatorProvisioningTOBuilder("ei")
				+ " FROM " + getEntityName() + " ei";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ExternalIndicatorTO> getExternalIndicatorTOForLabel(String label) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.externalIndicatorTOBuilder("ei") + " FROM "
				+ getEntityName() + " ei" + " WHERE ei." + ExternalIndicator.FIELD_LABEL + "=:label";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("label", label);
		return query.getResultList();
	}
}

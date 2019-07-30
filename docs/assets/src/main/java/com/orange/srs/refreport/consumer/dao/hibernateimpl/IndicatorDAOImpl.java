package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.IndicatorDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorProvisioningTO;

@Stateless
public class IndicatorDAOImpl extends AbstractJpaDao<Indicator, Long> implements IndicatorDAO {

	@Override
	public List<IndicatorProvisioningTO> findAllIndicatorProvisioningTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.indicatorProvisioningTOBuilder("i") + " FROM "
				+ getEntityName() + " i";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}

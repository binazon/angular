package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.FilterDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.TO.provisioning.FilterProvisioningTO;
import com.orange.srs.statcommon.model.TO.FilterTO;

@Stateless
public class FilterDAOImpl extends AbstractJpaDao<Filter, Long> implements FilterDAO {

	@Override
	public List<FilterTO> findAllFilterTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.filterTOBuilder("f") + " FROM " + getEntityName()
				+ " f";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<FilterProvisioningTO> findAllFilterProvisioningTO() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.filterProvisioningTOBuilder("f") + " FROM "
				+ getEntityName() + " f";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}

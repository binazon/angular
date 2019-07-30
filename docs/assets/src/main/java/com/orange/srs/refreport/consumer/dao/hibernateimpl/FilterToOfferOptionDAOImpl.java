package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.FilterToOfferOptionDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.FilterToOfferOption;
import com.orange.srs.refreport.model.FilterToOfferOptionId;
import com.orange.srs.refreport.model.TO.FilterToOfferOptionTO;

@Stateless
public class FilterToOfferOptionDAOImpl extends AbstractJpaDao<FilterToOfferOption, FilterToOfferOptionId>
		implements FilterToOfferOptionDAO {

	@Override
	public List<FilterToOfferOptionTO> findAllFilterToOfferOptionTOs() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.filterToOfferOptionTOBuilderFromLink("fo") + " FROM "
				+ getEntityName() + " fo";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

}

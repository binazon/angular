package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.OfferDAO;
import com.orange.srs.refreport.model.Offer;
import com.orange.srs.refreport.model.OfferOption;

@Stateless
public class OfferDAOImpl extends AbstractJpaDao<Offer, Long> implements OfferDAO {

	@Override
	public List<Object[]> findAllOffersAndOptionInfo() {
		String jpqlQuery = "SELECT o, oo." + OfferOption.FIELD_ALIAS + ", oo." + OfferOption.FIELD_LABEL + ", oo."
				+ OfferOption.FIELD_TYPE + " FROM " + getEntityName() + " o " + " LEFT OUTER JOIN o."
				+ Offer.FIELD_OFFER_OPTIONS + " oo" + " ORDER BY o." + Offer.FIELD_ALIAS;
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<String> findAllOfferAliases() {
		String jpqlQuery = "SELECT o." + Offer.FIELD_ALIAS + " FROM " + getEntityName() + " o";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

}

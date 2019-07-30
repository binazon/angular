package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.Offer;

public interface OfferDAO extends Dao<Offer, Long> {

	public List<Object[]> findAllOffersAndOptionInfo();

	public List<String> findAllOfferAliases();

}

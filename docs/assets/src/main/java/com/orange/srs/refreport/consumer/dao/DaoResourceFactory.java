package com.orange.srs.refreport.consumer.dao;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public final class DaoResourceFactory {

	@Produces
	@PersistenceContext(unitName = "persitenceUnit")
	private EntityManager em;

	// @Produces @testEntity
	// @PersistenceContext(unitName="clientDB")
	// private EntityManager em2;

	private DaoResourceFactory() {
	}
}

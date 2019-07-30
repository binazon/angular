package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.EntityTypeAndSubTypeDAO;
import com.orange.srs.refreport.model.EntityTypeAndSubtype;
import com.orange.srs.refreport.model.EntityTypeAndSubtypeId;

@Stateless
public class EntityTypeAndSubtypeDAOImpl extends AbstractJpaDao<EntityTypeAndSubtype, EntityTypeAndSubtypeId>
		implements EntityTypeAndSubTypeDAO {

	@Override
	public List<String> findDistinctType() {
		String jpqlQuery = "SELECT DISTINCT(ets." + EntityTypeAndSubtype.FIELD_TYPE + ")" + " FROM " + getEntityName()
				+ " ets";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}

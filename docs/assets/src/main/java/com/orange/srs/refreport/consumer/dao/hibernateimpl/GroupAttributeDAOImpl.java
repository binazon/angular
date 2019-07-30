package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.GroupAttributeDAO;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.model.GroupAttribute;
import com.orange.srs.refreport.model.GroupAttributeId;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.statcommon.model.parameter.GroupAttributeParameter;

@Stateless
public class GroupAttributeDAOImpl extends AbstractJpaDao<GroupAttribute, GroupAttributeId>
		implements GroupAttributeDAO {

	@SuppressWarnings("unchecked")
	public List<GroupAttributeParameter> findAllGroupAttributeParameter() {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.groupAttributeParameterBuilder("a", "rg") + " FROM "
				+ getEntityName() + " a" + " INNER JOIN a." + GroupAttribute.FIELD_REPORTING_GROUP + " rg";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<GroupAttributeParameter> findGroupAttributeParameterByGroup(String origin, String reportingGroupRef) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.groupAttributeParameterBuilder("a", "rg") + " FROM "
				+ getEntityName() + " a" + " INNER JOIN a." + GroupAttribute.FIELD_REPORTING_GROUP + " rg"
				+ " WHERE rg." + ReportingGroup.FIELD_ORIGIN + "=:origin" + " AND rg."
				+ ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("origin", origin);
		query.setParameter("reportingGroupRef", reportingGroupRef);
		return query.getResultList();
	}
}

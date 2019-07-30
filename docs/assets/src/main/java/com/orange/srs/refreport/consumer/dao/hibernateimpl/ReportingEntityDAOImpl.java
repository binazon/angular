package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.consumer.dao.ReportingEntityDAO;
import com.orange.srs.refreport.model.DataLocation;
import com.orange.srs.refreport.model.EntityAttribute;
import com.orange.srs.refreport.model.EntityTypeAndSubtype;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.ReportingEntityPartitionTO;
import com.orange.srs.refreport.model.TO.ReportingEntityTypeTO;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryGkTO;
import com.orange.srs.statcommon.model.constant.OrientDBAttributeName;

@Stateless
public class ReportingEntityDAOImpl extends AbstractJpaDao<ReportingEntity, Long> implements ReportingEntityDAO {

	@Override
	public List<Object[]> findEntityDataLocationInfo(String entityType, String... origins) {

		String sqlQuery = "SELECT re." + ReportingEntity.COL_NAME_PK + ", re." + ReportingEntity.COL_NAME_ENTITY_ID + //
				", re." + ReportingEntity.COL_NAME_ORIGIN + ", a." + EntityAttribute.COL_NAME_VALUE + //
				" FROM " + ReportingEntity.TABLE_NAME + " re " + //
				" LEFT JOIN " + DataLocation.TABLE_NAME + " d ON (d." + DataLocation.COL_NAME_REPORTING_ENTITY_FK
				+ "=re." + ReportingEntity.COL_NAME_PK + ") " + //
				" LEFT JOIN " + EntityAttribute.TABLE_NAME + " a ON " + //
				"(a." + EntityAttribute.COL_NAME_REPORTING_ENTITY_FK + "=re." + ReportingEntity.COL_NAME_PK + " AND a."
				+ EntityAttribute.COL_NAME_NAME + "='" + OrientDBAttributeName.DOMAIN + "')" + //
				" WHERE re." + ReportingEntity.COL_NAME_ENTITY_TYPE + "=:entitytype AND d."
				+ DataLocation.COL_NAME_REPORTING_ENTITY_FK + " IS NULL";

		if (origins != null && origins.length > 0) {
			sqlQuery += " AND re." + ReportingEntity.COL_NAME_ORIGIN + " IN :origins";
		}

		Query query = getEntityManager().createNativeQuery(sqlQuery);
		query.setParameter("entitytype", entityType);
		if (origins != null && origins.length > 0) {
			query.setParameter("origins", Arrays.asList(origins));
		}
		return query.getResultList();
	}

	@Override
	public List<Long> findPkOrderedByGroupAndType() {
		String jpqlQuery = "SELECT DISTINCT re." + ReportingEntity.FIELD_PK + " FROM " + getEntityName() + " re ";
		jpqlQuery += " LEFT JOIN re." + ReportingEntity.FIELD_REPORTINGGROUP + " rg";
		jpqlQuery += " ORDER BY rg." + ReportingGroup.FIELD_PK + ",re." + ReportingEntity.FIELD_ENTITYTYPE + ",re."
				+ ReportingEntity.FIELD_PK;
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ReportingEntityPartitionTO> findEntityPartitionWherePkInSelectionOrderByType(List<Long> pkSelection) {
		StringBuilder jpqlQuery = new StringBuilder("SELECT NEW ")
				.append(JPATOConstructorBuilder.reportingEntityPartitionsTOBuilder("re"));
		jpqlQuery.append(" FROM ").append(getEntityName()).append(" re");
		jpqlQuery.append(" WHERE re.").append(ReportingEntity.FIELD_PK).append(" IN :pkSelection");
		// jpqlQuery.append(" WHERE re.").append(ReportingEntity.FIELD_PK).append(" IN
		// (").append(StringUtils.join(pkSelection, ',')).append(")");
		jpqlQuery.append(" ORDER BY re.").append(ReportingEntity.FIELD_ENTITYTYPE);
		Query query = getEntityManager().createQuery(jpqlQuery.toString());
		query.setParameter("pkSelection", pkSelection);
		return query.getResultList();
	}

	@Override
	public List<ReportingEntityPartitionTO> findEntityPartitionWherePkInSelectionOrderByGroupAndType(
			List<Long> pkSelection) {
		StringBuilder jpqlQuery = new StringBuilder("SELECT NEW ")
				.append(JPATOConstructorBuilder.reportingEntityPartitionsTOBuilder("re"));
		jpqlQuery.append(" FROM ").append(getEntityName()).append(" re");
		jpqlQuery.append(" LEFT JOIN re." + ReportingEntity.FIELD_REPORTINGGROUP + " rg");
		jpqlQuery.append(" WHERE re.").append(ReportingEntity.FIELD_PK).append(" IN :pkSelection");
		// jpqlQuery.append(" WHERE re.").append(ReportingEntity.FIELD_PK).append(" IN
		// (").append(StringUtils.join(pkSelection, ',')).append(")");
		jpqlQuery.append(" ORDER BY rg.").append(ReportingGroup.FIELD_PK).append(", re.")
				.append(ReportingEntity.FIELD_ENTITYTYPE).append(", re.").append(ReportingEntity.FIELD_PK);

		Query query = getEntityManager().createQuery(jpqlQuery.toString());
		query.setParameter("pkSelection", pkSelection);

		Set<ReportingEntityPartitionTO> reportingEntityPartitionTOs = new LinkedHashSet<>(query.getResultList());
		List<ReportingEntityPartitionTO> result = new ArrayList<>(reportingEntityPartitionTOs);
		reportingEntityPartitionTOs.clear();
		return result;
	}

	@Override
	public List<Long> findPkOrderBy(String[] orderAttributes, boolean reverseOrder) {
		String jpqlQuery = "SELECT re." + ReportingEntity.FIELD_PK + " FROM " + getEntityName() + " re ";
		jpqlQuery += " ORDER BY ";
		for (int i = 0; i < orderAttributes.length; i++) {
			jpqlQuery += "re." + orderAttributes[i];
			if (reverseOrder) {
				jpqlQuery += " DESC";
			}
			if (i < orderAttributes.length - 1) {
				jpqlQuery += ',';
			}
		}
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ReportingEntityTypeTO> findReportingEntitiesTypesForAReportingGroup(String reportingGroupRef,
			String reportingGroupOrigin, String entityType, String entitySubtype) {
		String jpqlQuery = "SELECT NEW " + ReportingEntityTypeTO.class.getCanonicalName() + "(" + " re."
				+ ReportingEntity.FIELD_ENTITYID + ", re." + ReportingEntity.FIELD_ENTITYTYPE + ", re."
				+ ReportingEntity.FIELD_LABEL + ", re." + ReportingEntity.FIELD_ORIGIN + ", re."
				+ ReportingEntity.FIELD_PARENT + "." + ReportingGroup.FIELD_PK + ", re." + ReportingEntity.FIELD_PK;
		if (!entitySubtype.isEmpty()) {
			jpqlQuery = jpqlQuery + ", et." + EntityTypeAndSubtype.FIELD_SUBTYPE;
		}
		jpqlQuery = jpqlQuery + ") FROM " + getEntityName() + " re" + " LEFT OUTER JOIN re."
				+ ReportingEntity.FIELD_REPORTINGGROUP + " rg ";

		if (!entitySubtype.isEmpty()) {
			jpqlQuery = jpqlQuery + " LEFT JOIN re." + ReportingEntity.FIELD_SUBTYPES + " et ";
		}
		jpqlQuery = jpqlQuery + " WHERE rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef"
				+ " AND rg." + ReportingGroup.FIELD_ORIGIN + "=:reportingGroupOrigin" + " AND re."
				+ ReportingEntity.FIELD_ENTITYTYPE + "=:entityType";

		if (!entitySubtype.isEmpty()) {
			jpqlQuery = jpqlQuery + " AND et." + EntityTypeAndSubtype.FIELD_TYPE + "=:entityType " + " AND et."
					+ EntityTypeAndSubtype.FIELD_SUBTYPE + "=:entitySubtype";
		}

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("reportingGroupRef", reportingGroupRef);
		query.setParameter("reportingGroupOrigin", reportingGroupOrigin);
		query.setParameter("entityType", entityType);
		if (!entitySubtype.isEmpty()) {
			query.setParameter("entitySubtype", entitySubtype);
		}

		return query.getResultList();
	}

	@Override
	public List<ExportSpecificInventoryGkTO> findExportInventoryForGK(List<String> entityTypes) {

		List<ExportSpecificInventoryGkTO> result;

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.exportGkInventoryTOBuilder("re") + " FROM "
				+ getEntityName() + " re" + " WHERE re." + ReportingEntity.FIELD_ENTITYTYPE + " IN :entityTypes ";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("entityTypes", entityTypes);

		result = query.getResultList();

		return result;
	}

	@Deprecated
	@Override
	public Long findLastPk() {
		String jpqlQuery = "SELECT re." + ReportingEntity.FIELD_PK + " FROM " + getEntityName() + " re ORDER BY re."
				+ ReportingEntity.FIELD_PK + " DESC";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setMaxResults(1);
		Object result = query.getSingleResult();
		return result == null ? 0L : (Long) result;
	}

}

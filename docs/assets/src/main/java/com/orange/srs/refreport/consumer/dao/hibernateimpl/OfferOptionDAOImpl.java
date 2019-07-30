package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.consumer.dao.OfferOptionDAO;
import com.orange.srs.refreport.model.EntityAttribute;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryOpenFlowTO;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryPaiTO;
import com.orange.srs.statcommon.model.constant.OrientDBAttributeName;
import com.orange.srs.statcommon.model.enums.EntityTypeEnum;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;
import com.orange.srs.statcommon.model.enums.OriginEnum;

@Stateless
public class OfferOptionDAOImpl extends AbstractJpaDao<OfferOption, Long> implements OfferOptionDAO {

	@Override
	public List<String> findOfferOptionAliasIfExistWithType(List<String> offerOptionAliases,
			List<OfferOptionTypeEnum> types) {
		String jpqlQuery = "SELECT oo." + OfferOption.FIELD_ALIAS + " FROM " + getEntityName() + " oo " + " WHERE oo."
				+ OfferOption.FIELD_ALIAS + " IN :offerOptionAliases " + " AND oo." + OfferOption.FIELD_TYPE
				+ " IN :types";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("offerOptionAliases", offerOptionAliases);
		query.setParameter("types", types);
		return query.getResultList();
	}

	public List<OfferOption> findOfferOptionsByTypes(List<OfferOptionTypeEnum> types) {
		String jpqlQuery = "SELECT oo FROM " + getEntityName() + " oo WHERE oo." + OfferOption.FIELD_TYPE
				+ " IN :types";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("types", types);
		return query.getResultList();
	}

	public List<OfferOption> findOfferOptionsFiltered(List<OfferOptionTypeEnum> types, String reportingGroupRef,
			OriginEnum reportingGroupOrigin) {
		String jpqlQuery = "SELECT oo FROM " + getEntityName() + " oo ";

		boolean originPresent = reportingGroupOrigin != null;
		boolean rgRefPresent = StringUtils.isNotEmpty(reportingGroupRef);
		boolean typePresent = !CollectionUtils.isEmpty(types);
		String AND = "";

		if (rgRefPresent || originPresent) {
			jpqlQuery += "INNER JOIN oo." + OfferOption.FIELD_REPORTING_GROUPS + " rg ";
		}

		if (rgRefPresent || originPresent || typePresent) {
			jpqlQuery += "WHERE ";
		}

		if (rgRefPresent) {
			jpqlQuery += "rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF + " = :reportingGroupRef ";
			AND = " AND ";
		}
		if (originPresent) {
			jpqlQuery += AND + "rg." + ReportingGroup.FIELD_ORIGIN + " = :reportingGroupOrigin ";
			AND = " AND ";
		}
		if (typePresent) {
			jpqlQuery += AND + "oo." + OfferOption.FIELD_TYPE + " IN :types ";
		}

		Query query = getEntityManager().createQuery(jpqlQuery);
		if (rgRefPresent) {
			query.setParameter("reportingGroupRef", reportingGroupRef);
		}
		if (originPresent) {
			query.setParameter("reportingGroupOrigin", reportingGroupOrigin.getValue());
		}
		if (typePresent) {
			query.setParameter("types", types);
		}

		return query.getResultList();
	}

	@Override
	public List<ExportSpecificInventoryOpenFlowTO> findExportInventoryForOpenFlowWithOfferOption(
			List<String> offerOptionAliases) {

		List<ExportSpecificInventoryOpenFlowTO> result;

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.exportOpenFlowInventoryTOBuilder("re", "ea")
				+ " FROM " + getEntityName() + " oo" + " INNER JOIN oo." + OfferOption.FIELD_REPORTING_GROUPS + " rg"
				+ " INNER JOIN rg." + ReportingGroup.FIELD_ENTITIES + " re on re." + ReportingEntity.FIELD_ENTITYTYPE
				+ "='" + EntityTypeEnum.EQUIPMENT.getValue() + "' " + " INNER JOIN re."
				+ ReportingEntity.FIELD_ATTRIBUTES + " ea on ea." + EntityAttribute.FIELD_NAME + "='"
				+ OrientDBAttributeName.ADMIN_ADDRESS + "' " + " WHERE oo." + OfferOption.FIELD_ALIAS
				+ " IN :offerOptionAliases " + " GROUP BY re." + ReportingEntity.FIELD_ENTITYID;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("offerOptionAliases", offerOptionAliases);

		result = query.getResultList();

		return result;
	}

	@Override
	public List<ExportSpecificInventoryPaiTO> findExportInventoryForPaiWithOfferOption(
			List<String> offerOptionAliases) {

		List<ExportSpecificInventoryPaiTO> result;

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.exportPaiInventoryTOBuilder("re", "rg") + " FROM "
				+ getEntityName() + " oo" + " INNER JOIN oo." + OfferOption.FIELD_REPORTING_GROUPS + " rg"
				+ " INNER JOIN rg." + ReportingGroup.FIELD_ENTITIES + " re on re." + ReportingEntity.FIELD_ENTITYTYPE
				+ "='" + EntityTypeEnum.EQUIPMENT.getValue() + "' " + " WHERE oo." + OfferOption.FIELD_ALIAS
				+ " IN :offerOptionAliases " + " GROUP BY re." + ReportingEntity.FIELD_ENTITYID + ", rg."
				+ ReportingGroup.FIELD_REPORTING_GROUP_REF + ", rg." + ReportingGroup.FIELD_ORIGIN;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("offerOptionAliases", offerOptionAliases);

		result = query.getResultList();

		return result;
	}
}

package com.orange.srs.refreport.consumer.dao.hibernateimpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.orange.srs.refreport.consumer.dao.AbstractJpaDao;
import com.orange.srs.refreport.consumer.dao.JPATOConstructorBuilder;
import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.model.DataLocation;
import com.orange.srs.refreport.model.EntityTypeAndSubtype;
import com.orange.srs.refreport.model.FilterConfig;
import com.orange.srs.refreport.model.GroupReportConfig;
import com.orange.srs.refreport.model.GroupingRule;
import com.orange.srs.refreport.model.GroupingRuleId;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.ReportOutput;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.FilterToOfferOptionTO;
import com.orange.srs.refreport.model.TO.ReportingGroupFilterUriTO;
import com.orange.srs.refreport.model.TO.ReportingGroupTO;
import com.orange.srs.refreport.model.TO.ReportingGroupWithOfferOptionTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportTemplateTO;
import com.orange.srs.refreport.model.TO.inventory.OfferOptionListNewProvisioningTO;
import com.orange.srs.refreport.model.TO.inventory.OfferOptionNewProvisioningTO;
import com.orange.srs.refreport.model.enumerate.ReportingGroupTypeEnum;
import com.orange.srs.statcommon.model.TO.ReportingGroupKeyTO;
import com.orange.srs.statcommon.model.TO.ReportingGroupLocationTO;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;
import com.orange.srs.statcommon.model.enums.ReportOutputTypeEnum;
import com.orange.srs.statcommon.technical.ModelUtils;

@Stateless
public class ReportingGroupDAOImpl extends AbstractJpaDao<ReportingGroup, Long> implements ReportingGroupDAO {

	@Override
	public List<ReportingGroupKeyTO> findAllReportingGroupsKeys() {

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportingGroupKeyTOBuilder("p") + " FROM "
				+ getEntityName() + " p";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ReportingGroupKeyTO> findAllReportingGroupsKeysWithSourceColumn() {

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportingGroupKeyWithSourceColumnTOBuilder("p")
				+ " FROM " + getEntityName() + " p";

		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public List<ReportingGroupLocationTO> findLocationForOption(String optionAlias) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportingGroupLocationTOBuilder("rg") + " FROM "
				+ getEntityName() + " rg, " + ModelUtils.getEntityNameForClass(OfferOption.class) + " so "
				+ " WHERE so." + OfferOption.FIELD_ALIAS + "=:optionAlias AND so MEMBER OF rg."
				+ ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS + " ORDER BY rg." + ReportingGroup.FIELD_DATA_LOCATION
				+ "." + DataLocation.FIELD_LOCATION_PATTERN + " ASC";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("optionAlias", optionAlias);

		return query.getResultList();
	}

	@Override
	public List<ReportingGroupTO> findAllReportingGroupTO(String groupingRuleSeparator, String groupingValueSeparator) {
		String nativeQuery = buildSelectNativeQueryForReportingGroupTO(groupingRuleSeparator, groupingValueSeparator)
				+ buildGroupByNativeQueryForReportingGroupTO();
		Query query = getEntityManager().createNativeQuery(nativeQuery);
		return buildReportingGroupTOFromNativeQueryResult(query.getResultList());
	}

	private String buildSelectNativeQueryForReportingGroupTO(String groupingRuleSeparator,
			String groupingValueSeparator) {
		return "SELECT t." + ReportingGroup.COL_NAME_PK + ",t." + ReportingGroup.COL_NAME_REPORTING_GROUP_REF + ",t."
				+ ReportingGroup.COL_NAME_ORIGIN + ",t." + ReportingGroup.COL_NAME_LABEL + ",t."
				+ ReportingGroup.COL_NAME_SOURCE + ",t." + ReportingGroup.COL_NAME_TYPE + ",group_concat(t."
				+ GroupingRuleId.COL_NAME_GROUPING_RULE + " separator '" + groupingRuleSeparator + "') as "
				+ GroupingRuleId.COL_NAME_GROUPING_RULE + ",group_concat(t." + GroupingRuleId.COL_NAME_GROUPING_VALUE
				+ " separator '" + groupingRuleSeparator + "') as " + GroupingRuleId.COL_NAME_GROUPING_VALUE + ",t."
				+ DataLocation.COL_NAME_LOCATION_PATTERN + " FROM (" + "SELECT rg." + ReportingGroup.COL_NAME_PK
				+ ",rg." + ReportingGroup.COL_NAME_REPORTING_GROUP_REF + ",rg." + ReportingGroup.COL_NAME_ORIGIN
				+ ",rg." + ReportingGroup.COL_NAME_LABEL + ",rg." + ReportingGroup.COL_NAME_SOURCE + ",rg."
				+ ReportingGroup.COL_NAME_TYPE + ",gr." + GroupingRuleId.COL_NAME_GROUPING_RULE + ",group_concat(gr."
				+ GroupingRuleId.COL_NAME_GROUPING_VALUE + " separator '" + groupingValueSeparator + "') as "
				+ GroupingRuleId.COL_NAME_GROUPING_VALUE + ",d." + DataLocation.COL_NAME_LOCATION_PATTERN + " FROM "
				+ ModelUtils.getEntityTableName(ReportingGroup.class) + " rg" + " JOIN "
				+ ModelUtils.getEntityTableName(GroupingRule.class) + " gr on gr."
				+ GroupingRuleId.COL_NAME_REPORTING_GROUP_FK + " = rg." + ReportingGroup.COL_NAME_PK + " JOIN "
				+ ModelUtils.getEntityTableName(DataLocation.class) + " d on d." + DataLocation.COL_NAME_PK + " = rg."
				+ ReportingGroup.COL_NAME_DATA_LOCATION_FK;
	}

	private String buildGroupByNativeQueryForReportingGroupTO() {
		return " GROUP BY rg." + ReportingGroup.COL_NAME_PK + ", gr." + GroupingRuleId.COL_NAME_GROUPING_RULE + ") as t"
				+ " GROUP BY t." + ReportingGroup.COL_NAME_PK;
	}

	private List<ReportingGroupTO> buildReportingGroupTOFromNativeQueryResult(List<Object[]> queryResult) {
		List<ReportingGroupTO> reportingGroupTOs = new ArrayList<>();
		for (Object[] rowResult : queryResult) {
			int index = 0;
			ReportingGroupTO reportingGroupTO = new ReportingGroupTO();
			reportingGroupTOs.add(reportingGroupTO);
			reportingGroupTO.setReportingGroupPk(((BigInteger) rowResult[index++]).longValue());
			reportingGroupTO.setReportingGroupRef((String) rowResult[index++]);
			reportingGroupTO.setOrigin((String) rowResult[index++]);
			reportingGroupTO.setLabel((String) rowResult[index++]);
			reportingGroupTO.setSource((String) rowResult[index++]);
			reportingGroupTO.setType(ReportingGroupTypeEnum.valueOf((String) rowResult[index++]));
			reportingGroupTO.setGroupingCriteria((String) rowResult[index++]);
			reportingGroupTO.setGroupingValue((String) rowResult[index++]);
			reportingGroupTO.setDataLocation((String) rowResult[index++]);
		}
		return reportingGroupTOs;
	}

	@Override
	public List<ReportingGroup> findReportingGroupForOfferOption(String offerOptionAlias) {

		String jpqlQuery = "SELECT rg FROM " + getEntityName() + " rg, "
				+ ModelUtils.getEntityNameForClass(OfferOption.class) + " so " + " WHERE so." + OfferOption.FIELD_ALIAS
				+ "=:offerOptionAlias " + " AND so MEMBER OF rg." + ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("offerOptionAlias", offerOptionAlias);

		return query.getResultList();
	}

	@Override
	public List<Object[]> findReportingGroupKeysByOfferOption(List<String> origins, List<String> offerOptionAliases) {
		String jpqlQuery = "SELECT rg." + ReportingGroup.FIELD_ORIGIN + ", rg."
				+ ReportingGroup.FIELD_REPORTING_GROUP_REF + ", oo." + OfferOption.FIELD_ALIAS + " FROM "
				+ getEntityName() + " rg, " + ModelUtils.getEntityNameForClass(OfferOption.class) + " oo "
				+ " WHERE rg." + ReportingGroup.FIELD_ORIGIN + " IN :origins" + " AND oo." + OfferOption.FIELD_ALIAS
				+ " IN :offerOptionAliases " + " AND oo MEMBER OF rg." + ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS
				+ " ORDER BY oo." + OfferOption.FIELD_ALIAS;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("origins", origins);
		query.setParameter("offerOptionAliases", offerOptionAliases);
		return query.getResultList();
	}

	@Override
	public List<ReportingGroupTO> findReportingGroupTOByMultipleCriteria(String[] attributes, Object[] values,
			String groupingRuleSeparator, String groupingValueSeparator) {

		if (attributes.length != values.length) {
			throw new RuntimeException(
					"Exception in findReportingGroupTOByMultipleCriteria, attributes length and values length must be equals ");
		}

		StringBuilder nativeQuery = new StringBuilder(
				buildSelectNativeQueryForReportingGroupTO(groupingRuleSeparator, groupingValueSeparator));
		nativeQuery.append(" WHERE ");

		for (int i = 0; i < attributes.length; i++) {
			nativeQuery.append("rg.").append(attributes[i]).append("=:objet").append(i);
			if (i < attributes.length - 1) {
				nativeQuery.append(" AND ");
			}
		}
		nativeQuery.append(buildGroupByNativeQueryForReportingGroupTO());

		Query query = getEntityManager().createNativeQuery(nativeQuery.toString());
		for (int i = 0; i < values.length; i++) {
			query.setParameter("objet" + i, values[i]);
		}
		return buildReportingGroupTOFromNativeQueryResult(query.getResultList());
	}

	@Override
	public List<ReportingGroupTO> findReportingGroupTOByOfferOption(String optionAlias, String groupingRuleSeparator,
			String groupingValueSeparator) {

		String nativeQuery = buildSelectNativeQueryForReportingGroupTO(groupingRuleSeparator, groupingValueSeparator)
				+ " JOIN " + ReportingGroup.TJ_NAME_OFFER_OPTION + " tj on tj."
				+ ReportingGroup.TJ_COL_NAME_REPORTING_GROUP + "=rg." + ReportingGroup.COL_NAME_PK + " JOIN "
				+ ModelUtils.getEntityTableName(OfferOption.class) + " oo on oo." + OfferOption.COL_NAME_PK + "=tj."
				+ ReportingGroup.TJ_COL_NAME_OFFER_OPTION + " WHERE oo." + OfferOption.COL_NAME_ALIAS + "=:optionAlias"
				+ buildGroupByNativeQueryForReportingGroupTO();

		Query query = getEntityManager().createNativeQuery(nativeQuery);
		query.setParameter("optionAlias", optionAlias);
		return buildReportingGroupTOFromNativeQueryResult(query.getResultList());
	}

	@Override
	public List<ExportInventoryReportTO> findExportInventoryReportTOForReportingGroupWithOption(String origin,
			String reportingGroupRef, OfferOptionListNewProvisioningTO offerOptionListNewProvisioningTO) {

		List<ExportInventoryReportTO> result;

		// NEW PROVISIONING: get the list of offer options associated to the new provisioning process
		String offerOptionsNewProv = "";
		for (OfferOptionNewProvisioningTO offerOptionNewProvisioningTO : offerOptionListNewProvisioningTO.offerOptionNewProvisioningTOs) {
			offerOptionsNewProv += "'" + offerOptionNewProvisioningTO.alias + "', ";
		}
		String whereClauseOfferOptionAlias = "";
		if (!offerOptionsNewProv.isEmpty()) {
			offerOptionsNewProv = offerOptionsNewProv.substring(0, offerOptionsNewProv.length() - 2);
			whereClauseOfferOptionAlias = " AND oo." + OfferOption.FIELD_ALIAS + " NOT IN (" + offerOptionsNewProv
					+ ")";
		}

		// Requests to get report inventory for the OLD PROVISIONING process
		String firstJpqlQuery = "SELECT NEW "
				+ JPATOConstructorBuilder.exportReportInventoryTOBuilder("rc", "i", "r", "ro", "o", "oo", "grc")
				+ " FROM " + getEntityName() + " rg" + " INNER JOIN rg." + ReportingGroup.FIELD_GROUP_REPORT_CONFIGS
				+ " grc" + " INNER JOIN rg." + ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS + " oo" + " INNER JOIN oo."
				+ OfferOption.FIELD_RELATED_0FFER + " o" + " INNER JOIN grc." + GroupReportConfig.FIELD_REPORT_CONFIG
				+ " rc" + " INNER JOIN rc." + ReportConfig.FIELD_ASSOCIATED_REPORT + " r" + " INNER JOIN rc."
				+ ReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " ro" + " INNER JOIN rc."
				+ ReportConfig.FIELD_INDICATORS + " i" + " WHERE rg." + ReportingGroup.FIELD_ORIGIN + "=:origin"
				+ " AND rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef" + " AND grc."
				+ GroupReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " IS NULL" + " AND grc."
				+ GroupReportConfig.FIELD_IS_ENABLE + "=:enable" + " AND oo." + OfferOption.FIELD_TYPE + "='"
				+ OfferOptionTypeEnum.INTERACTIVE + "'" + whereClauseOfferOptionAlias + " AND rc MEMBER OF oo."
				+ OfferOption.FIELD_REPORTCONFIGS;

		Query query = getEntityManager().createQuery(firstJpqlQuery);
		query.setParameter("enable", true);
		query.setParameter("origin", origin);
		query.setParameter("reportingGroupRef", reportingGroupRef);

		result = query.getResultList();

		String secondJpqlQuery = "SELECT NEW "
				+ JPATOConstructorBuilder.exportReportInventoryTOBuilder("rc", "i", "r", "ro", "o", "oo", "grc")
				+ " FROM " + getEntityName() + " rg" + " INNER JOIN rg." + ReportingGroup.FIELD_GROUP_REPORT_CONFIGS
				+ " grc" + " INNER JOIN rg." + ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS + " oo" + " INNER JOIN oo."
				+ OfferOption.FIELD_RELATED_0FFER + " o" + " INNER JOIN grc." + GroupReportConfig.FIELD_REPORT_CONFIG
				+ " rc" + " INNER JOIN rc." + ReportConfig.FIELD_ASSOCIATED_REPORT + " r" + " INNER JOIN grc."
				+ GroupReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " ro" + " INNER JOIN rc."
				+ ReportConfig.FIELD_INDICATORS + " i" + " WHERE rg." + ReportingGroup.FIELD_ORIGIN + "=:origin"
				+ " AND rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef" + " AND grc."
				+ GroupReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " IS NOT NULL" + " AND grc."
				+ GroupReportConfig.FIELD_IS_ENABLE + "=:enable" + " AND oo." + OfferOption.FIELD_TYPE + "='"
				+ OfferOptionTypeEnum.INTERACTIVE + "'" + whereClauseOfferOptionAlias + " AND rc MEMBER OF oo."
				+ OfferOption.FIELD_REPORTCONFIGS;

		Query secondQuery = getEntityManager().createQuery(secondJpqlQuery);
		secondQuery.setParameter("enable", true);
		secondQuery.setParameter("origin", origin);
		secondQuery.setParameter("reportingGroupRef", reportingGroupRef);

		result.addAll(secondQuery.getResultList());

		// Requests to get report inventory for the NEW PROVISIONING process (the WHERE clause "is_enable=TRUE" is
		// obsolete)
		if (!offerOptionsNewProv.isEmpty()) {
			firstJpqlQuery = "SELECT NEW "
					+ JPATOConstructorBuilder.exportReportInventoryTOBuilder("rc", "i", "r", "ro", "o", "oo", "grc")
					+ " FROM " + getEntityName() + " rg" + " INNER JOIN rg." + ReportingGroup.FIELD_GROUP_REPORT_CONFIGS
					+ " grc" + " INNER JOIN rg." + ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS + " oo"
					+ " INNER JOIN oo." + OfferOption.FIELD_RELATED_0FFER + " o" + " INNER JOIN grc."
					+ GroupReportConfig.FIELD_REPORT_CONFIG + " rc" + " INNER JOIN rc."
					+ ReportConfig.FIELD_ASSOCIATED_REPORT + " r" + " INNER JOIN rc."
					+ ReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " ro" + " INNER JOIN rc."
					+ ReportConfig.FIELD_INDICATORS + " i" + " WHERE rg." + ReportingGroup.FIELD_ORIGIN + "=:origin"
					+ " AND rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef" + " AND grc."
					+ GroupReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " IS NULL" + " AND oo."
					+ OfferOption.FIELD_TYPE + "='" + OfferOptionTypeEnum.INTERACTIVE + "'" + " AND oo."
					+ OfferOption.FIELD_ALIAS + " IN (" + offerOptionsNewProv + ")" + " AND rc MEMBER OF oo."
					+ OfferOption.FIELD_REPORTCONFIGS;

			query = getEntityManager().createQuery(firstJpqlQuery);
			query.setParameter("origin", origin);
			query.setParameter("reportingGroupRef", reportingGroupRef);

			result.addAll(query.getResultList());

			secondJpqlQuery = "SELECT NEW "
					+ JPATOConstructorBuilder.exportReportInventoryTOBuilder("rc", "i", "r", "ro", "o", "oo", "grc")
					+ " FROM " + getEntityName() + " rg" + " INNER JOIN rg." + ReportingGroup.FIELD_GROUP_REPORT_CONFIGS
					+ " grc" + " INNER JOIN rg." + ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS + " oo"
					+ " INNER JOIN oo." + OfferOption.FIELD_RELATED_0FFER + " o" + " INNER JOIN grc."
					+ GroupReportConfig.FIELD_REPORT_CONFIG + " rc" + " INNER JOIN rc."
					+ ReportConfig.FIELD_ASSOCIATED_REPORT + " r" + " INNER JOIN grc."
					+ GroupReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " ro" + " INNER JOIN rc."
					+ ReportConfig.FIELD_INDICATORS + " i" + " WHERE rg." + ReportingGroup.FIELD_ORIGIN + "=:origin"
					+ " AND rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef" + " AND grc."
					+ GroupReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " IS NOT NULL" + " AND oo."
					+ OfferOption.FIELD_TYPE + "='" + OfferOptionTypeEnum.INTERACTIVE + "'" + " AND oo."
					+ OfferOption.FIELD_ALIAS + " IN (" + offerOptionsNewProv + ")" + " AND rc MEMBER OF oo."
					+ OfferOption.FIELD_REPORTCONFIGS;

			secondQuery = getEntityManager().createQuery(secondJpqlQuery);
			secondQuery.setParameter("origin", origin);
			secondQuery.setParameter("reportingGroupRef", reportingGroupRef);

			result.addAll(secondQuery.getResultList());
		}

		return result;
	}

	@Override
	public List<ExportInventoryReportTemplateTO> findExportInventoryReportTemplateTOForReportingGroupWithOption(
			String origin, String reportingGroupRef) {

		List<ExportInventoryReportTemplateTO> result;

		String firstJpqlQuery = "SELECT NEW "
				+ JPATOConstructorBuilder.exportReportInventoryTemplateTOBuilder("rc", "r", "ro", "o", "oo", "grc")
				+ " FROM " + getEntityName() + " rg" + " INNER JOIN rg." + ReportingGroup.FIELD_GROUP_REPORT_CONFIGS
				+ " grc" + " INNER JOIN rg." + ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS + " oo" + " INNER JOIN oo."
				+ OfferOption.FIELD_RELATED_0FFER + " o" + " INNER JOIN grc." + GroupReportConfig.FIELD_REPORT_CONFIG
				+ " rc" + " INNER JOIN rc." + ReportConfig.FIELD_ASSOCIATED_REPORT + " r" + " INNER JOIN rc."
				+ ReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " ro" + " WHERE rg." + ReportingGroup.FIELD_ORIGIN
				+ "=:origin" + " AND rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef"
				+ " AND grc." + GroupReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " IS NULL" + " AND (oo."
				+ OfferOption.FIELD_TYPE + "='" + OfferOptionTypeEnum.DOCUMENT + "'" + " OR (oo."
				+ OfferOption.FIELD_TYPE + "='" + OfferOptionTypeEnum.BATCH + "' AND ro." + ReportOutput.FIELD_TYPE
				+ "='" + ReportOutputTypeEnum.TEMPLATE + "')" + " ) AND rc MEMBER OF oo."
				+ OfferOption.FIELD_REPORTCONFIGS;

		Query firstQuery = getEntityManager().createQuery(firstJpqlQuery);
		firstQuery.setParameter("origin", origin);
		firstQuery.setParameter("reportingGroupRef", reportingGroupRef);

		result = firstQuery.getResultList();

		String secondJpqlQuery = "SELECT NEW "
				+ JPATOConstructorBuilder.exportReportInventoryTemplateTOBuilder("rc", "r", "ro", "o", "oo", "grc")
				+ " FROM " + getEntityName() + " rg" + " INNER JOIN rg." + ReportingGroup.FIELD_GROUP_REPORT_CONFIGS
				+ " grc" + " INNER JOIN rg." + ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS + " oo" + " INNER JOIN oo."
				+ OfferOption.FIELD_RELATED_0FFER + " o" + " INNER JOIN grc." + GroupReportConfig.FIELD_REPORT_CONFIG
				+ " rc" + " INNER JOIN rc." + ReportConfig.FIELD_ASSOCIATED_REPORT + " r" + " INNER JOIN grc."
				+ GroupReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " ro" + " WHERE rg." + ReportingGroup.FIELD_ORIGIN
				+ "=:origin" + " AND rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef"
				+ " AND grc." + GroupReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT + " IS NOT NULL" + " AND (oo."
				+ OfferOption.FIELD_TYPE + "='" + OfferOptionTypeEnum.DOCUMENT + "'" + " OR (oo."
				+ OfferOption.FIELD_TYPE + "='" + OfferOptionTypeEnum.BATCH + "' AND ro." + ReportOutput.FIELD_TYPE
				+ "='" + ReportOutputTypeEnum.TEMPLATE + "')" + " ) AND rc MEMBER OF oo."
				+ OfferOption.FIELD_REPORTCONFIGS;

		Query secondQuery = getEntityManager().createQuery(secondJpqlQuery);
		secondQuery.setParameter("origin", origin);
		secondQuery.setParameter("reportingGroupRef", reportingGroupRef);

		result.addAll(secondQuery.getResultList());

		return result;
	}

	@Override
	public List<FilterToOfferOptionTO> findOptionAliasAndFilterIdForReportingGroup(String origin,
			String reportingGroupRef) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.filterToOfferOptionTOBuilder("oo", "f") + " FROM "
				+ getEntityName() + " rg" + " JOIN rg." + ReportingGroup.FIELD_FILTER_CONFIGS + " fc" + " JOIN fc."
				+ FilterConfig.FIELD_FILTER + " f" + " JOIN fc." + FilterConfig.FIELD_OFFER_OPTION + " oo"
				+ " WHERE rg." + ReportingGroup.FIELD_ORIGIN + "=:origin" + " AND rg."
				+ ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef";

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("origin", origin);
		query.setParameter("reportingGroupRef", reportingGroupRef);
		return query.getResultList();
	}

	@Override
	public List<String> findOptionAliasForReportingGroup(String origin, String reportingGroupRef) {

		String jpqlQuery = "SELECT oo." + OfferOption.FIELD_ALIAS + " FROM " + getEntityName() + " rg, "
				+ ModelUtils.getEntityNameForClass(OfferOption.class) + " oo " + " WHERE rg."
				+ ReportingGroup.FIELD_REPORTING_GROUP_REF + "=:reportingGroupRef" + " AND rg."
				+ ReportingGroup.FIELD_ORIGIN + "=:origin" + " AND oo MEMBER OF rg."
				+ ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("reportingGroupRef", reportingGroupRef);
		query.setParameter("origin", origin);

		return query.getResultList();
	}

	@Override
	public List<ReportingGroupFilterUriTO> findReportingGroupAndFilterUriForOption(String optionAlias) {

		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportingGroupFilterUriTOTOBuilder("rg", "f")
				+ " FROM " + ModelUtils.getEntityNameForClass(OfferOption.class) + " oo, "
				+ ModelUtils.getEntityNameForClass(FilterConfig.class) + " fc" + " JOIN fc."
				+ FilterConfig.FIELD_REPORTING_GROUP + " rg" + " JOIN fc." + FilterConfig.FIELD_FILTER + " f"
				+ " WHERE oo." + OfferOption.FIELD_ALIAS + "=:optionAlias " + " AND fc MEMBER OF oo."
				+ OfferOption.FIELD_FILTER_CONFIGS;

		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setParameter("optionAlias", optionAlias);
		return query.getResultList();
	}

	@Override
	public List<Object[]> findAllReportingGroupPkAndTypeSubtypeValues() {
		String jpqlQuery = "SELECT rg." + ReportingGroup.FIELD_PK + ", tst." + EntityTypeAndSubtype.FIELD_TYPE
				+ ", tst." + EntityTypeAndSubtype.FIELD_SUBTYPE + " FROM " + getEntityName() + " rg" + " INNER JOIN rg."
				+ ReportingGroup.FIELD_ENTITIES + " re" + " INNER JOIN re." + ReportingEntity.FIELD_SUBTYPES + " tst";
		Query query = getEntityManager().createQuery(jpqlQuery);

		return query.getResultList();
	}

	@Override
	public List<Long> findAllReportingGroupPk() {
		String jpqlQuery = "SELECT rg." + ReportingGroup.FIELD_PK + " FROM " + getEntityName() + " rg ORDER BY "
				+ ReportingGroup.FIELD_PK;
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}

	@Override
	public Long getLastPkValue() {
		String jpqlQuery = "SELECT rg." + ReportingGroup.FIELD_PK + " FROM " + getEntityName() + " rg" + " ORDER BY rg."
				+ ReportingGroup.FIELD_PK + " DESC";
		Query query = getEntityManager().createQuery(jpqlQuery);
		query.setMaxResults(1);
		Long lastPkValue = 0L;
		try {
			lastPkValue = (Long) query.getSingleResult();
		} catch (NoResultException nre) {
			lastPkValue = 0L;
		}
		return lastPkValue;
	}

	@Override
	public List<ReportingGroupWithOfferOptionTO> findAllReportingGroupAndOfferOptionByOfferOptionType(
			List<OfferOptionTypeEnum> offerOptionTypes) {
		String jpqlQuery = "SELECT NEW " + JPATOConstructorBuilder.reportingGroupWithOfferOptionTOBuilder("rg", "oo")
				+ " FROM " + getEntityName() + " rg, " + ModelUtils.getEntityNameForClass(OfferOption.class) + " oo "
				+ " WHERE oo MEMBER OF rg." + ReportingGroup.FIELD_REPORT_SOURCE_OPTIONS;
		if (offerOptionTypes.size() > 0) {
			jpqlQuery += " AND oo." + OfferOption.FIELD_TYPE + " IN :offerOptionTypes";
		}

		jpqlQuery += " ORDER BY rg." + ReportingGroup.FIELD_REPORTING_GROUP_REF + ", oo." + OfferOption.FIELD_ALIAS;

		Query query = getEntityManager().createQuery(jpqlQuery);
		if (offerOptionTypes.size() > 0) {
			query.setParameter("offerOptionTypes", offerOptionTypes);
		}
		return query.getResultList();
	}

	@Override
	public List<ReportingGroup> findReportingGroupWithoutOfferOption() {
		String jpqlQuery = "SELECT rg " + " FROM " + getEntityName() + " rg " + " WHERE NOT EXISTS (" + " 	SELECT 1 "
				+ "    FROM " + ModelUtils.getEntityNameForClass(OfferOption.class) + " oo "
				+ "    WHERE rg MEMBER OF oo." + OfferOption.FIELD_REPORTING_GROUPS + ")";
		Query query = getEntityManager().createQuery(jpqlQuery);
		return query.getResultList();
	}
}

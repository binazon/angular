package com.orange.srs.refreport.consumer.dao;

import java.util.List;

import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.FilterToOfferOptionTO;
import com.orange.srs.refreport.model.TO.ReportingGroupFilterUriTO;
import com.orange.srs.refreport.model.TO.ReportingGroupTO;
import com.orange.srs.refreport.model.TO.ReportingGroupWithOfferOptionTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportTemplateTO;
import com.orange.srs.refreport.model.TO.inventory.OfferOptionListNewProvisioningTO;
import com.orange.srs.statcommon.model.TO.ReportingGroupKeyTO;
import com.orange.srs.statcommon.model.TO.ReportingGroupLocationTO;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;

public interface ReportingGroupDAO extends Dao<ReportingGroup, Long> {

	public List<ReportingGroupKeyTO> findAllReportingGroupsKeys();

	public List<ReportingGroupKeyTO> findAllReportingGroupsKeysWithSourceColumn();

	public List<ReportingGroupLocationTO> findLocationForOption(String optionAlias);

	public List<ReportingGroupTO> findAllReportingGroupTO(String groupingRuleSeparator, String groupingValueSeparator);

	public List<ReportingGroup> findReportingGroupForOfferOption(String offerOptionAlias);

	public List<Object[]> findReportingGroupKeysByOfferOption(List<String> origins, List<String> offerOptionAliases);

	public List<ReportingGroupTO> findReportingGroupTOByMultipleCriteria(String[] attributes, Object[] values,
			String groupingRuleSeparator, String groupingValueSeparator);

	public List<ReportingGroupTO> findReportingGroupTOByOfferOption(String optionAlias, String groupingRuleSeparator,
			String groupingValueSeparator);

	public List<ExportInventoryReportTO> findExportInventoryReportTOForReportingGroupWithOption(String origin,
			String reportingGroupRef, OfferOptionListNewProvisioningTO offerOptionListNewProvisioningTO);

	public List<ExportInventoryReportTemplateTO> findExportInventoryReportTemplateTOForReportingGroupWithOption(
			String origin, String reportingGroupRef);

	public List<FilterToOfferOptionTO> findOptionAliasAndFilterIdForReportingGroup(String origin,
			String reportingGroupRef);

	public List<String> findOptionAliasForReportingGroup(String origin, String reportingGroupRef);

	public List<ReportingGroupFilterUriTO> findReportingGroupAndFilterUriForOption(String optionAlias);

	public List<Object[]> findAllReportingGroupPkAndTypeSubtypeValues();

	public List<ReportingGroupWithOfferOptionTO> findAllReportingGroupAndOfferOptionByOfferOptionType(
			List<OfferOptionTypeEnum> offerOptionTypes);

	public Long getLastPkValue();

	public List<ReportingGroup> findReportingGroupWithoutOfferOption();

	public List<Long> findAllReportingGroupPk();
}

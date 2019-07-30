package com.orange.srs.refreport.consumer.dao;

import com.orange.srs.refreport.model.BookmarkDirectReport;
import com.orange.srs.refreport.model.Criteria;
import com.orange.srs.refreport.model.DataLocation;
import com.orange.srs.refreport.model.EntityAttribute;
import com.orange.srs.refreport.model.EntityGroupAttribute;
import com.orange.srs.refreport.model.EntityLink;
import com.orange.srs.refreport.model.EntityLinkAttribute;
import com.orange.srs.refreport.model.ExternalIndicator;
import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.FilterConfig;
import com.orange.srs.refreport.model.FilterToOfferOption;
import com.orange.srs.refreport.model.GroupAttribute;
import com.orange.srs.refreport.model.GroupReportConfig;
import com.orange.srs.refreport.model.Hyperlink;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.InputColumn;
import com.orange.srs.refreport.model.InputFormat;
import com.orange.srs.refreport.model.Offer;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.Proxy;
import com.orange.srs.refreport.model.Report;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.ReportOutput;
import com.orange.srs.refreport.model.ReportUser;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.EntityDataLocationTO;
import com.orange.srs.refreport.model.TO.FilterConfigTO;
import com.orange.srs.refreport.model.TO.FilterToOfferOptionTO;
import com.orange.srs.refreport.model.TO.GroupReportConfigTO;
import com.orange.srs.refreport.model.TO.ReportConfigIndicatorIdTO;
import com.orange.srs.refreport.model.TO.ReportConfigParamTypeAliasTO;
import com.orange.srs.refreport.model.TO.ReportRefIdAndOfferOptionTO;
import com.orange.srs.refreport.model.TO.ReportUserTO;
import com.orange.srs.refreport.model.TO.ReportingEntityPartitionTO;
import com.orange.srs.refreport.model.TO.ReportingGroupFilterUriTO;
import com.orange.srs.refreport.model.TO.ReportingGroupWithOfferOptionTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportTO;
import com.orange.srs.refreport.model.TO.inventory.ExportInventoryReportTemplateTO;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryGkTO;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryOpenFlowTO;
import com.orange.srs.refreport.model.TO.inventory.ExportSpecificInventoryPaiTO;
import com.orange.srs.refreport.model.TO.provisioning.BookmarkDirectReportProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.CriteriaProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ExternalIndicatorProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.FilterProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.HyperlinkProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.InputColumnProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ProxyProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportConfigProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportProvisioningTO;
import com.orange.srs.refreport.model.parameter.EntityToEntityAttributeParameter;
import com.orange.srs.refreport.model.parameter.GroupEntityAttributeParameter;
import com.orange.srs.statcommon.model.TO.FilterTO;
import com.orange.srs.statcommon.model.TO.ParamTypeTO;
import com.orange.srs.statcommon.model.TO.ReportInputTO;
import com.orange.srs.statcommon.model.TO.ReportingGroupKeyTO;
import com.orange.srs.statcommon.model.TO.ReportingGroupLocationTO;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;
import com.orange.srs.statcommon.model.TO.rest.ExternalIndicatorTO;
import com.orange.srs.statcommon.model.parameter.GroupAttributeParameter;
import com.orange.srs.statcommon.technical.ModelUtils;

public class JPATOConstructorBuilder {

	private static final Character JOIN_CHAR = '.';

	public static String reportInputKeyTOListBuilder(String entityName) {
		return ModelUtils.buildJPATOConstructorArguments(ReportInputKeyTO.class, entityName, ReportInput.FIELD_PK,
				ReportInput.FIELD_REPORT_INPUT_REF, ReportInput.FIELD_GRANULARITY, ReportInput.FIELD_SOURCE_TIME_UNIT);
	}

	public static String reportingEntityPartitionsTOBuilder(String entity) {
		return ModelUtils.buildJPATOConstructorArguments(ReportingEntityPartitionTO.class, entity,
				ReportingEntity.FIELD_PK, ReportingEntity.FIELD_ENTITYID, ReportingEntity.FIELD_ORIGIN,
				ReportingEntity.FIELD_ENTITYTYPE, ReportingEntity.FIELD_PARTITION_NUMBER);
	}

	public static String reportingGroupKeyTOBuilder(String entityName) {
		return ModelUtils.buildJPATOConstructorArguments(ReportingGroupKeyTO.class, entityName,
				ReportingGroup.FIELD_REPORTING_GROUP_REF, ReportingGroup.FIELD_ORIGIN);
	}

	public static String reportingGroupKeyWithSourceColumnTOBuilder(String entityName) {
		return ModelUtils.buildJPATOConstructorArguments(ReportingGroupKeyTO.class, entityName,
				ReportingGroup.FIELD_REPORTING_GROUP_REF, ReportingGroup.FIELD_ORIGIN, ReportingGroup.FIELD_SOURCE);
	}

	public static String groupEntityAttributeParameterBuilder(String attribut, String reportingGroup,
			String reportingEntity) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(GroupEntityAttributeParameter.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroup, true,
				ReportingGroup.FIELD_ORIGIN, ReportingGroup.FIELD_REPORTING_GROUP_REF));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingEntity, false,
				ReportingEntity.FIELD_ENTITYID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(attribut, false,
				EntityGroupAttribute.FIELD_NAME, EntityGroupAttribute.FIELD_VALUE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String entityToEntityAttributeParameterBuilder(String attribut, String reportingEntityOrigin,
			String reportingEntityEnd, String link) {
		StringBuilder response = ModelUtils
				.buildJPATOConstructorArgumentsStarter(EntityToEntityAttributeParameter.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingEntityOrigin, true,
				ReportingEntity.FIELD_ORIGIN, ReportingEntity.FIELD_ENTITYID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingEntityEnd, false,
				ReportingEntity.FIELD_ENTITYID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(attribut, false,
				EntityLinkAttribute.FIELD_NAME, EntityLinkAttribute.FIELD_VALUE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(link, false, EntityLink.FIELD_TYPE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String groupAttributeParameterBuilder(String attribut, String reportingGroup) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(GroupAttributeParameter.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroup, true,
				ReportingGroup.FIELD_ORIGIN, ReportingGroup.FIELD_REPORTING_GROUP_REF));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(attribut, false, GroupAttribute.FIELD_NAME,
				GroupAttribute.FIELD_VALUE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String reportingGroupLocationTOBuilder(String entityName) {
		return ModelUtils.buildJPATOConstructorArguments(ReportingGroupLocationTO.class, entityName,
				ReportingGroup.FIELD_PK,
				ReportingGroup.FIELD_DATA_LOCATION + JOIN_CHAR + DataLocation.FIELD_LOCATION_PATTERN,
				ReportingGroup.FIELD_ORIGIN, ReportingGroup.FIELD_LANGUAGE, ReportingGroup.FIELD_TIME_ZONE);
	}

	public static String reportInputTOBuilder(String reportInputName, String inputFormatName) {

		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ReportInputTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportInputName, true,
				ReportInput.FIELD_REPORT_INPUT_REF, ReportInput.FIELD_LOCATION_PATTERN_PREFIX,
				ReportInput.FIELD_LOCATION_PATTERN_SUFFIX, ReportInput.FIELD_SOURCE, ReportInput.FIELD_GRANULARITY,
				ReportInput.FIELD_SOURCE_TIME_UNIT));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(inputFormatName, false,
				InputFormat.FIELD_FORMAT_TYPE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String exportReportInventoryTOBuilder(String reportConfigName, String indicatorName,
			String reportName, String reportOutputName, String offerName, String offerOptionName,
			String groupReportConfigName) {

		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ExportInventoryReportTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportConfigName, true,
				ReportConfig.FIELD_PK, ReportConfig.FIELD_COMPUTE_SCOPE));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(indicatorName, false, Indicator.FIELD_INDICATORID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportName, false, Report.FIELD_REFID,
				Report.FIELD_REPORT_TIME_UNIT, Report.FIELD_GRANULARITY, Report.FIELD_COMPUTE_URI));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportOutputName, false,
				ReportOutput.FIELD_LOCATION_PATTERN_PREFIX, ReportOutput.FIELD_LOCATION_PATTERN_SUFFIX,
				ReportOutput.FIELD_URI, ReportOutput.FIELD_TYPE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(offerName, false, Offer.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(offerOptionName, false,
				OfferOption.FIELD_ALIAS, OfferOption.FIELD_LABEL));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(groupReportConfigName, false,
				GroupReportConfig.FIELD_REPORT_VERSION));
		String criteriaName = groupReportConfigName + JOIN_CHAR + GroupReportConfig.FIELD_CRITERIA;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(criteriaName, false,
				Criteria.FIELD_CRITERIA_TYPE, Criteria.FIELD_CRITERIA_VALUE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String exportReportInventoryTemplateTOBuilder(String reportConfigName, String reportName,
			String reportOutputName, String offerName, String offerOptionName, String groupReportConfigName) {

		StringBuilder response = ModelUtils
				.buildJPATOConstructorArgumentsStarter(ExportInventoryReportTemplateTO.class);
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(reportConfigName, true, ReportConfig.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportName, false, Report.FIELD_REFID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportOutputName, false,
				ReportOutput.FIELD_LOCATION_PATTERN_PREFIX, ReportOutput.FIELD_LOCATION_PATTERN_SUFFIX,
				ReportOutput.FIELD_TYPE, ReportOutput.FIELD_FORMAT));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(offerName, false, Offer.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(offerOptionName, false,
				OfferOption.FIELD_ALIAS, OfferOption.FIELD_LABEL));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(groupReportConfigName, false,
				GroupReportConfig.FIELD_REPORT_VERSION));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String paramTypeTOBuilder(String entityName) {
		return ModelUtils.buildJPATOConstructorArguments(ParamTypeTO.class, entityName, ParamType.FIELD_ENTITY_TYPE,
				ParamType.FIELD_ENTITY_SUBTYPE, ParamType.FIELD_PARENT_TYPE, ParamType.FIELD_PARENT_SUBTYPE,
				ParamType.FIELD_ALIAS);
	}

	public static String reportConfigParamTypeAliasTOBuilder(String reportConfigName, String paramTypeName) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ReportConfigParamTypeAliasTO.class);
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(reportConfigName, true, ReportConfig.FIELD_PK));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(paramTypeName, false, ParamType.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String filterToOfferOptionTOBuilder(String offerOptionName, String filterName) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(FilterToOfferOptionTO.class);
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(offerOptionName, true, OfferOption.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(filterName, false, Filter.FIELD_FILTERID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String filterToOfferOptionTOBuilderFromLink(String filterToOfferOptionName) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(FilterToOfferOptionTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(
				filterToOfferOptionName + '.' + FilterToOfferOption.FIELD_OFFER_OPTION, true, OfferOption.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(
				filterToOfferOptionName + '.' + FilterToOfferOption.FIELD_FILTER, false, Filter.FIELD_FILTERID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(filterToOfferOptionName, false,
				FilterToOfferOption.FIELD_DEFAULT_FOR_ALL_GROUPS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String reportUserTOBuilder(String reportUser) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ReportUserTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportUser, true, ReportUser.FIELD_PK));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String criteriaProvisioningTOBuilder(String criteria) {
		return ModelUtils.buildJPATOConstructorArguments(CriteriaProvisioningTO.class, criteria,
				Criteria.FIELD_CRITERIA_TYPE, Criteria.FIELD_CRITERIA_VALUE);
	}

	public static String paramTypeProvisioningTOBuilder(String paramType) {
		return ModelUtils.buildJPATOConstructorArguments(ParamTypeProvisioningTO.class, paramType,
				ParamType.FIELD_ALIAS, ParamType.FIELD_ENTITY_TYPE, ParamType.FIELD_ENTITY_SUBTYPE,
				ParamType.FIELD_PARENT_TYPE, ParamType.FIELD_PARENT_SUBTYPE);
	}

	public static String reportConfigProvisioningTOBuilder(String reportConfig) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ReportConfigProvisioningTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportConfig, true, ReportConfig.FIELD_PK,
				ReportConfig.FIELD_ALIAS, ReportConfig.FIELD_TYPE, ReportConfig.FIELD_REPORT_DEFAULT_VERSION,
				ReportConfig.FIELD_COMPUTE_SCOPE, ReportConfig.FIELD_OPTIONAL));
		String report = reportConfig + JOIN_CHAR + ReportConfig.FIELD_ASSOCIATED_REPORT;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(report, false, Report.FIELD_REFID));
		String reportOutput = reportConfig + JOIN_CHAR + ReportConfig.FIELD_ASSOCIATED_REPORT_OUTPUT;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportOutput, false,
				ReportOutput.FIELD_TYPE, ReportOutput.FIELD_FORMAT, ReportOutput.FIELD_LOCATION_PATTERN_PREFIX,
				ReportOutput.FIELD_LOCATION_PATTERN_SUFFIX, ReportOutput.FIELD_URI, ReportOutput.FIELD_COMPRESSION));
		String criteria = reportConfig + JOIN_CHAR + ReportConfig.FIELD_CRITERIA;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(criteria, false,
				Criteria.FIELD_CRITERIA_TYPE, Criteria.FIELD_CRITERIA_VALUE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String indicatorProvisioningTOBuilder(String indicator) {
		return ModelUtils.buildJPATOConstructorArguments(IndicatorProvisioningTO.class, indicator,
				Indicator.FIELD_INDICATORID, Indicator.FIELD_LABEL);
	}

	public static String reportConfigIndicatorIdTOBuilder(String reportConfig, String indicator) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ReportConfigIndicatorIdTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportConfig, true, ReportConfig.FIELD_PK));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(indicator, false, Indicator.FIELD_INDICATORID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String groupReportConfigTOBuilder(String groupReportConfig, String reportingGroup,
			String reportConfig, String offerOption) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(GroupReportConfigTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(groupReportConfig, true,
				GroupReportConfig.FIELD_PK));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroup, false, ReportingGroup.FIELD_PK));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(reportConfig, false, ReportConfig.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(offerOption, false, OfferOption.FIELD_PK));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String reportProvisioningTOBuilder(String report) {
		return ModelUtils.buildJPATOConstructorArguments(ReportProvisioningTO.class, report, Report.FIELD_PK,
				Report.FIELD_REFID, Report.FIELD_LABEL, Report.FIELD_COMPUTE_URI, Report.FIELD_REPORT_TIME_UNIT,
				Report.FIELD_GRANULARITY);
	}

	public static String reportInputProvisioningTOBuilder(String reportInput) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ReportInputProvisioningTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportInput, true,
				ReportInput.FIELD_REPORT_INPUT_REF, ReportInput.FIELD_GRANULARITY, ReportInput.FIELD_SOURCE_TIME_UNIT,
				ReportInput.FIELD_LOCATION_PATTERN_PREFIX, ReportInput.FIELD_LOCATION_PATTERN_SUFFIX,
				ReportInput.FIELD_SOURCE, ReportInput.FIELD_TYPEDB, ReportInput.FIELD_TABLEDB));
		String inputFormat = reportInput + JOIN_CHAR + ReportInput.FIELD_FORMAT;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(inputFormat, false, InputFormat.FIELD_FORMAT_TYPE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String inputColumnProvisioningTOBuilder(String inputColumn) {
		return ModelUtils.buildJPATOConstructorArguments(InputColumnProvisioningTO.class, inputColumn,
				InputColumn.FIELD_COLUMN_NAME, InputColumn.FIELD_ALIAS, InputColumn.FIELD_DATE_FORMAT,
				InputColumn.FIELD_TYPE, InputColumn.FIELD_DEFAULT_VALUE, InputColumn.FIELD_COMMENTS);
	}

	public static String proxyProvisioningTOBuilder(String proxy) {
		return ModelUtils.buildJPATOConstructorArguments(ProxyProvisioningTO.class, proxy, Proxy.FIELD_URI,
				Proxy.FIELD_NAME, Proxy.FIELD_VERSION, Proxy.FIELD_ISSSL);
	}

	public static String hyperlinkProvisioningTOBuilder(String hyperlink, String additionalParamType) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(HyperlinkProvisioningTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(hyperlink, true, Hyperlink.FIELD_LABEL));
		String indicator = hyperlink + JOIN_CHAR + Hyperlink.FIELD_INDICATOR;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(indicator, false, Indicator.FIELD_INDICATORID));
		String offerOption = hyperlink + JOIN_CHAR + Hyperlink.FIELD_OFFER_OPTION;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(offerOption, false, OfferOption.FIELD_ALIAS));
		String paramType = hyperlink + JOIN_CHAR + Hyperlink.FIELD_PARAM_TYPE;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(paramType, false, ParamType.FIELD_ALIAS));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(additionalParamType, false, ParamType.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String bookmarkDirectReportProvisioningTOBuilder(String bookmark, String additionalParamType) {
		StringBuilder response = ModelUtils
				.buildJPATOConstructorArgumentsStarter(BookmarkDirectReportProvisioningTO.class);
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(bookmark, true, BookmarkDirectReport.FIELD_LABEL));
		String indicator = bookmark + JOIN_CHAR + BookmarkDirectReport.FIELD_INDICATOR;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(indicator, false, Indicator.FIELD_INDICATORID));
		String offerOption = bookmark + JOIN_CHAR + BookmarkDirectReport.FIELD_OFFER_OPTION;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(offerOption, false, OfferOption.FIELD_ALIAS));
		String paramType = bookmark + JOIN_CHAR + BookmarkDirectReport.FIELD_PARAM_TYPE;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(paramType, false, ParamType.FIELD_ALIAS));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(additionalParamType, false, ParamType.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(bookmark, false,
				BookmarkDirectReport.FIELD_HIERARCHY));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String externalIndicatorProvisioningTOBuilder(String externalIndicator) {
		StringBuilder response = ModelUtils
				.buildJPATOConstructorArgumentsStarter(ExternalIndicatorProvisioningTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(externalIndicator, true,
				ExternalIndicator.FIELD_LABEL, ExternalIndicator.FIELD_COMPUTE_SCOPE));
		String indicator = externalIndicator + JOIN_CHAR + ExternalIndicator.FIELD_INDICATOR;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(indicator, false, Indicator.FIELD_INDICATORID));
		String offerOption = externalIndicator + JOIN_CHAR + ExternalIndicator.FIELD_OFFER_OPTION;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(offerOption, false, OfferOption.FIELD_ALIAS));
		String paramType = externalIndicator + JOIN_CHAR + ExternalIndicator.FIELD_PARAM_TYPE;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(paramType, false, ParamType.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String externalIndicatorTOBuilder(String externalIndicator) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ExternalIndicatorTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(externalIndicator, true,
				ExternalIndicator.FIELD_LABEL, ExternalIndicator.FIELD_COMPUTE_SCOPE));
		String indicator = externalIndicator + JOIN_CHAR + ExternalIndicator.FIELD_INDICATOR;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(indicator, false, Indicator.FIELD_INDICATORID));
		String offerOption = externalIndicator + JOIN_CHAR + ExternalIndicator.FIELD_OFFER_OPTION;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(offerOption, false, OfferOption.FIELD_ALIAS));
		String paramType = externalIndicator + JOIN_CHAR + ExternalIndicator.FIELD_PARAM_TYPE;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(paramType, false, ParamType.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String filterProvisioningTOBuilder(String filter) {
		return ModelUtils.buildJPATOConstructorArguments(FilterProvisioningTO.class, filter, Filter.FIELD_FILTERID,
				Filter.FIELD_URI, Filter.FIELD_TYPE, Filter.FIELD_NAME, Filter.FIELD_COMMENTS, Filter.FIELD_VALUE);
	}

	public static String filterTOBuilder(String entityName) {
		return ModelUtils.buildJPATOConstructorArguments(FilterTO.class, entityName, Filter.FIELD_FILTERID,
				Filter.FIELD_NAME, Filter.FIELD_COMMENTS, Filter.FIELD_URI, Filter.FIELD_TYPE, Filter.FIELD_VALUE);
	}

	public static String reportingGroupFilterUriTOTOBuilder(String reportingGroup, String filter) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ReportingGroupFilterUriTO.class);
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroup, true, ReportingGroup.FIELD_PK));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(filter, false, Filter.FIELD_URI));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String reportRefIdAndOfferOptionTOBuilder(String report, String offerOption, String reportOutput) {

		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ReportRefIdAndOfferOptionTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(report, true, Report.FIELD_REFID,
				Report.FIELD_GRANULARITY));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(reportOutput, false, ReportOutput.FIELD_TYPE));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(offerOption, false, OfferOption.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String reportingGroupWithOfferOptionTOBuilder(String reportingGroup, String offerOption) {
		StringBuilder response = ModelUtils
				.buildJPATOConstructorArgumentsStarter(ReportingGroupWithOfferOptionTO.class);
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroup, true, ReportingGroup.FIELD_PK));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroup, false,
				ReportingGroup.FIELD_REPORTING_GROUP_REF));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroup, false,
				ReportingGroup.FIELD_ORIGIN));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(offerOption, false, OfferOption.FIELD_ALIAS));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String filterConfigTOBuilder(String filterConfigAlias) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(FilterConfigTO.class);
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(filterConfigAlias, true, FilterConfig.FIELD_PK));
		String filterAlias = filterConfigAlias + JOIN_CHAR + FilterConfig.FIELD_FILTER;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(filterAlias, false, Filter.FIELD_FILTERID));
		String offerOptionAlias = filterConfigAlias + JOIN_CHAR + FilterConfig.FIELD_OFFER_OPTION;
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(offerOptionAlias, false, OfferOption.FIELD_PK));
		String reportingGroupAlias = filterConfigAlias + JOIN_CHAR + FilterConfig.FIELD_REPORTING_GROUP;
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroupAlias, false,
				ReportingGroup.FIELD_PK));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String entityDomainDataLocationTOBuilder(String reportingEntity, String attribute) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(EntityDataLocationTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingEntity, true,
				ReportingEntity.FIELD_PK, ReportingEntity.FIELD_ORIGIN));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(attribute, false, EntityAttribute.FIELD_VALUE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String entityIdAndDomainDataLocationTOBuilder(String reportingEntity, String attribute) {
		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(EntityDataLocationTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingEntity, true,
				ReportingEntity.FIELD_PK, ReportingEntity.FIELD_ENTITYID, ReportingEntity.FIELD_ORIGIN));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(attribute, false, EntityAttribute.FIELD_VALUE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String exportOpenFlowInventoryTOBuilder(String reportingEntity, String attribute) {

		StringBuilder response = ModelUtils
				.buildJPATOConstructorArgumentsStarter(ExportSpecificInventoryOpenFlowTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingEntity, true,
				ReportingEntity.FIELD_ENTITYID));
		response.append(
				ModelUtils.buildJPATOConstructorArgumentsParameters(attribute, false, EntityAttribute.FIELD_VALUE));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String exportPaiInventoryTOBuilder(String reportingEntity, String reportingGroup) {

		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ExportSpecificInventoryPaiTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingEntity, true,
				ReportingEntity.FIELD_ENTITYID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroup, false,
				ReportingGroup.FIELD_REPORTING_GROUP_REF));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingGroup, false,
				ReportingGroup.FIELD_ORIGIN));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}

	public static String exportGkInventoryTOBuilder(String reportingEntity) {

		StringBuilder response = ModelUtils.buildJPATOConstructorArgumentsStarter(ExportSpecificInventoryGkTO.class);
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingEntity, true,
				ReportingEntity.FIELD_ENTITYID));
		response.append(ModelUtils.buildJPATOConstructorArgumentsParameters(reportingEntity, false,
				ReportingEntity.FIELD_LABEL));
		response.append(ModelUtils.buildJPATOConstructorArgumentsEnder());
		return response.toString();
	}
}

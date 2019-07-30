package com.orange.srs.refreport.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.delegate.GroupReportConfigDelegate;
import com.orange.srs.refreport.business.delegate.OfferOptionDelegate;
import com.orange.srs.refreport.business.delegate.ReportingGroupDelegate;
import com.orange.srs.refreport.consumer.dao.GroupReportConfigDAO;
import com.orange.srs.refreport.consumer.dao.OfferOptionDAO;
import com.orange.srs.refreport.consumer.dao.ReportConfigDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.model.GroupReportConfig;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.ProvisioningActionStatusTO;
import com.orange.srs.refreport.model.TO.TOBuilder;
import com.orange.srs.refreport.model.external.OfferOptionTOList;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refura.model.external.InternalCredentialTypeEnum;
import com.orange.srs.statcommon.model.TO.BatchConfigTO;
import com.orange.srs.statcommon.model.TO.GetBatchReportOptimizedTO;
import com.orange.srs.statcommon.model.TO.GetBatchReportOptimizedTOList;
import com.orange.srs.statcommon.model.TO.PerimetersByCredentialTO;
import com.orange.srs.statcommon.model.TO.PerimetersByCredentialTOList;
import com.orange.srs.statcommon.model.TO.ReportingGroupKeyTO;
import com.orange.srs.statcommon.model.TO.ReportingGroupLocationTO;
import com.orange.srs.statcommon.model.TO.ReportingGroupLocationTOList;
import com.orange.srs.statcommon.model.enums.OfferOptionTypeEnum;
import com.orange.srs.statcommon.model.enums.OriginEnum;
import com.orange.srs.statcommon.model.parameter.EntityTypeParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.interceptor.Logged;

/**
 * Business Facade regarding every operations on business objects involved in catalog description
 * 
 * @author A159138
 */
@Logged
@Stateless
public class SOA03CatalogFacade {

	private static final Logger LOGGER = Logger.getLogger(SOA03CatalogFacade.class);

	@EJB
	private OfferOptionDelegate offerOptionDelegate;

	@EJB
	private GroupReportConfigDelegate groupReportConfigDelegate;

	@EJB
	private ReportingGroupDelegate reportingGroupDelegate;

	@EJB
	private ReportingGroupDAO reportingGroupDAO;

	@EJB
	private GroupReportConfigDAO groupReportConfigDAO;

	@EJB
	private ReportConfigDAO reportConfigDAO;

	@EJB
	private OfferOptionDAO offerOptionDAO;

	public OfferOptionTOList getAllOfferOptions() {
		OfferOptionTOList offerOptionTOList = new OfferOptionTOList();
		offerOptionTOList.offerOptionTOs = offerOptionDelegate.getAllOfferOptionTO();
		return offerOptionTOList;
	}

	public OfferOptionTOList getOfferOptionsByTypes(List<OfferOptionTypeEnum> types) {
		OfferOptionTOList offerOptionTOList = new OfferOptionTOList();
		offerOptionTOList.offerOptionTOs = offerOptionDelegate.getOfferOptionTOsByTypes(types);
		return offerOptionTOList;
	}

	public ReportingGroupLocationTOList getAllReportingGroupsForOfferOption(String optionAlias) {
		List<ReportingGroupLocationTO> list = reportingGroupDAO.findLocationForOption(optionAlias);
		ReportingGroupLocationTOList result = new ReportingGroupLocationTOList();
		result.reportingGroupLocationTOs = list;

		return result;
	}

	public OfferOptionTOList getOfferOptionsFiltered(List<OfferOptionTypeEnum> types, String reportingGroupRef,
			OriginEnum reportingGroupOrigin) {
		// filter null elements
		types.removeIf(item -> item == null);

		OfferOptionTOList offerOptionTOList = new OfferOptionTOList();
		offerOptionTOList.offerOptionTOs = offerOptionDelegate.getOfferOptionTOsFiltered(types, reportingGroupRef,
				reportingGroupOrigin);
		return offerOptionTOList;
	}

	public PerimetersByCredentialTOList getReportingGroupKeysByOfferOption(String origin,
			List<String> optionAliasesGiven) throws BusinessException {

		List<OfferOptionTypeEnum> optionTypes = Arrays.asList(OfferOptionTypeEnum.INTERACTIVE,
				OfferOptionTypeEnum.DOCUMENT);
		List<String> optionAliasesExisting = offerOptionDAO.findOfferOptionAliasIfExistWithType(optionAliasesGiven,
				optionTypes);
		if (optionAliasesExisting.size() != optionAliasesGiven.size()) {
			Collection<String> optionAliasesDiffer = CollectionUtils.disjunction(optionAliasesGiven,
					optionAliasesExisting);
			throw new BusinessException(
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION + " with offerOptionAlias(es) "
							+ StringUtils.join(optionAliasesDiffer, ','),
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION_CODE);
		}
		List<String> optionAliasesWithoutReportingGroup = optionAliasesExisting;
		PerimetersByCredentialTOList result = new PerimetersByCredentialTOList();
		PerimetersByCredentialTO perimetersByCredentialTO = null;
		for (Object[] values : reportingGroupDAO.findReportingGroupKeysByOfferOption(Arrays.asList(origin, "ALL"),
				optionAliasesGiven)) {
			ReportingGroupKeyTO reportingGroupKeyTO = new ReportingGroupKeyTO();
			reportingGroupKeyTO.setOrigin((String) values[0]);
			reportingGroupKeyTO.setReportingGroupRef((String) values[1]);
			String offerOptionAlias = (String) values[2];
			if (optionAliasesWithoutReportingGroup.contains(offerOptionAlias) || perimetersByCredentialTO == null) {
				// It means it first time this offerOptionAlias is found
				optionAliasesWithoutReportingGroup.remove(offerOptionAlias);
				perimetersByCredentialTO = new PerimetersByCredentialTO();
				perimetersByCredentialTO.credentialName = offerOptionAlias;
				perimetersByCredentialTO.credentialType = InternalCredentialTypeEnum.STAT_OFFER_OPTION.getValue();
				result.perimetersByCredentialTOs.add(perimetersByCredentialTO);
			}
			perimetersByCredentialTO.perimeters.add(reportingGroupKeyTO);
		}
		if (result.perimetersByCredentialTOs.isEmpty()) {
			throw new BusinessException(BusinessException.NO_DATA_FOUND_EXCEPTION_MESSAGE + "offerOptionAlias(es) "
					+ StringUtils.join(optionAliasesGiven, ','), BusinessException.NO_DATA_FOUND_EXCEPTION);
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public GetBatchReportOptimizedTOList getBatchReportsForOptionOptimized(String optionAlias,
			boolean filteredGroupReportConfig) throws BusinessException {

		// Just call to check that offerOption exists in database
		offerOptionDelegate.getOfferOptionByKey(optionAlias);

		List<ReportingGroupLocationTO> locations = reportingGroupDAO.findLocationForOption(optionAlias);
		List<GroupReportConfig> groupReportConfigs = groupReportConfigDAO
				.findGroupReportConfigForOptionLocationASC(optionAlias);
		LOGGER.debug("[getBatchReportsForOptionOptimized] Response Size " + groupReportConfigs.size());

		List<GetBatchReportOptimizedTO> result = new ArrayList<>();
		GetBatchReportOptimizedTOList resultList = new GetBatchReportOptimizedTOList();
		resultList.getBatchReportTOs = result;
		resultList.reportingGroupLocation = locations;

		String lastReportConfigAlias = null;
		int locationIndex = locations.size();

		int removal = 0;

		GetBatchReportOptimizedTO getBatchReportTO = null;
		Map<String, List<GetBatchReportOptimizedTO>> batchReportTOsByParamType = new HashMap<>();
		Set<String> paramTypeKey = new TreeSet<>();
		for (GroupReportConfig config : groupReportConfigs) {
			// configSource and its alias are not nillable so no additional control is
			// needed
			if (!config.getReportConfig().getAlias().equals(lastReportConfigAlias)) {
				lastReportConfigAlias = config.getReportConfig().getAlias();
				if (locationIndex < locations.size()) {
					for (int j = locationIndex; j < locations.size(); j++) {
						getBatchReportTO.removedReportingGroupPks.add(locations.get(j).pk);
						removal++;
					}
				}
				locationIndex = 0;
				if (getBatchReportTO != null) {
					if (!batchReportTOsByParamType.containsKey(getBatchReportTO.paramTypeKey)) {
						batchReportTOsByParamType.put(getBatchReportTO.paramTypeKey,
								new ArrayList<GetBatchReportOptimizedTO>());
					}
					batchReportTOsByParamType.get(getBatchReportTO.paramTypeKey).add(getBatchReportTO);
				}
				getBatchReportTO = new GetBatchReportOptimizedTO();
				getBatchReportTO.reportRefId = config.getReportConfig().getAssociatedReport().getRefId();
				getBatchReportTO.computeUri = config.getReportConfig().getAssociatedReport().getComputeUri()
						.replaceAll("\\{version\\}", config.getReportVersion());
				getBatchReportTO.criteria = config.getCriteria().getCriteriaValue();
				getBatchReportTO.reportTimeUnit = config.getReportConfig().getAssociatedReport().getReportTimeUnit();
				getBatchReportTO.reportGranularity = config.getReportConfig().getAssociatedReport().getGranularity();
				getBatchReportTO.defaultReportOutputParameter = TOBuilder
						.buildReportOutputParameter(config.getReportConfig().getAssociatedReportOutput());
				getBatchReportTO.defaultReportOutputParameter.outputUri = getBatchReportTO.defaultReportOutputParameter.outputUri
						.replaceAll("\\{version\\}", config.getReportVersion());
				getBatchReportTO.entityTypes = new HashSet<>();
				getBatchReportTO.computeScope = config.getReportConfig().getComputeScope().getValue();

				for (ParamType type : config.getReportConfig().getParamTypes()) {
					paramTypeKey.add(type.getEntityType());
					EntityTypeParameter params = new EntityTypeParameter();
					params.entityType = type.getEntityType();
					params.entitySubtype = type.getEntitySubtype();
					params.parentType = type.getParentType();
					params.parentSubtype = type.getParentSubtype();
					params.typeAlias = type.getAlias();
					getBatchReportTO.entityTypes.add(params);
				}
				getBatchReportTO.paramTypeKey = StringUtils.join(paramTypeKey, ',');
				paramTypeKey.clear();
			}

			while (locationIndex < locations.size() && !locations.get(locationIndex).location
					.equals(config.getReportingGroup().getDataLocation().getLocationPattern())) {
				LOGGER.debug("[getBatchReportsForOptionOptimized] Not Handling " + locations.get(locationIndex).location
						+ " index " + locationIndex + " "
						+ config.getReportingGroup().getDataLocation().getLocationPattern());
				getBatchReportTO.removedReportingGroupPks.add(locations.get(locationIndex).pk);
				locationIndex++;
				removal++;
			}

			if (locationIndex < locations.size()) {
				if (filteredGroupReportConfig && !config.isEnable()) {
					getBatchReportTO.removedReportingGroupPks.add(locations.get(locationIndex).pk);
				} else if (config.getAssociatedReportOutput() != null) {
					BatchConfigTO configTO = new BatchConfigTO();
					ReportingGroup reportingGroup = config.getReportingGroup();
					ReportingGroupLocationTO locationTO = new ReportingGroupLocationTO(reportingGroup.getPk(),
							reportingGroup.getDataLocation().getLocationPattern(), reportingGroup.getOrigin(),
							reportingGroup.getLanguage(), reportingGroup.getTimeZone());
					configTO.reportingGroupLocation = locationTO;
					if (config.getAssociatedReportOutput() != null) {
						configTO.reportOutputParameter = TOBuilder
								.buildReportOutputParameter(config.getAssociatedReportOutput());
						configTO.reportOutputParameter.outputUri = configTO.reportOutputParameter.outputUri
								.replaceAll("\\{version\\}", config.getReportVersion());
					}
					getBatchReportTO.specializedConfigs.add(configTO);
				}
				locationIndex++;
			}
		}

		if (locationIndex < locations.size()) {
			for (int j = locationIndex; j < locations.size(); j++) {
				getBatchReportTO.removedReportingGroupPks.add(locations.get(j).pk);
				removal++;
			}
		}
		if (!batchReportTOsByParamType.containsKey(getBatchReportTO.paramTypeKey)) {
			batchReportTOsByParamType.put(getBatchReportTO.paramTypeKey, new ArrayList<GetBatchReportOptimizedTO>());
		}
		batchReportTOsByParamType.get(getBatchReportTO.paramTypeKey).add(getBatchReportTO);

		for (Entry<String, List<GetBatchReportOptimizedTO>> batchReportTO : batchReportTOsByParamType.entrySet()) {
			result.addAll(batchReportTO.getValue());
		}

		Map<Long, List<String>> filterUrisByReportingGroupPk = reportingGroupDelegate
				.getFilterUrisByReportingGroupPkForOption(optionAlias);
		for (ReportingGroupLocationTO reportingGroupLocationTO : locations) {
			List<String> filterUrisForCurReportingGroup = filterUrisByReportingGroupPk.get(reportingGroupLocationTO.pk);
			if (filterUrisForCurReportingGroup != null) {
				reportingGroupLocationTO.filterUris = filterUrisForCurReportingGroup;
			}
		}

		LOGGER.debug("[getBatchReportsForOptionOptimized] Result Size " + resultList.getBatchReportTOs.size() + ", "
				+ removal + ", "
				+ (resultList.getBatchReportTOs.size() * resultList.reportingGroupLocation.size() - removal));
		return resultList;
	}

	/**
	 * Request RefReport Database to get the reportConfigs and the reportingGroup having the offerOptionAlias passed in
	 * parameter
	 * 
	 * @param optionAlias
	 * @return GetBatchReportOptimizedTOList reportConfigs and the reportingGroup
	 * @throws BusinessException
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public GetBatchReportOptimizedTOList getBatchReportsAndReportingGroupsForOfferOption(String optionAlias)
			throws BusinessException {

		// Just call to check that offerOption exists in database
		offerOptionDelegate.getOfferOptionByKey(optionAlias);

		LOGGER.debug("[getBatchReportsForOptionOptimized] Get ReportingGroups for OfferOption " + optionAlias);
		List<ReportingGroupLocationTO> locations = reportingGroupDAO.findLocationForOption(optionAlias);

		LOGGER.debug("[getBatchReportsForOptionOptimized] Get FilterURIs for ReportingGroups for OfferOption "
				+ optionAlias);

		// Get filter URIs by ReportingGroups
		Map<Long, List<String>> filterUrisByReportingGroupPk = reportingGroupDelegate
				.getFilterUrisByReportingGroupPkForOption(optionAlias);
		for (ReportingGroupLocationTO reportingGroupLocationTO : locations) {
			List<String> filterUrisForCurReportingGroup = filterUrisByReportingGroupPk.get(reportingGroupLocationTO.pk);
			if (filterUrisForCurReportingGroup != null) {
				reportingGroupLocationTO.filterUris = filterUrisForCurReportingGroup;
			}
		}
		LOGGER.debug("[getBatchReportsForOptionOptimized] Get ReportingConfigs for OfferOption " + optionAlias);

		GetBatchReportOptimizedTOList result = getReportConfigsForOption(optionAlias);
		result.reportingGroupLocation.addAll(locations);

		LOGGER.debug("[getBatchReportsForOptionOptimized] Response Size:" + result.getBatchReportTOs.size()
				+ " reportConfig founds for " + result.reportingGroupLocation.size() + " reportingGroups");

		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public GetBatchReportOptimizedTOList getReportConfigsForOption(String option) {
		List<ReportConfig> reportConfigs = reportConfigDAO.findReportConfigsForOptionASC(option);
		LOGGER.debug("[getReportConfigsForOption] Response Size " + reportConfigs.size());
		List<GetBatchReportOptimizedTO> result = new ArrayList<>();
		GetBatchReportOptimizedTOList resultList = new GetBatchReportOptimizedTOList();
		resultList.getBatchReportTOs = result;
		resultList.reportingGroupLocation = new ArrayList<>();

		GetBatchReportOptimizedTO getBatchReportTO = null;
		Map<String, List<GetBatchReportOptimizedTO>> batchReportTOsByParamType = new HashMap<>();
		Set<String> paramTypeKey = new TreeSet<>();
		for (ReportConfig config : reportConfigs) {
			// configSource and its alias are not nillable so no additional control is
			// needed
			if (getBatchReportTO != null) {
				if (!batchReportTOsByParamType.containsKey(getBatchReportTO.paramTypeKey)) {
					batchReportTOsByParamType.put(getBatchReportTO.paramTypeKey,
							new ArrayList<GetBatchReportOptimizedTO>());
				}
				batchReportTOsByParamType.get(getBatchReportTO.paramTypeKey).add(getBatchReportTO);
			}
			getBatchReportTO = new GetBatchReportOptimizedTO();
			getBatchReportTO.reportRefId = config.getAssociatedReport().getRefId();
			getBatchReportTO.computeUri = config.getAssociatedReport().getComputeUri().replaceAll("\\{version\\}",
					config.getReportDefaultVersion());
			getBatchReportTO.criteria = config.getCriteria().getCriteriaValue();
			getBatchReportTO.reportTimeUnit = config.getAssociatedReport().getReportTimeUnit();
			getBatchReportTO.reportGranularity = config.getAssociatedReport().getGranularity();
			getBatchReportTO.defaultReportOutputParameter = TOBuilder
					.buildReportOutputParameter(config.getAssociatedReportOutput());
			getBatchReportTO.defaultReportOutputParameter.outputUri = getBatchReportTO.defaultReportOutputParameter.outputUri
					.replaceAll("\\{version\\}", config.getReportDefaultVersion());
			getBatchReportTO.entityTypes = new HashSet<>();
			getBatchReportTO.computeScope = config.getComputeScope().getValue();

			for (ParamType type : config.getParamTypes()) {
				paramTypeKey.add(type.getEntityType());
				EntityTypeParameter params = new EntityTypeParameter();
				params.entityType = type.getEntityType();
				params.entitySubtype = type.getEntitySubtype();
				params.parentType = type.getParentType();
				params.parentSubtype = type.getParentSubtype();
				params.typeAlias = type.getAlias();
				getBatchReportTO.entityTypes.add(params);
			}
			getBatchReportTO.paramTypeKey = StringUtils.join(paramTypeKey, ',');
			paramTypeKey.clear();
		}
		if (!batchReportTOsByParamType.containsKey(getBatchReportTO.paramTypeKey)) {
			batchReportTOsByParamType.put(getBatchReportTO.paramTypeKey, new ArrayList<GetBatchReportOptimizedTO>());
		}
		batchReportTOsByParamType.get(getBatchReportTO.paramTypeKey).add(getBatchReportTO);

		for (Entry<String, List<GetBatchReportOptimizedTO>> batchReportTO : batchReportTOsByParamType.entrySet()) {
			result.addAll(batchReportTO.getValue());
		}
		LOGGER.debug("[getReportConfigsForOption] Result Size " + resultList.getBatchReportTOs.size());
		return resultList;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ProvisioningActionStatusTO updateGroupReportConfig(SOAContext soaContext) {

		ProvisioningActionStatusTO status = new ProvisioningActionStatusTO("updateGroupReportConfig");

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[updateGroupReportConfig]: Start"));
		long start = Utils.getTime();

		Map<String, Map<String, Set<Long>>> typeSubtypeAndReportingGroupList = groupReportConfigDelegate
				.getReportingGroupAndItsTypeSubtypes(soaContext);
		Map<String, Set<Long>> paramTypeAndReportingGroupList = groupReportConfigDelegate
				.getParamTypesAndItsReportingGroups(typeSubtypeAndReportingGroupList, soaContext);
		typeSubtypeAndReportingGroupList.clear();

		groupReportConfigDelegate.updateGroupReportConfigStatus(paramTypeAndReportingGroupList, status, soaContext);

		long end = Utils.getTime();
		LOGGER.info(
				SOATools.buildSOALogMessage(soaContext, "[updateGroupReportConfig]: End in " + (end - start) + " ms."));

		status.duration = end - start;
		status.comment = "GroupReportconfigs are enabled/disabled in MySQL database";

		return status;
	}
}

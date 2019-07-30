package com.orange.srs.refreport.business.delegate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.consumer.dao.FilterConfigDAO;
import com.orange.srs.refreport.consumer.dao.FilterToOfferOptionDAO;
import com.orange.srs.refreport.consumer.dao.GroupReportConfigDAO;
import com.orange.srs.refreport.consumer.dao.GroupingRuleDAO;
import com.orange.srs.refreport.consumer.dao.OfferOptionDAO;
import com.orange.srs.refreport.consumer.dao.ReportConfigDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.model.Criteria;
import com.orange.srs.refreport.model.DataLocation;
import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.FilterConfig;
import com.orange.srs.refreport.model.GroupReportConfig;
import com.orange.srs.refreport.model.GroupingRule;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.FilterConfigTO;
import com.orange.srs.refreport.model.TO.FilterToOfferOptionTO;
import com.orange.srs.refreport.model.TO.GroupReportConfigTO;
import com.orange.srs.refreport.model.TO.ReportingGroupFilterUriTO;
import com.orange.srs.refreport.model.TO.ReportingGroupTO;
import com.orange.srs.refreport.model.external.OfferOptionTO;
import com.orange.srs.refreport.model.parameter.UpdateReportingGroupContext;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refura.model.external.ReportGroupKeyTO;
import com.orange.srs.statcommon.model.TO.ReportingGroupKeyTO;
import com.orange.srs.statcommon.model.enums.FileActionEnum;
import com.orange.srs.statcommon.model.enums.OriginEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.provisioning.CreateOfferOptionParameter;
import com.orange.srs.statcommon.model.parameter.provisioning.CreateReportingGroupOfferParameter;
import com.orange.srs.statcommon.model.parameter.provisioning.CreateReportingGroupParameter;
import com.orange.srs.statcommon.model.parameter.provisioning.CreateReportingGroupRuleParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

/**
 * Business delegate regarding every operations on Reporting Groups
 * 
 * @author A159138
 */
@Stateless
public class ReportingGroupDelegate {

	private static final Logger LOGGER = Logger.getLogger(ReportingGroupDelegate.class);

	public static final String GROUPING_CRITERIA_SEPARATOR = "_||_";
	public static final String GROUPING_VALUES_SEPARATOR = "|";

	@EJB
	private OfferOptionDAO offerOptionDao;

	@EJB
	private ReportingGroupDAO reportingGroupDao;

	@EJB
	private FilterConfigDAO filterConfigDao;

	@EJB
	private GroupReportConfigDAO groupReportConfigDao;

	@EJB
	private FilterToOfferOptionDAO filterToOfferOptionDAO;

	@EJB
	private ReportConfigDAO reportConfigDAO;

	@EJB
	private FilterConfigDelegate filterConfigDelegate;

	@EJB
	private GroupingRuleDAO groupingRuleDAO;

	@EJB
	private FilterDelegate filterDelegate;

	@EJB
	private GroupReportConfigDelegate groupReportConfigDelegate;

	@EJB
	private OfferOptionDelegate offerOptionDelegate;

	public UpdateReportingGroupContext initUpdateReportingGroupContext(Date provisioningDate, SOAContext soaContext) {
		long startInitTime = Utils.getTime();
		UpdateReportingGroupContext context = new UpdateReportingGroupContext();
		context.soaContext = soaContext;
		context.provisioningDate = provisioningDate;
		context.hashOfferOptions = offerOptionDelegate.getAllOfferOptionByOptionAlias(soaContext);
		context.hashFilters = filterDelegate.getAllFilterByFilterId(soaContext);
		context.filterConfigTOByOptionPkByReportingGroupPk = filterConfigDelegate
				.getFilterConfigTOByOptionPkByReportingGroupPk();
		for (FilterToOfferOptionTO filterToOfferOptionTO : filterToOfferOptionDAO.findAllFilterToOfferOptionTOs()) {
			Set<String> filterIds = context.filterIdsByOfferOptionAlias.get(filterToOfferOptionTO.offerOptionAlias);
			if (filterIds == null) {
				filterIds = new HashSet<>();
				context.filterIdsByOfferOptionAlias.put(filterToOfferOptionTO.offerOptionAlias, filterIds);
			}
			filterIds.add(filterToOfferOptionTO.filterId);
			if (filterToOfferOptionTO.defaultForAllGroups) {
				Set<String> defaultFilterIds = context.defaultFilterIdsByOfferOptionAlias
						.get(filterToOfferOptionTO.offerOptionAlias);
				if (defaultFilterIds == null) {
					defaultFilterIds = new HashSet<>();
					context.defaultFilterIdsByOfferOptionAlias.put(filterToOfferOptionTO.offerOptionAlias,
							defaultFilterIds);
				}
				defaultFilterIds.add(filterToOfferOptionTO.filterId);
			}
		}
		context.lastReportingGroupPk = reportingGroupDao.getLastPkValue(); // TODO JLN 16/06/2015 Remove this line when
																			// provisioning done with RefObject

		List<ReportConfig> additionalReportConfigs = reportConfigDAO.findBy(ReportConfig.FIELD_OPTIONAL, true);
		for (ReportConfig additionalReportConfig : additionalReportConfigs) {
			context.hashAdditionalReportConfigs.put(additionalReportConfig.getAlias(), additionalReportConfig);
		}
		context.groupReportConfigByOptionPkByReportingGroupPk = groupReportConfigDelegate
				.getAdditionalReportConfigTOByOptionPkByReportingGroupPk();
		context.reportingGroupRefsByOrigin = getAllReportingGroupRefByOrigin();

		context.timeInit = Utils.getTime() - startInitTime;
		return context;
	}

	private Map<OriginEnum, Set<String>> getAllReportingGroupRefByOrigin() {
		Map<OriginEnum, Set<String>> reportingGroupRefsByOrigin = new HashMap<>();
		List<ReportingGroupKeyTO> reportingGroupKeys = reportingGroupDao.findAllReportingGroupsKeys();
		for (ReportingGroupKeyTO reportingGroupKey : reportingGroupKeys) {
			OriginEnum origin = OriginEnum.fromValue(reportingGroupKey.getOrigin());
			Set<String> reportingGroupRefs = reportingGroupRefsByOrigin.get(origin);
			if (reportingGroupRefs == null) {
				reportingGroupRefs = new HashSet<>();
				reportingGroupRefsByOrigin.put(origin, reportingGroupRefs);
			}
			reportingGroupRefs.add(reportingGroupKey.getReportingGroupRef());
		}
		reportingGroupKeys.clear();
		return reportingGroupRefsByOrigin;
	}

	private void updateFilterConfigs(UpdateReportingGroupContext updateContext,
			Map<Long, List<FilterConfigTO>> existingFilterConfigTOs, ReportingGroup reportingGroup,
			OfferOption offerOption) {

		if (existingFilterConfigTOs != null) {
			List<FilterConfigTO> currentFilterConfigTOs = existingFilterConfigTOs.get(offerOption.getPk());
			if (currentFilterConfigTOs != null) {
				for (FilterConfigTO currentFilterConfigTO : currentFilterConfigTOs) {
					String currentFilterId = currentFilterConfigTO.filterId;
					if (!updateContext.filterIds.remove(currentFilterId)) {
						FilterConfig currentFilterConfig = filterConfigDao
								.findById(currentFilterConfigTO.filterConfigPk);
						filterConfigDelegate.removeFilterConfig(currentFilterConfig);
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("FilterConfig removed for filter: " + currentFilterId);
						}
					}
				}
			}
		}

		for (String filterId : updateContext.filterIds) {
			addFilterConfig(updateContext, reportingGroup, offerOption, filterId);
		}
	}

	private void updateAdditionalReportsAlias(UpdateReportingGroupContext updateContext,
			Map<Long, List<GroupReportConfigTO>> existingGroupReportConfigTOs, ReportingGroup reportingGroup,
			OfferOption offerOption) {

		if (existingGroupReportConfigTOs != null) {
			List<GroupReportConfigTO> currentGroupReportConfigTOs = existingGroupReportConfigTOs
					.get(offerOption.getPk());
			if (currentGroupReportConfigTOs != null) {
				for (GroupReportConfigTO currentGroupReportConfigTO : currentGroupReportConfigTOs) {
					String currentReportConfigAlias = currentGroupReportConfigTO.reportConfigAlias;
					if (!updateContext.optionalReportConfigs.remove(currentReportConfigAlias)) {
						GroupReportConfig currentGroupReportConfig = groupReportConfigDao
								.findById(currentGroupReportConfigTO.groupReportConfigPk);
						groupReportConfigDao.remove(currentGroupReportConfig);
						if (LOGGER.isTraceEnabled()) {
							LOGGER.trace("AdditionalGroupReportConfig removed for groupReportConfig: "
									+ currentGroupReportConfig);
						}
					}
				}
			}
		}

		for (String optionalReportConfig : updateContext.optionalReportConfigs) {
			addAdditionalGroupReportConfig(updateContext, reportingGroup, optionalReportConfig);
		}
	}

	private void addFilterConfig(UpdateReportingGroupContext updateContext, ReportingGroup reportingGroup,
			OfferOption offerOption, String filterId) {
		Filter filter = updateContext.hashFilters.get(filterId);
		if (filter == null) {
			LOGGER.warn("Filter '" + filterId + "' not found in database");
		} else {
			Set<String> filterIdsForOfferOptionAlias = updateContext.filterIdsByOfferOptionAlias
					.get(offerOption.getAlias());
			if (filterIdsForOfferOptionAlias != null && filterIdsForOfferOptionAlias.contains(filterId)) {
				// Create filterConfig
				reportingGroup.getFilterConfigs().add(
						filterConfigDelegate.createFilterConfigWithoutPersist(filter, offerOption, reportingGroup));
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("New filterConfig for filter: " + filterId);
				}
			} else if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Filter '" + filterId + "' not related to offer option " + offerOption.getAlias());
			}
		}
	}

	private void addAdditionalGroupReportConfig(UpdateReportingGroupContext updateContext,
			ReportingGroup reportingGroup, String optionalReportConfig) {
		ReportConfig reportConfig = updateContext.hashAdditionalReportConfigs.get(optionalReportConfig);
		if (reportConfig == null) {
			LOGGER.warn("ReportConfig '" + optionalReportConfig + "' not found in database");
		} else {
			// Create GroupReportConfig
			GroupReportConfig groupReportConfig = new GroupReportConfig();
			groupReportConfig.setReportConfig(reportConfig);
			groupReportConfig.setEnable(true);
			groupReportConfig.setCriteria(reportConfig.getCriteria() == null ? CriteriaDelegate.getDefaultCriteria()
					: reportConfig.getCriteria());
			groupReportConfig.setReportVersion(reportConfig.getReportDefaultVersion());
			reportingGroup.getGroupReportConfigs().add(groupReportConfig);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("New AdditionalReportConfig for reportConfig: " + optionalReportConfig);
			}
		}
	}

	private void addGroupReportConfig(ReportingGroup reportingGroup, OfferOption offerOption) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("ReportConfig count: " + offerOption.getReportConfigs().size() + " for Option "
					+ offerOption.getAlias());
		}
		for (ReportConfig reportConfig : offerOption.getReportConfigs()) {
			if (!reportConfig.isOptional()) {
				// Modification of the link groupReportConfig/reportingGroup
				GroupReportConfig groupReportConfig = new GroupReportConfig();
				groupReportConfig.setReportConfig(reportConfig);
				groupReportConfig.setEnable(true);
				groupReportConfig.setCriteria(reportConfig.getCriteria() == null ? CriteriaDelegate.getDefaultCriteria()
						: reportConfig.getCriteria());
				groupReportConfig.setReportVersion(reportConfig.getReportDefaultVersion());
				reportingGroup.getGroupReportConfigs().add(groupReportConfig);
			}
		}
	}

	private void removeGroupReportConfigs(ReportingGroup reportingGroup, List<OfferOption> deletedOptionList) {
		for (OfferOption deletedOfferOption : deletedOptionList) {
			List<GroupReportConfig> listGroupReportConfigToSuppressForOption = groupReportConfigDao
					.findGroupReportConfigForOptionAndGroup(deletedOfferOption.getAlias(), reportingGroup.getOrigin(),
							reportingGroup.getReportingGroupRef());
			reportingGroup.getGroupReportConfigs().removeAll(listGroupReportConfigToSuppressForOption);
		}
	}

	public List<ReportingGroup> getReportingGroupListFromOriginAndRefGroup(String origin, String reportingGroupRef) {

		String[] attributes = { ReportingGroup.FIELD_ORIGIN, ReportingGroup.FIELD_REPORTING_GROUP_REF };
		Object[] values = { origin, reportingGroupRef };
		return reportingGroupDao.findByMultipleCriteria(attributes, values);
	}

	public ReportingGroup getReportingGroupFromOriginAndRefGroup(String origin, String reportingGroupRef)
			throws BusinessException {
		List<ReportingGroup> reportingGroups = getReportingGroupListFromOriginAndRefGroup(origin, reportingGroupRef);

		if (reportingGroups.isEmpty()) {
			return null;
		} else if (reportingGroups.size() == 1) {
			return reportingGroups.get(0);
		} else {
			throw new BusinessException(BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION
					+ " for ReportingGroup with reportingGroupRef " + reportingGroupRef + " and origin " + origin);
		}
	}

	public Map<String, List<String>> getFilterIdsByOptionAliasForReportingGroup(ReportingGroupTO reportingGroupTO) {
		Map<String, List<String>> filterIdsByOptionAlias = new HashMap<>();
		for (FilterToOfferOptionTO optionAliasFilterIdTO : reportingGroupDao
				.findOptionAliasAndFilterIdForReportingGroup(reportingGroupTO.getOrigin(),
						reportingGroupTO.getReportingGroupRef())) {
			List<String> filterIds = filterIdsByOptionAlias.get(optionAliasFilterIdTO.offerOptionAlias);
			if (filterIds == null) {
				filterIds = new ArrayList<>();
				filterIdsByOptionAlias.put(optionAliasFilterIdTO.offerOptionAlias, filterIds);
			}
			if (optionAliasFilterIdTO.filterId != null) {
				filterIds.add(optionAliasFilterIdTO.filterId);
			}
		}
		return filterIdsByOptionAlias;
	}

	public List<OfferOptionTO> findOptionTOsForReportingGroup(ReportingGroupTO reportingGroupTO) {
		List<OfferOptionTO> optionTOs = new ArrayList<>();
		Map<String, List<String>> filerIdsByOptionAlias = getFilterIdsByOptionAliasForReportingGroup(reportingGroupTO);
		for (String optionAlias : reportingGroupDao.findOptionAliasForReportingGroup(reportingGroupTO.getOrigin(),
				reportingGroupTO.getReportingGroupRef())) {
			OfferOptionTO offerOptionTO = new OfferOptionTO(optionAlias);
			optionTOs.add(offerOptionTO);
			List<String> filerIdsForCurrentOptionAlias = filerIdsByOptionAlias.get(optionAlias);
			if (filerIdsForCurrentOptionAlias != null) {
				for (String filterId : filerIdsForCurrentOptionAlias) {
					offerOptionTO.addFilterId(filterId);
				}
			}
		}
		return optionTOs;
	}

	public ReportingGroup getReportingGroup(ReportGroupKeyTO reportingGroupKey) throws BusinessException {
		ReportingGroup result = getReportingGroupFromOriginAndRefGroup(reportingGroupKey.origin,
				reportingGroupKey.reportGroupRef);

		if (result == null) {
			throw new BusinessException(
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION + " : reportingGroup with key " + reportingGroupKey,
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION_CODE);
		}

		return result;
	}

	public Set<ReportingGroup> getReportingGroupsListByReportKey(Set<ReportGroupKeyTO> reportingGroupsKeys,
			SOAContext soaContext) {
		Set<ReportingGroup> groupsFound = new HashSet<>(reportingGroupsKeys.size());

		for (ReportGroupKeyTO groupKeyToFind : reportingGroupsKeys) {
			try {
				groupsFound.add(getReportingGroup(groupKeyToFind));
			} catch (BusinessException bex) {
				LOGGER.error(SOATools.buildSOALogMessage(soaContext,
						"Cannot find reportingGroup with key " + groupKeyToFind));
			}
		}

		return groupsFound;
	}

	public Map<Long, List<String>> getFilterUrisByReportingGroupPkForOption(String optionAlias) {
		Map<Long, List<String>> filterUrisByReportingGroupPk = new HashMap<>();
		for (ReportingGroupFilterUriTO to : reportingGroupDao.findReportingGroupAndFilterUriForOption(optionAlias)) {
			List<String> filterUris = filterUrisByReportingGroupPk.get(to.reportingGroupPk);
			if (filterUris == null) {
				filterUris = new ArrayList<>();
				filterUrisByReportingGroupPk.put(to.reportingGroupPk, filterUris);
			}
			filterUris.add(to.filterUri);
		}
		return filterUrisByReportingGroupPk;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateReportingGroup(List<CreateReportingGroupParameter> createReportingGroupParameters,
			UpdateReportingGroupContext updateContext) throws BusinessException {

		List<ReportingGroup> reportingGroupUpdated = new ArrayList<>();
		// List<ReportingGroup> reportingGroupToRemoved = new ArrayList<>();

		for (CreateReportingGroupParameter createReportingGroupParameter : createReportingGroupParameters) {

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Process reporting group " + createReportingGroupParameter.reportingGroupRef + "("
						+ createReportingGroupParameter.origin + ")");
			}

			ReportingGroup reportingGroupToUpdate = null;
			if (createReportingGroupParameter.status != null
					&& FileActionEnum.DELETE.equals(createReportingGroupParameter.status)) {
				// TODO JLN 08/10/2015 ReportingGroup must be delete in RefObject and here
				// reportingGroupToRemoved.add(reportingGroupToUpdate);

			} else {

				long startGetTime = Utils.getTime();
				reportingGroupToUpdate = getReportingGroupFromOriginAndRefGroup(
						createReportingGroupParameter.origin.getValue(),
						createReportingGroupParameter.reportingGroupRef);
				updateContext.timeGetFromDB += Utils.getTime() - startGetTime;

				if (reportingGroupToUpdate != null) { // Update Reporting group
					updateCriteriaAndDataLocation(updateContext, reportingGroupToUpdate,
							createReportingGroupParameter.reportingGroupRef);

					// Unecessary update of grouping rules
					// updateGroupingRules(updateContext, reportingGroupToUpdate,
					// createReportingGroupParameter.rules);

					updateOptionAndReports(updateContext, reportingGroupToUpdate, createReportingGroupParameter.offers);

					// if (reportingGroupToUpdate.getReportSourceOptions().isEmpty()) {
					// TODO JLN 08/10/2015 ReportingGroup must be delete in RefObject and here
					// reportingGroupToRemoved.add(reportingGroupToUpdate);
					// } else {
					reportingGroupUpdated.add(reportingGroupToUpdate);
					// }
				}
			}
			updateContext.clearOffers();
		}
		long startPersistTime = Utils.getTime();
		reportingGroupDao.persist(reportingGroupUpdated);
		// reportingGroupDao.remove(reportingGroupToRemoved);
		updateContext.timePersist += Utils.getTime() - startPersistTime;
	}

	@Deprecated
	private void updateGroupingRules(UpdateReportingGroupContext updateContext, ReportingGroup reportingGroup,
			Set<CreateReportingGroupRuleParameter> createRulesParameters) {

		long startTime = Utils.getTime();

		updateContext.groupingRules.clear();
		updateContext.groupingRulesToRemove.clear();
		for (CreateReportingGroupRuleParameter createReportingGroupRule : createRulesParameters) {
			GroupingRule groupingRule = new GroupingRule();
			groupingRule.setGroupingRule(createReportingGroupRule.groupingCriteria);
			groupingRule.setGroupingValue(createReportingGroupRule.groupingValue);
			groupingRule.setTargetGroup(reportingGroup);
			updateContext.groupingRules.add(groupingRule);
		}
		List<GroupingRule> existingGroupingRules = reportingGroup.getGroupingRules();// updateContext.groupingRulesByReportingGroupPk.get(reportingGroup.getPk());
		if (existingGroupingRules != null) {
			for (GroupingRule currentGroupingRule : existingGroupingRules) {
				if (!updateContext.groupingRules.remove(currentGroupingRule)) {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("GroupingRule removed : " + currentGroupingRule.getGroupingRule() + " - "
								+ currentGroupingRule.getGroupingValue());
					}
					updateContext.groupingRulesToRemove.add(currentGroupingRule);
				}
			}
		}
		if (CollectionUtils.isNotEmpty(updateContext.groupingRulesToRemove)) {
			reportingGroup.getGroupingRules().removeAll(updateContext.groupingRulesToRemove);
		}
		reportingGroup.getGroupingRules().addAll(updateContext.groupingRules);

		updateContext.timeUpdateGroupingRules += Utils.getTime() - startTime;
	}

	private void updateOptionAndReports(UpdateReportingGroupContext updateContext, ReportingGroup reportingGroup,
			Set<CreateReportingGroupOfferParameter> createOfferParameters) {

		long startTime = Utils.getTime();

		// Retrieve new offerOptions in newOfferOptionList
		updateContext.newOfferOptions.clear();
		updateContext.offerOptions.clear();
		List<OfferOption> existingOfferOptions = reportingGroup.getReportSourceOptions();
		Map<Long, List<FilterConfigTO>> existingFilterConfigTOs = updateContext.filterConfigTOByOptionPkByReportingGroupPk
				.get(reportingGroup.getPk());
		Map<Long, List<GroupReportConfigTO>> existingGroupReportConfigTOs = updateContext.groupReportConfigByOptionPkByReportingGroupPk
				.get(reportingGroup.getPk());

		for (CreateReportingGroupOfferParameter offerParameter : createOfferParameters) {
			if (offerParameter.offers != null) {
				for (CreateOfferOptionParameter offerOptionParameter : offerParameter.offers) {
					if (LOGGER.isTraceEnabled()) {
						LOGGER.trace("Process option: " + offerOptionParameter.name);
					}
					OfferOption offerOption = updateContext.hashOfferOptions.get(offerOptionParameter.name);
					if (offerOption == null) {
						LOGGER.warn("Option '" + offerOptionParameter.name + "' not found in database");
					} else {
						updateContext.offerOptions.add(offerOption);
						if (!existingOfferOptions.contains(offerOption)) {
							updateContext.newOfferOptions.add(offerOption);
							if (LOGGER.isTraceEnabled()) {
								LOGGER.trace("New option: " + offerOptionParameter.name);
							}
						}

						updateContext.filterIds.clear();
						if (offerParameter.filterIds != null) {
							updateContext.filterIds.addAll(offerParameter.filterIds);
						}
						Set<String> defaultFilterIds = updateContext.defaultFilterIdsByOfferOptionAlias
								.get(offerOptionParameter.name);
						if (defaultFilterIds != null) {
							updateContext.filterIds.addAll(defaultFilterIds);
						}
						updateFilterConfigs(updateContext, existingFilterConfigTOs, reportingGroup, offerOption);

						updateContext.optionalReportConfigs.clear();
						if (offerOptionParameter.additionalReportsAlias != null) {
							updateContext.optionalReportConfigs.addAll(offerOptionParameter.additionalReportsAlias);
						}
						updateAdditionalReportsAlias(updateContext, existingGroupReportConfigTOs, reportingGroup,
								offerOption);
					}
				}
			}
		}

		// Retrieve deleted options in deletedOfferOptionList
		updateContext.deletedOfferOptions.clear();
		for (OfferOption existingOfferOption : existingOfferOptions) {
			if (!updateContext.offerOptions.contains(existingOfferOption)) {
				updateContext.deletedOfferOptions.add(existingOfferOption);
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Option to be deleted: " + existingOfferOption.getAlias());
				}
			}
		}

		// Update of links reportingGroup/offerOption
		reportingGroup.setReportSourceOptions(new ArrayList<>(updateContext.offerOptions));

		// Remove reports and groupreportconfigs associated to the deleted options
		removeGroupReportConfigs(reportingGroup, updateContext.deletedOfferOptions);

		// Remove filterConfigs associated to the deleted options
		for (OfferOption deletedOfferOption : updateContext.deletedOfferOptions) {
			filterConfigDao.remove(filterConfigDelegate.getFilterConfigsForOptionAndReportingGroup(deletedOfferOption,
					reportingGroup));
		}

		// Add new reports and new group report configs associated to the new options
		// for each option, adding associated reports to the reporting group
		for (OfferOption newOfferOption : updateContext.newOfferOptions) {
			addGroupReportConfig(reportingGroup, newOfferOption);
		}

		updateContext.timeUpdateOptionAndReports += Utils.getTime() - startTime;
	}

	private void updateCriteriaAndDataLocation(UpdateReportingGroupContext updateContext, ReportingGroup reportingGroup,
			String location) {
		long startTime = Utils.getTime();
		Criteria defaultCriteria = CriteriaDelegate.getDefaultCriteria();
		Criteria rgCriteria = CriteriaDelegate.getReportingGroupCriteria();
		reportingGroup.getCriterias().add(defaultCriteria);
		reportingGroup.getCriterias().add(rgCriteria);

		if (reportingGroup.getDataLocation() == null
				|| !defaultCriteria.equals(reportingGroup.getDataLocation().getCriteria())
				|| !StringUtils.equals(location, reportingGroup.getDataLocation().getLocationPattern())) {
			DataLocation dataLocation = new DataLocation();
			dataLocation.setLocationPattern(location);
			dataLocation.setCriteria(defaultCriteria);
			reportingGroup.setDataLocation(dataLocation);
		}
		updateContext.timeUpdateCriteriaAndDataLocation += Utils.getTime() - startTime;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int updateReportingGroupsDataLocation(SOAContext soaContext, List<ReportingGroupKeyTO> reportingGroupKeys)
			throws BusinessException {

		List<ReportingGroup> reportingGroupsUpdated = new ArrayList<>();
		for (ReportingGroupKeyTO reportingGroupKey : reportingGroupKeys) {
			ReportingGroup reportingGroup = getReportingGroupFromOriginAndRefGroup(reportingGroupKey.getOrigin(),
					reportingGroupKey.getReportingGroupRef());
			if (reportingGroup != null) {
				String location = reportingGroupKey.getReportingGroupRef();
				Criteria defaultCriteria = CriteriaDelegate.getDefaultCriteria();
				if (reportingGroup.getDataLocation() == null
						|| !defaultCriteria.equals(reportingGroup.getDataLocation().getCriteria())
						|| !StringUtils.equals(location, reportingGroup.getDataLocation().getLocationPattern())) {
					DataLocation dataLocation = new DataLocation();
					dataLocation.setLocationPattern(location);
					dataLocation.setCriteria(defaultCriteria);
					reportingGroup.setDataLocation(dataLocation);
					reportingGroupsUpdated.add(reportingGroup);
				}
			}
		}
		reportingGroupDao.persist(reportingGroupsUpdated);
		return reportingGroupsUpdated.size();
	}

	public Map<String, Map<String, String>> getReportingGroupRefDataLocationByDomainByOrigin(Calendar provisioningDate,
			SOAContext soaContext) {

		Map<String, Map<String, String>> result = new HashMap<>();
		result.put(OriginEnum.ALL.getValue(), new HashMap<String, String>());
		result.put(OriginEnum.EQUANT.getValue(), new HashMap<String, String>());
		result.put(OriginEnum.SCE.getValue(), new HashMap<String, String>());

		for (Object[] reportingGroupsAndDomain : groupingRuleDAO
				.findAllReportingGroupsAndAssociatedDomainGroupingValues()) {
			String reportingGroupOrigin = (String) reportingGroupsAndDomain[0];
			String reportingGroupRef = (String) reportingGroupsAndDomain[1];
			String domainGroupingValue = (String) reportingGroupsAndDomain[2];
			result.get(reportingGroupOrigin).put(domainGroupingValue, reportingGroupRef);
		}

		return result;

	}

}

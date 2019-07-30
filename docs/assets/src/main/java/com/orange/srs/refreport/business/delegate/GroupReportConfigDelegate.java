package com.orange.srs.refreport.business.delegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.consumer.dao.GroupReportConfigDAO;
import com.orange.srs.refreport.consumer.dao.ParamTypeDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.TO.GroupReportConfigTO;
import com.orange.srs.refreport.model.TO.ProvisioningActionStatusTO;
import com.orange.srs.refreport.provider.service.rest.catalog.GroupReportConfigService;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

@Stateless
public class GroupReportConfigDelegate {

	private static Logger LOGGER = Logger.getLogger(GroupReportConfigService.class);

	@EJB
	private ReportingGroupDAO reportingGroupDAO;

	@EJB
	private GroupReportConfigDAO groupReportConfigDAO;

	@EJB
	private ParamTypeDAO paramTypeDAO;

	public Map<String, Map<String, Set<Long>>> getReportingGroupAndItsTypeSubtypes(SOAContext soaContext) {
		long start = -1;
		if (LOGGER.isDebugEnabled()) {
			start = Utils.getTime();
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "[getReportingGroupAndItsTypeSubtypes]: Start"));
		}
		List<Object[]> reportingGroupAndTypeSubtypeList = reportingGroupDAO
				.findAllReportingGroupPkAndTypeSubtypeValues();

		Map<String, Map<String, Set<Long>>> typeSubtypeAndReportingGroupList = new HashMap<>();
		for (Object[] reportingGroupAndTypeSubtype : reportingGroupAndTypeSubtypeList) {
			String type = (String) reportingGroupAndTypeSubtype[1];
			String subtype = (String) reportingGroupAndTypeSubtype[2];
			long reportingGroupPk = (long) reportingGroupAndTypeSubtype[0];
			Map<String, Set<Long>> subtypeAndReportingGroup = typeSubtypeAndReportingGroupList.get(type);
			if (subtypeAndReportingGroup == null) {
				subtypeAndReportingGroup = new HashMap<>();
				typeSubtypeAndReportingGroupList.put(type, subtypeAndReportingGroup);
			}
			Set<Long> reportingGroupPks = subtypeAndReportingGroup.get(subtype);
			if (reportingGroupPks == null) {
				reportingGroupPks = new HashSet<>();
				subtypeAndReportingGroup.put(subtype, reportingGroupPks);
			}
			reportingGroupPks.add(reportingGroupPk);
		}
		if (LOGGER.isDebugEnabled()) {
			long end = Utils.getTime();
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
					"[getReportingGroupAndItsTypeSubtypes]: End in " + (end - start) + " ms."));
		}
		return typeSubtypeAndReportingGroupList;
	}

	public Map<String, Set<Long>> getParamTypesAndItsReportingGroups(
			Map<String, Map<String, Set<Long>>> typeSubtypeAndReportingGroupList, SOAContext soaContext) {
		long start = -1;
		if (LOGGER.isDebugEnabled()) {
			start = Utils.getTime();
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "[getParamTypesAndItsReportingGroups]: Start"));
		}
		// Get ParamTypes and found reportingGroupList which having paramType
		List<ParamType> allParamTypes = paramTypeDAO.findAll();
		Map<String, Set<Long>> paramTypeAndReportingGroupList = new HashMap<>();
		for (ParamType paramType : allParamTypes) {
			Map<String, Set<Long>> subtypeAndReportingGroup = typeSubtypeAndReportingGroupList
					.get(paramType.getEntityType());
			if (subtypeAndReportingGroup != null) {
				Set<Long> reportingGroupsWithEntityParam = new HashSet<>();
				if (paramType.getEntitySubtype().equalsIgnoreCase("*")) {
					for (Set<Long> reportingGroupPk : subtypeAndReportingGroup.values()) {
						reportingGroupsWithEntityParam.addAll(reportingGroupPk);
					}
				} else {
					Set<Long> reportingGroupPks = subtypeAndReportingGroup.get(paramType.getEntitySubtype());
					if (reportingGroupPks != null) {
						reportingGroupsWithEntityParam.addAll(reportingGroupPks);
					}
				}

				if (!reportingGroupsWithEntityParam.isEmpty() && !paramType.getParentType().equalsIgnoreCase("*")) {
					Set<Long> reportingGroupsWithParentEntityParam = new HashSet<>();
					subtypeAndReportingGroup = typeSubtypeAndReportingGroupList.get(paramType.getParentType());
					if (subtypeAndReportingGroup != null) {
						if (paramType.getParentSubtype().equalsIgnoreCase("*")) {
							for (Set<Long> reportingGroupPk : subtypeAndReportingGroup.values()) {
								reportingGroupsWithParentEntityParam.addAll(reportingGroupPk);
							}
						} else {
							Set<Long> reportingGroupPks = subtypeAndReportingGroup.get(paramType.getParentSubtype());
							if (reportingGroupPks != null) {
								reportingGroupsWithParentEntityParam.addAll(reportingGroupPks);
							}
						}
					}

					reportingGroupsWithEntityParam.retainAll(reportingGroupsWithParentEntityParam);
				}

				if (!reportingGroupsWithEntityParam.isEmpty()) {
					paramTypeAndReportingGroupList.put(paramType.getAlias(), reportingGroupsWithEntityParam);
				}
			} else if (paramType.getEntityType().equalsIgnoreCase("*")) {
				Set<Long> reportingGroupsWithEntityParam = new HashSet<>();
				for (Map<String, Set<Long>> subtypeAndReportingGroupForNETWORKParam : typeSubtypeAndReportingGroupList
						.values()) {
					for (Set<Long> reportingGroupPk : subtypeAndReportingGroupForNETWORKParam.values()) {
						reportingGroupsWithEntityParam.addAll(reportingGroupPk);
					}
				}
				reportingGroupsWithEntityParam.retainAll(reportingGroupsWithEntityParam);
				paramTypeAndReportingGroupList.put(paramType.getAlias(), reportingGroupsWithEntityParam);
			}
		}
		if (LOGGER.isDebugEnabled()) {
			long end = Utils.getTime();
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
					"[getParamTypesAndItsReportingGroups]: End in " + (end - start) + " ms."));
		}
		return paramTypeAndReportingGroupList;
	}

	public void updateGroupReportConfigStatus(Map<String, Set<Long>> paramTypeAndReportingGroupList,
			ProvisioningActionStatusTO status, SOAContext soaContext) {
		long start = -1;
		if (LOGGER.isDebugEnabled()) {
			start = Utils.getTime();
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "[updateGroupReportConfigStatus]: Start"));
		}
		// Get GroupReportconfig And ParamType by Reporting group
		List<Object[]> groupReportConfigByReportingGroupAndParamTypeList = groupReportConfigDAO
				.findGroupReportConfigPksAndParamTypeAliasAndReportingGroupPkAssociated();

		Set<Long> groupReportConfigToDisable = new HashSet<>();
		Set<Long> groupReportConfigToEnable = new HashSet<>();
		long previousGroupReportConfigPk = -1;
		boolean groupReportingConfigAlreadyUpdated = false;
		for (Object[] grc : groupReportConfigByReportingGroupAndParamTypeList) {
			long groupReportConfigPk = (long) grc[0];
			boolean isEnable = (boolean) grc[1];
			String paramTypeAlias = (String) grc[2];
			long reportingGroupPk = (long) grc[3];
			if (groupReportConfigPk != previousGroupReportConfigPk) {
				groupReportingConfigAlreadyUpdated = false;
				previousGroupReportConfigPk = groupReportConfigPk;
			}
			Set<Long> reportingGroupPksForParamType = paramTypeAndReportingGroupList.get(paramTypeAlias);

			if (paramTypeAlias.equalsIgnoreCase("NETWORK")) {
				if (!isEnable) {
					groupReportConfigToEnable.add(groupReportConfigPk);
				}
				groupReportingConfigAlreadyUpdated = true;
			} else if (reportingGroupPksForParamType != null) {
				if (groupReportingConfigAlreadyUpdated) {
					if (reportingGroupPksForParamType.contains(reportingGroupPk)) {
						if (groupReportConfigToDisable.contains(groupReportConfigPk)) {
							groupReportConfigToDisable.remove(groupReportConfigPk);
						}
						if (!isEnable) {
							groupReportConfigToEnable.add(groupReportConfigPk);
						}
					}
				} else {
					if (isEnable && !reportingGroupPksForParamType.contains(reportingGroupPk)) {
						groupReportConfigToDisable.add(groupReportConfigPk);
					} else if (!isEnable && reportingGroupPksForParamType.contains(reportingGroupPk)) {
						groupReportConfigToEnable.add(groupReportConfigPk);
					}
				}
				groupReportingConfigAlreadyUpdated = true;
			} else if (!groupReportingConfigAlreadyUpdated && isEnable) {
				groupReportConfigToDisable.add(groupReportConfigPk);
				groupReportingConfigAlreadyUpdated = true;
			}
		}

		// Update DataBase
		int nbGroupReportConfigDisabled = 0;
		int nbGroupReportConfigEnabled = 0;
		int startIndex = 0;
		List<Long> groupReportConfigToUpdate = new ArrayList<>(groupReportConfigToDisable);
		while (startIndex < groupReportConfigToUpdate.size()) {
			int endIndex = startIndex + Configuration.paginationSize > groupReportConfigToUpdate.size()
					? groupReportConfigToUpdate.size()
					: (startIndex + Configuration.paginationSize);
			nbGroupReportConfigDisabled = groupReportConfigDAO
					.enableOrDisableGroupReportConfig(groupReportConfigToUpdate.subList(startIndex, endIndex), false);
			startIndex = endIndex;
		}
		groupReportConfigToDisable.clear();
		startIndex = 0;
		groupReportConfigToUpdate = new ArrayList<>(groupReportConfigToEnable);
		while (startIndex < groupReportConfigToUpdate.size()) {
			int endIndex = startIndex + Configuration.paginationSize > groupReportConfigToUpdate.size()
					? groupReportConfigToUpdate.size()
					: (startIndex + Configuration.paginationSize);
			nbGroupReportConfigEnabled = groupReportConfigDAO
					.enableOrDisableGroupReportConfig(groupReportConfigToUpdate.subList(startIndex, endIndex), true);
			startIndex = endIndex;
		}
		groupReportConfigToEnable.clear();

		if (LOGGER.isDebugEnabled()) {
			long end = Utils.getTime();
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
					"[updateGroupReportConfigStatus]: End in " + (end - start) + " ms."));
		}
		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[updateGroupReportConfigStatus]: " + nbGroupReportConfigDisabled
						+ " GroupReportConfig are disabled and " + nbGroupReportConfigEnabled
						+ " GroupReportConfig are enabled"));

		status.addInfo("nbGroupReportConfigEnabled", nbGroupReportConfigEnabled);
		status.addInfo("nbGroupReportConfigDisabled", nbGroupReportConfigDisabled);
	}

	public Map<Long, Map<Long, List<GroupReportConfigTO>>> getAdditionalReportConfigTOByOptionPkByReportingGroupPk() {
		Map<Long, Map<Long, List<GroupReportConfigTO>>> groupReportConfigTOByOptionPkByReportingGroupPk = new HashMap<>();
		for (GroupReportConfigTO groupReportConfigTO : groupReportConfigDAO.findAdditionalGroupReportConfigTO()) {
			Map<Long, List<GroupReportConfigTO>> reportConfigTOByOption = groupReportConfigTOByOptionPkByReportingGroupPk
					.get(groupReportConfigTO.reportingGroupPk);
			if (reportConfigTOByOption == null) {
				reportConfigTOByOption = new HashMap<>();
				groupReportConfigTOByOptionPkByReportingGroupPk.put(groupReportConfigTO.reportingGroupPk,
						reportConfigTOByOption);
			}
			List<GroupReportConfigTO> groupReportConfigTOs = reportConfigTOByOption
					.get(groupReportConfigTO.offerOptionPk);
			if (groupReportConfigTOs == null) {
				groupReportConfigTOs = new ArrayList<>();
				reportConfigTOByOption.put(groupReportConfigTO.offerOptionPk, groupReportConfigTOs);
			}
			groupReportConfigTOs.add(groupReportConfigTO);
		}
		return groupReportConfigTOByOptionPkByReportingGroupPk;
	}
}

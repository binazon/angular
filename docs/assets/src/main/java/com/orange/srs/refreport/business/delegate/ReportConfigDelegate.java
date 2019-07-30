package com.orange.srs.refreport.business.delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.GroupReportConfigDAO;
import com.orange.srs.refreport.consumer.dao.ReportConfigDAO;
import com.orange.srs.refreport.consumer.dao.ReportingGroupDAO;
import com.orange.srs.refreport.model.Criteria;
import com.orange.srs.refreport.model.GroupReportConfig;
import com.orange.srs.refreport.model.Indicator;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ParamType;
import com.orange.srs.refreport.model.Report;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.ReportOutput;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.CriteriaKeyTO;
import com.orange.srs.refreport.model.TO.ReportConfigIndicatorIdTO;
import com.orange.srs.refreport.model.TO.ReportConfigKeyTO;
import com.orange.srs.refreport.model.TO.ReportConfigParamTypeAliasTO;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorIdProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeAliasProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportConfigListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportConfigProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.enums.ComputeScopeEnum;

@Stateless
public class ReportConfigDelegate {

	@EJB
	private ReportConfigDAO reportConfigDAO;

	@EJB
	private ReportingGroupDAO reportingGroupDAO;

	@EJB
	private GroupReportConfigDAO groupReportConfigDAO;

	@EJB
	private ReportDelegate reportDelegate;

	@EJB
	private CriteriaDelegate criteriaDelegate;

	@EJB
	private OfferOptionDelegate offerOptionDelegate;

	public static ReportConfigKeyTO getReportConfigKey(ReportConfig reportConfig) {
		return new ReportConfigKeyTO(reportConfig.getAlias());
	}

	public static ReportConfigKeyTO getReportConfigProvisioningTOKey(
			ReportConfigProvisioningTO reportConfigProvisioningTO) {
		return new ReportConfigKeyTO(reportConfigProvisioningTO.alias + ReportConfig.REPORT_CONFIG_ALIAS_SEPARATOR
				+ reportConfigProvisioningTO.reportRefId + ReportConfig.REPORT_CONFIG_ALIAS_SEPARATOR
				+ reportConfigProvisioningTO.type);
	}

	public ReportConfig createReportConfigAndSingleLinks(ReportConfigProvisioningTO reportConfigProvisioningTO,
			String offerOptionAlias, ReportOutput reportOutput) throws BusinessException {

		ReportConfig reportConfig = new ReportConfig();
		reportConfig.setAlias(reportConfigProvisioningTO.alias + ReportConfig.REPORT_CONFIG_ALIAS_SEPARATOR
				+ reportConfigProvisioningTO.reportRefId + ReportConfig.REPORT_CONFIG_ALIAS_SEPARATOR
				+ reportConfigProvisioningTO.type);
		reportConfig.setAssociatedReportOutput(reportOutput);
		reportConfig.setComputeScope(ComputeScopeEnum.valueOf(reportConfigProvisioningTO.computeScope));
		reportConfig.setReportDefaultVersion(reportConfigProvisioningTO.reportVersion);
		reportConfig.setType(reportConfigProvisioningTO.type);
		reportConfig.setOptional(reportConfigProvisioningTO.optional);

		Report report = reportDelegate.getReportByKey(reportConfigProvisioningTO.reportRefId);
		reportConfig.setAssociatedReport(report);

		Criteria criteria = criteriaDelegate.getCriteriaByKey(reportConfigProvisioningTO.criteriaProvisioningTO.type,
				reportConfigProvisioningTO.criteriaProvisioningTO.value);
		reportConfig.setCriteria(criteria);

		OfferOption offerOption = offerOptionDelegate.getOfferOptionByKey(offerOptionAlias);
		reportConfig.setOfferOption(offerOption);

		reportConfigDAO.persistAndFlush(reportConfig);
		return reportConfig;
	}

	public void createReportConfigIndicatorLink(ReportConfig reportConfig, Indicator indicator) {
		reportConfig.getIndicators().add(indicator);
		reportConfigDAO.persistAndFlush(reportConfig);
	}

	public void removeReportConfigIndicatorLink(ReportConfig reportConfig, Indicator indicator) {
		reportConfig.getIndicators().remove(indicator);
		reportConfigDAO.persistAndFlush(reportConfig);
	}

	public int createGroupReportConfigsForReportConfig(ReportConfig reportConfig, String offerOptionAlias) {

		Criteria defaultCriteria = CriteriaDelegate.getDefaultCriteria();

		List<ReportingGroup> reportingGroups = reportingGroupDAO.findReportingGroupForOfferOption(offerOptionAlias);
		for (ReportingGroup reportingGroup : reportingGroups) {

			GroupReportConfig groupReportConfig = new GroupReportConfig();
			groupReportConfig.setReportConfig(reportConfig);
			groupReportConfig.setEnable(true);
			groupReportConfig.setCriteria(defaultCriteria);
			groupReportConfig.setReportVersion(reportConfig.getReportDefaultVersion());
			groupReportConfig.setReportingGroup(reportingGroup);

			// Do not remove this part !!!! relationship doesn't use "mappedBy"
			reportingGroup.getGroupReportConfigs().add(groupReportConfig);
			groupReportConfigDAO.persistAndFlush(groupReportConfig);
		}

		return reportingGroups.size();
	}

	public boolean updateReportConfigAndSingleLinksIfNecessary(ReportConfig reportConfig,
			ReportConfigProvisioningTO reportConfigProvisioningTO) throws BusinessException {

		boolean updated = false;

		ComputeScopeEnum computeScopeEnumTO = ComputeScopeEnum.valueOf(reportConfigProvisioningTO.computeScope);
		String reportRefIdCurrent = reportConfig.getAssociatedReport().getRefId();
		CriteriaKeyTO criteriaKeyTOCurrent = CriteriaDelegate.getCriteriaKey(reportConfig.getCriteria());
		CriteriaKeyTO criterieKeyTONew = CriteriaDelegate
				.getCriteriaProvisioningTOKey(reportConfigProvisioningTO.criteriaProvisioningTO);

		if (!reportConfigProvisioningTO.type.equals(reportConfig.getType())
				|| !reportConfigProvisioningTO.reportVersion.equals(reportConfig.getReportDefaultVersion())
				|| !computeScopeEnumTO.equals(reportConfig.getComputeScope())
				|| !reportConfigProvisioningTO.reportRefId.equals(reportRefIdCurrent)
				|| criterieKeyTONew.compareTo(criteriaKeyTOCurrent) != 0) {

			reportConfig.setComputeScope(computeScopeEnumTO);
			reportConfig.setReportDefaultVersion(reportConfigProvisioningTO.reportVersion);
			reportConfig.setType(reportConfigProvisioningTO.type);
			reportConfig.setOptional(reportConfigProvisioningTO.optional);

			Report report = reportDelegate.getReportByKey(reportConfigProvisioningTO.reportRefId);
			reportConfig.setAssociatedReport(report);

			Criteria criteria = criteriaDelegate.getCriteriaByKey(
					reportConfigProvisioningTO.criteriaProvisioningTO.type,
					reportConfigProvisioningTO.criteriaProvisioningTO.value);
			reportConfig.setCriteria(criteria);

			reportConfigDAO.persistAndFlush(reportConfig);
			updated = true;
		}
		return updated;
	}

	public void removeReportConfig(ReportConfig reportConfig) {
		for (GroupReportConfig groupReportConfig : reportConfig.getGroupReportConfigs()) {
			groupReportConfig.getReportingGroup().getGroupReportConfigs().remove(groupReportConfig);
		}
		reportConfigDAO.remove(reportConfig);
	}

	public void createReportConfigParamTypeLink(ReportConfig reportConfig, ParamType paramType) {
		reportConfig.getParamTypes().add(paramType);
		reportConfigDAO.persistAndFlush(reportConfig);
	}

	public void removeReportConfigParamTypeLink(ReportConfig reportConfig, ParamType paramType) {
		reportConfig.getParamTypes().remove(paramType);
		reportConfigDAO.persistAndFlush(reportConfig);
	}

	public Map<Long, List<String>> getAllIndicatorIdByReportConfigPk() {
		Map<Long, List<String>> indicatorIdByReportConfigPk = new HashMap<>();
		for (ReportConfigIndicatorIdTO reportConfigIndicatorIdTO : reportConfigDAO
				.getAllIndicatorIdAndReportConfigPk()) {
			List<String> indicatorIdForCurrentReportConfigPk = indicatorIdByReportConfigPk
					.get(reportConfigIndicatorIdTO.reportConfigPk);
			if (indicatorIdForCurrentReportConfigPk == null) {
				indicatorIdForCurrentReportConfigPk = new ArrayList<>();
				indicatorIdByReportConfigPk.put(reportConfigIndicatorIdTO.reportConfigPk,
						indicatorIdForCurrentReportConfigPk);
			}
			indicatorIdForCurrentReportConfigPk.add(reportConfigIndicatorIdTO.indicatorId);
		}
		return indicatorIdByReportConfigPk;
	}

	public Map<Long, List<String>> getAllParamTypeAliasByReportConfigPk() {
		Map<Long, List<String>> paramTypeAliasByReportConfigPk = new HashMap<>();
		for (ReportConfigParamTypeAliasTO reportConfigParamTypeAliasTO : reportConfigDAO
				.getAllParamTypeAliasAndReportConfigPk()) {
			List<String> paramTypeAliasForCurrentReportConfigPk = paramTypeAliasByReportConfigPk
					.get(reportConfigParamTypeAliasTO.reportConfigPk);
			if (paramTypeAliasForCurrentReportConfigPk == null) {
				paramTypeAliasForCurrentReportConfigPk = new ArrayList<>();
				paramTypeAliasByReportConfigPk.put(reportConfigParamTypeAliasTO.reportConfigPk,
						paramTypeAliasForCurrentReportConfigPk);
			}
			paramTypeAliasForCurrentReportConfigPk.add(reportConfigParamTypeAliasTO.paramTypeAlias);
		}
		return paramTypeAliasByReportConfigPk;
	}

	public ReportConfigListProvisioningTO getReportConfigListProvisioningTOSortedForOfferOption(
			String offerOptionAlias) {
		Map<Long, List<String>> indicatorIdByReportPk = getAllIndicatorIdByReportConfigPk();
		Map<Long, List<String>> paramTypeAliasByReportConfigPk = getAllParamTypeAliasByReportConfigPk();
		List<ReportConfigProvisioningTO> reportConfigProvisioningTOs = reportConfigDAO
				.findAllReportConfigProvisioningTOWithoutParamTypeForOfferOption(offerOptionAlias);
		for (ReportConfigProvisioningTO reportConfigProvisioningTO : reportConfigProvisioningTOs) {
			// Fill the existing paramTypes for the reportConfig
			List<String> paramTypeAliasForCurrentReportConfigPk = paramTypeAliasByReportConfigPk
					.get(reportConfigProvisioningTO.reportConfigPk);
			if (paramTypeAliasForCurrentReportConfigPk != null) {
				for (String paramTypeAlias : paramTypeAliasForCurrentReportConfigPk) {
					ParamTypeAliasProvisioningTO paramTypeAliasProvisioningTO = new ParamTypeAliasProvisioningTO();
					paramTypeAliasProvisioningTO.alias = paramTypeAlias;
					reportConfigProvisioningTO.paramTypeAliasProvisioningTOs.add(paramTypeAliasProvisioningTO);
				}
				ParamTypeDelegate
						.sortParamTypeAliasProvisioningTO(reportConfigProvisioningTO.paramTypeAliasProvisioningTOs);
			}
			// Fill the existing indicators for the reportConfig
			List<String> indicatorIdForCurrentReportConfigPk = indicatorIdByReportPk
					.get(reportConfigProvisioningTO.reportConfigPk);
			if (indicatorIdForCurrentReportConfigPk != null) {
				for (String indicatorId : indicatorIdForCurrentReportConfigPk) {
					IndicatorIdProvisioningTO indicatorIdProvisioningTO = new IndicatorIdProvisioningTO();
					indicatorIdProvisioningTO.id = indicatorId;
					reportConfigProvisioningTO.indicatorIdProvisioningTOs.add(indicatorIdProvisioningTO);
				}
				IndicatorDelegate.sortIndicatorIdProvisioningTO(reportConfigProvisioningTO.indicatorIdProvisioningTOs);
			}
			// Remove refId and reportConfigType from the alias found in database to build
			// fill data in the provisioning file
			String endReportConfigAlias = ReportConfig.REPORT_CONFIG_ALIAS_SEPARATOR
					+ reportConfigProvisioningTO.reportRefId + ReportConfig.REPORT_CONFIG_ALIAS_SEPARATOR
					+ reportConfigProvisioningTO.type;
			reportConfigProvisioningTO.alias = reportConfigProvisioningTO.alias.substring(0,
					reportConfigProvisioningTO.alias.length() - endReportConfigAlias.length());
		}
		sortReportConfigProvisioningTO(reportConfigProvisioningTOs);
		ReportConfigListProvisioningTO reportConfigListProvisioningTO = new ReportConfigListProvisioningTO();
		reportConfigListProvisioningTO.reportConfigProvisioningTOs = reportConfigProvisioningTOs;
		return reportConfigListProvisioningTO;
	}

	public List<ReportConfig> getAllReportConfigSortedForOfferOption(String offerOptionAlias) {
		List<ReportConfig> reportConfigs = reportConfigDAO
				.findBy(ReportConfig.FIELD_OFFER_OPTION + '.' + OfferOption.FIELD_ALIAS, offerOptionAlias);
		Collections.sort(reportConfigs, new ReportConfigComparator());
		return reportConfigs;
	}

	public Set<String> findAllReportRefIdLinkedToReportConfigForOfferOption(String offerOptionAlias) {
		Set<String> reportRefIds = new HashSet<>();
		reportRefIds.addAll(reportConfigDAO.findAllReportRefIdLinkedToReportConfigForOfferOption(offerOptionAlias));
		return reportRefIds;
	}

	public static void sortReportConfigProvisioningTO(List<ReportConfigProvisioningTO> reportConfigProvisioningTOs) {
		Collections.sort(reportConfigProvisioningTOs, new ReportConfigProvisioningTOComparator());
	}

	private static class ReportConfigComparator implements Comparator<ReportConfig> {
		@Override
		public int compare(ReportConfig firstObj, ReportConfig secondObj) {
			return getReportConfigKey(firstObj).compareTo(getReportConfigKey(secondObj));
		}
	}

	private static class ReportConfigProvisioningTOComparator implements Comparator<ReportConfigProvisioningTO> {
		@Override
		public int compare(ReportConfigProvisioningTO firstObj, ReportConfigProvisioningTO secondObj) {
			return getReportConfigProvisioningTOKey(firstObj).compareTo(getReportConfigProvisioningTOKey(secondObj));
		}
	}

}

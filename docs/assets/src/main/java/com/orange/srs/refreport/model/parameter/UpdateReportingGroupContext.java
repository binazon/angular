package com.orange.srs.refreport.model.parameter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.GroupingRule;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ReportConfig;
import com.orange.srs.refreport.model.TO.FilterConfigTO;
import com.orange.srs.refreport.model.TO.GroupReportConfigTO;
import com.orange.srs.statcommon.model.enums.OriginEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.provisioning.CreateReportingGroupParameter;

public class UpdateReportingGroupContext {

	public SOAContext soaContext;
	public Date provisioningDate;
	public Map<String, Filter> hashFilters;
	public Map<Long, Map<Long, List<FilterConfigTO>>> filterConfigTOByOptionPkByReportingGroupPk = new HashMap<>();
	public Map<String, Set<String>> filterIdsByOfferOptionAlias = new HashMap<>();
	public Map<String, Set<String>> defaultFilterIdsByOfferOptionAlias = new HashMap<>();
	@Deprecated
	public long lastReportingGroupPk; // TODO JLN 16/06/2015 Remove this line when provisioning done with RefObject

	public List<OfferOption> newOfferOptions = new ArrayList<>();
	public Set<OfferOption> offerOptions = new HashSet<>();
	public List<OfferOption> deletedOfferOptions = new ArrayList<>();
	public Set<String> filterIds = new HashSet<>();

	public Map<Long, List<GroupingRule>> groupingRulesByReportingGroupPk = new HashMap<>();
	public Set<GroupingRule> groupingRules = new HashSet<>();
	public List<GroupingRule> groupingRulesToRemove = new ArrayList<>();

	public Set<String> optionalReportConfigs = new HashSet<>();
	public Map<Long, Map<Long, List<GroupReportConfigTO>>> groupReportConfigByOptionPkByReportingGroupPk = new HashMap<>();
	public Map<String, ReportConfig> hashAdditionalReportConfigs = new HashMap<>();

	public Map<String, OfferOption> hashOfferOptions = new HashMap<>();
	public List<CreateReportingGroupParameter> groupsInDB;
	public Map<OriginEnum, Set<String>> reportingGroupRefsByOrigin = new HashMap<>();

	public long timeInit = 0L;
	public long timeUnmarshal = 0L;
	public long timeGetFromDB = 0L;
	public long timeUpdateCriteriaAndDataLocation = 0L;
	public long timeUpdateGroupingRules = 0L;
	public long timeUpdateOptionAndReports = 0L;
	public long timePersist = 0L;

	public void clearOffers() {
		newOfferOptions.clear();
		deletedOfferOptions.clear();
		offerOptions.clear();
	}

	public void clearReportingGroups() {
		reportingGroupRefsByOrigin.clear();
	}
}

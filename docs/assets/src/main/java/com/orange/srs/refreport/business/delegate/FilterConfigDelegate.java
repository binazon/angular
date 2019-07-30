package com.orange.srs.refreport.business.delegate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.consumer.dao.FilterConfigDAO;
import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.FilterConfig;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.ReportingGroup;
import com.orange.srs.refreport.model.TO.FilterConfigTO;

@Stateless
public class FilterConfigDelegate {

	@EJB
	private FilterDelegate filterDelegate;

	@EJB
	private FilterConfigDAO filterConfigDAO;

	public void createFilterConfig(Filter filter, OfferOption offerOption, ReportingGroup reportingGroup) {
		filterConfigDAO.persistAndFlush(createFilterConfigWithoutPersist(filter, offerOption, reportingGroup));
	}

	public FilterConfig createFilterConfigWithoutPersist(Filter filter, OfferOption offerOption,
			ReportingGroup reportingGroup) {
		FilterConfig filterConfig = new FilterConfig();
		filterConfig.setFilter(filter);
		filterConfig.setOfferOption(offerOption);
		filterConfig.setReportingGroup(reportingGroup);
		return filterConfig;
	}

	public void removeFilterConfig(FilterConfig filterConfig) {
		filterConfigDAO.remove(filterConfig);
	}

	public Map<Long, Map<Long, List<FilterConfigTO>>> getFilterConfigTOByOptionPkByReportingGroupPk() {
		Map<Long, Map<Long, List<FilterConfigTO>>> filterConfigTOByOptionPkByReportingGroupPk = new HashMap<>();
		for (FilterConfigTO filterConfigTO : filterConfigDAO.getFilterConfigTO()) {
			Map<Long, List<FilterConfigTO>> filterConfigTOByOption = filterConfigTOByOptionPkByReportingGroupPk
					.get(filterConfigTO.reportingGroupPk);
			if (filterConfigTOByOption == null) {
				filterConfigTOByOption = new HashMap<>();
				filterConfigTOByOptionPkByReportingGroupPk.put(filterConfigTO.reportingGroupPk, filterConfigTOByOption);
			}
			List<FilterConfigTO> filterConfigTOs = filterConfigTOByOption.get(filterConfigTO.offerOptionPk);
			if (filterConfigTOs == null) {
				filterConfigTOs = new ArrayList<>();
				filterConfigTOByOption.put(filterConfigTO.offerOptionPk, filterConfigTOs);
			}
			filterConfigTOs.add(filterConfigTO);
		}
		return filterConfigTOByOptionPkByReportingGroupPk;
	}

	public List<FilterConfig> getFilterConfigsForOptionAndReportingGroup(OfferOption offerOption,
			ReportingGroup reportingGroup) {
		return filterConfigDAO.getFilterConfigsForOptionAndReportingGroup(offerOption.getAlias(),
				reportingGroup.getOrigin(), reportingGroup.getReportingGroupRef());
	}

}

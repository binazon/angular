package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.orange.srs.refreport.business.delegate.FilterDelegate;
import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.TO.FilterKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.FilterProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class FilterDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<FilterProvisioningTO, Filter, FilterKeyTO> {

	@EJB
	private FilterDelegate filterDelegate;

	@Inject
	private FilterOfferOptionLinkDifferentialUpdate filterOfferOptionLinkDifferentialUpdate;

	@Override
	protected void sortProvisioningTOs(List<FilterProvisioningTO> filterProvisioningTO) {
		FilterDelegate.sortFilterProvisioningTO(filterProvisioningTO);
	}

	@Override
	protected List<Filter> getModelObjectsSorted() {
		return filterDelegate.getAllFilterSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<FilterProvisioningTO> filterProvisioningTO) throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(FilterProvisioningTO filterProvisioningTO) throws BusinessException {
		// No functional checks
	}

	@Override
	protected FilterKeyTO getProvisioningTOKey(FilterProvisioningTO filterProvisioningTO) {
		return FilterDelegate.getFilterProvisioningTOKey(filterProvisioningTO);
	}

	@Override
	protected FilterKeyTO getModelObjectKey(Filter filter) {
		return FilterDelegate.getFilterKey(filter);
	}

	@Override
	protected Boolean getSuppressFlag(FilterProvisioningTO filterProvisioningTO) {
		return filterProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(Filter filter) {
		filterDelegate.removeFilter(filter);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, Filter filter,
			FilterProvisioningTO filterProvisioningTO) throws BusinessException {

		// Update filter if necessary
		boolean filterHasBeenUpdated = filterDelegate.updateFilterIfNecessary(filter, filterProvisioningTO, soaContext);

		// Update filter offeroption links if necessary
		filterOfferOptionLinkDifferentialUpdate.setFilter(filter);
		boolean offerOptionLinkHasBeenUpdated = filterOfferOptionLinkDifferentialUpdate.updateByDifferential(soaContext,
				filterProvisioningTO.offerOptionAliasProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);

		return filterHasBeenUpdated || offerOptionLinkHasBeenUpdated;
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext, FilterProvisioningTO filterProvisioningTO)
			throws BusinessException {
		Filter filter = filterDelegate.createFilter(filterProvisioningTO, soaContext);
		filterOfferOptionLinkDifferentialUpdate.setFilter(filter);
		filterOfferOptionLinkDifferentialUpdate.updateByDifferential(soaContext,
				filterProvisioningTO.offerOptionAliasProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "filter";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}

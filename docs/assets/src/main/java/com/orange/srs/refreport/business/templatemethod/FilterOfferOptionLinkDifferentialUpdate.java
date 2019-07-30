package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.OfferOptionDelegate;
import com.orange.srs.refreport.consumer.dao.FilterToOfferOptionDAO;
import com.orange.srs.refreport.model.Filter;
import com.orange.srs.refreport.model.FilterToOfferOption;
import com.orange.srs.refreport.model.FilterToOfferOptionId;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.TO.OfferOptionKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferOptionLinkProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class FilterOfferOptionLinkDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<OfferOptionLinkProvisioningTO, OfferOption, OfferOptionKeyTO> {

	@EJB
	private FilterToOfferOptionDAO filterToOfferOptionDAO;

	@EJB
	private OfferOptionDelegate offerOptionDelegate;

	private Filter filter;

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	@Override
	protected void sortProvisioningTOs(List<OfferOptionLinkProvisioningTO> offerOptionLinkProvisioningTOs) {
		OfferOptionDelegate.sortOfferOptionLinkProvisioningTO(offerOptionLinkProvisioningTOs);
	}

	@Override
	protected List<OfferOption> getModelObjectsSorted() {
		return offerOptionDelegate.getAllOfferOptionSortedForFilter(filter);
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<OfferOptionLinkProvisioningTO> offerOptionLinkProvisioningTOs) {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(OfferOptionLinkProvisioningTO offerOptionLinkProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected OfferOptionKeyTO getProvisioningTOKey(OfferOptionLinkProvisioningTO offerOptionLinkProvisioningTO) {
		return OfferOptionDelegate.getOfferOptionLinkProvisioningTOKey(offerOptionLinkProvisioningTO);
	}

	@Override
	protected OfferOptionKeyTO getModelObjectKey(OfferOption offerOption) {
		return OfferOptionDelegate.getOfferOptionKey(offerOption);
	}

	@Override
	protected Boolean getSuppressFlag(OfferOptionLinkProvisioningTO offerOptionLinkProvisioningTO) {
		return offerOptionLinkProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(OfferOption offerOption) {
		FilterToOfferOptionId filterToOfferOptionId = new FilterToOfferOptionId();
		filterToOfferOptionId.setFilter(filter);
		filterToOfferOptionId.setOfferOption(offerOption);
		FilterToOfferOption filterToOfferOption = filterToOfferOptionDAO.findById(filterToOfferOptionId);
		filterToOfferOptionDAO.remove(filterToOfferOption);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, OfferOption offerOption,
			OfferOptionLinkProvisioningTO offerOptionLinkProvisioningTO) {
		FilterToOfferOptionId filterToOfferOptionId = new FilterToOfferOptionId();
		filterToOfferOptionId.setFilter(filter);
		filterToOfferOptionId.setOfferOption(offerOption);
		FilterToOfferOption filterToOfferOption = filterToOfferOptionDAO.findById(filterToOfferOptionId);
		boolean hasBeenUpdated = false;
		if (filterToOfferOption.isDefaultForAllGroups() != offerOptionLinkProvisioningTO.defaultForAllGroups) {
			filterToOfferOption.setDefaultForAllGroups(offerOptionLinkProvisioningTO.defaultForAllGroups);
			hasBeenUpdated = true;
		}
		return hasBeenUpdated;
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			OfferOptionLinkProvisioningTO offerOptionLinkProvisioningTO) throws BusinessException {
		OfferOption offerOption = offerOptionDelegate.getOfferOptionByKey(offerOptionLinkProvisioningTO.alias);
		FilterToOfferOption filterToOfferOption = new FilterToOfferOption();
		filterToOfferOption.setFilter(filter);
		filterToOfferOption.setOfferOption(offerOption);
		filterToOfferOption.setDefaultForAllGroups(offerOptionLinkProvisioningTO.defaultForAllGroups);
		filterToOfferOptionDAO.persistAndFlush(filterToOfferOption);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "offerOption link";
	}

	@Override
	protected String getEndLogMessage() {
		return " for filter " + filter.getFilterId();
	}

}

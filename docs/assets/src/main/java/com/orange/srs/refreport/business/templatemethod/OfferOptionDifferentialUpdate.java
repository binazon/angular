package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.OfferOptionDelegate;
import com.orange.srs.refreport.model.Offer;
import com.orange.srs.refreport.model.OfferOption;
import com.orange.srs.refreport.model.TO.OfferOptionKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferOptionProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class OfferOptionDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<OfferOptionProvisioningTO, OfferOption, OfferOptionKeyTO> {

	@EJB
	private OfferOptionDelegate offerOptionDelegate;

	private Offer offer;

	public void setOffer(Offer offer) {
		this.offer = offer;
	}

	@Override
	protected void sortProvisioningTOs(List<OfferOptionProvisioningTO> offerOptionProvisioningTOs) {
		OfferOptionDelegate.sortOfferOptionProvisioningTO(offer.getAlias(), offerOptionProvisioningTOs);
	}

	@Override
	protected List<OfferOption> getModelObjectsSorted() {
		return offerOptionDelegate.getAllOfferOptionSortedForOffer(offer);
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<OfferOptionProvisioningTO> offerOptionProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(OfferOptionProvisioningTO offerOptionProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected OfferOptionKeyTO getProvisioningTOKey(OfferOptionProvisioningTO offerOptionProvisioningTO) {
		return OfferOptionDelegate.getOfferOptionProvisioningTOKey(offer.getAlias(), offerOptionProvisioningTO);
	}

	@Override
	protected OfferOptionKeyTO getModelObjectKey(OfferOption offerOption) {
		return OfferOptionDelegate.getOfferOptionKey(offerOption);
	}

	@Override
	protected Boolean getSuppressFlag(OfferOptionProvisioningTO offerOptionProvisioningTO) {
		return offerOptionProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(OfferOption offerOption) {
		offerOptionDelegate.removeOfferOption(offerOption);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, OfferOption offerOption,
			OfferOptionProvisioningTO offerOptionProvisioningTO) throws BusinessException {
		return offerOptionDelegate.updateOfferOptionIfNecessary(offerOption, offerOptionProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			OfferOptionProvisioningTO offerOptionProvisioningTO) throws BusinessException {
		offerOptionDelegate.createOfferOption(offer, offerOptionProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "offerOption";
	}

	@Override
	protected String getEndLogMessage() {
		return " for offer " + offer.getAlias();
	}

}

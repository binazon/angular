package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.orange.srs.refreport.business.delegate.OfferDelegate;
import com.orange.srs.refreport.model.Offer;
import com.orange.srs.refreport.model.TO.OfferKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferAndOptionProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class OfferAndOptionDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<OfferAndOptionProvisioningTO, Offer, OfferKeyTO> {

	@EJB
	private OfferDelegate offerDelegate;

	@Inject
	private OfferOptionDifferentialUpdate offerOptionDifferentialUpdate;

	@Override
	protected void sortProvisioningTOs(List<OfferAndOptionProvisioningTO> offerAndOptionProvisioningTOs) {
		OfferDelegate.sortOfferAndOptionProvisioningTO(offerAndOptionProvisioningTOs);
	}

	@Override
	protected List<Offer> getModelObjectsSorted() {
		return offerDelegate.getAllOfferSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<OfferAndOptionProvisioningTO> offerAndOptionProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(OfferAndOptionProvisioningTO offerAndOptionProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected OfferKeyTO getProvisioningTOKey(OfferAndOptionProvisioningTO offerAndOptionProvisioningTO) {
		return OfferDelegate.getOfferAndOptionProvisioningTOKey(offerAndOptionProvisioningTO);
	}

	@Override
	protected OfferKeyTO getModelObjectKey(Offer offer) {
		return OfferDelegate.getOfferKey(offer);
	}

	@Override
	protected Boolean getSuppressFlag(OfferAndOptionProvisioningTO offerAndOptionProvisioningTO) {
		return offerAndOptionProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(Offer offer) {
		offerDelegate.removeOffer(offer);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, Offer offer,
			OfferAndOptionProvisioningTO offerAndOptionProvisioningTO) throws BusinessException {
		boolean offerHasBeenUpdated = offerDelegate.updateOfferIfNecessary(offer, offerAndOptionProvisioningTO);
		offerOptionDifferentialUpdate.setOffer(offer);
		boolean offerOptionHasBeenUpdated = offerOptionDifferentialUpdate.updateByDifferential(soaContext,
				offerAndOptionProvisioningTO.offerOptionProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);
		return offerHasBeenUpdated || offerOptionHasBeenUpdated;
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			OfferAndOptionProvisioningTO offerAndOptionProvisioningTO) throws BusinessException {
		Offer createdOffer = offerDelegate.createOffer(offerAndOptionProvisioningTO);
		offerOptionDifferentialUpdate.setOffer(createdOffer);
		offerOptionDifferentialUpdate.updateByDifferential(soaContext,
				offerAndOptionProvisioningTO.offerOptionProvisioningTOs, FORCE_UPDATE_FROM_FILE_TO_DATABASE);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "offer";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}

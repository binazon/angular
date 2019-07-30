package com.orange.srs.refreport.business.templatemethod;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.HyperlinkDelegate;
import com.orange.srs.refreport.model.Hyperlink;
import com.orange.srs.refreport.model.TO.HyperlinkKeyTO;
import com.orange.srs.refreport.model.TO.provisioning.HyperlinkProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class HyperlinkDifferentialUpdate
		extends AbstractDifferentialUpdateTemplateMethod<HyperlinkProvisioningTO, Hyperlink, HyperlinkKeyTO> {

	@EJB
	private HyperlinkDelegate hyperlinkDelegate;

	@Override
	protected void sortProvisioningTOs(List<HyperlinkProvisioningTO> hyperlinkProvisioningTOs) {
		HyperlinkDelegate.sortHyperlinkProvisioningTO(hyperlinkProvisioningTOs);
	}

	@Override
	protected List<Hyperlink> getModelObjectsSorted() {
		return hyperlinkDelegate.getAllHyperlinkSorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<HyperlinkProvisioningTO> hyperlinkProvisioningTOs)
			throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(HyperlinkProvisioningTO hyperlinkProvisioningTO)
			throws BusinessException {
		// No functional checks
	}

	@Override
	protected HyperlinkKeyTO getProvisioningTOKey(HyperlinkProvisioningTO hyperlinkProvisioningTO) {
		return HyperlinkDelegate.getHyperlinkProvisioningTOKey(hyperlinkProvisioningTO);
	}

	@Override
	protected HyperlinkKeyTO getModelObjectKey(Hyperlink hyperlink) {
		return HyperlinkDelegate.getHyperlinkKey(hyperlink);
	}

	@Override
	protected Boolean getSuppressFlag(HyperlinkProvisioningTO hyperlinkProvisioningTO) {
		return hyperlinkProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(Hyperlink hyperlink) {
		hyperlinkDelegate.removeHyperlink(hyperlink);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, Hyperlink hyperlink,
			HyperlinkProvisioningTO hyperlinkProvisioningTO) throws BusinessException {
		return hyperlinkDelegate.updateHyperlinkIfNecessary(hyperlink, hyperlinkProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext,
			HyperlinkProvisioningTO hyperlinkProvisioningTO) throws BusinessException {
		hyperlinkDelegate.createHyperlink(hyperlinkProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "hyperlink";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}

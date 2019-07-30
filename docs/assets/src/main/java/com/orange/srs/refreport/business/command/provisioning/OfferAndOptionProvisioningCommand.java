package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.OfferDelegate;
import com.orange.srs.refreport.model.TO.provisioning.OfferAndOptionListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class OfferAndOptionProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(OfferAndOptionProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private OfferDelegate offerDelegate;

	@Override
	public void execute(SOAContext soaContext, File offerAndOptionProvisioningFile,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[offerAndOptionProvisioningCommand] provisioning file "
				+ offerAndOptionProvisioningFile.getAbsolutePath()));

		OfferAndOptionListProvisioningTO offerAndOptionListProvisioningTOUnmarshalled = (OfferAndOptionListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				offerAndOptionProvisioningFile);
		provisioningFacade.updateOfferAndOptionByDifferential(soaContext,
				offerAndOptionListProvisioningTOUnmarshalled.offerAndOptionProvisioningTOs,
				forceUpdateFromFileToDatabase);
		if (!forceUpdateFromFileToDatabase) {
			OfferAndOptionListProvisioningTO fferAndOptionListProvisioningTOToMarshall = offerDelegate
					.getOfferAndOptionListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(fferAndOptionListProvisioningTOToMarshall,
					offerAndOptionProvisioningFile, false);
		}
	}

}

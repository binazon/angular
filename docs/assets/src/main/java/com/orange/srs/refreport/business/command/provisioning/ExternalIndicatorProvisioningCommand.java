package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.ExternalIndicatorDelegate;
import com.orange.srs.refreport.model.TO.provisioning.ExternalIndicatorListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class ExternalIndicatorProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(ExternalIndicatorProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private ExternalIndicatorDelegate externalIndicatorDelegate;

	@Override
	public void execute(SOAContext soaContext, File externalIndicatorProvisioningFile,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[externalIndicatorProvisioningCommand] provisioning file "
				+ externalIndicatorProvisioningFile.getAbsolutePath()));

		ExternalIndicatorListProvisioningTO externalIndicatorListProvisioningTOUnmarshalled = (ExternalIndicatorListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				externalIndicatorProvisioningFile);
		provisioningFacade.updateExternalIndicatorByDifferential(soaContext,
				externalIndicatorListProvisioningTOUnmarshalled.externalIndicatorProvisioningTOs,
				forceUpdateFromFileToDatabase);
		if (!forceUpdateFromFileToDatabase) {
			ExternalIndicatorListProvisioningTO externalIndicatorListProvisioningTOToMarshall = externalIndicatorDelegate
					.getExternalIndicatorListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(externalIndicatorListProvisioningTOToMarshall,
					externalIndicatorProvisioningFile, false);
		}
	}

}

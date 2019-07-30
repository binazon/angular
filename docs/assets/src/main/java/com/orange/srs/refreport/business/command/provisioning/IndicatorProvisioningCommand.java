package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.IndicatorDelegate;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class IndicatorProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(IndicatorProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private IndicatorDelegate indicatorDelegate;

	@Override
	public void execute(SOAContext soaContext, File indicatorProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[indicatorProvisioningCommand] provisioning file " + indicatorProvisioningFile.getAbsolutePath()));

		IndicatorListProvisioningTO indicatorListProvisioningTOUnmarshalled = (IndicatorListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				indicatorProvisioningFile);
		provisioningFacade.updateIndicatorByDifferential(soaContext,
				indicatorListProvisioningTOUnmarshalled.indicatorProvisioningTOs, forceUpdateFromFileToDatabase);
		if (!forceUpdateFromFileToDatabase) {
			IndicatorListProvisioningTO indicatorListProvisioningTOToMarshall = indicatorDelegate
					.getIndicatorListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(indicatorListProvisioningTOToMarshall, indicatorProvisioningFile,
					false);
		}
	}

}

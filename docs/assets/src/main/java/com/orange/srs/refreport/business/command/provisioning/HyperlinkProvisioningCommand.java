package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.HyperlinkDelegate;
import com.orange.srs.refreport.model.TO.provisioning.HyperlinkListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class HyperlinkProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(HyperlinkProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private HyperlinkDelegate hyperlinkDelegate;

	@Override
	public void execute(SOAContext soaContext, File hyperlinkProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[hyperlinkProvisioningCommand] provisioning file " + hyperlinkProvisioningFile.getAbsolutePath()));

		HyperlinkListProvisioningTO hyperlinkListProvisioningTOUnmarshalled = (HyperlinkListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				hyperlinkProvisioningFile);
		provisioningFacade.updateHyperlinkByDifferential(soaContext,
				hyperlinkListProvisioningTOUnmarshalled.hyperlinkProvisioningTOs, forceUpdateFromFileToDatabase);
		if (!forceUpdateFromFileToDatabase) {
			HyperlinkListProvisioningTO hyperlinkListProvisioningTOToMarshall = hyperlinkDelegate
					.getHyperlinkListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(hyperlinkListProvisioningTOToMarshall, hyperlinkProvisioningFile,
					false);
		}
	}

}

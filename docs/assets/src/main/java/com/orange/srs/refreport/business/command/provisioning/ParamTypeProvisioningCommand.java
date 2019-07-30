package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.ParamTypeDelegate;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class ParamTypeProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(ParamTypeProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private ParamTypeDelegate paramTypeDelegate;

	@Override
	public void execute(SOAContext soaContext, File paramTypeProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[paramTypeProvisioningCommand] provisioning file " + paramTypeProvisioningFile.getAbsolutePath()));

		ParamTypeListProvisioningTO paramTypeListProvisioningTOUnmarshalled = (ParamTypeListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				paramTypeProvisioningFile);
		provisioningFacade.updateParamTypeByDifferential(soaContext,
				paramTypeListProvisioningTOUnmarshalled.paramTypeProvisioningTOs, forceUpdateFromFileToDatabase);

		if (!forceUpdateFromFileToDatabase) {
			ParamTypeListProvisioningTO paramTypeListProvisioningTOToMarshall = paramTypeDelegate
					.getParamTypeListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(paramTypeListProvisioningTOToMarshall, paramTypeProvisioningFile,
					false);
		}
	}

}

package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.EntityTypeAndSubtypeDelegate;
import com.orange.srs.refreport.model.TO.provisioning.TypeSubtypeListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class TypeSubtypeProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(TypeSubtypeProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private EntityTypeAndSubtypeDelegate entityTypeAndSubtypeDelegate;

	@Override
	public void execute(SOAContext soaContext, File typeSubtypeProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[typeSubtypeProvisioningCommand] provisioning file " + typeSubtypeProvisioningFile.getAbsolutePath()));

		TypeSubtypeListProvisioningTO typeSubtypeListProvisioningTOUnmarshalled = (TypeSubtypeListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				typeSubtypeProvisioningFile);
		provisioningFacade.updateTypeSubtypeByDifferential(soaContext,
				typeSubtypeListProvisioningTOUnmarshalled.typeAndSubtypesProvisioningTOs,
				forceUpdateFromFileToDatabase);

		if (!forceUpdateFromFileToDatabase) {
			TypeSubtypeListProvisioningTO typeSubtypeListProvisioningTOToMarshall = entityTypeAndSubtypeDelegate
					.getTypeSubtypeListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(typeSubtypeListProvisioningTOToMarshall,
					typeSubtypeProvisioningFile, false);
		}
	}

}

package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.model.TO.provisioning.SourceClassListProvisioningTO;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class SourceClassProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(SourceClassProvisioningCommand.class);

	private static final String FILE_NAME_SOURCE_CLASS = "sourceClass.xml";

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@Override
	public void execute(SOAContext soaContext, File sourceClassProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[sourceClassProvisioningCommand] provisioning file " + sourceClassProvisioningFile.getAbsolutePath()));

		SourceClassListProvisioningTO sourceClassListProvisioningTOUnmarshalled = (SourceClassListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				sourceClassProvisioningFile);
		provisioningFacade.updateSourceClassByDifferentialAndMarshallToFile(soaContext,
				sourceClassListProvisioningTOUnmarshalled.sourceClassProvisioningTOs, sourceClassProvisioningFile,
				forceUpdateFromFileToDatabase);
	}

	public static String getSourceClassProvisioningFilePath() {
		return Configuration.rootProperty + File.separatorChar + Configuration.configSourceclassProperty
				+ File.separatorChar + FILE_NAME_SOURCE_CLASS;
	}

}

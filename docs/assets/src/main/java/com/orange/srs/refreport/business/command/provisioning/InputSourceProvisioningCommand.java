package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.report.AbstractInputSourceListProvisioningTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class InputSourceProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(InputSourceProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@Override
	public void execute(SOAContext soaContext, File inputSourceProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[inputSourceProvisioningCommand] provisioning file " + inputSourceProvisioningFile.getAbsolutePath()));

		AbstractInputSourceListProvisioningTO inputSourceListProvisioningTOUnmarshalled = (AbstractInputSourceListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				inputSourceProvisioningFile);
		String sourceClass = FilenameUtils.removeExtension(inputSourceProvisioningFile.getName());
		provisioningFacade.updateInputSourceByDifferentialAndMarshallToFile(soaContext,
				inputSourceListProvisioningTOUnmarshalled.inputSourceProvisioningTOs, sourceClass,
				inputSourceProvisioningFile, forceUpdateFromFileToDatabase);
	}

	public static String getInputSourceProvisioningDirectoryPath() {
		return Configuration.rootProperty + File.separatorChar + Configuration.configInputsourceProperty;
	}

}

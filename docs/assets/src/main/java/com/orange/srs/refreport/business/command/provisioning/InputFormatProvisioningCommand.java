package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.InputFormatDelegate;
import com.orange.srs.refreport.model.TO.provisioning.InputColumnListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class InputFormatProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(InputFormatProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private InputFormatDelegate inputFormatDelegate;

	@Override
	public void execute(SOAContext soaContext, File inputFormatProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[inputFormatProvisioningCommand] provisioning file " + inputFormatProvisioningFile.getAbsolutePath()));

		InputColumnListProvisioningTO inputColumnListProvisioningTOUnmarshalled = (InputColumnListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				inputFormatProvisioningFile);
		String formatType = FilenameUtils.removeExtension(inputFormatProvisioningFile.getName());
		provisioningFacade.updateInputFormatByDifferential(soaContext,
				inputColumnListProvisioningTOUnmarshalled.inputColumnProvisioningTOs, formatType,
				forceUpdateFromFileToDatabase);
		if (!forceUpdateFromFileToDatabase) {
			InputColumnListProvisioningTO inputColumnListProvisioningTOToMarshall = inputFormatDelegate
					.getInputColumnListProvisioningTOSortedForInputFormat(formatType);
			marshallProvisioningTOToProvisioningFile(inputColumnListProvisioningTOToMarshall,
					inputFormatProvisioningFile, true);
		}
	}
}

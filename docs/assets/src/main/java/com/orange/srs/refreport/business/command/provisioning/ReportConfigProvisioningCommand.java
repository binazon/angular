package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.ReportConfigDelegate;
import com.orange.srs.refreport.model.TO.provisioning.ReportConfigListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class ReportConfigProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(ReportConfigProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private ReportConfigDelegate reportConfigDelegate;

	@Override
	public void execute(SOAContext soaContext, File reportConfigProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[reportConfigProvisioningCommand] provisioning file "
				+ reportConfigProvisioningFile.getAbsolutePath()));

		ReportConfigListProvisioningTO reportConfigListProvisioningTOUnmarshalled = (ReportConfigListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				reportConfigProvisioningFile);
		String offerOptionAlias = FilenameUtils.removeExtension(reportConfigProvisioningFile.getName());
		provisioningFacade.updateReportConfigByDifferential(soaContext,
				reportConfigListProvisioningTOUnmarshalled.reportConfigProvisioningTOs, offerOptionAlias,
				forceUpdateFromFileToDatabase);

		if (!forceUpdateFromFileToDatabase) {
			ReportConfigListProvisioningTO reportConfigListProvisioningTOToMarshall = reportConfigDelegate
					.getReportConfigListProvisioningTOSortedForOfferOption(offerOptionAlias);
			marshallProvisioningTOToProvisioningFile(reportConfigListProvisioningTOToMarshall,
					reportConfigProvisioningFile, false);
		}
	}
}

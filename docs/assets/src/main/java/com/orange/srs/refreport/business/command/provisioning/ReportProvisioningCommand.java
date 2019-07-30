package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.ReportDelegate;
import com.orange.srs.refreport.model.TO.provisioning.ReportListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class ReportProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(ReportProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private ReportDelegate reportDelegate;

	@Override
	public void execute(SOAContext soaContext, File reportProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[reportProvisioningCommand] provisioning file " + reportProvisioningFile.getAbsolutePath()));

		ReportListProvisioningTO reportListProvisioningTOUnmarshalled = (ReportListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				reportProvisioningFile);
		provisioningFacade.updateReportByDifferential(soaContext,
				reportListProvisioningTOUnmarshalled.reportProvisioningTOs, forceUpdateFromFileToDatabase);

		if (!forceUpdateFromFileToDatabase) {
			ReportListProvisioningTO reportListProvisioningTOToMarshall = reportDelegate
					.getReportListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(reportListProvisioningTOToMarshall, reportProvisioningFile, false);
		}
	}

}

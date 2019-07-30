package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.ReportInputDelegate;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class ReportInputProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(ReportInputProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private ReportInputDelegate reportInputDelegate;

	@Override
	public void execute(SOAContext soaContext, File reportInputProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[reportInputProvisioningCommand] provisioning file " + reportInputProvisioningFile.getAbsolutePath()));

		ReportInputListProvisioningTO reportInputListProvisioningTOUnmarshalled = (ReportInputListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				reportInputProvisioningFile);
		provisioningFacade.updateReportInputByDifferential(soaContext,
				reportInputListProvisioningTOUnmarshalled.reportInputProvisioningTOs, forceUpdateFromFileToDatabase);

		if (!forceUpdateFromFileToDatabase) {
			ReportInputListProvisioningTO reportInputListProvisioningTOToMarshall = reportInputDelegate
					.getReportInputListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(reportInputListProvisioningTOToMarshall,
					reportInputProvisioningFile, false);
		}
	}

}

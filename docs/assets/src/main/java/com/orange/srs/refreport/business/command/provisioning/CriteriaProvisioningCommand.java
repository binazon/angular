package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.CriteriaDelegate;
import com.orange.srs.refreport.model.TO.provisioning.CriteriaListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class CriteriaProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(CriteriaProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private CriteriaDelegate criteriaDelegate;

	@Override
	public void execute(SOAContext soaContext, File criteriaProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[criteriaProvisioningCommand] provisioning file " + criteriaProvisioningFile.getAbsolutePath()));

		CriteriaListProvisioningTO criteriaListProvisioningTOUnmarshalled = (CriteriaListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				criteriaProvisioningFile);
		provisioningFacade.addNewCriteria(soaContext, criteriaListProvisioningTOUnmarshalled.criteriaProvisioningTOs);

		if (!forceUpdateFromFileToDatabase) {
			CriteriaListProvisioningTO criteriaListProvisioningTOToMarshall = criteriaDelegate
					.getCriteriaListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(criteriaListProvisioningTOToMarshall, criteriaProvisioningFile,
					false);
		}
	}

}

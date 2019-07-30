package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.FilterDelegate;
import com.orange.srs.refreport.model.TO.provisioning.FilterListProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class FilterProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(FilterProvisioningCommand.class);

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private FilterDelegate filterDelegate;

	@Override
	public void execute(SOAContext soaContext, File filterProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[filterProvisioningCommand] provisioning file " + filterProvisioningFile.getAbsolutePath()));

		FilterListProvisioningTO filterListProvisioningTOUnmarshalled = (FilterListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				filterProvisioningFile);
		provisioningFacade.updateFilterByDifferential(soaContext,
				filterListProvisioningTOUnmarshalled.filterProvisioningTOs, forceUpdateFromFileToDatabase);
		if (!forceUpdateFromFileToDatabase) {
			FilterListProvisioningTO filterListProvisioningTOToMarshall = filterDelegate
					.getFilterListProvisioningTOSorted();
			AbstractProvisioningCommand.marshallProvisioningTOToProvisioningFile(filterListProvisioningTOToMarshall,
					filterProvisioningFile, false);
		}
	}

}

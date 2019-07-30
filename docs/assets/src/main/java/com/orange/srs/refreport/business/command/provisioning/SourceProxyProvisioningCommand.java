package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.business.delegate.SourceProxyDelegate;
import com.orange.srs.refreport.model.TO.provisioning.SourceProxyListProvisioningTO;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class SourceProxyProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(SourceProxyProvisioningCommand.class);

	private static final String FILE_NAME_SOURCE_PROXY = "sourceProxy.xml";

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@EJB
	private SourceProxyDelegate sourceProxyDelegate;

	@Override
	public void execute(SOAContext soaContext, File sourceProxyProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[sourceProxyProvisioningCommand] provisioning file " + sourceProxyProvisioningFile.getAbsolutePath()));

		SourceProxyListProvisioningTO sourceProxyListProvisioningTOUnmarshalled = (SourceProxyListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				sourceProxyProvisioningFile);
		provisioningFacade.updateSourceProxyByDifferential(soaContext,
				sourceProxyListProvisioningTOUnmarshalled.inputSourceProxyProvisioningTOs);

		if (!forceUpdateFromFileToDatabase) {
			SourceProxyListProvisioningTO sourceProxyListProvisioningTOToMarshall = sourceProxyDelegate
					.getSourceProxyListProvisioningTOSorted();
			marshallProvisioningTOToProvisioningFile(sourceProxyListProvisioningTOToMarshall,
					sourceProxyProvisioningFile, false);
		}
	}

	public static String getSourceProxyProvisioningFilePath() {
		return Configuration.rootProperty + File.separatorChar + Configuration.configSourceproxyProperty
				+ File.separatorChar + FILE_NAME_SOURCE_PROXY;
	}

}

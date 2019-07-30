package com.orange.srs.refreport.business.command.provisioning;

import java.io.File;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA05ProvisioningFacade;
import com.orange.srs.refreport.model.TO.provisioning.ProxyListProvisioningTO;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

@Stateless
public class ProxyProvisioningCommand extends AbstractProvisioningCommand {

	private static final Logger LOGGER = Logger.getLogger(ProxyProvisioningCommand.class);

	private static final String FILE_NAME_PROXY = "proxy.xml";

	@EJB
	private SOA05ProvisioningFacade provisioningFacade;

	@Override
	public void execute(SOAContext soaContext, File proxyProvisioningFile, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[proxyProvisioningCommand] provisioning file " + proxyProvisioningFile.getAbsolutePath()));

		ProxyListProvisioningTO proxyListProvisioningTOUnmarshalled = (ProxyListProvisioningTO) unmarshallProvisioningTOFromProvisioningFile(
				proxyProvisioningFile);
		provisioningFacade.updateProxyByDifferentialAndMarshallToFile(soaContext,
				proxyListProvisioningTOUnmarshalled.proxyProvisioningTOs, proxyProvisioningFile,
				forceUpdateFromFileToDatabase);
	}

	public static String getProxyProvisioningFilePath() {
		return Configuration.rootProperty + File.separatorChar + Configuration.configProxyProperty + File.separatorChar
				+ FILE_NAME_PROXY;
	}
}

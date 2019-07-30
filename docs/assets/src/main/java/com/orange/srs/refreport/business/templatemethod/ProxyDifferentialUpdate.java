package com.orange.srs.refreport.business.templatemethod;

import java.net.URI;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import com.orange.srs.refreport.business.delegate.ProxyDelegate;
import com.orange.srs.refreport.model.Proxy;
import com.orange.srs.refreport.model.TO.provisioning.ProxyProvisioningTO;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.parameter.SOAContext;

@Stateless
public class ProxyDifferentialUpdate extends AbstractDifferentialUpdateTemplateMethod<ProxyProvisioningTO, Proxy, URI> {

	@EJB
	private ProxyDelegate proxyDelegate;

	@Override
	protected void sortProvisioningTOs(List<ProxyProvisioningTO> proxyProvisioningTOs) {
		ProxyDelegate.sortProxyProvisioningTO(proxyProvisioningTOs);
	}

	@Override
	protected List<Proxy> getModelObjectsSorted() {
		return proxyDelegate.getAllProxySorted();
	}

	@Override
	protected void initForChecks() {
		// Nothing to initialize
	}

	@Override
	protected void checkProvisioningTOsData(List<ProxyProvisioningTO> proxyProvisioningTOs) throws BusinessException {
		// No data checks
	}

	@Override
	protected void processFunctionalCreationChecks(ProxyProvisioningTO proxyProvisioningTO) throws BusinessException {
		proxyDelegate.checkProxyValidity(proxyProvisioningTO);
		proxyDelegate.checkProxyUnicity(proxyProvisioningTO);
	}

	@Override
	protected URI getProvisioningTOKey(ProxyProvisioningTO proxyProvisioningTO) {
		return ProxyDelegate.getProxyProvisioningTOKey(proxyProvisioningTO);
	}

	@Override
	protected URI getModelObjectKey(Proxy proxy) {
		return ProxyDelegate.getProxyKey(proxy);
	}

	@Override
	protected Boolean getSuppressFlag(ProxyProvisioningTO proxyProvisioningTO) {
		return proxyProvisioningTO.suppress;
	}

	@Override
	protected void removeModelObject(Proxy proxy) {
		proxyDelegate.removeProxy(proxy);
	}

	@Override
	protected boolean updateModelObjectIfNecessary(SOAContext soaContext, Proxy proxy,
			ProxyProvisioningTO proxyProvisioningTO) throws BusinessException {
		return proxyDelegate.updateProxyIfNecessary(proxy, proxyProvisioningTO);
	}

	@Override
	protected void createModelObjectFromProvisioningTO(SOAContext soaContext, ProxyProvisioningTO proxyProvisioningTO)
			throws BusinessException {
		proxyDelegate.createProxy(proxyProvisioningTO);
	}

	@Override
	protected String getInfoModelObjectForLog() {
		return "proxy";
	}

	@Override
	protected String getEndLogMessage() {
		return "";
	}

}

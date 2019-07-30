package com.orange.srs.refreport.business;

import java.net.URI;
import java.security.KeyException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.delegate.InputSourceDelegate;
import com.orange.srs.refreport.business.delegate.ProxyDelegate;
import com.orange.srs.refreport.business.delegate.ReportInputDelegate;
import com.orange.srs.refreport.business.delegate.SourceClassDelegate;
import com.orange.srs.refreport.business.delegate.SourceProxyDelegate;
import com.orange.srs.refreport.consumer.dao.ProxyDAO;
import com.orange.srs.refreport.consumer.dao.SourceClassDAO;
import com.orange.srs.refreport.model.InputSource;
import com.orange.srs.refreport.model.Proxy;
import com.orange.srs.refreport.model.ReportInput;
import com.orange.srs.refreport.model.SourceClass;
import com.orange.srs.refreport.model.SourceProxy;
import com.orange.srs.refreport.model.TO.TOBuilder;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.TO.report.GetProxyTOList;
import com.orange.srs.statcommon.model.TO.report.InputSourceConfigurationTOList;
import com.orange.srs.statcommon.model.TO.report.InputSourceKey;
import com.orange.srs.statcommon.model.TO.report.InputSourceKeyList;
import com.orange.srs.statcommon.model.TO.report.InputSourceParameterList;
import com.orange.srs.statcommon.model.TO.report.InputSourceStatusTOList;
import com.orange.srs.statcommon.model.TO.report.InputSourceTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceTOList;
import com.orange.srs.statcommon.model.TO.report.ProxyTO;
import com.orange.srs.statcommon.model.TO.report.PutSourceProxyParameter;
import com.orange.srs.statcommon.model.TO.report.ReportInputKeyTO;
import com.orange.srs.statcommon.model.TO.report.SourceClassTO;
import com.orange.srs.statcommon.model.TO.report.SourceClassTOList;
import com.orange.srs.statcommon.model.TO.report.SourceDefinition;
import com.orange.srs.statcommon.model.TO.report.SourceProxyTO;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.report.GetSourceStateParameter;
import com.orange.srs.statcommon.model.parameter.report.UpdateSourceStateParameter;
import com.orange.srs.statcommon.technical.interceptor.Logged;

@Logged
@Stateless
public class SOA06SourceFacade {

	private static Logger LOGGER = Logger.getLogger(SOA06SourceFacade.class);

	@EJB
	private ProxyDAO proxyDAO;

	@EJB
	private SourceClassDAO sourceClassDAO;

	@EJB
	private SourceClassDelegate sourceClassDelegate;

	@EJB
	private ReportInputDelegate reportInputDelegate;

	@EJB
	private ProxyDelegate proxyDelegate;

	@EJB
	private InputSourceDelegate inputSourceDelegate;

	@EJB
	private SourceProxyDelegate sourceProxyDelegate;

	public SourceClassTOList getAllSourceClass() {
		List<SourceClass> reportsClass = sourceClassDAO.findAll();
		SourceClassTOList result = new SourceClassTOList();

		for (SourceClass reportInputClass : reportsClass) {
			SourceClassTO to = new SourceClassTO();
			to.sourceClass = reportInputClass.getSourceClass();
			for (ReportInput input : reportInputClass.getProducedInputs()) {
				ReportInputKeyTO keyTo = new ReportInputKeyTO();
				keyTo.reportInputId = input.getPk();
				keyTo.granularity = input.getGranularity();
				keyTo.sourceTimeUnit = input.getSourceTimeUnit();
				keyTo.reportInputRef = input.getReportInputRef();

				to.getReportInputKeyTO().add(keyTo);
			}
			result.reportInputClass.add(to);
		}

		return result;
	}

	public String createSourceClass(SourceClassTO classInputParameter) throws KeyException, BusinessException {
		SourceClass inputClazz = sourceClassDAO.findById(classInputParameter.sourceClass);
		if (inputClazz != null) {
			throw new KeyException("ReportInputClass key " + classInputParameter.sourceClass + " already exists");
		}

		SourceClass result = new SourceClass();
		result.setSourceClass(classInputParameter.sourceClass);
		for (ReportInputKeyTO to : classInputParameter.getReportInputKeyTO()) {
			ReportInput reportInput = reportInputDelegate.getReportInputByKey(to.reportInputRef, to.granularity,
					to.sourceTimeUnit);
			result.getProducedInputs().add(reportInput);
		}

		sourceClassDAO.persistAndFlush(result);

		return result.getSourceClass();
	}

	public void deleteSourceClass(String sourceClass) throws BusinessException {
		SourceClass sourceClazz = sourceClassDelegate.getSourceClassByKey(sourceClass);
		sourceClassDelegate.removeSourceClass(sourceClazz);
	}

	public InputSourceKey saveInputSource(InputSourceTO inputSourceTO, SOAContext soaContext)
			throws BusinessException, JAXBException {
		InputSourceKey key = null;
		try {
			key = inputSourceDelegate.checkInputSourceUnicity(inputSourceTO);
		} catch (BusinessException e) {
			// if exception there it means that input source already exists in database.
			// Just exit without doing anything else
			LOGGER.warn("Input source already exists : " + inputSourceTO);
			return null;
		}

		inputSourceDelegate.createInputSource(inputSourceTO);
		return key;
	}

	public void modifyInputSource(InputSourceKey key, InputSourceTO to, SOAContext soaContext)
			throws BusinessException, JAXBException {
		inputSourceDelegate.modifyInputSource(key, to);
	}

	public void deleteInputSource(InputSourceKey key, SOAContext soaContext) throws BusinessException, JAXBException {
		InputSource inputSource = inputSourceDelegate.getInputSourceByKey(key);
		inputSourceDelegate.removeInputSource(inputSource);
	}

	public InputSourceKeyList getSources(SOAContext soaContext) {
		return inputSourceDelegate.getSources();
	}

	public InputSourceConfigurationTOList getAllInputSources(String sourceClass, SOAContext soaContext) {
		return inputSourceDelegate.getInputSources(sourceClass);
	}

	public InputSourceParameterList getAllInputSourcesWithProxies(String sourceClass, SOAContext soaContext) {
		return inputSourceDelegate.getInputSourcesWithProxies(sourceClass, soaContext);
	}

	public InputSourceTOList getSource(InputSourceKey key, SOAContext soaContext) throws BusinessException {
		return inputSourceDelegate.getSource(key);
	}

	public SourceDefinition getSourceDefinition(InputSourceKey key, SOAContext soaContext) throws BusinessException {
		return inputSourceDelegate.getSourceDefinition(key, soaContext);
	}

	/**
	 * List of proxy
	 * 
	 * @return The list of all known proxies
	 */
	public GetProxyTOList getProxies(SOAContext soaContext) {
		GetProxyTOList result = new GetProxyTOList();
		List<Proxy> proxies = proxyDAO.findAll();
		for (Proxy proxy : proxies) {
			result.getProxyTOs().add(TOBuilder.buildGetProxyTO(proxy));
		}

		return result;
	}

	public void deleteProxy(Long proxyId, SOAContext soaContext) throws BusinessException {
		Proxy proxy = proxyDelegate.getProxyById(proxyId);
		proxyDelegate.removeProxy(proxy);

	}

	public Long updateProxy(Long proxyId, ProxyTO proxyTO, SOAContext soaContext) throws BusinessException {
		proxyDelegate.checkProxyValidity(proxyTO);

		Proxy proxy = proxyDelegate.getProxyById(proxyId);
		proxy.setUri(proxyTO.uri);
		proxy.setVersion(proxyTO.version);

		proxyDAO.persistAndFlush(proxy);
		return proxy.getPk();
	}

	public Long createProxy(ProxyTO proxyTO, SOAContext soaContext) throws BusinessException {
		proxyDelegate.checkProxyValidity(proxyTO);
		proxyDelegate.checkProxyUnicity(proxyTO);
		Proxy proxy = proxyDelegate.createProxy(proxyTO);
		return proxy.getPk();
	}

	public InputSourceStatusTOList getSourceStatus(InputSourceKey key, SOAContext soaContext) throws BusinessException {
		return inputSourceDelegate.getSourceStatus(key);
	}

	public SourceProxyTO addProxyToResource(PutSourceProxyParameter putSourceProxyParam, Long proxyId,
			InputSourceKey key, SOAContext soaContext) throws JAXBException, BusinessException {
		Proxy proxy = proxyDelegate.getProxyById(proxyId);
		return sourceProxyDelegate.createSourceProxy(putSourceProxyParam.index, proxy, key);
	}

	public SourceProxyTO addProxyToResource(PutSourceProxyParameter putSourceProxyParam, URI proxyUri,
			InputSourceKey key, SOAContext soaContext) throws JAXBException, BusinessException {
		Proxy proxy = proxyDelegate.getProxyByKey(proxyUri);
		return sourceProxyDelegate.createSourceProxy(putSourceProxyParam.index, proxy, key);
	}

	public SourceProxyTO modifySourceProxy(PutSourceProxyParameter spt, Long proxyId, InputSourceKey key)
			throws BusinessException {
		SourceProxy targetProxy = sourceProxyDelegate.getSourceProxyByKey(key, proxyId);
		return sourceProxyDelegate.updateSourceProxy(spt.index, targetProxy, key);
	}

	public void deleteSourceProxy(Long proxyId, InputSourceKey key, SOAContext soaContext) throws BusinessException {
		SourceProxy targetProxy = sourceProxyDelegate.getSourceProxyByKey(key, proxyId);
		sourceProxyDelegate.removeSourceProxy(targetProxy, key);
	}

	public void updateSourceProxyState(UpdateSourceStateParameter parameter) throws BusinessException {
		inputSourceDelegate.updateSourceProxyState(parameter);
	}

	public String getSourceProxyState(GetSourceStateParameter parameter) throws BusinessException {
		return inputSourceDelegate.getSourceProxyState(parameter);
	}

}

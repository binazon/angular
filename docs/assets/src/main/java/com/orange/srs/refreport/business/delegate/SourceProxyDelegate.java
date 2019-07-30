package com.orange.srs.refreport.business.delegate;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.consumer.dao.InputSourceDAO;
import com.orange.srs.refreport.consumer.dao.SourceProxyDAO;
import com.orange.srs.refreport.model.InputSource;
import com.orange.srs.refreport.model.Proxy;
import com.orange.srs.refreport.model.SourceProxy;
import com.orange.srs.refreport.model.TO.provisioning.SourceProxyListProvisioningTO;
import com.orange.srs.refreport.model.enumerate.SourceProxyStateEnum;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.business.report.SourceFactory;
import com.orange.srs.statcommon.model.TO.InputSourceProxyProvisioningTO;
import com.orange.srs.statcommon.model.TO.SourceProxyProvisioningTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceKey;
import com.orange.srs.statcommon.model.TO.report.InputSourceTO;
import com.orange.srs.statcommon.model.TO.report.SourceProxyTO;
import com.orange.srs.statcommon.model.TO.report.pollingConfiguration.PollingConfiguration;
import com.orange.srs.statcommon.model.enums.ServiceMethodEnum;
import com.orange.srs.statcommon.model.parameter.SendRestParameter;
import com.orange.srs.statcommon.model.parameter.rest.ServiceConfigurationParameter;
import com.orange.srs.statcommon.technical.exception.RestServiceConsumerException;
import com.orange.srs.statcommon.technical.rest.RestServiceBuilder;
import com.orange.srs.statcommon.technical.xml.JAXBFactory;

@Stateless
public class SourceProxyDelegate {

	private static Logger LOGGER = Logger.getLogger(SourceProxyDelegate.class);

	@EJB
	private InputSourceDAO inputSourceDAO;

	@EJB
	private SourceProxyDAO sourceProxyDAO;

	@EJB
	private InputSourceDelegate inputSourceDelegate;

	@EJB
	private ProxyDelegate proxyDelegate;

	@EJB
	private RestServiceBuilder restServiceBuilder;

	public SourceProxy getSourceProxyByKey(InputSourceKey inputSourceKey, Long proxyId) throws BusinessException {
		List<SourceProxy> sourcesProxy = inputSourceDAO.findProxyIndex(inputSourceKey, proxyId);
		if (sourcesProxy.size() == 0) {
			throw new BusinessException(BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": InputSource with key "
					+ inputSourceKey + " and Proxy with Id " + proxyId);
		} else if (sourcesProxy.size() > 1) {
			throw new BusinessException(BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION + ": InputSource with key "
					+ inputSourceKey + " and Proxy with Id " + proxyId);
		}
		return sourcesProxy.get(0);
	}

	public List<SourceProxy> getAllSourceProxySortedForInputSource(InputSourceKey inputSourceKey)
			throws BusinessException {
		InputSource inputSource = inputSourceDelegate.getInputSourceByKey(inputSourceKey);
		// It's unnecessary to sort the sourceProxy, they are already order by proxy
		// index (see annotation in class SourceProxy)
		return inputSource.getProxies();
	}

	public SourceProxyListProvisioningTO getSourceProxyListProvisioningTOSorted() {
		SourceProxyListProvisioningTO sourceProxyListProvisioningTO = new SourceProxyListProvisioningTO();
		for (InputSource inputSource : inputSourceDAO.findAll()) {
			if (!inputSource.getProxies().isEmpty()) {
				InputSourceProxyProvisioningTO inputSourceProxyProvisioningTO = new InputSourceProxyProvisioningTO();
				inputSourceProxyProvisioningTO.sourceClass = inputSource.getSourceClass().getSourceClass();
				inputSourceProxyProvisioningTO.sourceName = inputSource.getName();
				for (SourceProxy sourceProxy : inputSource.getProxies()) {
					SourceProxyProvisioningTO sourceProxyProvisioningTO = new SourceProxyProvisioningTO();
					sourceProxyProvisioningTO.index = sourceProxy.getIndex();
					sourceProxyProvisioningTO.uri = sourceProxy.getUri();
					inputSourceProxyProvisioningTO.sourceProxies.add(sourceProxyProvisioningTO);
				}
				Collections.sort(inputSourceProxyProvisioningTO.sourceProxies,
						new SourceProxyProvisioningTOIndexComparator());
				sourceProxyListProvisioningTO.inputSourceProxyProvisioningTOs.add(inputSourceProxyProvisioningTO);
			}
		}
		Collections.sort(sourceProxyListProvisioningTO.inputSourceProxyProvisioningTOs,
				new InputSourceProxyProvisioningTOComparator());
		return sourceProxyListProvisioningTO;
	}

	public void createSourceProxyFromProvisioningTO(SourceProxyProvisioningTO sourceProxyProvisioningTO,
			InputSourceKey inputSourceKey) throws BusinessException {
		try {
			Proxy proxy = proxyDelegate.getProxyByKey(sourceProxyProvisioningTO.uri);
			createSourceProxy(sourceProxyProvisioningTO.index, proxy, inputSourceKey);
		} catch (JAXBException jaxbe) {
			throw new BusinessException(jaxbe);
		}
	}

	public SourceProxyTO createSourceProxy(int proxyIndex, Proxy proxy, InputSourceKey inputSourceKey)
			throws JAXBException, BusinessException {

		SourceProxyTO result = new SourceProxyTO();
		result.proxyId = proxy.getPk();

		InputSource source = inputSourceDelegate.getInputSourceByKey(inputSourceKey);

		List<SourceProxy> sp = inputSourceDAO.findProxyIndex(inputSourceKey, proxy.getPk());
		if (sp.size() > 0) {
			throw new RuntimeException(BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION + " " + " proxy with id "
					+ proxy.getPk() + " already associated with inputSource with key " + inputSourceKey);
		}

		result.sourceKey = inputSourceKey;

		InputSourceTO to = (InputSourceTO) JAXBFactory.getUnmarshaller()
				.unmarshal(new StringReader(source.getConfiguration()));

		// SendRestRequestParameter<InputSourceTO> cparameter=new
		// SendRestRequestParameter<InputSourceTO>();
		// cparameter.httpMethod=ServiceMethodEnum.PUT;
		// cparameter.parameter=to;
		// cparameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
		// cparameter.servicePath=Configuration.serviceConfiguration.getProperty(Configuration.TELECONFIGURATION_SERVICE_PROPERTY);

		ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
		sparameter.httpMethod = ServiceMethodEnum.PUT;
		sparameter.requestMediaType = MediaType.APPLICATION_XML;

		SendRestParameter<InputSourceTO> sendRestParameter = new SendRestParameter<>();
		sendRestParameter.parameter = to;

		String servicePath = Configuration.serviceConfiguration
				.getProperty(Configuration.TELECONFIGURATION_SERVICE_PROPERTY);

		try {
			// consumer.initConsumer(proxy.getUri(), proxy.isSsl());
			// consumer.sendRequest(cparameter);
			sparameter.uri = proxy.getUri();
			restServiceBuilder.sendRequest(sendRestParameter, sparameter, servicePath);
		} catch (RestServiceConsumerException rsce) {
			throw new BusinessException("Error while sending request to proxy with id " + proxy.getPk() + " and uri "
					+ proxy.getUri().getPath() + " cause :" + rsce.getMessage(), rsce);
		}

		PollingConfiguration conf = SourceFactory.createPollingConfiguration(to.getSourceClass());
		StringWriter confWriter = new StringWriter();
		JAXBRefReportFactory.getMarshaller().marshal(conf, confWriter);

		if (proxyIndex > source.getProxies().size()) {
			SourceProxy sproxy = new SourceProxy(source.getProxies().size() + 1, proxy);
			sproxy.setState(SourceProxyStateEnum.OK);
			result.index = source.getProxies().size() + 1;
			source.getProxies().add(sproxy);
		} else {
			boolean found = false;
			for (int i = 0; i < source.getProxies().size(); i++) {
				if (found) {
					SourceProxy sproxy = source.getProxies().get(i);
					sproxy.setIndex(i + 1);
				}

				if (proxyIndex == (i + 1) && !found) {
					found = true;
					SourceProxy newproxy = new SourceProxy(i + 1, proxy);
					source.getProxies().add(i, newproxy);
					newproxy.setState(SourceProxyStateEnum.OK);
					result.index = i + 1;
				}
			}
		}

		source.setPollingState(confWriter.toString());
		inputSourceDAO.persistAndFlush(source);
		return result;
	}

	public SourceProxyTO updateSourceProxy(int proxyIndex, SourceProxy targetProxy, InputSourceKey key)
			throws BusinessException {
		SourceProxyTO result = new SourceProxyTO();
		result.sourceKey = key;

		InputSource source = inputSourceDelegate.getInputSourceByKey(key);

		int oldindex = targetProxy.getIndex();

		targetProxy.setIndex(Math.min(proxyIndex, source.getProxies().size()));
		result.index = targetProxy.getIndex();
		if (oldindex > proxyIndex) {
			for (int i = proxyIndex - 1; i < oldindex - 1 && i < source.getProxies().size(); i++) {
				SourceProxy tmpProxy = source.getProxies().get(i);
				tmpProxy.setIndex(tmpProxy.getIndex() + 1);
			}

			inputSourceDAO.persistAndFlush(source);
		} else if (oldindex < proxyIndex) {
			for (int i = oldindex; i < proxyIndex && i < source.getProxies().size(); i++) {
				SourceProxy tmpProxy = source.getProxies().get(i);
				tmpProxy.setIndex(tmpProxy.getIndex() - 1);
			}

			inputSourceDAO.persistAndFlush(source);
		}

		return result;
	}

	public void removeSourceProxy(SourceProxy targetProxy, InputSourceKey key) throws BusinessException {

		InputSource source = inputSourceDelegate.getInputSourceByKey(key);
		boolean proceedToDelete = true;

		// SendRestRequestParameter<Void> cparameter=new
		// SendRestRequestParameter<Void>();
		// cparameter.httpMethod=ServiceMethodEnum.DELETE;
		// cparameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
		// cparameter.servicePath=Configuration.serviceConfiguration.getProperty(Configuration.TELECONFIGURATION_SERVICE_PROPERTY)+'/'+key.sourceClass+'/'+key.sourceName;

		ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
		sparameter.httpMethod = ServiceMethodEnum.DELETE;
		sparameter.requestMediaType = MediaType.APPLICATION_XML;

		SendRestParameter<Void> sendRestParameter = new SendRestParameter<>();

		String servicePath = Configuration.serviceConfiguration.getProperty(
				Configuration.TELECONFIGURATION_SERVICE_PROPERTY) + '/' + key.sourceClass + '/' + key.sourceName;

		try {
			// consumer.initConsumer(targetProxy.getUri(), targetProxy.isSsL());
			// consumer.sendRequest(cparameter);
			sparameter.uri = targetProxy.getUri();
			restServiceBuilder.sendRequest(sendRestParameter, sparameter, servicePath);
		} catch (RestServiceConsumerException rsce) {

			if (rsce.getResponseStatusCode() == Status.BAD_REQUEST.getStatusCode()) {
				LOGGER.warn("Unknown SourceProxy on ProxyCollector. Force Database delete. "
						+ targetProxy.getUri().getPath() + " cause :" + rsce.getMessage());
			} else {
				proceedToDelete = false;
				targetProxy.setKoCause(rsce.getMessage());
				sourceProxyDAO.persistAndFlush(targetProxy);
				throw new BusinessException("Error while sending request to proxy with uri "
						+ targetProxy.getUri().getPath() + " cause :" + rsce.getMessage(), rsce);
			}
		} catch (JAXBException jaxe) {
			proceedToDelete = false;
			targetProxy.setKoCause(jaxe.getMessage());
			sourceProxyDAO.persistAndFlush(targetProxy);
			throw new BusinessException("Error while sending request to proxy with uri "
					+ targetProxy.getUri().getPath() + " cause :" + jaxe.getMessage(), jaxe);
		} finally {

			if (proceedToDelete) {
				for (int i = targetProxy.getIndex(); i < source.getProxies().size(); i++) {
					source.getProxies().get(i).setIndex(i);
				}

				source.getProxies().remove(targetProxy);
				sourceProxyDAO.remove(targetProxy);
				inputSourceDAO.persistAndFlush(source);
			}
		}
	}

	public static void sortSourceProxyProvisioningTO(List<SourceProxyProvisioningTO> sourceProxyProvisioningTOs) {
		Collections.sort(sourceProxyProvisioningTOs, new SourceProxyProvisioningTOIndexComparator());
	}

	private static class SourceProxyProvisioningTOIndexComparator implements Comparator<SourceProxyProvisioningTO> {

		@Override
		public int compare(SourceProxyProvisioningTO firstObj, SourceProxyProvisioningTO secondObj) {
			return firstObj.index - secondObj.index;
		}
	}

	private static class InputSourceProxyProvisioningTOComparator
			implements Comparator<InputSourceProxyProvisioningTO> {

		@Override
		public int compare(InputSourceProxyProvisioningTO firstObj, InputSourceProxyProvisioningTO secondObj) {
			return firstObj.compareTo(secondObj);
		}
	}

}

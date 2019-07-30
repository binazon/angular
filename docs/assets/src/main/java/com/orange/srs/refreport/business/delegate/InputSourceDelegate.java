package com.orange.srs.refreport.business.delegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
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
import com.orange.srs.refreport.model.InputSource;
import com.orange.srs.refreport.model.SourceClass;
import com.orange.srs.refreport.model.SourceProxy;
import com.orange.srs.refreport.model.enumerate.InputSourceStateEnum;
import com.orange.srs.refreport.model.enumerate.SourceProxyStateEnum;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.business.report.SourceFactory;
import com.orange.srs.statcommon.model.TO.report.IndexedProxyTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceConfigurationTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceConfigurationTOList;
import com.orange.srs.statcommon.model.TO.report.InputSourceKey;
import com.orange.srs.statcommon.model.TO.report.InputSourceKeyList;
import com.orange.srs.statcommon.model.TO.report.InputSourceParameter;
import com.orange.srs.statcommon.model.TO.report.InputSourceParameterList;
import com.orange.srs.statcommon.model.TO.report.InputSourceStatusTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceStatusTOList;
import com.orange.srs.statcommon.model.TO.report.InputSourceTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceTOList;
import com.orange.srs.statcommon.model.TO.report.ProxyConfiguration;
import com.orange.srs.statcommon.model.TO.report.SourceDefinition;
import com.orange.srs.statcommon.model.TO.report.pollingConfiguration.PollingConfiguration;
import com.orange.srs.statcommon.model.TO.rest.RestResponse;
import com.orange.srs.statcommon.model.enums.ServiceMethodEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.SendRestParameter;
import com.orange.srs.statcommon.model.parameter.report.GetSourceStateParameter;
import com.orange.srs.statcommon.model.parameter.report.UpdateSourceStateParameter;
import com.orange.srs.statcommon.model.parameter.rest.ServiceConfigurationParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.RestServiceConsumerException;
import com.orange.srs.statcommon.technical.rest.RestServiceBuilder;
import com.orange.srs.statcommon.technical.xml.JAXBFactory;

@Stateless
public class InputSourceDelegate {

	private static final Logger LOGGER = Logger.getLogger(InputSourceDelegate.class);

	@EJB
	private InputSourceDAO inputSourceDAO;

	@EJB
	private SourceClassDelegate sourceClassDelegate;

	@EJB
	private RestServiceBuilder restServiceBuilder;

	public static InputSourceKey getInputSourceKey(InputSource inputSource) {
		return new InputSourceKey(inputSource.getName(), inputSource.getSourceClass().getSourceClass());
	}

	public static InputSourceKey getInputSourceTOKey(InputSourceTO inputSourceTO) {
		return new InputSourceKey(inputSourceTO.getSourceName(), inputSourceTO.getSourceClass());
	}

	public InputSource getInputSourceByKey(InputSourceKey inputSourceKey) throws BusinessException {
		List<InputSource> inputSources = inputSourceDAO.findByInputSourceKey(inputSourceKey);
		if (inputSources == null || inputSources.size() == 0) {
			throw new BusinessException(
					BusinessException.ENTITY_NOT_FOUND_EXCEPTION + ": InputSource with " + inputSourceKey);
		} else if (inputSources.size() > 1) {
			throw new BusinessException(
					BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION + ": InputSource with " + inputSourceKey);
		}
		return inputSources.get(0);
	}

	public InputSource createInputSource(InputSourceTO inputSourceTO) throws BusinessException, JAXBException {

		InputSource inputSource = new InputSource();
		inputSource.setName(inputSourceTO.getSourceName());

		SourceClass sourceClass = sourceClassDelegate.getSourceClassByKey(inputSourceTO.getSourceClass());
		inputSource.setSourceClass(sourceClass);

		StringWriter writer = new StringWriter();
		JAXBRefReportFactory.getMarshaller().marshal(inputSourceTO, writer);
		inputSource.setConfiguration(writer.toString());

		PollingConfiguration configuration = SourceFactory.createPollingConfiguration(inputSourceTO.getSourceClass());
		configuration.reset();
		StringWriter confWriter = new StringWriter();
		JAXBRefReportFactory.getMarshaller().marshal(configuration, confWriter);
		inputSource.setPollingState(confWriter.toString());

		inputSource.setState(InputSourceStateEnum.START);

		inputSourceDAO.persistAndFlush(inputSource);

		return inputSource;
	}

	public void modifyInputSource(InputSourceKey inputSourceKey, InputSourceTO inputSourceTO)
			throws BusinessException, JAXBException {
		if (!inputSourceKey.sourceClass.equals(inputSourceTO.getSourceClass())
				|| (!inputSourceTO.getSourceName().trim().equals("")
						&& !inputSourceKey.sourceName.equals(inputSourceTO.getSourceName()))) {
			throw new BusinessException(
					"Incoherent input, in request path (sourceName, sourceClass) = (" + inputSourceKey.sourceName + ", "
							+ inputSourceKey.sourceClass + ") and (sourceName, sourceClass)= ("
							+ inputSourceTO.getSourceName() + ", " + inputSourceTO.getSourceClass() + ") in body.");
		}
		inputSourceTO.setSourceName(inputSourceKey.sourceName);

		InputSource inputSource = getInputSourceByKey(inputSourceKey);

		StringWriter writer = new StringWriter();
		JAXBRefReportFactory.getMarshaller().marshal(inputSourceTO, writer);
		String configuration = writer.toString();
		updateInputSource(inputSource, inputSourceTO, configuration);
	}

	public boolean updateInputSourceIfNecessary(InputSource inputSource, InputSourceTO inputSourceTO)
			throws JAXBException {
		boolean updated = false;
		StringWriter writer = new StringWriter();
		JAXBRefReportFactory.getMarshaller().marshal(inputSourceTO, writer);
		String configurationNew = writer.toString();
		if (!configurationNew.equals(inputSource.getConfiguration())) {
			updateInputSource(inputSource, inputSourceTO, configurationNew);
			updated = true;
		}
		return updated;
	}

	private void updateInputSource(InputSource inputSource, InputSourceTO inputSourceTO, String configuration)
			throws JAXBException {

		inputSource.setConfiguration(configuration);

		// SendRestRequestParameter<InputSourceTO> cparameter = new
		// SendRestRequestParameter<InputSourceTO>();
		// cparameter.httpMethod = ServiceMethodEnum.POST;
		// cparameter.parameter = inputSourceTO;
		// cparameter.requestMedia = MediaType.TEXT_XML_TYPE;

		ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
		sparameter.httpMethod = ServiceMethodEnum.POST;
		sparameter.requestMediaType = MediaType.TEXT_XML;

		SendRestParameter<InputSourceTO> sendRestParameter = new SendRestParameter<>();
		sendRestParameter.parameter = inputSourceTO;

		String servicePath = Configuration.serviceConfiguration
				.getProperty(Configuration.TELECONFIGURATION_SERVICE_PROPERTY) + "/" + inputSourceTO.getSourceClass()
				+ "/" + inputSourceTO.getSourceName();

		for (SourceProxy sourceProxy : inputSource.getProxies()) {
			try {
				sparameter.uri = sourceProxy.getUri();
				// consumer.initConsumer(sourceProxy.getUri(), sourceProxy.isSsL());
				// consumer.sendRequest(cparameter);
				restServiceBuilder.sendRequest(sendRestParameter, sparameter, servicePath);
			} catch (RestServiceConsumerException rsce) {
				sourceProxy.setState(SourceProxyStateEnum.KO);
				sourceProxy.setKoCause(rsce.getMessage());
			} catch (NullPointerException npe) {
				sourceProxy.setState(SourceProxyStateEnum.KO);
				sourceProxy.setKoCause(npe.getMessage());
			}
		}

		inputSourceDAO.persistAndFlush(inputSource);
	}

	public void removeInputSource(InputSource inputSource) throws JAXBException {

		InputSourceTO parameter = (InputSourceTO) JAXBFactory.getUnmarshaller()
				.unmarshal(new StringReader(inputSource.getConfiguration()));
		// SendRestRequestParameter<InputSourceTO> cparameter=new
		// SendRestRequestParameter<InputSourceTO>();
		// cparameter.httpMethod=ServiceMethodEnum.DELETE;
		// cparameter.requestMedia=MediaType.APPLICATION_XML_TYPE;

		boolean deleteSuccesfull = true;

		ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
		sparameter.httpMethod = ServiceMethodEnum.DELETE;
		sparameter.requestMediaType = MediaType.APPLICATION_XML;

		SendRestParameter<InputSourceTO> sendRestParameter = new SendRestParameter<>();

		String servicePath = Configuration.serviceConfiguration
				.getProperty(Configuration.TELECONFIGURATION_SERVICE_PROPERTY) + "/"
				+ inputSource.getSourceClass().getSourceClass() + "/" + inputSource.getName();

		for (SourceProxy sourceProxy : inputSource.getProxies()) {
			try {
				// consumer.initConsumer(sourceProxy.getUri(), sourceProxy.isSsL());
				// consumer.sendRequest(cparameter);
				sparameter.uri = sourceProxy.getUri();
				restServiceBuilder.sendRequest(sendRestParameter, sparameter, servicePath);
			} catch (RestServiceConsumerException rsce) {
				deleteSuccesfull = false;
				sourceProxy.setState(SourceProxyStateEnum.KO);
				sourceProxy.setKoCause(rsce.getMessage());
			} catch (NullPointerException npe) {
				deleteSuccesfull = false;
				sourceProxy.setState(SourceProxyStateEnum.KO);
				sourceProxy.setKoCause(npe.getMessage());
			}
		}

		if (deleteSuccesfull) {
			inputSourceDAO.remove(inputSource);
		} else {
			inputSource.setState(InputSourceStateEnum.DESACTIVATED);
			inputSourceDAO.persistAndFlush(inputSource);
		}
	}

	public InputSourceKey checkInputSourceUnicity(InputSourceTO inputSourceTO) throws BusinessException {
		InputSourceKey key = new InputSourceKey();
		key.sourceClass = inputSourceTO.getSourceClass();
		key.sourceName = inputSourceTO.getSourceName();
		List<InputSource> sources = inputSourceDAO.findByInputSourceKey(key);
		if (!sources.isEmpty()) {
			throw new BusinessException(
					BusinessException.ENTITY_NOT_UNIQUE_EXCEPTION + ": InputSource with " + key.toString());
		}
		return key;
	}

	public static List<InputSource> getAllInputSourceSortedForSourceClass(SourceClass sourceClass) {
		List<InputSource> inputSourceList = sourceClass.getInputSources();
		Collections.sort(inputSourceList, new InputSourceComparator());
		return inputSourceList;
	}

	public List<InputSourceTO> getInputSourceTOsSortedForSourceClass(String sourceClazz) throws BusinessException {
		SourceClass sourceClass = sourceClassDelegate.getSourceClassByKey(sourceClazz);
		List<InputSourceTO> inputSourceTOs = new ArrayList<InputSourceTO>();
		for (InputSource inputSource : getAllInputSourceSortedForSourceClass(sourceClass)) {
			InputSourceTO inputSourceTO;
			try {
				inputSourceTO = (InputSourceTO) JAXBFactory.getUnmarshaller()
						.unmarshal(new StringReader(inputSource.getConfiguration()));
			} catch (JAXBException jaxbe) {
				throw new BusinessException(jaxbe);
			}
			inputSourceTOs.add(inputSourceTO);
		}
		return inputSourceTOs;
	}

	public void updateSourceProxyState(UpdateSourceStateParameter parameter) throws BusinessException {
		InputSource source = getInputSourceByKey(parameter.sourceKey);

		PollingConfiguration conf = SourceFactory.createPollingConfiguration(source.getSourceClass().getSourceClass());
		if (!parameter.pollingState.getClass().equals(conf.getClass())) {
			throw new BusinessException(BusinessException.WRONG_PARAMETER_EXCEPTION
					+ " - BAD POLLING CONFIGURATION PARAMETER sourceClass " + source.getClass().getClass()
					+ " is not compatible with parameter " + parameter.pollingState.getClass().toString());
		}

		StringWriter writer = new StringWriter();
		try {
			JAXBRefReportFactory.getMarshaller().marshal(parameter.pollingState, writer);
		} catch (JAXBException jaxe) {
			throw new RuntimeException(jaxe.getMessage(), jaxe);
		}

		source.setPollingState(writer.toString());
		inputSourceDAO.persistAndFlush(source);
	}

	public String getSourceProxyState(GetSourceStateParameter parameter) throws BusinessException {
		InputSource source = getInputSourceByKey(parameter.sourceKey);
		return source.getPollingState();
	}

	public InputSourceStatusTOList getSourceStatus(InputSourceKey key) throws BusinessException {
		InputSourceStatusTOList result = new InputSourceStatusTOList();

		InputSource source = getInputSourceByKey(key);

		// SendRestRequestParameter<Void> cparameter=new
		// SendRestRequestParameter<Void>();
		// cparameter.httpMethod=ServiceMethodEnum.GET;
		// cparameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
		// cparameter.servicePath=Configuration.serviceConfiguration.getProperty(Configuration.STATUS_SERVICE_PROPERTY)+"/"+key.sourceClass+"/"+key.sourceName;

		ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
		sparameter.httpMethod = ServiceMethodEnum.GET;
		sparameter.requestMediaType = MediaType.APPLICATION_XML;

		SendRestParameter<Void> sendRestParameter = new SendRestParameter<>();

		String servicePath = Configuration.serviceConfiguration.getProperty(Configuration.STATUS_SERVICE_PROPERTY) + "/"
				+ key.sourceClass + "/" + key.sourceName;

		for (SourceProxy sproxy : source.getProxies()) {
			InputSourceStatusTO statusTO = null;
			try {
				// consumer.initConsumer(sproxy.getUri(), sproxy.isSsL());
				// consumer.addRestResponseType(ServiceMethodEnum.GET, 200,
				// InputSourceStatusTO.class);
				// RestResponse response=consumer.sendRequest(cparameter);
				restServiceBuilder.addRestResponseType(ServiceMethodEnum.GET, Status.OK.getStatusCode(),
						InputSourceStatusTO.class);
				sparameter.uri = sproxy.getUri();
				RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter, servicePath);
				statusTO = (InputSourceStatusTO) response.getResult();
			} catch (RestServiceConsumerException rsex) {
				statusTO = new InputSourceStatusTO();
				statusTO.status = "KO";
				statusTO.description = "Error while sending status request to distant proxy ";
				statusTO.message = rsex.getLocalizedMessage();
			} catch (JAXBException jaxbe) {
				statusTO = new InputSourceStatusTO();
				statusTO.status = "KO";
				statusTO.description = "Error while interpreting status request from distant proxy";
				statusTO.message = jaxbe.getLocalizedMessage();
			}

			statusTO.proxyUri = sproxy.getUri();
			statusTO.proxyName = sproxy.getName();
			result.inputSourceStatuses.add(statusTO);
		}

		return result;
	}

	public InputSourceKeyList getSources() {
		List<InputSourceKey> list = inputSourceDAO.findAllInputSourceKeys();
		InputSourceKeyList keys = new InputSourceKeyList();
		keys.inputSources = list;
		return keys;
	}

	public InputSourceConfigurationTOList getInputSources(String sourceClass) {
		List<InputSourceConfigurationTO> list = inputSourceDAO.findAllInputSourceKeys(sourceClass);
		InputSourceConfigurationTOList inputSourceConfiguration = new InputSourceConfigurationTOList();
		inputSourceConfiguration.inputSourcesConfigutation = list;
		return inputSourceConfiguration;
	}

	public InputSourceParameterList getInputSourcesWithProxies(String sourceClass, SOAContext soaContext) {
		InputSourceParameterList inputSourceParameter = new InputSourceParameterList();
		List<InputSource> inputSources = inputSourceDAO
				.findBy(InputSource.FIELD_SOURCE_CLASS + "." + SourceClass.FIELD_SOURCE_CLASS, sourceClass);
		for (InputSource inputSource : inputSources) {
			try {
				InputSourceParameter param = new InputSourceParameter();
				InputSourceTO inputSourceTO = (InputSourceTO) JAXBRefReportFactory.getUnmarshaller()
						.unmarshal(new StringReader(inputSource.getConfiguration()));
				param.sourceDefinition = new SourceDefinition();
				param.sourceDefinition.sourceClass = inputSourceTO.getSourceClass();
				param.sourceDefinition.sourceName = inputSourceTO.getSourceName();
				param.sourceDefinition.pollingPeriod = inputSourceTO.getPollingPeriod();
				param.sourceDefinition.origin = inputSourceTO.getOrigin();
				param.sourceDefinition.timeZone = inputSourceTO.getTimeZone();
				param.pollingState = inputSource.getPollingState();
				param.retentionTime = inputSourceTO.retentionTime;
				for (SourceProxy sproxy : inputSource.getProxies()) {
					IndexedProxyTO proxy = new IndexedProxyTO();
					proxy.uri = sproxy.getUri();
					proxy.name = sproxy.getName();
					proxy.isSsl = sproxy.isSsL();
					proxy.version = sproxy.getVersion();
					proxy.index = sproxy.getIndex();
					param.proxy.add(proxy);
				}
				inputSourceParameter.inputSourceParameter.add(param);
			} catch (JAXBException e) {
				LOGGER.error(SOATools.buildSOALogMessage(soaContext,
						"An error occurs when getting inputSourceParameter for source: " + inputSource.getName(), e));
			}
		}
		return inputSourceParameter;
	}

	public InputSourceTOList getSource(InputSourceKey key) throws BusinessException {
		InputSourceTOList result = new InputSourceTOList();

		InputSource source = getInputSourceByKey(key);
		result.mainConfiguration = source.getConfiguration();

		InputSourceTO mainConf = null;
		try {
			mainConf = (InputSourceTO) JAXBRefReportFactory.getUnmarshaller()
					.unmarshal(new StringReader(result.mainConfiguration));
			result.mainConfValid = true;
		} catch (JAXBException jaxbe) {
			jaxbe.printStackTrace();
			result.mainConfValid = false;
		}

		// SendRestRequestParameter<Void> cparameter=new
		// SendRestRequestParameter<Void>();
		// cparameter.httpMethod=ServiceMethodEnum.GET;
		// cparameter.requestMedia=MediaType.APPLICATION_XML_TYPE;
		// cparameter.servicePath=Configuration.serviceConfiguration.getProperty(Configuration.TELECONFIGURATION_SERVICE_PROPERTY)+"/"+key.sourceClass+"/"+key.sourceName;

		ServiceConfigurationParameter sparameter = new ServiceConfigurationParameter();
		sparameter.httpMethod = ServiceMethodEnum.GET;
		sparameter.requestMediaType = MediaType.APPLICATION_XML;

		SendRestParameter<Void> sendRestParameter = new SendRestParameter<>();

		String servicePath = Configuration.serviceConfiguration.getProperty(
				Configuration.TELECONFIGURATION_SERVICE_PROPERTY) + "/" + key.sourceClass + "/" + key.sourceName;

		for (SourceProxy sproxy : source.getProxies()) {
			ProxyConfiguration conf = new ProxyConfiguration();
			conf.proxyName = sproxy.getName();
			conf.proxyURI = sproxy.getUri();
			try {
				// consumer.initConsumer(sproxy.getUri(), sproxy.isSsL());
				// RestResponse response=consumer.sendRequest(cparameter);
				sparameter.uri = sproxy.getUri();
				RestResponse response = restServiceBuilder.sendRequest(sendRestParameter, sparameter, servicePath);

				InputStream stream = (InputStream) response.getResult();
				InputStreamReader ireader = new InputStreamReader(stream);
				BufferedReader breader = new BufferedReader(ireader);
				String proxyConf = "";
				String confLine;
				while ((confLine = breader.readLine()) != null) {
					proxyConf += confLine;
				}
				InputSourceTO proxyConfObject = (InputSourceTO) JAXBRefReportFactory.getUnmarshaller()
						.unmarshal(new StringReader(proxyConf));

				conf.configuration = proxyConf;
				if (mainConf != null && proxyConfObject != null && mainConf.equals(proxyConfObject)) {
					conf.isValid = true;
				} else {
					conf.isValid = false;
				}
			} catch (IOException ioex) {
				conf.isValid = false;
				conf.errorMessage = ioex.getLocalizedMessage();
			} catch (RestServiceConsumerException rsex) {
				conf.isValid = false;
				rsex.printStackTrace();
				conf.errorMessage = rsex.getLocalizedMessage();
			} catch (JAXBException jaxbe) {
				conf.isValid = false;
				jaxbe.printStackTrace();
				conf.errorMessage = jaxbe.getLocalizedMessage();
			}

			result.proxyConfiguration.add(conf);
		}

		return result;
	}

	public SourceDefinition getSourceDefinition(InputSourceKey key, SOAContext soaContext) throws BusinessException {

		SourceDefinition result = new SourceDefinition();
		InputSource source = getInputSourceByKey(key);

		try {
			InputSourceTO inputSource = (InputSourceTO) JAXBRefReportFactory.getUnmarshaller()
					.unmarshal(new StringReader(source.getConfiguration()));
			result.sourceClass = inputSource.getSourceClass();
			result.sourceName = inputSource.getSourceName();
			result.pollingPeriod = inputSource.getPollingPeriod();
			result.origin = inputSource.getOrigin();
			result.timeZone = inputSource.getTimeZone();
		} catch (JAXBException e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, e.getMessage()));
		}
		return result;
	}

	public static void sortInputSourceTO(List<InputSourceTO> inputSourceTOs) {
		Collections.sort(inputSourceTOs, new InputSourceTOComparator());
	}

	private static class InputSourceComparator implements Comparator<InputSource> {
		@Override
		public int compare(InputSource firstObj, InputSource secondObj) {
			return getInputSourceKey(firstObj).compareTo(getInputSourceKey(secondObj));
		}
	}

	private static class InputSourceTOComparator implements Comparator<InputSourceTO> {
		@Override
		public int compare(InputSourceTO firstObj, InputSourceTO secondObj) {
			return getInputSourceTOKey(firstObj).compareTo(getInputSourceTOKey(secondObj));
		}
	}

}

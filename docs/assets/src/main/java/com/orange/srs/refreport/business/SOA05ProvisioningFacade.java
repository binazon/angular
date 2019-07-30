package com.orange.srs.refreport.business;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.RollbackException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.command.provisioning.AbstractProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.InputSourceProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ProxyProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.SourceClassProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.SourceProxyProvisioningCommand;
import com.orange.srs.refreport.business.delegate.CriteriaDelegate;
import com.orange.srs.refreport.business.delegate.EntitiesDataLocationDelegate;
import com.orange.srs.refreport.business.delegate.InputFormatDelegate;
import com.orange.srs.refreport.business.delegate.InputSourceDelegate;
import com.orange.srs.refreport.business.delegate.ProvisioningDelegate;
import com.orange.srs.refreport.business.delegate.ProvisioningFileDelegate;
import com.orange.srs.refreport.business.delegate.ProxyDelegate;
import com.orange.srs.refreport.business.delegate.ReportingGroupDelegate;
import com.orange.srs.refreport.business.delegate.SourceClassDelegate;
import com.orange.srs.refreport.business.delegate.SourceProxyDelegate;
import com.orange.srs.refreport.business.templatemethod.BookmarkDirectReportDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.EntityTypeDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.ExternalIndicatorDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.FilterDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.HyperlinkDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.IndicatorDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.InputColumnDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.InputSourceDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.OfferAndOptionDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.ParamTypeDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.ProxyDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.ReportConfigDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.ReportDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.ReportInputDifferentialUpdate;
import com.orange.srs.refreport.business.templatemethod.SourceClassDifferentialUpdate;
import com.orange.srs.refreport.model.Criteria;
import com.orange.srs.refreport.model.InputFormat;
import com.orange.srs.refreport.model.SourceClass;
import com.orange.srs.refreport.model.SourceProxy;
import com.orange.srs.refreport.model.TO.CriteriaKeyTO;
import com.orange.srs.refreport.model.TO.FileStatusTO;
import com.orange.srs.refreport.model.TO.GetFileNameToRetrieveTO;
import com.orange.srs.refreport.model.TO.GetFilesTO;
import com.orange.srs.refreport.model.TO.ProvisioningActionStatusTO;
import com.orange.srs.refreport.model.TO.ProvisioningFileStatusTO;
import com.orange.srs.refreport.model.TO.ProvisioningSourceTypeConfigurationTO;
import com.orange.srs.refreport.model.TO.provisioning.BookmarkDirectReportProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.CriteriaProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ExportEntitiesTO;
import com.orange.srs.refreport.model.TO.provisioning.ExternalIndicatorProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.FilterProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.HyperlinkProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.IndicatorProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.InputColumnProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.OfferAndOptionProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ParamTypeProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ProxyListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ProxyProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportConfigProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportInputProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.ReportProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.SourceClassListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.SourceClassProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.SourceProxyListProvisioningTO;
import com.orange.srs.refreport.model.TO.provisioning.TypeAndSubtypesProvisioningTO;
import com.orange.srs.refreport.model.parameter.GetFileNameToRetrieveParameter;
import com.orange.srs.refreport.model.parameter.GetProvisioningFilesParameter;
import com.orange.srs.refreport.model.parameter.RetrieveProvisioningFileParameter;
import com.orange.srs.refreport.model.parameter.RetrieveProvisioningFilesForASourceParameter;
import com.orange.srs.refreport.model.parameter.SourceFileParameter;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.business.commonFunctions.PatternResolutionDelegate;
import com.orange.srs.statcommon.business.report.SourceFactory;
import com.orange.srs.statcommon.model.TO.InputSourceProxyProvisioningTO;
import com.orange.srs.statcommon.model.TO.SourceProxyProvisioningTO;
import com.orange.srs.statcommon.model.TO.JobTO.JobSummaryTO;
import com.orange.srs.statcommon.model.TO.report.AbstractInputSourceListProvisioningTO;
import com.orange.srs.statcommon.model.TO.report.InputSourceTO;
import com.orange.srs.statcommon.model.enums.OriginEnum;
import com.orange.srs.statcommon.model.parameter.ProvisioningFileParameter;
import com.orange.srs.statcommon.model.parameter.ProvisioningSourceParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.interceptor.Logged;

/**
 * Business Facade regarding every operations on business objects involved in inventory description
 *
 * @author A159138
 * @author a116174
 */
@Logged
@Stateless
public class SOA05ProvisioningFacade {

	private static final Logger LOGGER = Logger.getLogger(SOA05ProvisioningFacade.class);

	/**
	 * Delegates
	 */
	@EJB
	private ProvisioningFileDelegate provisioningFileDelegate;

	@EJB
	private ProvisioningDelegate provisioningDelegate;

	@EJB
	private ReportingGroupDelegate reportingGroupDelegate;

	@EJB
	private CriteriaDelegate criteriaDelegate;

	@EJB
	private InputFormatDelegate inputFormatDelegate;

	@EJB
	private SourceClassDelegate sourceClassDelegate;

	@EJB
	private SourceProxyDelegate sourceProxyDelegate;

	/**
	 * Others
	 */
	@EJB
	private ProxyDelegate proxyDelegate;

	@EJB
	private InputSourceDelegate inputSourceDelegate;

	@EJB
	private FilterDifferentialUpdate filterDifferentialUpdate;

	@EJB
	private OfferAndOptionDifferentialUpdate offerAndOptionDifferentialUpdate;

	@EJB
	private ParamTypeDifferentialUpdate paramTypeDifferentialUpdate;

	@Inject
	private ReportConfigDifferentialUpdate reportConfigDifferentialUpdate;

	@EJB
	private ReportDifferentialUpdate reportDifferentialUpdate;

	@EJB
	private IndicatorDifferentialUpdate indicatorDifferentialUpdate;

	@EJB
	private ReportInputDifferentialUpdate reportInputDifferentialUpdate;

	@Inject
	private InputColumnDifferentialUpdate inputColumnDifferentialUpdate;

	@EJB
	private SourceClassDifferentialUpdate sourceClassDifferentialUpdate;

	@EJB
	private ProxyDifferentialUpdate proxyDifferentialUpdate;

	@Inject
	private InputSourceDifferentialUpdate inputSourceDifferentialUpdate;

	@EJB
	private EntityTypeDifferentialUpdate entityTypeDifferentialUpdate;

	@EJB
	private HyperlinkDifferentialUpdate hyperlinkDifferentialUpdate;

	@EJB
	private BookmarkDirectReportDifferentialUpdate bookmarkDirectReportDifferentialUpdate;

	@EJB
	private ExternalIndicatorDifferentialUpdate externalIndicatorDifferentialUpdate;

	@EJB
	private EntitiesDataLocationDelegate entitiesDataLocationDelegate;

	private final static Character fileSeparator = File.separatorChar;

	/**
	 * Retrieve the provisioning files listed in parameter, from the source specified in parameter, and uncompress them
	 */
	@Deprecated
	public ProvisioningFileStatusTO retrieveProvisioningFiles(RetrieveProvisioningFileParameter parameter,
			SOAContext soaContext) throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[retrieveProvisioningFiles] Start"));

		ProvisioningFileStatusTO result = new ProvisioningFileStatusTO();
		ArrayList<SourceFileParameter> fileListToRetrieve = new ArrayList<>();
		ArrayList<FileStatusTO> retrievedFileList = new ArrayList<>();
		ArrayList<FileStatusTO> notRetrievedFileList = new ArrayList<>();

		int totalNbFilesToBeRetrieved = 0;
		for (ProvisioningSourceParameter source : parameter.sourceList) {
			SourceFileParameter fileParam;
			String mount;
			String destinationFilePath;

			if (source.fileList.isEmpty()) {
				LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[retrieveProvisioningFiles] For source :"
						+ source.host + " not any files will be retrieved for Origin"));
			} else {
				// Compute the complete destination path for each file (or files if wildcard
				// present) to be retrieved
				for (ProvisioningFileParameter fileSet : source.fileList) {

					mount = Configuration.mountConfiguration
							.getProperty(fileSet.origin.toUpperCase() + PatternResolutionDelegate.FOLDER_SUFFIX);
					destinationFilePath = Configuration.rootProperty + fileSeparator + mount + fileSeparator
							+ Configuration.provisioningProperty + fileSeparator;
					GetFileNameToRetrieveTO fileNameToRetrieve = provisioningFileDelegate.getFileNameToRetrieve(
							new GetFileNameToRetrieveParameter(fileSet.fileName, fileSet.filePath,
									fileSet.fileExtension, fileSet.fileDate, fileSet.filePattern),
							soaContext);

					String sourceFileName = fileNameToRetrieve.getSourceFileName() + "." + fileSet.fileExtension;
					List<File> sourceFiles = (List<File>) FileUtils.listFiles(
							new File(fileNameToRetrieve.getSourceFilePath()), new WildcardFileFilter(sourceFileName),
							null);

					for (File sourceFile : sourceFiles) {
						String currentSourceFileName = sourceFile.getName();
						// if destinationFileName is empty, it means we should copy it from original
						// file
						String destinationFileName = fileNameToRetrieve.getDestFileName();
						if (destinationFileName == null || destinationFileName.isEmpty()) {
							destinationFileName = currentSourceFileName;
						} else {
							destinationFileName += "." + fileSet.fileExtension;
						}
						fileParam = new SourceFileParameter(sourceFile.getAbsolutePath(),
								destinationFilePath + destinationFileName);
						fileListToRetrieve.add(fileParam);
						LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[retrieveProvisioningFiles] For source :"
								+ source.host + " file path to be retrieved:" + fileNameToRetrieve.getDestFileName()));
					}
				}

				LOGGER.info(SOATools.buildSOALogMessage(soaContext,
						"[retrieveProvisioningFile] Get files from source :" + source.host));

				GetProvisioningFilesParameter getProvisioningFilesParameter = new GetProvisioningFilesParameter(
						fileListToRetrieve, source.userName, source.password, source.host, source.protocol);
				GetFilesTO retrievedResult = provisioningFileDelegate
						.getProvisioningFiles(getProvisioningFilesParameter, soaContext);

				retrievedFileList.addAll(retrievedResult.getRetrievedFileFlist());
				notRetrievedFileList.addAll(retrievedResult.getNotRetrievedFileFlist());

				totalNbFilesToBeRetrieved += fileListToRetrieve.size();
				fileListToRetrieve.clear();
			}
		}

		if (!retrievedFileList.isEmpty()) {
			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[retrieveProvisioningFile] Uncompressing Files"));

			for (FileStatusTO file : retrievedFileList) {
				try {
					LOGGER.info(SOATools.buildSOALogMessage(soaContext,
							"[retrieveProvisioningFile] Uncompressing file: " + file.name));
					if (!provisioningFileDelegate.uncompressProvisioningFile(file, soaContext))
						LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
								"[retrieveProvisioningFile] Error uncompressing file: " + file.name));
				} catch (IOException e) {
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
							"[retrieveProvisioningFile] Error uncompressing file: " + file.name));
				} catch (InstantiationException e) {
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
							"[retrieveProvisioningFile] Error uncompressing file: " + file.name));
				} catch (IllegalAccessException e) {
					LOGGER.warn(SOATools.buildSOALogMessage(soaContext,
							"[retrieveProvisioningFile] Error uncompressing file: " + file.name));
				}
			}
		}
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[retrieveProvisioningFile] End"));

		result.nbEffectiveRetrievedFile = retrievedFileList.size();
		result.nbExpectedRetrievedFile = totalNbFilesToBeRetrieved;
		result.files.addAll(retrievedFileList);
		result.files.addAll(notRetrievedFileList);

		return result;
	}

	/**
	 * Retrieve files from a source Type, the list of files to retrieve by source is configured in
	 * ProvisioningSourceTypeComfiguration.xml file
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ProvisioningFileStatusTO retrieveProvisioningFilesForASource(
			RetrieveProvisioningFilesForASourceParameter parameter, SOAContext soaContext) throws BusinessException {

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[retrieveProvisioningFilesForASource] Start the list of files retrieve for the sourceType: "
						+ parameter.getSource() + " and origin: " + parameter.getOrigin()));

		ProvisioningFileStatusTO result = null;
		ProvisioningSourceTypeConfigurationTO provisioningSourceTO = provisioningFileDelegate
				.getProvisioningSourceConfiguration(parameter.getSource(), soaContext);
		if (provisioningSourceTO != null) {
			RetrieveProvisioningFileParameter provisioningFileParameter = new RetrieveProvisioningFileParameter(
					provisioningSourceTO, parameter.getOrigin(), parameter.getDate());
			result = retrieveProvisioningFiles(provisioningFileParameter, soaContext);
		} else {
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"[retrieveProvisioningFilesForASource] The source type is unknown in configuration => no files to be retieved"));
			result = new ProvisioningFileStatusTO();
		}

		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[retrieveProvisioningFilesForASource] End"));
		return result;
	}

	public void addNewCriteria(SOAContext soaContext, List<CriteriaProvisioningTO> criteriaProvisioningTOs)
			throws BusinessException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "[addNewCriteria] Start"));
		}

		int nbCriteriaCreated = 0;
		try {

			CriteriaDelegate.sortCriteriaProvisioningTO(criteriaProvisioningTOs);
			List<Criteria> criteriaList = criteriaDelegate.getAllCriteriaSorted();

			int criteriaProvisioningTOIndex = 0;
			int criteriaIndex = 0;
			while (criteriaProvisioningTOIndex < criteriaProvisioningTOs.size()
					&& criteriaIndex < criteriaList.size()) {
				CriteriaProvisioningTO criteriaProvisioningTO = criteriaProvisioningTOs
						.get(criteriaProvisioningTOIndex);
				Criteria criteria = criteriaList.get(criteriaIndex);
				CriteriaKeyTO criteriaProvisioningTOKey = CriteriaDelegate
						.getCriteriaProvisioningTOKey(criteriaProvisioningTO);
				CriteriaKeyTO criteriaKey = CriteriaDelegate.getCriteriaKey(criteria);
				int keyComparisonResult = criteriaProvisioningTOKey.compareTo(criteriaKey);
				if (keyComparisonResult == 0) {
					// Both in XML and base: no update nor remove
					criteriaProvisioningTOIndex++;
					criteriaIndex++;
				} else if (keyComparisonResult < 0) {
					// In XML not in base:
					criteriaDelegate.createCriteria(criteriaProvisioningTO);
					nbCriteriaCreated++;
					criteriaProvisioningTOIndex++;
				} else {
					// In base not in XML: do nothing
					criteriaIndex++;
				}
			}
			while (criteriaProvisioningTOIndex < criteriaProvisioningTOs.size()) {
				criteriaDelegate.createCriteria(criteriaProvisioningTOs.get(criteriaProvisioningTOIndex));
				nbCriteriaCreated++;
				criteriaProvisioningTOIndex++;
			}

		} catch (Exception e) {
			throw new BusinessException("[addNewCriteria] Error in criteria provisioning", e);
		}

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"[addNewCriteria] Number of criteria to be created:" + nbCriteriaCreated + " at transaction commit"));
	}

	public void updateFilterByDifferential(SOAContext soaContext, List<FilterProvisioningTO> filterProvisioningTOs,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {
		filterDifferentialUpdate.updateByDifferential(soaContext, filterProvisioningTOs, forceUpdateFromFileToDatabase);
	}

	public void updateOfferAndOptionByDifferential(SOAContext soaContext,
			List<OfferAndOptionProvisioningTO> offerAndOptionProvisioningTOs, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {
		offerAndOptionDifferentialUpdate.updateByDifferential(soaContext, offerAndOptionProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	public void updateParamTypeByDifferential(SOAContext soaContext,
			List<ParamTypeProvisioningTO> paramTypeProvisioningTOs, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {
		paramTypeDifferentialUpdate.updateByDifferential(soaContext, paramTypeProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	public void updateReportConfigByDifferential(SOAContext soaContext,
			List<ReportConfigProvisioningTO> reportConfigProvisioningTOs, String offerOptionAlias,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {
		reportConfigDifferentialUpdate.setOfferOptionAlias(offerOptionAlias);
		reportConfigDifferentialUpdate.updateByDifferential(soaContext, reportConfigProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	public void updateIndicatorByDifferential(SOAContext soaContext,
			List<IndicatorProvisioningTO> indicatorProvisioningTOs, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {
		indicatorDifferentialUpdate.updateByDifferential(soaContext, indicatorProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	public void updateReportByDifferential(SOAContext soaContext, List<ReportProvisioningTO> reportProvisioningTOs,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {
		reportDifferentialUpdate.updateByDifferential(soaContext, reportProvisioningTOs, forceUpdateFromFileToDatabase);
	}

	public void updateReportInputByDifferential(SOAContext soaContext,
			List<ReportInputProvisioningTO> reportInputProvisioningTOs, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {
		reportInputDifferentialUpdate.updateByDifferential(soaContext, reportInputProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	public void updateInputFormatByDifferential(SOAContext soaContext,
			List<InputColumnProvisioningTO> inputColumnProvisioningTOs, String formatType,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {
		InputFormat inputFormat = inputFormatDelegate.createInputFormatIfNotExist(formatType);
		inputColumnDifferentialUpdate.setInputFormat(inputFormat);
		inputColumnDifferentialUpdate.updateByDifferential(soaContext, inputColumnProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	public SourceClassListProvisioningTO updateSourceClassByDifferentialAndMarshallToFile(SOAContext soaContext,
			List<SourceClassProvisioningTO> sourceClassProvisioningTOs, File sourceClassProvisioningFile,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {
		sourceClassDifferentialUpdate.updateByDifferential(soaContext, sourceClassProvisioningTOs,
				forceUpdateFromFileToDatabase);
		if (forceUpdateFromFileToDatabase) {
			// Do not regenerate files in case of force update
			return null;
		}
		SourceClassListProvisioningTO sourceClassListProvisioningTOToMarshall = sourceClassDelegate
				.getSourceClassListProvisioningTOSorted();
		AbstractProvisioningCommand.marshallProvisioningTOToProvisioningFile(sourceClassListProvisioningTOToMarshall,
				sourceClassProvisioningFile, false);
		return sourceClassListProvisioningTOToMarshall;
	}

	public SourceClassListProvisioningTO updateSourceClassByDifferentialMarshalToFileAndRollbackIfNecessary(
			SOAContext soaContext, List<SourceClassProvisioningTO> sourceClassProvisioningTOs,
			boolean forceUpdateFromFileToDatabase) {
		try {
			File sourceClassProvisioningFile = new File(
					SourceClassProvisioningCommand.getSourceClassProvisioningFilePath());
			return updateSourceClassByDifferentialAndMarshallToFile(soaContext, sourceClassProvisioningTOs,
					sourceClassProvisioningFile, forceUpdateFromFileToDatabase);
		} catch (BusinessException be) {
			// Throw a RollbackException (runtime exception) instead to business exception
			// in order to rollback the transaction and so cancel commit in database
			throw new RollbackException(be);
		}
	}

	public ProxyListProvisioningTO updateProxyByDifferentialAndMarshallToFile(SOAContext soaContext,
			List<ProxyProvisioningTO> proxyProvisioningTOs, File proxyProvisioningFile,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {
		proxyDifferentialUpdate.updateByDifferential(soaContext, proxyProvisioningTOs, forceUpdateFromFileToDatabase);
		if (forceUpdateFromFileToDatabase) {
			// Do not regenerate files in case of force update
			return null;
		}
		ProxyListProvisioningTO proxyListProvisioningTOToMarshall = proxyDelegate.getProxyListProvisioningTOSorted();
		AbstractProvisioningCommand.marshallProvisioningTOToProvisioningFile(proxyListProvisioningTOToMarshall,
				proxyProvisioningFile, false);
		return proxyListProvisioningTOToMarshall;
	}

	public ProxyListProvisioningTO updateProxyByDifferentialMarshalToFileAndRollbackIfNecessary(SOAContext soaContext,
			List<ProxyProvisioningTO> proxyProvisioningTOs, boolean forceUpdateFromFileToDatabase) {
		try {
			File proxyProvisioningFile = new File(ProxyProvisioningCommand.getProxyProvisioningFilePath());
			return updateProxyByDifferentialAndMarshallToFile(soaContext, proxyProvisioningTOs, proxyProvisioningFile,
					forceUpdateFromFileToDatabase);
		} catch (BusinessException be) {
			// Throw a RollbackException (runtime exception) instead to business exception
			// in order to rollback the transaction and so cancel commit in database
			throw new RollbackException(be);
		}
	}

	public AbstractInputSourceListProvisioningTO updateInputSourceByDifferentialAndMarshallToFile(SOAContext soaContext,
			List<InputSourceTO> inputSourceTOs, String sourceClazz, File inputSourceProvisioningFile,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {
		SourceClass sourceClass = sourceClassDelegate.getSourceClassByKey(sourceClazz);
		inputSourceDifferentialUpdate.setSourceClass(sourceClass);
		inputSourceDifferentialUpdate.updateByDifferential(soaContext, inputSourceTOs, forceUpdateFromFileToDatabase);
		AbstractInputSourceListProvisioningTO inputSourceListProvisioningTOToMarshall = SourceFactory
				.getInputSourceListProvisioningTO(sourceClazz);
		if (inputSourceListProvisioningTOToMarshall == null) {
			throw new BusinessException("InputSource provisioning not managed for sourceClass=" + sourceClazz);
		}
		if (forceUpdateFromFileToDatabase) {
			// Do not regenerate files in case of force update
			return null;
		}
		inputSourceListProvisioningTOToMarshall.inputSourceProvisioningTOs = inputSourceDelegate
				.getInputSourceTOsSortedForSourceClass(sourceClazz);
		AbstractProvisioningCommand.marshallProvisioningTOToProvisioningFile(inputSourceListProvisioningTOToMarshall,
				inputSourceProvisioningFile, true);
		return inputSourceListProvisioningTOToMarshall;
	}

	public AbstractInputSourceListProvisioningTO updateInputSourceByDifferentialMarshalToFileAndRollbackIfNecessary(
			SOAContext soaContext, List<InputSourceTO> inputSourceTOs, String sourceClazz,
			boolean forceUpdateFromFileToDatabase) {
		try {
			File inputSourceProvisioningFile = new File(
					InputSourceProvisioningCommand.getInputSourceProvisioningDirectoryPath() + File.separatorChar
							+ sourceClazz + ".xml");
			return updateInputSourceByDifferentialAndMarshallToFile(soaContext, inputSourceTOs, sourceClazz,
					inputSourceProvisioningFile, forceUpdateFromFileToDatabase);
		} catch (BusinessException be) {
			// Throw a RollbackException (runtime exception) instead to business exception
			// in order to rollback the transaction and so cancel commit in database
			throw new RollbackException(be);
		}
	}

	public void updateSourceProxyByDifferential(SOAContext soaContext,
			InputSourceProxyProvisioningTO inputSourceProxyProvisioningTO) throws BusinessException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext,
					"[updateByDifferential] Start for " + inputSourceProxyProvisioningTO));
		}

		int nbCreated = 0;
		int nbUpdated = 0;
		int nbRemoved = 0;
		try {

			// Sort both lists that will be compared
			SourceProxyDelegate.sortSourceProxyProvisioningTO(inputSourceProxyProvisioningTO.sourceProxies);
			List<SourceProxy> sourceProxies = sourceProxyDelegate
					.getAllSourceProxySortedForInputSource(inputSourceProxyProvisioningTO);

			// Check provisioning data
			Set<Integer> indexProxyProvisioningTOs = new HashSet<>();
			for (SourceProxyProvisioningTO sourceProxyProvisioningTO : inputSourceProxyProvisioningTO.sourceProxies) {
				if (!Boolean.TRUE.equals(sourceProxyProvisioningTO.suppress)) {
					if (indexProxyProvisioningTOs.contains(sourceProxyProvisioningTO.index)) {
						throw new BusinessException("Unable to proccess sourceProxy [uri="
								+ sourceProxyProvisioningTO.uri + "], the index [" + sourceProxyProvisioningTO.index
								+ "] is already defined in the provisoning file for another sourceProxy of "
								+ inputSourceProxyProvisioningTO);
					}
					indexProxyProvisioningTOs.add(sourceProxyProvisioningTO.index);
				}
			}

			int sourceProxyProvisioningTOIndex = 0;
			int sourceProxyIndex = 0;

			while (sourceProxyProvisioningTOIndex < inputSourceProxyProvisioningTO.sourceProxies.size()
					&& sourceProxyIndex < sourceProxies.size()) {

				// Retrieve objects and keys
				SourceProxyProvisioningTO sourceProxyProvisioningTO = inputSourceProxyProvisioningTO.sourceProxies
						.get(sourceProxyProvisioningTOIndex);
				SourceProxy sourceProxy = sourceProxies.get(sourceProxyIndex);

				// Compare the objects
				if (sourceProxyProvisioningTO.index == sourceProxy.getIndex()) {
					if (sourceProxyProvisioningTO.uri.equals(sourceProxy.getUri())) {
						if (Boolean.TRUE.equals(sourceProxyProvisioningTO.suppress)) {
							sourceProxyDelegate.removeSourceProxy(sourceProxy, inputSourceProxyProvisioningTO);
							nbRemoved++;
						} else {
							sourceProxyIndex++;
						}
					} else if (!Boolean.TRUE.equals(sourceProxyProvisioningTO.suppress)) {
						// Loop if the Proxy is already defined in the existing
						// proxy list
						SourceProxy targetProxy = null;
						for (SourceProxy sourceProxyCurrent : sourceProxies) {
							if (sourceProxyCurrent.getUri().equals(sourceProxyProvisioningTO.uri)) {
								targetProxy = sourceProxyCurrent;
								break;
							}
						}
						if (targetProxy != null) {
							sourceProxyDelegate.updateSourceProxy(sourceProxyProvisioningTO.index, targetProxy,
									inputSourceProxyProvisioningTO);
							nbUpdated++;
						} else {
							sourceProxyDelegate.createSourceProxyFromProvisioningTO(sourceProxyProvisioningTO,
									inputSourceProxyProvisioningTO);
							nbCreated++;
						}
					}
					sourceProxyProvisioningTOIndex++;
				} else {
					// At this point of the algorithm, the proxyIndex defined in
					// sourceProxyProvisioningTO can not be lower than the
					// proxyIndex of current sourceProxy
					sourceProxyIndex++;
				}
			}

			while (sourceProxyProvisioningTOIndex < inputSourceProxyProvisioningTO.sourceProxies.size()) {

				SourceProxyProvisioningTO sourceProxyProvisioningTO = inputSourceProxyProvisioningTO.sourceProxies
						.get(sourceProxyProvisioningTOIndex);

				// Create if necessary
				if (!Boolean.TRUE.equals(sourceProxyProvisioningTO.suppress)) {
					sourceProxyDelegate.createSourceProxyFromProvisioningTO(sourceProxyProvisioningTO,
							inputSourceProxyProvisioningTO);
					nbCreated++;
				}
				sourceProxyProvisioningTOIndex++;
			}

		} catch (BusinessException be) {
			throw be;
		} catch (Exception e) {
			throw new BusinessException(
					"[updateByDifferential] Error in sourceProxy provisioning for " + inputSourceProxyProvisioningTO,
					e);
		}

		String logMessage = "[updateByDifferential] Number of sourceProxy to be created:" + nbCreated
				+ ", to be updated:" + nbUpdated + ", to be deleted:" + nbRemoved + " at transaction commit for "
				+ inputSourceProxyProvisioningTO;
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, logMessage));
	}

	public void updateSourceProxyByDifferential(SOAContext soaContext,
			List<InputSourceProxyProvisioningTO> inputSourceProxyProvisioningTOs) throws BusinessException {

		if (inputSourceProxyProvisioningTOs != null && inputSourceProxyProvisioningTOs.size() > 0) {
			for (InputSourceProxyProvisioningTO inputSourceProxyProvisioningTO : inputSourceProxyProvisioningTOs) {

				updateSourceProxyByDifferential(soaContext, inputSourceProxyProvisioningTO);

			}
		}
	}

	public SourceProxyListProvisioningTO updateSourceProxyByDifferentialAndMarshallToFile(SOAContext soaContext,
			List<InputSourceProxyProvisioningTO> inputSourceProxyProvisioningTOs, File sourceProxyProvisioningFile)
			throws BusinessException {
		updateSourceProxyByDifferential(soaContext, inputSourceProxyProvisioningTOs);
		SourceProxyListProvisioningTO sourceProxyListProvisioningTOToMarshall = sourceProxyDelegate
				.getSourceProxyListProvisioningTOSorted();
		AbstractProvisioningCommand.marshallProvisioningTOToProvisioningFile(sourceProxyListProvisioningTOToMarshall,
				sourceProxyProvisioningFile, false);
		return sourceProxyListProvisioningTOToMarshall;
	}

	public SourceProxyListProvisioningTO updateSourceProxyByDifferentialMarshalToFileAndRollbackIfNecessary(
			SOAContext soaContext, List<InputSourceProxyProvisioningTO> inputSourceProxyProvisioningTOs) {
		try {
			File sourceProxyProvisioningFile = new File(
					SourceProxyProvisioningCommand.getSourceProxyProvisioningFilePath());
			return updateSourceProxyByDifferentialAndMarshallToFile(soaContext, inputSourceProxyProvisioningTOs,
					sourceProxyProvisioningFile);
		} catch (BusinessException be) {
			// Throw a RollbackException (runtime exception) instead to business exception
			// in order to rollback the transaction and so cancel commit in database
			throw new RollbackException(be);
		}
	}

	public void updateTypeSubtypeByDifferential(SOAContext soaContext,
			List<TypeAndSubtypesProvisioningTO> typeAndSubtypesProvisioningTOs, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {
		entityTypeDifferentialUpdate.updateByDifferential(soaContext, typeAndSubtypesProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	public void updateHyperlinkByDifferential(SOAContext soaContext,
			List<HyperlinkProvisioningTO> hyperlinkProvisioningTOs, boolean forceUpdateFromFileToDatabase)
			throws BusinessException {
		hyperlinkDifferentialUpdate.updateByDifferential(soaContext, hyperlinkProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	public void updateBookmarkDirectReportByDifferential(SOAContext soaContext,
			List<BookmarkDirectReportProvisioningTO> bookmarkDirectReportProvisioningTOs,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {
		bookmarkDirectReportDifferentialUpdate.updateByDifferential(soaContext, bookmarkDirectReportProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	public void updateExternalIndicatorByDifferential(SOAContext soaContext,
			List<ExternalIndicatorProvisioningTO> externalIndicatorProvisioningTOs,
			boolean forceUpdateFromFileToDatabase) throws BusinessException {
		externalIndicatorDifferentialUpdate.updateByDifferential(soaContext, externalIndicatorProvisioningTOs,
				forceUpdateFromFileToDatabase);
	}

	@Deprecated
	public void migrateRefReportReportingGroupsToRefObjectReportingGroups(SOAContext soaContext,
			boolean simulateMigration) throws Exception {
		provisioningDelegate.migrateRefReportReportingGroups(soaContext, simulateMigration);
	}

	public void exportObjects(SOAContext soaContext) throws Exception {

		long start = Utils.getTime();
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Start: Exporting objects"));

		Path exportFilePath = Paths.get(Configuration.rootProperty, Configuration.configProvisioningProperty,
				"exportEntities.xml");
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Reading configuration file" + exportFilePath);
		}
		ExportEntitiesTO entitiesTO = null;
		try {
			entitiesTO = (ExportEntitiesTO) JAXBRefReportFactory.getUnmarshaller().unmarshal(exportFilePath.toFile());
		} catch (Exception e) {
			throw new BusinessException("Unable to unmarshall information in provisioning file " + exportFilePath, e);
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Configuration file" + exportFilePath + " read in " + (Utils.getTime() - start) + " ms");
		}

		provisioningDelegate.exportObjects(soaContext, entitiesTO);

		LOGGER.info(SOATools.buildSOALogMessage(soaContext,
				"End: Exporting objects in " + (Utils.getTime() - start) + " ms"));
	}

	public ProvisioningActionStatusTO importProvisioningFiles(SOAContext soaContext, JobSummaryTO jobSummaryTO,
			Calendar provisioningDate, boolean provisioningDiff) {
		return provisioningDelegate.importProvisioningFiles(soaContext, jobSummaryTO, provisioningDate,
				provisioningDiff);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ProvisioningActionStatusTO updateEntitiesDataLocation(SOAContext soaContext, Calendar provisioningDate) {
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "[updateEntitiesDataLocation] Start"));
		ProvisioningActionStatusTO actionStatus = new ProvisioningActionStatusTO("updateEntitiesDataLocation");
		long startTime = Utils.getTime();
		try {
			Map<String, Map<String, String>> mapReportinGroupEquantLocationByOrigin = reportingGroupDelegate
					.getReportingGroupRefDataLocationByDomainByOrigin(provisioningDate, soaContext);
			entitiesDataLocationDelegate.updateEntitiesDataLocation(soaContext, actionStatus,
					mapReportinGroupEquantLocationByOrigin.get(OriginEnum.EQUANT.getValue()),
					mapReportinGroupEquantLocationByOrigin.get(OriginEnum.SCE.getValue()));
			actionStatus.duration = Utils.getTime() - startTime;
			LOGGER.info(SOATools.buildSOALogMessage(soaContext,
					"[updateEntitiesDataLocation] End in " + actionStatus.duration + " ms"));
		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext,
					"[updateEntitiesDataLocation] Error: " + e.getMessage(), e));
			actionStatus.error = true;
		}
		return actionStatus;
	}

}

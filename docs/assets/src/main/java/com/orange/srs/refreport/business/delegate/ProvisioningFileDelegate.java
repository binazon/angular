package com.orange.srs.refreport.business.delegate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.orange.srs.refreport.business.ConfigurationXMLConstant;
import com.orange.srs.refreport.consumer.file.NASFileConsumer;
import com.orange.srs.refreport.consumer.file.SFTPFileConsumer;
import com.orange.srs.refreport.model.TO.FileStatusTO;
import com.orange.srs.refreport.model.TO.GetFileNameToRetrieveTO;
import com.orange.srs.refreport.model.TO.GetFilesTO;
import com.orange.srs.refreport.model.TO.ProvisioningSourceTypeConfigurationTO;
import com.orange.srs.refreport.model.parameter.GetFileNameToRetrieveParameter;
import com.orange.srs.refreport.model.parameter.GetProvisioningFilesParameter;
import com.orange.srs.refreport.technical.CompressedFile;
import com.orange.srs.refreport.technical.CompressedFileFactory;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.business.commonFunctions.PatternResolutionDelegate;
import com.orange.srs.statcommon.model.TO.ProvisioningFileTO;
import com.orange.srs.statcommon.model.TO.ProvisioningSourceTO;
import com.orange.srs.statcommon.model.parameter.PatternParameter;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

@Stateless
public class ProvisioningFileDelegate {

	private static Logger logger = Logger.getLogger(ProvisioningFileDelegate.class);

	private static final String fileSeparator = File.separator;

	/**
	 * Get the Name Of the file to retrieve, taking into account the Origin, the date, etc...
	 * 
	 * @param parameter
	 * @param soacontext
	 * @return
	 * @throws BusinessException
	 */
	public GetFileNameToRetrieveTO getFileNameToRetrieve(GetFileNameToRetrieveParameter parameter,
			SOAContext soacontext) throws BusinessException {
		GetFileNameToRetrieveTO result;
		if (parameter.filePattern == null) {
			throw new BusinessException(
					BusinessException.WRONG_PARAMETER_EXCEPTION + " FilePattern of the file to be retrieved is null");
		} else if (parameter.filePath == null) {
			throw new BusinessException(
					BusinessException.WRONG_PARAMETER_EXCEPTION + " FilePath of the file to be retrieved is null");
		} else if (parameter.fileExtension == null) {
			throw new BusinessException(
					BusinessException.WRONG_PARAMETER_EXCEPTION + " Extension of the file to be retrieved is null");
		}

		PatternParameter patternParam = new PatternParameter();
		patternParam.startUnit = parameter.fileDate.getTime();
		patternParam.properties = Configuration.mountConfiguration;

		result = new GetFileNameToRetrieveTO(
				PatternResolutionDelegate.resolveBasePattern(parameter.filePattern, patternParam),
				PatternResolutionDelegate.resolveBasePattern(parameter.filePath, patternParam),
				PatternResolutionDelegate.resolveBasePattern(parameter.fileName, patternParam));
		return result;
	}

	/**
	 * Copy the files listed in "parameter" in a local directory from a distant source. The protocol to access the
	 * source can be SFTP, or a Mount Point
	 * 
	 * @param parameter
	 *            Contains: the list of the files to get, The information relative to the source (host name, password,
	 *            protocol...)
	 * @param soaContext
	 *            To log in SOA context
	 * @return The list of the Retrieved Files
	 * @throws BusinessException
	 */
	public GetFilesTO getProvisioningFiles(GetProvisioningFilesParameter parameter, SOAContext soaContext)
			throws BusinessException {

		GetFilesTO result = new GetFilesTO();
		SFTPFileConsumer sftpFileConsumer = null;
		try {
			if (parameter.protocol.equalsIgnoreCase("sftp")) {
				sftpFileConsumer = new SFTPFileConsumer();
				result = sftpFileConsumer.getFiles(parameter, soaContext);
			} else if (parameter.protocol.equalsIgnoreCase("nas")) {
				NASFileConsumer nasFileConsumer = new NASFileConsumer();
				result = nasFileConsumer.getFiles(parameter, soaContext);
			} else {
				String strErrorMessage = "[getProvisioningFiles] Protocol:\"" + parameter.protocol + "\" not handled";
				throw new BusinessException(strErrorMessage);
			}
		} catch (IOException e) {
			throw new BusinessException(e.getMessage(), e);
		} finally {
			if (sftpFileConsumer != null) {
				try {
					sftpFileConsumer.close(soaContext);
				} catch (IOException e) {
					logger.error(SOATools.buildSOALogMessage(soaContext,
							"[getProvisioningFiles] Error SFTP connection close"));
				}
			}
		}

		return result;
	}

	/**
	 * Get the provisioning source configuration from the configuration file: ProvisioningSourceTypeConfiguration.xml
	 * corresponding to the sourceType passed in parameter
	 * 
	 * @param sourceType
	 * @return provisioning source configuration
	 */
	public ProvisioningSourceTypeConfigurationTO getProvisioningSourceConfiguration(String sourceType,
			SOAContext soaContext) {
		Character fileSeparator = File.separatorChar;

		Map<String, ProvisioningSourceTypeConfigurationTO> lstProvisioningSourceTypeConfiguration = new HashMap<String, ProvisioningSourceTypeConfigurationTO>();

		SAXBuilder sxb = new SAXBuilder();
		try {
			logger.info(SOATools.buildSOALogMessage(soaContext,
					"[getProvisioningSourceConfiguration] Reading file: ProvisioningSourceTypeConfiguration.xml"));
			Document document;
			document = sxb.build(
					new FileReader(Configuration.rootProperty + fileSeparator + Configuration.configProvisioningProperty
							+ fileSeparator + "ProvisioningSourceTypeConfiguration.xml"));
			List<Element> lstProvisioningSourceType = document.getDocument().getRootElement()
					.getChildren(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_PROVISIONING_SOURCE_TYPE);
			for (Element provisioningSourceType : lstProvisioningSourceType) {
				ProvisioningSourceTypeConfigurationTO provisioningSourceTypeTO = new ProvisioningSourceTypeConfigurationTO();
				if (provisioningSourceType
						.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_SOURCE_TYPE) != null) {

					provisioningSourceTypeTO.setSource(provisioningSourceType
							.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_SOURCE_TYPE).getText());
					if (logger.isDebugEnabled()) {
						logger.debug(SOATools.buildSOALogMessage(soaContext,
								"[getProvisioningSourceConfiguration] SourceType found: "
										+ provisioningSourceTypeTO.getSource()));
					}
					Element lstProvisioningSource = provisioningSourceType
							.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_LIST_PROVISIONING_SOURCE);

					for (Element provisioningSource : (List<Element>) lstProvisioningSource
							.getChildren(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_PROVISIONING_SOURCE)) {

						ProvisioningSourceTO provisioningSourceTO = new ProvisioningSourceTO();
						provisioningSourceTO.setSourceName(provisioningSource
								.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_SOURCE_NAME).getText());
						provisioningSourceTO.setProtocol(provisioningSource
								.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_PROTOCOL).getText());
						String strHost = provisioningSource
								.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_HOST).getText();
						if (strHost != null)
							provisioningSourceTO.setHost(strHost);
						else
							provisioningSourceTO.setHost(provisioningSourceTO.getSourceName());
						provisioningSourceTO.setUserName(provisioningSource
								.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_USER_NAME).getText());
						provisioningSourceTO.setPassword(provisioningSource
								.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_PASSWORD).getText());
						if (logger.isDebugEnabled()) {
							logger.debug(SOATools.buildSOALogMessage(soaContext,
									"[getProvisioningSourceConfiguration] Source type "
											+ provisioningSourceTypeTO.getSource() + " found: "
											+ provisioningSourceTO.getSourceName()));
						}
						Element lstProvisioningFile = provisioningSource
								.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_LIST_FILES);
						List<Element> listProvisioningFiles = lstProvisioningFile
								.getChildren(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_FILE);

						for (Element provisioningFile : listProvisioningFiles) {
							ProvisioningFileTO provisioningFileTO = new ProvisioningFileTO();

							provisioningFileTO.setFileName(provisioningFile
									.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_FILE_NAME).getText());
							provisioningFileTO.setFileExtension(provisioningFile
									.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_FILE_EXTENSION).getText());
							provisioningFileTO.setFilePath(provisioningFile
									.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_FILE_PATH).getText());
							provisioningFileTO.setFilePattern(provisioningFile
									.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_FILE_PATTERN).getText());
							provisioningFileTO.setOrigin(provisioningFile
									.getChild(ConfigurationXMLConstant.PROVISIONINGFILE_NODE_ORIGIN).getText());
							if (logger.isDebugEnabled()) {
								logger.debug(SOATools.buildSOALogMessage(soaContext,
										"[getProvisioningSourceConfiguration] File found for source:"
												+ provisioningSourceTO.getSourceName() + ", file: "
												+ provisioningFileTO.getFileName()));
							}
							provisioningSourceTO.addProvisioningFile(provisioningFileTO);
						}
						if (!provisioningSourceTO.getListProvisioningFileTO().isEmpty()) {
							provisioningSourceTypeTO.addProvisioningSourceTO(provisioningSourceTO);
						}
						logger.info(SOATools.buildSOALogMessage(soaContext,
								"[getProvisioningSourceConfiguration] The source contains:"
										+ String.valueOf(provisioningSourceTO.getListProvisioningFileTO().size())
										+ " file(s) "));
					}
					lstProvisioningSourceTypeConfiguration.put(provisioningSourceTypeTO.getSource(),
							provisioningSourceTypeTO);

				}

			}
		} catch (FileNotFoundException e) {
			logger.error(SOATools.buildSOALogMessage(soaContext,
					"[getProvisioningSourceConfiguration] " + Configuration.rootProperty + fileSeparator
							+ Configuration.configProvisioningProperty + fileSeparator
							+ "ProvisioningSourceTypeConfiguration.xml" + ": File not found"));
		} catch (JDOMException e) {
			logger.error(SOATools.buildSOALogMessage(soaContext,
					"[getProvisioningSourceConfiguration] " + Configuration.rootProperty + fileSeparator
							+ Configuration.configProvisioningProperty + fileSeparator
							+ "ProvisioningSourceTypeConfiguration.xml" + ": Incorrect XML format -> "
							+ e.getMessage()));
		} catch (IOException e) {
			logger.error(SOATools.buildSOALogMessage(soaContext,
					"[getProvisioningSourceConfiguration] " + Configuration.rootProperty + fileSeparator
							+ Configuration.configProvisioningProperty + fileSeparator
							+ "ProvisioningSourceTypeConfiguration.xml" + ": I/O problems -> " + e.getMessage()));
		}

		ProvisioningSourceTypeConfigurationTO config = lstProvisioningSourceTypeConfiguration.get(sourceType);
		return config;
	}

	/**
	 * Uncompress a file according to its extension: .Z, .gz
	 * 
	 * @param fileParam
	 *            Names of the file to uncompress
	 * @param soaContext
	 *            to log in SOA context
	 * @return
	 * 
	 * @throws BusinessException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public boolean uncompressProvisioningFile(FileStatusTO fileParam, SOAContext soaContext)
			throws BusinessException, IOException, InstantiationException, IllegalAccessException {
		CompressedFile compressedFile;
		String filePath = fileParam.name;

		int index_extension = filePath.lastIndexOf('.', filePath.length());
		if (index_extension != -1) {
			long startTime = Utils.getTime();

			String ext = (filePath.substring(index_extension, filePath.length()));
			if (logger.isDebugEnabled()) {
				logger.debug(
						SOATools.buildSOALogMessage(soaContext, "[uncompressProvisioningFile] File extension= " + ext));
			}

			compressedFile = CompressedFileFactory
					.createCompressedFile(filePath.substring(index_extension, filePath.length()));
			if (compressedFile == null) {
				logger.info(
						SOATools.buildSOALogMessage(soaContext, "[uncompressProvisioningFile] The extension of file "
								+ filePath + " does not indicate it as compressed => no need to be uncompressed"));
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug(SOATools.buildSOALogMessage(soaContext,
							"[uncompressProvisioningFile] Uncompressing file " + filePath));
				}
				compressedFile.uncompress(filePath, filePath.substring(0, index_extension), soaContext);
				FileUtils.deleteQuietly(new File(filePath));
			}
			fileParam.treatmentDuration = Utils.getTime() - startTime;
		} else {
			logger.warn(SOATools.buildSOALogMessage(soaContext,
					"[uncompressProvisioningFile] File " + filePath + "does not have any extension"));
			return false;
		}

		return true;
	}

}

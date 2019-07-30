package com.orange.srs.refreport.test.yellow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.orange.srs.common.test.model.ConfigTestParameters;
import com.orange.srs.common.test.parameter.MySQLDumpDBParameterFromConfiguration;
import com.orange.srs.common.test.parameter.MySQLRestoreDBParameterFromConfiguration;
import com.orange.srs.common.test.technical.FileResultTO;
import com.orange.srs.common.test.technical.TableResultTO;
import com.orange.srs.common.test.technical.TestCaseWithConfigurationCommand;
import com.orange.srs.common.test.technical.TestException;
import com.orange.srs.refreport.business.command.provisioning.AbstractProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.CriteriaProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.HyperlinkProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.IndicatorProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.InputFormatProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.InputSourceProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.OfferAndOptionProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ParamTypeProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ProxyProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ReportConfigProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ReportInputProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ReportProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.SourceClassProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.SourceProxyProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.TypeSubtypeProvisioningCommand;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.XmlFileOnlyFilter;
import com.orange.srs.refreport.technical.test.RefReportTestConfiguration;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

public abstract class YellowProvisioningTest extends TestCaseWithConfigurationCommand {

	@EJB
	private CriteriaProvisioningCommand criteriaProvisioningCommand;

	@EJB
	private OfferAndOptionProvisioningCommand offerProvisioningCommand;

	@EJB
	private ParamTypeProvisioningCommand paramTypeProvisioningCommand;

	@EJB
	private ReportConfigProvisioningCommand reportConfigProvisioningCommand;

	@EJB
	private IndicatorProvisioningCommand indicatorProvisioningCommand;

	@EJB
	private ReportProvisioningCommand reportProvisioningCommand;

	@EJB
	private ReportInputProvisioningCommand reportInputProvisioningCommand;

	@EJB
	private InputFormatProvisioningCommand inputFormatProvisioningCommand;

	@EJB
	private SourceClassProvisioningCommand sourceClassProvisioningCommand;

	@EJB
	private ProxyProvisioningCommand proxyProvisioningCommand;

	@EJB
	private InputSourceProvisioningCommand inputSourceProvisioningCommand;

	@EJB
	private SourceProxyProvisioningCommand sourceProxyProvisioningCommand;

	@EJB
	private TypeSubtypeProvisioningCommand typeSubtypeProvisioningCommand;

	@EJB
	private HyperlinkProvisioningCommand hyperlinkProvisioningCommand;

	private static final String FILE_NAME_PROXY = "proxy.xml";
	private static final String FILE_NAME_CRITERIA = "criteria.xml";
	private static final String FILE_NAME_INDICATOR = "indicator.xml";
	private static final String FILE_NAME_PARAM_TYPE = "paramType.xml";
	private static final String FILE_NAME_TYPE_SUBTYPE = "typeSubtype.xml";
	private static final String FILE_NAME_SOURCE_CLASS = "sourceClass.xml";
	private static final String FILE_NAME_SOURCE_PROXY = "sourceProxy.xml";
	private static final String FILE_NAME_HYPERLINK = "hyperlink.xml";
	private static final String FILE_ERROR_EXTENSION = ".error";

	protected String initialRoot;
	protected String confRoot;
	protected String testName;
	protected File generatedDB;
	protected String testMessage;
	protected ArrayList<FileResultTO> fileResults;
	protected Map<String, AbstractProvisioningCommand> provisioningCommandsByWatchedPath;
	protected ArrayList<String> subpathList;
	protected ArrayList<TableResultTO> tableResults;

	protected abstract boolean compareDatabases(UriInfo context, ConfigTestParameters configTestParameters)
			throws TestException;

	@Override
	public String getTestName() {
		if (testName != null) {
			return "YellowProvisioningTest - " + testName;
		} else {
			return "YellowProvisioningTest";
		}
	}

	@Override
	public void buildData(UriInfo context, ConfigTestParameters configTestParameters) throws TestException {
		// Save the name of the test
		testName = configTestParameters.getTestName();

		// Change the root property
		initialRoot = Configuration.rootProperty;
		String tempRoot = Configuration.rootProperty + File.separatorChar + "test" + File.separatorChar + "unittest"
				+ File.separatorChar + "data";
		Configuration.rootProperty = tempRoot;
		Configuration.mountConfiguration.put("ROOT", tempRoot);

		// Backup of the current database
		MySQLDumpDBParameterFromConfiguration backupParameter = RefReportTestConfiguration
				.getMySQLDumpBDParameterFromConfiguration("BackupMySqlDBPath");
		backupMySQLDataBaseWithDump(backupParameter);

		if (testName.equals("YellowProvisioningTest")) {
			// Start from an empty database for the test
			MySQLRestoreDBParameterFromConfiguration parameter = RefReportTestConfiguration
					.getMySQLRestoreBDParameterFromConfiguration("EmptyDatabasePath");
			restoreMySQLDataBaseWithDump(parameter, "refreport", true);
		} else {
			// Start from a provisioned yellow part
			MySQLRestoreDBParameterFromConfiguration parameter = RefReportTestConfiguration
					.getMySQLRestoreBDParameterFromConfiguration("UpdateYellowDBPath");
			restoreMySQLDataBaseWithDump(parameter, "refreport", true);
		}

		// Copy the right source files into the correct directory
		File sourceDirectory = null;
		confRoot = Configuration.acceptancetestFolderProperty;
		if (testName.equals("YellowProvisioningTest")) {
			sourceDirectory = new File(confRoot + File.separatorChar + "source" + File.separatorChar
					+ Configuration.configProvisioningProperty + File.separatorChar);
		} else {
			sourceDirectory = new File(confRoot + File.separatorChar + "sourceUpdate" + File.separatorChar
					+ Configuration.configProvisioningProperty + File.separatorChar);
		}

		File destDirectory = new File(Configuration.rootProperty + File.separatorChar
				+ Configuration.configProvisioningProperty + File.separatorChar);
		try {
			FileUtils.copyDirectory(sourceDirectory, destDirectory);
		} catch (IOException ioEx) {
			throw new TestException(getTestName(), ioEx.getMessage());
		}

		// Put in a map the path of every configuration files needed for the test
		subpathList = new ArrayList<String>();
		String rootPath = Configuration.rootProperty + File.separatorChar;
		provisioningCommandsByWatchedPath = new LinkedHashMap<String, AbstractProvisioningCommand>();

		String criteriaProvisioningFilePath = rootPath + Configuration.configCriteriaProperty + File.separatorChar
				+ FILE_NAME_CRITERIA;
		provisioningCommandsByWatchedPath.put(criteriaProvisioningFilePath, criteriaProvisioningCommand);
		subpathList.add(Configuration.configCriteriaProperty + File.separatorChar + FILE_NAME_CRITERIA);

		String offerProvisioningDirectoryPath = rootPath + Configuration.configOfferProperty;
		provisioningCommandsByWatchedPath.put(offerProvisioningDirectoryPath, offerProvisioningCommand);
		subpathList.add(Configuration.configOfferProperty);

		String inputFormatProvisioningDirectoryPath = rootPath + Configuration.configInputformatProperty;
		provisioningCommandsByWatchedPath.put(inputFormatProvisioningDirectoryPath, inputFormatProvisioningCommand);
		subpathList.add(Configuration.configInputformatProperty);

		String reportInputProvisioningDirectoryPath = rootPath + Configuration.configReportinputProperty;
		provisioningCommandsByWatchedPath.put(reportInputProvisioningDirectoryPath, reportInputProvisioningCommand);
		subpathList.add(Configuration.configReportinputProperty);

		String indicatorProvisioningFilePath = rootPath + Configuration.configIndicatorProperty + File.separatorChar
				+ FILE_NAME_INDICATOR;
		provisioningCommandsByWatchedPath.put(indicatorProvisioningFilePath, indicatorProvisioningCommand);
		subpathList.add(Configuration.configIndicatorProperty + File.separatorChar + FILE_NAME_INDICATOR);

		String reportProvisioningDirectoryPath = rootPath + Configuration.configReportProperty;
		provisioningCommandsByWatchedPath.put(reportProvisioningDirectoryPath, reportProvisioningCommand);
		subpathList.add(Configuration.configReportProperty);

		String typeSubtypeFilePath = rootPath + Configuration.configTypesubtypeProperty + File.separatorChar
				+ FILE_NAME_TYPE_SUBTYPE;
		provisioningCommandsByWatchedPath.put(typeSubtypeFilePath, typeSubtypeProvisioningCommand);
		subpathList.add(Configuration.configTypesubtypeProperty + File.separatorChar + FILE_NAME_TYPE_SUBTYPE);

		String paramTypeProvisioningFilePath = rootPath + Configuration.configParamtypeProperty + File.separatorChar
				+ FILE_NAME_PARAM_TYPE;
		provisioningCommandsByWatchedPath.put(paramTypeProvisioningFilePath, paramTypeProvisioningCommand);
		subpathList.add(Configuration.configParamtypeProperty + File.separatorChar + FILE_NAME_PARAM_TYPE);

		String reportConfigProvisioningDirectoryPath = rootPath + Configuration.configReportconfigProperty;
		provisioningCommandsByWatchedPath.put(reportConfigProvisioningDirectoryPath, reportConfigProvisioningCommand);
		subpathList.add(Configuration.configReportconfigProperty);

		String sourceClassProvisioningFilePath = SourceClassProvisioningCommand.getSourceClassProvisioningFilePath();
		provisioningCommandsByWatchedPath.put(sourceClassProvisioningFilePath, sourceClassProvisioningCommand);
		subpathList.add(Configuration.configSourceclassProperty + File.separatorChar + FILE_NAME_SOURCE_CLASS);

		String proxyProvisioningFilePath = ProxyProvisioningCommand.getProxyProvisioningFilePath();
		provisioningCommandsByWatchedPath.put(proxyProvisioningFilePath, proxyProvisioningCommand);
		subpathList.add(Configuration.configProxyProperty + File.separatorChar + FILE_NAME_PROXY);

		String inputSourceProvisioningDirectoryPath = InputSourceProvisioningCommand
				.getInputSourceProvisioningDirectoryPath();
		provisioningCommandsByWatchedPath.put(inputSourceProvisioningDirectoryPath, inputSourceProvisioningCommand);
		subpathList.add(Configuration.configInputsourceProperty);

		String sourceProxyProvisioningFilePath = SourceProxyProvisioningCommand.getSourceProxyProvisioningFilePath();
		provisioningCommandsByWatchedPath.put(sourceProxyProvisioningFilePath, sourceProxyProvisioningCommand);
		subpathList.add(Configuration.configSourceproxyProperty + File.separatorChar + FILE_NAME_SOURCE_PROXY);

		String hyperlinkFilePath = rootPath + Configuration.configHyperlinkProperty + File.separatorChar
				+ FILE_NAME_HYPERLINK;
		provisioningCommandsByWatchedPath.put(hyperlinkFilePath, hyperlinkProvisioningCommand);
		subpathList.add(Configuration.configHyperlinkProperty + File.separatorChar + FILE_NAME_HYPERLINK);
	}

	@Override
	public void operateTestCase(UriInfo context, ConfigTestParameters configTestParameters) throws TestException {
		SOAContext soaContext = SOATools.buildSOAContext(null);

		// Execute each command in the map
		for (String path : provisioningCommandsByWatchedPath.keySet()) {
			File provisioningFilePath = new File(path);
			if (provisioningFilePath.exists()) {
				AbstractProvisioningCommand command = provisioningCommandsByWatchedPath.get(path);

				try {
					if (provisioningFilePath.isDirectory()) {
						// Execute the command for all the files in the directory
						for (File provisioningFile : provisioningFilePath.listFiles(new XmlFileOnlyFilter())) {
							command.executeAndRollbackIfNecessary(soaContext, provisioningFile, false);
						}

					} else {
						// Execute command for a single file
						command.executeAndRollbackIfNecessary(soaContext, provisioningFilePath, false);
					}
				} catch (Exception ex) {
					try {
						writeExceptionInAnErrorTimestampFile(provisioningFilePath, ex);
					} catch (IOException IOEx) {
						IOEx.printStackTrace();
					}
				}
			}
		}

	}

	@Override
	public boolean checkResult(UriInfo context, ConfigTestParameters configTestParameters) throws TestException {
		// Dump current database
		MySQLDumpDBParameterFromConfiguration backupParameter = RefReportTestConfiguration
				.getMySQLDumpBDParameterFromConfiguration("ResultTestDBPath");
		backupMySQLDataBaseWithDump(backupParameter);

		// Get generated SQL File
		generatedDB = new File(confRoot + File.separatorChar + "dump" + File.separatorChar + "RESULT_TEST_TMP.sql");

		// 1st step : compare both databases
		tableResults = new ArrayList<TableResultTO>();
		boolean result = compareDatabases(context, configTestParameters);

		// 2nd step : compare generated configuration files
		fileResults = new ArrayList<FileResultTO>();

		String generatedRootPath = Configuration.rootProperty + File.separatorChar;

		String sourceRootPath = null;
		if (testName.equals("YellowProvisioningTest")) {
			sourceRootPath = confRoot + File.separatorChar + "source" + File.separatorChar;
		} else {
			sourceRootPath = configTestParameters.getParametersPath();
		}
		int numberOfDifferences = 0;

		for (String subpath : subpathList) {

			File generatedPath = new File(generatedRootPath + subpath);
			File sourcePath = new File(sourceRootPath + subpath);
			if (generatedPath.exists() && sourcePath.exists()) {
				// If path is directory
				if (sourcePath.isDirectory()) {
					// Execute the comparison for all the files in the directory
					File[] generatedFiles = sourcePath.listFiles(new XmlFileOnlyFilter());
					File[] sourceFiles = generatedPath.listFiles(new XmlFileOnlyFilter());
					for (int i = 0; i < generatedFiles.length; i++) {
						FileResultTO fileResult = new FileResultTO();
						fileResult.filename = generatedFiles[i].getName();
						int nbDiff = compareFiles(generatedFiles[i], sourceFiles[i]);
						fileResult.numberOfDifferences = nbDiff;
						numberOfDifferences += nbDiff;
						fileResults.add(fileResult);
					}
				} else {
					// Execute comparison for a single file
					FileResultTO fileResult = new FileResultTO();
					fileResult.filename = sourcePath.getName();
					int nbDiff = compareFiles(generatedPath, sourcePath);
					fileResult.numberOfDifferences = nbDiff;
					numberOfDifferences += nbDiff;
					fileResults.add(fileResult);
				}
			}
		}
		if (result == true) {
			if (numberOfDifferences == 0) {
				testMessage = "Test OK - The comparison of the files and the databases has been successful";
				return true;
			} else {
				testMessage = "Test ERROR - The databases are OK but the generated provisioning files are different";
				return false;
			}
		} else {
			if (numberOfDifferences == 0) {
				testMessage = "Test ERROR - The generated provisioning files are OK but the databases are different";
				return false;
			} else {
				testMessage = "Test ERROR - Both the files and the databases are different";
				return false;
			}
		}
	}

	private int compareFiles(File generatedFile, File sourceFile) throws TestException {
		int numberOfDifferences = 0;

		// Create readers
		BufferedReader generatedReportReader = null;
		BufferedReader sourceReportReader = null;

		try {
			generatedReportReader = new BufferedReader(new FileReader(generatedFile));
			sourceReportReader = new BufferedReader(new FileReader(sourceFile));

			// Comparison
			String CurrentLineOfGeneratedReport = generatedReportReader.readLine();
			String CurrentLineOfSourceReport = sourceReportReader.readLine();

			// We go through each line of both reports until we reach the end of the file or
			// until we find a difference
			while (CurrentLineOfGeneratedReport != null && CurrentLineOfSourceReport != null) {

				if (!CurrentLineOfGeneratedReport.equals(CurrentLineOfSourceReport)) {
					numberOfDifferences++;
				}

				CurrentLineOfGeneratedReport = generatedReportReader.readLine();
				CurrentLineOfSourceReport = sourceReportReader.readLine();
			}

			// When we reach the end of one file, we look if we have reached the end of the
			// other file
			while (CurrentLineOfGeneratedReport != null) {
				numberOfDifferences++;
			}
			while (CurrentLineOfSourceReport != null) {
				numberOfDifferences++;
			}

		} catch (FileNotFoundException fileNotFoundEx) {
			throw new TestException(getTestName(), "File nout found : " + fileNotFoundEx.getMessage(), fileNotFoundEx);
		} catch (IOException ioEx) {
			throw new TestException(getTestName(), "Error while comparing both reports : " + ioEx.getMessage(), ioEx);
		} finally {
			// Close the readers
			try {
				if (generatedReportReader != null) {
					generatedReportReader.close();
				}
				if (sourceReportReader != null) {
					sourceReportReader.close();
				}
			} catch (IOException ioex) {
				throw new TestException(getTestName(), "Error while closing the reports : " + ioex);
			}
		}

		return numberOfDifferences;
	}

	@Override
	public boolean regenerateReferenceFile(UriInfo context, ConfigTestParameters configTestParameters)
			throws TestException {

		/** Create, Copy and Change the main SQL file **/
		// Generate the SQL File
		MySQLDumpDBParameterFromConfiguration backupParameter = RefReportTestConfiguration
				.getMySQLDumpBDParameterFromConfiguration("ResultTestDBPath");
		backupMySQLDataBaseWithDump(backupParameter);

		// Get both SQL files
		generatedDB = new File(confRoot + File.separatorChar + "dump" + File.separatorChar + "RESULT_TEST_TMP.sql");
		File referenceDB = new File(configTestParameters.getReferenceFilePath());

		// Delete the old reference file
		referenceDB.delete();

		// Initiate parameters
		BufferedReader generatedDBReader = null;
		BufferedWriter referenceDBWriter = null;

		try {

			// Create readers
			generatedDBReader = new BufferedReader(new FileReader(generatedDB));
			referenceDBWriter = new BufferedWriter(new FileWriter(referenceDB));

			String currentLineOfGeneratedDB = generatedDBReader.readLine();

			while (currentLineOfGeneratedDB != null && !currentLineOfGeneratedDB.contains("CREATE DATABASE")) {
				referenceDBWriter.write(currentLineOfGeneratedDB + "\n");
				currentLineOfGeneratedDB = generatedDBReader.readLine();
			}

			// 1st modification
			String currentModifiedLine = currentLineOfGeneratedDB.replace("refreport", "refreporttarget");
			referenceDBWriter.write(currentModifiedLine + "\n");
			currentLineOfGeneratedDB = generatedDBReader.readLine();

			while (currentLineOfGeneratedDB != null && !currentLineOfGeneratedDB.contains("USE RefReport")) {
				referenceDBWriter.write(currentLineOfGeneratedDB + "\n");
				currentLineOfGeneratedDB = generatedDBReader.readLine();
			}

			// 2nd modification
			currentModifiedLine = currentLineOfGeneratedDB.replace("RefReport", "RefReportTarget");
			referenceDBWriter.write(currentModifiedLine + "\n");
			currentLineOfGeneratedDB = generatedDBReader.readLine();

			while (currentLineOfGeneratedDB != null) {
				referenceDBWriter.write(currentLineOfGeneratedDB + "\n");
				currentLineOfGeneratedDB = generatedDBReader.readLine();
			}

			/**
			 * Regenerate configuration files when performing UpdateYellowProvisioningTest
			 **/
			if (testName.equals("UpdateYellowProvisioningTest")) {
				// Clean the reference file directory
				File referenceDirectory = new File(configTestParameters.getParametersPath() + File.separatorChar
						+ Configuration.configProvisioningProperty + File.separatorChar);
				FileUtils.cleanDirectory(referenceDirectory);

				// Copy generated files into the reference directory
				File generatedDirectory = new File(Configuration.rootProperty + File.separatorChar
						+ Configuration.configProvisioningProperty + File.separatorChar);
				FileUtils.copyDirectory(generatedDirectory, referenceDirectory);
			}
		} catch (FileNotFoundException fileNotFoundEx) {
			throw new TestException(getTestName(), "File nout found : " + fileNotFoundEx.getMessage(), fileNotFoundEx);
		} catch (IOException ioEx) {
			throw new TestException(getTestName(), "Error while comparing both reports : " + ioEx.getMessage(), ioEx);
		} finally {

			// Close the reader and the writer
			try {
				if (generatedDBReader != null) {
					generatedDBReader.close();
				}
				if (referenceDBWriter != null) {
					referenceDBWriter.close();
				}
			} catch (IOException ioex) {
				throw new TestException(getTestName(), "Error while closing the reports : " + ioex);
			}
		}

		testMessage = "Regeneration OK";
		return true;
	}

	@Override
	public void clearDataAfterTest(UriInfo context) throws TestException {

		// Copy dumped DB and delete it
		if (generatedDB != null) {
			MySQLDumpDBParameterFromConfiguration regenParameter = RefReportTestConfiguration
					.getMySQLDumpBDParameterFromConfiguration("ProvisioningTestDBPath");
			try {
				FileUtils.copyFile(generatedDB,
						new File(getDumpFilePath(regenParameter.dumpfilePropertyInConfiguration)));
			} catch (IOException e) {
				throw new TestException(getTestName(), "Error while replacing ProvisioningTest.sql");
			}
			generatedDB.delete();
		}

		// Restore the initial database with the created backup
		MySQLRestoreDBParameterFromConfiguration restoreParameter = RefReportTestConfiguration
				.getMySQLRestoreBDParameterFromConfiguration("BackupMySqlDBPath");
		restoreMySQLDataBaseWithDump(restoreParameter, "refreport", true);

		// Restore some parameters to their default values
		Configuration.rootProperty = initialRoot;
		Configuration.mountConfiguration.put("ROOT", initialRoot);
		initialRoot = null;
		testName = null;
	}

	private void writeExceptionInAnErrorTimestampFile(File provisioningFile, Exception be) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_kk-mm-ss", Locale.FRANCE);
		String fileNameError = FilenameUtils.removeExtension(provisioningFile.getAbsolutePath()) + "_"
				+ dateFormat.format(new Date()) + FILE_ERROR_EXTENSION;
		File errorFile = new File(fileNameError);
		errorFile.createNewFile();
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(errorFile);
			be.printStackTrace(printWriter);
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
		}
	}

	@Override
	public String getGiven() {
		return "a specific database (empty or filled with entities according to the test)";
	}

	@Override
	public String getWhen() {
		return "receiving the \"yellow\" part provisioning command";
	}

	@Override
	public String getThen() {
		return "update the database accordingly with the new data";
	}

	@Override
	public String getSOAService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUseCaseReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTestMessage() {
		return testMessage;
	}

	@Override
	public ArrayList<TableResultTO> getTableResults() {
		return tableResults;
	}

	@Override
	public ArrayList<FileResultTO> getFileResults() {
		return fileResults;
	}

}

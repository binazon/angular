package com.orange.srs.refreport.test.provisioning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.orange.srs.common.test.model.ConfigTestParameters;
import com.orange.srs.common.test.parameter.MockObjectMessage;
import com.orange.srs.common.test.parameter.MySQLDumpDBParameterFromConfiguration;
import com.orange.srs.common.test.parameter.MySQLRestoreDBParameterFromConfiguration;
import com.orange.srs.common.test.technical.TestCaseWithConfigurationCommand;
import com.orange.srs.common.test.technical.TestException;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.test.RefReportTestConfiguration;
import com.orange.srs.refreport.test.wrapper.ProvisioningConsumerEJBWrapper;
import com.orange.srs.statcommon.model.parameter.jobparameter.ProvisioningJobParameter;
import com.orange.srs.statcommon.technical.xml.JAXBFactory;

public abstract class ProvisioningTest extends TestCaseWithConfigurationCommand {

	@Inject
	private ProvisioningConsumerEJBWrapper provisioningConsumer;

	protected String initialRoot;
	protected String confRoot;
	protected String testName;
	protected String testMessage;
	protected File generatedDB;

	@Override
	public String getTestName() {
		if (testName != null) {
			return "ProvisioningTest - " + testName;
		} else {
			return "ProvisioningTest";
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
		confRoot = Configuration.acceptancetestFolderProperty;

		// Backup of the database
		MySQLDumpDBParameterFromConfiguration backupParameter = RefReportTestConfiguration
				.getMySQLDumpBDParameterFromConfiguration("BackupMySqlDBPath");
		backupMySQLDataBaseWithDump(backupParameter);

		// Restore the database of test
		MySQLRestoreDBParameterFromConfiguration restoreParameter = RefReportTestConfiguration
				.getMySQLRestoreBDParameterFromConfiguration("ProvisioningTestDBPath");
		restoreMySQLDataBaseWithDump(restoreParameter, "refreport", true);
	}

	@Override
	public void operateTestCase(UriInfo context, ConfigTestParameters configTestParameters) throws TestException {

		try {

			// Unmarshalling of the file with the ProvisioningJobParameters
			Unmarshaller unmarshaller;
			unmarshaller = JAXBFactory.getUnmarshaller();
			File fileWithTestParameters = new File(configTestParameters.getParametersPath());
			ProvisioningJobParameter provisioningJobParameter = (ProvisioningJobParameter) unmarshaller
					.unmarshal(fileWithTestParameters);

			// Send the message through JMS
			MockObjectMessage message = new MockObjectMessage(testName);
			message.setObject(provisioningJobParameter);
			provisioningConsumer.onMessage(message);

		} catch (JAXBException jaxbEx) {
			throw new TestException(getTestName(),
					"Error while unmarshalling the test parameters: " + jaxbEx.getMessage(), jaxbEx);

		} catch (JMSException jmsEx) {
			throw new TestException(getTestName(), "Error while creating JMS message: " + jmsEx.getMessage(), jmsEx);
		}

		// Dump current database
		MySQLDumpDBParameterFromConfiguration backupParameter = RefReportTestConfiguration
				.getMySQLDumpBDParameterFromConfiguration("ResultTestDBPath");
		backupMySQLDataBaseWithDump(backupParameter);

		// Get generated SQL File
		generatedDB = new File(confRoot + File.separatorChar + "dump" + File.separatorChar + "RESULT_TEST_TMP.sql");

	}

	@Override
	public boolean regenerateReferenceFile(UriInfo context, ConfigTestParameters configTestParameters)
			throws TestException {

		// Get reference SQL file
		File referenceDB = new File(configTestParameters.getReferenceFilePath());

		// Delete the old reference file
		referenceDB.delete();

		// Initiate parameters
		BufferedReader generatedDBReader = null;
		BufferedWriter referenceDBWriter = null;

		try {
			// Create readers
			generatedDBReader = new BufferedReader(new FileReader(generatedDB));
			GZIPOutputStream gzipStream = new GZIPOutputStream(
					new FileOutputStream(configTestParameters.getReferenceFilePath() + ".gz"));
			referenceDBWriter = new BufferedWriter(new OutputStreamWriter(gzipStream));

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

		// Compress DB, copy it and delete it
		if (generatedDB != null) {
			MySQLDumpDBParameterFromConfiguration regenParameter = RefReportTestConfiguration
					.getMySQLDumpBDParameterFromConfiguration("ExportInventoryTestDBPath");
			compressGzipFile(generatedDB.getAbsolutePath(),
					getDumpFilePath(regenParameter.dumpfilePropertyInConfiguration));
			generatedDB.delete();
		}

		// Restore the initial database with the created backup
		MySQLRestoreDBParameterFromConfiguration parameter = RefReportTestConfiguration
				.getMySQLRestoreBDParameterFromConfiguration("BackupMySqlDBPath");
		restoreMySQLDataBaseWithDump(parameter, "refreport", true);

		// Restore some parameters to their default values
		Configuration.rootProperty = initialRoot;
		Configuration.mountConfiguration.put("ROOT", initialRoot);
		initialRoot = null;
		testName = null;

	}

	private static void compressGzipFile(String file, String gzipFile) {
		try {
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(gzipFile + ".gz");
			GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) != -1) {
				gzipOS.write(buffer, 0, len);
			}
			// close resources
			gzipOS.close();
			fos.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getTestMessage() {
		return testMessage;
	}

	@Override
	public String getGiven() {
		return "a filled database with entities";
	}

	@Override
	public String getWhen() {
		return "receiving the provisioning command";
	}

	@Override
	public String getThen() {
		return "update the database accordingly with the corresponding data";
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

}

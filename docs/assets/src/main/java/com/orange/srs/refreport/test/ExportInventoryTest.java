package com.orange.srs.refreport.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;

import com.orange.srs.common.test.model.ConfigTestParameters;
import com.orange.srs.common.test.parameter.ComparisonH2DBResult;
import com.orange.srs.common.test.parameter.MockObjectMessage;
import com.orange.srs.common.test.parameter.MySQLDumpDBParameterFromConfiguration;
import com.orange.srs.common.test.parameter.MySQLRestoreDBParameterFromConfiguration;
import com.orange.srs.common.test.technical.ComparisonH2DBUtils;
import com.orange.srs.common.test.technical.FileResultTO;
import com.orange.srs.common.test.technical.TableResultTO;
import com.orange.srs.common.test.technical.TestCaseWithConfigurationCommand;
import com.orange.srs.common.test.technical.TestException;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.test.RefReportTestConfiguration;
import com.orange.srs.refreport.test.wrapper.ExportInventoryConsumerEJBWrapper;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.jobparameter.ExportInventoryJobParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.jdbc.CreateH2Connection;
import com.orange.srs.statcommon.technical.xml.JAXBFactory;

@Stateless
public class ExportInventoryTest extends TestCaseWithConfigurationCommand {

	@Inject
	private ExportInventoryConsumerEJBWrapper exportInventoryConsumer;

	protected String initialRoot;
	protected String testName;
	protected File generatedDB;
	protected ExportInventoryJobParameter exportInventoryJobParameter;
	protected Connection generatedDBConnection;
	protected Connection referenceDBConnection;
	private String testMessage;
	private ArrayList<TableResultTO> tableResults;

	@Override
	public String getTestName() {
		if (testName != null) {
			return "ExportInventoryTest - " + testName;
		} else {
			return "ExportInventoryTest";
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

		// Backup of the database
		MySQLDumpDBParameterFromConfiguration backupParameter = RefReportTestConfiguration
				.getMySQLDumpBDParameterFromConfiguration("BackupMySqlDBPath");
		backupMySQLDataBaseWithDump(backupParameter);

		// Decompress the DB of test and restore it
		MySQLRestoreDBParameterFromConfiguration restoreParameter = RefReportTestConfiguration
				.getMySQLRestoreBDParameterFromConfiguration("ExportInventoryTestDBPath");
		String dumpFilePath = getDumpFilePath(restoreParameter.dumpfilePropertyInConfiguration);
		decompressGzipFile(dumpFilePath, dumpFilePath);
		restoreMySQLDataBaseWithDump(restoreParameter, "refreport", true);
		File decompressedDB = new File(dumpFilePath);
		decompressedDB.delete();
	}

	@Override
	public void operateTestCase(UriInfo context, ConfigTestParameters configTestParameters) throws TestException {
		try {

			// Unmarshalling of the file with the ExportInventoryJobParameter
			Unmarshaller unmarshaller;
			unmarshaller = JAXBFactory.getUnmarshaller();
			File fileWithTestParameters = new File(configTestParameters.getParametersPath() + "\\export.xml");
			exportInventoryJobParameter = (ExportInventoryJobParameter) unmarshaller.unmarshal(fileWithTestParameters);
			exportInventoryJobParameter.responseCanal = "canal3";

			// Send the message through JMS
			MockObjectMessage message = new MockObjectMessage(testName);
			message.setObject(exportInventoryJobParameter);
			exportInventoryConsumer.onMessage(message);

		} catch (JAXBException jaxbEx) {
			throw new TestException(getTestName(),
					"Error while unmarshalling the test parameters: " + jaxbEx.getMessage(), jaxbEx);

		} catch (JMSException jmsEx) {
			throw new TestException(getTestName(), "Error while creating JMS message: " + jmsEx.getMessage(), jmsEx);
		}

	}

	@Override
	public boolean checkResult(UriInfo context, ConfigTestParameters configTestParameters) throws TestException {
		// Compare the H2 generated database with the H2 database of reference
		int numberOfDifferences = 0;
		int numberOfMissingObjectsInGeneratedDB = 0;
		int numberOfAdditonalObjectsInGeneratedDB = 0;

		try {
			// Initiate result parameters
			testMessage = "";
			tableResults = new ArrayList<TableResultTO>();

			// Construct the reportContext
			SOAContext soaContext = SOATools.buildSOAContext(exportInventoryJobParameter);

			// Generate the resultSets
			int month = exportInventoryJobParameter.exportDate.get(Calendar.MONTH) + 1;
			int year = exportInventoryJobParameter.exportDate.get(Calendar.YEAR) % 100;
			int day = exportInventoryJobParameter.exportDate.get(Calendar.DAY_OF_MONTH);
			String folderPath = year + (month < 10 ? "0" + month : "" + month);
			String date = folderPath + (day < 10 ? "0" + day : "" + day);

			String generatedDBPath = Configuration.rootProperty + "\\data\\groups\\"
					+ exportInventoryJobParameter.reportingGroupRefId + "_"
					+ exportInventoryJobParameter.origin.toLowerCase() + "\\inventory\\" + folderPath + "\\inventory_"
					+ date + ".h2.db";
			generatedDB = new File(generatedDBPath);
			File referenceDB = new File(configTestParameters.getReferenceFilePath());

			if (referenceDB.exists() && generatedDB.exists()) {
				// Creation of the connections to the two databases
				generatedDBConnection = CreateH2Connection
						.buildConnectionIfExists(Configuration.h2MemoryOptionsProperty, true);
				HashMap<String, ResultSet> generatedDBMap = ComparisonH2DBUtils.selectFromH2Database(getTestName(),
						generatedDB.getAbsolutePath(), "_GEN", soaContext, generatedDBConnection);

				referenceDBConnection = CreateH2Connection
						.buildConnectionIfExists(Configuration.h2MemoryOptionsProperty, true);
				HashMap<String, ResultSet> referenceDBMap = ComparisonH2DBUtils.selectFromH2Database(getTestName(),
						referenceDB.getAbsolutePath(), "_REF", soaContext, referenceDBConnection);

				// Compare both H2 databases by going through each resultSet
				for (String tableName : referenceDBMap.keySet()) {
					if (generatedDBMap.containsKey(tableName)) {
						// Compare two tables
						ComparisonH2DBResult comparisonH2DBResult = ComparisonH2DBUtils.compare2ResultSets(tableName,
								referenceDBMap.get(tableName), generatedDBMap.get(tableName));

						// Add the result of the comparison to the tableResult
						tableResults.add(comparisonH2DBResult.getTableResult());

						// Add the number of differences for this comparison to the main number
						numberOfDifferences += comparisonH2DBResult.getNumberOfDifferences();
						numberOfAdditonalObjectsInGeneratedDB += comparisonH2DBResult
								.getNumberOfAdditonalObjectsInGeneratedDB();
						numberOfMissingObjectsInGeneratedDB += comparisonH2DBResult
								.getNumberOfMissingObjectsInGeneratedDB();
					}
				}

			} else {
				throw new TestException(getTestName(), "One of the databases is missing");
			}
		} catch (JAXBException e) {
			throw new TestException(getTestName(), e.toString());
		} catch (TestException testEx) {
			throw testEx;
		} catch (Exception e) {
			throw new TestException(getTestName(), e.toString());
		} finally {
			try {

				if (generatedDBConnection != null) {
					generatedDBConnection.close();
				}

				if (referenceDBConnection != null) {
					referenceDBConnection.close();
				}
			} catch (SQLException e) {
				throw new TestException(getTestName(), "Can't close connections to the two databases");
			}
		}

		if (numberOfDifferences == 0) {
			testMessage += "Test OK - The test has been completed successfully\n";
			return true;
		} else {
			testMessage += "Test ERROR - " + numberOfDifferences + " differences were found during the test\n";
			if (numberOfMissingObjectsInGeneratedDB != 0) {
				testMessage += " - Number of missing objects in generated DB : " + numberOfMissingObjectsInGeneratedDB
						+ " \n";
			}
			if (numberOfAdditonalObjectsInGeneratedDB != 0) {
				testMessage += " - Number of additional objects that are not part of the reference DB : "
						+ numberOfAdditonalObjectsInGeneratedDB + "\n";
			}
			return false;
		}

	}

	@Override
	public boolean regenerateReferenceFile(UriInfo context, ConfigTestParameters configTestParameters)
			throws TestException {

		// Get both H2DB files
		int month = exportInventoryJobParameter.exportDate.get(Calendar.MONTH) + 1;
		int year = exportInventoryJobParameter.exportDate.get(Calendar.YEAR) % 100;
		int day = exportInventoryJobParameter.exportDate.get(Calendar.DAY_OF_MONTH);
		String folderPath = year + (month < 10 ? "0" + month : "" + month);
		String date = folderPath + (day < 10 ? "0" + day : "" + day);
		String generatedDBPath = Configuration.rootProperty + "\\data\\groups\\"
				+ exportInventoryJobParameter.reportingGroupRefId + "_"
				+ exportInventoryJobParameter.origin.toLowerCase() + "\\inventory\\" + folderPath + "\\inventory_"
				+ date + ".h2.db";

		generatedDB = new File(generatedDBPath);
		File referenceDB = new File(configTestParameters.getReferenceFilePath());

		// Overwrite old reference
		try {
			FileUtils.copyFile(generatedDB, referenceDB);
		} catch (IOException ioEx) {
			throw new TestException(getTestName(), ioEx.getMessage());
		}

		testMessage = "Regeneration OK";
		return true;
	}

	@Override
	public void clearDataAfterTest(UriInfo context) throws TestException {
		// Delete generated database
		if (generatedDB != null) {
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

	private static void decompressGzipFile(String gzipFile, String newFile) {
		try {
			FileInputStream fis = new FileInputStream(gzipFile + ".gz");
			GZIPInputStream gis = new GZIPInputStream(fis);
			FileOutputStream fos = new FileOutputStream(newFile);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			// close resources
			fos.close();
			gis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getGiven() {
		return "a specific set of data";
	}

	@Override
	public String getWhen() {
		return "receiving exportInventoryJobParameters";
	}

	@Override
	public String getThen() {
		return "perform the export corresponding to the export parameters";
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
		return null;
	}

}

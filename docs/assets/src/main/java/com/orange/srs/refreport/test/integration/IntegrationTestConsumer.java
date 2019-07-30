package com.orange.srs.refreport.test.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ejb.EJB;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.orange.srs.common.test.technical.DBTestUtils;
import com.orange.srs.common.test.technical.MySQLConfigurationTO;
import com.orange.srs.common.test.technical.TestException;
import com.orange.srs.refreport.technical.JMSConnectionHandler;
import com.orange.srs.refreport.test.MySQLFileComparisonUtils;
import com.orange.srs.statcommon.model.TO.JobTO.JobMessageTO;
import com.orange.srs.statcommon.model.enums.JobEventCriticityEnum;
import com.orange.srs.statcommon.model.enums.JobEventTypeEnum;
import com.orange.srs.statcommon.model.enums.JobStateEnum;
import com.orange.srs.statcommon.model.enums.TaskTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.jobparameter.JobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.TestComparisonJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.TestRestorationJobParameter;
import com.orange.srs.statcommon.model.parameter.jobparameter.TestSetupJobParameter;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

public class IntegrationTestConsumer implements MessageListener {

	private static final Logger LOGGER = Logger.getLogger(IntegrationTestConsumer.class);

	private final static String REFREPORT_DATABASE_NAME = "RefReport";
	private final static String RESULT_DATABASE_NAME = "ContinuousIntegration";
	private final static String TEST_RESULT_TABLE = "t_provisioning_test_results";

	@EJB
	private JMSConnectionHandler jmsConnectionHandler;

	private String getTestName() {
		return "IntegrationTest - RefReport";
	}

	/**
	 * Default constructor.
	 */
	public IntegrationTestConsumer() {
	}

	/**
	 * @see MessageListener#onMessage(Message)
	 */
	@Override
	public void onMessage(Message message) {

		ObjectMessage omessage = (ObjectMessage) message;
		LOGGER.info("Message recu " + omessage.toString());

		try {
			JobParameter parameter = (JobParameter) omessage.getObject();

			// build SOA Context for parameter
			SOAContext soaContext = SOATools.buildSOAContext(parameter);

			LOGGER.info("Received Task " + parameter.taskType.toString());
			LOGGER.info(SOATools.buildSOAStartLogMessage(soaContext, "onMessage", parameter.taskType.toString()));
			if (parameter.taskType == TaskTypeEnum.TEST_SETUP) {
				TestSetupJobParameter testSetupJobParameter = (TestSetupJobParameter) parameter;
				doSetup(testSetupJobParameter);
			} else if (parameter.taskType == TaskTypeEnum.TEST_COMPARISON) {
				TestComparisonJobParameter testComparisonJobParameter = (TestComparisonJobParameter) parameter;
				doComparison(testComparisonJobParameter);
			} else if (parameter.taskType == TaskTypeEnum.TEST_RESTORE) {
				TestRestorationJobParameter testRestorationJobParameter = (TestRestorationJobParameter) parameter;
				doRestoration(testRestorationJobParameter);
			}

			LOGGER.info(SOATools.buildSOAEndLogMessage(soaContext, "onMessage", parameter.taskType.toString()));
		} catch (Exception ex) {
			LOGGER.error("Error occurs when receiving Task " + ex.getMessage());
		}

	}

	private void doSetup(TestSetupJobParameter parameter) {

		System.out.println("[IntegrationTestConsumer] doSetup Start");

		JobMessageTO jobMessageTO = new JobMessageTO();
		jobMessageTO.buildMessageTO(parameter);
		Long start = Utils.getTime();

		MySQLConfigurationTO configuration = parameter.mysqlConfiguration;

		// Backup of the current RefReport database
		File backupFile = new File(parameter.backupDatabasePath);
		try {
			DBTestUtils.backupMySQLDatabase(configuration.mysqlDumpExe, configuration.user, configuration.password,
					REFREPORT_DATABASE_NAME, backupFile);
		} catch (Exception e) {
			String errorMsg = "[IntegrationTestConsumer] Problem while creating the backup dump " + e.getMessage();
			jobMessageTO.jobSummaryTO.stateDetails = errorMsg;
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.MISCELLANEOUS, JobEventCriticityEnum.ERROR, errorMsg);
			LOGGER.error(errorMsg, e);
			jobMessageTO.jobSummaryTO.state = JobStateEnum.ERROR;
		}

		// Import of the test database
		File testFile = new File(parameter.testDatabasePath);
		try {
			DBTestUtils.restoreMySQLDatabase(configuration.mysqlExe, configuration.user, configuration.password,
					REFREPORT_DATABASE_NAME, testFile, true);
			jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
			jobMessageTO.jobSummaryTO.jobOutput = testFile.getAbsolutePath();
		} catch (Exception e) {
			String errorMsg = "[IntegrationTestConsumer] Problem while restoring the backup dump " + e.getMessage();
			jobMessageTO.jobSummaryTO.stateDetails = errorMsg;
			jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.MISCELLANEOUS, JobEventCriticityEnum.ERROR, errorMsg);
			LOGGER.error(errorMsg, e);
			jobMessageTO.jobSummaryTO.state = JobStateEnum.ERROR;
		}

		jobMessageTO.setJobDuration(start);
		jmsConnectionHandler.sendJMSMessage(jobMessageTO, parameter.responseCanal);
		LOGGER.info("[IntegrationTestConsumer] Setup complete");
		System.out.println("[IntegrationTestConsumer] doSetup End");
	}

	private void doComparison(TestComparisonJobParameter parameter) throws TestException {

		System.out.println("[IntegrationTestConsumer] doComparison Start");
		JobMessageTO jobMessageTO = new JobMessageTO();
		jobMessageTO.buildMessageTO(parameter);
		Long start = Utils.getTime();

		MySQLConfigurationTO configuration = parameter.mysqlConfiguration;
		File referenceDB = new File(parameter.referenceDatabasePath);

		if (referenceDB.exists()) {

			// Generation of the temporary DB
			File generatedDB = new File(parameter.temporaryDumpPath);
			try {
				DBTestUtils.backupMySQLDatabase(configuration.mysqlDumpExe, configuration.user, configuration.password,
						REFREPORT_DATABASE_NAME, generatedDB);
			} catch (Exception e) {
				LOGGER.error("[IntegrationTestConsumer] Problem while generating the temporary dump " + e.getMessage());
			}

			// Comparison
			int numberOfDifferences = 0;
			List<String> listOfTablesWithDifferences = new ArrayList<String>();

			// Initiate parameters
			BufferedReader generatedDBReader = null;
			BufferedReader referenceDBReader = null;

			try {
				// Create readers
				generatedDBReader = new BufferedReader(new FileReader(generatedDB));
				referenceDBReader = new BufferedReader(new FileReader(referenceDB));

				// Get the names of the tables of both databases
				ArrayList<String> tablesOfGeneratedDB = MySQLFileComparisonUtils.getTableNames(generatedDBReader);
				ArrayList<String> tablesOfReferenceDB = MySQLFileComparisonUtils.getTableNames(referenceDBReader);
				ArrayList<String> commonTables = new ArrayList<String>();

				for (String table : tablesOfGeneratedDB) {
					if (tablesOfReferenceDB.contains(table)) {
						commonTables.add(table);
					}
				}

				// Reset the readers by closing the current ones and creating new ones
				if (generatedDBReader != null) {
					generatedDBReader.close();
				}
				if (referenceDBReader != null) {
					referenceDBReader.close();
				}

				generatedDBReader = new BufferedReader(new FileReader(generatedDB));
				referenceDBReader = new BufferedReader(new FileReader(referenceDB));

				// Now that we know the tables that are common to both databases
				// We can go through each file and do a more thorough comparison
				// Step 1 : Check the format of the tables with the same name
				// Step 2 : If the format is identical, check the values
				// The program reports any differences that are found during the comparison

				String currentLineOfGeneratedFile = "";
				String currentLineOfReferenceFile = "";
				String currentTableName = "";

				while (!commonTables.isEmpty()) {
					// Read next line
					currentLineOfGeneratedFile = generatedDBReader.readLine();
					currentLineOfReferenceFile = referenceDBReader.readLine();

					// Position the readers of both files to the next common tables
					boolean readerPositioned = false;
					while (!readerPositioned) {
						while (!currentLineOfGeneratedFile.startsWith("CREATE TABLE")) {
							currentLineOfGeneratedFile = generatedDBReader.readLine();
						}
						String[] tmpTab = currentLineOfGeneratedFile.split(" ");
						if (commonTables.contains(tmpTab[2])) {
							currentTableName = tmpTab[2];
							readerPositioned = true;
						} else {
							currentLineOfGeneratedFile = generatedDBReader.readLine();
							numberOfDifferences++;
							listOfTablesWithDifferences.add(currentTableName);
						}
					}

					readerPositioned = false;
					while (!readerPositioned) {
						while (!currentLineOfReferenceFile.startsWith("CREATE TABLE")) {
							currentLineOfReferenceFile = referenceDBReader.readLine();
						}
						String[] tmpTab = currentLineOfReferenceFile.split(" ");
						if (commonTables.contains(tmpTab[2])) {
							readerPositioned = true;
						} else {
							currentLineOfReferenceFile = referenceDBReader.readLine();
							numberOfDifferences++;
							listOfTablesWithDifferences.add(currentTableName);
						}
					}

					// Compare the format of both tables
					if (MySQLFileComparisonUtils.isFormatIdentical(referenceDBReader, generatedDBReader)) {

						// If the format of both tables is identical, we continue the comparison by
						// checking the values
						int numberOfDifferencesForOneSingleTable = MySQLFileComparisonUtils
								.comparisonOfTableData(referenceDBReader, generatedDBReader);
						if (numberOfDifferencesForOneSingleTable != 0) {
							numberOfDifferences += numberOfDifferencesForOneSingleTable;
							listOfTablesWithDifferences.add(currentTableName);
						}

					} else {
						numberOfDifferences++;
						listOfTablesWithDifferences.add(currentTableName);
					}

					commonTables.remove(currentTableName);
				}

			} catch (FileNotFoundException fileNotFoundEx) {
				jobMessageTO.jobSummaryTO.state = JobStateEnum.ERROR;
				throw new TestException(getTestName(), "File nout found : " + fileNotFoundEx.getMessage(),
						fileNotFoundEx);
			} catch (IOException ioEx) {
				jobMessageTO.jobSummaryTO.state = JobStateEnum.ERROR;
				throw new TestException(getTestName(), "Error while comparing both reports : " + ioEx.getMessage(),
						ioEx);
			} finally {

				// Close the readers
				try {
					if (generatedDBReader != null) {
						generatedDBReader.close();
					}
					if (referenceDBReader != null) {
						referenceDBReader.close();
					}
				} catch (IOException ioex) {
					jobMessageTO.jobSummaryTO.state = JobStateEnum.ERROR;
					throw new TestException(getTestName(), "Error while closing the reports : " + ioex);
				}
			}

			writeResultInTable(true, numberOfDifferences, listOfTablesWithDifferences);
		} else {

			// Generation of the new reference file
			try {
				DBTestUtils.backupMySQLDatabase(configuration.mysqlDumpExe, configuration.user, configuration.password,
						REFREPORT_DATABASE_NAME, referenceDB);
			} catch (Exception e) {
				String errorMsg = "[IntegrationTestConsumer] Problem while generating the new reference dump "
						+ e.getMessage();
				jobMessageTO.jobSummaryTO.stateDetails = errorMsg;
				jobMessageTO.jobSummaryTO.addEvent(JobEventTypeEnum.MISCELLANEOUS, JobEventCriticityEnum.ERROR,
						errorMsg);
				LOGGER.error(errorMsg, e);
				jobMessageTO.jobSummaryTO.state = JobStateEnum.ERROR;
			}

			// Writing of the results in the result table
			writeResultInTableWhenNoReferenceFileExists();
		}

		jobMessageTO.setJobDuration(start);
		jobMessageTO.jobSummaryTO.jobOutput = referenceDB.getAbsolutePath();
		jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
		jmsConnectionHandler.sendJMSMessage(jobMessageTO, parameter.responseCanal);
		LOGGER.info("[IntegrationTestConsumer] Comparison complete");
		System.out.println("[IntegrationTestConsumer] doComparison End");
	}

	private void doRestoration(TestRestorationJobParameter parameter) {

		System.out.println("[IntegrationTestConsumer] doRestoration Start");
		JobMessageTO jobMessageTO = new JobMessageTO();
		jobMessageTO.buildMessageTO(parameter);
		Long start = Utils.getTime();

		MySQLConfigurationTO configuration = parameter.mysqlConfiguration;

		// Restoration of the database that was saved in the setup phase
		File backupFile = new File(parameter.backupDatabasePath);
		try {
			DBTestUtils.restoreMySQLDatabase(configuration.mysqlExe, configuration.user, configuration.password,
					REFREPORT_DATABASE_NAME, backupFile, true);
		} catch (Exception e) {
			LOGGER.error("[IntegrationTestConsumer] Problem while restoring the backup dump " + e.getMessage());
		}

		jobMessageTO.setJobDuration(start);
		jobMessageTO.jobSummaryTO.jobOutput = backupFile.getAbsolutePath();
		jobMessageTO.jobSummaryTO.state = JobStateEnum.COMPLETE;
		jmsConnectionHandler.sendJMSMessage(jobMessageTO, parameter.responseCanal);
		LOGGER.info("[IntegrationTestConsumer] Restoration complete");
		System.out.println("[IntegrationTestConsumer] doRestoration End");
	}

	/*
	 * Result Table Format Column 1 : Test date - TEST_DATE Column 2 : Existence of reference file -
	 * COMPARISON_BASE_EXISTS Column 3 : Number of differences - NB_DIFFERENCES Column 4 : List of columns with
	 * difference - TABLES_WITH_DIFFERENCES create schema ContinuousIntegration; create table
	 * t_provisioning_test_results ( TEST_DATE TIMESTAMP, COMPARISON_BASE_EXISTS BOOLEAN, NB_DIFFERENCES INT,
	 * TABLES_WITH_DIFFERENCES VARCHAR(255), primary key (TEST_DATE));
	 */

	private void writeResultInTable(boolean referenceFileExists, int numberOfDifferences,
			List<String> listOfTablesWithDifferences) {
		System.out.println("[IntegrationTestConsumer] Writing Results Start");
		Connection connection = createConnection();
		if (connection != null) {

			StringBuffer insertQuery;
			if (referenceFileExists) {
				insertQuery = new StringBuffer("INSERT INTO " + TEST_RESULT_TABLE + " VALUES (?, ?, ?, ?)");
			} else {
				insertQuery = new StringBuffer(
						"INSERT INTO " + TEST_RESULT_TABLE + " (TEST_DATE, COMPARISON_BASE_EXISTS) VALUES (?, ?)");
			}

			try {
				PreparedStatement preparedStatement = connection.prepareStatement(insertQuery.toString());
				preparedStatement.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
				preparedStatement.setObject(2, referenceFileExists);

				if (referenceFileExists) {
					preparedStatement.setObject(3, numberOfDifferences);

					String columns = "";
					for (int i = 0; i < listOfTablesWithDifferences.size(); i++) {
						if (i > 0) {
							columns += ",";
						}
						columns += listOfTablesWithDifferences.get(i);
					}
					preparedStatement.setObject(4, columns);

				}

				preparedStatement.executeUpdate();
				connection.commit();
				preparedStatement.close();
				System.out.println("[IntegrationTestConsumer] Writing Results End");

			} catch (SQLException sqle) {
				LOGGER.error("[IntegrationTestConsumer] " + sqle.getMessage());
			} finally {
				try {
					if (connection != null) {
						connection.close();
					}
				} catch (SQLException sqle) {
					LOGGER.error("[SQLRenderer] Cannot close connection " + sqle.getMessage());
				}
			}
		}
	}

	private void writeResultInTableWhenNoReferenceFileExists() {
		writeResultInTable(false, 0, null);
	}

	private Connection createConnection() {
		Connection connection = null;
		InitialContext initialContext;
		DataSource defaultDataSource = null;
		try {
			initialContext = new InitialContext();
			defaultDataSource = (DataSource) initialContext.lookupLink("dataSourceContinuousIntegration");
			if (defaultDataSource != null) {
				connection = defaultDataSource.getConnection();
				connection.setAutoCommit(false);
			}
		} catch (NamingException e) {
			LOGGER.error("[IntegrationTestConsumer] " + e.getMessage());
		} catch (SQLException e) {
			LOGGER.error("[IntegrationTestConsumer] " + e.getMessage());
		}

		return connection;
	}

}

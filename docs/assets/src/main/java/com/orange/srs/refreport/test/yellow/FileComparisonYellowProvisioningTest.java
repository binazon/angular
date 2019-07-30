package com.orange.srs.refreport.test.yellow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.ws.rs.core.UriInfo;

import com.orange.srs.common.test.model.ConfigTestParameters;
import com.orange.srs.common.test.technical.TableResultTO;
import com.orange.srs.common.test.technical.TestException;

@Stateless
public class FileComparisonYellowProvisioningTest extends YellowProvisioningTest {

	@Override
	protected boolean compareDatabases(UriInfo context, ConfigTestParameters configTestParameters)
			throws TestException {
		// Get reference SQL file
		File referenceDB = new File(configTestParameters.getReferenceFilePath());

		// Initiate parameters
		BufferedReader generatedDBReader = null;
		BufferedReader referenceDBReader = null;
		boolean result = true;

		try {
			// Create readers
			generatedDBReader = new BufferedReader(new FileReader(generatedDB));
			referenceDBReader = new BufferedReader(new FileReader(referenceDB));

			// Get the names of the tables of both databases
			ArrayList<String> tablesOfGeneratedDB = getTableNames(generatedDBReader);
			ArrayList<String> tablesOfReferenceDB = getTableNames(referenceDBReader);
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

				// Create TableResultTO
				TableResultTO tableResult = new TableResultTO();

				// Position the readers of both files to the next common tables
				boolean readerPositioned = false;
				while (!readerPositioned) {
					while (!currentLineOfGeneratedFile.startsWith("CREATE TABLE")) {
						currentLineOfGeneratedFile = generatedDBReader.readLine();
					}
					String[] tmpTab = currentLineOfGeneratedFile.split(" ");
					if (commonTables.contains(tmpTab[2])) {
						currentTableName = tmpTab[2];
						tableResult.tableName = currentTableName;
						readerPositioned = true;
					} else {
						currentLineOfGeneratedFile = generatedDBReader.readLine();
						result = false;
						TableResultTO tmp = new TableResultTO();
						tmp.tableName = tmpTab[2];
						tmp.presence = "Only present in the generated Database";
						tableResults.add(tmp);
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
						result = false;
						TableResultTO tmp = new TableResultTO();
						tmp.tableName = tmpTab[2];
						tmp.presence = "Only present in the reference Database";
						tableResults.add(tmp);
					}
				}

				tableResult.presence = "OK";

				// Compare the format of both tables
				if (IsFormatIdentical(referenceDBReader, generatedDBReader)) {

					// If the format of both tables is identical, we continue the comparison by
					// checking the values
					tableResult.format = "OK";

					int numberOfDifferences = comparisonOfTableData(referenceDBReader, generatedDBReader);
					if (numberOfDifferences == 0) {
						tableResult.data = "OK";
					} else {
						result = false;
						tableResult.data = "ERROR : " + numberOfDifferences + " differences found";
					}

				} else {
					// If the format is different, we issue a warning and we skip to the next table
					result = false;
					tableResult.format = "ERROR : different formats";
				}

				tableResults.add(tableResult);
				commonTables.remove(currentTableName);
			}

		} catch (FileNotFoundException fileNotFoundEx) {
			throw new TestException(getTestName(), "File nout found : " + fileNotFoundEx.getMessage(), fileNotFoundEx);
		} catch (IOException ioEx) {
			throw new TestException(getTestName(), "Error while comparing both reports : " + ioEx.getMessage(), ioEx);
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
				throw new TestException(getTestName(), "Error while closing the reports : " + ioex);
			}
		}

		return result;
	}

	private ArrayList<String> getTableNames(BufferedReader reader) throws TestException {
		ArrayList<String> tableNames = new ArrayList<String>();

		try {

			String currentLine = reader.readLine();

			while (currentLine != null) {
				if (currentLine.startsWith("CREATE TABLE")) {
					String[] tmpTab = currentLine.split(" ");
					tableNames.add(tmpTab[2]);
				}
				currentLine = reader.readLine();
			}

		} catch (IOException ioEx) {
			throw new TestException(getTestName(), "Error while looking for table names : " + ioEx.getMessage());
		}
		return tableNames;
	}

	private boolean IsFormatIdentical(BufferedReader referenceDBReader, BufferedReader generatedDBReader)
			throws TestException {
		boolean formatIdentical = true;

		try {
			boolean comparisonFinished = false;
			String currentLineOfGeneratedReport = generatedDBReader.readLine();
			String currentLineOfReferenceReport = referenceDBReader.readLine();

			while (!comparisonFinished && formatIdentical && currentLineOfGeneratedReport != null
					&& currentLineOfReferenceReport != null) {
				if (!currentLineOfGeneratedReport.equals(currentLineOfReferenceReport)) {
					formatIdentical = false;
				}

				if (formatIdentical && currentLineOfGeneratedReport.equals("")) {
					comparisonFinished = true;
				} else {
					currentLineOfGeneratedReport = generatedDBReader.readLine();
					currentLineOfReferenceReport = referenceDBReader.readLine();
				}
			}

			while (!currentLineOfGeneratedReport.equals("")) {
				currentLineOfGeneratedReport = generatedDBReader.readLine();
			}
			while (!currentLineOfReferenceReport.equals("")) {
				currentLineOfReferenceReport = referenceDBReader.readLine();
			}

		} catch (IOException ioEx) {
			throw new TestException(getTestName(), "Error while comparing table format : " + ioEx.getMessage());
		}

		return formatIdentical;
	}

	private int comparisonOfTableData(BufferedReader referenceDBReader, BufferedReader generatedDBReader)
			throws TestException {
		int numberOfDifferences = 0;

		try {
			String currentLineOfGeneratedReport = generatedDBReader.readLine();
			String currentLineOfReferenceReport = referenceDBReader.readLine();

			while (currentLineOfGeneratedReport != null && currentLineOfReferenceReport != null
					&& currentLineOfGeneratedReport.startsWith("INSERT INTO")
					&& currentLineOfReferenceReport.startsWith("INSERT INTO")) {
				if (!currentLineOfGeneratedReport.equals(currentLineOfReferenceReport)) {
					numberOfDifferences++;
				}
				currentLineOfGeneratedReport = generatedDBReader.readLine();
				currentLineOfReferenceReport = referenceDBReader.readLine();
			}

			while (currentLineOfGeneratedReport != null && currentLineOfGeneratedReport.startsWith("INSERT INTO")) {
				numberOfDifferences++;
				currentLineOfGeneratedReport = generatedDBReader.readLine();
			}

			while (currentLineOfReferenceReport != null && currentLineOfReferenceReport.startsWith("INSERT INTO")) {
				numberOfDifferences++;
				currentLineOfReferenceReport = referenceDBReader.readLine();
			}

		} catch (IOException ioEx) {
			throw new TestException(getTestName(), "Error while comparing table data : " + ioEx.getMessage());
		}

		return numberOfDifferences;
	}

}

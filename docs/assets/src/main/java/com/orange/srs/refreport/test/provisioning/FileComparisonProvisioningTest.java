package com.orange.srs.refreport.test.provisioning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import javax.ejb.Stateless;
import javax.ws.rs.core.UriInfo;

import com.orange.srs.common.test.model.ConfigTestParameters;
import com.orange.srs.common.test.technical.FileResultTO;
import com.orange.srs.common.test.technical.TableResultTO;
import com.orange.srs.common.test.technical.TestException;
import com.orange.srs.refreport.test.MySQLFileComparisonUtils;

@Stateless
public class FileComparisonProvisioningTest extends ProvisioningTest {

	private ArrayList<TableResultTO> tableResults;

	@Override
	public boolean checkResult(UriInfo context, ConfigTestParameters configTestParameters) throws TestException {

		// Get reference SQL file
		File referenceDB = new File(configTestParameters.getReferenceFilePath() + ".gz");

		// Initiate parameters
		BufferedReader generatedDBReader = null;
		BufferedReader referenceDBReader = null;
		testMessage = "";
		tableResults = new ArrayList<TableResultTO>();
		boolean result = true;

		try {
			// Create readers
			generatedDBReader = new BufferedReader(new FileReader(generatedDB));
			GZIPInputStream gzipStream = new GZIPInputStream(new FileInputStream(referenceDB));
			referenceDBReader = new BufferedReader(new InputStreamReader(gzipStream));

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
			gzipStream = new GZIPInputStream(new FileInputStream(referenceDB));
			referenceDBReader = new BufferedReader(new InputStreamReader(gzipStream));

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
				if (MySQLFileComparisonUtils.isFormatIdentical(referenceDBReader, generatedDBReader)) {

					// If the format of both tables is identical, we continue the comparison by
					// checking the values
					tableResult.format = "OK";

					int numberOfDifferences = MySQLFileComparisonUtils.comparisonOfTableData(referenceDBReader,
							generatedDBReader);
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

		if (result == true) {
			testMessage += "Test OK - The test has been completed successfully\n";
		} else {
			testMessage += "Test ERROR - Differences were found during the test\n";
		}

		return result;
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

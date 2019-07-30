package com.orange.srs.refreport.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import com.orange.srs.common.test.technical.TestException;

public class MySQLFileComparisonUtils {

	public static ArrayList<String> getTableNames(BufferedReader reader) throws TestException {
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
			throw new TestException("getTableNames", "Error while looking for table names : " + ioEx.getMessage());
		}
		return tableNames;
	}

	public static boolean isFormatIdentical(BufferedReader referenceDBReader, BufferedReader generatedDBReader)
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
			throw new TestException("isFormatIdentical", "Error while comparing table format : " + ioEx.getMessage());
		}

		return formatIdentical;
	}

	public static int comparisonOfTableData(BufferedReader referenceDBReader, BufferedReader generatedDBReader)
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
			throw new TestException("comparisonOfTableData", "Error while comparing table data : " + ioEx.getMessage());
		}

		return numberOfDifferences;
	}

}

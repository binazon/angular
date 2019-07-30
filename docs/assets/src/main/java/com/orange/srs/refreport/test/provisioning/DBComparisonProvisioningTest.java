package com.orange.srs.refreport.test.provisioning;

import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.ws.rs.core.UriInfo;

import com.orange.srs.common.test.model.ConfigTestParameters;
import com.orange.srs.common.test.parameter.MySQLCleanDBParameter;
import com.orange.srs.common.test.parameter.MySQLCompareDBParameter;
import com.orange.srs.common.test.parameter.MySQLRestoreDBParameter;
import com.orange.srs.common.test.technical.FileResultTO;
import com.orange.srs.common.test.technical.TableResultTO;
import com.orange.srs.common.test.technical.TestException;
import com.orange.srs.refreport.technical.test.RefReportTestConfiguration;

@Stateless
public class DBComparisonProvisioningTest extends ProvisioningTest {

	private String testMessage;

	@Override
	public boolean checkResult(UriInfo context, ConfigTestParameters configTestParameters) throws TestException {

		// Import the reference DB
		MySQLRestoreDBParameter parameter = RefReportTestConfiguration.getMySQLRestoreBDParameter();
		restoreMySQLDataBaseWithDump(parameter, configTestParameters.getReferenceFilePath(), "refreporttarget", false);

		try {

			// Use of mysqldbcompare
			MySQLCompareDBParameter compareParameter = RefReportTestConfiguration.getMySQLCompareDBParameter();
			String result = compareMySQLDatabases(compareParameter, "refreport", "refreporttarget");

			// Compute the result
			testMessage = null;
			testMessage = result;

		} catch (TestException testEx) {
			throw testEx;
		} catch (Exception ex) {
			throw new TestException(getTestName(), "Error while comparing both databases : " + ex.getMessage());
		} finally {

			// Clean the reference DB
			MySQLCleanDBParameter cleanParameter = RefReportTestConfiguration.getMySQLCleanDBParameter();
			cleanMySQLDataBase(cleanParameter, "refreporttarget", false);

		}

		if (testMessage.contains("FAIL")) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getTestMessage() {
		return testMessage;
	}

	@Override
	public ArrayList<TableResultTO> getTableResults() {
		return null;
	}

	@Override
	public ArrayList<FileResultTO> getFileResults() {
		return null;
	}

}

package com.orange.srs.refreport.test.yellow;

import javax.ejb.Stateless;
import javax.ws.rs.core.UriInfo;

import com.orange.srs.common.test.model.ConfigTestParameters;
import com.orange.srs.common.test.parameter.MySQLCleanDBParameter;
import com.orange.srs.common.test.parameter.MySQLCompareDBParameter;
import com.orange.srs.common.test.parameter.MySQLRestoreDBParameter;
import com.orange.srs.common.test.technical.TableResultTO;
import com.orange.srs.common.test.technical.TestException;
import com.orange.srs.refreport.technical.test.RefReportTestConfiguration;

@Stateless
public class DBComparisonYellowProvisioningTest extends YellowProvisioningTest {

	@Override
	protected boolean compareDatabases(UriInfo context, ConfigTestParameters configTestParameters)
			throws TestException {
		// Import the reference DB
		MySQLRestoreDBParameter parameter = RefReportTestConfiguration.getMySQLRestoreBDParameter();
		restoreMySQLDataBaseWithDump(parameter, configTestParameters.getReferenceFilePath(), "refreporttarget", false);

		String result = "";

		try {
			// Use of mysqldbcompare
			MySQLCompareDBParameter compareParameter = RefReportTestConfiguration.getMySQLCompareDBParameter();
			result = compareMySQLDatabases(compareParameter, "refreport", "refreporttarget");

			// Add the result to the tableResult list
			TableResultTO tableResult = new TableResultTO();
			tableResult.tableName = "refreport";
			tableResult.message = result;
			tableResults.add(tableResult);

		} catch (TestException testEx) {
			throw testEx;
		} catch (Exception ex) {
			throw new TestException(getTestName(), "Error while comparing both databases : " + ex.getMessage());
		} finally {

			// Clean the reference DB
			MySQLCleanDBParameter cleanParameter = RefReportTestConfiguration.getMySQLCleanDBParameter();
			cleanMySQLDataBase(cleanParameter, "refreporttarget", false);

		}

		if (result.contains("FAIL")) {
			return false;
		} else {
			return true;
		}
	}

}

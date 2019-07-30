package com.orange.srs.refreport.technical.test;

import com.orange.srs.common.test.parameter.MySQLCleanDBParameter;
import com.orange.srs.common.test.parameter.MySQLCompareDBParameter;
import com.orange.srs.common.test.parameter.MySQLDumpDBParameterFromConfiguration;
import com.orange.srs.common.test.parameter.MySQLRestoreDBParameter;
import com.orange.srs.common.test.parameter.MySQLRestoreDBParameterFromConfiguration;

public class RefReportTestConfiguration {
	public static MySQLDumpDBParameterFromConfiguration getMySQLDumpBDParameterFromConfiguration(
			String dumpfilePropertyInConfiguration) {
		MySQLDumpDBParameterFromConfiguration parameter = new MySQLDumpDBParameterFromConfiguration();
		parameter.mysqldumpexePathPropertyInConfiguration = "mysqldumppath";
		parameter.userPropertyInConfiguration = "mysqluser";
		parameter.passwordPropertyInConfiguration = "mysqlpassword";
		parameter.dumpfilePropertyInConfiguration = dumpfilePropertyInConfiguration;
		parameter.mysqlconfPathPropertyInConfiguration = "mysqlconfpath";
		parameter.schemaPropertyInConfiguration = "RefReportSchema";

		return parameter;
	}

	public static MySQLRestoreDBParameterFromConfiguration getMySQLRestoreBDParameterFromConfiguration(
			String dumpfilePropertyInConfiguration) {
		MySQLRestoreDBParameterFromConfiguration parameter = new MySQLRestoreDBParameterFromConfiguration();
		parameter.mysqlexePathPropertyInConfiguration = "mysqlpath";
		parameter.userPropertyInConfiguration = "mysqluser";
		parameter.passwordPropertyInConfiguration = "mysqlpassword";
		parameter.dumpfilePropertyInConfiguration = dumpfilePropertyInConfiguration;
		parameter.mysqlconfPathPropertyInConfiguration = "mysqlconfpath";

		return parameter;
	}

	public static MySQLRestoreDBParameter getMySQLRestoreBDParameter() {
		MySQLRestoreDBParameter parameter = new MySQLRestoreDBParameter();
		parameter.mysqlexePathPropertyInConfiguration = "mysqlpath";
		parameter.userPropertyInConfiguration = "mysqluser";
		parameter.passwordPropertyInConfiguration = "mysqlpassword";

		return parameter;
	}

	public static MySQLCleanDBParameter getMySQLCleanDBParameter() {
		MySQLCleanDBParameter parameter = new MySQLCleanDBParameter();
		parameter.mysqlexePathPropertyInConfiguration = "mysqlpath";
		parameter.userPropertyInConfiguration = "mysqluser";
		parameter.passwordPropertyInConfiguration = "mysqlpassword";

		return parameter;
	}

	public static MySQLCompareDBParameter getMySQLCompareDBParameter() {
		MySQLCompareDBParameter parameter = new MySQLCompareDBParameter();
		parameter.mysqldbcompareexePathPropertyInConfiguration = "mysqlcomparepath";
		parameter.userPropertyInConfiguration = "mysqluser";
		parameter.passwordPropertyInConfiguration = "mysqlpassword";

		return parameter;
	}
}

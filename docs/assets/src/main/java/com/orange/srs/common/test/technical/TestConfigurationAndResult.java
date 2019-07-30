package com.orange.srs.common.test.technical;

import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.orange.srs.common.test.model.ConfigTestParameters;
import com.orange.srs.refreport.technical.Configuration;

@Singleton
@Startup
@DependsOn({ "Configuration", "Log4jStartupBean" })
public class TestConfigurationAndResult extends AbstractTestConfigurationAndResult {

	public void buildParameters() {
		String confTestFolder = Configuration.acceptancetestFolderProperty;
		configTestParameters.put("AllProvisioningTest",
				new ConfigTestParameters(confTestFolder + "\\parameters\\PARAMETER_TEST_1.xml",
						confTestFolder + "\\references\\RESULT_TEST_1.sql", "AllProvisioningTest"));
		configTestParameters.put("UpdateProvisioningTest",
				new ConfigTestParameters(confTestFolder + "\\parameters\\PARAMETER_TEST_2",
						confTestFolder + "\\references\\RESULT_TEST_2.sql", "UpdateProvisioningTest"));
		configTestParameters.put("ExportInventoryTest",
				new ConfigTestParameters(confTestFolder + "\\parameters\\PARAMETER_TEST_3",
						confTestFolder + "\\references\\RESULT_TEST_3.h2.db", "ExportInventoryTest"));
		configTestParameters.put("YellowProvisioningTest", new ConfigTestParameters("",
				confTestFolder + "\\references\\RESULT_TEST_4.sql", "YellowProvisioningTest"));
		configTestParameters.put("UpdateYellowProvisioningTest",
				new ConfigTestParameters(confTestFolder + "\\references\\RESULT_TEST_5\\",
						confTestFolder + "\\references\\RESULT_TEST_5.sql", "UpdateYellowProvisioningTest"));
	}

}

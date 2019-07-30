package com.orange.srs.refreport.test.provisioning;

import javax.ejb.Stateless;
import javax.ws.rs.core.UriInfo;

import com.orange.srs.common.test.model.ConfigTestParameters;
import com.orange.srs.common.test.technical.TestException;

@Stateless
public class UpdateDBComparisonProvisioningTest extends DBComparisonProvisioningTest {

	@Override
	public void operateTestCase(UriInfo context, ConfigTestParameters configTestParameters) throws TestException {

		ConfigTestParameters parameters = new ConfigTestParameters(configTestParameters);

		// Provisioning on the first day
		parameters.setParametersPath(configTestParameters.getParametersPath() + "\\1.xml");
		super.operateTestCase(context, parameters);

		// Update on the following day
		parameters.setParametersPath(configTestParameters.getParametersPath() + "\\2.xml");
		super.operateTestCase(context, parameters);

	}
}

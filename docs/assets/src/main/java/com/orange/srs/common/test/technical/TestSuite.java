package com.orange.srs.common.test.technical;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.orange.srs.common.test.model.ConfigTestParameters;

@Stateless
public class TestSuite {

	private static final Logger LOGGER = Logger.getLogger(TestSuite.class);

	@EJB
	private TestConfigurationAndResult testConfigurationAndResult;

	public TestResultsTO executeTestSuite(String suiteLabel, List<TestCase> testCases, UriInfo context)
			throws TestException {
		return executeTestSuite(suiteLabel, testCases, context, false);
	}

	public TestResultsTO executeTestSuite(String suiteLabel, List<TestCase> testCases, UriInfo context,
			boolean regenerationOfReferenceFiles) throws TestException {
		if (testCases == null) {
			throw new TestException("TestSuite", "testCases cannot be null");
		}

		TestResultsTO result = new TestResultsTO();
		result.suiteLabel = suiteLabel;
		boolean stop = false;
		testConfigurationAndResult.clearResults();
		for (TestCase testCase : testCases) {
			TestCaseCommand command = testCase.getCommand();
			TestResultTO to = new TestResultTO();

			TestDescriptionTO descriptionTO = command.getDescriptionTO();

			if (descriptionTO.given != null || descriptionTO.when != null || descriptionTO.then != null
					|| descriptionTO.SOAService != null || descriptionTO.useCaseReference != null) {
				to.description = descriptionTO;
			}

			try {
				ConfigTestParameters configTestParameters = testConfigurationAndResult
						.getParametersFromTestName(testCase.getLabel());
				if (configTestParameters != null) {
					to.testCaseLabel = command.getTestName() + " - " + configTestParameters.getTestName();
				} else {
					to.testCaseLabel = command.getTestName();
				}
				result.results.add(to);

				if (!stop) {
					to.hasBeenRun = true;

					LOGGER.info("Building " + command.getTestName());
					command.buildData(context, configTestParameters);
					LOGGER.info("Operating " + command.getTestName());
					command.operateTestCase(context, configTestParameters);
					LOGGER.info("Checking " + command.getTestName());

					// Regenerate reference files or check the results
					if (regenerationOfReferenceFiles) {
						to.inError = !command.regenerateReferenceFile(context, configTestParameters);
					} else {
						to.inError = !command.checkResult(context, configTestParameters);
						ArrayList<TableResultTO> tableResults = command.getTableResults();
						if (tableResults != null) {
							to.tableResults = tableResults;
						}
						ArrayList<FileResultTO> fileResults = command.getFileResults();
						if (fileResults != null) {
							to.fileResults = fileResults;
						}
					}

					String testMessage = command.getTestMessage();
					if (testMessage != null) {
						to.message = testMessage;
					}

				}
			} catch (TestException te) {
				LOGGER.error(te.getMessage(), te);
				to.testLabel = te.getTestLabel();
				to.inError = true;
				to.message = te.getMessage();
			} finally {
				command.clearDataAfterTest(context);
				testConfigurationAndResult.clearResults();
			}

			if (to.inError && testCase.isBreakTestSuiteOnFailure()) {
				stop = true;
			}

		}

		return result;
	}

}

package com.orange.srs.refreport.provider.service.rest.unittest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.orange.srs.common.test.technical.TestCase;
import com.orange.srs.common.test.technical.TestException;
import com.orange.srs.common.test.technical.TestResultsTO;
import com.orange.srs.common.test.technical.TestSuite;
import com.orange.srs.refreport.test.ExportInventoryTest;
import com.orange.srs.refreport.test.provisioning.DBComparisonProvisioningTest;
import com.orange.srs.refreport.test.provisioning.FileComparisonProvisioningTest;
import com.orange.srs.refreport.test.provisioning.UpdateDBComparisonProvisioningTest;
import com.orange.srs.refreport.test.provisioning.UpdateFileComparisonProvisioningTest;
import com.orange.srs.refreport.test.yellow.DBComparisonYellowProvisioningTest;
import com.orange.srs.refreport.test.yellow.FileComparisonYellowProvisioningTest;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

@Stateless
@Path("UnitTest")
public class UnitTestService {

	private static final Logger LOGGER = Logger.getLogger(UnitTestService.class);

	@Context
	private UriInfo context;

	@EJB
	private TestSuite testSuite;

	@EJB
	private FileComparisonProvisioningTest fileComparisonProvisioningTest;

	@EJB
	private DBComparisonProvisioningTest DBComparisonProvisioningTest;

	@EJB
	private UpdateFileComparisonProvisioningTest updateFileComparisonProvisioningTest;

	@EJB
	private UpdateDBComparisonProvisioningTest updateDBComparisonProvisioningTest;

	@EJB
	private ExportInventoryTest exportInventoryTest;

	@EJB
	private FileComparisonYellowProvisioningTest fileComparisonYellowTest;

	@EJB
	private DBComparisonYellowProvisioningTest DBComparisonYellowTest;

	@GET
	@Path("/test/{testName}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getResultFromExecutionOfOneTest(@PathParam(value = "testName") String testName) {
		Response response = null;
		try {
			List<TestCase> suite = new ArrayList<TestCase>(1);

			for (TestCase testCase : getTestCases()) {
				if (testCase.getLabel().equals(testName)) {
					suite.add(testCase);
				}
			}

			TestResultsTO to = testSuite.executeTestSuite(testName, suite, context);
			response = Response.ok(to).build();
		} catch (RuntimeException ejbtex) {

			ejbtex.printStackTrace();
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		} catch (TestException te) {
			te.printStackTrace();
			response = RestResponseFactory.makeExceptionResponseFactory(te, Status.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	@GET
	@Path("/test/DB/{testName}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getResultFromExecutionOfOneTestWithDB(@PathParam(value = "testName") String testName) {
		Response response = null;
		try {
			List<TestCase> suite = new ArrayList<TestCase>(1);

			for (TestCase testCase : getTestCases("DBComparison")) {
				if (testCase.getLabel().equals(testName)) {
					suite.add(testCase);
				}
			}

			TestResultsTO to = testSuite.executeTestSuite(testName, suite, context);
			response = Response.ok(to).build();
		} catch (RuntimeException ejbtex) {

			ejbtex.printStackTrace();
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		} catch (TestException te) {
			te.printStackTrace();
			response = RestResponseFactory.makeExceptionResponseFactory(te, Status.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getResultsFromExecutionOfDefaultTests() {

		Response response = null;
		try {
			TestResultsTO to = testSuite.executeTestSuite("Default", getTestCases(), context);
			response = Response.ok(to).build();
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		} catch (TestException te) {
			response = RestResponseFactory.makeExceptionResponseFactory(te, Status.INTERNAL_SERVER_ERROR);
		}

		return response;

	}

	@GET
	@Path("suite/{suiteLabel}")
	@Produces(MediaType.APPLICATION_XML)
	public Response gettestsuite(@PathParam(value = "suiteLabel") String suiteLabel) {
		Response response = null;
		try {
			TestResultsTO to = testSuite.executeTestSuite(suiteLabel, getTestCases(suiteLabel), context);
			response = Response.ok(to).build();
		} catch (RuntimeException ejbtex) {

			ejbtex.printStackTrace();
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		} catch (TestException te) {
			te.printStackTrace();
			response = RestResponseFactory.makeExceptionResponseFactory(te, Status.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	@GET
	@Path("regeneration")
	@Produces(MediaType.APPLICATION_XML)
	public Response regenerateReferenceFilesOfAllTests() {
		Response response = null;
		try {
			TestResultsTO to = testSuite.executeTestSuite("Default", getTestCases("Regeneration"), context, true);
			response = Response.ok(to).build();
		} catch (RuntimeException ejbtex) {
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		} catch (TestException te) {
			response = RestResponseFactory.makeExceptionResponseFactory(te, Status.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	@GET
	@Path("regeneration/{testName}")
	@Produces(MediaType.APPLICATION_XML)
	public Response regenerateReferenceFileOfSingleTest(@PathParam(value = "testName") String testName) {
		Response response = null;
		try {
			List<TestCase> suite = new ArrayList<TestCase>(1);

			for (TestCase testCase : getTestCases("Regeneration")) {
				if (testCase.getLabel().equals(testName)) {
					suite.add(testCase);
				}
			}

			TestResultsTO to = testSuite.executeTestSuite(testName, suite, context, true);
			response = Response.ok(to).build();
		} catch (RuntimeException ejbtex) {
			ejbtex.printStackTrace();
			response = RestResponseFactory.makeInternalErrorResponseFactory(ejbtex, LOGGER);
		} catch (TestException te) {
			te.printStackTrace();
			response = RestResponseFactory.makeExceptionResponseFactory(te, Status.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	public void setContext(UriInfo context) {
		this.context = context;
	}

	public List<TestCase> getTestCases() {
		return getTestCases(null);
	}

	public List<TestCase> getTestCases(String suiteLabel) {

		// Add all your tests to the "testCases" list
		List<TestCase> testCases = new ArrayList<TestCase>();
		if ("DataInitialization".equals(suiteLabel)) {
			testCases.add(new TestCase(fileComparisonYellowTest, "YellowProvisioningTest", false));
			testCases.add(new TestCase(fileComparisonProvisioningTest, "AllProvisioningTest", false));
			testCases.add(new TestCase(exportInventoryTest, "ExportInventoryTest", false));
		} else if ("DBComparison".equals(suiteLabel)) {
			testCases.add(new TestCase(DBComparisonProvisioningTest, "AllProvisioningTest", false));
			testCases.add(new TestCase(updateDBComparisonProvisioningTest, "UpdateProvisioningTest", false));
			testCases.add(new TestCase(DBComparisonYellowTest, "YellowProvisioningTest", false));
			testCases.add(new TestCase(DBComparisonYellowTest, "UpdateYellowProvisioningTest", false));
		} else if ("FileComparison".equals(suiteLabel)) {
			testCases.add(new TestCase(fileComparisonProvisioningTest, "AllProvisioningTest", false));
			testCases.add(new TestCase(updateFileComparisonProvisioningTest, "UpdateProvisioningTest", false));
			testCases.add(new TestCase(fileComparisonYellowTest, "YellowProvisioningTest", false));
			testCases.add(new TestCase(fileComparisonYellowTest, "UpdateYellowProvisioningTest", false));
		} else if ("ExportInventory".equals(suiteLabel)) {
			testCases.add(new TestCase(exportInventoryTest, "ExportInventoryTest", false));
		} else if ("Regeneration".equals(suiteLabel)) {
			testCases.add(new TestCase(fileComparisonYellowTest, "UpdateYellowProvisioningTest", false));
			testCases.add(new TestCase(fileComparisonYellowTest, "YellowProvisioningTest", false));
			testCases.add(new TestCase(fileComparisonProvisioningTest, "AllProvisioningTest", false));
			testCases.add(new TestCase(updateFileComparisonProvisioningTest, "UpdateProvisioningTest", false));
			testCases.add(new TestCase(exportInventoryTest, "ExportInventoryTest", false));
		} else {
			testCases.add(new TestCase(fileComparisonProvisioningTest, "AllProvisioningTest", false));
			testCases.add(new TestCase(updateFileComparisonProvisioningTest, "UpdateProvisioningTest", false));
			testCases.add(new TestCase(exportInventoryTest, "ExportInventoryTest", false));
			testCases.add(new TestCase(fileComparisonYellowTest, "YellowProvisioningTest", false));
			testCases.add(new TestCase(fileComparisonYellowTest, "UpdateYellowProvisioningTest", false));
		}
		return testCases;
	}
}

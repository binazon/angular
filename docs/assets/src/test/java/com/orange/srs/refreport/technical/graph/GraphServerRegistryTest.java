package com.orange.srs.refreport.technical.graph;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.InitialContext;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.orange.srs.refreport.model.TO.inventory.GraphDatabaseServiceTO;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.graph.GraphDatabaseServiceWithStateProxy.GraphServiceStateEnum;
import com.orange.srs.refreport.technical.log.Log4jStartupTest;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;

/**
 * JUnit test class of GraphServerRegistry.<br/>
 * 
 * @see GraphServerRegistry
 * @author Pascal Morvan (Atos)
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GraphServerRegistry.class, InitialContext.class })
@PowerMockIgnore({ "org.neo4j.*" })
public class GraphServerRegistryTest {

	// Configure Log4j in a static block of the JUnit test class
	static {
		Log4jStartupTest.configure();
	}

	private static final Logger LOGGER = Logger.getLogger(GraphServerRegistryTest.class);

	@InjectMocks
	private GraphServerRegistry graphServerRegistry;

	/**
	 * Method executed before each JUnit test method.
	 * 
	 * @throws Throwable
	 */
	@Before
	public void before() throws Throwable {
		Configuration.configGraphCachePolicy = "NO";

		// Delete the temporary export directory
		// Create the temporary export directory
		try {
			File exportDir = new File("tmp/export");
			if (exportDir.exists()) {
				FileUtils.deleteDirectory(exportDir);
			}
			exportDir.mkdirs();
		} catch (Throwable t) {
			LOGGER.error("Failed to delete the temporary export directory", t);
		}

		// Static mock for InitialContext
		PowerMockito.mockStatic(InitialContext.class);
		PowerMockito.when(InitialContext.doLookup("java:module/ModuleName")).thenReturn("arbrrt");
	}

	/**
	 * Method executed after each JUnit test method.
	 * 
	 * @throws Throwable
	 */
	@After
	public void after() throws Throwable {
		// Delete the temporary export directory
		try {
			File exportDir = new File("tmp/export");
			if (exportDir.exists()) {
				FileUtils.deleteDirectory(exportDir);
			}
		} catch (Throwable t) {
			LOGGER.error("Failed to delete the temporary export directory", t);
		}
	}

	/**
	 * Test method of FlushGraphDatabases: success case.
	 * 
	 * @throws Throwable
	 * 
	 * @see {@link GraphServerRegistry#flushAllExceptActiveGraphDatabases(SOAContext)}
	 */
	@Test
	public void testFlushGraphDatabases_success1() throws Throwable {
		// Init
		SOAContext soaContext = SOATools.buildSOAContext(null);

		GraphDatabaseServiceWithStateProxy proxy = null;
		GraphDatabaseServiceWithStateProxy proxy1 = null;
		GraphDatabaseServiceWithStateProxy proxy4 = null;
		final String SEP1 = "\n";

		// Datasets: create several empty Neo4J graph databases with different states
		// Dataset #1
		proxy = graphServerRegistry.createGraphDatabaseService("tmp/export/neo4jGraphDatabase1", null, soaContext);
		proxy.activate(soaContext);
		graphServerRegistry.getCurrentService();
		Assert.assertEquals(GraphServiceStateEnum.ACTIVE, proxy.getState());
		proxy1 = proxy;

		// Dataset #2
		proxy = graphServerRegistry.createGraphDatabaseService("tmp/export/neo4jGraphDatabase2", null, soaContext);
		proxy.activate(soaContext);
		graphServerRegistry.getCurrentService();
		proxy.inactivate(soaContext);
		Assert.assertEquals(GraphServiceStateEnum.INACTIVE, proxy.getState());

		// Note: The database #1 have been inactivated automatically after the creation of the database #2
		Assert.assertEquals(GraphServiceStateEnum.INACTIVE, proxy1.getState());

		// Dataset #3
		proxy = graphServerRegistry.createGraphDatabaseService("tmp/export/neo4jGraphDatabase3", null, soaContext);
		Assert.assertEquals(GraphServiceStateEnum.NEW, proxy.getState());

		// Dataset #4
		proxy = graphServerRegistry.createGraphDatabaseService("tmp/export/neo4jGraphDatabase4", null, soaContext);
		proxy.activate(soaContext);
		graphServerRegistry.getCurrentService();
		proxy.release(soaContext);
		Assert.assertEquals(GraphServiceStateEnum.ACTIVE, proxy.getState());
		proxy4 = proxy;

		// Dataset #5
		proxy = graphServerRegistry.createGraphDatabaseService("tmp/export/neo4jGraphDatabase5", null, soaContext);
		proxy.activate(soaContext);
		proxy.inactivate(soaContext);
		Assert.assertEquals(GraphServiceStateEnum.TO_GARBAGE, proxy.getState());

		// Note: The state of the database #4 have been set automatically to TO_GARBAGE after the creation of the
		// database #5
		Assert.assertEquals(GraphServiceStateEnum.TO_GARBAGE, proxy4.getState());

		// Dataset #6
		proxy = graphServerRegistry.createGraphDatabaseService("tmp/export/neo4jGraphDatabase6", null, soaContext);
		proxy.activate(soaContext);
		graphServerRegistry.getCurrentService();
		Assert.assertEquals(GraphServiceStateEnum.ACTIVE, proxy.getState());

		proxy = null;
		proxy1 = null;
		proxy4 = null;

		// Assertions/Logs before test
		// Check the graph databases in memory before the flushAll operation:
		// Check that only the TO_GARBAGE graph databases of the datasets have been deleted in memory and on disk
		Set<String> expectedGraphDatabases = new TreeSet<>();
		expectedGraphDatabases.add("neo4jGraphDatabase1:INACTIVE");
		expectedGraphDatabases.add("neo4jGraphDatabase2:INACTIVE");
		expectedGraphDatabases.add("neo4jGraphDatabase3:NEW");
		expectedGraphDatabases.add("neo4jGraphDatabase6:ACTIVE");

		Set<String> actualGraphDatabases = new TreeSet<>();
		for (GraphDatabaseServiceTO service : graphServerRegistry.getGraphServicesList().graphDatabaseServices) {
			File dir = new File(service.folder);
			actualGraphDatabases.add(String.format("%s:%s", dir.getName(), service.state));
		}
		for (String actualGraphDatabase : actualGraphDatabases) {
			LOGGER.info("BEFORE: " + actualGraphDatabase);
		}
		Assert.assertEquals("Neo4j graph databases before test", String.join(SEP1, expectedGraphDatabases),
				String.join(SEP1, actualGraphDatabases));

		// Test
		Exception exception = null;
		try {
			graphServerRegistry.flushAllExceptActiveGraphDatabases(soaContext);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			exception = e;
		}

		// Assertions/Logs after test
		// No exception should be thrown during the flushAll operation
		Assert.assertEquals(null, exception);

		// Check the graph databases in memory after the flushAll operation:
		// Only the ACTIVE graph databases are kept in memory
		expectedGraphDatabases = new TreeSet<>();
		expectedGraphDatabases.add("neo4jGraphDatabase6:ACTIVE");

		actualGraphDatabases = new TreeSet<>();
		for (GraphDatabaseServiceTO service : graphServerRegistry.getGraphServicesList().graphDatabaseServices) {
			File dir = new File(service.folder);
			actualGraphDatabases.add(String.format("%s:%s", dir.getName(), service.state));
		}
		for (String actualGraphDatabase : actualGraphDatabases) {
			LOGGER.info("AFTER: " + actualGraphDatabase);
		}
		Assert.assertEquals("Neo4J graph databases after test", String.join(SEP1, expectedGraphDatabases),
				String.join(SEP1, actualGraphDatabases));

		// Check the graph databases on disk after the flushAll operation:
		// The graph databases that have been flushed in memory should not be deleted on disk
		Set<String> expectedDirnames = new TreeSet<>();
		expectedDirnames.add("neo4jGraphDatabase1");
		expectedDirnames.add("neo4jGraphDatabase2");
		expectedDirnames.add("neo4jGraphDatabase3");
		expectedDirnames.add("neo4jGraphDatabase6");

		Set<String> actualDirnames = new TreeSet<>();
		File exportDir = new File("tmp/export");
		for (File f : exportDir.listFiles()) {
			if (f.isDirectory()) {
				actualDirnames.add(f.getName());
				LOGGER.info("Existing directory after test: " + f);
			}
		}
		Assert.assertEquals("Neo4J directory names after test", String.join(SEP1, expectedDirnames),
				String.join(SEP1, actualDirnames));

		// Shutdown the current ACTIVE graph database service
		// (in order to be able to delete this graph database folder on disk)
		GraphDatabaseServiceHelper.stopGraphDatabaseService(graphServerRegistry.getCurrentService().getService());
	}

}

package com.orange.srs.refreport.technical.log;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Log4j configuration class for testing.<br/>
 * 
 * The {@link Log4jStartupTest #configure()} method should be called in a static block of the JUnit test superclass in
 * order to configure Log4j for testing.
 * 
 * @author Pascal Morvan (Atos)
 */
public class Log4jStartupTest {

	private static Path log4jPath = Paths.get("src/test/resources/conf/log4j.xml");

	public static void configure() {
		String moduleName = "arbrrt-test";
		String error = null;
		try {
			// Init Log4j
			DOMConfigurator.configure(log4jPath.toFile().toURI().toURL());
		} catch (Throwable t) {
			t.printStackTrace();
			error = "Application logging configuration not found";
		}

		Logger logger = Logger.getLogger(Log4jStartupTest.class);
		if (error != null) {
			System.err.println(error);
			logger.error(String.format("Logger initialization: %s...", error));
		}

		logger.info(String.format("Log4j logger for %s initialized with %s...", moduleName, log4jPath.toString()));
	}

}

package com.orange.srs.refreport.technical;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.orange.srs.statcommon.business.commonFunctions.PatternResolutionDelegate;
import com.orange.srs.statcommon.model.enums.FileActionEnum;
import com.orange.srs.statcommon.model.registry.WatchedFileRegistry;
import com.orange.srs.statcommon.technical.ConfigurationLoader;
import com.orange.srs.statcommon.technical.MainConfiguration;
import com.orange.srs.statcommon.technical.i18n.LanguageUtils;

@Singleton
@Startup
@DependsOn("Log4jStartupBean")
public class Configuration extends MainConfiguration implements ConfigurationLoader {

	private static final Logger LOGGER = Logger.getLogger(Configuration.class);

	public static Properties applicationConfiguration;
	public static Properties channelConfiguration;
	public static Properties mountConfiguration;
	public static Properties serviceConfiguration;
	public static Properties supervisionConfiguration;

	private static WatchedFileRegistry applicationConfigurationWatcher;
	private static WatchedFileRegistry channelConfigurationWatcher;
	private static WatchedFileRegistry supervisionConfigurationWatcher;
	@Deprecated
	private static WatchedFileRegistry languageConfigurationWatcher;

	// application.properties
	public static String helpUrlProperty;
	public static int watchProvisioningFilesPeriodProperty;
	public static String configGraphCachePolicy;
	public static String h2FileAutoserverOptionsProperty;
	public static String h2FileReadwriteLockOptionsProperty;
	public static String h2FileReadonlyNolockOptionsProperty;
	public static String h2MemoryOptionsProperty;
	public static int jdbcProvisioningFetchSize;
	public static int groupPartitionsStatusThreadsNumber;
	public static int groupPartitionsStatusBatchSize;

	// mount.properties
	public static String rootProperty;
	public static String statgroupFolderProperty;
	public static String allFolderProperty;
	public static String statgroupDataFolderProperty;
	public static String sharedFolderProperty;
	public static String inventoryFolderProperty;
	public static String acceptancetestFolderProperty;
	public static String configCriteriaProperty;
	public static String configFilterProperty;
	public static String configHyperlinkProperty;
	public static String configBookmarkDirectReportProperty;
	public static String configIndicatorProperty;
	public static String configInputformatProperty;
	public static String configInputsourceProperty;
	public static String configOfferProperty;
	public static String configParamtypeProperty;
	public static String configProxyProperty;
	public static String configReportProperty;
	public static String configReportconfigProperty;
	public static String configReportinputProperty;
	public static String configSourceclassProperty;
	public static String configSourceproxyProperty;
	public static String configTypesubtypeProperty;
	public static String configProvisioningProperty;
	public static String configDiversProperty;
	public static String configInventoryProperty;
	public static String provisioningProperty;
	public static String provisioningFolder;
	public static String graphdatabaseFolder;
	public static String mscosInputProperty;
	// Number of days before final deletion of statEntities
	public static long provisioningDurationBeforeDeletion;
	private static String workFolder;
	private static String exportFolder;
	public static String workExportPathName;

	// channels.properties
	public static String producedChannels;

	// supervision.properties
	public static final String APPLICATION_NAME = "RefReport";
	public static int updateStatusTimer;
	public static boolean jmsSupervisionRequired;
	public static int momTestTimer;
	public static int lastConsumingDateThreshold;
	public static String mom_supervision_channel_name;
	public static boolean graphDbSupervisionRequired;
	public static int graphDbActivationDateThreshold;

	// other properties calculated here
	public static String tmpDirectory;

	// constants or variables used outside
	public static final String TELECONFIGURATION_SERVICE_PROPERTY = "TELECONFIGURATION_SERVICE";
	public static final String STATUS_SERVICE_PROPERTY = "STATUS_SERVICE";
	// size of the number of entities that are handled in a partition
	public static final int partitionSize = 1500;
	public static final int h2PaginationSize = 7500;
	public static final int paginationSize = 1000;
	public static String statEntityConfFileName;
	public static String graphExportConfFileName;
	public static String partitionConfFileName;
	@Deprecated
	public static LanguageUtils languageUtils;

	public Configuration() {
	}

	@Override
	@PostConstruct
	public void initConfiguration() {

		try {
			moduleName = (String) InitialContext.doLookup("java:module/ModuleName");
			LOGGER.debug("Initializing configuration for module : " + moduleName);

			loadOnlyOnceConfiguration();
			loadReloadableConfiguration();

			channelConfigurationWatcher = new WatchedFileRegistry(
					buildPathToPropertyFile(moduleName, "channels.properties").toString()) {

				@Override
				protected void onChange(FileActionEnum action) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Reloading channels configuration");
					}
					try {
						loadChannelConfiguration();
					} catch (Exception e) {
						LOGGER.error("Exception while reloading channels configuration " + e.getMessage());
					}
				}
			};

			applicationConfigurationWatcher = new WatchedFileRegistry(
					buildPathToPropertyFile(moduleName, "application.properties").toString()) {

				@Override
				protected void onChange(FileActionEnum action) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Reloading application configuration");
					}
					try {
						loadApplicationConfiguration();
					} catch (Exception e) {
						LOGGER.error("Exception while reloading application configuration " + e.getMessage());
					}
				}
			};

			supervisionConfigurationWatcher = new WatchedFileRegistry(
					buildPathToPropertyFile(moduleName, "supervision.properties").toString()) {

				@Override
				protected void onChange(FileActionEnum action) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Reloading supervision configuration");
					}
					try {
						loadSupervisionConfiguration();
					} catch (Exception e) {
						LOGGER.error("Exception while reloading supervision configuration " + e.getMessage());
					}
				}
			};

			languageConfigurationWatcher = new WatchedFileRegistry(
					buildPathToConfigurationFile(moduleName, "defaultlanguages.xml").toString()) {
				@Override
				protected void onChange(FileActionEnum action) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Reloading defaultlanguages.xml configuration");
					}
					try {
						languageUtils.reloadLanguageUtils(action);
					} catch (Exception e) {
						LOGGER.error("Exception while reloading supervision configuration " + e.getMessage());
					}
				}
			};

			setTimerIntervalAndLaunch(TIMER_INTERVAL_DURATION_SECONDS);

		} catch (NamingException ne) {
			LOGGER.fatal("Cannot open context", ne);
		} catch (IOException e) {
			LOGGER.fatal("Exception while loading configuration " + e.getLocalizedMessage(), e);
		}
	}

	@Override
	@Timeout
	@Lock(LockType.WRITE)
	public void watchFiles(Timer timer) {

		channelConfigurationWatcher.checkFile();
		applicationConfigurationWatcher.checkFile();
		supervisionConfigurationWatcher.checkFile();
		languageConfigurationWatcher.checkFile();
	}

	@Override
	public void loadOnlyOnceConfiguration() throws IOException {

		loadMountConfiguration();
		loadServiceConfiguration();

		statEntityConfFileName = Paths.get(rootProperty, configInventoryProperty, "inventoryConfig.xml").toString();
		graphExportConfFileName = Paths.get(rootProperty, configInventoryProperty, "inventoryGraphConfig.xml")
				.toString();
		partitionConfFileName = Paths.get(rootProperty, configInventoryProperty, "partitionsConfig.xml").toString();

		tmpDirectory = Paths.get(System.getProperty("java.io.tmpdir")).toString();

		// Load languages configuration
		String configurationTranslationPath = buildPathToConfigurationFile(moduleName, "defaultlanguages.xml")
				.toString();
		languageUtils = new LanguageUtils(configurationTranslationPath);
		try {
			languageUtils.buildLanguageUtils();
		} catch (NamingException | JAXBException e) {
			throw new IOException("Error while loading defaultlanguages.xml", e.getCause());
		}
	}

	@Override
	public void loadReloadableConfiguration() throws IOException {

		loadApplicationConfiguration();
		loadChannelConfiguration();
		loadSupervisionConfiguration();

		// print public fields of objects
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Public fields of " + this.getClass().getCanonicalName() + " after initialization or reload : "
					+ printPublicFieldsOf());
		}
	}

	public void loadApplicationConfiguration() throws IOException {

		applicationConfiguration = loadPropertiesFile(moduleName, "application.properties");

		helpUrlProperty = applicationConfiguration.getProperty("HELP_URL");
		configGraphCachePolicy = applicationConfiguration.getProperty("GRAPH_CACHE_POLICY");
		watchProvisioningFilesPeriodProperty = Integer
				.parseInt(applicationConfiguration.getProperty("WATCH_PROVISIONING_FILES_PERIOD"));

		PatternResolutionDelegate.USE_HASH = Boolean.valueOf(applicationConfiguration.getProperty("HASH", "true"));

		String h2FileOptions = applicationConfiguration.getProperty("H2_FILE_OPTIONS",
				";MAX_COMPACT_TIME=1000;LOCK_TIMEOUT=10000;CACHE_SIZE=65526;QUERY_CACHE_SIZE=16;CACHE_TYPE=TQ;EARLY_FILTER=1;MAX_LOG_SIZE=32");
		h2FileAutoserverOptionsProperty = ";AUTO_SERVER=TRUE;MVCC=TRUE" + h2FileOptions;
		h2FileReadwriteLockOptionsProperty = h2FileOptions;
		h2FileReadonlyNolockOptionsProperty = ";FILE_LOCK=NO;LOCK_MODE=0;ACCESS_MODE_DATA=r" + h2FileOptions;
		h2MemoryOptionsProperty = applicationConfiguration.getProperty("H2_MEMORY_OPTIONS",
				";CACHE_SIZE=65526;QUERY_CACHE_SIZE=16;CACHE_TYPE=TQ;EARLY_FILTER=1");
		jdbcProvisioningFetchSize = Integer
				.parseInt(applicationConfiguration.getProperty("JDBC_PROVISIONING_FETCH_SIZE", "1000"));
		groupPartitionsStatusThreadsNumber = Integer
				.parseInt(applicationConfiguration.getProperty("GROUP_PARTITIONS_STATUS_THREADS_NUMBER", "10"));
		groupPartitionsStatusBatchSize = Integer
				.parseInt(applicationConfiguration.getProperty("GROUP_PARTITIONS_STATUS_BATCH_SIZE", "1000"));

		printPropertiesFileInDebug(applicationConfiguration, LOGGER);

	}

	public void loadChannelConfiguration() throws IOException {

		channelConfiguration = loadPropertiesFile(moduleName, "channels.properties");

		producedChannels = channelConfiguration.getProperty("producedchannels",
				"replyScheduler01,replyScheduler02,replyScheduler03,supervision,notifyPurge");

		printPropertiesFileInDebug(channelConfiguration, LOGGER);
	}

	public void loadSupervisionConfiguration() throws IOException {

		supervisionConfiguration = loadPropertiesFile(moduleName, "supervision.properties");

		updateStatusTimer = Integer.parseInt(supervisionConfiguration.getProperty("UPDATE_STATUS_TIMER", "300000"));
		jmsSupervisionRequired = Boolean
				.parseBoolean(supervisionConfiguration.getProperty("JMS_SUPERVISION_REQUIRED", "true"));
		momTestTimer = Integer.parseInt(supervisionConfiguration.getProperty("MOM_TEST_TIMER", "300000"));
		lastConsumingDateThreshold = Integer
				.parseInt(supervisionConfiguration.getProperty("LAST_CONSUMING_DATE_DURATION", "300000"));
		mom_supervision_channel_name = supervisionConfiguration.getProperty("MOM_SUPERVISION_CHANNEL_NAME",
				"supervision");
		graphDbSupervisionRequired = Boolean
				.parseBoolean(supervisionConfiguration.getProperty("GRAPH_DB_SUPERVISION_REQUIRED", "true"));
		graphDbActivationDateThreshold = Integer
				.parseInt(supervisionConfiguration.getProperty("GRAPH_DB_ACTIVATION_DATE_THRESHOLD", "53"));

		printPropertiesFileInDebug(supervisionConfiguration, LOGGER);
	}

	public void loadMountConfiguration() throws IOException {

		mountConfiguration = loadPropertiesFile(moduleName, "mount.properties");

		rootProperty = mountConfiguration.getProperty("ROOT");
		statgroupFolderProperty = mountConfiguration.getProperty("STATGROUP_FOLDER");
		allFolderProperty = mountConfiguration.getProperty("ALL_FOLDER");
		statgroupDataFolderProperty = mountConfiguration.getProperty("STATGROUP_DATA_FOLDER");
		sharedFolderProperty = mountConfiguration.getProperty("SHARED_FOLDER");
		inventoryFolderProperty = mountConfiguration.getProperty("INVENTORY_FOLDER");
		configCriteriaProperty = mountConfiguration.getProperty("CONFIG_CRITERIA");
		configFilterProperty = mountConfiguration.getProperty("CONFIG_FILTER");
		configHyperlinkProperty = mountConfiguration.getProperty("CONFIG_HYPERLINK");
		configBookmarkDirectReportProperty = mountConfiguration.getProperty("CONFIG_BOOKMARK_DIRECT_REPORT");
		configIndicatorProperty = mountConfiguration.getProperty("CONFIG_INDICATOR");
		configInputformatProperty = mountConfiguration.getProperty("CONFIG_INPUTFORMAT");
		configInputsourceProperty = mountConfiguration.getProperty("CONFIG_INPUTSOURCE");
		configOfferProperty = mountConfiguration.getProperty("CONFIG_OFFER");
		configParamtypeProperty = mountConfiguration.getProperty("CONFIG_PARAMTYPE");
		configProxyProperty = mountConfiguration.getProperty("CONFIG_PROXY");
		configReportProperty = mountConfiguration.getProperty("CONFIG_REPORT");
		configReportconfigProperty = mountConfiguration.getProperty("CONFIG_REPORTCONFIG");
		configReportinputProperty = mountConfiguration.getProperty("CONFIG_REPORTINPUT");
		configSourceclassProperty = mountConfiguration.getProperty("CONFIG_SOURCECLASS");
		configSourceproxyProperty = mountConfiguration.getProperty("CONFIG_SOURCEPROXY");
		configTypesubtypeProperty = mountConfiguration.getProperty("CONFIG_TYPESUBTYPE");
		configProvisioningProperty = mountConfiguration.getProperty("CONFIG_PROVISIONING");
		configDiversProperty = mountConfiguration.getProperty("CONFIG_DIVERS");
		configInventoryProperty = mountConfiguration.getProperty("CONFIG_INVENTORY");
		provisioningProperty = mountConfiguration.getProperty("PROVISIONING");
		mscosInputProperty = mountConfiguration.getProperty("MSCOS_INPUT");
		acceptancetestFolderProperty = Paths
				.get(rootProperty, "test", "unittest", mountConfiguration.getProperty("ACCEPTANCETEST_FOLDER"))
				.toString();
		provisioningDurationBeforeDeletion = Long
				.parseLong(mountConfiguration.getProperty("PROVISIONING_DURATION_BEFORE_DELETION", "400"));
		graphdatabaseFolder = mountConfiguration.getProperty("GRAPHDATABASE_FOLDER");

		workFolder = mountConfiguration.getProperty("WORK_FOLDER");
		exportFolder = mountConfiguration.getProperty("EXPORT");
		Path workExportPath = Paths.get(rootProperty, workFolder, exportFolder);
		Files.createDirectories(workExportPath);
		workExportPathName = workExportPath.toString();

		provisioningFolder = Paths.get(rootProperty, "{MOUNT}", provisioningProperty).toString();

		printPropertiesFileInDebug(mountConfiguration, LOGGER);
	}

	private void loadServiceConfiguration() throws IOException {

		serviceConfiguration = loadPropertiesFile(moduleName, "services.properties");
		printPropertiesFileInDebug(serviceConfiguration, LOGGER);

	}
}

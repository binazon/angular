package com.orange.srs.refreport.applicative;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA02ReportFacade;
import com.orange.srs.refreport.business.command.provisioning.AbstractProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.BookmarkDirectReportProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.CriteriaProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ExternalIndicatorProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.FilterProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.HyperlinkProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.IndicatorProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.InputFormatProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.InputSourceProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.OfferAndOptionProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ParamTypeProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ProxyProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ReportConfigProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ReportInputProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.ReportProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.SourceClassProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.SourceProxyProvisioningCommand;
import com.orange.srs.refreport.business.command.provisioning.TypeSubtypeProvisioningCommand;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.XmlFileOnlyFilter;
import com.orange.srs.statcommon.business.commonFunctions.Translation;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;

@Singleton
@Startup
@DependsOn({ "Configuration", "Log4jStartupBean" })
public class WatcherProvisioningFiles {

	private static final Logger LOGGER = Logger.getLogger(WatcherProvisioningFiles.class);

	private static final String FILE_ERROR_EXTENSION = ".error";

	private static final String FILE_NAME_CRITERIA = "criteria.xml";
	private static final String FILE_NAME_EXTERNAL_INDICATOR = "externalIndicator.xml";
	private static final String FILE_NAME_HYPERLINK = "hyperlink.xml";
	private static final String FILE_NAME_INDICATOR = "indicator.xml";
	private static final String FILE_NAME_PARAM_TYPE = "paramType.xml";
	private static final String FILE_NAME_TYPE_SUBTYPE = "typeSubtype.xml";
	private static final String FILE_NAME_FILTER = "filter.xml";
	private static final String FILE_NAME_BOOKMARK_DIRECT_REPORT = "bookmarkDirectReport.xml";

	@EJB
	private SOA02ReportFacade reportFacade;

	@EJB
	private CriteriaProvisioningCommand criteriaProvisioningCommand;

	@EJB
	private FilterProvisioningCommand filterProvisioningCommand;

	@EJB
	private OfferAndOptionProvisioningCommand offerProvisioningCommand;

	@EJB
	private ParamTypeProvisioningCommand paramTypeProvisioningCommand;

	@EJB
	private ReportConfigProvisioningCommand reportConfigProvisioningCommand;

	@EJB
	private IndicatorProvisioningCommand indicatorProvisioningCommand;

	@EJB
	private ReportProvisioningCommand reportProvisioningCommand;

	@EJB
	private ReportInputProvisioningCommand reportInputProvisioningCommand;

	@EJB
	private InputFormatProvisioningCommand inputFormatProvisioningCommand;

	@EJB
	private SourceClassProvisioningCommand sourceClassProvisioningCommand;

	@EJB
	private ProxyProvisioningCommand proxyProvisioningCommand;

	@EJB
	private InputSourceProvisioningCommand inputSourceProvisioningCommand;

	@EJB
	private SourceProxyProvisioningCommand sourceProxyProvisioningCommand;

	@EJB
	private TypeSubtypeProvisioningCommand typeSubtypeProvisioningCommand;

	@EJB
	private HyperlinkProvisioningCommand hyperlinkProvisioningCommand;

	@EJB
	private BookmarkDirectReportProvisioningCommand bookmarkDirectReportProvisioningCommand;

	@EJB
	private ExternalIndicatorProvisioningCommand externalIndicatorProvisioningCommand;

	@Resource
	private TimerService timerService;

	private Map<String, AbstractProvisioningCommand> provisioningCommandsByWatchedPath = new LinkedHashMap<>();

	private List<String> watchedPathsImplyingToExportH2Table = new ArrayList<>();

	private long lastCheckTime;

	private boolean checkProvisioningInProgress;

	@PostConstruct
	public void listWatchedPath() {

		String rootPath = Configuration.rootProperty + File.separatorChar;

		String criteriaProvisioningFilePath = rootPath + Configuration.configCriteriaProperty + File.separatorChar
				+ FILE_NAME_CRITERIA;
		provisioningCommandsByWatchedPath.put(criteriaProvisioningFilePath, criteriaProvisioningCommand);

		String offerProvisioningDirectoryPath = rootPath + Configuration.configOfferProperty;
		provisioningCommandsByWatchedPath.put(offerProvisioningDirectoryPath, offerProvisioningCommand);

		String filterProvisioningFilePath = rootPath + Configuration.configFilterProperty + File.separatorChar
				+ FILE_NAME_FILTER;
		provisioningCommandsByWatchedPath.put(filterProvisioningFilePath, filterProvisioningCommand);

		String inputFormatProvisioningDirectoryPath = rootPath + Configuration.configInputformatProperty;
		provisioningCommandsByWatchedPath.put(inputFormatProvisioningDirectoryPath, inputFormatProvisioningCommand);
		watchedPathsImplyingToExportH2Table.add(inputFormatProvisioningDirectoryPath);

		String reportInputProvisioningDirectoryPath = rootPath + Configuration.configReportinputProperty;
		provisioningCommandsByWatchedPath.put(reportInputProvisioningDirectoryPath, reportInputProvisioningCommand);
		watchedPathsImplyingToExportH2Table.add(reportInputProvisioningDirectoryPath);

		String indicatorProvisioningFilePath = rootPath + Configuration.configIndicatorProperty + File.separatorChar
				+ FILE_NAME_INDICATOR;
		provisioningCommandsByWatchedPath.put(indicatorProvisioningFilePath, indicatorProvisioningCommand);

		String reportProvisioningDirectoryPath = rootPath + Configuration.configReportProperty;
		provisioningCommandsByWatchedPath.put(reportProvisioningDirectoryPath, reportProvisioningCommand);

		String typeSubtypeFilePath = rootPath + Configuration.configTypesubtypeProperty + File.separatorChar
				+ FILE_NAME_TYPE_SUBTYPE;
		provisioningCommandsByWatchedPath.put(typeSubtypeFilePath, typeSubtypeProvisioningCommand);

		String paramTypeProvisioningFilePath = rootPath + Configuration.configParamtypeProperty + File.separatorChar
				+ FILE_NAME_PARAM_TYPE;
		provisioningCommandsByWatchedPath.put(paramTypeProvisioningFilePath, paramTypeProvisioningCommand);

		String reportConfigProvisioningDirectoryPath = rootPath + Configuration.configReportconfigProperty;
		provisioningCommandsByWatchedPath.put(reportConfigProvisioningDirectoryPath, reportConfigProvisioningCommand);

		String sourceClassProvisioningFilePath = SourceClassProvisioningCommand.getSourceClassProvisioningFilePath();
		provisioningCommandsByWatchedPath.put(sourceClassProvisioningFilePath, sourceClassProvisioningCommand);

		String proxyProvisioningFilePath = ProxyProvisioningCommand.getProxyProvisioningFilePath();
		provisioningCommandsByWatchedPath.put(proxyProvisioningFilePath, proxyProvisioningCommand);

		String inputSourceProvisioningDirectoryPath = InputSourceProvisioningCommand
				.getInputSourceProvisioningDirectoryPath();
		provisioningCommandsByWatchedPath.put(inputSourceProvisioningDirectoryPath, inputSourceProvisioningCommand);

		String sourceProxyProvisioningFilePath = SourceProxyProvisioningCommand.getSourceProxyProvisioningFilePath();
		provisioningCommandsByWatchedPath.put(sourceProxyProvisioningFilePath, sourceProxyProvisioningCommand);

		String hyperlinkFilePath = rootPath + Configuration.configHyperlinkProperty + File.separatorChar
				+ FILE_NAME_HYPERLINK;
		provisioningCommandsByWatchedPath.put(hyperlinkFilePath, hyperlinkProvisioningCommand);

		String externalIndicatorFilePath = rootPath + Configuration.configProvisioningProperty + File.separatorChar
				+ FILE_NAME_EXTERNAL_INDICATOR;
		provisioningCommandsByWatchedPath.put(externalIndicatorFilePath, externalIndicatorProvisioningCommand);

		String bookmarkDirectReportFilePath = rootPath + Configuration.configBookmarkDirectReportProperty
				+ File.separatorChar + FILE_NAME_BOOKMARK_DIRECT_REPORT;
		provisioningCommandsByWatchedPath.put(bookmarkDirectReportFilePath, bookmarkDirectReportProvisioningCommand);

		lastCheckTime = 0;
		checkProvisioningInProgress = false;
	}

	@Lock(LockType.WRITE)
	private boolean canCheckProvisioningFiles() {
		boolean result = !checkProvisioningInProgress;
		this.checkProvisioningInProgress = true;
		return result;
	}

	@Lock(LockType.WRITE)
	private void endCheckProvisioningFiles() {
		this.checkProvisioningInProgress = false;
	}

	@Lock(LockType.READ)
	public boolean startTimerIfPossible() {
		SOAContext soaContext = SOATools.buildSOAContext(null);
		for (Timer timer : timerService.getTimers()) {
			if (timer != null) {
				LOGGER.warn(SOATools.buildSOALogMessage(soaContext, "YellowPart Provisioning Timer already started"));
				return false;
			}
		}
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Starting YellowPart Provisioning Timer"));
		// The 1st timer will timeout after WATCH_PROVISIONING_FILES_PERIOD_VALUE
		// seconds
		timerService.createIntervalTimer(
				Configuration.watchProvisioningFilesPeriodProperty * Translation.MILLISECOND_TO_SECOND,
				Configuration.watchProvisioningFilesPeriodProperty * Translation.MILLISECOND_TO_SECOND,
				new TimerConfig(null, false));
		this.lastCheckTime = 0;
		LOGGER.info(SOATools.buildSOALogMessage(soaContext, "YellowPart Provisioning Timer started"));
		return true;
	}

	@Lock(LockType.READ)
	public boolean stopYellowTimer() {
		SOAContext soaContext = SOATools.buildSOAContext(null);
		boolean timerFound = false;
		for (Timer timer : timerService.getTimers()) {
			if (timer != null) {
				LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Stopping YellowPart Provisioning Timer"));
				timer.cancel();
				timer = null;
				timerFound = true;
				LOGGER.info(SOATools.buildSOALogMessage(soaContext, "YellowPart Provisioning Timer stopped"));
			}
		}
		return timerFound;
	}

	@Lock(LockType.READ)
	public boolean forceUpdateFromFilesToDataBase() {
		stopYellowTimer();
		return loopWatchedPathsToForceConfigurationFromProvisioningToDataBase();
	}

	@Timeout
	@Lock(LockType.READ)
	public void loopWatchedPathsToCheckIfProvisioningFilesHaveChanged(Timer timer) {

		if (!canCheckProvisioningFiles()) {
			return;
		}

		SOAContext soaContext = SOATools.buildSOAContext(null);
		try {
			boolean reportInputPatternH2TableHasToBeExported = false;
			long currentCheckTime = Utils.getTime();
			for (Entry<String, AbstractProvisioningCommand> provisioningCommandsByWatchedPathEntry : provisioningCommandsByWatchedPath
					.entrySet()) {
				boolean hasDatabaseBeenUpdatedWithThisFile = executeCommandIfOneOfProvisioningFilesHasChanged(
						soaContext, provisioningCommandsByWatchedPathEntry);
				if (!reportInputPatternH2TableHasToBeExported && hasDatabaseBeenUpdatedWithThisFile) {
					if (watchedPathsImplyingToExportH2Table.contains(provisioningCommandsByWatchedPathEntry.getKey())) {
						reportInputPatternH2TableHasToBeExported = true;
					}
				}
			}
			lastCheckTime = currentCheckTime;
			if (reportInputPatternH2TableHasToBeExported) {
				reportFacade.exportH2ReportInputs(soaContext);
			}
		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Exception checking provisioning file changes", e));
		} finally {
			endCheckProvisioningFiles();
		}
	}

	@Lock(LockType.READ)
	private boolean loopWatchedPathsToForceConfigurationFromProvisioningToDataBase() {

		if (!canCheckProvisioningFiles()) {
			return false;
		}

		SOAContext soaContext = SOATools.buildSOAContext(null);
		try {

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "Forcing YellowPart Provisioning from files"));

			for (Entry<String, AbstractProvisioningCommand> provisioningCommandsByWatchedPathEntry : provisioningCommandsByWatchedPath
					.entrySet()) {
				String provisioningFilePathName = provisioningCommandsByWatchedPathEntry.getKey();
				File provisioningFilePath = new File(provisioningFilePathName);
				if (provisioningFilePath.exists()) {
					if (provisioningFilePath.isDirectory()) {
						for (File provisioningFile : provisioningFilePath.listFiles(new XmlFileOnlyFilter())) {
							executeCommandForcingConfigurationFromProvisioningFile(soaContext, provisioningFile,
									provisioningCommandsByWatchedPathEntry.getValue());
						}
					} else {
						// It's a single file
						executeCommandForcingConfigurationFromProvisioningFile(soaContext, provisioningFilePath,
								provisioningCommandsByWatchedPathEntry.getValue());
					}
				} else {
					LOGGER.error(
							SOATools.buildSOALogMessage(soaContext, "Unable to find path " + provisioningFilePathName));
				}
			}
			reportFacade.exportH2ReportInputs(soaContext);

			LOGGER.info(SOATools.buildSOALogMessage(soaContext, "YellowPart Provisioning force done from files"));

		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Exception checking provisioning file changes", e));
		} finally {
			endCheckProvisioningFiles();
		}
		return true;
	}

	private boolean executeCommandIfOneOfProvisioningFilesHasChanged(SOAContext soaContext,
			Entry<String, AbstractProvisioningCommand> provisioningCommandsByWatchedPathEntry) {

		boolean hasDatabaseBeenUpdatedWithThisFile = false;
		String provisioningFilePathName = provisioningCommandsByWatchedPathEntry.getKey();
		File provisioningFilePath = new File(provisioningFilePathName);
		if (provisioningFilePath.exists()) {
			if (provisioningFilePath.isDirectory()) {
				for (File provisioningFile : provisioningFilePath.listFiles(new XmlFileOnlyFilter())) {
					boolean result = executeCommandIfProvisioningFileHasChanged(soaContext, provisioningFile,
							provisioningCommandsByWatchedPathEntry.getValue());
					if (hasDatabaseBeenUpdatedWithThisFile == false) {
						hasDatabaseBeenUpdatedWithThisFile = result;
					}
				}
			} else {
				// It's a single file
				boolean result = executeCommandIfProvisioningFileHasChanged(soaContext, provisioningFilePath,
						provisioningCommandsByWatchedPathEntry.getValue());
				if (hasDatabaseBeenUpdatedWithThisFile == false) {
					hasDatabaseBeenUpdatedWithThisFile = result;
				}
			}
		} else {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Unable to find path " + provisioningFilePathName));
		}
		return hasDatabaseBeenUpdatedWithThisFile;
	}

	private boolean executeCommandIfProvisioningFileHasChanged(SOAContext soaContext, File provisioningFile,
			AbstractProvisioningCommand provisioningCommand) {

		boolean hasDatabaseBeenUpdatedWithThisFile = false;
		if (provisioningFile.lastModified() > lastCheckTime) {
			try {
				provisioningCommand.executeAndRollbackIfNecessary(soaContext, provisioningFile, false);
				LOGGER.info("Transaction commit: modifications done in database");
				hasDatabaseBeenUpdatedWithThisFile = true;
			} catch (Exception e) {
				LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Exception in provisioning using the file "
						+ provisioningFile.getAbsolutePath() + ", Rollback all modifications in database", e));
				try {
					writeExceptionInAnErrorTimestampFile(provisioningFile, e);
				} catch (IOException ioe) {
					LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Unable to create dedicated error file", ioe));
				}
			}
		}
		return hasDatabaseBeenUpdatedWithThisFile;
	}

	private void executeCommandForcingConfigurationFromProvisioningFile(SOAContext soaContext, File provisioningFile,
			AbstractProvisioningCommand provisioningCommand) {

		try {
			provisioningCommand.executeAndRollbackIfNecessary(soaContext, provisioningFile, true);
			LOGGER.info("Transaction commit: modifications done in database");
		} catch (Exception e) {
			LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Exception in provisioning using the file "
					+ provisioningFile.getAbsolutePath() + ", Rollback all modifications in database", e));
			try {
				writeExceptionInAnErrorTimestampFile(provisioningFile, e);
			} catch (IOException ioe) {
				LOGGER.error(SOATools.buildSOALogMessage(soaContext, "Unable to create dedicated error file", ioe));
			}
		}
	}

	private void writeExceptionInAnErrorTimestampFile(File provisioningFile, Exception be) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_kk-mm-ss", Locale.FRANCE);
		String fileNameError = FilenameUtils.removeExtension(provisioningFile.getAbsolutePath()) + "_"
				+ dateFormat.format(new Date()) + FILE_ERROR_EXTENSION;
		File errorFile = new File(fileNameError);
		errorFile.createNewFile();
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(errorFile);
			be.printStackTrace(printWriter);
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
		}
	}

}

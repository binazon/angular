package com.orange.srs.refreport.technical.supervision;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA16InventoryGraphFacade;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.JMSConnectionHandler;
import com.orange.srs.statcommon.model.TO.rest.StatusTO;
import com.orange.srs.statcommon.model.TO.rest.SubStatusTO;
import com.orange.srs.statcommon.model.enums.ApplicationStateEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.model.parameter.SupervisionMessage;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.Utils;
import com.orange.srs.statcommon.technical.exception.ExceptionHelper;
import com.orange.srs.statcommon.technical.supervision.AbstractJMSSupervisionState;
import com.orange.srs.statcommon.technical.supervision.ApplicationStateHandler;
import com.orange.srs.statcommon.technical.supervision.JMSSupervisionState;
import com.orange.srs.statcommon.technical.supervision.SupervisionDelegate;

@Singleton
@Startup
@DependsOn({ "Log4jStartupBean", "JMSConnectionHandler" })
public class ApplicationStateSingleton extends AbstractJMSSupervisionState
		implements ApplicationStateHandler, JMSSupervisionState {

	@EJB
	private SupervisionDelegate supervisionDelegate;

	@EJB
	private SOA16InventoryGraphFacade soa16InventoryGraphFacade;

	@EJB
	private JMSConnectionHandler jmsConnectionHandler;

	@Resource(lookup = "dataSourceRefReport")
	private DataSource refReportDataSource;

	@Resource
	public TimerService timerService;

	private static final Logger LOGGER = Logger.getLogger("Supervision");

	private StatusTO currentStatus = new StatusTO();

	private Timer updateStatusTimer;
	private TimerConfig updateStatusConfig = new TimerConfig("updateStatus", false);
	private int currentUpdateStatusTimer;

	private Timer checkMomAvailabilityTimer;
	private TimerConfig checkMomAvailabilityconfig = new TimerConfig("checkMomAvailability", false);
	private int currentCheckMomAvailabilityTimer;

	@PostConstruct
	protected void constructTimer() {
		if (Configuration.jmsSupervisionRequired) {
			checkMomAvailabilityTimer = timerService.createIntervalTimer(0, Configuration.momTestTimer,
					checkMomAvailabilityconfig);
			currentCheckMomAvailabilityTimer = Configuration.momTestTimer;
		}

		updateStatusTimer = timerService.createIntervalTimer(7000, Configuration.updateStatusTimer, updateStatusConfig);
		currentUpdateStatusTimer = Configuration.updateStatusTimer;
	}

	@Timeout
	public void executeScheduledSupervisionMethods(Timer timer) {
		SOAContext soaContext = SOATools.buildSOAContext(null);
		if ("updateStatus".equals(timer.getInfo())) {
			computeState(soaContext);
		} else if ("checkMomAvailability".equals(timer.getInfo())) {
			comsumingAvailability = comsumingAvailability
					&& (Utils.getTime() - lastConsumingDate < Configuration.lastConsumingDateThreshold);

			try {
				SupervisionMessage supervisionMessage = new SupervisionMessage(HOSTINFO, Utils.getTime());
				jmsConnectionHandler.sendMessage(supervisionMessage, Configuration.mom_supervision_channel_name);
				LOGGER.debug("[ApplicationStateSingleton] sent message = " + supervisionMessage.getUuid() + " / date"
						+ supervisionMessage.getSendDate());
				producingAvailability = true;
			} catch (Exception e) {
				producingAvailability = false;
				LOGGER.error(SOATools.buildSOALogMessage(soaContext,
						"[ApplicationStateSingleton] Can't send JMS message to supervision topic." + e.getMessage(),
						e));
			}
		}
		reconstructTimersIfConfChanges();
	}

	@Lock(LockType.WRITE)
	public void reconstructTimersIfConfChanges() {
		if (currentUpdateStatusTimer != Configuration.updateStatusTimer) {
			updateStatusTimer.cancel();
			updateStatusTimer = timerService.createIntervalTimer(Configuration.updateStatusTimer,
					Configuration.updateStatusTimer, updateStatusConfig);
			currentUpdateStatusTimer = Configuration.updateStatusTimer;
		}
		if (Configuration.jmsSupervisionRequired && currentCheckMomAvailabilityTimer != Configuration.momTestTimer) {
			checkMomAvailabilityTimer.cancel();
			checkMomAvailabilityTimer = timerService.createIntervalTimer(Configuration.momTestTimer,
					Configuration.momTestTimer, checkMomAvailabilityconfig);
			currentCheckMomAvailabilityTimer = Configuration.momTestTimer;
		}
	}

	@Override
	public StatusTO getStatus() {
		return currentStatus;
	}

	@Lock(LockType.WRITE)
	private void computeState(SOAContext soaContext) {

		LOGGER.debug("[ApplicationStateSingleton] computing application status.");
		currentStatus.subStatus.clear();

		// TODO faire en sorte de ne pas avoir de try catch ici
		try {
			currentStatus.add(supervisionDelegate.getSqlConnectionStatus(refReportDataSource, soaContext));
		} catch (Exception e) {
			SubStatusTO sqlConnection = new SubStatusTO("SQL DataBase Connection", ApplicationStateEnum.KO,
					SupervisionDelegate.EXCEPTION_STATUS_LABEL + ExceptionHelper.getOriginalException(e).getMessage());
			currentStatus.add(sqlConnection);
		}
		if (Configuration.graphDbSupervisionRequired) {
			currentStatus.add(getGraphDataBaseAccessStatus(soaContext));
		}
		if (Configuration.jmsSupervisionRequired) {
			currentStatus.add(supervisionDelegate.getMomConnectionStatus(Configuration.APPLICATION_NAME,
					producingAvailability, comsumingAvailability));
		}

		supervisionDelegate.computeGlobalApplicationState(currentStatus);
		LOGGER.debug("[ApplicationStateSingleton] application status = " + currentStatus.available.status.toString());
	}

	// TODO should be moved to SupervisionDelegate when OrientDB will replaced neo4j
	private SubStatusTO getGraphDataBaseAccessStatus(SOAContext soaContext) {

		SubStatusTO graphDataBaseAccess;
		try {
			Date activeGraphDatabaseDate = soa16InventoryGraphFacade.activeGraphDatabaseCreationDate();
			if (activeGraphDatabaseDate != null && soa16InventoryGraphFacade.testGraphDatabaseConnection()) {
				if (Utils.getTime() - activeGraphDatabaseDate.getTime() < TimeUnit.MILLISECONDS
						.convert(Configuration.graphDbActivationDateThreshold, TimeUnit.HOURS)) {
					graphDataBaseAccess = new SubStatusTO("Graph DataBase Access", ApplicationStateEnum.OK,
							"Active Graph DataBase date: " + activeGraphDatabaseDate.toString());
				} else {
					graphDataBaseAccess = new SubStatusTO("Graph DataBase Access", ApplicationStateEnum.WARNING,
							"Old graph database activated. DB date: " + activeGraphDatabaseDate.toString());
				}
			} else {
				graphDataBaseAccess = new SubStatusTO("Graph DataBase Access", ApplicationStateEnum.KO,
						"No graph database activated");
			}
		} catch (ParseException e) {
			graphDataBaseAccess = new SubStatusTO("Graph DataBase Access", ApplicationStateEnum.OK,
					"Unable to determine Active Graph DataBase date.");
			LOGGER.error(SOATools.buildSOALogMessage(soaContext,
					"[getGraphDataBaseAccessStatus] Cannot parse DB name." + e.getMessage(), e));
		} catch (Exception e) {
			graphDataBaseAccess = new SubStatusTO("Graph DataBase Access", ApplicationStateEnum.KO,
					SupervisionDelegate.EXCEPTION_STATUS_LABEL + e.getMessage());
			LOGGER.error(SOATools.buildSOALogMessage(soaContext,
					"[getGraphDataBaseAccessStatus] Cannot retrieve graph database" + e.getMessage(), e));
		}
		return graphDataBaseAccess;

	}
}

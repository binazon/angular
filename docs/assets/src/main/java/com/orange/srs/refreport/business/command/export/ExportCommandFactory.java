package com.orange.srs.refreport.business.command.export;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.orange.srs.statcommon.model.enums.ExportTypeEnum;

@Singleton
@Startup
public class ExportCommandFactory {
	private static Map<ExportTypeEnum, DBExportSpecificInventoryCommand> commands;

	@EJB
	private DBExportOpenFlowInventoryCommand dBExportOpenFlowInventoryCommand;

	@EJB
	private DBExportPAIInventoryCommand dBExportPAIInventoryCommand;

	@EJB
	private DBExportGKInventoryCommand dBExportGKInventoryCommand;

	@PostConstruct
	public void finishBuild() {
		commands = new HashMap<ExportTypeEnum, DBExportSpecificInventoryCommand>();
		commands.put(ExportTypeEnum.EXPORT_ENTITIES_OPEN_FLOW, dBExportOpenFlowInventoryCommand);
		commands.put(ExportTypeEnum.EXPORT_PAI, dBExportPAIInventoryCommand);
		commands.put(ExportTypeEnum.EXPORT_GK, dBExportGKInventoryCommand);
	}

	public DBExportSpecificInventoryCommand getCommand(ExportTypeEnum exportType) {
		return commands.get(exportType);
	}
}

package com.orange.srs.refreport.model.TO.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orange.srs.statcommon.technical.jdbc.StatementWithColumnNameAdapter;
import com.orange.srs.statcommon.technical.jdbc.UpdateStatementAdapter;

public class EntityTypeH2StatementAndInsertionInfo {

	private String type;
	private StatementWithColumnNameAdapter statement;
	private Map<String, UpdateStatementAdapter> updateStatementList;
	private boolean hasParentColumn = false;
	private Long tablePk;
	private List<String> indexColumns = new ArrayList<>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public StatementWithColumnNameAdapter getStatement() {
		return statement;
	}

	public void addUpdateStatement(String columnName, UpdateStatementAdapter adapter) {
		if (this.updateStatementList == null) {
			this.updateStatementList = new HashMap<>();
		}
		this.updateStatementList.put(columnName, adapter);
	}

	public UpdateStatementAdapter getUpdateStatement(String columnName) {
		if (this.updateStatementList == null) {
			this.updateStatementList = new HashMap<>();
		}
		return updateStatementList.get(columnName);
	}

	public void setStatement(StatementWithColumnNameAdapter adapter) {
		this.statement = adapter;
	}

	public boolean hasParentColumn() {
		return hasParentColumn;
	}

	public void setHasParentColumn(boolean hasParentColumn) {
		this.hasParentColumn = hasParentColumn;
	}

	public Long getTablePk() {
		return tablePk;
	}

	public void setTablePk(Long tablePk) {
		this.tablePk = tablePk;
	}

	public List<String> getIndexColumns() {
		return indexColumns;
	}

	public void setIndexColumns(List<String> indexColumns) {
		this.indexColumns = indexColumns;
	}

}

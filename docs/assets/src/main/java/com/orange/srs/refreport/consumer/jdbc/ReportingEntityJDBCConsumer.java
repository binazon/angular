package com.orange.srs.refreport.consumer.jdbc;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.model.EntityLinkAttribute;
import com.orange.srs.refreport.model.EntityLinkAttributeId;
import com.orange.srs.refreport.model.EntityLinkId;
import com.orange.srs.refreport.model.ReportingEntity;
import com.orange.srs.refreport.model.TO.inventory.EntityTypeH2StatementAndInsertionInfo;
import com.orange.srs.refreport.model.parameter.ListAttributeElementParameter;
import com.orange.srs.refreport.model.parameter.ListAttributeValueParameter;
import com.orange.srs.refreport.model.parameter.ListAttributeValueParameterList;
import com.orange.srs.refreport.model.parameter.inventory.EntityInfoParameter;
import com.orange.srs.refreport.technical.Configuration;
import com.orange.srs.refreport.technical.xml.JAXBRefReportFactory;
import com.orange.srs.statcommon.business.commonFunctions.BDStatCommonFunction;
import com.orange.srs.statcommon.model.constant.H2Table;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.jdbc.StatementWithColumnNameAdapter;
import com.orange.srs.statcommon.technical.jdbc.UpdateStatementAdapter;

public class ReportingEntityJDBCConsumer {

	private static final Logger LOGGER = Logger.getLogger(ReportingEntityJDBCConsumer.class);

	@Resource(lookup = "dataSourceRefReport")
	private DataSource dataSource;

	private Connection refReportConnection;

	private static final String ENTITY_WITH_ID_AND_TYPE_QUERY = "select  E.PK, E.ENTITY_ID, E.ENTITY_TYPE"
			+ " FROM T_REPORTING_ENTITY E";

	private static final String ENTITY_WITH_PARENT_QUERY = "select  ENTITY.ENTITY_ID, ENTITY.ENTITY_TYPE, ENTITY.ORIGIN, PARENT.ENTITY_ID, PARENT.ENTITY_TYPE, PARENT.ORIGIN"
			+ " FROM T_REPORTING_ENTITY ENTITY "
			+ " LEFT JOIN T_REPORTING_ENTITY PARENT ON ENTITY.REPORTING_ENTITY_PARENT_FK=PARENT.PK";

	private static final String ENTITY_LINKS_WITH_ID_AND_ORIGIN = "select  LINK.PARAMETER, LINK.ROLE,"
			+ "SOURCE.ENTITY_ID, SOURCE.ENTITY_TYPE, SOURCE.ORIGIN, "
			+ "DESTINATION.ENTITY_ID, DESTINATION.ENTITY_TYPE, DESTINATION.ORIGIN" + " FROM T_ENTITY_LINK LINK "
			+ " JOIN T_REPORTING_ENTITY SOURCE ON LINK.REPORTING_ENTITY_SRC_FK=SOURCE.PK"
			+ " JOIN T_REPORTING_ENTITY DESTINATION ON LINK.REPORTING_ENTITY_DEST_FK=DESTINATION.PK";

	private static final String FULL_ENTITY_QUERY_START = "select  E.PK, E.ENTITY_ID, E.ENTITY_TYPE, E1.ENTITY_ID, E1.ENTITY_TYPE, E.LABEL, E.SHORT_LABEL, E.PARTITION_NUMBER, E.ORIGIN, E.SOURCE, E.CREATION_DATE, E.UPDATE_DATE, TJ.BELONGS_TO"
			+ " FROM T_REPORTING_ENTITY E"
			+ " LEFT OUTER JOIN T_REPORTING_ENTITY E1 ON E.REPORTING_ENTITY_PARENT_FK=E1.PK"
			+ " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ ON E.PK=TJ.REPORTING_ENTITY_FK AND TJ.REPORTING_GROUP_FK=";

	private static final String FULL_ENTITY_QUERY_END = " ORDER BY E.ENTITY_TYPE ASC, E.PK ASC;";

	private static final String PARENT_LINK_ENTITY_QUERY_START = "select  E2.PK, E.ENTITY_ID, E.ENTITY_TYPE, E2.ENTITY_TYPE, E2.ORIGIN, L.ROLE, L.TYPE, L.PARAMETER, IF((L.TYPE,E2.ENTITY_TYPE) IN (";

	private static final String PARENT_LINK_ENTITY_QUERY_MIDDLE_FIRST = "),true,false) FROM T_REPORTING_ENTITY E"
			+ " JOIN T_ENTITY_LINK L ON E.PK=L.REPORTING_ENTITY_SRC_FK"
			+ " JOIN T_REPORTING_ENTITY E2 ON L.REPORTING_ENTITY_DEST_FK=E2.PK"
			+ " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ ON E.PK=TJ.REPORTING_ENTITY_FK AND TJ.REPORTING_GROUP_FK=";

	private static final String PARENT_LINK_ENTITY_QUERY_MIDDLE_SECOND = " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ2 ON E2.PK=TJ2.REPORTING_ENTITY_FK AND TJ2.REPORTING_GROUP_FK=";

	private static final String PARENT_LINK_ENTITY_QUERY_END = " ORDER BY E2.ENTITY_TYPE ASC, E2.PK ASC";

	private static final String CHILD_LINK_ENTITY_QUERY_START = "select  E.PK, E2.ENTITY_ID, E2.ENTITY_TYPE, E.ENTITY_TYPE, E.ORIGIN, L.ROLE, L.TYPE, L.PARAMETER, IF((L.TYPE,E.ENTITY_TYPE) IN (";

	private static final String CHILD_LINK_ENTITY_QUERY_MIDDLE_FIRST = "),true,false) FROM T_REPORTING_ENTITY E"
			+ " JOIN T_ENTITY_LINK L ON E.PK=L.REPORTING_ENTITY_DEST_FK"
			+ " JOIN T_REPORTING_ENTITY E2 ON L.REPORTING_ENTITY_SRC_FK=E2.PK"
			+ " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ ON E.PK=TJ.REPORTING_ENTITY_FK AND TJ.REPORTING_GROUP_FK=";

	private static final String CHILD_LINK_ENTITY_QUERY_MIDDLE_SECOND = " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ2 ON E2.PK=TJ2.REPORTING_ENTITY_FK AND TJ2.REPORTING_GROUP_FK=";

	private static final String CHILD_LINK_ENTITY_QUERY_END = " ORDER BY E.ENTITY_TYPE ASC, E.PK ASC";

	private static final String CHILD_LINK_ATTRIBUTE_QUERY_START = "select  E.PK, E2.ENTITY_ID, E2.ENTITY_TYPE, E.ENTITY_TYPE, E.ORIGIN, L.ROLE, L.TYPE, L.PARAMETER, P.NAME, P.VALUE, IF((L.TYPE,E.ENTITY_TYPE) IN (";

	private static final String CHILD_LINK_ATTRIBUTE_QUERY_MIDDLE_FIRST = "),true,false) FROM T_REPORTING_ENTITY E"
			+ " JOIN T_ENTITY_LINK L ON E.PK=L.REPORTING_ENTITY_DEST_FK"
			+ " JOIN T_REPORTING_ENTITY E2 ON L.REPORTING_ENTITY_SRC_FK=E2.PK"
			+ " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ ON E.PK=TJ.REPORTING_ENTITY_FK AND TJ.REPORTING_GROUP_FK=";

	private static final String CHILD_LINK_ATTRIBUTE_QUERY_MIDDLE_SECOND = " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ2 ON E2.PK=TJ2.REPORTING_ENTITY_FK AND TJ2.REPORTING_GROUP_FK=";

	private static final String CHILD_LINK_ATTRIBUTE_QUERY_END = " JOIN " + EntityLinkAttribute.TABLE_NAME + " P "
			+ " ON P." + EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_DEST_FK + "=L."
			+ EntityLinkId.COL_NAME_REPORTING_ENTITY_DEST_FK + " AND P."
			+ EntityLinkAttributeId.COL_NAME_REPORTING_ENTITY_SRC_FK + "=L."
			+ EntityLinkId.COL_NAME_REPORTING_ENTITY_SRC_FK + " AND P." + EntityLinkAttributeId.COL_NAME_ROLE + "=L."
			+ EntityLinkId.COL_NAME_ROLE + " ORDER BY E." + ReportingEntity.COL_NAME_ENTITY_TYPE + " ASC, E."
			+ ReportingEntity.COL_NAME_PK + " ASC";

	private static final String ENTITY_ATTRIBUTE_QUERY_START = "select  E.PK, P.NAME, P.VALUE, P.PK from T_ENTITY_ATTRIBUTE P"
			+ " JOIN T_REPORTING_ENTITY E ON P.REPORTING_ENTITY_FK=E.PK"
			+ " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ ON E.PK=TJ.REPORTING_ENTITY_FK AND TJ.REPORTING_GROUP_FK=";

	private static final String ENTITY_ATTRIBUTE_QUERY_END = " ORDER BY E.ENTITY_TYPE ASC, E.PK ASC, P.PK DESC";

	private static final String ENTITY_GROUP_ATTRIBUTE_QUERY_START = "SELECT E.PK, P.NAME, P.VALUE FROM T_ENTITY_GROUP_ATTRIBUTE P"
			+ " JOIN T_REPORTING_ENTITY E ON P.REPORTING_ENTITY_FK=E.PK"
			+ " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ ON E.PK=TJ.REPORTING_ENTITY_FK AND TJ.REPORTING_GROUP_FK=P.REPORTING_GROUP_FK AND TJ.REPORTING_GROUP_FK=";

	private static final String ENTITY_GROUP_ATTRIBUTE_QUERY_END = " ORDER BY E.ENTITY_TYPE ASC, E.PK ASC";

	private static final String ENTITY_TYPE_AND_SUBTYPE_QUERY_START = "select TJTAS.REPORTING_ENTITY_FK, TJTAS.SUBTYPE"
			+ " FROM TJ_REPORTING_ENTITY_TO_TYPE_AND_SUBTYPE TJTAS"
			+ " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJGE ON TJTAS.REPORTING_ENTITY_FK=TJGE.REPORTING_ENTITY_FK"
			+ " AND TJGE.REPORTING_GROUP_FK=";
	private static final String ENTITY_TYPE_AND_SUBTYPE_QUERY_END = " ORDER BY TJTAS.TYPE ASC, TJTAS.REPORTING_ENTITY_FK ASC";

	private static final String ENTITY_LIST_ATTRIBUTE_QUERY_START = "select  E.PK, P.NAME, P.VALUE, E.ENTITY_TYPE, E.ORIGIN from T_ENTITY_ATTRIBUTE_LIST P"
			+ " JOIN T_REPORTING_ENTITY E ON P.REPORTING_ENTITY_FK=E.PK "
			+ " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ ON E.PK=TJ.REPORTING_ENTITY_FK AND TJ.REPORTING_GROUP_FK=";

	private static final String ENTITY_LIST_ATTRIBUTE_QUERY_END = " ORDER BY E.ENTITY_TYPE ASC, E.PK ASC";

	private static final String DATA_LOCATION_ENTITY_QUERY_START = "select  E.PK, D.CRITERIA_VALUE, D.LOCATION_PATTERN from T_DATA_LOCATION D"
			+ " JOIN T_REPORTING_ENTITY E ON D.REPORTING_ENTITY_FK=E.PK"
			+ " JOIN TJ_REPORTING_GROUP_TO_ENTITIES TJ ON E.PK=TJ.REPORTING_ENTITY_FK AND TJ.REPORTING_GROUP_FK=";

	private static final String DATA_LOCATION_ENTITY_QUERY_END = " ORDER BY E.ENTITY_TYPE ASC, E.PK ASC";

	public Connection openRefReportConnection() throws SQLException {
		refReportConnection = dataSource.getConnection();
		return refReportConnection;
	}

	public void closeRefReportConnection() throws SQLException {
		if (refReportConnection != null) {
			refReportConnection.close();
		}
	}

	public EntityWithIdAndTypeResultSet getEntityWithIdAndType(Connection refReportConnection) throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();

		ResultSet result = statement.executeQuery(ENTITY_WITH_ID_AND_TYPE_QUERY);
		return new EntityWithIdAndTypeResultSet(result);
	}

	public LinkWithExtremityIdAndOriginResultSet getLinkWithExtremityIdAndOriginResultSet() throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();
		ResultSet result = statement.executeQuery(ENTITY_LINKS_WITH_ID_AND_ORIGIN);
		return new LinkWithExtremityIdAndOriginResultSet(result);
	}

	public EntityAndParentWithIdAndTypeResultSet getEntityAndParentWithIdAndType() throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();
		ResultSet result = statement.executeQuery(ENTITY_WITH_PARENT_QUERY);
		return new EntityAndParentWithIdAndTypeResultSet(result);
	}

	public EntityResultSet getEntityForGroupOrderedByTypeAndPk(Long reportingGroupPk) throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();
		ResultSet result = statement.executeQuery(FULL_ENTITY_QUERY_START + reportingGroupPk + FULL_ENTITY_QUERY_END);
		return new EntityResultSet(result);
	}

	public LinkResultSet getChildLinkEntityForGroupOrderedByTypeAndPk(Long reportingGroupPk,
			Connection refReportConnection, Map<String, Set<String>> complexLinks) throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();
		StringBuilder builder = new StringBuilder(CHILD_LINK_ENTITY_QUERY_START);

		boolean first = true;
		for (String complexLink : complexLinks.keySet()) {
			if (!first)
				builder.append(',');
			else
				first = false;

			boolean first2 = true;
			for (String target : complexLinks.get(complexLink)) {
				if (!first2)
					builder.append(',');
				else
					first2 = false;

				builder.append("('" + complexLink + "','" + target + "')");
			}
		}

		builder.append(CHILD_LINK_ENTITY_QUERY_MIDDLE_FIRST).append(reportingGroupPk)
				.append(CHILD_LINK_ENTITY_QUERY_MIDDLE_SECOND).append(reportingGroupPk)
				.append(CHILD_LINK_ENTITY_QUERY_END);
		ResultSet result = statement.executeQuery(builder.toString());
		LinkResultSet linkResultSet = new LinkResultSet(result);
		return linkResultSet;
	}

	public LinkResultSet getParentLinkEntityForGroupOrderedByTypeAndPk(Long reportingGroupPk,
			Map<String, Set<String>> complexLinks) throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();
		StringBuilder builder = new StringBuilder(PARENT_LINK_ENTITY_QUERY_START);

		boolean first = true;
		for (String complexLink : complexLinks.keySet()) {
			if (!first)
				builder.append(',');
			else
				first = false;

			boolean first2 = true;
			for (String target : complexLinks.get(complexLink)) {
				if (!first2)
					builder.append(',');
				else
					first2 = false;

				builder.append("('" + complexLink + "','" + target + "')");
			}
		}

		builder.append(PARENT_LINK_ENTITY_QUERY_MIDDLE_FIRST).append(reportingGroupPk)
				.append(PARENT_LINK_ENTITY_QUERY_MIDDLE_SECOND).append(reportingGroupPk)
				.append(PARENT_LINK_ENTITY_QUERY_END);
		ResultSet result = statement.executeQuery(builder.toString());
		LinkResultSet linkResultSet = new LinkResultSet(result);
		return linkResultSet;
	}

	public EntityAttributeResultSet getEntityAttributeForEntityForGroupOrderedByTypeAndPk(Long reportingGroupPk)
			throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();

		String query = ENTITY_ATTRIBUTE_QUERY_START + reportingGroupPk + ENTITY_ATTRIBUTE_QUERY_END;
		LOGGER.trace("query to get EntityAttributes: " + query);
		ResultSet result = statement.executeQuery(query);
		EntityAttributeResultSet entityAttributeResultSet = new EntityAttributeResultSet(result);
		return entityAttributeResultSet;
	}

	public EntityGroupAttributeResultSet getEntityGroupAttributeForEntityForGroupOrderedByEntityPk(
			Long reportingGroupPk) throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();

		ResultSet result = statement
				.executeQuery(ENTITY_GROUP_ATTRIBUTE_QUERY_START + reportingGroupPk + ENTITY_GROUP_ATTRIBUTE_QUERY_END);
		EntityGroupAttributeResultSet entityGroupAttributeResultSet = new EntityGroupAttributeResultSet(result);
		return entityGroupAttributeResultSet;
	}

	public EntitySubtypeResultSet getEntitySubtypeForEntityForGroupOrderedByTypeAndPk(Long reportingGroupPk)
			throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();

		ResultSet result = statement.executeQuery(
				ENTITY_TYPE_AND_SUBTYPE_QUERY_START + reportingGroupPk + ENTITY_TYPE_AND_SUBTYPE_QUERY_END);
		EntitySubtypeResultSet entitySubtypeResultSet = new EntitySubtypeResultSet(result);
		return entitySubtypeResultSet;
	}

	public EntityListAttributeResultSet getEntityListAttributeForEntityForGroupOrderedByTypeAndPk(Long reportingGroupPk)
			throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();

		ResultSet result = statement
				.executeQuery(ENTITY_LIST_ATTRIBUTE_QUERY_START + reportingGroupPk + ENTITY_LIST_ATTRIBUTE_QUERY_END);
		EntityListAttributeResultSet entityListAttributeResultSet = new EntityListAttributeResultSet(result);
		return entityListAttributeResultSet;
	}

	public DataLocationResultSet getDataLocationForEntityForGroupOrderedByTypeAndPk(Long reportingGroupPk)
			throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();

		ResultSet result = statement
				.executeQuery(DATA_LOCATION_ENTITY_QUERY_START + reportingGroupPk + DATA_LOCATION_ENTITY_QUERY_END);
		DataLocationResultSet dataLocationResultSet = new DataLocationResultSet(result);
		return dataLocationResultSet;
	}

	public class EntityWithIdAndTypeResultSet extends ResultSetProxy {
		public EntityWithIdAndTypeResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		public String getEntityId() throws SQLException {
			return resultSet.getString(1);
		}

		public String getEntityType() throws SQLException {
			return resultSet.getString(2);
		}
	}

	public class LinkWithExtremityIdAndOriginResultSet extends ResultSetProxy {
		public LinkWithExtremityIdAndOriginResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		public String getParameter() throws SQLException {
			return resultSet.getString(1);
		}

		public String getRole() throws SQLException {
			return resultSet.getString(2);
		}

		public String getSourceId() throws SQLException {
			return resultSet.getString(3);
		}

		public String getSourceType() throws SQLException {
			return resultSet.getString(4);
		}

		public String getSourceOrigin() throws SQLException {
			return resultSet.getString(5);
		}

		public String getDestinationId() throws SQLException {
			return resultSet.getString(6);
		}

		public String getDestinationType() throws SQLException {
			return resultSet.getString(7);
		}

		public String getDestinationOrigin() throws SQLException {
			return resultSet.getString(8);
		}

	}

	public class EntityAndParentWithIdAndTypeResultSet extends ResultSetProxy {
		public EntityAndParentWithIdAndTypeResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		public String getEntityId() throws SQLException {
			return resultSet.getString(1);
		}

		public String getEntityType() throws SQLException {
			return resultSet.getString(2);
		}

		public String getEntityOrigin() throws SQLException {
			return resultSet.getString(3);
		}

		public String getEntityParentId() throws SQLException {
			return resultSet.getString(4);
		}

		public String getEntityParentType() throws SQLException {
			return resultSet.getString(5);
		}

		public String getEntityParentOrigin() throws SQLException {
			return resultSet.getString(6);
		}

	}

	public class EntityResultSet extends ResultSetProxy {
		public EntityResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		public Long getPk() throws SQLException {
			return resultSet.getLong(1);
		}

		public String getEntityId() throws SQLException {
			return resultSet.getString(2);
		}

		public String getEntityType() throws SQLException {
			return resultSet.getString(3);
		}

		public String getParentId() throws SQLException {
			return resultSet.getString(4);
		}

		public String getParentType() throws SQLException {
			return resultSet.getString(5);
		}

		public String getLabel() throws SQLException {
			return resultSet.getString(6);
		}

		public String getShortLabel() throws SQLException {
			return resultSet.getString(7);
		}

		public String getPartitionNumber() throws SQLException {
			return resultSet.getString(8);
		}

		public String getOrigin() throws SQLException {
			return resultSet.getString(9);
		}

		public String getSource() throws SQLException {
			return resultSet.getString(10);
		}

		public Timestamp getCreationDate() throws SQLException {
			return resultSet.getTimestamp(11);
		}

		public Timestamp getUpdateDate() throws SQLException {
			return resultSet.getTimestamp(12);
		}

		public boolean getBelongsTo() throws SQLException {
			return resultSet.getBoolean(13);
		}

	}

	public class EntityAttributeResultSet extends ScrollableInsertionForEntityResultSetProxy {

		public EntityAttributeResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		public String getName() throws SQLException {
			return resultSet.getString(2);
		}

		public String getValue() throws SQLException {
			return resultSet.getString(3);
		}

		@Override
		public void insertData(EntityInfoParameter entityParameter,
				StatementWithColumnNameAdapter entityStatementWithColumnNameAdapter,
				Map<String, EntityTypeH2StatementAndInsertionInfo> StatementByTableNameMap) throws SQLException {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Add attribut(name=" + getName() + ";value=" + getValue() + ") for entity pk '" + getPk()
						+ "'");
			}
			entityStatementWithColumnNameAdapter.setValue(getName(), getValue());
		}

	}

	public class EntityGroupAttributeResultSet extends ScrollableInsertionForEntityResultSetProxy {

		public EntityGroupAttributeResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		public String getName() throws SQLException {
			return resultSet.getString(2);
		}

		public String getValue() throws SQLException {
			return resultSet.getString(3);
		}

		@Override
		public void insertData(EntityInfoParameter entityParameter,
				StatementWithColumnNameAdapter entityStatementWithColumnNameAdapter,
				Map<String, EntityTypeH2StatementAndInsertionInfo> StatementByTableNameMap) throws SQLException {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Add entityGroupAttribut(name=" + getName() + ";value=" + getValue() + ") for entity pk '"
						+ getPk() + "'");
			}
			entityStatementWithColumnNameAdapter.setValue(getName(), getValue());
		}

	}

	public class EntityLinkAttributeResultSet extends ScrollableInsertionForEntityResultSetProxy {

		public EntityLinkAttributeResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		@Override
		public Long getPk() throws SQLException {
			return resultSet.getLong(1);
		}

		public String getLinkedEntityId() throws SQLException {
			return resultSet.getString(2);
		}

		public String getLinkedEntityType() throws SQLException {
			return resultSet.getString(3);
		}

		public String getEntityType() throws SQLException {
			return resultSet.getString(4);
		}

		public String getEntityOrigin() throws SQLException {
			return resultSet.getString(5);
		}

		public String getRole() throws SQLException {
			return resultSet.getString(6);
		}

		public String getType() throws SQLException {
			return resultSet.getString(7);
		}

		public String getParameter() throws SQLException {
			return resultSet.getString(8);
		}

		public String getAdditionalParameter() throws SQLException {
			return resultSet.getString(9);
		}

		public String getAdditionalValue() throws SQLException {
			return resultSet.getString(10);
		}

		public Boolean isComplex() throws SQLException {
			return resultSet.getBoolean(11);
		}

		/*
		 * Insert addtional parameter in a link table between entities Only for complex links
		 */
		@Override
		public void insertData(EntityInfoParameter entityParameter,
				StatementWithColumnNameAdapter entityStatementWithColumnNameAdapter,
				Map<String, EntityTypeH2StatementAndInsertionInfo> statementByTableNameMap) throws SQLException {
			if (isComplex()) {
				String tableName = BDStatCommonFunction.buildLinkTableName(getType(), getEntityType());

				EntityTypeH2StatementAndInsertionInfo info = statementByTableNameMap.get(tableName);

				UpdateStatementAdapter adapter = info.getUpdateStatement(getAdditionalParameter());
				if (adapter != null) {
					adapter.setStringInSetStatement(getAdditionalParameter(), getAdditionalValue());

					adapter.setStringInWhereStatement(H2Table.Link.COLUMN_SOURCE_ENTITY_ID, getLinkedEntityId());
					adapter.setStringInWhereStatement(H2Table.Link.COLUMN_ORIGIN, getEntityOrigin());
					adapter.setStringInWhereStatement(H2Table.Link.COLUMN_DESTINATION_ENTITY_ID,
							entityParameter.entityId);
					adapter.setStringInWhereStatement(H2Table.Link.COLUMN_ROLE, getRole());

					adapter.execute();
				}
			}
		}
	}

	public class EntitySubtypeResultSet extends ScrollableInsertionForEntityResultSetProxy {

		public EntitySubtypeResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		public String getSubtype() throws SQLException {
			return resultSet.getString(2);
		}

		@Override
		public void insertData(EntityInfoParameter entityParameter,
				StatementWithColumnNameAdapter entityStatementWithColumnNameAdapter,
				Map<String, EntityTypeH2StatementAndInsertionInfo> StatementByTableNameMap) throws SQLException {
			String subtype = getSubtype();
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Add subtype (" + subtype + ") for entity pk '" + getPk() + "'");
			}

			EntityTypeH2StatementAndInsertionInfo filterinfo = StatementByTableNameMap
					.get(BDStatCommonFunction.buildEntityFilterTableName(subtype));
			if (filterinfo != null) {
				StatementWithColumnNameAdapter statement = filterinfo.getStatement();
				statement.setValue(H2Table.EntityFilter.COLUMN_FILTER_JOIN, entityParameter.inventoryPk);
				statement.execute();
				filterinfo.setTablePk(filterinfo.getTablePk() + 1);
			}
		}
	}

	public class EntityListAttributeResultSet extends ScrollableInsertionForEntityResultSetProxy {

		private String oldListAttributeTableName = "";
		private StatementWithColumnNameAdapter adapter;
		private EntityTypeH2StatementAndInsertionInfo info;

		public EntityListAttributeResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		public String getName() throws SQLException {
			return resultSet.getString(2);
		}

		public String getValue() throws SQLException {
			return resultSet.getString(3);
		}

		public String getEntityType() throws SQLException {
			return resultSet.getString(4);
		}

		public String getOrigin() throws SQLException {
			return resultSet.getString(5);
		}

		@Override
		public void insertData(EntityInfoParameter entityParameter,
				StatementWithColumnNameAdapter entityStatementWithColumnNameAdapter,
				Map<String, EntityTypeH2StatementAndInsertionInfo> statementByTableNameMap) throws SQLException {

			String tableName = BDStatCommonFunction.buildListTableName(getEntityType(), getName().toUpperCase());
			if (!oldListAttributeTableName.equalsIgnoreCase(tableName)) {
				info = statementByTableNameMap.get(tableName);

				oldListAttributeTableName = tableName;

				adapter = info.getStatement();
			}

			Unmarshaller unmarshaller;
			ListAttributeValueParameterList listAttributeValueParameterList = null;
			try {
				unmarshaller = JAXBRefReportFactory.getUnmarshaller();

				listAttributeValueParameterList = (ListAttributeValueParameterList) unmarshaller
						.unmarshal(new StringReader(getValue()));

			} catch (JAXBException e) {
				throw new SQLException(e);
			}

			for (ListAttributeValueParameter listAttributeValueParameter : listAttributeValueParameterList
					.getValueList()) {

				Long pk = info.getTablePk();
				adapter.setValue(BDStatCommonFunction.makePk(tableName), pk);
				adapter.setValue(H2Table.AttributeList.COLUMN_ENTITY_FK, entityParameter.inventoryPk);
				adapter.setValue(H2Table.AttributeList.COLUMN_ENTITY_TYPE, getEntityType());

				for (ListAttributeElementParameter element : listAttributeValueParameter.element) {
					adapter.setValue(element.name, element.value);
				}

				adapter.execute();

				info.setTablePk(pk + 1);
			}

		}

	}

	public class DataLocationResultSet extends NameValuePairForEntityResultSetProxy {
		public DataLocationResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		public String getDataLocationValue() throws SQLException {
			return resultSet.getString(2);
		}

		public String getLocationPattern() throws SQLException {
			return resultSet.getString(3);
		}

		@Override
		public String getColumnName() throws SQLException {
			return BDStatCommonFunction.makeDataLocationColumnName(getDataLocationValue());
		}

		@Override
		public String getColumnValue() throws SQLException {
			return getLocationPattern();
		}

	}

	public class LinkResultSet extends ScrollableInsertionForEntityResultSetProxy {

		private Long amountOfComplexLink = 0L;
		private Long amountOfSimpleLink = 0L;

		public LinkResultSet(ResultSet resultSet) {
			super(resultSet);
		}

		@Override
		public Long getPk() throws SQLException {
			return resultSet.getLong(1);
		}

		public String getLinkedEntityId() throws SQLException {
			return resultSet.getString(2);
		}

		public String getLinkedEntityType() throws SQLException {
			return resultSet.getString(3);
		}

		public String getEntityType() throws SQLException {
			return resultSet.getString(4);
		}

		public String getEntityOrigin() throws SQLException {
			return resultSet.getString(5);
		}

		public String getRole() throws SQLException {
			return resultSet.getString(6);
		}

		public String getType() throws SQLException {
			return resultSet.getString(7);
		}

		public String getParameter() throws SQLException {
			return resultSet.getString(8);
		}

		public Boolean isComplex() throws SQLException {
			return resultSet.getBoolean(9);
		}

		@Override
		public void insertData(EntityInfoParameter entityParameter,
				StatementWithColumnNameAdapter statementWithColumnNameAdapter,
				Map<String, EntityTypeH2StatementAndInsertionInfo> statementByTableNameMap) throws SQLException {
			if (LOGGER.isTraceEnabled()) {
				String linkType;
				if (isComplex()) {
					linkType = "Complex";
				} else {
					linkType = "Single";
				}
				LOGGER.trace("Entity pk '" + getPk() + "' of type " + getEntityType() + " link to "
						+ getLinkedEntityId() + " of type " + getLinkedEntityType() + " -> link is " + linkType);
			}
			if (!isComplex()) {
				amountOfSimpleLink++;
				statementWithColumnNameAdapter.setValue(getLinkedEntityType(), getLinkedEntityId());
			} else {
				amountOfComplexLink++;
				String tableName = BDStatCommonFunction.buildLinkTableName(getType(), getEntityType());

				EntityTypeH2StatementAndInsertionInfo info = statementByTableNameMap.get(tableName);
				Long pk = info.getTablePk();

				StatementWithColumnNameAdapter adapter = info.getStatement();
				adapter.setValue(BDStatCommonFunction.makePk(tableName), pk);
				adapter.setValue(H2Table.Link.COLUMN_DESTINATION_ENTITY_ID, entityParameter.entityId);
				adapter.setValue(H2Table.Link.COLUMN_ORIGIN, getEntityOrigin());
				adapter.setValue(H2Table.Link.COLUMN_SOURCE_ENTITY_ID, getLinkedEntityId());
				adapter.setValue(H2Table.Link.COLUMN_ROLE, getRole());
				adapter.setValue(H2Table.Link.COLUMN_PARAMETER, getParameter());
				adapter.execute();

				info.setTablePk(pk + 1);
			}
		}

		public Long getAmountOfComplexLink() {
			return amountOfComplexLink;
		}

		public Long getAmountOfSimpleLink() {
			return amountOfSimpleLink;
		}

	}

	private class ResultSetProxy {
		protected Long numberOfElements = 0L;
		protected ResultSet resultSet;

		public ResultSetProxy(ResultSet resultSet) {
			super();
			this.resultSet = resultSet;
		}

		public boolean next() throws SQLException {
			numberOfElements++;
			return resultSet.next();
		}

		public boolean isAfterLast() throws SQLException {
			return resultSet.isAfterLast();
		}

		public Long getNumberOfElements() {
			return numberOfElements;
		}

		public void beforeFirst() throws SQLException {
			resultSet.beforeFirst();
		}

		public void close(SOAContext context) {
			try {
				resultSet.close();
			} catch (SQLException sqle) {
				LOGGER.warn(SOATools.buildSOALogMessage(context, "Cannot close ResultSet"));
			}
		}
	}

	private abstract class ScrollableInsertionForEntityResultSetProxy extends ResultSetProxy {
		private boolean insertionHasNotStarted;
		private boolean resultSetIsNotEmpty;
		protected Double avgNumberOfElements = 0D;

		protected Long maxNumberOfElements = 0L;
		protected Long numberOfPK = 0L;

		public ScrollableInsertionForEntityResultSetProxy(ResultSet resultSet) {
			super(resultSet);
			insertionHasNotStarted = true;
			resultSetIsNotEmpty = true;
		}

		public Long getPk() throws SQLException {
			return resultSet.getLong(1);
		}

		public void insertAllNameValueForEntity(StatementWithColumnNameAdapter statementWithColumnNameAdapter,
				EntityInfoParameter entityParameter,
				Map<String, EntityTypeH2StatementAndInsertionInfo> statementByTableNameMap) throws SQLException {
			if (insertionHasNotStarted) {
				resultSetIsNotEmpty = resultSet.next();
				insertionHasNotStarted = false;
			}

			if (resultSetIsNotEmpty && !isAfterLast()) {
				boolean isSetScrollToEntityParam = getPk().equals(entityParameter.entityPk);
				numberOfElements++;
				numberOfPK++;
				Long nbElementsScrolled = 0L;
				while (isSetScrollToEntityParam) {
					insertData(entityParameter, statementWithColumnNameAdapter, statementByTableNameMap);
					if (resultSet.next()) {
						numberOfElements++;
						nbElementsScrolled++;
						isSetScrollToEntityParam = getPk().equals(entityParameter.entityPk);
					} else {
						isSetScrollToEntityParam = false;
					}
				}

				if (nbElementsScrolled > maxNumberOfElements) {
					maxNumberOfElements = nbElementsScrolled;
				}

				avgNumberOfElements = (double) numberOfElements / (double) numberOfPK;
			}

		}

		public abstract void insertData(EntityInfoParameter entityParameter,
				StatementWithColumnNameAdapter statementWithColumnNameAdapter,
				Map<String, EntityTypeH2StatementAndInsertionInfo> statementByTableNameMap) throws SQLException;

		public Double getAvgNumberOfElements() {
			return avgNumberOfElements;
		}

		public Long getMaxNumberOfElements() {
			return maxNumberOfElements;
		}

		public Long getNumberOfPK() {
			return numberOfPK;
		}

	}

	private abstract class NameValuePairForEntityResultSetProxy extends ScrollableInsertionForEntityResultSetProxy {
		public NameValuePairForEntityResultSetProxy(ResultSet resultSet) {
			super(resultSet);
		}

		public abstract String getColumnName() throws SQLException;

		public abstract String getColumnValue() throws SQLException;

		@Override
		public void insertData(EntityInfoParameter entityParameter,
				StatementWithColumnNameAdapter entityStatementWithColumnNameAdapter,
				Map<String, EntityTypeH2StatementAndInsertionInfo> StatementByTableNameMap) throws SQLException {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Add column(name=" + getColumnName() + ";value=" + getColumnValue() + ") for entity pk '"
						+ getPk() + "'");
			}
			entityStatementWithColumnNameAdapter.setValue(getColumnName(), getColumnValue());
		}
	}

	public EntityLinkAttributeResultSet getEntityLinkAttributeForEntityForGroupOrderedByTypeAndPk(Long reportingGroupPk,
			Map<String, Set<String>> complexLinksWithParams) throws SQLException {
		Statement statement = refReportConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Configuration.jdbcProvisioningFetchSize);
		statement.closeOnCompletion();
		StringBuilder builder = new StringBuilder(CHILD_LINK_ATTRIBUTE_QUERY_START);

		boolean first = true;
		for (String complexLink : complexLinksWithParams.keySet()) {
			if (!first)
				builder.append(',');
			else
				first = false;

			boolean first2 = true;
			for (String target : complexLinksWithParams.get(complexLink)) {
				if (!first2)
					builder.append(',');
				else
					first2 = false;

				builder.append("('" + complexLink + "','" + target + "')");
			}
		}

		builder.append(CHILD_LINK_ATTRIBUTE_QUERY_MIDDLE_FIRST).append(reportingGroupPk)
				.append(CHILD_LINK_ATTRIBUTE_QUERY_MIDDLE_SECOND).append(reportingGroupPk)
				.append(CHILD_LINK_ATTRIBUTE_QUERY_END);
		ResultSet result = statement.executeQuery(builder.toString());

		EntityLinkAttributeResultSet entityLinkAttributeResultSet = new EntityLinkAttributeResultSet(result);
		return entityLinkAttributeResultSet;
	}

}

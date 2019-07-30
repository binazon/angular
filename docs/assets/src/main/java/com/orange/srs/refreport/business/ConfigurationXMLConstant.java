package com.orange.srs.refreport.business;

public class ConfigurationXMLConstant {

	/*
	 * ClientStat
	 */
	public final static String CLIENTSTAT_NODE_CLIENT = "client";
	public final static String CLIENTSTAT_NODE_LABEL = "label";
	public final static String CLIENTSTAT_NODE_CUSTOMER_CODE = "customerCode";
	public final static String CLIENTSTAT_NODE_CODE = "code";
	public final static String CLIENTSTAT_NODE_DU = "DU";
	public final static String CLIENTSTAT_NODE_DUNAME = "DUname";
	public final static String CLIENTSTAT_NODE_SERVICE_TYPE = "serviceType";
	public final static String CLIENTSTAT_NODE_SERVICE_TYPE_LABEL = "serviceTypeLabel";
	public final static String CLIENTSTAT_NODE_IS_3G = "is3G";
	public final static String CLIENTSTAT_NODE_FILTER_ID = "filterId";

	/*
	 * InventoryConfig
	 */
	public final static String INVENTORYCONFIG_NODE_REPORTING_GROUP_TABLE = "reportingGroupTable";
	public final static String INVENTORYCONFIG_NODE_INVENTORY_VERSION = "inventoryVersion";
	public final static String INVENTORYCONFIG_NODE_OPENSTAT_VERSION = "openStatVersion";
	public final static String INVENTORYCONFIG_NODE_REPORTING_ENTITY_TABLE = "reportingEntityTable";
	public final static String INVENTORYCONFIG_NODE_LINK = "link";
	public final static String INVENTORYCONFIG_NODE_FILTER = "filter";
	public final static String INVENTORYCONFIG_NODE_ATTRIBUTE = "attribute";
	public final static String INVENTORYCONFIG_NODE_GROUPATTRIBUTE = "groupAttribute";
	public final static String INVENTORYCONFIG_NODE_LIST_ATTRIBUTE = "listAttribute";
	public final static String INVENTORYCONFIG_NODE_LINKATTRIBUTE = "linkAttribute";
	public final static String INVENTORYCONFIG_NODE_LIST_ATTRIBUTE_PARAMETER = "parameter";
	public final static String INVENTORYCONFIG_ATTRIBUTE_TYPE = "type";
	public final static String INVENTORYCONFIG_ATTRIBUTE_NAME = "name";
	public final static String INVENTORYCONFIG_ATTRIBUTE_TARGET = "target";
	public final static String INVENTORYCONFIG_ATTRIBUTE_INDEX = "index";
	public final static String INVENTORYCONFIG_ATTRIBUTE_DEFAULT = "default";

	public static enum EntityLinkType {
		SINGLE("single"), COMPLEX("complex"), PARENT("parent");
		private String value;

		EntityLinkType(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	/*
	 * Provisioning files
	 */
	public final static String PROVISIONINGFILE_NODE_LIST_PROVISIONING_SOURCE_TYPE = "listProvisioningSourceTypes";
	public final static String PROVISIONINGFILE_NODE_PROVISIONING_SOURCE_TYPE = "provisioningSourceType";
	public final static String PROVISIONINGFILE_NODE_SOURCE_TYPE = "SourceType";
	public final static String PROVISIONINGFILE_NODE_LIST_PROVISIONING_SOURCE = "listProvisioningSource";
	public final static String PROVISIONINGFILE_NODE_PROVISIONING_SOURCE = "provisioningSource";
	public final static String PROVISIONINGFILE_NODE_SOURCE_NAME = "sourceName";
	public final static String PROVISIONINGFILE_NODE_PROTOCOL = "protocol";
	public final static String PROVISIONINGFILE_NODE_USER_NAME = "userName";
	public final static String PROVISIONINGFILE_NODE_HOST = "host";
	public final static String PROVISIONINGFILE_NODE_PASSWORD = "password";
	public final static String PROVISIONINGFILE_NODE_LIST_FILES = "listProvisioningFiles";
	public final static String PROVISIONINGFILE_NODE_FILE = "provisioningFile";
	public final static String PROVISIONINGFILE_NODE_FILE_NAME = "fileName";
	public final static String PROVISIONINGFILE_NODE_FILE_PATH = "filePath";
	public final static String PROVISIONINGFILE_NODE_FILE_EXTENSION = "fileExtension";
	public final static String PROVISIONINGFILE_NODE_FILE_PATTERN = "filePattern";
	public final static String PROVISIONINGFILE_NODE_ORIGIN = "origin";

}

CREATE DATABASE  IF NOT EXISTS `refreport` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `refreport`;
-- MySQL dump 10.13  Distrib 5.5.16, for Win32 (x86)
--
-- Host: localhost    Database: refreport
-- ------------------------------------------------------
-- Server version	5.5.17

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_proxy`
--

DROP TABLE IF EXISTS `t_proxy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_proxy` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `ISSSL` bit(1) DEFAULT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `URI` tinyblob NOT NULL,
  `VERSION` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_report`
--

DROP TABLE IF EXISTS `t_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_report` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `COMPUTE_URI` varchar(255) NOT NULL,
  `GRANULARITY` varchar(255) NOT NULL,
  `LABEL` varchar(255) NOT NULL,
  `REF_ID` varchar(255) NOT NULL,
  `REPORT_TIME_UNIT` varchar(255) NOT NULL,
  PRIMARY KEY (`PK`)
) ENGINE=InnoDB AUTO_INCREMENT=438 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_report_input`
--

DROP TABLE IF EXISTS `t_report_input`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_report_input` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `GRANULARITY` varchar(255) NOT NULL,
  `LOCATION_PATTERN_PREFIX` varchar(255) DEFAULT NULL,
  `LOCATION_PATTERN_SUFFIX` varchar(255) DEFAULT NULL,
  `REPORT_INPUT_REF` varchar(255) NOT NULL,
  `SOURCE` varchar(255) NOT NULL,
  `SOURCE_TIME_UNIT` varchar(255) NOT NULL,
  `TABLE_DB` varchar(255) DEFAULT NULL,
  `TYPE_DB` varchar(255) DEFAULT NULL,
  `INPUT_FORMAT_FK` bigint(20) DEFAULT NULL,
  `SOURCE_CLASS_FK` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  UNIQUE KEY `GRANULARITY` (`GRANULARITY`,`REPORT_INPUT_REF`,`SOURCE_TIME_UNIT`),
  KEY `FK97F3E6EAC6A81280` (`INPUT_FORMAT_FK`),
  KEY `FK97F3E6EA77E3ECF4` (`SOURCE_CLASS_FK`),
  CONSTRAINT `FK97F3E6EA77E3ECF4` FOREIGN KEY (`SOURCE_CLASS_FK`) REFERENCES `t_source_class` (`SOURCE_CLASS`),
  CONSTRAINT `FK97F3E6EAC6A81280` FOREIGN KEY (`INPUT_FORMAT_FK`) REFERENCES `t_input_format` (`PK`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_entity_type_and_subtype`
--

DROP TABLE IF EXISTS `t_entity_type_and_subtype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_entity_type_and_subtype` (
  `SUBTYPE` varchar(255) NOT NULL,
  `TYPE` varchar(255) NOT NULL,
  `COMMENT` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SUBTYPE`,`TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_input_column`
--

DROP TABLE IF EXISTS `t_input_column`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_input_column` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `ALIAS` varchar(255) DEFAULT NULL,
  `COLUMN_NAME` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(2000) DEFAULT NULL,
  `DATA_FORMAT` varchar(255) DEFAULT NULL,
  `TYPE` varchar(255) DEFAULT NULL,
  `INPUT_FORMAT_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FKEE8CE396C6A81280` (`INPUT_FORMAT_FK`),
  CONSTRAINT `FKEE8CE396C6A81280` FOREIGN KEY (`INPUT_FORMAT_FK`) REFERENCES `t_input_format` (`PK`)
) ENGINE=InnoDB AUTO_INCREMENT=1338 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_filter`
--

DROP TABLE IF EXISTS `t_filter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_filter` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `TYPE` varchar(255) DEFAULT NULL,
  `URI` varchar(255) NOT NULL,
  PRIMARY KEY (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_report_config`
--

DROP TABLE IF EXISTS `t_group_report_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_group_report_config` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `REPORT_VERSION` varchar(255) NOT NULL,
  `REPORT_OUTPUT_FK` bigint(20) DEFAULT NULL,
  `CRITERIA_TYPE` varchar(255) NOT NULL,
  `CRITERIA_VALUE` varchar(255) NOT NULL,
  `FILTER_FK` bigint(20) DEFAULT NULL,
  `REPORT_CONFIG_FK` bigint(20) NOT NULL,
  `REPORTING_GROUP_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FKD70002628477A166` (`REPORT_OUTPUT_FK`),
  KEY `FKD7000262BE04EDC6` (`REPORT_CONFIG_FK`),
  KEY `FKD70002625A87B7BD` (`FILTER_FK`),
  KEY `FKD70002622CB512C0` (`REPORTING_GROUP_FK`),
  KEY `FKD70002621CD74323` (`CRITERIA_TYPE`,`CRITERIA_VALUE`),
  CONSTRAINT `FKD70002621CD74323` FOREIGN KEY (`CRITERIA_TYPE`, `CRITERIA_VALUE`) REFERENCES `t_criteria` (`CRITERIA_TYPE`, `CRITERIA_VALUE`),
  CONSTRAINT `FKD70002622CB512C0` FOREIGN KEY (`REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`),
  CONSTRAINT `FKD70002625A87B7BD` FOREIGN KEY (`FILTER_FK`) REFERENCES `t_filter` (`PK`),
  CONSTRAINT `FKD70002628477A166` FOREIGN KEY (`REPORT_OUTPUT_FK`) REFERENCES `t_report_output` (`PK`),
  CONSTRAINT `FKD7000262BE04EDC6` FOREIGN KEY (`REPORT_CONFIG_FK`) REFERENCES `t_report_config` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_partition_status`
--

DROP TABLE IF EXISTS `t_group_partition_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_group_partition_status` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `DATE` varchar(6) DEFAULT NULL,
  `ENTITY_TYPE` varchar(255) DEFAULT NULL,
  `PARTITIONS` text,
  `REPORTING_GROUP_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FKEDA695322CB512C0` (`REPORTING_GROUP_FK`),
  KEY `INDEX_T_GROUP_PARTITION_STATUS` (`REPORTING_GROUP_FK`,`DATE`),
  CONSTRAINT `FKEDA695322CB512C0` FOREIGN KEY (`REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tj_composed_reporting_group`
--

DROP TABLE IF EXISTS `tj_composed_reporting_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tj_composed_reporting_group` (
  `COMPOSED_REPORTING_GROUP_FK` bigint(20) NOT NULL,
  `REPORTING_GROUP_FK` bigint(20) NOT NULL,
  KEY `FK9728B16A2CB512C0` (`REPORTING_GROUP_FK`),
  KEY `FK9728B16A9F17E7BF` (`COMPOSED_REPORTING_GROUP_FK`),
  CONSTRAINT `FK9728B16A9F17E7BF` FOREIGN KEY (`COMPOSED_REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`),
  CONSTRAINT `FK9728B16A2CB512C0` FOREIGN KEY (`REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_reporting_group`
--

DROP TABLE IF EXISTS `t_reporting_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_reporting_group` (
  `REPORTING_GROUP_TYPE` varchar(31) NOT NULL,
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `CREATION_DATE` datetime DEFAULT NULL,
  `GROUPING_CRITERIA` varchar(255) NOT NULL,
  `GROUPING_VALUE` blob NOT NULL,
  `LABEL` varchar(255) DEFAULT NULL,
  `LANGUAGE` varchar(255) DEFAULT NULL,
  `ORIGIN` varchar(255) NOT NULL,
  `REPORTING_GROUP_REF` varchar(255) NOT NULL,
  `SOURCE` varchar(255) NOT NULL,
  `TYPE` varchar(255) NOT NULL,
  `UPDATE_DATE` datetime DEFAULT NULL,
  `DATA_LOCATION_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  UNIQUE KEY `REPORTING_GROUP_REF` (`REPORTING_GROUP_REF`,`ORIGIN`),
  KEY `FKD8312303D4C1F492` (`DATA_LOCATION_FK`),
  CONSTRAINT `FKD8312303D4C1F492` FOREIGN KEY (`DATA_LOCATION_FK`) REFERENCES `t_data_location` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_reports_bookmark`
--

DROP TABLE IF EXISTS `t_reports_bookmark`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_reports_bookmark` (
  `BOOKMARK_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `GRANULARITY` varchar(255) NOT NULL,
  `REPORT_TIME_UNIT` varchar(255) NOT NULL,
  `INDICATOR_FK` bigint(20) DEFAULT NULL,
  `OFFER_OPTION_FK` bigint(20) DEFAULT NULL,
  `PARAM_TYPE_FK` bigint(20) NOT NULL,
  `REPORT_USER_FK` bigint(20) DEFAULT NULL,
  `REPORTING_ENTITY_FK` bigint(20) DEFAULT NULL,
  `REPORTING_GROUP_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`BOOKMARK_ID`),
  UNIQUE KEY `BOOKMARK_ID` (`BOOKMARK_ID`),
  KEY `FK48D66061F155BF0B` (`INDICATOR_FK`),
  KEY `FK48D66061FA70B5A6` (`REPORT_USER_FK`),
  KEY `FK48D660619288D448` (`REPORTING_ENTITY_FK`),
  KEY `FK48D660612CB512C0` (`REPORTING_GROUP_FK`),
  KEY `FK48D66061C9ED0BA6` (`PARAM_TYPE_FK`),
  KEY `FK48D6606116D09AE4` (`OFFER_OPTION_FK`),
  CONSTRAINT `FK48D6606116D09AE4` FOREIGN KEY (`OFFER_OPTION_FK`) REFERENCES `t_offer_option` (`PK`),
  CONSTRAINT `FK48D660612CB512C0` FOREIGN KEY (`REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`),
  CONSTRAINT `FK48D660619288D448` FOREIGN KEY (`REPORTING_ENTITY_FK`) REFERENCES `t_reporting_entity` (`PK`),
  CONSTRAINT `FK48D66061C9ED0BA6` FOREIGN KEY (`PARAM_TYPE_FK`) REFERENCES `t_param_type` (`PK`),
  CONSTRAINT `FK48D66061F155BF0B` FOREIGN KEY (`INDICATOR_FK`) REFERENCES `t_indicator` (`PK`),
  CONSTRAINT `FK48D66061FA70B5A6` FOREIGN KEY (`REPORT_USER_FK`) REFERENCES `t_report_user` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_entity_link`
--

DROP TABLE IF EXISTS `t_entity_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_entity_link` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `PARAMETER` varchar(255) DEFAULT NULL,
  `ROLE` varchar(255) NOT NULL,
  `TYPE` varchar(255) NOT NULL,
  `REPORTING_ENTITY_DEST_FK` bigint(20) NOT NULL,
  `REPORTING_ENTITY_SRC_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`PK`),
  KEY `FKEC627F6BED558303` (`REPORTING_ENTITY_SRC_FK`),
  KEY `FKEC627F6BD48BABCF` (`REPORTING_ENTITY_DEST_FK`),
  CONSTRAINT `FKEC627F6BD48BABCF` FOREIGN KEY (`REPORTING_ENTITY_DEST_FK`) REFERENCES `t_reporting_entity` (`PK`),
  CONSTRAINT `FKEC627F6BED558303` FOREIGN KEY (`REPORTING_ENTITY_SRC_FK`) REFERENCES `t_reporting_entity` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_entity_attribute`
--

DROP TABLE IF EXISTS `t_entity_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_entity_attribute` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `VALUE` varchar(255) NOT NULL,
  `REPORTING_ENTITY_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FK5422E1AB9288D448` (`REPORTING_ENTITY_FK`),
  KEY `INDEX_T_ENTITY_ATTRIBUTE_NAME_VALUE` (`NAME`,`VALUE`),
  KEY `INDEX_T_ENTITY_ATTRIBUTE_NAME_REPORTING_ENTITY_FK` (`NAME`,`REPORTING_ENTITY_FK`),
  CONSTRAINT `FK5422E1AB9288D448` FOREIGN KEY (`REPORTING_ENTITY_FK`) REFERENCES `t_reporting_entity` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_data_location`
--

DROP TABLE IF EXISTS `t_data_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_data_location` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `LOCATION_PATTERN` varchar(255) NOT NULL,
  `CRITERIA_VALUE` varchar(255) DEFAULT NULL,
  `CRITERIA_TYPE` varchar(255) DEFAULT NULL,
  `REPORTING_ENTITY_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FK234087FF1CD74323` (`CRITERIA_VALUE`,`CRITERIA_TYPE`),
  KEY `FK234087FF9288D448` (`REPORTING_ENTITY_FK`),
  CONSTRAINT `FK234087FF9288D448` FOREIGN KEY (`REPORTING_ENTITY_FK`) REFERENCES `t_reporting_entity` (`PK`),
  CONSTRAINT `FK234087FF1CD74323` FOREIGN KEY (`CRITERIA_VALUE`, `CRITERIA_TYPE`) REFERENCES `t_criteria` (`CRITERIA_TYPE`, `CRITERIA_VALUE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_report_config`
--

DROP TABLE IF EXISTS `t_report_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_report_config` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `ALIAS` varchar(255) NOT NULL,
  `COMPUTE_SCOPE` varchar(255) NOT NULL,
  `REPORT_DEFAULT_VERSION` varchar(255) NOT NULL,
  `TYPE` varchar(255) NOT NULL,
  `REPORT_FK` bigint(20) DEFAULT NULL,
  `REPORT_OUTPUT_FK` bigint(20) DEFAULT NULL,
  `CRITERIA_TYPE` varchar(255) NOT NULL,
  `CRITERIA_VALUE` varchar(255) NOT NULL,
  `OFFER_OPTION_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`PK`),
  KEY `FK5C58D8028477A166` (`REPORT_OUTPUT_FK`),
  KEY `FK5C58D8021CD74323` (`CRITERIA_TYPE`,`CRITERIA_VALUE`),
  KEY `FK5C58D8028E448E3D` (`REPORT_FK`),
  KEY `FK5C58D80216D09AE4` (`OFFER_OPTION_FK`),
  CONSTRAINT `FK5C58D80216D09AE4` FOREIGN KEY (`OFFER_OPTION_FK`) REFERENCES `t_offer_option` (`PK`),
  CONSTRAINT `FK5C58D8021CD74323` FOREIGN KEY (`CRITERIA_TYPE`, `CRITERIA_VALUE`) REFERENCES `t_criteria` (`CRITERIA_TYPE`, `CRITERIA_VALUE`),
  CONSTRAINT `FK5C58D8028477A166` FOREIGN KEY (`REPORT_OUTPUT_FK`) REFERENCES `t_report_output` (`PK`),
  CONSTRAINT `FK5C58D8028E448E3D` FOREIGN KEY (`REPORT_FK`) REFERENCES `t_report` (`PK`)
) ENGINE=InnoDB AUTO_INCREMENT=643 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_indicator`
--

DROP TABLE IF EXISTS `t_indicator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_indicator` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `INDICATOR_ID` varchar(255) NOT NULL,
  `LABEL` varchar(255) NOT NULL,
  PRIMARY KEY (`PK`),
  UNIQUE KEY `INDICATOR_ID` (`INDICATOR_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_hyperlink`
--

DROP TABLE IF EXISTS `t_hyperlink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_hyperlink` (
  `LABEL` varchar(255) NOT NULL,
  `INDICATOR_FK` bigint(20) DEFAULT NULL,
  `OFFER_OPTION_FK` bigint(20) DEFAULT NULL,
  `PARAM_TYPE_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`LABEL`),
  UNIQUE KEY `LABEL` (`LABEL`),
  KEY `FK552B8A1BF155BF0B` (`INDICATOR_FK`),
  KEY `FK552B8A1BC9ED0BA6` (`PARAM_TYPE_FK`),
  KEY `FK552B8A1B16D09AE4` (`OFFER_OPTION_FK`),
  CONSTRAINT `FK552B8A1B16D09AE4` FOREIGN KEY (`OFFER_OPTION_FK`) REFERENCES `t_offer_option` (`PK`),
  CONSTRAINT `FK552B8A1BC9ED0BA6` FOREIGN KEY (`PARAM_TYPE_FK`) REFERENCES `t_param_type` (`PK`),
  CONSTRAINT `FK552B8A1BF155BF0B` FOREIGN KEY (`INDICATOR_FK`) REFERENCES `t_indicator` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_partition_status`
--

DROP TABLE IF EXISTS `t_partition_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_partition_status` (
  `DATE` varchar(6) NOT NULL,
  `NUMBER_OF_ENTITY` int(11) DEFAULT NULL,
  `PARTITION_NUMBER` int(11) DEFAULT NULL,
  PRIMARY KEY (`DATE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_offer_option`
--

DROP TABLE IF EXISTS `t_offer_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_offer_option` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `ALIAS` varchar(255) NOT NULL,
  `LABEL` varchar(255) NOT NULL,
  `OFFER_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  UNIQUE KEY `ALIAS` (`ALIAS`),
  KEY `FKEB352DE329E05B6B` (`OFFER_FK`),
  CONSTRAINT `FKEB352DE329E05B6B` FOREIGN KEY (`OFFER_FK`) REFERENCES `t_offer` (`PK`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tj_reporting_group_to_offer_option`
--

DROP TABLE IF EXISTS `tj_reporting_group_to_offer_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tj_reporting_group_to_offer_option` (
  `REPORTING_GROUP_FK` bigint(20) NOT NULL,
  `OFFER_OPTION_FK` bigint(20) NOT NULL,
  KEY `FK588830022CB512C0` (`REPORTING_GROUP_FK`),
  KEY `FK5888300216D09AE4` (`OFFER_OPTION_FK`),
  CONSTRAINT `FK5888300216D09AE4` FOREIGN KEY (`OFFER_OPTION_FK`) REFERENCES `t_offer_option` (`PK`),
  CONSTRAINT `FK588830022CB512C0` FOREIGN KEY (`REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_source_class`
--

DROP TABLE IF EXISTS `t_source_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_source_class` (
  `SOURCE_CLASS` varchar(255) NOT NULL,
  PRIMARY KEY (`SOURCE_CLASS`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_criteria`
--

DROP TABLE IF EXISTS `t_criteria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_criteria` (
  `CRITERIA_TYPE` varchar(255) NOT NULL,
  `CRITERIA_VALUE` varchar(255) NOT NULL,
  PRIMARY KEY (`CRITERIA_TYPE`,`CRITERIA_VALUE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_source_proxy`
--

DROP TABLE IF EXISTS `t_source_proxy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_source_proxy` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `PROXY_INDEX` int(11) DEFAULT NULL,
  `KO_CAUSE` text,
  `STATE` int(11) DEFAULT NULL,
  `PROXY_FK` bigint(20) NOT NULL,
  `INPUT_SOURCE_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FKC38663351D0A8A2B` (`PROXY_FK`),
  KEY `FKC3866335BA27000` (`INPUT_SOURCE_FK`),
  CONSTRAINT `FKC3866335BA27000` FOREIGN KEY (`INPUT_SOURCE_FK`) REFERENCES `t_input_source` (`PK`),
  CONSTRAINT `FKC38663351D0A8A2B` FOREIGN KEY (`PROXY_FK`) REFERENCES `t_proxy` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tj_reporting_group_to_entities`
--

DROP TABLE IF EXISTS `tj_reporting_group_to_entities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tj_reporting_group_to_entities` (
  `TYPE` varchar(31) NOT NULL,
  `BELONGS_TO` bit(1) DEFAULT NULL,
  `REPORTING_ENTITY_FK` bigint(20) NOT NULL,
  `REPORTING_GROUP_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`REPORTING_ENTITY_FK`,`REPORTING_GROUP_FK`),
  KEY `FK370770EB9288D448` (`REPORTING_ENTITY_FK`),
  KEY `FK370770EB2CB512C0` (`REPORTING_GROUP_FK`),
  CONSTRAINT `FK370770EB2CB512C0` FOREIGN KEY (`REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`),
  CONSTRAINT `FK370770EB9288D448` FOREIGN KEY (`REPORTING_ENTITY_FK`) REFERENCES `t_reporting_entity` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_report_output`
--

DROP TABLE IF EXISTS `t_report_output`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_report_output` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `FORMAT` varchar(255) DEFAULT NULL,
  `LOCATION_PATTERN_PREFIX` varchar(255) NOT NULL,
  `LOCATION_PATTERN_SUFFIX` varchar(255) DEFAULT NULL,
  `TYPE` varchar(255) DEFAULT NULL,
  `URI` varchar(255) NOT NULL,
  PRIMARY KEY (`PK`)
) ENGINE=InnoDB AUTO_INCREMENT=643 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tj_report_user_to_reporting_group`
--

DROP TABLE IF EXISTS `tj_report_user_to_reporting_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tj_report_user_to_reporting_group` (
  `REPORT_USER_FK` bigint(20) NOT NULL,
  `WORKING_GROUP_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`REPORT_USER_FK`,`WORKING_GROUP_FK`),
  KEY `FK2032E8FCFA70B5A6` (`REPORT_USER_FK`),
  KEY `FK2032E8FC991BA11D` (`WORKING_GROUP_FK`),
  CONSTRAINT `FK2032E8FC991BA11D` FOREIGN KEY (`WORKING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`),
  CONSTRAINT `FK2032E8FCFA70B5A6` FOREIGN KEY (`REPORT_USER_FK`) REFERENCES `t_report_user` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_entity_group_attribute`
--

DROP TABLE IF EXISTS `t_entity_group_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_entity_group_attribute` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `VALUE` varchar(255) NOT NULL,
  `REPORTING_ENTITY_FK` bigint(20) DEFAULT NULL,
  `REPORTING_GROUP_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FK7B37508B9288D448` (`REPORTING_ENTITY_FK`),
  KEY `FK7B37508B2CB512C0` (`REPORTING_GROUP_FK`),
  CONSTRAINT `FK7B37508B2CB512C0` FOREIGN KEY (`REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`),
  CONSTRAINT `FK7B37508B9288D448` FOREIGN KEY (`REPORTING_ENTITY_FK`) REFERENCES `t_reporting_entity` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tj_report_to_input`
--

DROP TABLE IF EXISTS `tj_report_to_input`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tj_report_to_input` (
  `REPORT_FK` bigint(20) NOT NULL,
  `REPORT_INPUT_FK` bigint(20) NOT NULL,
  KEY `FK5E17BC87EE68D42` (`REPORT_INPUT_FK`),
  KEY `FK5E17BC88E448E3D` (`REPORT_FK`),
  CONSTRAINT `FK5E17BC88E448E3D` FOREIGN KEY (`REPORT_FK`) REFERENCES `t_report` (`PK`),
  CONSTRAINT `FK5E17BC87EE68D42` FOREIGN KEY (`REPORT_INPUT_FK`) REFERENCES `t_report_input` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_report_user`
--

DROP TABLE IF EXISTS `t_report_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_report_user` (
  `PK` bigint(20) NOT NULL,
  PRIMARY KEY (`PK`),
  UNIQUE KEY `PK` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tj_report_config_to_param_type`
--

DROP TABLE IF EXISTS `tj_report_config_to_param_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tj_report_config_to_param_type` (
  `REPORT_CONFIG_FK` bigint(20) NOT NULL,
  `PARAM_TYPE_FK` bigint(20) NOT NULL,
  PRIMARY KEY (`REPORT_CONFIG_FK`,`PARAM_TYPE_FK`),
  UNIQUE KEY `REPORT_CONFIG_FK` (`REPORT_CONFIG_FK`,`PARAM_TYPE_FK`),
  KEY `FK41EBBDB5BE04EDC6` (`REPORT_CONFIG_FK`),
  KEY `FK41EBBDB5C9ED0BA6` (`PARAM_TYPE_FK`),
  CONSTRAINT `FK41EBBDB5C9ED0BA6` FOREIGN KEY (`PARAM_TYPE_FK`) REFERENCES `t_param_type` (`PK`),
  CONSTRAINT `FK41EBBDB5BE04EDC6` FOREIGN KEY (`REPORT_CONFIG_FK`) REFERENCES `t_report_config` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_attribute`
--

DROP TABLE IF EXISTS `t_group_attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_group_attribute` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `VALUE` varchar(255) NOT NULL,
  `REPORTING_GROUP_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FK6FC6B6B12CB512C0` (`REPORTING_GROUP_FK`),
  KEY `INDEX_T_GROUP_ATTRIBUTE_NAME_VALUE` (`NAME`,`VALUE`),
  CONSTRAINT `FK6FC6B6B12CB512C0` FOREIGN KEY (`REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_param_type`
--

DROP TABLE IF EXISTS `t_param_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_param_type` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `ALIAS` varchar(255) DEFAULT NULL,
  `ENTITY_SUBTYPE` varchar(255) DEFAULT NULL,
  `ENTITY_TYPE` varchar(255) DEFAULT NULL,
  `PARENT_SUBTYPE` varchar(255) DEFAULT NULL,
  `PARENT_TYPE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  UNIQUE KEY `ALIAS` (`ALIAS`),
  KEY `FKD8F497A22F60D` (`ENTITY_SUBTYPE`,`ENTITY_TYPE`),
  KEY `FKD8F497901C236D` (`PARENT_SUBTYPE`,`PARENT_TYPE`),
  CONSTRAINT `FKD8F497901C236D` FOREIGN KEY (`PARENT_SUBTYPE`, `PARENT_TYPE`) REFERENCES `t_entity_type_and_subtype` (`SUBTYPE`, `TYPE`),
  CONSTRAINT `FKD8F497A22F60D` FOREIGN KEY (`ENTITY_SUBTYPE`, `ENTITY_TYPE`) REFERENCES `t_entity_type_and_subtype` (`SUBTYPE`, `TYPE`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_input_format`
--

DROP TABLE IF EXISTS `t_input_format`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_input_format` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `FORMAT_TYPE` varchar(255) NOT NULL,
  PRIMARY KEY (`PK`),
  UNIQUE KEY `FORMAT_TYPE` (`FORMAT_TYPE`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tj_indicator_to_report`
--

DROP TABLE IF EXISTS `tj_indicator_to_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tj_indicator_to_report` (
  `REPORT_FK` bigint(20) NOT NULL,
  `INDICATOR_FK` bigint(20) NOT NULL,
  UNIQUE KEY `INDICATOR_FK` (`INDICATOR_FK`,`REPORT_FK`),
  KEY `FKC636B5DFF155BF0B` (`INDICATOR_FK`),
  KEY `FKC636B5DF8E448E3D` (`REPORT_FK`),
  CONSTRAINT `FKC636B5DF8E448E3D` FOREIGN KEY (`REPORT_FK`) REFERENCES `t_report` (`PK`),
  CONSTRAINT `FKC636B5DFF155BF0B` FOREIGN KEY (`INDICATOR_FK`) REFERENCES `t_indicator` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_input_source`
--

DROP TABLE IF EXISTS `t_input_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_input_source` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `SOURCE_CONFIGURATION` text,
  `SOURCE_NAME` varchar(255) DEFAULT NULL,
  `POLLING_STATE` text,
  `STATE` int(11) DEFAULT NULL,
  `SOURCE_CLASS_FK` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FK9DE785B77E3ECF4` (`SOURCE_CLASS_FK`),
  CONSTRAINT `FK9DE785B77E3ECF4` FOREIGN KEY (`SOURCE_CLASS_FK`) REFERENCES `t_source_class` (`SOURCE_CLASS`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tj_reporting_group_to_criteria`
--

DROP TABLE IF EXISTS `tj_reporting_group_to_criteria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tj_reporting_group_to_criteria` (
  `REPORTING_GROUP_FK` bigint(20) NOT NULL,
  `CRITERIA_VALUE` varchar(255) NOT NULL,
  `CRITERIA_TYPE` varchar(255) NOT NULL,
  KEY `FK28AD08891CD74323` (`CRITERIA_VALUE`,`CRITERIA_TYPE`),
  KEY `FK28AD08892CB512C0` (`REPORTING_GROUP_FK`),
  CONSTRAINT `FK28AD08892CB512C0` FOREIGN KEY (`REPORTING_GROUP_FK`) REFERENCES `t_reporting_group` (`PK`),
  CONSTRAINT `FK28AD08891CD74323` FOREIGN KEY (`CRITERIA_VALUE`, `CRITERIA_TYPE`) REFERENCES `t_criteria` (`CRITERIA_TYPE`, `CRITERIA_VALUE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tj_reporting_entity_to_type_and_subtype`
--

DROP TABLE IF EXISTS `tj_reporting_entity_to_type_and_subtype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tj_reporting_entity_to_type_and_subtype` (
  `REPORTING_ENTITY_FK` bigint(20) NOT NULL,
  `SUBTYPE` varchar(255) NOT NULL,
  `TYPE` varchar(255) NOT NULL,
  PRIMARY KEY (`REPORTING_ENTITY_FK`,`SUBTYPE`,`TYPE`),
  KEY `FKA67F552F9288D448` (`REPORTING_ENTITY_FK`),
  KEY `FKA67F552F44AB338D` (`SUBTYPE`,`TYPE`),
  CONSTRAINT `FKA67F552F44AB338D` FOREIGN KEY (`SUBTYPE`, `TYPE`) REFERENCES `t_entity_type_and_subtype` (`SUBTYPE`, `TYPE`),
  CONSTRAINT `FKA67F552F9288D448` FOREIGN KEY (`REPORTING_ENTITY_FK`) REFERENCES `t_reporting_entity` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_reporting_entity`
--

DROP TABLE IF EXISTS `t_reporting_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_reporting_entity` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `CREATION_DATE` datetime NOT NULL,
  `ENTITY_ID` varchar(255) NOT NULL,
  `ENTITY_TYPE` varchar(50) NOT NULL,
  `LABEL` varchar(255) NOT NULL,
  `ORIGIN` varchar(255) NOT NULL,
  `PARTITION_NUMBER` varchar(110) DEFAULT NULL,
  `SHORT_LABEL` varchar(255) NOT NULL,
  `SOURCE` varchar(255) NOT NULL,
  `UPDATE_DATE` datetime NOT NULL,
  `REPORTING_ENTITY_PARENT_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  UNIQUE KEY `ENTITY_ID` (`ENTITY_ID`,`ENTITY_TYPE`,`ORIGIN`),
  KEY `FK2A5347BFA4A6E387` (`REPORTING_ENTITY_PARENT_FK`),
  KEY `INDEX_ENTITY_TYPE` (`ENTITY_TYPE`),
  CONSTRAINT `FK2A5347BFA4A6E387` FOREIGN KEY (`REPORTING_ENTITY_PARENT_FK`) REFERENCES `t_reporting_entity` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_offer`
--

DROP TABLE IF EXISTS `t_offer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_offer` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `ALIAS` varchar(255) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  PRIMARY KEY (`PK`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_entity_attribute_list`
--

DROP TABLE IF EXISTS `t_entity_attribute_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_entity_attribute_list` (
  `PK` bigint(20) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(255) NOT NULL,
  `VALUE` longtext NOT NULL,
  `REPORTING_ENTITY_FK` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`PK`),
  KEY `FKCACD2B29288D448` (`REPORTING_ENTITY_FK`),
  KEY `INDEX_T_ENTITY_ATTRIBUTE_NAME_REPORTING_ENTITY_FK` (`NAME`,`REPORTING_ENTITY_FK`),
  CONSTRAINT `FKCACD2B29288D448` FOREIGN KEY (`REPORTING_ENTITY_FK`) REFERENCES `t_reporting_entity` (`PK`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-27  9:25:57

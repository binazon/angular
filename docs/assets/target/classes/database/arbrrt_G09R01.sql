USE RefReport;

UPDATE T_OFFER_OPTION SET ALIAS='IPER_E2E_BATCH' WHERE ALIAS='IPER_BATCH';
UPDATE T_OFFER_OPTION SET ALIAS='IPER_BB_DOWNLOADABLE_REPORTS' WHERE ALIAS='IPER_DOWNLOADABLE_REPORTS';
UPDATE T_OFFER_OPTION SET ALIAS='IPER_E2E_INTERACTIVE' WHERE ALIAS='IPER_INTERACTIVE';
UPDATE T_OFFER_OPTION SET ALIAS='IPER_E2E_INTERMEDIARY' WHERE ALIAS='IPER_INTERMEDIARY';
UPDATE T_OFFER_OPTION SET ALIAS='IPER-BB_BB_BATCH' WHERE ALIAS='IPER-BB_BATCH';
UPDATE T_OFFER_OPTION SET ALIAS='IPER-BB_BB_INTERMEDIARY' WHERE ALIAS='IPER-BB_INTERMEDIARY';
CREATE TABLE t_tmp_offer_option (`entity_pk` BIGINT(20) NOT NULL, PRIMARY KEY (`entity_pk`));
INSERT INTO t_tmp_offer_option SELECT PK FROM T_OFFER_OPTION WHERE ALIAS IN ('IPER_BBPATHAVAIL_QOS_INTERACTIVE','IPER_BBPATHAVAIL_QOS_INTERNET_INTERACTIVE','IPER_BBPATHAVAIL_SLA_INTERACTIVE','IPER_BBPATHAVAIL_SLA_INTERNET_INTERACTIVE','IPER_BBPATHPERF_QOS_INTERACTIVE','IPER_BBPATHPERF_QOS_INTERNET_INTERACTIVE','IPER_BBPATHPERF_SLA_INTERACTIVE','IPER_BBPATHPERF_SLA_INTERNET_INTERACTIVE','IPER_E2E_INTERMEDIARY','IPER_E2E_INTERACTIVE','BTG-hkh_INTERMEDIARY_3','BTG-hkh-France_INTERMEDIARY_3');
CREATE TABLE t_tmp_report_config (`entity_pk` BIGINT(20) NOT NULL, PRIMARY KEY (`entity_pk`));
INSERT INTO t_tmp_report_config SELECT PK FROM T_REPORT_CONFIG WHERE OFFER_OPTION_FK IN (SELECT entity_pk FROM t_tmp_offer_option);
DELETE FROM TJ_INDICATOR_TO_REPORT_CONFIG WHERE REPORT_CONFIG_FK IN (SELECT entity_pk FROM t_tmp_report_config);
DELETE FROM TJ_REPORT_CONFIG_TO_PARAM_TYPE WHERE REPORT_CONFIG_FK IN (SELECT entity_pk FROM t_tmp_report_config);
DELETE FROM T_GROUP_REPORT_CONFIG WHERE REPORT_CONFIG_FK IN (SELECT entity_pk FROM t_tmp_report_config);
DELETE FROM T_REPORT_CONFIG WHERE PK IN (SELECT entity_pk FROM t_tmp_report_config);
DROP TABLE t_tmp_report_config;
DELETE FROM TJ_REPORTING_GROUP_TO_OFFER_OPTION WHERE OFFER_OPTION_FK IN (SELECT entity_pk FROM t_tmp_offer_option);
DELETE FROM T_HYPERLINK WHERE OFFER_OPTION_FK IN (SELECT entity_pk FROM t_tmp_offer_option);
DELETE FROM T_OFFER_OPTION WHERE PK IN (SELECT entity_pk FROM t_tmp_offer_option);
DROP TABLE t_tmp_offer_option;

delete l from T_ENTITY_LINK as l left outer join (select MAX(l1.PK) as PK, l1.REPORTING_ENTITY_DEST_FK from T_ENTITY_LINK l1 where l1.ROLE='ORIGIN' and l1.TYPE='SHADOW_SITE' group by l1.REPORTING_ENTITY_DEST_FK) as t1 on l.PK = t1.PK where l.ROLE='ORIGIN' and l.TYPE='SHADOW_SITE' and t1.PK is null;
delete l from T_ENTITY_LINK as l left outer join (select MAX(l1.PK) as PK, l1.REPORTING_ENTITY_DEST_FK from T_ENTITY_LINK l1 where l1.ROLE='END' and l1.TYPE='SHADOW_SITE' group by l1.REPORTING_ENTITY_DEST_FK) as t1 on l.PK = t1.PK where l.ROLE='END' and l.TYPE='SHADOW_SITE' and t1.PK is null;
delete l from T_ENTITY_LINK as l left outer join (select MAX(l1.PK) as PK, l1.REPORTING_ENTITY_DEST_FK from T_ENTITY_LINK l1 where l1.ROLE='CALLLIMITER' and l1.TYPE='CALLLIMITER' group by l1.REPORTING_ENTITY_DEST_FK) as t1 on l.PK = t1.PK where l.ROLE='CALLLIMITER' and l.TYPE='CALLLIMITER' and t1.PK is null;

alter table `T_ENTITY_LINK_ATTRIBUTE` add column `REPORTING_ENTITY_DEST_FK` bigint(20) NOT NULL;
alter table `T_ENTITY_LINK_ATTRIBUTE` add column `REPORTING_ENTITY_SRC_FK` bigint(20) NOT NULL;
alter table `T_ENTITY_LINK_ATTRIBUTE` add column `ROLE` varchar(255) NOT NULL;
update `T_ENTITY_LINK_ATTRIBUTE` set `T_ENTITY_LINK_ATTRIBUTE`.`REPORTING_ENTITY_DEST_FK` = (select `T_ENTITY_LINK`.`REPORTING_ENTITY_DEST_FK` from `T_ENTITY_LINK` where `T_ENTITY_LINK_ATTRIBUTE`.`ENTITY_LINK_FK` = `T_ENTITY_LINK`.`PK`);
update `T_ENTITY_LINK_ATTRIBUTE` set `T_ENTITY_LINK_ATTRIBUTE`.`REPORTING_ENTITY_SRC_FK` = (select `T_ENTITY_LINK`.`REPORTING_ENTITY_SRC_FK` from `T_ENTITY_LINK` where `T_ENTITY_LINK_ATTRIBUTE`.`ENTITY_LINK_FK` = `T_ENTITY_LINK`.`PK`);
update `T_ENTITY_LINK_ATTRIBUTE` set `T_ENTITY_LINK_ATTRIBUTE`.`ROLE` = (select `T_ENTITY_LINK`.`ROLE` from `T_ENTITY_LINK` where `T_ENTITY_LINK_ATTRIBUTE`.`ENTITY_LINK_FK` = `T_ENTITY_LINK`.`PK`);

set @FK_T_ENTITY_LINK_ATTRIBUTE = (select `CONSTRAINT_NAME` from `INFORMATION_SCHEMA`.`KEY_COLUMN_USAGE`
                                    where `TABLE_SCHEMA` = "RefReport" and `TABLE_NAME` = "T_ENTITY_LINK_ATTRIBUTE" 
                                    and `REFERENCED_COLUMN_NAME` is not NULL LIMIT 1);
set @ALTER_QUERY_FK = CONCAT('ALTER TABLE T_ENTITY_LINK_ATTRIBUTE DROP FOREIGN KEY `', @FK_T_ENTITY_LINK_ATTRIBUTE, '`');
set @ALTER_QUERY_KEY = CONCAT('ALTER TABLE T_ENTITY_LINK_ATTRIBUTE DROP KEY `', @FK_T_ENTITY_LINK_ATTRIBUTE, '`');
PREPARE stmt1 from @ALTER_QUERY_FK;
EXECUTE stmt1;
PREPARE stmt2 from @ALTER_QUERY_KEY;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt1;
DEALLOCATE PREPARE stmt2;

alter table `T_ENTITY_LINK_ATTRIBUTE` drop primary key, drop column `ENTITY_LINK_FK`;
alter table `T_ENTITY_LINK` change `PK` `PK` bigint(20) NOT NULL;
alter table `T_ENTITY_LINK` drop primary key, add primary key(`REPORTING_ENTITY_DEST_FK`, `REPORTING_ENTITY_SRC_FK`, `ROLE`);
alter table `T_ENTITY_LINK` drop column `PK`;
alter table `T_ENTITY_LINK_ATTRIBUTE` add primary key(NAME, `REPORTING_ENTITY_DEST_FK`, `REPORTING_ENTITY_SRC_FK`, `ROLE`);
alter table `T_ENTITY_LINK_ATTRIBUTE` add foreign key (`REPORTING_ENTITY_DEST_FK`,`REPORTING_ENTITY_SRC_FK`, `ROLE`) references `T_ENTITY_LINK` (`REPORTING_ENTITY_DEST_FK`, `REPORTING_ENTITY_SRC_FK`, `ROLE`);

alter table `T_REPORTING_ENTITY` change `PK` `PK` bigint(20) NOT NULL;
alter table `T_REPORTING_GROUP` change `PK` `PK` bigint(20) NOT NULL;

update `T_REPORT_OUTPUT` set `TYPE` = "CASSANDRA_GK_CDR" where `TYPE` = "CASSANDRA_CDR";

create table T_TMP_ENTITY_TYPE (`ENTITY_PK` BIGINT(20) NOT NULL, PRIMARY KEY (`ENTITY_PK`));
insert into T_TMP_ENTITY_TYPE select REPORTING_ENTITY_FK from TJ_REPORTING_ENTITY_TO_TYPE_AND_SUBTYPE where SUBTYPE in ('GKSAN', 'GKSBC', 'GKSHAREDCALLLIMITER', 'GKSITE', 'GKTRUNK', 'GKVPN');
insert into T_TMP_ENTITY_TYPE select re.PK from T_REPORTING_ENTITY re where not exists (select tj.REPORTING_ENTITY_FK from TJ_REPORTING_ENTITY_TO_TYPE_AND_SUBTYPE tj where tj.REPORTING_ENTITY_FK = re.PK);
delete dt from T_DATA_LOCATION as dt join T_TMP_ENTITY_TYPE et on et.ENTITY_PK = dt.REPORTING_ENTITY_FK;
delete ea from T_ENTITY_ATTRIBUTE as ea join T_TMP_ENTITY_TYPE et on et.ENTITY_PK = ea.REPORTING_ENTITY_FK;
delete el from T_ENTITY_LINK as el join T_TMP_ENTITY_TYPE et on et.ENTITY_PK = el.REPORTING_ENTITY_SRC_FK;
delete el from T_ENTITY_LINK as el join T_TMP_ENTITY_TYPE et on et.ENTITY_PK = el.REPORTING_ENTITY_DEST_FK;
delete rets from TJ_REPORTING_ENTITY_TO_TYPE_AND_SUBTYPE as rets join T_TMP_ENTITY_TYPE et on et.ENTITY_PK = rets.REPORTING_ENTITY_FK;
delete rge from TJ_REPORTING_GROUP_TO_ENTITIES as rge join T_TMP_ENTITY_TYPE et on et.ENTITY_PK = rge.REPORTING_ENTITY_FK;
update T_REPORTING_ENTITY re set re.REPORTING_ENTITY_PARENT_FK = NULL where exists (select et.ENTITY_PK from T_TMP_ENTITY_TYPE et where et.ENTITY_PK = re.REPORTING_ENTITY_PARENT_FK);
delete red from T_REPORTING_ENTITY as red join T_TMP_ENTITY_TYPE et on et.ENTITY_PK = red.PK;
drop table T_TMP_ENTITY_TYPE;

insert into T_ENTITY_TYPE_AND_SUBTYPE (SUBTYPE, TYPE) values ('*', 'GKSAN');
insert into T_ENTITY_TYPE_AND_SUBTYPE (SUBTYPE, TYPE) values ('*', 'GKSBC');
insert into T_ENTITY_TYPE_AND_SUBTYPE (SUBTYPE, TYPE) values ('*', 'GKSHAREDCALLLIMITER');
insert into T_ENTITY_TYPE_AND_SUBTYPE (SUBTYPE, TYPE) values ('*', 'GKSITE');
insert into T_ENTITY_TYPE_AND_SUBTYPE (SUBTYPE, TYPE) values ('*', 'GKTRUNK');
insert into T_ENTITY_TYPE_AND_SUBTYPE (SUBTYPE, TYPE) values ('*', 'GKVPN');
update T_PARAM_TYPE set ENTITY_SUBTYPE='*',ENTITY_TYPE='GKSAN' where ENTITY_TYPE='SAN';
update T_PARAM_TYPE set ENTITY_SUBTYPE='*',ENTITY_TYPE='GKSBC' where ALIAS='GKSBC';
update T_PARAM_TYPE set ENTITY_SUBTYPE='*',ENTITY_TYPE='GKSHAREDCALLLIMITER' where ALIAS='GKSHAREDCALLLIMITER';
update T_PARAM_TYPE set ENTITY_SUBTYPE='*',ENTITY_TYPE='GKSITE' where ALIAS='GKSITE';
update T_PARAM_TYPE set ENTITY_SUBTYPE='*',ENTITY_TYPE='GKTRUNK' where ALIAS='GKTRUNK';
update T_PARAM_TYPE set ENTITY_SUBTYPE='*',ENTITY_TYPE='GKVPN' where ALIAS='GKVPN';
delete from T_PARAM_TYPE where ENTITY_TYPE='SAN';
delete from T_ENTITY_TYPE_AND_SUBTYPE where TYPE='SAN';
delete from T_ENTITY_TYPE_AND_SUBTYPE where SUBTYPE='GKSBC';
delete from T_ENTITY_TYPE_AND_SUBTYPE where SUBTYPE='GKSHAREDCALLLIMITER';
delete from T_ENTITY_TYPE_AND_SUBTYPE where SUBTYPE='GKSITE';
delete from T_ENTITY_TYPE_AND_SUBTYPE where SUBTYPE='GKTRUNK';
delete from T_ENTITY_TYPE_AND_SUBTYPE where SUBTYPE='GKVPN';

-- ---------------------------------- --
-- START REPORTING_GROUP_PK MIGRATION --
-- ---------------------------------- --
set @LAST_PK = (select GREATEST(re.PK, rg.PK) from (select PK from T_REPORTING_ENTITY order by PK desc limit 1) as re, (select PK from T_REPORTING_GROUP order by PK desc limit 1) as rg);
create table if not exists T_TMP_RG_PK (
    OLD_PK BIGINT(20) NOT NULL,
    NEW_PK BIGINT(20) NOT NULL,
    primary key (OLD_PK)
);
insert into T_TMP_RG_PK (OLD_PK, NEW_PK) select PK, (@LAST_PK := @LAST_PK + 1) from T_REPORTING_GROUP order by PK;
set FOREIGN_KEY_CHECKS=0;
update T_FILTER_CONFIG as tf set tf.REPORTING_GROUP_FK = (select map.NEW_PK from T_TMP_RG_PK as map where map.OLD_PK = tf.REPORTING_GROUP_FK);
update T_GROUP_ATTRIBUTE as ga set ga.REPORTING_GROUP_FK = (select map.NEW_PK from T_TMP_RG_PK as map where map.OLD_PK = ga.REPORTING_GROUP_FK);
update T_GROUP_PARTITION_STATUS as gp set gp.REPORTING_GROUP_FK = (select map.NEW_PK from T_TMP_RG_PK as map where map.OLD_PK = gp.REPORTING_GROUP_FK);
update T_GROUP_REPORT_CONFIG as grc set grc.REPORTING_GROUP_FK = (select map.NEW_PK from T_TMP_RG_PK as map where map.OLD_PK = grc.REPORTING_GROUP_FK);
update TJ_REPORT_USER_TO_REPORTING_GROUP as rurg set rurg.WORKING_GROUP_FK = (select map.NEW_PK from T_TMP_RG_PK as map where map.OLD_PK = rurg.WORKING_GROUP_FK);
update TJ_REPORTING_GROUP_TO_CRITERIA as rgc set rgc.REPORTING_GROUP_FK = (select map.NEW_PK from T_TMP_RG_PK as map where map.OLD_PK = rgc.REPORTING_GROUP_FK);
update TJ_REPORTING_GROUP_TO_ENTITIES as rge set rge.REPORTING_GROUP_FK = (select map.NEW_PK from T_TMP_RG_PK as map where map.OLD_PK = rge.REPORTING_GROUP_FK);
update TJ_REPORTING_GROUP_TO_OFFER_OPTION as rgoo set rgoo.REPORTING_GROUP_FK = (select map.NEW_PK from T_TMP_RG_PK as map where map.OLD_PK = rgoo.REPORTING_GROUP_FK);
update T_REPORTING_GROUP as rg set rg.PK = (select map.NEW_PK from T_TMP_RG_PK as map where map.OLD_PK = rg.PK);
set FOREIGN_KEY_CHECKS=1;
drop table if exists T_TMP_RG_PK;
-- -------------------------------- --
-- END REPORTING_GROUP_PK MIGRATION --
-- -------------------------------- --

-- ----------------------------- --
-- START GROUPING_RULE MIGRATION --
-- ----------------------------- --
create or replace view GENERATOR_16
as select 0 n union all select 1  union all select 2  union all 
   select 3   union all select 4  union all select 5  union all
   select 6   union all select 7  union all select 8  union all
   select 9   union all select 10 union all select 11 union all
   select 12  union all select 13 union all select 14 union all 
   select 15;
create or replace view GENERATOR_256
as select ( ( hi.n << 4 ) | lo.n ) as n
     from GENERATOR_16 lo, GENERATOR_16 hi;
create or replace view GENERATOR_64K
as select ( ( hi.n << 8 ) | lo.n ) as n
     from GENERATOR_256 lo, GENERATOR_256 hi;
set @NUMBER_SPLIT = (select MAX(LENGTH(GROUPING_VALUE)-LENGTH(REPLACE(GROUPING_VALUE ,'|',''))) from T_REPORTING_GROUP);
create table if not exists T_TMP_NUMBERS (
    NUMBER BIGINT(20) NOT NULL, PRIMARY KEY (NUMBER)
);
insert into T_TMP_NUMBERS(NUMBER) select n from GENERATOR_64K where n < @NUMBER_SPLIT;
delete from T_TMP_NUMBERS where NUMBER = 0;
create table if not exists T_GROUPING_RULE (
   GROUPING_RULE varchar(255) NOT NULL,
   GROUPING_VALUE varchar(255) NOT NULL,
   REPORTING_GROUP_FK bigint NOT NULL,
   primary key (GROUPING_RULE, GROUPING_VALUE, REPORTING_GROUP_FK),
   foreign key (REPORTING_GROUP_FK) references T_REPORTING_GROUP(PK)
);
insert into T_GROUPING_RULE (REPORTING_GROUP_FK, GROUPING_RULE, GROUPING_VALUE)
    select
        T_TMP_GROUPING_RULE.REPORTING_GROUP_FK,
        T_TMP_GROUPING_RULE.GROUPING_RULE,
        SUBSTRING_INDEX(SUBSTRING_INDEX(T_TMP_GROUPING_RULE.G_VALUE, '|', nb2.number), '|', -1) as GROUPING_VALUE
    from
        T_TMP_NUMBERS nb2 inner join (
                select
                    T_REPORTING_GROUP.PK as REPORTING_GROUP_FK,
                    SUBSTRING_INDEX(SUBSTRING_INDEX(T_REPORTING_GROUP.GROUPING_CRITERIA, '_||_', nb1.number), '_||_', -1) as GROUPING_RULE,
                    SUBSTRING_INDEX(SUBSTRING_INDEX(T_REPORTING_GROUP.GROUPING_VALUE, '_||_', nb1.number), '_||_', -1) as G_VALUE
                from
                    T_TMP_NUMBERS nb1 inner join T_REPORTING_GROUP 
                        on CHAR_LENGTH(T_REPORTING_GROUP.GROUPING_CRITERIA)-CHAR_LENGTH(REPLACE(T_REPORTING_GROUP.GROUPING_CRITERIA, '_||_', ''))>=(nb1.NUMBER-1)*4
                ) as T_TMP_GROUPING_RULE
            on CHAR_LENGTH(T_TMP_GROUPING_RULE.G_VALUE)-CHAR_LENGTH(REPLACE(T_TMP_GROUPING_RULE.G_VALUE, '|', ''))>=nb2.NUMBER-1
    where T_TMP_GROUPING_RULE.G_VALUE != '';
drop view if exists GENERATOR_16; drop view if exists GENERATOR_256; drop view if exists GENERATOR_64K; drop table if exists T_TMP_NUMBERS;
-- --------------------------- --
-- END GROUPING_RULE MIGRATION --
-- --------------------------- --

delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'ACCESS_DSL_TYPE';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'serviceType';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'reportingGroupRef';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'supDomain';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'SRV_IPER_SITE_AVAIL';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'SRV_IPER_SITE_AVAIL_SLA';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'SRV_IPER_PATH_AVAIL';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'SRV_IPER_PATH_AVAIL_SLA';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'RTD';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'PLR';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'PLRSD';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'PLRDS';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'JITTERSD';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'JITTERDS';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'ndi' and a.value ='';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'SLAClass' and a.value ='';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'COS' and a.value ='';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'comment' and a.value ='';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'bandwidth_COS_IN' and a.value ='';
delete a from T_ENTITY_ATTRIBUTE as a where a.name = 'bandwidth_COS_OUT' and a.value ='';
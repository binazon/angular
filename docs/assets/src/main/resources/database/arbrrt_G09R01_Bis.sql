USE RefReport;

create table T_TMP_WRONG_ORIGIN (`ENTITY_PK` BIGINT(20) NOT NULL, PRIMARY KEY (`ENTITY_PK`));
insert into T_TMP_WRONG_ORIGIN select re.PK from T_REPORTING_ENTITY re join TJ_REPORTING_GROUP_TO_ENTITIES rge on rge.reporting_entity_fk = re.pk join T_REPORTING_GROUP rg on rg.pk = rge.reporting_group_fk where re.entity_type = 'ZONE' and re.origin = 'SCE' and rg.origin = 'EQUANT';
insert into T_TMP_WRONG_ORIGIN select re.PK from T_REPORTING_ENTITY re join TJ_REPORTING_GROUP_TO_ENTITIES rge on rge.reporting_entity_fk = re.pk join T_REPORTING_GROUP rg on rg.pk = rge.reporting_group_fk where re.entity_type = 'ZONE' and re.origin = 'EQUANT' and rg.origin = 'SCE';
delete dt from T_DATA_LOCATION as dt join T_TMP_WRONG_ORIGIN tmp on tmp.ENTITY_PK = dt.REPORTING_ENTITY_FK;
delete ea from T_ENTITY_ATTRIBUTE as ea join T_TMP_WRONG_ORIGIN tmp on tmp.ENTITY_PK = ea.REPORTING_ENTITY_FK;
delete el from T_ENTITY_LINK as el join T_TMP_WRONG_ORIGIN tmp on tmp.ENTITY_PK = el.REPORTING_ENTITY_SRC_FK;
delete el from T_ENTITY_LINK as el join T_TMP_WRONG_ORIGIN tmp on tmp.ENTITY_PK = el.REPORTING_ENTITY_DEST_FK;
delete rets from TJ_REPORTING_ENTITY_TO_TYPE_AND_SUBTYPE as rets join T_TMP_WRONG_ORIGIN tmp on tmp.ENTITY_PK = rets.REPORTING_ENTITY_FK;
delete rge from TJ_REPORTING_GROUP_TO_ENTITIES as rge join T_TMP_WRONG_ORIGIN tmp on tmp.ENTITY_PK = rge.REPORTING_ENTITY_FK;
update T_REPORTING_ENTITY re set re.REPORTING_ENTITY_PARENT_FK = NULL where exists (select tmp.ENTITY_PK from T_TMP_WRONG_ORIGIN tmp where tmp.ENTITY_PK = re.REPORTING_ENTITY_PARENT_FK);
delete red from T_REPORTING_ENTITY as red join T_TMP_WRONG_ORIGIN tmp on tmp.ENTITY_PK = red.PK;
drop table T_TMP_WRONG_ORIGIN;

delete rge from TJ_REPORTING_GROUP_TO_ENTITIES as rge join T_REPORTING_GROUP rg on rg.pk = rge.reporting_group_fk join T_REPORTING_ENTITY re on re.pk = rge.reporting_entity_fk where rg.origin = 'SCE' and re.origin = 'EQUANT';
delete rge from TJ_REPORTING_GROUP_TO_ENTITIES as rge join T_REPORTING_GROUP rg on rg.pk = rge.reporting_group_fk join T_REPORTING_ENTITY re on re.pk = rge.reporting_entity_fk where rg.origin = 'EQUANT' and re.origin = 'SCE';
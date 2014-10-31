insert ignore into m_permission VALUES (null,'billing','PROCESS_DATAUPLOADS','DATAUPLOADS','PROCESS',0);

update m_permission set code='CREATE_GROUPSPROVISION',entity_name='GROUPSPROVISION' where code='CREATE_PROVISION';

update m_permission set code='CREATE_GROUPSDETAILS',entity_name='GROUPSDETAILS' where code='CREATE_GROUPS';

update m_permission set code='READ_GROUPSDETAILS', entity_name='GROUPSDETAILS' where code='READ_GROUPS';

insert IGNORE into m_permission values(null, 'organisation', 'CREATE_VOUCHER', 'VOUCHER', 'CREATE', '0');

insert IGNORE into m_permission values(null, 'organisation', 'PROCESS_VOUCHER', 'VOUCHER', 'PROCESS', '1');

insert IGNORE into m_permission values(null, 'organisation', 'READ_VOUCHER', 'VOUCHER', 'READ', '1');

delete from m_permission where entity_name Like '%RANDOMGENERATOR%';

insert IGNORE into c_configuration VALUES (null,'OSD_ProvisioningSystem',1,'Comvenient');

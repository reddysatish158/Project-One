insert ignore into m_permission VALUES (null,'billing','PROCESS_DATAUPLOADS','DATAUPLOADS','PROCESS',0);

update m_permission set code='CREATE_GROUPSPROVISION',entity_name='GROUPSPROVISION' where code='CREATE_PROVISION';

update m_permission set code='CREATE_GROUPSDETAILS',entity_name='GROUPSDETAILS' where code='CREATE_GROUPS';

update m_permission set code='READ_GROUPSDETAILS', entity_name='GROUPSDETAILS' where code='READ_GROUPS';

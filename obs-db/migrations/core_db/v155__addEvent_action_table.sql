
 CREATE TABLE if not exists `b_provisioning_actions` (
  `id` bigint(10) NOT NULL AUTO_INCREMENT,
  `provision_type` varchar(15) NOT NULL,
  `action` varchar(15) NOT NULL,
  `provisioning_system` varchar(20) DEFAULT NULL,
  `is_enable` char(1) DEFAULT 'N',
  `is_delete` char(1) DEFAULT 'N',
    PRIMARY KEY (`id`),
  UNIQUE KEY `provisining_key` (`provision_type`)
);


insert ignore into b_provisioning_actions values(null,'Create Client','ACTIVATION','Beenius','Y','N');
insert ignore into b_provisioning_actions values (null,'Close Client','TERMINATE','Beenius','Y','N');
insert ignore into b_provisioning_actions values (null,'Event Order','PROVISION IT','Beenius','Y','N');
DELETE FROM b_eventaction_mapping  WHERE  event_name='Event Order';
insert ignore into m_permission values(null,'billing','ACTIVE_PROVISIONACTIONS','PROVISIONACTIONS','ACTIVE',0);
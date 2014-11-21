
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

Drop procedure IF EXISTS makercheker; 
DELIMITER //
create procedure makercheker() 
Begin
IF  EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_NAME = 'm_portfolio_command_source' and COLUMN_NAME ='command_as_json' and IS_NULLABLE='NO'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE m_portfolio_command_source CHANGE COLUMN `command_as_json` `command_as_json` TEXT DEFAULT NULL;
END IF;
END //
DELIMITER ;
call makercheker();
Drop procedure IF EXISTS makercheker;

insert ignore into b_provisioning_actions values(null,'Create Client','ACTIVATION','Beenius','Y','N');
insert ignore into b_provisioning_actions values (null,'Close Client','TERMINATE','Beenius','Y','N');
insert ignore into b_provisioning_actions values (null,'Event Order','PROVISION IT','Beenius','Y','N');
DELETE FROM b_eventaction_mapping  WHERE  event_name='Event Order';
insert ignore into m_permission values(null,'billing','ACTIVE_PROVISIONACTIONS','PROVISIONACTIONS','ACTIVE',0);
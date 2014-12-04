CREATE TABLE IF NOT EXISTS `b_addons` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `plan_id` bigint(10) NOT NULL,
  `service_id` bigint(10) NOT NULL,
  `charge_code` varchar(10) NOT NULL,
  `price_region_id` bigint(10) NOT NULL,
  `price` decimal(19,6) NOT NULL,
  `is_deleted` char(1) DEFAULT 'N',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `unique_addon_price` (`plan_id`,`service_id`,`charge_code`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='utf8_general_ci';


CREATE TABLE IF NOT EXISTS `b_orders_addons` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(10) NOT NULL,
  `service_id` bigint(10) NOT NULL,
  `contract_id` bigint(10) NOT NULL,
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `status` varchar(10) DEFAULT NULL,
  `provision_system` varchar(10) DEFAULT NULL,
  `is_deleted` char(1) DEFAULT 'N',
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='utf8_general_ci';


Drop procedure IF EXISTS serviceProvisioning; 
DELIMITER //
create procedure serviceProvisioning() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME ='provision_system'  
      and TABLE_NAME = 'b_prov_service_details'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_prov_service_details add column provision_system varchar(10) DEFAULT NULL after `sub_category`;

END IF;
END //
DELIMITER ;
call serviceProvisioning();
Drop procedure IF EXISTS serviceProvisioning;
INSERT IGNORE INTO m_code VALUES(null ,'Agreement Type', '0','Describe the agreement status');
SET @id=(select id from m_code where code_name='Agreement Type');
INSERT IGNORE INTO m_code_value VALUES(null, @id, 'Signed', '0');
INSERT IGNORE INTO m_code_value VALUES(null, @id, 'Pending', '0');


INSERT IGNORE INTO m_code VALUES(null ,'Source Category', '0','Describe the different sources');
SET @id=(select id from m_code where code_name='Source Category');
INSERT IGNORE INTO m_code_value VALUES(null, @id, 'Subscriptions', '0');
INSERT IGNORE INTO m_code_value VALUES(null, @id, 'Hardware', '1');
INSERT IGNORE INTO m_code_value VALUES(null, @id, 'On-demand', '2');

INSERT IGNORE INTO  m_permission VALUES (null,'organization', 'CREATE_PARTNERAGREEMENT', 'PARTNERAGREEMENT', 'CREATE', 0);
INSERT IGNORE INTO  m_permission VALUES (null,'organization', 'CREATE_PARTNER', 'PARTNER', 'CREATE', 0);



CREATE TABLE IF NOT EXISTS `m_office_agreement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `partner_id` bigint(20) NOT NULL,
  `office_id` bigint(20) NOT NULL,
  `agreement_status` varchar(20) NOT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `createdby_id` int(5) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` int(5) DEFAULT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`id`),
  KEY `fk_office_agreement` (`office_id`),
  CONSTRAINT `fk_office_agreement` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
  
  CREATE TABLE IF NOT EXISTS `m_office_agreement_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `agreement_id` bigint(20) NOT NULL,
  `source` int(10) NOT NULL,
  `share_amount` decimal(6,2) NOT NULL,
  `share_type` varchar(10) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
  UNIQUE KEY `agreement_dtl_ai_ps_mc_uniquekey` (`agreement_id`,`source`),
  KEY `fk_agreement` (`agreement_id`),
  CONSTRAINT `fk_agreement_2` FOREIGN KEY (`agreement_id`) REFERENCES `m_office_agreement` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


Drop procedure if exists officeInfo;
DELIMITER //
create procedure officeInfo()
BEGIN
IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.COLUMNS
WHERE COLUMN_NAME ='is_collective' 
AND TABLE_NAME='m_office_additional_info'
AND TABLE_SCHEMA=DATABASE())THEN
Alter table m_office_additional_info add column is_collective char(2) DEFAULT 'N';
END IF;
END // 
DELIMITER ;
call officeInfo();
Drop procedure if exists officeInfo;


CREATE TABLE IF NOT EXISTS `m_controlaccount_balance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) NOT NULL,
  `account_type` varchar(50) NOT NULL,
  `balance_amount`  decimal(6,2) NOT NULL,
  `createdby_id` int(5) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` int(5) DEFAULT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `m_office_balance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) NOT NULL,
  `balance_amount`  decimal(6,2) NOT NULL,
  `createdby_id` int(5) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` int(5) DEFAULT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


Drop procedure if exists officeAddress;
DELIMITER //
create procedure officeAddress()
BEGIN
IF EXISTS(SELECT * FROM INFORMATION_SCHEMA.COLUMNS
WHERE COLUMN_NAME ='company_logo' 
AND TABLE_NAME='b_office_address'
AND TABLE_SCHEMA=DATABASE())THEN
ALTER TABLE b_office_address CHANGE COLUMN  company_logo company_logo VARCHAR(500)  DEFAULT NULL;
END IF;
END // 
DELIMITER ;
call officeAddress();
Drop procedure if exists officeAddress;
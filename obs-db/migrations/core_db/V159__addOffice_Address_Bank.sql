CREATE TABLE IF NOT EXISTS `b_office_bank` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_name` varchar(100) DEFAULT NULL,
  `accno_1` varchar(200) DEFAULT NULL,
  `accno_2` varchar(200) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `bank_name` varchar(100) DEFAULT NULL,
  `branch` varchar(100) DEFAULT NULL,
  `swift_code` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `b_office_address` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `address_name` varchar(100) DEFAULT NULL,
  `line_1` varchar(200) DEFAULT NULL,
  `line_2` varchar(200) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `zip` varchar(20) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `email_id` varchar(50) DEFAULT NULL,
  `company_logo` varchar(20) DEFAULT NULL,
  `TIN` varchar(45) DEFAULT NULL,
  `VRN` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

Drop procedure IF EXISTS officeAddress; 
DELIMITER //
create procedure officeAddress() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_NAME = 'b_office_address' and COLUMN_NAME ='TIN' and COLUMN_NAME ='VRN'
     and TABLE_SCHEMA = DATABASE())THEN
alter table `b_office_address` add column `TIN` varchar(45) DEFAULT NULL;
alter table `b_office_address` add column `VRN` varchar(45) DEFAULT NULL;
END IF;
END //
DELIMITER ;
call officeAddress();
Drop procedure IF EXISTS officeAddress;


Drop procedure IF EXISTS paymentDates; 
DELIMITER //
create procedure paymentDates() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_NAME = 'b_paymentgateway' and COLUMN_NAME ='lastmodified_date' and COLUMN_NAME ='lastmodifiedby_id'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER TABLE b_paymentgateway ADD COLUMN `lastmodified_date` datetime DEFAULT NULL after `created_date`;
ALTER TABLE b_paymentgateway ADD COLUMN `lastmodifiedby_id` bigint(20) DEFAULT NULL after `created_date`;

END IF;
END //
DELIMITER ;
call paymentDates();
Drop procedure IF EXISTS paymentDates;





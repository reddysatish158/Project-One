-- INSERT IGNORE INTO c_paymentgateway_conf VALUES(null,'neteller',1,'');
INSERT IGNORE INTO m_code VALUES(null,'Partner Type',0,'Partner are created and mapped to type');

SET @id=(select id from m_code where code_name='Partner Type');

INSERT IGNORE INTO m_code_value VALUES(null,@id,'Partner',2);
INSERT IGNORE INTO m_code_value VALUES(null,@id,'Reseller',1);

CREATE TABLE IF NOT EXISTS `b_office_additional_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `office_id` bigint(20) NOT NULL,
  `partner_name` varchar(50) NOT NULL,
  `partner_type` int(10) NOT NULL,
   PRIMARY KEY (`id`),
   KEY `partner_office_key` (`office_id`),
  CONSTRAINT `partner_office_key` FOREIGN KEY (`office_id`) REFERENCES `m_office` (`id`)
);

SET SQL_SAFE_UPDATES=0;
SET FOREIGN_KEY_CHECKS=0;

Drop procedure if exists officeAddress;
DELIMITER //
create procedure officeAddress()
BEGIN
IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.COLUMNS
WHERE COLUMN_NAME ='office_id' 
AND TABLE_NAME='b_office_address'
AND TABLE_SCHEMA=DATABASE())THEN
ALTER TABLE b_office_address add column `office_id` bigint(20) not null,
 ADD CONSTRAINT `partner_officeaddress_key`
 FOREIGN KEY(`office_id`)
 REFERENCES `m_office` (`id`);
END IF;
END // 
DELIMITER ;
call officeAddress();

Drop procedure if exists officeAddress;

SET SQL_SAFE_UPDATES=1;
SET FOREIGN_KEY_CHECKS=1;
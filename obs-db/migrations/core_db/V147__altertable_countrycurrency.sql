Drop procedure IF EXISTS countryCurrency; 
DELIMITER //
create procedure countryCurrency() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'base_currency'
     and TABLE_NAME = 'b_country_currency'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_country_currency add column base_currency varchar(20) default null;
END IF;

IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'conversion_rate'
     and TABLE_NAME = 'b_country_currency'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_country_currency add column conversion_rate decimal (12,2) default null;
END IF;
END //
DELIMITER ;
call countryCurrency();
Drop procedure IF EXISTS countryCurrency;  

UPDATE b_country_currency SET base_currency='TZS' WHERE country='Tanzania';
UPDATE b_country_currency SET conversion_rate=1650.00 WHERE country='Tanzania';


Drop procedure IF EXISTS deviceId; 
DELIMITER //
create procedure deviceId() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'device_id'
     and TABLE_NAME = 'b_clientuser'
     and TABLE_SCHEMA = DATABASE())THEN 
alter table b_clientuser Add column device_id varchar(20) default null after unique_reference;
END IF;
END //
DELIMITER ;
call deviceId();
Drop procedure IF EXISTS deviceId;

Drop procedure IF EXISTS addVoucherPriceId;
DELIMITER //
create procedure addVoucherPriceId() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'price_id'
     and TABLE_NAME = 'b_pin_master'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_pin_master add column price_id bigint(20) NOT NULL after pin_value;

END IF;
END //
DELIMITER ;
call addVoucherPriceId();
Drop procedure IF EXISTS addVoucherPriceId;

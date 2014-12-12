Drop procedure IF EXISTS addAllocationTypeInAssociation; 
DELIMITER //
create procedure addAllocationTypeInAssociation() 
Begin
IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE TABLE_NAME = 'b_association' and COLUMN_NAME ='allocation_type'
     and TABLE_SCHEMA = DATABASE())THEN
 ALTER TABLE b_association ADD COLUMN allocation_type varchar(5) NOT NULL;
END IF;
END //
DELIMITER ;
call addAllocationTypeInAssociation();
Drop procedure IF EXISTS addAllocationTypeInAssociation;

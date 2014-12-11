SET SQL_SAFE_UPDATES = 0;

UPDATE m_client t1
        INNER JOIN b_clientuser t2 
             ON t1.id = t2.client_id
SET t1.external_id = t2.national_id 
WHERE t2.national_id <> '' and  t2.national_id <> Null;

Drop procedure IF EXISTS nationalIdDrop; 
DELIMITER //
create procedure nationalIdDrop() 
Begin
  IF  EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'national_id'
     and TABLE_NAME = 'b_clientuser'
     and TABLE_SCHEMA = DATABASE())THEN 
alter table b_clientuser drop column national_id; 
END IF;
END //
DELIMITER ;
call nationalIdDrop();
Drop procedure IF EXISTS nationalIdDrop;

SET SQL_SAFE_UPDATES = 1;
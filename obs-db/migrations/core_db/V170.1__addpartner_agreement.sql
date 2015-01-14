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



CREATE TABLE IF NOT EXISTS `b_agreement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `partner_account_id` bigint(20) NOT NULL,
  `agreement_status` varchar(20) NOT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `createdby_id` int(5) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` int(5) DEFAULT NULL,
  `is_deleted` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
  
  CREATE TABLE IF NOT EXISTS `b_agreement_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `agreement_id` bigint(20) NOT NULL,
  `source` int(10) NOT NULL,
  `royalty_share` decimal(6,2) NOT NULL,
  `share_type` varchar(10) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
  UNIQUE KEY `b_agreement_dtl_ai_ps_mc_uniquekey` (`agreement_id`,`source`),
  KEY `fk_b_agreement` (`agreement_id`),
  CONSTRAINT `fk_b_agreement_2` FOREIGN KEY (`agreement_id`) REFERENCES `b_agreement` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
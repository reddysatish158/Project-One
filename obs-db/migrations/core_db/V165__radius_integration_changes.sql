insert ignore into m_code_value values(null,13,'Radius',7);
SET SQL_SAFE_UPDATES = 0;
update job set name='RADIUS', display_name='Radius Integration',job_key='1RADIUSJobDetaildefault _ DEFAULT' where name="MIDDLEWARE";
update job_parameters set param_value='Radius' where param_name='ProvSystem';
update job_parameters set param_value='hugo' where param_name='Username';
update job_parameters set param_value='hugoadmin' where param_name='Password';
update job_parameters set param_value='http://v241.streamingmedia.is:8556/' where param_name='URL';
SET SQL_SAFE_UPDATES = 1;
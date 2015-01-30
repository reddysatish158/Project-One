insert ignore into r_enum_value VALUES ('radius',1,'version-1','version-1');
insert ignore into r_enum_value VALUES ('radius',2,'version-2','version-2');
update job_parameters set param_name='system' where param_name='ProvSystem';

SET @id=(select id from job where name='RADIUS');
insert ignore into job_parameters values(null,@id,'Mikrotik_api','String',null,
'{"ip":"","uname":"","pwd":""}','N',null);

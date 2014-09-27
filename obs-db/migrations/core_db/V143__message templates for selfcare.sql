delete from b_message_template where template_description = "CREATE SELFCARE";

INSERT IGNORE INTO b_message_template(template_description,subject,header,body,footer,message_type) values ('CREATE SELFCARE','Streaming Media Selfcare','Dear <PARAM1>','Your Selfcare User Account has been successfully created,Following are the User login Details. \n userName : <PARAM2> , \n password : <PARAM3> .','Thankyou','E');

delete from b_message_template where template_description = "SELFCARE REGISTRATION";

INSERT IGNORE INTO b_message_template(template_description,subject,header,body,footer,message_type) values ('SELFCARE REGISTRATION','Register Confirmation','Hai','Your Registration has been successfully completed.To approve this Registration please click on this link: \n URL : <PARAM1>.','Thankyou','E');

delete from b_message_template where template_description = "NEW SELFCARE PASSWORD";

INSERT IGNORE INTO b_message_template(template_description,subject,header,body,footer,message_type) values ('NEW SELFCARE PASSWORD','Reset Password','Dear <PARAM1>','The password for your SelfCare User Portal Account- <PARAM2>  was reset. . \n Password : <PARAM3>.','Thankyou','E');

INSERT IGNORE INTO b_message_template(template_description,subject,header,body,footer,message_type) values ('PROVISION CREDENTIALS','Streaming Media Provision Credentials','Dear <PARAM1>','Your Beenius Subscriber Account has been successfully created And Following are the Beenius Account Details.  \n subscriberUid : <PARAM2> , \n Authpin : <PARAM3> .','Thankyou','E');

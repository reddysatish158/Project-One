package org.mifosplatform.infrastructure.configuration.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ConfigurationConstants {
	
	public static final String CONFIG_PROPERTY_IMPLICIT_ASSOCIATION = "Implicit Association";
	public static final String CONFIG_PROPERTY_BALANCE_CHECK = "Forcible Balance Check";
	public static final String CONFIG_PROPERTY_AUTO_RENEWAL = "renewal";
	public static final String CONFIG_PROPERTY_LOGIN = "Login";
	public static final String CONFIG_PROPERTY_DATEFORMAT = "DateFormat";
	public static final String CONFIG_PROPERTY_ROUNDING = "Rounding";
	public static final String CPE_TYPE = "CPE_TYPE";
	public static final String CONFIR_PROPERTY_SALE = "SALE";
	public static final String CONFIR_PROPERTY_OWN = "OWN";
	public static final String CONFIR_PROPERTY_SELF_REGISTRATION = "Register_plan";
	public static final String CONFIR_PROPERTY_REGISTRATION_DEVICE = "Registration_requires_device";
	public static final String CONFIG_PROPERTY = "Implicit Association";
	public static final String CONFIG_DISCONNECT = "Disconnection Credit";
	public static final String CONFIG_CHANGE_PLAN_ALIGN_DATES = "CHANGE_PLAN_ALIGN_DATES";
	public static final String CONFIG_IS_SELFCAREUSER = "is_selfcareuser";  

	public static final String ENABLED = "enabled";
	public static final String VALUE = "value";
	public static final String ID = "id";
	public static final String NAME = "userName";
	public static final String MAIL = "mailId";
	public static final String PASSWORD = "password";
	public static final String HOSTNAME = "hostName";
	public static final String PORT = "port";
	public static final String STARTTLS = "starttls";
	public static final String CONFIGURATION_RESOURCE_NAME = "globalConfiguration";
	public static final Set<String> UPDATE_CONFIGURATION_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(ENABLED, VALUE));
	public static final Set<String> CREATE_CONFIGURATION_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(NAME, MAIL,PASSWORD,HOSTNAME,PORT,STARTTLS));
	public static final String CONFIG_PROPERTY_IS_PAYPAL_CHECK = "Is_Paypal";
	public static final String CONFIG_PROPERTY_IS_PAYPAL_CHECK_IOS = "Is_Paypal_For_Ios";
	public static final String CONFIG_PROPERTY_IS_ACTIVE_VIEWERS = "Active Viewers";

	//Constants
	public static final char CONST_IS_Y = 'Y';
	public static final char CONST_IS_N = 'N';

}

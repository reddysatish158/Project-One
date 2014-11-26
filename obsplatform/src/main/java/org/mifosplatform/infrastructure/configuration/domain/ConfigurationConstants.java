package org.mifosplatform.infrastructure.configuration.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class ConfigurationConstants {
	
	public static final String CONFIG_PROPERTY_MAKER_CHECKER="maker-checker";
	public static final String CONFIG_PROPERTY_AMAZON_C3="amazon-S3";
	public static final String CONFIG_PROPERTY_IMPLICIT_ASSOCIATION = "implicit-association";
	public static final String CONFIG_PROPERTY_BALANCE_CHECK = "forcible-balance-check";
	public static final String CONFIG_PROPERTY_AUTO_RENEWAL = "renewal";
	public static final String CONFIG_PROPERTY_LOGIN = "login";
	public static final String CONFIG_PROPERTY_DATEFORMAT = "date-format";
	public static final String CONFIG_PROPERTY_ROUNDING = "rounding";
	public static final String CONFIG_PROPERTY_DEVICE_AGREMENT_TYPE = "device-agrement-type";
	public static final String CONFIR_PROPERTY_SALE = "SALE";
	public static final String CONFIR_PROPERTY_OWN = "OWN";
	public static final String CONFIR_PROPERTY_SELF_REGISTRATION = "register-plan";
	public static final String CONFIR_PROPERTY_REGISTRATION_DEVICE = "registration-requires-device";
	public static final String CONFIG_DISCONNECT = "disconnection-credit";
	public static final String CONFIG_CHANGE_PLAN_ALIGN_DATES = "change-plan-align-dates";
	public static final String CONFIG_IS_SELFCAREUSER = "is-selfcareuser";
	public static final String CONFIG_PROPERTY_IS_PAYPAL_CHECK = "is-paypal";
	public static final String CONFIG_PROPERTY_IS_PAYPAL_CHECK_IOS = "is-paypal-for-ios";
	public static final String CONFIG_PROPERTY_IS_ACTIVE_VIEWERS = "active-viewers";
	public static final String CONFIG_PROPERTY_IS_ACTIVE_DEVICES = "active-devices";
	public static final String CONFIG_PROPERTY_INCLUDE_NETWORK_BROADCAST_IP = "include-network-broadcast-ip";
	public static final String CONFIG_PROPERTY_CONSTAINT_APPROACH_FOR_DATATABLES= "constraint-approach-for-datatables";
	public static final String CONFIG_PROPERTY_SELFCATE_REQUIRES_EMAIL= "selfcare-requires-email";
	public static final String CONFIG_PROPERTY_OSD_PROVISIONING_SYSTEM= "osd-provisioningSystem";
	public static final String CONFIG_PROPERTY_MEDIA_CRASH_EMAIL = "media-crash-email";
	public static final String CONFIG_PROPERTY_ONLINEPAYMODE = "online-paymode";
	
	public static final String CONFIG_PROPERTY_SMTP= "smtp";

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
	
	//Paymentgateway configurations
	public static final String PAYMENTGATEWAY_MPESA = "MPESA";
	public static final String PAYMENTGATEWAY_TIGO = "TIGO";
	public static final String PAYMENTGATEWAY_ONLINEPAYMENT = "ONLINE_PAYMENT";
	
	public static final String KORTA_PAYMENTGATEWAY = "korta";
	public static final String DALPAY_PAYMENTGATEWAY = "dalpay";
	public static final String GLOBALPAY_PAYMENTGATEWAY = "globalpay";
	public static final String PAYPAL_PAYMENTGATEWAY = "paypal";
	
	

	//Constants
	public static final char CONST_IS_Y = 'Y';
	public static final char CONST_IS_N = 'N';
	

}

package org.mifosplatform.freeradius.radius.service;


/**
 * @author hugo
 * 
 */
public interface RadiusReadPlatformService {

	String retrieveAllNasDetails();
	
	String createNas(String Json);

	String retrieveNasDetail(Long nasId);
	
	String deleteNasDetail(Long nasId);
	
	String retrieveAllRadServiceDetails(String attribute);

	String createRadService(String Json);

	String retrieveRadServiceDetail(Long radServiceId);
	
	String deleteRadService(Long radServiceId);

	String retrieveRadServiceTemplateData();



}

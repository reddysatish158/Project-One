package org.mifosplatform.portfolio.service.data;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.billing.emun.data.EnumValuesData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;


public class ServiceMasterOptionsData {
private Collection<ServiceMasterData> serviceMasterOptions;
private Long id;
private String serviceCode;
private String serviceDescription;
private String serviceType;
private String serviceStatus;
private String serviceUnitType;
private String isOptional;
private Collection<EnumValuesData> serviceTypes;
private List<EnumOptionData> serviceUnitTypes,status;

public ServiceMasterOptionsData(final Collection<ServiceMasterData> serviceMasterOptions)
{
	this.serviceMasterOptions=serviceMasterOptions;
}

public ServiceMasterOptionsData(final Long id, final String serviceCode,
		final String serviceDescription,final String serviceType, final String serviceUnitType, final String status,
		final String isOptional, final String isAutoProvision) {
	this.id=id;
	this.serviceDescription=serviceDescription;
	this.serviceCode=serviceCode;
	this.serviceType=serviceType;
	this.serviceUnitType=serviceUnitType;
	this.serviceStatus=status;
	this.isOptional=isOptional;

}


public ServiceMasterOptionsData(final Collection<EnumValuesData> serviceType,
		final List<EnumOptionData> serviceUnitType, final List<EnumOptionData> status) {
	this.serviceTypes=serviceType;
	this.serviceUnitTypes=serviceUnitType;
	this.status=status;
}

public Collection<ServiceMasterData> getServiceMasterOptions() {
	return serviceMasterOptions;
}

public Long getId() {
	return id;
}

public String getServiceCode() {
	return serviceCode;
}

public String getserviceDescription() {
	return serviceDescription;
}

public String getServiceType() {
	return serviceType;
}

public void setServiceMasterOptions(
		final Collection<ServiceMasterData> serviceMasterOptions) {
	this.serviceMasterOptions = serviceMasterOptions;
}

public void setId(final Long id) {
	this.id = id;
}

public void setServiceCode(final String serviceCode) {
	this.serviceCode = serviceCode;
}

public void setServiceDescription(final String serviceDescription) {
	this.serviceDescription = serviceDescription;
}

public void setServiceType(final String serviceType) {
	this.serviceType = serviceType;
}

public void setServiceStatus(final String serviceStatus) {
	this.serviceStatus = serviceStatus;
}

public void setServiceUnitType(final String serviceUnitType) {
	this.serviceUnitType = serviceUnitType;
}

public void setIsOptional(final String isOptional) {
	this.isOptional = isOptional;
}

public void setServiceTypes(final Collection<EnumValuesData> serviceTypes) {
	this.serviceTypes = serviceTypes;
}

public void setServiceUnitTypes(final List<EnumOptionData> serviceUnitTypes) {
	this.serviceUnitTypes = serviceUnitTypes;
}

public void setStatus(final List<EnumOptionData> status) {
	this.status = status;
}

public String getServiceDescription() {
	return serviceDescription;
}

public String getServiceStatus() {
	return serviceStatus;
}

public String getServiceUnitType() {
	return serviceUnitType;
}

public String getIsOptional() {
	return isOptional;
}

public Collection<EnumValuesData> getServiceTypes() {
	return serviceTypes;
}

public List<EnumOptionData> getServiceUnitTypes() {
	return serviceUnitTypes;
}

public List<EnumOptionData> getStatus() {
	return status;
}



}

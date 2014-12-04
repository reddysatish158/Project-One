package org.mifosplatform.portfolio.servicemapping.service;

import java.util.List;

import org.mifosplatform.portfolio.plan.data.ServiceData;
import org.mifosplatform.portfolio.servicemapping.data.ServiceCodeData;
import org.mifosplatform.portfolio.servicemapping.data.ServiceMappingData;
import org.mifosplatform.provisioning.provisioning.data.ServiceParameterData;

public interface ServiceMappingReadPlatformService {
	
	
	List<ServiceCodeData> getServiceCode();
	
	List<ServiceMappingData> getServiceMapping();

	ServiceMappingData getServiceMapping(Long serviceMappingId);

	List<ServiceParameterData> getSerivceParameters(Long orderId, Long serviceId);

	List<ServiceMappingData> retrieveOptionalServices(String string);
	
//	ServiceMappingData getServiceMapping(Long serviceMappingId);

}

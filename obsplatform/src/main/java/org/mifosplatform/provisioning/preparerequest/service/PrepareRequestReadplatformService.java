package org.mifosplatform.provisioning.preparerequest.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.provisioning.preparerequest.data.PrepareRequestData;
public interface PrepareRequestReadplatformService {

	List<PrepareRequestData> retrieveDataForProcessing();
	
	List<Long> retrieveRequestClientOrderDetails(Long clientId);
	
	CommandProcessingResult processingClientDetails(PrepareRequestData requestData, String configProp);
	
	List<Long> getPrepareRequestDetails(Long id);
	
	int getLastPrepareId(Long orderId);

}

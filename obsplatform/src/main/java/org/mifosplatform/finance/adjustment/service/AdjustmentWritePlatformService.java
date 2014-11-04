package org.mifosplatform.finance.adjustment.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;


public interface AdjustmentWritePlatformService {
	 Long createAdjustment(final Long id2,final Long id,final Long clientid,final JsonCommand command);
	
	 CommandProcessingResult createAdjustments(JsonCommand command);

}

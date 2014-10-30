package org.mifosplatform.organisation.voucher.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * 
 * @author ashokreddy
 *
 */
public interface VoucherWritePlatformService {

	CommandProcessingResult createRandomGenerator(JsonCommand command);

	CommandProcessingResult generateVoucherPinKeys(Long batchId);
	

	
}

package org.mifosplatform.billing.chargecode.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * @author hugo
 *
 */
public interface ChargeCodeWritePlatformService {

	 CommandProcessingResult createChargeCode(JsonCommand command);

	 CommandProcessingResult updateChargeCode(JsonCommand command,
			Long chargeCodeId);
}

package org.mifosplatform.organisation.partner.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface PartnersWritePlatformService {

	CommandProcessingResult createNewPartner(JsonCommand command);
	
}

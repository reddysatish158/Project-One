package org.mifosplatform.organisation.partneragreement.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface PartnersAgreementWritePlatformService {

	CommandProcessingResult createNewPartnerAgreement(JsonCommand command);

	
}

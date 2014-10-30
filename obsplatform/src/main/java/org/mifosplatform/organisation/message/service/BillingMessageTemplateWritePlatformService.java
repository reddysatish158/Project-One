package org.mifosplatform.organisation.message.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * 
 * @author ashokreddy
 *
 */
public interface BillingMessageTemplateWritePlatformService {
	
CommandProcessingResult addMessageTemplate(final JsonCommand json);

CommandProcessingResult updateMessageTemplate(final JsonCommand command);

CommandProcessingResult deleteMessageTemplate(final JsonCommand command);

}

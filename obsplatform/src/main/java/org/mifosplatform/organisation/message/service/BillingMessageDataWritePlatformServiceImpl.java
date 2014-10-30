package org.mifosplatform.organisation.message.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.organisation.message.data.BillingMessageTemplateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author ashokreddy
 *
 */
@Service
public class BillingMessageDataWritePlatformServiceImpl implements BillingMessageDataWritePlatformService {

	private final BillingMesssageReadPlatformService billingMesssageReadPlatformService;

	@Autowired
	public BillingMessageDataWritePlatformServiceImpl(final BillingMesssageReadPlatformService billingMesssageReadPlatformService) {
	
		this.billingMesssageReadPlatformService = billingMesssageReadPlatformService;
		
	}

	@Override
	public CommandProcessingResult createMessageData(Long id, String json) {

		final BillingMessageTemplateData templateData = this.billingMesssageReadPlatformService.retrieveMessageTemplate(id);
		
		final List<BillingMessageTemplateData> messageparam = this.billingMesssageReadPlatformService.retrieveMessageParams(id);
		
		final List<BillingMessageTemplateData> clientData = this.billingMesssageReadPlatformService.retrieveData(id, json, 
				templateData, messageparam, billingMesssageReadPlatformService);

		return new CommandProcessingResultBuilder().withCommandId(id).withEntityId(id).build();

	}
}

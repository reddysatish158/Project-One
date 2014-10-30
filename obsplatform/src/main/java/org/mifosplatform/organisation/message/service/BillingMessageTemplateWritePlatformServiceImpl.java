package org.mifosplatform.organisation.message.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.message.domain.BillingMessageParam;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.serialization.BillingMessageTemplateCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * 
 * @author ashokreddy
 *
 */
@Service
public class BillingMessageTemplateWritePlatformServiceImpl implements BillingMessageTemplateWritePlatformService {

	private int number;
	private final PlatformSecurityContext context;
	private final FromJsonHelper fromApiJsonHelper;
	private final BillingMessageTemplateRepository billingMessageTemplateRepository;
	private final BillingMessageTemplateCommandFromApiJsonDeserializer billingMessageTemplateCommandFromApiJsonDeserializer;

	@Autowired
	public BillingMessageTemplateWritePlatformServiceImpl(
			final PlatformSecurityContext context,
			final FromJsonHelper fromApiJsonHelper,
			final BillingMessageTemplateRepository billingMessageTemplateRepository,
			final BillingMessageTemplateCommandFromApiJsonDeserializer billingMessageTemplateCommandFromApiJsonDeserializer) {
		this.context = context;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.billingMessageTemplateRepository = billingMessageTemplateRepository;
		this.billingMessageTemplateCommandFromApiJsonDeserializer = billingMessageTemplateCommandFromApiJsonDeserializer;
	}

	// for post method
	@Override
	public CommandProcessingResult addMessageTemplate(final JsonCommand command) {

		try {
			number = 0;
			context.authenticatedUser();
			this.billingMessageTemplateCommandFromApiJsonDeserializer.validateForCreate(command.json());
			final BillingMessageTemplate billingMessageTemplate = BillingMessageTemplate.fromJson(command);

			final JsonArray billingMessageparamArray = command.arrayOfParameterNamed("messageParams").getAsJsonArray();
					
			if (billingMessageparamArray != null) {
				for (JsonElement jsonelement : billingMessageparamArray) {
					
					number = number + 1;
					final String parameterName = fromApiJsonHelper.extractStringNamed("parameter", jsonelement);
					BillingMessageParam billingMessageParam = new BillingMessageParam(Long.valueOf(number), parameterName);
					
					billingMessageTemplate.setBillingMessageParam(billingMessageParam);
				}
			}
			
			this.billingMessageTemplateRepository.save(billingMessageTemplate);
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(billingMessageTemplate.getId()).build();

		} catch (DataIntegrityViolationException dve) {
			return CommandProcessingResult.empty();
		}

	}

	// for put method
	@Override
	public CommandProcessingResult updateMessageTemplate(JsonCommand command) {
		
		try {
			number = 0;
			context.authenticatedUser();
			this.billingMessageTemplateCommandFromApiJsonDeserializer.validateForCreate(command.json());
			
			final BillingMessageTemplate messageTemplate = retriveMessageBy(command.entityId());
			Map<String, Object> actualChanges = messageTemplate.updateMessageTemplate(command);
			
			messageTemplate.getMessageParamDetails().clear();

			final JsonArray billingMessageparamArray = command.arrayOfParameterNamed("messageParams").getAsJsonArray();
			
			if (billingMessageparamArray != null) {
				for (JsonElement jsonelement : billingMessageparamArray) {
					
					final String parameterName = fromApiJsonHelper.extractStringNamed("parameter", jsonelement);
					number = number + 1;
					BillingMessageParam billingMessageParam = new BillingMessageParam(Long.valueOf(number), parameterName);
					messageTemplate.setBillingMessageParam(billingMessageParam);
				}
			}

			this.billingMessageTemplateRepository.save(messageTemplate);

			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					.withEntityId(messageTemplate.getId()).with(actualChanges).build();

		} catch (DataIntegrityViolationException dve) {
			return CommandProcessingResult.empty();
		}

	}

	@SuppressWarnings("null")
	@Override
	public CommandProcessingResult deleteMessageTemplate(JsonCommand command) {
		// TODO Auto-generated method stub
		context.authenticatedUser();
		final BillingMessageTemplate messageTemplate = retriveMessageBy(command.entityId());
		if (messageTemplate == null) {
			throw new DataIntegrityViolationException(messageTemplate.toString());
		}
		messageTemplate.isDelete();
		this.billingMessageTemplateRepository.save(messageTemplate);
		return new CommandProcessingResultBuilder().withEntityId(command.entityId()).build();
	}

	private BillingMessageTemplate retriveMessageBy(Long messageId) {
		return this.billingMessageTemplateRepository.findOne(messageId);
	}

}

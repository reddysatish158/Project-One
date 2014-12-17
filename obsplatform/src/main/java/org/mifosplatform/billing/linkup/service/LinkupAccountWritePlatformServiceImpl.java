package org.mifosplatform.billing.linkup.service;

import org.mifosplatform.billing.linkup.serialization.LinkupAccountCommandFromApiJsonDeserializer;
import org.mifosplatform.billing.selfcare.domain.SelfCare;
import org.mifosplatform.billing.selfcare.service.SelfCareRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author rakesh
 * 
 */
@Service
public class LinkupAccountWritePlatformServiceImpl implements LinkupAccountWritePlatformService {

	private final PlatformSecurityContext context;
	private final LinkupAccountCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final SelfCareRepository selfCareRepository;
	private final BillingMessageRepository messageDataRepository;
	private final BillingMessageTemplateRepository billingMessageTemplateRepository;

	/**
	 * @param context
	 * @param apiJsonDeserializer
	 *
	 */
	@Autowired
	public LinkupAccountWritePlatformServiceImpl(final PlatformSecurityContext context,
			final LinkupAccountCommandFromApiJsonDeserializer apiJsonDeserializer,
			final SelfCareRepository selfCareRepository,
			final BillingMessageRepository messageDataRepository,
			final BillingMessageTemplateRepository billingMessageTemplateRepository) {
		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.selfCareRepository = selfCareRepository;
		this.messageDataRepository = messageDataRepository;
		this.billingMessageTemplateRepository = billingMessageTemplateRepository;
	}

	@Transactional
	@Override
	public CommandProcessingResult createLinkupAccount(JsonCommand command) {

		try {

			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command);
			
			String uniqueReference = command.stringValueOfParameterNamed("userName");
			
			SelfCare repository=selfCareRepository.findOneByEmail(uniqueReference);
			
			if(repository != null){			
				
				String emailId = repository.getUserName();
				String password = repository.getPassword();
				String body = "Your Linkup Account activated successfully."+"<br/>"+"The following are credentils"+"<br/>"+
								"Username:"+" "+emailId+"&nbsp;"+"and "+"Password:"+" "+password;
				String subject = "Linkup Account";
				String header ="Hai,<br/>";
				String footer = "<br/><br/>Thanks";
				BillingMessageTemplate messageDetails=this.billingMessageTemplateRepository.findByTemplateDescription("NONE");
				
				BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, emailId,
						subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, messageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				
				this.messageDataRepository.save(billingMessage);	
				
			}else{
				throw new ClientNotFoundException(uniqueReference);
			}
			
			return new CommandProcessingResultBuilder().withEntityId(repository.getId()).withClientId(repository.getClientId()).build();

		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	private void handleDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
		
	}

}
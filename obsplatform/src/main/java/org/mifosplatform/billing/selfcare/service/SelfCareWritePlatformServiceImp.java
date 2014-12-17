package org.mifosplatform.billing.selfcare.service;

import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;
import org.mifosplatform.billing.loginhistory.domain.LoginHistory;
import org.mifosplatform.billing.loginhistory.domain.LoginHistoryRepository;
import org.mifosplatform.billing.selfcare.domain.SelfCare;
import org.mifosplatform.billing.selfcare.domain.SelfCareTemporary;
import org.mifosplatform.billing.selfcare.domain.SelfCareTemporaryRepository;
import org.mifosplatform.billing.selfcare.exception.SelfCareAlreadyVerifiedException;
import org.mifosplatform.billing.selfcare.exception.SelfCareEmailIdDuplicateException;
import org.mifosplatform.billing.selfcare.exception.SelfCareTemporaryGeneratedKeyNotFoundException;
import org.mifosplatform.billing.selfcare.exception.SelfcareEmailIdNotFoundException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.service.MessagePlatformEmailService;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.ClientStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;


@Service
public class SelfCareWritePlatformServiceImp implements SelfCareWritePlatformService{
	
	private PlatformSecurityContext context;
	private ClientRepository clientRepository;
	private SelfCareRepository selfCareRepository;
	private final LoginHistoryRepository loginHistoryRepository;
	private MessagePlatformEmailService messagePlatformEmailService;
	private SelfCareReadPlatformService selfCareReadPlatformService;
	private SelfCareTemporaryRepository selfCareTemporaryRepository;
	private final BillingMessageTemplateRepository billingMessageTemplateRepository;
	private SelfCareCommandFromApiJsonDeserializer selfCareCommandFromApiJsonDeserializer;
	private final BillingMessageRepository messageDataRepository;
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SelfCareWritePlatformServiceImp.class);
	

	@Autowired
	public SelfCareWritePlatformServiceImp(final PlatformSecurityContext context, 
			final SelfCareRepository selfCareRepository, 
		    final SelfCareCommandFromApiJsonDeserializer selfCareCommandFromApiJsonDeserializer,
		    final SelfCareReadPlatformService selfCareReadPlatformService, 
			final SelfCareTemporaryRepository selfCareTemporaryRepository,
			final BillingMessageTemplateRepository billingMessageTemplateRepository,
			final MessagePlatformEmailService messagePlatformEmailService,
			final ClientRepository clientRepository,
			final LoginHistoryRepository loginHistoryRepository,
			final BillingMessageRepository messageDataRepository) {
		
		this.context = context;
		this.selfCareRepository = selfCareRepository;
		this.selfCareCommandFromApiJsonDeserializer = selfCareCommandFromApiJsonDeserializer;
		this.selfCareReadPlatformService = selfCareReadPlatformService;
		this.selfCareTemporaryRepository = selfCareTemporaryRepository;
		this.messagePlatformEmailService = messagePlatformEmailService;
		this.billingMessageTemplateRepository = billingMessageTemplateRepository;
		this.messagePlatformEmailService= messagePlatformEmailService;
		this.clientRepository=clientRepository;
		this.loginHistoryRepository=loginHistoryRepository;
		this.messageDataRepository = messageDataRepository;
				
	}
	
	@Override
	public CommandProcessingResult createSelfCare(JsonCommand command) {
		
		SelfCare selfCare = null;
		Long clientId = null;
		String password = null;
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			selfCare = SelfCare.fromJson(command);
			clientId = command.longValueOfParameterNamed("clientId");
			password = command.stringValueOfParameterNamed("password");
			if(clientId == null){
				try{
					clientId = selfCareReadPlatformService.getClientId(selfCare.getUniqueReference());					
				}catch(EmptyResultDataAccessException erdae){
						throw new PlatformDataIntegrityException("this user is not registered","this user is not registered","");
				}catch(Exception e){
					if(e.getMessage() != null){
							throw new PlatformDataIntegrityException("this user not found","this user not found",e.getMessage());
					}else if(e.getCause().getLocalizedMessage() != null){
							throw new PlatformDataIntegrityException("this user not found","this user not found",e.getCause().getLocalizedMessage());
					}else{
							throw new PlatformDataIntegrityException("this user not found","this user not found","");
						}
					}
			}
			
			boolean mailnotification=command.booleanPrimitiveValueOfParameterNamed("mailNotification");
			
			if(clientId !=null && clientId > 0 ){
				
				selfCare.setClientId(clientId);
				if(password != null && password != ""){
					selfCare.setPassword(password);
				}else{
					RandomPasswordGenerator passwordGenerator = new RandomPasswordGenerator(8);
					String unencodedPassword = passwordGenerator.generate();
					selfCare.setPassword(unencodedPassword);
				}
				
				selfCareRepository.save(selfCare);
				if(mailnotification){
				//platformEmailService.sendToUserAccount(new EmailDetail("OBS Self Care Organisation ", "SelfCare",email, selfCare.getUserName()), unencodedPassword); 
				BillingMessageTemplate messageDetails=this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_CREATE_SELFCARE);
				String subject=messageDetails.getSubject();
				String body=messageDetails.getBody();
				String footer=messageDetails.getFooter();
				String header=messageDetails.getHeader().replace("<PARAM1>", selfCare.getUserName() +",");
				body=body.replace("<PARAM2>", selfCare.getUniqueReference());
				body=body.replace("<PARAM3>", selfCare.getPassword());
				/*StringBuilder prepareEmail =new StringBuilder();
				prepareEmail.append(header);
				prepareEmail.append("\t").append(body);
				prepareEmail.append(messageDetails.getFooter());*/
				
				BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, selfCare.getUniqueReference(),
						subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, messageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				
				this.messageDataRepository.save(billingMessage);
				//messagePlatformEmailService.sendGeneralMessage(selfCare.getUniqueReference(), prepareEmail.toString().trim(), subject);
				}
			}else{
				throw new PlatformDataIntegrityException("client does not exist", "client not registered","clientId", "client is null ");
			}
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).withClientId(clientId).build();
	}
	
	@Override
	public CommandProcessingResult createSelfCareUDPassword(JsonCommand command) {
		SelfCare selfCare = null;
		Long clientId = null;
		String ipAddress=command.stringValueOfParameterNamed("ipAddress");
		String session=command.stringValueOfParameterNamed("");
		Long loginHistoryId=null;
		
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreateUDPassword(command);
			selfCare = SelfCare.fromJsonODP(command);
			try{
			clientId = selfCareReadPlatformService.getClientId(selfCare.getUniqueReference());
			if(clientId == null || clientId <= 0 ){
				throw new PlatformDataIntegrityException("client does not exist", "this user is not registered","clientId", "client is null ");
			}
			selfCare.setClientId(clientId);

			selfCareRepository.save(selfCare);
			String username=selfCare.getUserName();
			LoginHistory loginHistory=new LoginHistory(ipAddress,null,session,new Date(),null,username,"ACTIVE");
    		this.loginHistoryRepository.save(loginHistory);
    		loginHistoryId=loginHistory.getId();
			}
			catch(EmptyResultDataAccessException dve){
				throw new PlatformDataIntegrityException("invalid.account.details","invalid.account.details","this user is not registered");
			}
			
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.email", "duplicate.email","duplicate.email", "duplicate.email");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		return new CommandProcessingResultBuilder().withEntityId(loginHistoryId).withClientId(clientId).build();
	}
		
	@Override
	public CommandProcessingResult updateSelfCareUDPassword(JsonCommand command) {
		   SelfCare selfCare=null;
		   context.authenticatedUser();
		   selfCareCommandFromApiJsonDeserializer.validateForUpdateUDPassword(command);
		   String email=command.stringValueOfParameterNamed("uniqueReference");
		   String password=command.stringValueOfParameterNamed("password");
		   selfCare=this.selfCareRepository.findOneByEmail(email);
		   if(selfCare==null){
			   throw new ClientNotFoundException(email);
		   }
		   selfCare.setPassword(password);
		   this.selfCareRepository.save(selfCare);
		   return new CommandProcessingResultBuilder().withEntityId(selfCare.getClientId()).build();
	}	
	
	@Override
	public CommandProcessingResult forgotSelfCareUDPassword(JsonCommand command) {
		SelfCare selfCare=null;
		context.authenticatedUser();
		selfCareCommandFromApiJsonDeserializer.validateForForgotUDPassword(command);
		String email=command.stringValueOfParameterNamed("uniqueReference");
		selfCare=this.selfCareRepository.findOneByEmail(email);
		if(selfCare == null){
			throw new ClientNotFoundException(email);
		}
		String password=selfCare.getPassword();
		Client client= this.clientRepository.findOne(selfCare.getClientId());
		String body="Dear "+client.getDisplayName()+","+"\n"+"Your login information is mentioned below."+"\n"+"Email Id : "+email+"\n"+"Password :"+password+"\n"+"Thanks";
		String subject="Login Information";
		messagePlatformEmailService.sendGeneralMessage(email, body, subject);
		return new CommandProcessingResult(selfCare.getClientId());
	}

	private void handleDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
		 Throwable realCause = dve.getMostSpecificCause();
		 logger.error(dve.getMessage(), dve);
	        if (realCause.getMessage().contains("username")){	
	        	throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.userName", "User Name: " + command.stringValueOfParameterNamed("userName")+ " already exists", "userName", command.stringValueOfParameterNamed("userName"));
	        }else if (realCause.getMessage().contains("unique_reference")){
	        	throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.email", "email: " + command.stringValueOfParameterNamed("uniqueReference")+ " already exists", "email", command.stringValueOfParameterNamed("uniqueReference"));
	        }

	}
	
	@Override
	public CommandProcessingResult updateClientStatus(JsonCommand command,Long entityId) {
            try{
            	
            	this.context.authenticatedUser();
            	String status=command.stringValueOfParameterNamed("status");
            	SelfCare client=this.selfCareRepository.findOneByClientId(entityId);
            	if(client == null){
            		throw new ClientNotFoundException(entityId);
            	}
            	if(status.equalsIgnoreCase("ACTIVE")){
            	
            		if(status.equals(client.getStatus())){
            			throw new ClientStatusException(entityId);
            		}
            	}
            	client.setStatus(status);
            	this.selfCareRepository.save(client);
            	return new CommandProcessingResult(Long.valueOf(entityId));
            	
            }catch(DataIntegrityViolationException dve){
            	handleDataIntegrityIssues(command, dve);
            	return new CommandProcessingResult(Long.valueOf(-1));
            }

	}

	@Override
	public CommandProcessingResult registerSelfCare(JsonCommand command) {
		
		SelfCareTemporary selfCareTemporary = null;
		Long clientId = 0L;
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("userName");
			String returnUrl = command.stringValueOfParameterNamed("returnUrl");
			SelfCare repository=selfCareRepository.findOneByEmail(uniqueReference);
			if(repository != null){				
				throw new SelfCareEmailIdDuplicateException(uniqueReference);				
			}else{		
				selfCareTemporary = SelfCareTemporary.fromJson(command);
				String unencodedPassword = RandomStringUtils.randomAlphanumeric(27);
				selfCareTemporary.setGeneratedKey(unencodedPassword);
				
				selfCareTemporaryRepository.save(selfCareTemporary);
				String generatedKey = selfCareTemporary.getGeneratedKey() + "11011";
				
				BillingMessageTemplate messageDetails=this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SELFCARE_REGISTER);
				String subject=messageDetails.getSubject();
				String body=messageDetails.getBody();
				String header=messageDetails.getHeader()+",";
				String footer=messageDetails.getFooter();
				
				body=body.replace("<PARAM1>", returnUrl + generatedKey);
				/*StringBuilder prepareEmail =new StringBuilder();
				prepareEmail.append(header);
				prepareEmail.append("\t").append(body);
				prepareEmail.append(messageDetails.getFooter());*/
				
				BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, selfCareTemporary.getUserName(),
						subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, messageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				
				this.messageDataRepository.save(billingMessage);
				
				//this.messagePlatformEmailService.sendGeneralMessage(selfCareTemporary.getUserName(), prepareEmail.toString().trim(), subject);
				return new CommandProcessingResultBuilder().withEntityId(selfCareTemporary.getId()).withClientId(clientId).build();
			}
				
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
	}

	@Override
	public CommandProcessingResult selfCareEmailVerification(JsonCommand command) {
		SelfCareTemporary selfCareTemporary = null;
		Long clientId = 0L;
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String verificationKey = command.stringValueOfParameterNamed("verificationKey");
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
			
			
			selfCareTemporary =selfCareTemporaryRepository.findOneByGeneratedKey(verificationKey,uniqueReference);
			
			if(selfCareTemporary == null){				
				throw new SelfCareTemporaryGeneratedKeyNotFoundException(verificationKey,uniqueReference);				
			}else{		
				
				if(selfCareTemporary.getStatus().equalsIgnoreCase("INACTIVE") || selfCareTemporary.getStatus().equalsIgnoreCase("PENDING")){
					
					selfCareTemporary.setStatus("PENDING");
					
				} else{
					throw new SelfCareAlreadyVerifiedException(verificationKey);		
				}
			}
				
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		return new CommandProcessingResultBuilder().withEntityId(selfCareTemporary.getId()).withClientId(clientId).build();
	}

	@Override
	public CommandProcessingResult generateNewSelfcarePassword(JsonCommand command) {
		
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");

			SelfCare selfCare =selfCareRepository.findOneByEmail(uniqueReference);
			
			if(selfCare == null){				
				throw new SelfcareEmailIdNotFoundException(uniqueReference);			
			}else{		
				String generatedKey = RandomStringUtils.randomAlphabetic(10);	
				selfCare.setPassword(generatedKey);
				
				BillingMessageTemplate messageDetails=this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NEW_SELFCARE_PASSWORD);
				String subject=messageDetails.getSubject();
				String body=messageDetails.getBody();
				String footer=messageDetails.getFooter();
				String header=messageDetails.getHeader().replace("<PARAM1>", selfCare.getUserName() +",");
				body=body.replace("<PARAM2>", uniqueReference);
				body=body.replace("<PARAM3>", generatedKey);
				/*StringBuilder prepareEmail =new StringBuilder();
				prepareEmail.append(header);
				prepareEmail.append("\t").append(body);
				//prepareEmail.append("\n").append("\n");
				prepareEmail.append(messageDetails.getFooter());
				messagePlatformEmailService.sendGeneralMessage(selfCare.getUniqueReference(), prepareEmail.toString().trim(), subject);*/
				
				BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, selfCare.getUniqueReference(),
						subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, messageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				
				this.messageDataRepository.save(billingMessage);
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).withClientId(selfCare.getClientId()).build();
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		
	}

	@Override
	public CommandProcessingResult selfcareChangePassword(JsonCommand command) {
		
		try{
			
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
			String password = command.stringValueOfParameterNamed("password");
			SelfCare selfCare =selfCareRepository.findOneByEmail(uniqueReference);
			
			if(selfCare == null){				
				throw new SelfcareEmailIdNotFoundException(uniqueReference);			
			}else{		
				if(command.parameterExists("userName")){
					String userName = command.stringValueOfParameterNamed("userName");
					selfCare.setUserName(userName);
				}
				selfCare.setPassword(password);
				this.selfCareRepository.save(selfCare);
			
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).withClientId(selfCare.getClientId()).build();
			
		} catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
	}
}
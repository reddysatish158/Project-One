package org.mifosplatform.billing.selfcare.service;

import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.model.parameter.Language;

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
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.service.MessagePlatformEmailService;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.ClientStatusException;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
	private TransactionHistoryWritePlatformService transactionHistoryWritePlatformService;
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SelfCareWritePlatformServiceImp.class);
	private final ConfigurationRepository globalConfigurationRepository; 
	@Autowired
	public SelfCareWritePlatformServiceImp(final PlatformSecurityContext context, final SelfCareRepository selfCareRepository, 
		    final SelfCareCommandFromApiJsonDeserializer selfCareCommandFromApiJsonDeserializer,final SelfCareReadPlatformService selfCareReadPlatformService, 
			final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService,final SelfCareTemporaryRepository selfCareTemporaryRepository,
			final BillingMessageTemplateRepository billingMessageTemplateRepository,final MessagePlatformEmailService messagePlatformEmailService,
			ClientRepository clientRepository,final LoginHistoryRepository loginHistoryRepository,final ConfigurationRepository globalConfigurationRepository) {
		
		this.context = context;
		this.selfCareRepository = selfCareRepository;
		this.selfCareCommandFromApiJsonDeserializer = selfCareCommandFromApiJsonDeserializer;
		this.selfCareReadPlatformService = selfCareReadPlatformService;
		this.transactionHistoryWritePlatformService = transactionHistoryWritePlatformService;
		this.selfCareTemporaryRepository = selfCareTemporaryRepository;
		this.messagePlatformEmailService = messagePlatformEmailService;
		this.billingMessageTemplateRepository = billingMessageTemplateRepository;
		this.messagePlatformEmailService= messagePlatformEmailService;
		this.clientRepository=clientRepository;
		this.globalConfigurationRepository=globalConfigurationRepository;
		this.loginHistoryRepository=loginHistoryRepository;
				
	}
	
	@Override
	public CommandProcessingResult createSelfCare(JsonCommand command) {
		
		SelfCare selfCare = null;
		Long clientId = null;
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			selfCare = SelfCare.fromJson(command);
			clientId = command.longValueOfParameterNamed("clientId");
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
			
				String message=null;
			if(clientId !=null && clientId > 0 ){
				
				selfCare.setClientId(clientId);
				RandomPasswordGenerator passwordGenerator = new RandomPasswordGenerator(8);
				String unencodedPassword = passwordGenerator.generate();
				selfCare.setPassword(unencodedPassword);
				selfCareRepository.save(selfCare);
				if(mailnotification){
				//platformEmailService.sendToUserAccount(new EmailDetail("OBS Self Care Organisation ", "SelfCare",email, selfCare.getUserName()), unencodedPassword); 
				List<BillingMessageTemplate> messageDetails=this.billingMessageTemplateRepository.findByTemplateDescription("CREATE SELFCARE");
				String subject=messageDetails.get(0).getSubject();
				String body=messageDetails.get(0).getBody();
				String header=messageDetails.get(0).getHeader().replace("<PARAM1>", selfCare.getUserName() +",");
				body=body.replace("<PARAM2>", selfCare.getUniqueReference());
				body=body.replace("<PARAM3>", selfCare.getPassword());
				StringBuilder prepareEmail =new StringBuilder();
				prepareEmail.append(header);
				prepareEmail.append("\t").append(body);
				//prepareEmail.append("\n").append("\n");
				prepareEmail.append(messageDetails.get(0).getFooter());
				 message = messagePlatformEmailService.sendGeneralMessage(selfCare.getUniqueReference(), prepareEmail.toString().trim(), subject);
			
				
				/*//
				
				
				StringBuilder builder = new StringBuilder();
				builder.append("Dear " + selfCare.getUserName() + "\n");
				builder.append("\n");
				builder.append("Your Selfcare User Account has been successfully created.");
				builder.append("Following are the User login Details. ");
				builder.append("\n");
				builder.append("userName :" + selfCare.getUniqueReference() + ".");
				builder.append("\n");
				builder.append("password :" + selfCare.getPassword() + ".");
				builder.append("\n");
				builder.append("Thankyou");
=======
				Client client= this.clientRepository.findOne(clientId);
				BillingMessageTemplate messageDetails=this.billingMessageTemplateRepository.findByTemplateDescription("SELF CARE");
				String subject=messageDetails.getSubject();
				String body=messageDetails.getBody();
				String header=messageDetails.getHeader().replace("PARAM1", client.getDisplayName()+","+"\n");
				body=body.replace("PARAM2"," "+email );
				body=body.replace("PARAM3",messageDetails.getFooter());
				body=body.replace("PARAM4",selfCare.getUserName());
				body=body.replace("PARAM5", unencodedPassword);
				StringBuilder body1 =new StringBuilder(header).append(body+"\n").append("\n"+"Thanks"+"\n"+messageDetails.getFooter());
				body=new String(body1);
				messagePlatformEmailService.sendGeneralMessage(email, body, subject);
				/*platformEmailService.sendToUserAccount(new EmailDetail("Hugo Self Care Organisation ", "SelfCare",email, selfCare.getUserName()), unencodedPassword);


				
				String message = this.messagePlatformEmailService.sendGeneralMessage(selfCare.getUniqueReference(), builder.toString(), emailSubject);		
				*/
				
				transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care user activation", new Date(), "USerName: "+selfCare.getUserName()+" ClientId" 
						+ selfCare.getClientId() + "Email Sending Result :" + message);
			
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
			transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care user activation", new Date(), "USerName: "+username+" ClientId"+selfCare.getClientId());
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
				
				List<BillingMessageTemplate> messageDetails=this.billingMessageTemplateRepository.findByTemplateDescription("SELFCARE REGISTRATION");
				String subject=messageDetails.get(0).getSubject();
				String body=messageDetails.get(0).getBody();
				String header=messageDetails.get(0).getHeader()+",";
				body=body.replace("<PARAM1>", returnUrl + generatedKey);
				StringBuilder prepareEmail =new StringBuilder();
				prepareEmail.append(header);
				prepareEmail.append("\t").append(body);
			//	prepareEmail.append("\n").append("\n");
				prepareEmail.append(messageDetails.get(0).getFooter());
				
				/*StringBuilder body = new StringBuilder();
				body.append("hi");
				body.append("\n");
				body.append("\n");
				body.append("Your Registration has been successfully completed.");
				body.append("\n");
				body.append("To approve this Registration please click on this link:");
				body.append("\n");
				body.append("URL: " + returnUrl + generatedKey);
				body.append("\n");
				body.append("\n");
				body.append("Thankyou");
				
				String subject = "Register Conformation";*/
				
			//	String translatedText = Translate.execute("Bonjour le monde", "IS","en");
					
				String result = messagePlatformEmailService.sendGeneralMessage(selfCareTemporary.getUserName(), prepareEmail.toString().trim(), subject);
					
				transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care User Registration", new Date(),
						"EmailId: "+selfCareTemporary.getUserName() + ", returnUrl: "+ returnUrl +", Email Sending Resopnse: " + result);
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
					
					transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care User Registration is Verified Through Email", new Date(),
							"EmailId: "+selfCareTemporary.getUserName());			
				} else{
					transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Self Care User Registration is Already Verified Through this GeneratedKey"+verificationKey, new Date(),
							"EmailId: "+selfCareTemporary.getUserName());	
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
				
				List<BillingMessageTemplate> messageDetails=this.billingMessageTemplateRepository.findByTemplateDescription("NEW SELFCARE PASSWORD");
				String subject=messageDetails.get(0).getSubject();
				String body=messageDetails.get(0).getBody();
				String header=messageDetails.get(0).getHeader().replace("<PARAM1>", selfCare.getUserName() +",");
				body=body.replace("<PARAM2>", uniqueReference);
				body=body.replace("<PARAM3>", generatedKey);
				StringBuilder prepareEmail =new StringBuilder();
				prepareEmail.append(header);
				prepareEmail.append("\t").append(body);
				//prepareEmail.append("\n").append("\n");
				prepareEmail.append(messageDetails.get(0).getFooter());
				
				/*StringBuilder body = new StringBuilder();
				body.append("Dear "+selfCare.getUserName() + ",");
				body.append("\n");
				body.append("\n");
				body.append("The password for your SelfCare User Portal Account- "+ uniqueReference +" was reset. .");
				body.append("\n");
				body.append("Password:"+ generatedKey);
				body.append("\n");
				body.append("\n");
				body.append("Thankyou");
				
				String subject = "Reset Password";*/
				
					
				String result = messagePlatformEmailService.sendGeneralMessage(selfCare.getUniqueReference(), prepareEmail.toString().trim(), subject);
					
				transactionHistoryWritePlatformService.saveTransactionHistory(selfCare.getClientId(), "Self Care Password Reset", new Date(),
						"EmailId: "+selfCare.getUniqueReference() + ", Email Sending Resopnse: " + result);
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
				
				selfCare.setPassword(password);
				this.selfCareRepository.save(selfCare);
				
				transactionHistoryWritePlatformService.saveTransactionHistory(selfCare.getClientId(), "Self Care Password Reset", new Date(),
						"EmailId: "+selfCare.getUniqueReference());
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).withClientId(selfCare.getClientId()).build();
			
		} catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
	}
	/*@Override
	public void verifyActiveViewers(String serialNo, Long clientId) {
	   		
       	OwnedHardware ownedHardware =this.ownedHardwareJpaRepository.findBySerialNumber(serialNo, clientId);
       	ownedHardware.setStatus("ACTIVE");
       	this.ownedHardwareJpaRepository.saveAndFlush(ownedHardware);
       	
      }*/


	
}
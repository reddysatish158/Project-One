/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.billing.selfcare.domain.SelfCare;
import org.mifosplatform.billing.selfcare.service.SelfCareRepository;
import org.mifosplatform.cms.eventorder.service.PrepareRequestWriteplatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.configuration.exception.ConfigurationPropertyNotFoundException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentManagementException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.domain.StatusTypeEnum;
import org.mifosplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.mifosplatform.organisation.address.domain.Address;
import org.mifosplatform.organisation.address.domain.AddressRepository;
import org.mifosplatform.organisation.groupsdetails.domain.GroupsDetails;
import org.mifosplatform.organisation.groupsdetails.domain.GroupsDetailsRepository;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.client.data.ClientDataValidator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.InvalidClientStateTransitionException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.order.data.OrderData;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.provisioning.preparerequest.service.PrepareRequestReadplatformService;
import org.mifosplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.mifosplatform.provisioning.provisioning.domain.ServiceParameters;
import org.mifosplatform.provisioning.provisioning.domain.ServiceParametersRepository;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.mifosplatform.provisioning.provsionactions.domain.ProvisionActions;
import org.mifosplatform.provisioning.provsionactions.domain.ProvisioningActionsRepository;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientWritePlatformServiceJpaRepositoryImpl implements ClientWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ClientWritePlatformServiceJpaRepositoryImpl.class);

    private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
    private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
    private final PrepareRequestWriteplatformService prepareRequestWriteplatformService;
    private final PortfolioCommandSourceWritePlatformService  portfolioCommandSourceWritePlatformService;
    private final PlanRepository planRepository;
    private final OrderReadPlatformService orderReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ConfigurationRepository configurationRepository;
    private final ServiceParametersRepository serviceParametersRepository;
    private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
    private final ProvisioningWritePlatformService ProvisioningWritePlatformService;
    private final OrderRepository orderRepository;
    private final PlatformSecurityContext context;
    private final OfficeRepository officeRepository;
    private final AddressRepository addressRepository;
    private final SelfCareRepository selfCareRepository;
    private final CodeValueRepository codeValueRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientDataValidator fromApiJsonDeserializer;
    private final GroupsDetailsRepository groupsDetailsRepository;
    private final ProvisioningActionsRepository provisioningActionsRepository;
  
    
   

    @Autowired
    public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,final AddressRepository addressRepository,
            final ClientRepositoryWrapper clientRepository, final OfficeRepository officeRepository,final ClientDataValidator fromApiJsonDeserializer, 
            final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,final ServiceParametersRepository serviceParametersRepository,
            final ActiondetailsWritePlatformService actiondetailsWritePlatformService,final ConfigurationRepository configurationRepository,
            final ActionDetailsReadPlatformService actionDetailsReadPlatformService,final CodeValueRepository codeValueRepository,
            final OrderReadPlatformService orderReadPlatformService,final ProvisioningWritePlatformService  ProvisioningWritePlatformService,
            final GroupsDetailsRepository groupsDetailsRepository,final OrderRepository orderRepository,final PlanRepository planRepository,
            final PrepareRequestWriteplatformService prepareRequestWriteplatformService,final ClientReadPlatformService clientReadPlatformService,
            final SelfCareRepository selfCareRepository,final PortfolioCommandSourceWritePlatformService  portfolioCommandSourceWritePlatformService,
            final ProvisioningActionsRepository provisioningActionsRepository,final PrepareRequestReadplatformService prepareRequestReadplatformService) {
    	
        this.context = context;
        this.ProvisioningWritePlatformService=ProvisioningWritePlatformService;
        this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
        this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
        this.prepareRequestWriteplatformService=prepareRequestWriteplatformService;
        this.planRepository=planRepository;
        this.groupsDetailsRepository=groupsDetailsRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.orderReadPlatformService=orderReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.serviceParametersRepository = serviceParametersRepository;
        this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
        this.portfolioCommandSourceWritePlatformService=portfolioCommandSourceWritePlatformService;
        this.orderRepository=orderRepository;
        this.clientRepository = clientRepository;
        this.addressRepository=addressRepository;
        this.officeRepository = officeRepository;
        this.provisioningActionsRepository=provisioningActionsRepository;
        this.selfCareRepository=selfCareRepository;
        this.codeValueRepository=codeValueRepository;
        this.configurationRepository=configurationRepository;
       
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClient(final Long clientId,final JsonCommand command) {

        try {

            final AppUser currentUser = this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateClose(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final LocalDate closureDate = command.localDateValueOfParameterNamed(ClientApiConstants.closureDateParamName);
            final Long closureReasonId = command.longValueOfParameterNamed(ClientApiConstants.closureReasonIdParamName);
            final CodeValue closureReason = this.codeValueRepository.findByCodeNameAndId(ClientApiConstants.CLIENT_CLOSURE_REASON, closureReasonId);
            
            final List<OrderData> orderDatas=this.orderReadPlatformService.getActivePlans(clientId, null);
            
            if(!orderDatas.isEmpty()){
            	
            	 throw new ActivePlansFoundException(clientId);
            }

            if (ClientStatus.fromInt(client.getStatus()).isClosed()) {
                final String errorMessage = "Client is already closed.";
                throw new InvalidClientStateTransitionException("close", "is.already.closed", errorMessage);
            } 

            if (client.isNotPending() && client.getActivationLocalDate().isAfter(closureDate)) {
                final String errorMessage = "The client closureDate cannot be before the client ActivationDate.";
                throw new InvalidClientStateTransitionException("close", "date.cannot.before.client.actvation.date", errorMessage,
                        closureDate, client.getActivationLocalDate());
            }

            client.close(currentUser,closureReason, closureDate.toDate());
            this.clientRepository.saveAndFlush(client);
            
            if(client.getEmail() != null){
            	final SelfCare selfCare=this.selfCareRepository.findOneByEmail(client.getEmail());
            	 if(selfCare != null){
            		 selfCare.setIsDeleted(true);
            		 this.selfCareRepository.save(selfCare);
            	 }

            }
            
            
            final List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CLOSE_CLIENT);
			if(actionDetaislDatas.size() != 0){
			this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,command.entityId(), clientId.toString(),null);
			}
			
			
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    
    	}

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

    	final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId", "Client with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo", "Client with accountNo `" + accountNo
                    + "` already exists", "accountNo", accountNo);
        }else if (realCause.getMessage().contains("email_key")) {
            final String email = command.stringValueOfParameterNamed("email");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.email", "Client with email `" + email
                    + "` already exists", "email", email);
            
        }else if (realCause.getMessage().contains("login_key")) {
            final String login = command.stringValueOfParameterNamed("login");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.login", "Client with login `" + login
                    + "` already exists", "login", login);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    
    @Override
    public CommandProcessingResult createClient(final JsonCommand command) {

        try {
            context.authenticatedUser();
            final Configuration configuration=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_IS_SELFCAREUSER);
            
            if(configuration == null){
            	throw new ConfigurationPropertyNotFoundException(ConfigurationConstants.CONFIG_IS_SELFCAREUSER);
            }
            
            this.fromApiJsonDeserializer.validateForCreate(command.json(),configuration.isEnabled());
            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);
            final Office clientOffice = this.officeRepository.findOne(officeId);

            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }

            final Long groupId = command.longValueOfParameterNamed(ClientApiConstants.groupIdParamName);
            final Group clientParentGroup = null;

            final Client newClient = Client.createNew(clientOffice, clientParentGroup, command);
            this.clientRepository.save(newClient);
            
            final Address address = Address.fromJson(newClient.getId(),command);
			this.addressRepository.save(address);

            if (newClient.isAccountNumberRequiresAutoGeneration()) {
                final AccountNumberGenerator accountNoGenerator = this.accountIdentifierGeneratorFactory
                        .determineClientAccountNoGenerator(newClient.getId());
                newClient.updateAccountNo(accountNoGenerator.generate());
                this.clientRepository.saveAndFlush(newClient);
            }

			if (configuration.isEnabled()) {

				final JSONObject selfcarecreation = new JSONObject();
				selfcarecreation.put("userName", newClient.getFirstname());
				selfcarecreation.put("uniqueReference", newClient.getEmail());
				selfcarecreation.put("clientId", newClient.getId());
				selfcarecreation.put("device", command.stringValueOfParameterNamed("device"));
				selfcarecreation.put("mailNotification", true);
				selfcarecreation.put("password", newClient.getPassword());

				final CommandWrapper selfcareCommandRequest = new CommandWrapperBuilder().createSelfCare()
						.withJson(selfcarecreation.toString()).build();
				this.portfolioCommandSourceWritePlatformService.logCommandSource(selfcareCommandRequest);
			}

            
            
            final List<ActionDetaislData> actionDetailsDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_CLIENT);
            if(!actionDetailsDatas.isEmpty()){
            this.actiondetailsWritePlatformService.AddNewActions(actionDetailsDatas,newClient.getId(),newClient.getId().toString(),null);
            }
            
            ProvisionActions provisionActions=this.provisioningActionsRepository.findOneByProvisionType(ProvisioningApiConstants.PROV_EVENT_CREATE_CLIENT);
			
            if(provisionActions != null && provisionActions.isEnable() == 'Y'){
				/*this.prepareRequestWriteplatformService.prepareRequestForRegistration(newClient.getId(),provisionActions.getAction(),
						   provisionActions.getProvisioningSystem());*/
				this.ProvisioningWritePlatformService.postDetailsForProvisioning(newClient.getId(),ProvisioningApiConstants.REQUEST_CLIENT_ACTIVATION,
						               provisionActions.getProvisioningSystem(),null);
			}

            
            
            return new CommandProcessingResultBuilder() 
                    .withCommandId(command.commandId()) 
                    .withOfficeId(clientOffice.getId()) 
                    .withClientId(newClient.getId())
                    .withResourceIdAsString(newClient.getId().toString())
                    .withGroupId(groupId) 
                    .withEntityId(newClient.getId()) 
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        } catch (JSONException e) {
        	   return CommandProcessingResult.empty();
		}
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClient(final Long clientId, final JsonCommand command) {

        try {
            context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            
            final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);
            final Office clientOffice = this.officeRepository.findOne(officeId);
            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }
            final Map<String, Object> changes = clientForUpdate.update(command);
            clientForUpdate.setOffice(clientOffice);
            this.clientRepository.saveAndFlush(clientForUpdate);
            
            if (changes.containsKey(ClientApiConstants.groupParamName)) {
            	
            		final List<ServiceParameters> serviceParameters=this.serviceParametersRepository.findGroupNameByclientId(clientId);
            	   String newGroup=null;
            	   if(clientForUpdate.getGroupName() != null){
            		   final GroupsDetails groupsDetails=this.groupsDetailsRepository.findOne(clientForUpdate.getGroupName());
            		   newGroup=groupsDetails.getGroupName();
            	   }
            		   for(ServiceParameters serviceParameter:serviceParameters){
            		   
            			 final Order  order=this.orderRepository.findOne(serviceParameters.get(0).getOrderId());
            		   
            			 final Plan plan=this.planRepository.findOne(order.getPlanId());
            			 final String oldGroup=serviceParameter.getParameterValue();
            		   if(newGroup == null){
            			   newGroup=plan.getPlanCode();
            		   }
            		   serviceParameter.setParameterValue(newGroup);
            		   this.serviceParametersRepository.saveAndFlush(serviceParameter);
            		   
                      if(order.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())){
                    	  final CommandProcessingResult processingResult=this.prepareRequestWriteplatformService.prepareNewRequest(order, plan, UserActionStatusTypeEnum.CHANGE_GROUP.toString());
               	        this.ProvisioningWritePlatformService.postOrderDetailsForProvisioning(order,plan.getCode(),UserActionStatusTypeEnum.CHANGE_GROUP.toString(),
               			processingResult.resourceId(),oldGroup,null,order.getId(),plan.getProvisionSystem(),null);
                      }
            	   }
            		
            	}
           
            return new CommandProcessingResultBuilder() 
                    .withCommandId(command.commandId()) 
                    .withOfficeId(clientForUpdate.officeId()) 
                    .withClientId(clientId) 
                    .withEntityId(clientId) 
                    .with(changes) 
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult activateClient(final Long clientId, final JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateActivation(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
            final LocalDate activationDate = command.localDateValueOfParameterNamed("activationDate");

            client.activate(fmt, activationDate);

            this.clientRepository.saveAndFlush(client);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(client.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final String imageName, final InputStream inputStream) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            final String imageLocation = FileUtils.saveToFileSystem(inputStream, imageUploadLocation, imageName);

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException(imageName);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClientImage(final Long clientId) {

        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

        // delete image from the file system
        if (StringUtils.isNotEmpty(client.imageKey())) {
            FileUtils.deleteClientImage(clientId, client.imageKey());
        }
        return updateClientImage(clientId, client, null);
    }

    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final Base64EncodedImage encodedImage) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            final String imageLocation = FileUtils.saveToFileSystem(encodedImage, imageUploadLocation, "image");

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException("image");
        }
    }

    private String setupForClientImageUpdate(final Long clientId, final Client client) {
        if (client == null) { throw new ClientNotFoundException(clientId); }

        final String imageUploadLocation = FileUtils.generateClientImageParentDirectory(clientId);
        // delete previous image from the file system
        if (StringUtils.isNotEmpty(client.imageKey())) {
            FileUtils.deleteClientImage(clientId, client.imageKey());
        }

        /** Recursively create the directory if it does not exist **/
        if (!new File(imageUploadLocation).isDirectory()) {
            new File(imageUploadLocation).mkdirs();
        }
        return imageUploadLocation;
    }

    private CommandProcessingResult updateClientImage(final Long clientId, final Client client, final String imageLocation) {
        client.updateImageKey(imageLocation);
        this.clientRepository.save(client);

        return new CommandProcessingResult(clientId);
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    /* (non-Javadoc)
     * @see #updateClientTaxExemption(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonCommand)
     */
    @Transactional
	@Override
	public CommandProcessingResult updateClientTaxExemption(final Long clientId,final JsonCommand command) {
		
		Client clientTaxStatus=null;
		
		try{
			 this.context.authenticatedUser();
			 clientTaxStatus = this.clientRepository.findOneWithNotFoundDetection(clientId);
			 char taxValue=clientTaxStatus.getTaxExemption();
			 final boolean taxStatus=command.booleanPrimitiveValueOfParameterNamed("taxExemption");
			 if(taxStatus){
				  taxValue='Y';
				  clientTaxStatus.setTaxExemption(taxValue);
			 }else{
				 taxValue='N';
				 clientTaxStatus.setTaxExemption(taxValue);
			 }
			 this.clientRepository.save(clientTaxStatus); 
			 return new CommandProcessingResultBuilder().withEntityId(clientTaxStatus.getId()).build();
		 }catch(DataIntegrityViolationException dve){
			 handleDataIntegrityIssues(command, dve);
			 return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

    /* (non-Javadoc)
     * @see #updateClientBillModes(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonCommand)
     */
    @Transactional
	@Override

	public CommandProcessingResult updateClientBillModes(final Long clientId,final JsonCommand command) {

		Client clientBillMode=null;
	
		try{
			 this.context.authenticatedUser();
			 this.fromApiJsonDeserializer.ValidateBillMode(command);
			 clientBillMode=this.clientRepository.findOneWithNotFoundDetection(clientId);
			 final String billMode=command.stringValueOfParameterNamed("billMode");
			 if(billMode.equals(clientBillMode.getBillMode())==false){
				 clientBillMode.setBillMode(billMode);
			 }else{
				 
			 }
		 this.clientRepository.save(clientBillMode); 
		 return new CommandProcessingResultBuilder().withCommandId(command.commandId())
				 .withEntityId(clientBillMode.getId()).build();
		}catch(DataIntegrityViolationException dve){
			 handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
		
	}

    /* (non-Javadoc)
     * @see #createClientParent(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonCommand)
     */
    @Transactional
	@Override

	public CommandProcessingResult createParentClient(final Long entityId,final JsonCommand command) {
  
			Client childClient=null;
			Client parentClient=null;
		
				try {
					this.context.authenticatedUser();
					this.fromApiJsonDeserializer.ValidateParent(command);
					final String parentAcntId=command.stringValueOfParameterNamed("accountNo");
					childClient = this.clientRepository.findOneWithNotFoundDetection(entityId);
					//count no of childs for a given client 
					final Boolean count =this.clientReadPlatformService.countChildClients(entityId);
					parentClient=this.clientRepository.findOneWithAccountId(parentAcntId);
					
						if(parentClient.getParentId() == null && !parentClient.getId().equals(childClient.getId())&&count.equals(false)){	
							childClient.setParentId(parentClient.getId());
							this.clientRepository.saveAndFlush(childClient);
						}else if(parentClient.getId().equals(childClient.getId())){
							final String errorMessage="himself can not be parent to his account.";
							throw new InvalidClientStateTransitionException("Not parent", "himself.can.not.be.parent.to his.account", errorMessage);
						}else if(count){ 
							final String errorMessage="he is already parent to some other clients";
							throw new InvalidClientStateTransitionException("Not Parent", "he.is. already. a parent.to.some other clients", errorMessage);
						}else{
							final String errorMessage="can not be parent to this account.";
							throw new InvalidClientStateTransitionException("Not parent", "can.not.be.parent.to this.account", errorMessage);
						}
						
				return new CommandProcessingResultBuilder().withEntityId(childClient.getId()).withClientId(childClient.getId()).build();
						
			  	}catch(DataIntegrityViolationException dve){
					handleDataIntegrityIssues(command, dve);
					return new CommandProcessingResult(Long.valueOf(-1));
				}
		}
	
	
	/* (non-Javadoc)
	 * @see #deleteChildFromParentClient(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override

	public CommandProcessingResult deleteChildFromParentClient(final Long childId, final JsonCommand command) {
		
		try {
			context.authenticatedUser();
			Client childClient = this.clientRepository.findOneWithNotFoundDetection(childId);
			final Long parentId=childClient.getParentId();
			childClient.setParentId(null);
			this.clientRepository.saveAndFlush(childClient);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(parentId).build();
	
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

}
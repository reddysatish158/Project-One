package org.mifosplatform.provisioning.entitlements.service;

import java.util.List;

import org.mifosplatform.billing.selfcare.domain.SelfCare;
import org.mifosplatform.billing.selfcare.service.SelfCareRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.processrequest.service.ProcessRequestWriteplatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EntitlementWritePlatformServiceImpl implements EntitlementWritePlatformService {

	private final ProcessRequestRepository entitlementRepository;
	private final ProcessRequestWriteplatformService processRequestWriteplatformService;
	private final ClientRepository clientRepository;
	private final SelfCareRepository selfCareRepository;
	private final BillingMessageTemplateRepository billingMessageTemplateRepository;
	private final BillingMessageRepository messageDataRepository;
	

	@Autowired
	public EntitlementWritePlatformServiceImpl(
			final ProcessRequestWriteplatformService processRequestWriteplatformService,
			final ProcessRequestRepository entitlementRepository, 
			final ClientRepository clientRepository,
			final SelfCareRepository selfCareRepository,
			final BillingMessageTemplateRepository billingMessageTemplateRepository,
			final BillingMessageRepository messageDataRepository) {

		this.processRequestWriteplatformService = processRequestWriteplatformService;
		this.entitlementRepository = entitlementRepository;
		this.clientRepository = clientRepository;
		this.selfCareRepository = selfCareRepository;
		this.billingMessageTemplateRepository = billingMessageTemplateRepository;
		this.messageDataRepository = messageDataRepository;
	}

	/* In This create(JsonCommand command) method, 
	 * The provSystem,clientId,Authpin parameters are sends only for beenius integration. 
	 * For sending Beenius Generated Authpin to Client Email Address.
	 */
	
	@Override
	public CommandProcessingResult create(JsonCommand command) {
		String authPin = null;
		String message = null;	
		String provSystem = command.stringValueOfParameterNamed("provSystem");
		String requestType = command.stringValueOfParameterNamed("requestType");
		
		if(provSystem != null && requestType !=null && provSystem.equalsIgnoreCase("Beenius") 
				&& requestType.equalsIgnoreCase("ACTIVATION")){
			
			authPin = command.stringValueOfParameterNamed("authPin");
			Long clientId = command.longValueOfParameterNamed("clientId");	
			
			if(clientId !=null && authPin !=null && authPin.length()>0 && clientId>0){
				
				Client client = this.clientRepository.findOne(clientId);
				SelfCare selfcare = this.selfCareRepository.findOneByClientId(clientId);
				
				if(client == null){
					throw new ClientNotFoundException(clientId);
				}
				
				if(selfcare == null){
					throw new PlatformDataIntegrityException("client does not exist", "client not registered","clientId", "client is null ");
				}
				
				selfcare.setAuthPin(authPin);
				this.selfCareRepository.save(selfcare);
				String Name = client.getLastname();
				
				BillingMessageTemplate messageDetails=this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_PROVISION_CREDENTIALS);
				
				String subject=messageDetails.getSubject();
				String body=messageDetails.getBody();
				String header=messageDetails.getHeader()+","+"\n"+"\n";
				String footer=messageDetails.getFooter();
				
				header = header.replace("<PARAM1>", Name);
				body = body.replace("<PARAM2>", client.getAccountNumber());
				body = body.replace("<PARAM3>", authPin);
				
				BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, client.getEmail(),
						subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, messageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				
				this.messageDataRepository.save(billingMessage);		
			}
		}
		
		if(provSystem != null && requestType !=null && provSystem.equalsIgnoreCase("ZebraOTT") 
				&& requestType.equalsIgnoreCase("ACTIVATION")){
			
			String zebraSubscriberId = command.stringValueOfParameterNamed("zebraSubscriberId");
			Long clientId = command.longValueOfParameterNamed("clientId");	
			
			if(clientId !=null && zebraSubscriberId !=null && zebraSubscriberId.length()>0 && clientId>0){
				
				Client client = this.clientRepository.findOne(clientId);
				SelfCare selfcare = this.selfCareRepository.findOneByClientId(clientId);
				
				if(client == null){
					throw new ClientNotFoundException(clientId);
				}
				
				if(selfcare == null){
					throw new PlatformDataIntegrityException("client does not exist", "client not registered","clientId", "client is null ");
				}
				
				selfcare.setZebraSubscriberId(new Long(zebraSubscriberId));
				this.selfCareRepository.save(selfcare);
								
			}
			
		}
		
		ProcessRequest processRequest = this.entitlementRepository.findOne(command.entityId());
		String receiveMessage = command.stringValueOfParameterNamed("receiveMessage");
		char status;
		if (receiveMessage.contains("failure :")) {
			status = 'F';
		} else {
			status = 'Y';
		}
			
		List<ProcessRequestDetails> details = processRequest.getProcessRequestDetails();
		

		for (ProcessRequestDetails processRequestDetails : details) {
			Long id = command.longValueOfParameterNamed("prdetailsId");
			if (processRequestDetails.getId().longValue() == id.longValue()) {
				processRequestDetails.updateStatus(command);

				if(provSystem != null && requestType !=null && authPin !=null && provSystem.equalsIgnoreCase("Beenius") && requestType.equalsIgnoreCase("ACTIVATION") ){
					processRequestDetails.setReceiveMessage(processRequestDetails.getReceiveMessage() +
							", generated authpin=" + authPin + ", Email output=" + message);
				}else{
					processRequestDetails.setReceiveMessage(processRequestDetails.getReceiveMessage());
				}
				
			}
		}

		/*if (processRequest.getRequestType().equalsIgnoreCase("DEVICE_SWAP") && !checkProcessDetailsUpdated(details)) {
			status = 'F';
		}*/
		processRequest.setProcessStatus(status);

		// this.entitlementRepository.save(processRequest);
		this.processRequestWriteplatformService.notifyProcessingDetails(processRequest, status);
		
		return new CommandProcessingResult(processRequest.getId());

	}

	/*private boolean checkProcessDetailsUpdated(List<ProcessRequestDetails> details) {
		boolean flag = true;
		if (details.get(0).getReceiveMessage().contains("failure : Exce")) {
			flag = false;
		}
		return flag;
	}*/

}

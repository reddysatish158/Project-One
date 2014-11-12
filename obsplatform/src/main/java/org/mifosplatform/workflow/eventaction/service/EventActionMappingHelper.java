package org.mifosplatform.workflow.eventaction.service;

import java.util.List;

import org.mifosplatform.crm.ticketmaster.data.TicketMasterData;
import org.mifosplatform.crm.ticketmaster.domain.TicketMaster;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.exception.EmailNotFoundException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class EventActionMappingHelper {
	
	private final BillingMessageTemplateRepository messageTemplateRepository;
	private final PlatformSecurityContext context;
	private final BillingMessageRepository messageDataRepository;
	
	
	@Autowired
	public EventActionMappingHelper(final BillingMessageTemplateRepository messageTemplateRepository,final PlatformSecurityContext context,
			final BillingMessageRepository billingMessageRepository){
		
		this.messageTemplateRepository=messageTemplateRepository;
		this.context=context;
		this.messageDataRepository=billingMessageRepository;
	}

	public void sendEmailAction(TicketMasterData data, TicketMaster ticketMaster, String resourceId, String ticketURL, String eventName, String mailId) {
		
		
		  List<BillingMessageTemplate> billingMessageTemplate = this.messageTemplateRepository.findByTemplateDescription("TICKET_TEMPLATE");
		  String value=ticketURL+""+resourceId;
		  BillingMessage billingMessage =null;
		  String removeUrl="<br/><b>URL : </b>"+"<a href="+value+">View Ticket</a>";
		  AppUser user=getUser();
		  	if(!user.getEmail().isEmpty()){
		  		mailId=user.getEmail();
		  	}else if(mailId.isEmpty()){
		  		throw new EmailNotFoundException(new Long(data.getUserId()));
		  	}
		  
		  	if(EventActionConstants.EVENT_CREATE_TICKET.equalsIgnoreCase(eventName)){
		  		billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"<br/>"
		  	             +ticketMaster.getDescription()+"\n"+removeUrl, "", mailId, mailId,"Ticket:"+resourceId, "N", billingMessageTemplate.get(0),'E',null);
				  
	           
		  	}else if(EventActionConstants.EVENT_EDIT_TICKET.equalsIgnoreCase(eventName)){
		  		  billingMessage = new BillingMessage("ADD COMMENT", data.getProblemDescription()+"<br/>"
		  				+ticketMaster.getDescription()+"<br/>"+"COMMENT: "+data.getLastComment()+"<br/>"+removeUrl, "", mailId,mailId,
		  				"Ticket:"+resourceId, "N", billingMessageTemplate.get(0),'E',null);
		  	
		  	}else if(EventActionConstants.EVENT_CLOSE_TICKET.equalsIgnoreCase(eventName)){
        	  	
    	  			 billingMessage = new BillingMessage("CLOSED TICKET", data.getProblemDescription()+"<br/>"
    	  			+ticketMaster.getDescription()+"<br/>"+"RESOLUTION: \t"+ticketMaster.getResolutionDescription()+"<br/>"+removeUrl, "", mailId,mailId,
					"Ticket:"+resourceId, "N", billingMessageTemplate.get(0),'E',null);
    	  }
		  	this.messageDataRepository.save(billingMessage);
		}
	
private AppUser getUser() {
		
	AppUser appUser=null;
		
	SecurityContext context = SecurityContextHolder.getContext();
	
	if(context.getAuthentication() != null){
	
		appUser=this.context.authenticatedUser();
		
	}
			
			return appUser;
	}

}

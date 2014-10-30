package org.mifosplatform.portfolio.association.service;

import java.util.Map;

import net.sf.json.JSONObject;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.mifosplatform.portfolio.association.domain.HardwareAssociation;
import org.mifosplatform.portfolio.association.exception.HardwareDetailsNotFoundException;
import org.mifosplatform.portfolio.order.data.CustomValidationData;
import org.mifosplatform.portfolio.order.domain.HardwareAssociationRepository;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.service.OrderDetailsReadPlatformServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class HardwareAssociationWriteplatformServiceImpl implements HardwareAssociationWriteplatformService
{

	private final PlatformSecurityContext context;
	private final OrderRepository orderRepository;
	private final HardwareAssociationRepository associationRepository;
	private final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices;
	private final HardwareAssociationCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	
    @Autowired
	public HardwareAssociationWriteplatformServiceImpl(final PlatformSecurityContext context,final HardwareAssociationCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final HardwareAssociationRepository associationRepository,final OrderRepository orderRepository,
			final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices ){
		
	    this.context=context;
		this.associationRepository=associationRepository;
		this.fromApiJsonDeserializer=fromApiJsonDeserializer;
		this.orderRepository=orderRepository;
		this.orderDetailsReadPlatformServices=orderDetailsReadPlatformServices;
	}
	
	@Override
	public void createNewHardwareAssociation(Long clientId, Long planId,String serialNo,Long orderId) 
	{
	        try{
	        	
	        //	this.context.authenticatedUser();
	        	HardwareAssociation hardwareAssociation=new HardwareAssociation(clientId,planId,serialNo,orderId);
	        	this.associationRepository.saveAndFlush(hardwareAssociation);
	        	
	        }catch(DataIntegrityViolationException exception){
	        	exception.printStackTrace();
	        }
		
	}

	@Override
	public CommandProcessingResult createAssociation(JsonCommand command) {
		try {
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			//Long planId = command.longValueOfParameterNamed("planId");
			Long orderId = command.longValueOfParameterNamed("orderId");
			String serialNo = command.stringValueOfParameterNamed("serialNo");
			Order order=this.orderRepository.findOne(orderId);
			String provisionNum = command.stringValueOfParameterNamed("provisionNum");
			HardwareAssociation hardwareAssociation = new HardwareAssociation(command.entityId(), order.getPlanId(), provisionNum, orderId);
			//Check for Custome_Validation
			CustomValidationData customValidationData   = this.orderDetailsReadPlatformServices.checkForCustomValidations(hardwareAssociation.getClientId(),"Pairing", command.json());
			if(customValidationData.getErrorCode() != 0 && customValidationData.getErrorMessage() != null){
				throw new ActivePlansFoundException(customValidationData.getErrorMessage()); 
			}
			
			this.associationRepository.saveAndFlush(hardwareAssociation);
			return new CommandProcessingResultBuilder().withEntityId(
					hardwareAssociation.getId()).withClientId(command.entityId()).build();
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
		
	}

	@Override
	public CommandProcessingResult updateAssociation(JsonCommand command) {
		
		// TODO Auto-generated method stub
		try {
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			HardwareAssociation hardwareAssociation = this.associationRepository.findOne(command.entityId());
			final Map<String, Object> changes = hardwareAssociation.updateAssociationDetails(command);
			if (!changes.isEmpty()) {
				this.associationRepository.save(hardwareAssociation);
			}
			return new CommandProcessingResult(hardwareAssociation.getId(),hardwareAssociation.getClientId());
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
	@Transactional
	@Override
	public CommandProcessingResult deAssociationHardware(Long associationId) {
		
		try {
			
//			AssociationData associationData=this.associationReadplatformService.retrieveSingleDetails(orderId);
			
		      HardwareAssociation association=this.associationRepository.findOne(associationId);
		      if(association == null){
					throw new HardwareDetailsNotFoundException(associationId);
				}
		      
		      JSONObject jsonObject=new JSONObject();
		      jsonObject.put("clientId", association.getClientId());
		      jsonObject.put("planId", association.getPlanId());
		      jsonObject.put("serialNo", association.getSerialNo());
		      jsonObject.put("orderId", association.getOrderId());
		      
		       
		        //Check for Custome_Validation
				CustomValidationData customValidationData   = this.orderDetailsReadPlatformServices.checkForCustomValidations(association.getClientId(),"UnPairing", jsonObject.toString());
				if(customValidationData.getErrorCode() != 0 && customValidationData.getErrorMessage() != null){
					throw new ActivePlansFoundException(customValidationData.getErrorMessage()); 
				}
    		   association.delete();
    		   this.associationRepository.save(association);
    		   return new CommandProcessingResult(association.getId(),association.getClientId());
    		   
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	
	}

}

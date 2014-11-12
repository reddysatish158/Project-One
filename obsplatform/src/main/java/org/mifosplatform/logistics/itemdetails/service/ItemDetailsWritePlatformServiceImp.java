package org.mifosplatform.logistics.itemdetails.service;

import java.util.List;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.domain.ItemMaster;
import org.mifosplatform.logistics.item.domain.ItemRepository;
import org.mifosplatform.logistics.itemdetails.data.AllocationHardwareData;
import org.mifosplatform.logistics.itemdetails.domain.InventoryGrn;
import org.mifosplatform.logistics.itemdetails.domain.InventoryGrnRepository;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetails;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetailsAllocation;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetailsAllocationRepository;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetailsRepository;
import org.mifosplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.mifosplatform.logistics.itemdetails.exception.OrderQuantityExceedsException;
import org.mifosplatform.logistics.itemdetails.serialization.InventoryItemAllocationCommandFromApiJsonDeserializer;
import org.mifosplatform.logistics.itemdetails.serialization.InventoryItemCommandFromApiJsonDeserializer;
import org.mifosplatform.logistics.mrn.domain.InventoryTransactionHistory;
import org.mifosplatform.logistics.mrn.domain.InventoryTransactionHistoryJpaRepository;
import org.mifosplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.mifosplatform.logistics.onetimesale.domain.OneTimeSale;
import org.mifosplatform.logistics.onetimesale.domain.OneTimeSaleRepository;
import org.mifosplatform.logistics.onetimesale.service.OneTimeSaleReadPlatformService;
import org.mifosplatform.portfolio.association.data.AssociationData;
import org.mifosplatform.portfolio.association.data.HardwareAssociationData;
import org.mifosplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.mifosplatform.portfolio.association.service.HardwareAssociationWriteplatformService;
import org.mifosplatform.portfolio.order.exceptions.NoGrnIdFoundException;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.mifosplatform.workflow.eventactionmapping.exception.EventActionMappingNotFoundException;
import org.mifosplatform.workflow.eventvalidation.service.EventValidationReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


@Service
public class ItemDetailsWritePlatformServiceImp implements ItemDetailsWritePlatformService{
	
	private final static Logger logger = (Logger) LoggerFactory.getLogger(ItemDetailsWritePlatformServiceImp.class);
	private final FromJsonHelper fromJsonHelper;
	private final PlatformSecurityContext context;
	private final ItemRepository itemRepository;
	private final OneTimeSaleRepository oneTimeSaleRepository;
	private final InventoryGrnRepository inventoryGrnRepository;
	private final ConfigurationRepository configurationRepository;
	private final OrderReadPlatformService orderReadPlatformService;
	private final ItemDetailsRepository inventoryItemDetailsRepository;
	private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	private final HardwareAssociationReadplatformService associationReadplatformService;
	private final ItemDetailsReadPlatformService inventoryItemDetailsReadPlatformService;
	private final EventValidationReadPlatformService eventValidationReadPlatformService;
	private final HardwareAssociationWriteplatformService associationWriteplatformService;
	private final ProvisioningWritePlatformService provisioningWritePlatformService;
	private final ItemDetailsAllocationRepository inventoryItemDetailsAllocationRepository; 
	private final InventoryTransactionHistoryJpaRepository inventoryTransactionHistoryJpaRepository;
	private final InventoryItemCommandFromApiJsonDeserializer inventoryItemCommandFromApiJsonDeserializer;
	private final InventoryItemAllocationCommandFromApiJsonDeserializer inventoryItemAllocationCommandFromApiJsonDeserializer;
	
	
	@Autowired
	public ItemDetailsWritePlatformServiceImp(final ItemDetailsReadPlatformService inventoryItemDetailsReadPlatformService, 
			final PlatformSecurityContext context, final InventoryGrnRepository inventoryitemRopository,
			final InventoryItemCommandFromApiJsonDeserializer inventoryItemCommandFromApiJsonDeserializer,
			final InventoryItemAllocationCommandFromApiJsonDeserializer inventoryItemAllocationCommandFromApiJsonDeserializer, 
			final ItemDetailsAllocationRepository inventoryItemDetailsAllocationRepository,final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService, 
			final OneTimeSaleRepository oneTimeSaleRepository,final ItemDetailsRepository inventoryItemDetailsRepository,final FromJsonHelper fromJsonHelper, 
			final InventoryTransactionHistoryJpaRepository inventoryTransactionHistoryJpaRepository,final ConfigurationRepository  configurationRepository,
			final HardwareAssociationReadplatformService associationReadplatformService,final HardwareAssociationWriteplatformService associationWriteplatformService,
			final ItemRepository itemRepository,final OrderReadPlatformService orderReadPlatformService,
			final ProvisioningWritePlatformService provisioningWritePlatformService,final EventValidationReadPlatformService eventValidationReadPlatformService) 
	{
		this.inventoryItemDetailsReadPlatformService = inventoryItemDetailsReadPlatformService;
		this.context=context;
		this.inventoryItemDetailsRepository=inventoryItemDetailsRepository;
		this.inventoryGrnRepository=inventoryitemRopository;
		this.inventoryItemCommandFromApiJsonDeserializer = inventoryItemCommandFromApiJsonDeserializer;
		this.inventoryItemAllocationCommandFromApiJsonDeserializer = inventoryItemAllocationCommandFromApiJsonDeserializer;
		this.inventoryItemDetailsAllocationRepository = inventoryItemDetailsAllocationRepository;
		this.oneTimeSaleReadPlatformService=oneTimeSaleReadPlatformService;
		this.oneTimeSaleRepository = oneTimeSaleRepository;
		this.fromJsonHelper=fromJsonHelper;
		this.inventoryTransactionHistoryJpaRepository = inventoryTransactionHistoryJpaRepository;
		this.configurationRepository=configurationRepository;
		this.associationReadplatformService=associationReadplatformService;
		this.associationWriteplatformService=associationWriteplatformService;
		this.itemRepository=itemRepository;
		this.orderReadPlatformService=orderReadPlatformService;
		this.provisioningWritePlatformService=provisioningWritePlatformService;
		this.eventValidationReadPlatformService=eventValidationReadPlatformService;
		
	}
	

	
	
	

	@Override
	public CommandProcessingResult addItem(final JsonCommand command,Long flag) {

		try{
			context.authenticatedUser();
			ItemDetails inventoryItemDetails=null;			
			inventoryItemCommandFromApiJsonDeserializer.validateForCreate(command);
			inventoryItemDetails = ItemDetails.fromJson(command,fromJsonHelper);
			InventoryGrn inventoryGrn = inventoryGrnRepository.findOne(inventoryItemDetails.getGrnId());

			if(inventoryGrn != null){
				inventoryItemDetails.setOfficeId(inventoryGrn.getOfficeId());
				inventoryItemDetails.setLocationId(inventoryGrn.getOfficeId());

				if(inventoryGrn.getReceivedQuantity() < inventoryGrn.getOrderdQuantity()){
					inventoryGrn.setReceivedQuantity(inventoryGrn.getReceivedQuantity()+1);
					this.inventoryGrnRepository.save(inventoryGrn);
				
				}else{
					throw new OrderQuantityExceedsException(inventoryGrn.getOrderdQuantity());
				}
			
			}else{
				throw new NoGrnIdFoundException(inventoryItemDetails.getGrnId());
			}
			
			this.inventoryItemDetailsRepository.save(inventoryItemDetails);
			
			//InventoryTransactionHistory transactionHistory = InventoryTransactionHistory.logTransaction(new LocalDate().toDate(), inventoryItemDetails.getId(),"Item Detail",inventoryItemDetails.getSerialNumber(), inventoryItemDetails.getItemMasterId(), inventoryItemDetails.getGrnId(), inventoryGrn.getOfficeId());
			//InventoryTransactionHistory transactionHistory = InventoryTransactionHistory.logTransaction(new LocalDate().toDate(),inventoryItemDetails.getId(),"Item Detail",inventoryItemDetails.getSerialNumber(),inventoryGrn.getOfficeId(),inventoryItemDetails.getClientId(),inventoryItemDetails.getItemMasterId());
			//inventoryTransactionHistoryJpaRepository.save(transactionHistory);
			/*++processRecords;
             processStatus="Processed";*/
			return new CommandProcessingResultBuilder().withEntityId(inventoryItemDetails.getId()).build();
			
		} catch (DataIntegrityViolationException dve){
			
			handleDataIntegrityIssues(command,dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	
		private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
	         Throwable realCause = dve.getMostSpecificCause();
	        if (realCause.getMessage().contains("serial_no_constraint")){
	        	final String serialNumber=command.stringValueOfParameterNamed("serialNumber");
	        	throw new PlatformDataIntegrityException("validation.error.msg.inventory.item.duplicate.serialNumber", "validation.error.msg.inventory.item.duplicate.serialNumber", "validation.error.msg.inventory.item.duplicate.serialNumber",serialNumber);
	        	
	        }

	        logger.error(dve.getMessage(), dve);   	
	}
		@Transactional
		@Override
		public CommandProcessingResult updateItem(Long id,JsonCommand command)
		{
	        try{
	        	  
	        	this.context.authenticatedUser();
	        	this.inventoryItemCommandFromApiJsonDeserializer.validateForUpdate(command.json());
	        	ItemDetails inventoryItemDetails=ItemretrieveById(id);
	        	final String oldHardware =inventoryItemDetails.getProvisioningSerialNumber();
	        	final String oldSerilaNumber =inventoryItemDetails.getSerialNumber();
	        	final Map<String, Object> changes = inventoryItemDetails.update(command);  
	        	
	        	if(!changes.isEmpty()){
	            this.inventoryItemDetailsRepository.saveAndFlush(inventoryItemDetails);
	        	}
	        	
	        	if(!oldHardware.equalsIgnoreCase(inventoryItemDetails.getProvisioningSerialNumber())&&inventoryItemDetails.getClientId()!=null){
	          	  
	        		this.provisioningWritePlatformService.updateHardwareDetails(inventoryItemDetails.getClientId(),inventoryItemDetails.getSerialNumber(),oldSerilaNumber,
	        				inventoryItemDetails .getProvisioningSerialNumber(),oldHardware);
	        	}
	         return new CommandProcessingResultBuilder().withEntityId(inventoryItemDetails.getId()).build();
	        	
	        }
	        catch(DataIntegrityViolationException dve){
	        	
	        	 if(dve.getCause()instanceof ConstraintViolationException){
	        		 handleDataIntegrityIssues(command, dve);
	        	 }
	        	 return CommandProcessingResult.empty(); 
	        }
	        
	}
		private ItemDetails ItemretrieveById(Long id) {
            
			ItemDetails itemId=this.inventoryItemDetailsRepository.findOne(id);
	              if (itemId== null) { throw new EventActionMappingNotFoundException(id.toString()); }
		          return itemId;	
		}

		@Transactional
		@Override
		public CommandProcessingResult allocateHardware(JsonCommand command) {

			try{
				
				this.context.authenticatedUser();
				 Long clientId=null;
				 Long entityId=null;
				inventoryItemAllocationCommandFromApiJsonDeserializer.validateForCreate(command.json());
				final JsonElement element = fromJsonHelper.parse(command.json());
				JsonArray allocationData = fromJsonHelper.extractJsonArrayNamed("serialNumber", element);
				//int i=1;
					for(JsonElement j:allocationData){
			        	
						ItemDetailsAllocation inventoryItemDetailsAllocation = ItemDetailsAllocation.fromJson(j,fromJsonHelper);
						AllocationHardwareData allocationHardwareData = inventoryItemDetailsReadPlatformService.retriveInventoryItemDetail(inventoryItemDetailsAllocation.getSerialNumber());
			        	checkHardwareCondition(allocationHardwareData);
			        	ItemDetails inventoryItemDetails = inventoryItemDetailsRepository.findOne(allocationHardwareData.getItemDetailsId());
						inventoryItemDetails.setItemMasterId(inventoryItemDetailsAllocation.getItemMasterId());
						inventoryItemDetails.setClientId(inventoryItemDetailsAllocation.getClientId());
						inventoryItemDetails.setStatus("In Use");
						
						this.inventoryItemDetailsRepository.saveAndFlush(inventoryItemDetails);
						this.inventoryItemDetailsAllocationRepository.saveAndFlush(inventoryItemDetailsAllocation);
						OneTimeSale oneTimeSale = this.oneTimeSaleRepository.findOne(inventoryItemDetailsAllocation.getOrderId());
						oneTimeSale.setHardwareAllocated("ALLOCATED");
						this.oneTimeSaleRepository.saveAndFlush(oneTimeSale);
						clientId=oneTimeSale.getClientId();
						entityId=oneTimeSale.getId();

						InventoryTransactionHistory transactionHistory = InventoryTransactionHistory.logTransaction(new LocalDate().toDate(), 
								oneTimeSale.getId(),"Allocation",inventoryItemDetailsAllocation.getSerialNumber(), inventoryItemDetailsAllocation.getItemMasterId(),
								inventoryItemDetails.getOfficeId(),inventoryItemDetailsAllocation.getClientId());
						
						this.inventoryTransactionHistoryJpaRepository.save(transactionHistory);
						inventoryItemDetailsAllocation.getId();
					//	i++;
						
						  //For Plan And HardWare Association
						Configuration configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_IMPLICIT_ASSOCIATION);
						
						if(configurationProperty.isEnabled()){
							configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_DEVICE_AGREMENT_TYPE);
							
							if(configurationProperty.getValue().equalsIgnoreCase(ConfigurationConstants.CONFIR_PROPERTY_SALE)){
								ItemMaster itemMaster=this.itemRepository.findOne(inventoryItemDetails.getItemMasterId());
								List<HardwareAssociationData> allocationDetailsDatas=this.associationReadplatformService.retrieveClientAllocatedPlan(oneTimeSale.getClientId(),itemMaster.getItemCode());						    		   
								if(!allocationDetailsDatas.isEmpty()){
									
									this.associationWriteplatformService.createNewHardwareAssociation(oneTimeSale.getClientId(),
											allocationDetailsDatas.get(0).getPlanId(),inventoryItemDetails.getSerialNumber(),
											allocationDetailsDatas.get(0).getorderId());
						    		   }	
									}	
						    	}	
					}
					return new CommandProcessingResult(entityId,clientId);
			
			}catch(DataIntegrityViolationException dve){
				handleDataIntegrityIssues(command, dve); 
					return new CommandProcessingResult(Long.valueOf(-1));
			}
			
		}
		
		
		private void checkHardwareCondition(AllocationHardwareData allocationHardwareData) {
			
			if(allocationHardwareData == null){
				throw new PlatformDataIntegrityException("invalid.serial.no", "invalid.serial.no","serialNumber");
			}
			
			if(!allocationHardwareData.getQuality().equalsIgnoreCase("Good") || !allocationHardwareData.getQuality().equalsIgnoreCase("Good")){
				throw new PlatformDataIntegrityException("product.not.in.good.condition", "product.not.in.good.condition","product.not.in.good.condition");
    		}
										
			if(allocationHardwareData.getClientId()!=null && allocationHardwareData.getClientId()!=0){
				
				if(allocationHardwareData.getClientId()>0){
					throw new PlatformDataIntegrityException("SerialNumber "+allocationHardwareData.getSerialNumber()+" already allocated.", 
							                "SerialNumber "+allocationHardwareData.getSerialNumber()+ "already allocated.","serialNumber"+allocationHardwareData.getSerialNumber());	
				}}
			}

		@Override
		public ItemDetailsAllocation deAllocateHardware(String serialNo,Long clientId) {
				try{
					AllocationDetailsData allocationDetailsData=this.oneTimeSaleReadPlatformService.retrieveAllocationDetailsBySerialNo(serialNo);
					ItemDetailsAllocation inventoryItemDetailsAllocation=null;
					
						if(allocationDetailsData!=null){
							inventoryItemDetailsAllocation =this.inventoryItemDetailsAllocationRepository.findOne(allocationDetailsData.getId());
							inventoryItemDetailsAllocation.deAllocate();
							this.inventoryItemDetailsAllocationRepository.save(inventoryItemDetailsAllocation);
							ItemDetails inventoryItemDetails=this.inventoryItemDetailsRepository.findOne(allocationDetailsData.getItemDetailId());
							inventoryItemDetails.delete();
							this.inventoryItemDetailsRepository.save(inventoryItemDetails);
					     
							InventoryTransactionHistory transactionHistory = InventoryTransactionHistory.logTransaction(new LocalDate().toDate(), 
					  			inventoryItemDetailsAllocation.getOrderId(),"De Allocation",inventoryItemDetailsAllocation.getSerialNumber(), inventoryItemDetailsAllocation.getItemMasterId(),
								inventoryItemDetailsAllocation.getClientId(),inventoryItemDetails.getOfficeId());
							inventoryTransactionHistoryJpaRepository.save(transactionHistory);
					   
						}
						
				   return inventoryItemDetailsAllocation;
			
				}catch(DataIntegrityViolationException exception){
					handleDataIntegrityIssues(null, exception);
					return null;
				}
		}

		@Override
		public CommandProcessingResult deAllocateHardware(JsonCommand command) {

           try{
        	   
        	  final  String serialNo=command.stringValueOfParameterNamed("serialNo");
        	  final Long clientId=command.longValueOfParameterNamed("clientId");
        	   
		        //Check for Custome_Validation
				this.eventValidationReadPlatformService.checkForCustomValidations(clientId,"UnPairing", command.json());
        	   final Long activeorders=this.orderReadPlatformService.retrieveClientActiveOrderDetails(clientId,serialNo);
        	   	if(activeorders!= 0){
        	   		throw new ActivePlansFoundException();
        	   	}
        	   
        	   	List<AssociationData> associationDatas=this.associationReadplatformService.retrieveClientAssociationDetails(clientId);
        	   	for(AssociationData associationData:associationDatas ){
        	   		this.associationWriteplatformService.deAssociationHardware(associationData.getId());
        	   	}
        	   
        	   ItemDetailsAllocation inventoryItemDetailsAllocation=this.deAllocateHardware(serialNo, clientId);
        	   OneTimeSale oneTimeSale=this.oneTimeSaleRepository.findOne(inventoryItemDetailsAllocation.getOrderId());
        	   oneTimeSale.setStatus();
        	   this.oneTimeSaleRepository.save(oneTimeSale);
/*        		transactionHistoryWritePlatformService.saveTransactionHistory(clientId, "Device Return", new Date(),"Serial Number :"
	    				+inventoryItemDetailsAllocation.getSerialNumber(),"Item Code:"+itemCode,"Order Id: "+inventoryItemDetailsAllocation.getOrderId());
*/        	   
        	   return new CommandProcessingResult(command.entityId(),clientId);
           }catch(DataIntegrityViolationException exception){
        	   
        	   return new CommandProcessingResult(Long.valueOf(-1));
           }
		}
		
		@Transactional
		@Override
		public CommandProcessingResult deleteItem(Long id,JsonCommand command)
		{
	        try{
	        	this.context.authenticatedUser();
	        	ItemDetails inventoryItemDetails=ItemretrieveById(id);
	        	InventoryGrn grn=this.inventoryGrnRepository.findOne(inventoryItemDetails.getGrnId());
	        	inventoryItemDetails.itemDelete();
	        	this.inventoryItemDetailsRepository.saveAndFlush(inventoryItemDetails);
	        	Long ReceivedItems=grn.getReceivedQuantity()-new Long(1);
	        	grn.setReceivedQuantity(ReceivedItems);
	        	this.inventoryGrnRepository.save(grn);
	        	return new CommandProcessingResult(id);
	        	
	        }catch(DataIntegrityViolationException dve){
	        	handleDataIntegrityIssues(command, dve);
	        	return new CommandProcessingResult(Long.valueOf(-1));
	        }	
    }
}



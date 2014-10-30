package org.mifosplatform.billing.planprice.service;

import java.util.List;
import java.util.Map;

import org.mifosplatform.billing.planprice.domain.Price;
import org.mifosplatform.billing.planprice.domain.PriceRepository;
import org.mifosplatform.billing.planprice.exceptions.ChargeCOdeExists;
import org.mifosplatform.billing.planprice.exceptions.PriceNotFoundException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.plan.data.ServiceData;
import org.mifosplatform.portfolio.service.serialization.PriceCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.service.service.ServiceMasterWritePlatformServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 *
 */
@Service
public class PriceWritePlatformServiceImpl implements PriceWritePlatformService {

	 private final static Logger LOGGER = LoggerFactory.getLogger(ServiceMasterWritePlatformServiceImpl.class);
	 private final PlatformSecurityContext context;
	 private final PriceReadPlatformService priceReadPlatformService;
	 private final PriceCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	 private final PriceRepository priceRepository;
	 
	@Autowired
	 public PriceWritePlatformServiceImpl(final PlatformSecurityContext context,final PriceReadPlatformService priceReadPlatformService,
			 final PriceCommandFromApiJsonDeserializer fromApiJsonDeserializer,final PriceRepository priceRepository)
		{
			this.context=context;
			this.priceReadPlatformService=priceReadPlatformService;
			this.fromApiJsonDeserializer=fromApiJsonDeserializer;
			this.priceRepository=priceRepository;
		}
	
	@Override
	public CommandProcessingResult createPricing(final Long planId,JsonCommand command) {
		
		try{
		context.authenticatedUser();
		this.fromApiJsonDeserializer.validateForCreate(command.json());
		List<ServiceData> serviceData = this.priceReadPlatformService.retrieveServiceCodeDetails(planId);
		
		final Price price =Price.fromJson(command,serviceData,planId);
			
			for (ServiceData data : serviceData) {
				
					if (data.getChargeCode() != null && data.getPlanId() == planId && data.getServiceCode().equalsIgnoreCase(price.getServiceCode())
							&& data.getPriceregion().equalsIgnoreCase(price.getPriceRegion().toString()) && data.getChargeCode().equalsIgnoreCase(price.getChargeCode())){
						
						throw new ChargeCOdeExists(data.getChargeDescription());
					}
			}
		this.priceRepository.save(price);
		return new CommandProcessingResult(price.getId());

	} catch (DataIntegrityViolationException dve) {
		LOGGER.error(dve.getMessage());
		 handleCodeDataIntegrityIssues(command, dve);
		return  CommandProcessingResult.empty();
	}
}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,DataIntegrityViolationException dve) {
		LOGGER.error(dve.getMessage(),dve);
		 throw new PlatformDataIntegrityException("error.msg.planprice.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource.");
		
	}
	@Override
	public CommandProcessingResult updatePrice(final Long priceId, JsonCommand command) {
		
		try{
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final Price price = retrievePriceBy(priceId);
			final  Map<String, Object> changes = price.update(command);
			if (!changes.isEmpty()) {
				this.priceRepository.save(price);
			}
  
			return new CommandProcessingResultBuilder() //
			.withCommandId(command.commandId()) //
			.withEntityId(priceId) //
			.with(changes) //
			.build();

         } catch (DataIntegrityViolationException dve) {
    		 handleCodeDataIntegrityIssues(command, dve);
    		return  CommandProcessingResult.empty();
    	}
	
	
}
	private Price retrievePriceBy(final Long priceId) {
		
		final Price price=this.priceRepository.findOne(priceId);
		if(price==null){
			{ throw new PriceNotFoundException(priceId.toString()); }
		}
		return price;
	}
	@Override
	public CommandProcessingResult deletePrice(final Long priceId) {
		  try {
				 Price price=this.priceRepository.findOne(priceId);
				 	if(price!= null){
				 		price.delete();	
				 	}
			     this.priceRepository.save(price);
			     return new CommandProcessingResultBuilder().withEntityId(priceId).build();
		  	
		  } catch (DataIntegrityViolationException dve) {
			  handleCodeDataIntegrityIssues(null, dve);
		  		return new CommandProcessingResultBuilder().withEntityId(Long.valueOf(-1)).build();
			}
		  
		  }
}

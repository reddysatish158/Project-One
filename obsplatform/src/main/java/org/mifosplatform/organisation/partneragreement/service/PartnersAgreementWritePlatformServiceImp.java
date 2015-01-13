package org.mifosplatform.organisation.partneragreement.service;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.partner.service.PartnersWritePlatformServiceImp;
import org.mifosplatform.organisation.partneragreement.domain.Agreement;
import org.mifosplatform.organisation.partneragreement.domain.AgreementDetails;
import org.mifosplatform.organisation.partneragreement.domain.AgreementRepository;
import org.mifosplatform.organisation.partneragreement.serialization.PartnersAgreementCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class PartnersAgreementWritePlatformServiceImp implements PartnersAgreementWritePlatformService {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(PartnersWritePlatformServiceImp.class);
	private final PlatformSecurityContext context;
	private final FromJsonHelper fromApiJsonHelper;
	private final PartnersAgreementCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final AgreementRepository agreementRepository;
	private final PartnersAgreementReadPlatformService agreementReadPlatformService;
	
	
	@Autowired
	public PartnersAgreementWritePlatformServiceImp(final PlatformSecurityContext context,final FromJsonHelper fromApiJsonHelper,
			final PartnersAgreementCommandFromApiJsonDeserializer apiJsonDeserializer,final AgreementRepository agreementRepository,
			final PartnersAgreementReadPlatformService agreementReadPlatformService) {
		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.agreementRepository = agreementRepository;
		this.agreementReadPlatformService = agreementReadPlatformService;

	}

	@Override
	public CommandProcessingResult createNewPartnerAgreement(final JsonCommand command) {

		
		try{
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			
			Agreement agreement=Agreement.fromJosn(command);
			
			final Long agreementId=this.agreementReadPlatformService.checkPartnerAgreementId(agreement.getPartnerId());
			final JsonArray partnerAgreementArray = command.arrayOfParameterNamed("sourceData").getAsJsonArray();
				
			if(agreementId !=null){
				
				Agreement existsAgreement=this.agreementRepository.findOne(agreementId);
				
				//agreement status
				if(existsAgreement.getAgreementStatus() == null && agreement.getAgreementStatus() == null){
	      			 
	        	}else if(existsAgreement.getAgreementStatus() == null && agreement.getAgreementStatus() != null){
	        		existsAgreement.setAgreementStatus(agreement.getAgreementStatus());
	   		 	}else if(!existsAgreement.getAgreementStatus().equals(agreement.getAgreementStatus()) ){
	   		 	     existsAgreement.setAgreementStatus(agreement.getAgreementStatus());		    		
	   		 	}
				
				//startDate
				if(existsAgreement.getStartDate() == null && existsAgreement.getStartDate() == null){
	    			 
	        	}else if(existsAgreement.getStartDate() == null && existsAgreement.getStartDate() != null){
	        		  existsAgreement.setStartDate(agreement.getStartDate());
	   		 	}else if(!existsAgreement.getStartDate().equals(agreement.getStartDate()) ){
	   		 	      existsAgreement.setStartDate(agreement.getStartDate());		    		
	   		 	}
	        	//endDate
	        	if(existsAgreement.getEndDate() == null && agreement.getEndDate() == null){
	   			 
	        	}else if(existsAgreement.getEndDate() == null && agreement.getEndDate() != null){
	        		  existsAgreement.setEndDate(agreement.getEndDate());
	   		 	}else if(!existsAgreement.getEndDate().equals(agreement.getEndDate()) ){
	   		 	     existsAgreement.setEndDate(agreement.getEndDate());		    		
	   		 	}
				
				if(existsAgreement.getIsDeleted()=='N'&& agreement.getIsDeleted()=='N'){
	        		
	        	}else if((existsAgreement.getIsDeleted()=='Y')&&(agreement.getIsDeleted()=='N')){
	        		
	        		existsAgreement.setIsDeleted(agreement.getIsDeleted());
	        	}
				
				for(int i=0; i<partnerAgreementArray.size();i++){ 
					
					final JsonElement element = fromApiJsonHelper.parse(partnerAgreementArray.get(i).toString());
					final Long source = fromApiJsonHelper.extractLongNamed("source", element);
					final String shareType = fromApiJsonHelper.extractStringNamed("shareType", element);
					final BigDecimal shareAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("shareAmount",element);
					final Long status = fromApiJsonHelper.extractLongNamed("status", element);
					AgreementDetails details=new AgreementDetails(source,shareType,shareAmount,status);
					existsAgreement.addAgreementDetails(details);
				}
				this.agreementRepository.save(existsAgreement);
				
				return new CommandProcessingResultBuilder().withCommandId(command.commandId())
                        .withEntityId(existsAgreement.getId()).build();
				
			}else{
			
			for(int i=0; i<partnerAgreementArray.size();i++){ 
				
				final JsonElement element = fromApiJsonHelper.parse(partnerAgreementArray.get(i).toString());
				final Long source = fromApiJsonHelper.extractLongNamed("source", element);
				final String shareType = fromApiJsonHelper.extractStringNamed("shareType", element);
				final BigDecimal shareAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("shareAmount",element);
				final Long status = fromApiJsonHelper.extractLongNamed("status", element);
				AgreementDetails details=new AgreementDetails(source,shareType,shareAmount,status);
				agreement.addAgreementDetails(details);
			}
			this.agreementRepository.save(agreement);
		
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
			                          .withEntityId(agreement.getId()).build();
		  }
		}catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause();
		LOGGER.error(dve.getMessage(), dve);
		
		if(dve.getMostSpecificCause().getMessage().contains("b_agreement_dtl_ai_ps_mc_uniquekey")){
			 throw new PlatformDataIntegrityException("error.msg.agreement.duplicate.source.data.entry.issue","A Agreement with sourceCategory " +
			 		"already exists","sourceType");
		}
		throw new PlatformDataIntegrityException("error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "+ realCause.getMessage());

	}
}

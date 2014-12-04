package org.mifosplatform.portfolio.addons.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.addons.domain.Addons;
import org.mifosplatform.portfolio.addons.domain.AddonsRepository;
import org.mifosplatform.portfolio.addons.serialization.AddOnsCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


@Service
public class AddonsWritePlatformServiceImpl implements AddOnsWritePlatformService{
	
	private final PlatformSecurityContext context;
	private final FromJsonHelper fromJsonHelper;
	private final AddOnsCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final AddonsRepository addonsRepository;
	
 @Autowired
 public AddonsWritePlatformServiceImpl(final PlatformSecurityContext context,
		 final AddOnsCommandFromApiJsonDeserializer fromApiJsonDeserializer,final FromJsonHelper fromJsonHelper,
		 final AddonsRepository addonsRepository){
		
	this.context=context;
	this.fromJsonHelper=fromJsonHelper;
	this.fromApiJsonDeserializer=fromApiJsonDeserializer;
	this.addonsRepository=addonsRepository;
}


@Transactional
@Override
public CommandProcessingResult createAddons(JsonCommand command) {
	
	try{
		
		this.context.authenticatedUser();
		this.fromApiJsonDeserializer.validateForCreate(command.json());
		final Long planId=command.longValueOfParameterNamed("planId");
		final JsonElement element = fromJsonHelper.parse(command.json());
		final JsonArray addonServices = fromJsonHelper.extractJsonArrayNamed("addonServices", element);
		
		for (JsonElement jsonElement : addonServices) {
			Addons addons=Addons.fromJson(jsonElement, fromJsonHelper, planId);
			this.addonsRepository.saveAndFlush(addons);
			
		}
		return new CommandProcessingResult(planId);
	
	}catch(DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(command, dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	}
	
}



private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
	// TODO Auto-generated method stub
	
}

}

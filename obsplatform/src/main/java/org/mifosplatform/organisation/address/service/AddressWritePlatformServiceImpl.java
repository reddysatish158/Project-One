package org.mifosplatform.organisation.address.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.mifosplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.address.data.AddressData;
import org.mifosplatform.organisation.address.domain.Address;
import org.mifosplatform.organisation.address.domain.AddressRepository;
import org.mifosplatform.organisation.address.domain.City;
import org.mifosplatform.organisation.address.domain.CityRepository;
import org.mifosplatform.organisation.address.domain.Country;
import org.mifosplatform.organisation.address.domain.CountryRepository;
import org.mifosplatform.organisation.address.domain.State;
import org.mifosplatform.organisation.address.domain.StateRepository;
import org.mifosplatform.organisation.address.exception.CityNotFoundException;
import org.mifosplatform.organisation.address.exception.CountryNotFoundException;
import org.mifosplatform.organisation.address.exception.StateNotFoundException;
import org.mifosplatform.organisation.address.serialization.LocationValidatorCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


@Service
public class AddressWritePlatformServiceImpl implements AddressWritePlatformService {
	  private final static Logger logger = LoggerFactory.getLogger(AddressWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final AddressRepository addressRepository;
	private final CityRepository cityRepository;
	private final StateRepository stateRepository;
	private final CountryRepository countryRepository;
	private final AddressReadPlatformService addressReadPlatformService;
	private final LocationValidatorCommandFromApiJsonDeserializer locationValidatorCommandFromApiJsonDeserializer;
	public static final String ADDRESSTYPE="addressType";
	
	
	


	@Autowired
	public AddressWritePlatformServiceImpl(final PlatformSecurityContext context,final CityRepository cityRepository,
			final AddressReadPlatformService addressReadPlatformService,final StateRepository stateRepository,
			final CountryRepository countryRepository,final AddressRepository addressRepository,
			final LocationValidatorCommandFromApiJsonDeserializer locationValidatorCommandFromApiJsonDeserializer) {
		this.context = context;
		this.addressRepository = addressRepository;
		this.cityRepository=cityRepository;
		this.stateRepository=stateRepository;
		this.countryRepository=countryRepository;
		this.addressReadPlatformService=addressReadPlatformService;
		this.locationValidatorCommandFromApiJsonDeserializer = locationValidatorCommandFromApiJsonDeserializer;
		
		

	}

	@Override
	public CommandProcessingResult createAddress(final Long clientId,final JsonCommand command) {
		try {
			context.authenticatedUser();
			final Address address = Address.fromJson(clientId,command);
			this.addressRepository.save(address);
			return new CommandProcessingResult(address.getId(),clientId);
		} catch (DataIntegrityViolationException dve) {
			 handleCodeDataIntegrityIssues(command, dve);
			return  CommandProcessingResult.empty();
		}
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause(); 
		String entityCode = command.stringValueOfParameterNamed("entityCode");
		if(realCause.getMessage().contains("country_code")){
			 throw new PlatformDataIntegrityException("Country Code with this '"+entityCode+ "' already exists",
					 "error.msg.addressmaster.country.duplicate.countrycode","countryCode",entityCode);
		}else if(realCause.getMessage().contains("state_code")){
			 throw new PlatformDataIntegrityException("State Code with this '"+entityCode+ "' already exists",
					 "error.msg.addressmaster.state.duplicate.statecode", "stateCode",entityCode);
		}else if(realCause.getMessage().contains("city_code")){
			 throw new PlatformDataIntegrityException("City Code with this '"+entityCode+ "' already exists", 
					 "error.msg.addressmaster.city.duplicate.citycode","cityCode",entityCode);
		}
		  logger.error(dve.getMessage(), dve);
		
	}

	@Override
	public CommandProcessingResult updateAddress(final Long clientId,final JsonCommand command) {
		try
		{
			  context.authenticatedUser();
	            
	             Map<String, Object> changes =new HashMap<String, Object>();
	             final List<AddressData> addressDatas =this.addressReadPlatformService.retrieveClientAddressDetails(clientId);
	                
	             final String addressType=command.stringValueOfParameterNamed(ADDRESSTYPE);
	             
	             
	             if(addressDatas.size()==1 && addressType.equalsIgnoreCase("BILLING")){
	            	 
	            	 final Address  newAddress=Address.fromJson(clientId, command);
               	  this.addressRepository.save(newAddress);
	            	 
	             }
	                     for(AddressData addressData:addressDatas){
	                    	 
	                    	  if(addressData.getAddressType().equalsIgnoreCase(addressType))
	                    	  {
	                    		  final Address address = retrieveAddressBy(addressData.getAddressId());  
	                    		  changes = address.update(command);
	                              
	                                  this.addressRepository.save(address);
	                    	  }
         }
         return new CommandProcessingResultBuilder() 
         .withCommandId(command.commandId()) 
         .withEntityId(clientId) 
         .withClientId(clientId)
         .with(changes) 
         .build();
	} catch (DataIntegrityViolationException dve) {
		 handleCodeDataIntegrityIssues(command,dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	}
}

	private Address retrieveAddressBy(final Long addrId) {
		final Address address=this.addressRepository.findOne(addrId);
	    if(address== null){
		throw new CodeNotFoundException(addrId);
	    }
	return address;
	}

	@Override
	public CommandProcessingResult createLocation(final JsonCommand command,final String entityType) {
  try{
	  
	  this.context.authenticatedUser();
	 
	  this.locationValidatorCommandFromApiJsonDeserializer.validateForCreate(command.json(),entityType);
	  if(entityType.equalsIgnoreCase("city")){
			final City city = City.fromJson(command);
		  this.cityRepository.save(city);
		  return new CommandProcessingResult(Long.valueOf(city.getId()));
	  }else if(entityType.equalsIgnoreCase("state")){
		  
		  final State state=State.fromJson(command);
		  this.stateRepository.save(state);
		  
		  return new CommandProcessingResult(Long.valueOf(state.getId()));
	  }else{
		  
		  final Country country=Country.fromJson(command);
		  this.countryRepository.save(country);
		  return new CommandProcessingResult(Long.valueOf(country.getId()));
	  }
	  
		  
	  
  } catch (DataIntegrityViolationException dve) {
	  handleCodeDataIntegrityIssues(command,dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	}

	}
	@Override
	public CommandProcessingResult updateLocation(final JsonCommand command,final String entityType, final Long id) {
	  try{
		this.context.authenticatedUser();
		this.locationValidatorCommandFromApiJsonDeserializer.validateForCreate(command.json(),entityType);
		if(entityType.equalsIgnoreCase("city")){
			final City city=cityObjectRetrieveById(id);
		   final Map<String, Object> changes = city.update(command);
		   	if(!changes.isEmpty()){
		   		this.cityRepository.saveAndFlush(city);
		   	}
		   
      	}else if(entityType.equalsIgnoreCase("state")){
			  
      		final State state=stateObjectRetrieveById(id);
  			final Map<String, Object> changes = state.update(command);
	  
  			if(!changes.isEmpty()){
  				this.stateRepository.saveAndFlush(state);
  			}
  	 	}else {
			  
  	 		final Country country=countryObjectRetrieveById(id);
  			final Map<String, Object> changes = country.update(command);
	  
  				if(!changes.isEmpty()){
  					this.countryRepository.saveAndFlush(country);
  				}
  	 		}
		return new CommandProcessingResult(id);
		  
	  	}catch (DataIntegrityViolationException dve) {
	  		if(dve.getCause() instanceof ConstraintViolationException){
	  			handleCodeDataIntegrityIssues(command,dve);
	  		}
	  		return new CommandProcessingResult(Long.valueOf(-1));
	  	}
	}

	private City cityObjectRetrieveById(final Long id){
		final City city=this.cityRepository.findOne(id);
		if (city== null) { throw new CityNotFoundException(id.toString()); }
		return city;
	}
	private State stateObjectRetrieveById(final Long id){
		final State state=this.stateRepository.findOne(id);
		if (state== null) { throw new StateNotFoundException(id.toString()); }
		return state;
	}
	private Country countryObjectRetrieveById(final Long id){
		final Country country=this.countryRepository.findOne(id);
		if (country== null) { throw new CountryNotFoundException(id.toString()); }
		return country;
	}

	@Override
	public CommandProcessingResult deleteLocation(final JsonCommand command,final String entityType, final Long id) {
		
		try{
	    	 this.context.authenticatedUser();
	    	 if(entityType.equalsIgnoreCase("city")){
	    		 final City city = this.cityRepository.findOne(id);
	    		 if(city==null){
	        		 throw new CityNotFoundException(id.toString());
	        	 }
	    		 city.delete();
	    		 this.cityRepository.save(city);
	    		 return new CommandProcessingResult(id);
	        	 
	    	 }else if(entityType.equalsIgnoreCase("state")){
	    		 final State state = this.stateRepository.findOne(id);
	    		 if(state==null){
	        		 throw new StateNotFoundException(id.toString());
	        	 }
	    		 state.delete();
	    		 this.stateRepository.save(state);
	    		 return new CommandProcessingResult(id);
	        	 
	    	 }else{
	    		 final Country country = this.countryRepository.findOne(id);
	    			if (country== null) { 
	    				throw new CountryNotFoundException(id.toString()); 
	    			}
	    			country.delete();
	    			this.countryRepository.save(country);
	    		 return new CommandProcessingResult(id);
	        	 
	    	 }
	}catch (DataIntegrityViolationException dve) {
		handleCodeDataIntegrityIssues(command,dve);
		return null;
  	}
  }
}


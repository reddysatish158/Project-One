/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.infrastructure.configuration.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.configuration.data.ConfigurationData;
import org.mifosplatform.infrastructure.configuration.data.ConfigurationPropertyData;
import org.mifosplatform.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/configurations")
@Component
@Scope("singleton")
public class ConfigurationApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("globalConfiguration"));

    private static final String RESOURCENAMEFORPERMISSIONS = "CONFIGURATION";
    private static final String CONFIGURATION_PATH_LOCATION = System.getProperty("user.home") + File.separator + ".obs" + File.separator + ".clientconfigurations";
    private static final String CONFIGURATION_FILE_LOCATION = CONFIGURATION_PATH_LOCATION + File.separator + "ClientConfiguration.txt";


    private final PlatformSecurityContext context;
    private final ConfigurationReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<ConfigurationData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<ConfigurationPropertyData> propertyDataJsonSerializer;

    @Autowired
    public ConfigurationApiResource(final PlatformSecurityContext context,
            final ConfigurationReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<ConfigurationData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<ConfigurationPropertyData> propertyDataJsonSerializer) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.propertyDataJsonSerializer=propertyDataJsonSerializer;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllConfigurations(@Context final UriInfo uriInfo) {

      //  context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);

        final ConfigurationData configurationData = this.readPlatformService.retrieveGlobalConfiguration();
        String defaultConfiguration = "{\"payment\":\"false\", \"IPTV\":\"false\", " +
        								"\"IsClientIndividual\":\"false\", \"deviceAgrementType\":\"SALE\", " +
        								"\"SubscriptionPayment\":\"false\",\"nationalId\":\"false\"}";
        String readDatas;
        File fileForPath = new File(CONFIGURATION_PATH_LOCATION);
        if(!fileForPath.isDirectory()){
        	fileForPath.mkdir();
        }
        File fileForLocation = new File(CONFIGURATION_FILE_LOCATION);
        if (!fileForLocation.isFile()) {
        	writeFileData(CONFIGURATION_FILE_LOCATION, defaultConfiguration);
        }else if(defaultConfiguration.split(",").length != readFileData(fileForLocation).split(",").length){
        	readDatas = readFileData(fileForLocation);
        	for(int i = readDatas.split(",").length; i < defaultConfiguration.split(",").length; i++ ){
        		readDatas = readDatas.split("}")[0] + "," + defaultConfiguration.split(",")[i];
        	}
        	writeFileData(CONFIGURATION_FILE_LOCATION, readDatas);
        }
        String returnData = readFileData(fileForLocation);
        configurationData.setClientConfiguration(returnData);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, configurationData, RESPONSE_DATA_PARAMETERS);
    }
    
    @GET
    @Path("{configId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveSingleConfiguration(@PathParam("configId") final Long configId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);

        final ConfigurationPropertyData configurationData = this.readPlatformService.retrieveGlobalConfiguration(configId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.propertyDataJsonSerializer.serialize(settings, configurationData, this.RESPONSE_DATA_PARAMETERS);
    }
    
    @PUT
    @Path("{configId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateConfiguration(@PathParam("configId") final Long configId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateConfiguration(configId) //
                .withJson(apiRequestBodyAsJson) //
                .build();
        
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        
        return this.toApiJsonSerializer.serialize(result);
    }
    
    @POST
    @Path("smtp")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String createSmtp(final String jsonRequestBody){
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().createSmtpConfiguration().withJson(jsonRequestBody).build();
    	final CommandProcessingResult result= this.commandsSourceWritePlatformService.logCommandSource(commandRequest); 
    	return this.toApiJsonSerializer.serialize(result);
    	
    }
    
    @PUT
    @Path("config")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateConfig(final String apiRequestBodyAsJson) throws JSONException {
    	String newtext = null;
    	JSONObject json = new JSONObject(apiRequestBodyAsJson);
    	String name = json.getString("name");
    	String oldValue = json.getString("oldValue");
    	String newValue = json.getString("newValue");
    	
    	File file = new File(CONFIGURATION_FILE_LOCATION);
    	String readData = readFileData(file);
    	newtext = readData.replaceAll("\""+name+"\":\""+oldValue+"\"", "\""+name+"\":\""+newValue+"\"");
    	writeFileData(CONFIGURATION_FILE_LOCATION, newtext);
		return newtext;
    }
    
    private String readFileData(File file){
    	String line = "", oldtext = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			while((line = reader.readLine()) != null)
			{
				oldtext += line;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return oldtext;
    }
    
    private void writeFileData(String fileLocation, String writeValue){
    		
		try {
			FileWriter writer = new FileWriter(fileLocation);
			writer.write(writeValue);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
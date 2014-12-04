package org.mifosplatform.portfolio.order.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.portfolio.order.data.OrderAddonsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/orderaddons")
@Component
@Scope("singleton")
public class OrderAddonsApiResource {
	
	private  final Set<String> RESPONSE_DATA_PARAMETERS=new HashSet<String>(Arrays.asList("id","orderId","serviceId","startDate","endDate",
			"contracrId","status","provisionSystem"));
	
	 private final String resourceNameForPermissions = "ADDONS";
	 private final DefaultToApiJsonSerializer<OrderAddonsData> toApiJsonSerializer;
	 private final ApiRequestParameterHelper apiRequestParameterHelper;
	  private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

@Autowired	  
public OrderAddonsApiResource(final DefaultToApiJsonSerializer<OrderAddonsData> apiJsonSerializer,final ApiRequestParameterHelper apiRequestParameterHelper,
			  final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService){
		  
		  this.toApiJsonSerializer=apiJsonSerializer;
		  this.apiRequestParameterHelper=apiRequestParameterHelper;
		  this.commandsSourceWritePlatformService=commandSourceWritePlatformService;
		  
	  }
	  
    @POST
	@Path("addons/{orderId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addOrderAddonServices(@PathParam("orderId") final Long orderId, final String apiRequestBodyAsJson){
			
	final CommandWrapper commandRequest = new CommandWrapperBuilder().createOrderAddons(orderId).withJson(apiRequestBodyAsJson).build();
	final CommandProcessingResult result=this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	return this.toApiJsonSerializer.serialize(result);
			
	}

}

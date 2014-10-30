package org.mifosplatform.organisation.redemption.api;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Class to Create {@link Redemption}
 * 
 * @author Raghu Chiluka
 *
 */
@Path("/redemption")
@Component
@Scope("singleton")
public class RedemptionApiResource {

	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */

	private final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService;
	
	@Autowired
	public RedemptionApiResource(final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService){
		this.portfolioCommandSourceWritePlatformService = portfolioCommandSourceWritePlatformService;
	}
	
	/**
	 * Defining Post Method for Creating Redemption
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createRedemption(final String apiReqBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createRedemption().withJson(apiReqBodyAsJson).build();
		this.portfolioCommandSourceWritePlatformService.logCommandSource(commandRequest);
		return null;
		
	}
}

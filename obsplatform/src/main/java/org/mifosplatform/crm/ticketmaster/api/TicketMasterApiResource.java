package org.mifosplatform.crm.ticketmaster.api;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.crm.ticketmaster.data.ClientTicketData;
import org.mifosplatform.crm.ticketmaster.data.TicketMasterData;
import org.mifosplatform.crm.ticketmaster.data.UsersData;
import org.mifosplatform.crm.ticketmaster.domain.TicketDetail;
import org.mifosplatform.crm.ticketmaster.domain.TicketDetailsRepository;
import org.mifosplatform.crm.ticketmaster.service.TicketMasterReadPlatformService;
import org.mifosplatform.crm.ticketmaster.service.TicketMasterWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Path("/tickets")
@Component
@Scope("singleton")
public class TicketMasterApiResource {
		
		private final String resourceNameForPermission = "TICKET";
		private final TicketMasterWritePlatformService ticketMasterWritePlatformService;
		private final TicketMasterReadPlatformService ticketMasterReadPlatformService ;
		private final DefaultToApiJsonSerializer<TicketMasterData> toApiJsonSerializer;
		private final DefaultToApiJsonSerializer<ClientTicketData> clientToApiJsonSerializer;
		private final DefaultToApiJsonSerializer<MCodeData> statusToApiJsonSerializer;
		private final ApiRequestParameterHelper apiRequestParameterHelper;
		private final TicketDetailsRepository detailsRepository;
		private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
		private final PlatformSecurityContext context;
		final private MCodeReadPlatformService codeReadPlatformService;
		private final FromJsonHelper fromApiJsonHelper;
		@Autowired
		public TicketMasterApiResource(final TicketMasterWritePlatformService ticketMasterWritePlatformService,final TicketMasterReadPlatformService ticketMasterReadPlatformService,
										final DefaultToApiJsonSerializer<TicketMasterData> toApiJsonSerializer, final DefaultToApiJsonSerializer<ClientTicketData> clientToApiJsonSerializer,
										final ApiRequestParameterHelper apiRequestParameterHelper, final TicketDetailsRepository detailsRepository,
										final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, final PlatformSecurityContext context,
										final MCodeReadPlatformService codeReadPlatformService,
										final FromJsonHelper fromApiJsonHelper,
										final DefaultToApiJsonSerializer<MCodeData> statusToApiJsonSerializer)	{
			this.ticketMasterWritePlatformService = ticketMasterWritePlatformService;
			this.ticketMasterReadPlatformService = ticketMasterReadPlatformService;
		    this.toApiJsonSerializer = toApiJsonSerializer;
			this.clientToApiJsonSerializer = clientToApiJsonSerializer;
			this.apiRequestParameterHelper = apiRequestParameterHelper;
			this.detailsRepository = detailsRepository;
			this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
			this.context = context;
			this.codeReadPlatformService = codeReadPlatformService;
			this.fromApiJsonHelper = fromApiJsonHelper;
			this.statusToApiJsonSerializer = statusToApiJsonSerializer;
		}
		private final Set<String> RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "priorityType", "problemsDatas", "usersData", "status", "assignedTo", 
														"userName", "ticketDate", "lastComment", "masterData"));   

		

		/**
		 * Method To Create TicketMaster 
		 * @param clientId
		 * @param jsonRequestBody
		 * @return String Json Result 
		 */
		@POST
	    @Path("{clientId}")
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		public String createTicketMaster(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {
			CommandProcessingResult result = null;
			Long userId = null;
			final SecurityContext context = SecurityContextHolder.getContext();
        	if (context.getAuthentication() != null) {
        		final AppUser appUser = this.context.authenticatedUser();
        		userId = appUser.getId();
        	}
        	if(userId != null){	 
        		final CommandWrapper commandRequest = new CommandWrapperBuilder().createTicketMaster(clientId).withJson(jsonRequestBody).build();
        		result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        		return this.toApiJsonSerializer.serialize(result);
        	}else{
        		final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonRequestBody.toString());
        		final JsonCommand command = JsonCommand.from(jsonRequestBody.toString(), parsedCommand, this.fromApiJsonHelper, "TICKET", clientId,
        													null, null, clientId, null, null, null, null, null, null, null);
        		result =this.ticketMasterWritePlatformService.createTicketMaster(command);
        		return this.toApiJsonSerializer.serialize(result);
        	}
		}	
		
		/**
		 * Method to Close Ticket
		 * @param ticketId
		 * @param jsonRequestBody
		 * @return
		 */
		@PUT
		@Path("{ticketId}")
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		public String closeTicket(@PathParam("ticketId") final Long ticketId, final String jsonRequestBody) {
			final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteTicketMaster(ticketId).withJson(jsonRequestBody).build();
			final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
			return this.toApiJsonSerializer.serialize(result);
		}
		
		
		/**
		 * Listing tickets
		 * */		
		@GET
		@Path("alltickets")
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		public String assignedAllTickets(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
				@QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset, @QueryParam("statusType") final String statusType){
			
			context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
			final SearchSqlQuery searchTicketMaster = SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		    final Page<ClientTicketData> data = this.ticketMasterReadPlatformService.retrieveAssignedTicketsForNewClient(searchTicketMaster,statusType);
		    
	        return this.clientToApiJsonSerializer.serialize(data);
		}
		
		/**
		 * Method for Retrieving Ticket Details for template
		 * @param uriInfo
		 * @return
		 */
		@GET
		@Path("template")
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		public String retrieveTicketMasterTemplateData(@Context final UriInfo uriInfo, @QueryParam("templateFor") final String templateFor) {
			
			context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
			final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			
			if(templateFor != null && "closeticket".equalsIgnoreCase(templateFor)){
				final Collection<MCodeData> closedStatusdata = this.codeReadPlatformService.getCodeValue("Ticket Status", "2");
				return this.statusToApiJsonSerializer.serialize(settings, closedStatusdata, RESPONSE_PARAMETERS);
				
			}else{
				final Collection<MCodeData> sourceData = codeReadPlatformService.getCodeValue("Ticket Source");
				final TicketMasterData templateData = handleTicketTemplateData(sourceData);
				return this.toApiJsonSerializer.serialize(settings, templateData, RESPONSE_PARAMETERS);
			}
		}
		
		/**
		 * Method for retrieve Single Ticket for clientId 
		 * @param clientId
		 * @param uriInfo
		 * @return
		 */
		@GET
	    @Path("{clientId}")
	    @Consumes({MediaType.APPLICATION_JSON})
	    @Produces({MediaType.APPLICATION_JSON})
	    public String retrieveSingleClientTicketDetails(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

			context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
	        final List<TicketMasterData> data = this.ticketMasterReadPlatformService.retrieveClientTicketDetails(clientId);
	        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	        return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_PARAMETERS);
	    }
		   
	    /**
	     * Method to get TicketDetails for update
	     * @param clientId
	     * @param ticketId
	     * @param uriInfo
	     * @return
	     */
	    @GET
	    @Path("{clientId}/update/{ticketId}")
	    @Consumes({MediaType.APPLICATION_JSON})
	    @Produces({MediaType.APPLICATION_JSON})
	    public String retrieveClientSingleTicketDetails(@PathParam("clientId")final Long clientId , @PathParam("ticketId") final Long ticketId,
	    												@Context final UriInfo uriInfo) {
	    	
	    	context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
	    	final TicketMasterData data = this.ticketMasterReadPlatformService.retrieveSingleTicketDetails(clientId, ticketId);
	        final Collection<MCodeData> statusdata = this.codeReadPlatformService.getCodeValue("Ticket Status", "1");
	        data.setStatusData(statusdata);
	        final List<UsersData>  userData = this.ticketMasterReadPlatformService.retrieveUsers();
	        data.setUsersData(userData);
	        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	        return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_PARAMETERS);
	    }
	   
		/**
		 * Method to retrieve Ticket History
		 * @param ticketId
		 * @param uriInfo
		 * @return
		 */
		@GET
	    @Path("{ticketId}/history")
	    @Consumes({MediaType.APPLICATION_JSON})
	    @Produces({MediaType.APPLICATION_JSON})
	    public String ticketHistory(@PathParam("ticketId") final Long ticketId, @Context final UriInfo uriInfo) {
			
			context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
			final String description = this.ticketMasterWritePlatformService.retrieveTicketProblems(ticketId);
	        final List<TicketMasterData> data = this.ticketMasterReadPlatformService.retrieveClientTicketHistory(ticketId);
	        
	        final TicketMasterData masterData = new TicketMasterData(description,data);
             
	        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
	        return this.toApiJsonSerializer.serialize(settings, masterData, RESPONSE_PARAMETERS);
	        
	    }
		
		/**
		 * Method to download file and print if exists
		 * @param ticketId
		 * @return
		 */
		@GET
		@Path("{ticketId}/print")
		@Consumes({ MediaType.APPLICATION_JSON })
		@Produces({ MediaType.APPLICATION_JSON })
		public Response downloadFile(@PathParam("ticketId") final Long ticketId) {

			context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
			final TicketDetail ticketDetail = this.detailsRepository.findOne(ticketId);
			final String printFileName = ticketDetail.getAttachments();
			final File file = new File(printFileName);
			final ResponseBuilder response = Response.ok(file);
			response.header("Content-Disposition", "attachment; filename=\"" + printFileName + "\"");
			response.header("Content-Type", "application/pdf");
			return response.build();
		}
		
		/**
		 * Method to handle Template Data
		 * @param responseParameters
		 * @param singleTicketData
		 * @return
		 */
		private TicketMasterData handleTicketTemplateData(final Collection<MCodeData> sourceData) {
			final List<EnumOptionData> priorityData = this.ticketMasterReadPlatformService.retrievePriorityData();
			final Collection<MCodeData> datas = this.codeReadPlatformService.getCodeValue("Problem Code");
			final List<UsersData>  userData = this.ticketMasterReadPlatformService.retrieveUsers();
			
			return  new TicketMasterData(datas, userData, priorityData, sourceData);
		}
		
		@GET
		@Path("{clientId}/{ticketId}")
		@Consumes({MediaType.APPLICATION_JSON})
		@Produces({MediaType.APPLICATION_JSON})
		public String retrieveClientSingleTicket(@PathParam("clientId")final Long clientId, @PathParam("ticketId") final Long ticketId, @Context final UriInfo uriInfo) {
		  
			context.authenticatedUser().validateHasReadPermission(resourceNameForPermission);
		    final TicketMasterData data = this.ticketMasterReadPlatformService.retrieveTicket(clientId, ticketId);
		    final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		    return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_PARAMETERS);
		     
		}
		 
}
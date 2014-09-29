package org.mifosplatform.provisioning.provisioning.api;

import java.io.File;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.jobs.exception.NoLogFileFoundException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.provisioning.provisioning.data.ProvisionAdapter;
import org.mifosplatform.provisioning.provisioning.data.ProvisioningData;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/adapter")
@Component
@Scope("singleton")
public class ProvisioningAdapterApiResource {

	private final String resourceNameForPermissions = "PROVISIONINGADAPTERSYSTEM";

	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<ProvisioningData> toApiJsonSerializer;
	private final ProvisioningWritePlatformService provisioningWritePlatformService;

	@Autowired
	public ProvisioningAdapterApiResource(final PlatformSecurityContext context,
			final DefaultToApiJsonSerializer<ProvisioningData> toApiJsonSerializer,
			final ProvisioningWritePlatformService provisioningWritePlatformService ) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.provisioningWritePlatformService = provisioningWritePlatformService;
	}

	@POST
	@Path("logs")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveProvisiongSystemDetail(final String apiRequestBodyAsJson) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		List<ProvisionAdapter> output = this.provisioningWritePlatformService.gettingLogInformation(apiRequestBodyAsJson);
		ProvisioningData data = new ProvisioningData();
		data.setProvisionAdapterData(output);
		return this.toApiJsonSerializer.serialize(data);
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addProvisiongSystemDetails(final String apiRequestBodyAsJson) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		String output = this.provisioningWritePlatformService.runAdapterCommands(apiRequestBodyAsJson);
		ProvisioningData data = new ProvisioningData();
		data.setStatus(output);
		return this.toApiJsonSerializer.serialize(data);
	}
	
	@GET
	@Path("printlog")
	@Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public Response logFile(@QueryParam("filePath") final String printFilePath) {
		if (printFilePath != null) {
			File file = new File(printFilePath);
			ResponseBuilder response = Response.ok(file);
			response.header("Content-Disposition", "attachment; filename=\"" + printFilePath + "\"");
			response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			return response.build();
		} else {
			throw new NoLogFileFoundException();
		}
	}
}

package org.mifosplatform.organisation.partneragreement.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.organisation.partneragreement.data.AgreementData;
import org.mifosplatform.portfolio.plan.service.PlanReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 *
 */
@Path("/agreements")
@Component
@Scope("singleton")
public class PartnersAgreementApiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(""));
	private final String resorceNameForPermission = "PARTNERSAGREEMENT";
	public static final String SOURCE_TYPE = "Source Type";
	public static final String AGREEMENT_TYPE = "Agreement Type";

	private final PlatformSecurityContext context;
	private final ToApiJsonSerializer<AgreementData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	private final PlanReadPlatformService planReadPlatformService;

	@Autowired
	public PartnersAgreementApiResource(final PlatformSecurityContext context,
			final ToApiJsonSerializer<AgreementData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final MCodeReadPlatformService mCodeReadPlatformService,
			final PlanReadPlatformService planReadPlatformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.mCodeReadPlatformService = mCodeReadPlatformService;
		this.planReadPlatformService = planReadPlatformService;

	}

	/**
	 * this method is using for getting template data to create a partner
	 * agreement
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTemplate(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resorceNameForPermission);
		AgreementData agreementData = handleAgreementTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, agreementData,RESPONSE_DATA_PARAMETERS);
	}

	private AgreementData handleAgreementTemplateData() {

		final Collection<MCodeData> shareTypes = this.mCodeReadPlatformService.getCodeValue("type");
		final Collection<MCodeData> sourceData = this.mCodeReadPlatformService.getCodeValue(SOURCE_TYPE);
		final Collection<MCodeData> agreementTypes = this.mCodeReadPlatformService.getCodeValue(AGREEMENT_TYPE);
		final List<EnumOptionData> statusData = this.planReadPlatformService.retrieveNewStatus();
		return new AgreementData(shareTypes, sourceData, statusData,agreementTypes);
	}

	/**
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Path("{partnerId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewPartnerAgreement(@PathParam("partnerId") final Long partnerId,final String apiRequestBodyAsJson) {

		context.authenticatedUser().validateHasReadPermission(resorceNameForPermission);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createPartnerAgreement(partnerId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

}

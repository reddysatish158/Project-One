package org.mifosplatform.organisation.partner.api;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiConstants;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.address.service.AddressReadPlatformService;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.organisation.monetary.data.ApplicationCurrencyConfigurationData;
import org.mifosplatform.organisation.monetary.service.OrganisationCurrencyReadPlatformService;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.partner.data.PartnersData;
import org.mifosplatform.organisation.partner.service.PartnersReadPlatformService;
import org.mifosplatform.organisation.partner.service.PartnersWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

/**
 * @author hugo
 *
 */
@Path("/partners")
@Component
@Scope("singleton")
public class PartnersApiResource {

  private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(""));
  private final String resorceNameForPermission = "PARTNERS";
  public static final String OFFICE_TYPE="Office Type";
  public static final String PARTNER_TYPE="Partner Type";
	
   private final PlatformSecurityContext context;
   private final ToApiJsonSerializer<PartnersData> toApiJsonSerializer;
   private final ApiRequestParameterHelper apiRequestParameterHelper;
   private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
   private final AddressReadPlatformService addressReadPlatformService;
   private final PartnersReadPlatformService readPlatformService;
   private final OfficeReadPlatformService officereadPlatformService;
   private final OrganisationCurrencyReadPlatformService currencyReadPlatformService;
   private final MCodeReadPlatformService mCodeReadPlatformService;
   private final CodeValueReadPlatformService codeValueReadPlatformService;
   private final PartnersWritePlatformService partnersWritePlatformService;
	
  @Autowired	
   public PartnersApiResource(final PlatformSecurityContext context,final ToApiJsonSerializer<PartnersData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final AddressReadPlatformService addressReadPlatformService,final PartnersReadPlatformService readPlatformService,
			final OrganisationCurrencyReadPlatformService currencyReadPlatformService, final MCodeReadPlatformService mCodeReadPlatformService,
			final OfficeReadPlatformService officereadPlatformService,final CodeValueReadPlatformService codeValueReadPlatformService,
			final PartnersWritePlatformService partnersWritePlatformService){
		
            this.context = context;
            this.toApiJsonSerializer = toApiJsonSerializer;
	        this.apiRequestParameterHelper = apiRequestParameterHelper;
	        this.commandSourceWritePlatformService = commandSourceWritePlatformService;
	        this.addressReadPlatformService=addressReadPlatformService;
	        this.readPlatformService = readPlatformService;
	        this.currencyReadPlatformService = currencyReadPlatformService;
	        this.mCodeReadPlatformService = mCodeReadPlatformService;
	        this.officereadPlatformService = officereadPlatformService;
	        this.codeValueReadPlatformService = codeValueReadPlatformService;
	        this.partnersWritePlatformService = partnersWritePlatformService;

	}
  
	/**
	 * this method is using for getting template data to create a partner
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTemplate(@Context final UriInfo uriInfo,@QueryParam("commandParam") final String commandParam) {

		context.authenticatedUser().validateHasReadPermission(resorceNameForPermission);
		PartnersData partnersData= handlePartnersTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, partnersData,RESPONSE_DATA_PARAMETERS);
	}

	private PartnersData handlePartnersTemplateData() {
		
		final Collection<MCodeData> partnerTypes = mCodeReadPlatformService.getCodeValue(PARTNER_TYPE);
		final List<String> countryData = this.addressReadPlatformService.retrieveCountryDetails();
		final List<String> statesData = this.addressReadPlatformService.retrieveStateDetails();
		final List<String> citiesData = this.addressReadPlatformService.retrieveCityDetails();
	    final Collection<CodeValueData> officeTypes=this.codeValueReadPlatformService.retrieveCodeValuesByCode(OFFICE_TYPE);
		final ApplicationCurrencyConfigurationData currencyData = this.currencyReadPlatformService.retrieveCurrencyConfiguration();
		final Collection<OfficeData> allowedParents = this.officereadPlatformService.retrieveAllOfficesForDropdown();
		return new PartnersData(partnerTypes,countryData,statesData,citiesData,officeTypes,currencyData,allowedParents);
	}
	
	
	/**
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewPartner(final String apiRequestBodyAsJson) {

		context.authenticatedUser().validateHasReadPermission(resorceNameForPermission);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createPartner().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	

    /**
     * @param uriInfo
     * @return all partners data
     */
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrievePartners(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resorceNameForPermission);
        final Collection<PartnersData> partners = this.readPlatformService.retrieveAllPartners();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, partners, RESPONSE_DATA_PARAMETERS);
    }
    
    
    /**
     * @param uriInfo
     * @return single partner details
     */
    @GET
    @Path("{partnerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrievePartner(@PathParam("partnerId") final Long partnerId,@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resorceNameForPermission);
        final PartnersData partner = this.readPlatformService.retrieveSinglePartnerDetails(partnerId);
        //final List<AgreementData> agreementData = this.readPlatformService.retrieveAgreementData(partnerId);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, partner, RESPONSE_DATA_PARAMETERS);
    }
    
    
    /**
     * Upload images through multi-part form upload
     */
	@POST
	@Path("{partnerId}/images")
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addNewPartnerImage(@PathParam("partnerId") final Long partnerId,@HeaderParam("Content-Length") final Long fileSize,
			@FormDataParam("file") final InputStream inputStream,@FormDataParam("file") final FormDataContentDisposition fileDetails,
			@FormDataParam("file") final FormDataBodyPart bodyPart) {

		FileUtils.validateClientImageNotEmpty(fileDetails.getFileName());
		FileUtils.validateImageMimeType(bodyPart.getMediaType().toString());
		FileUtils.validateFileSizeWithinPermissibleRange(fileSize,fileDetails.getFileName(),ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);

		final CommandProcessingResult result = this.partnersWritePlatformService.saveOrUpdatePartnerImage(partnerId, fileDetails.getFileName(),inputStream);
		
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * Upload image as a Data URL (essentially a base64 encoded stream)
	 */
	@POST
	@Path("{partnerId}/images")
	@Consumes({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML,
			MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addNewPartnerImage(@PathParam("partnerId") final Long partnerId,final String jsonRequestBody) {

		final Base64EncodedImage base64EncodedImage = FileUtils.extractImageFromDataURL(jsonRequestBody);

		final CommandProcessingResult result = this.partnersWritePlatformService.saveOrUpdatePartnerImage(partnerId, base64EncodedImage);

		return this.toApiJsonSerializer.serialize(result);
	}

}

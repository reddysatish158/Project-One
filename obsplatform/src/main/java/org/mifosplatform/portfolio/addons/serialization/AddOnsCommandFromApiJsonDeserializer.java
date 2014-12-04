package org.mifosplatform.portfolio.addons.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Deserializer for code JSON to validate API request.
 */
@Component
public final class AddOnsCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("addonServices","serviceId","chargeCode","price","planId",
    		"priceRegionId"));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AddOnsCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("addons");
        final JsonElement element = fromApiJsonHelper.parse(json);
        final JsonArray addonServicesArray = fromApiJsonHelper.extractJsonArrayNamed("addonServices", element);
        baseDataValidator.reset().parameter("addonServices").value(addonServicesArray).arrayNotEmpty();
        
        String[] serviceParameters = null;
		serviceParameters = new String[addonServicesArray.size()];
		int arraysize = addonServicesArray.size();
		baseDataValidator.reset().parameter(null).value(arraysize).integerGreaterThanZero();
		for (int i = 0; i < addonServicesArray.size(); i++) {
			serviceParameters[i] = addonServicesArray.get(i).toString();
		}
	
		
		for (String serviceParameter : serviceParameters) {

			final JsonElement attributeElement = fromApiJsonHelper.parse(serviceParameter);
			
			final Long planId = fromApiJsonHelper.extractLongNamed("planId", attributeElement);
			baseDataValidator.reset().parameter("planId").value(planId).notNull();
			final Long seviceId = fromApiJsonHelper.extractLongNamed("seviceId", attributeElement);
			baseDataValidator.reset().parameter("seviceId").value(seviceId).notNull();
			
			final Long priceRegionId = fromApiJsonHelper.extractLongNamed("priceRegionId", attributeElement);
			baseDataValidator.reset().parameter("priceRegionId").value(priceRegionId).notNull();
			
			final String chargeCode = fromApiJsonHelper.extractStringNamed("chargeCode", element);
			baseDataValidator.reset().parameter("chargeCode").value(chargeCode).notBlank();
			
			final BigDecimal price = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("price", element);
	        baseDataValidator.reset().parameter("price").value(price).notNull();

		
		}
        


        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }


    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }


}
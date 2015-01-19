package org.mifosplatform.organisation.partner.serialization;

import java.lang.reflect.Type;
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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class PartnersCommandFromApiJsonDeserializer {

	/*
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("partnerType", "partnerName","loginName","password","phone","email",
			                            "city","state","country","currency","organization","roleName","companyLogo","parentId","officeType","repeatPassword",
			                            "isCollective"));
	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public PartnersCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}
	
	/**
	 * @param json
	 * check validation for create partner
	 */
	public void validateForCreate(final String json) {
		
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("partner");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final String partnerName = fromApiJsonHelper.extractStringNamed("partnerName", element);
		baseDataValidator.reset().parameter("partnerName").value(partnerName).notBlank().notExceedingLengthOf(20);
		
		final String loginName = fromApiJsonHelper.extractStringNamed("loginName", element);
		baseDataValidator.reset().parameter("loginName").value(loginName).notBlank().notExceedingLengthOf(20);
		
		final String password = fromApiJsonHelper.extractStringNamed("password", element);
        baseDataValidator.reset().parameter("password").value(password).notBlank().notExceedingLengthOf(60);
        
        final String repeatPassword = fromApiJsonHelper.extractStringNamed("repeatPassword", element);
        baseDataValidator.reset().parameter("repeatPassword").value(repeatPassword).notBlank().notExceedingLengthOf(60);
        
        final Long partnerType = fromApiJsonHelper.extractLongNamed("partnerType", element);
        baseDataValidator.reset().parameter("partnerType").value(partnerType).notNull().integerGreaterThanZero();
        
        final Long parentId = fromApiJsonHelper.extractLongNamed("parentId", element);
        baseDataValidator.reset().parameter("parentId").value(parentId).notBlank();
        
        final String organization = fromApiJsonHelper.extractStringNamed("organization", element);
        baseDataValidator.reset().parameter("organization").value(organization).notBlank().notExceedingLengthOf(100);
        
        final String phone = fromApiJsonHelper.extractStringNamed("phone", element);
        baseDataValidator.reset().parameter("phone").value(phone).notBlank().notExceedingLengthOf(30);
        
        final String email = fromApiJsonHelper.extractStringNamed("email", element);
        baseDataValidator.reset().parameter("email").value(email).notBlank();
        
		final String city = fromApiJsonHelper.extractStringNamed("city", element);
		baseDataValidator.reset().parameter("city").value(city).notBlank().notExceedingLengthOf(100);
		
		final String state = fromApiJsonHelper.extractStringNamed("state", element);
		baseDataValidator.reset().parameter("state").value(state).notBlank().notExceedingLengthOf(100);
		
		final String country = fromApiJsonHelper.extractStringNamed("country", element);
		baseDataValidator.reset().parameter("country").value(country).notBlank().notExceedingLengthOf(100);
		
		final String currency = fromApiJsonHelper.extractStringNamed("currency", element);
        baseDataValidator.reset().parameter("currency").value(currency).notBlank();
		
		/*final String role = fromApiJsonHelper.extractStringNamed("roleName", element);
		baseDataValidator.reset().parameter("role").value(role).notBlank().notExceedingLengthOf(20);
		
		final Long officeType = fromApiJsonHelper.extractLongNamed("officeType", element);
		baseDataValidator.reset().parameter("officeType").value(officeType).notBlank().notExceedingLengthOf(20);
		*/
	
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}


	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
}



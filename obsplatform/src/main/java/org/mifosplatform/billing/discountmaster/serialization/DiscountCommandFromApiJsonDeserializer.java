package org.mifosplatform.billing.discountmaster.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Deserializer for code JSON to validate API request.
 */
@Component
public final class DiscountCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("discountCode", "discountDescription",
					"discountType", "discountRate", "startDate", "discountStatus","locale", "dateFormat"));
	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public DiscountCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	/**
	 * @param json
	 * check validation for create discount
	 */
	public void validateForCreate(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("discount");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final String discountCode = fromApiJsonHelper.extractStringNamed("discountCode", element);
		baseDataValidator.reset().parameter("discountCode").value(discountCode).notBlank().notExceedingLengthOf(20);
		
		final LocalDate startDate = fromApiJsonHelper.extractLocalDateNamed("startDate", element);
		baseDataValidator.reset().parameter("startDate").value(startDate).notBlank();
		
		final String discountDescription = fromApiJsonHelper.extractStringNamed("discountDescription", element);
		baseDataValidator.reset().parameter("discountDescription").value(discountDescription).notBlank();

		final String discountType = fromApiJsonHelper.extractStringNamed("discountType", element);
		baseDataValidator.reset().parameter("discountType").value(discountType).notBlank();
		
		final Integer discountRate = fromApiJsonHelper.extractIntegerWithLocaleNamed("discountRate", element);
		baseDataValidator.reset().parameter("discountRate").value(discountRate).notNull();
		
		final String discountStatus = fromApiJsonHelper.extractStringNamed("discountStatus",element);
		baseDataValidator.reset().parameter("discountStatus").value(discountStatus).notBlank();

		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}


	private void throwExceptionIfValidationWarningsExist(
			final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
}
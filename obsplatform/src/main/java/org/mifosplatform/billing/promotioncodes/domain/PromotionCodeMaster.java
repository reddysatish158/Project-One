package org.mifosplatform.billing.promotioncodes.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * @author hugo
 * 
 */
@Entity
@Table(name = "b_promotion_master", uniqueConstraints = @UniqueConstraint(name = "promotioncode", columnNames = { "promotion_code" }))
public class PromotionCodeMaster extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "promotion_code")
	private String promotionCode;

	@Column(name = "promotion_description")
	private String promotionDescription;

	@Column(name = "duration_type")
	private String durationType;

	@Column(name = "duration")
	private Long duration;

	@Column(name = "discount_type")
	private String discountType;

	@Column(name = "discount_rate")
	private BigDecimal discountRate;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "valid_until")
	private Date validUntil;

	@Column(name = "is_delete")
	private char isDeleted = 'N';

	public PromotionCodeMaster() {
		// TODO Auto-generated constructor stub

	}

	public PromotionCodeMaster(final String promotionCode,
			final String promotionDescription, final String durationType,
			final Long duration, final String discountType,
			final BigDecimal discountRate, final LocalDate startDate) {

		this.promotionCode = promotionCode;
		this.promotionDescription = promotionDescription;
		this.durationType = durationType;
		this.duration = duration;
		this.discountType = discountType;
		this.discountRate = discountRate;
		this.startDate = startDate.toDate();
		// this.validUntil=validUntil;
	}

	/**
	 * @return the promotionCode
	 */
	public String getPromotionCode() {
		return promotionCode;
	}

	public void setPromotionCode(final String promotionCode) {
		this.promotionCode = promotionCode;
	}

	/**
	 * @return the promotionDescription
	 */
	public String getPromotionDescription() {
		return promotionDescription;
	}

	public void setPromotionDescription(final String promotionDescription) {
		this.promotionDescription = promotionDescription;
	}

	/**
	 * @return the durationType
	 */
	public String getDurationType() {
		return durationType;
	}

	public void setDurationType(final String durationType) {
		this.durationType = durationType;
	}

	/**
	 * @return the duration
	 */
	public Long getDuration() {
		return duration;
	}

	public void setDuration(final Long duration) {
		this.duration = duration;
	}

	/**
	 * @return the discountType
	 */
	public String getDiscountType() {
		return discountType;
	}

	public void setDiscountType(final String discountType) {
		this.discountType = discountType;
	}

	/**
	 * @return the discountRate
	 */
	public BigDecimal getDiscountRate() {
		return discountRate;
	}

	public void setDiscountRate(final BigDecimal discountRate) {
		this.discountRate = discountRate;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(final Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @param command
	 * @return PromotionCodeMaster
	 */
	public static PromotionCodeMaster fromJson(final JsonCommand command) {

		final String promotionCode = command
				.stringValueOfParameterNamed("promotionCode");
		final String promotionDescription = command
				.stringValueOfParameterNamed("promotionDescription");
		final String durationType = command
				.stringValueOfParameterNamed("durationType");
		final Long duration = command.longValueOfParameterNamed("duration");
		final String discountType = command
				.stringValueOfParameterNamed("discountType");
		final BigDecimal discountRate = command
				.bigDecimalValueOfParameterNamed("discountRate");
		final LocalDate startDate = command
				.localDateValueOfParameterNamed("startDate");
		// final Date validUntil=command.DateValueOfParameterNamed("");

		return new PromotionCodeMaster(promotionCode, promotionDescription,
				durationType, duration, discountType, discountRate, startDate);
	}

	/**
	 * @param command
	 * @return changes of PromotionCode object
	 */
	public Map<String, Object> updatePromotion(final JsonCommand command) {
		final Map<String, Object> actualChanges = new ConcurrentHashMap<String, Object>(
				1);
		final String promotionCodeParamName = "promotionCode";
		if (command.isChangeInStringParameterNamed(promotionCodeParamName,
				this.promotionCode)) {
			final String newValue = command
					.stringValueOfParameterNamed(promotionCodeParamName);
			actualChanges.put(promotionCodeParamName, newValue);
			this.promotionCode = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String descriptionParamName = "promotionDescription";
		if (command.isChangeInStringParameterNamed(descriptionParamName,
				this.promotionDescription)) {
			final String newValue = command
					.stringValueOfParameterNamed(descriptionParamName);
			actualChanges.put(descriptionParamName, newValue);
			this.promotionDescription = StringUtils.defaultIfEmpty(newValue,
					null);
		}

		final String durationTypeParam = "durationType";
		if (command.isChangeInStringParameterNamed(durationTypeParam,
				this.durationType)) {
			final String newValue = command
					.stringValueOfParameterNamed(durationTypeParam);
			actualChanges.put(durationTypeParam, newValue);
			this.durationType = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String durationParam = "duration";
		if (command.isChangeInLongParameterNamed(durationParam, this.duration)) {
			final Long newValue = command
					.longValueOfParameterNamed(durationParam);
			actualChanges.put(durationParam, newValue);
			this.duration = newValue;
		}

		final String discountTypeParam = "discountType";
		if (command.isChangeInStringParameterNamed(discountTypeParam,
				this.discountType)) {
			final String newValue = command
					.stringValueOfParameterNamed(discountTypeParam);
			actualChanges.put(discountTypeParam, newValue);
			this.discountType = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String discountRateParamName = "discountRate";
		if (command.isChangeInBigDecimalParameterNamed(discountRateParamName,
				this.discountRate)) {
			final BigDecimal newValue = command
					.bigDecimalValueOfParameterNamed(discountRateParamName);
			actualChanges.put(discountRateParamName, newValue);
			this.discountRate = newValue;
		}

		final String startDateParamName = "startDate";
		if (command.isChangeInLocalDateParameterNamed(startDateParamName,
				new LocalDate(this.startDate))) {
			final LocalDate newValue = command
					.localDateValueOfParameterNamed(startDateParamName);
			actualChanges.put(startDateParamName, newValue);
			this.startDate = newValue.toDate();
		}

		return actualChanges;

	}

	/**
	 * updating column 'is_deleted' with 'Y' for delete of promotion code
	 */
	public void delete() {

		if (this.isDeleted == 'N') {
			this.isDeleted = 'Y';
		}

	}

}

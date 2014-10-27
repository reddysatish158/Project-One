package org.mifosplatform.billing.discountmaster.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;

/**
 * @author hugo
 *
 */
public class DiscountMasterData {
	private Long id;
	private String discountCode;
	private String discountDescription;
	private String discountType;
	private BigDecimal discountRate;
	private LocalDate discountStartDate;
	private LocalDate discountEndDate;
	private String isDeleted;
	private Long orderPriceId;
	private Long discountMasterId;
	private BigDecimal discountAmount;
	private BigDecimal discountedChargeAmount;
	private List<EnumOptionData> status;
	private Collection<MCodeData> discountTypeData;
	private String discountstatus;
	

	public DiscountMasterData(final Long id, final String discountCode,
			final String discountDescription, final String discounType,
			final BigDecimal discountRate, final LocalDate startDate,
			final String discountstatus) {
		this.discountMasterId = id;
		this.discountCode = discountCode;
		this.discountDescription = discountDescription;
		this.discountType = discounType;
		this.discountRate = discountRate;
		this.discountAmount = BigDecimal.ZERO;
		this.discountedChargeAmount = BigDecimal.ZERO;
		this.discountstatus = discountstatus;
		this.discountStartDate = startDate;

	}

	public DiscountMasterData(final Long id, final Long orderPriceId,
			final Long orderDiscountId, final LocalDate discountStartDate,
			final LocalDate discountEndDate, final String discountType, final BigDecimal discountRate,
			final String isDeleted) {
		this.id = id;
		this.orderPriceId = orderPriceId;
		this.discountMasterId = orderDiscountId;
		this.discountStartDate = discountStartDate;
		this.discountEndDate = discountEndDate;
		this.discountType = discountType;
		this.discountRate = discountRate;
		this.isDeleted = isDeleted;
		this.discountAmount = BigDecimal.ZERO;
		this.discountedChargeAmount = BigDecimal.ZERO;
	}

	/**
	 * @param status
	 * @param discountTypeData
	 */
	public DiscountMasterData(final List<EnumOptionData> status,
			final Collection<MCodeData> discountTypeData) {
		this.status = status;
		this.discountTypeData = discountTypeData;

	}

	public BigDecimal getDiscountedChargeAmount() {
		return discountedChargeAmount;
	}

	public void setDiscountedChargeAmount(BigDecimal discountedChargeAmount) {
		this.discountedChargeAmount = discountedChargeAmount;
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getDiscountCode() {
		return discountCode;
	}

	public void setDiscountCode(final String discountCode) {
		this.discountCode = discountCode;
	}

	public String getDiscountDescription() {
		return discountDescription;
	}

	public void setDiscountDescription(final String discountDescription) {
		this.discountDescription = discountDescription;
	}

	public String getDiscountType() {
		return discountType;
	}

	public void setDiscounType(final String discountType) {
		this.discountType = discountType;
	}

	public LocalDate getDiscountStartDate() {
		return discountStartDate;
	}

	public void setDiscountStartDate(final LocalDate discountStartDate) {
		this.discountStartDate = discountStartDate;
	}

	public LocalDate getDiscountEndDate() {
		return discountEndDate;
	}

	public void setDiscountEndDate(LocalDate discountEndDate) {
		this.discountEndDate = discountEndDate;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Long getOrderPriceId() {
		return orderPriceId;
	}

	public void setOrderPriceId(final Long orderPriceId) {
		this.orderPriceId = orderPriceId;
	}

	public Long getDiscountMasterId() {
		return discountMasterId;
	}

	public void setDiscountMasterId(final Long discountMasterId) {
		this.discountMasterId = discountMasterId;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(final BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	public BigDecimal getDiscountRate() {
		return discountRate;
	}

	public void setDiscountRate(final BigDecimal discountRate) {
		this.discountRate = discountRate;
	}

	public List<EnumOptionData> getStatus() {
		return status;
	}

	public void setStatus(final List<EnumOptionData> status) {
		this.status = status;
	}

	public Collection<MCodeData> getDiscounTypeData() {
		return discountTypeData;
	}

	public void setDiscounTypeData(final Collection<MCodeData> discountTypeData) {
		this.discountTypeData = discountTypeData;
	}

	public String getDiscountstatus() {
		return discountstatus;
	}

	public void setDiscountstatus(final String discountstatus) {
		this.discountstatus = discountstatus;
	}



}

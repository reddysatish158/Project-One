package org.mifosplatform.portfolio.addons.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.useradministration.domain.AppUser;

import com.google.gson.JsonElement;

@Entity
@Table(name="b_addons")
public class Addons extends AbstractAuditableCustom<AppUser,Long>{
	
	
	@Column(name = "plan_id")
	private Long planId;

	@Column(name = "service_id")
	private Long serviceId;

	@Column(name = "charge_code")
	private String chargeCode;

	@Column(name = "price_region_id")
	private Long priceRegionId;

	@Column(name = "price")
	private BigDecimal price;
	
	@Column(name ="is_deleted")
	private char isDelete ='N';
	
	
	public Addons(Long planId, Long serviceId, String chargeCode, Long priceRegionId, BigDecimal price){
	
		this.planId=planId;
		this.serviceId=serviceId;
		this.chargeCode=chargeCode;
		this.priceRegionId=priceRegionId;
		this.price=price;
		
	}

	

	public static Addons fromJson(final JsonElement element,final FromJsonHelper fromJsonHelper, Long planId) {
		
		final Long serviceId=fromJsonHelper.extractLongNamed("serviceId", element);
		final Long priceRegionId=fromJsonHelper.extractLongNamed("priceRegionId", element);
		final String chargeCode =fromJsonHelper.extractStringNamed("chargeCode", element);
		final BigDecimal price=fromJsonHelper.extractBigDecimalWithLocaleNamed("price", element);
		
		return new Addons(planId,serviceId,chargeCode,priceRegionId,price);
	}

	

}

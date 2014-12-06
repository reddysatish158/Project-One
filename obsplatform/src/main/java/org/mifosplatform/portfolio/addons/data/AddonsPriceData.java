package org.mifosplatform.portfolio.addons.data;

import java.math.BigDecimal;

public class AddonsPriceData {
	
	private final Long id;
	private final String serviceCode;
	private final Long serviceId;
	private final BigDecimal price;

	public AddonsPriceData(Long id, Long serviceId, String serviceCode,
			BigDecimal price) {
		
		this.id=id;
		this.serviceCode=serviceCode;
		this.serviceId=serviceId;
		this.price=price;
	}

}

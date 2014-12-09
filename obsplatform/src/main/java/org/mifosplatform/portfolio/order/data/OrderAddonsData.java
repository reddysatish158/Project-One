package org.mifosplatform.portfolio.order.data;

import java.util.List;

import org.mifosplatform.portfolio.addons.data.AddonsPriceData;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;

public class OrderAddonsData {
	
	private final List<AddonsPriceData> addonsPriceDatas;
	private final List<SubscriptionData> contractPeriods;

	public OrderAddonsData(List<AddonsPriceData> addonsPriceDatas,List<SubscriptionData> contractPeriods) {
		
		this.addonsPriceDatas=addonsPriceDatas;
		this.contractPeriods=contractPeriods;
	}

}

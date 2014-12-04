package org.mifosplatform.portfolio.addons.data;

import java.util.List;

import org.mifosplatform.billing.chargecode.data.ChargeCodeData;
import org.mifosplatform.organisation.priceregion.data.PriceRegionData;
import org.mifosplatform.portfolio.plan.data.PlanCodeData;
import org.mifosplatform.portfolio.servicemapping.data.ServiceMappingData;

public class AddonsData {

	private final List<PlanCodeData> planDatas;
	private final List<ChargeCodeData> chargeCodeDatas;
	private final List<PriceRegionData> priceRegionData;
	private final List<ServiceMappingData> servicedatas;
	
	
	public AddonsData(List<PlanCodeData> planDatas,List<ChargeCodeData> chargeCodeDatas,List<PriceRegionData> priceRegionData,
			    List<ServiceMappingData> servicedatas) {
		
		this.servicedatas=servicedatas;
		this.planDatas=planDatas;
		this.chargeCodeDatas=chargeCodeDatas;
		this.priceRegionData=priceRegionData;
	
	}

}

package org.mifosplatform.portfolio.plan.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.plan.data.BillRuleData;
import org.mifosplatform.portfolio.plan.data.PlanData;
import org.mifosplatform.portfolio.plan.data.ServiceData;

public interface PlanReadPlatformService {
	
	
	List<PlanData> retrievePlanData(String planType);
	
	List<SubscriptionData> retrieveSubscriptionData(Long orderId, String planType);
	
	List<EnumOptionData> retrieveNewStatus();
	
	List<BillRuleData> retrievebillRules();
	
	PlanData retrievePlanData(Long planCode);
	
	List<ServiceData> retrieveSelectedServices(Long planId);
	
	List<EnumOptionData> retrieveVolumeTypes();



}

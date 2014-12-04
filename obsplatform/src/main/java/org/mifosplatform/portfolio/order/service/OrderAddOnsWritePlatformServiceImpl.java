package org.mifosplatform.portfolio.order.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.contract.domain.Contract;
import org.mifosplatform.portfolio.contract.domain.ContractRepository;
import org.mifosplatform.portfolio.order.domain.OrderAddons;
import org.mifosplatform.portfolio.order.domain.OrderAddonsRepository;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.domain.StatusTypeEnum;
import org.mifosplatform.portfolio.order.serialization.OrderAddOnsCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.servicemapping.domain.ServiceMapping;
import org.mifosplatform.portfolio.servicemapping.domain.ServiceMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


@Service
public class OrderAddOnsWritePlatformServiceImpl implements OrderAddOnsWritePlatformService{
	
	private final PlatformSecurityContext context;
	private final FromJsonHelper fromJsonHelper;
	private final ServiceMappingRepository serviceMappingRepository;
	private final OrderAddOnsCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ContractRepository contractRepository;
	private final OrderAssembler orderAssembler;
	private final OrderRepository orderRepository;
	private final OrderAddonsRepository addonsRepository;
	
@Autowired
 public OrderAddOnsWritePlatformServiceImpl(final PlatformSecurityContext context,final OrderAddOnsCommandFromApiJsonDeserializer fromApiJsonDeserializer,
		 final FromJsonHelper fromJsonHelper,final ContractRepository contractRepository,final OrderAssembler orderAssembler,
		 final OrderRepository orderRepository,final ServiceMappingRepository serviceMappingRepository,final OrderAddonsRepository addonsRepository){
		
	this.context=context;
	this.fromJsonHelper=fromJsonHelper;
	this.fromApiJsonDeserializer=fromApiJsonDeserializer;
	this.contractRepository=contractRepository;
	this.orderRepository=orderRepository;
	this.orderAssembler=orderAssembler;
	this.addonsRepository=addonsRepository;
	this.serviceMappingRepository=serviceMappingRepository;
	
}


@Override
public CommandProcessingResult createOrderAddons(JsonCommand command,Long orderId) {
	
	try{
		
		this.context.authenticatedUser();
		this.fromApiJsonDeserializer.validateForCreate(command.json());
		final JsonElement element = fromJsonHelper.parse(command.json());
		final JsonArray addonServices = fromJsonHelper.extractJsonArrayNamed("addonServices", element);
		
		for (JsonElement jsonElement : addonServices) {
			OrderAddons addons=assembleOrderAddons(jsonElement,fromJsonHelper,orderId);
			this.addonsRepository.saveAndFlush(addons);
		}
		
	}catch(DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(command, dve);
	}
	return null;
}


private OrderAddons assembleOrderAddons(JsonElement jsonElement,FromJsonHelper fromJsonHelper, Long orderId) {
	
	OrderAddons orderAddons = OrderAddons.fromJson(jsonElement,fromJsonHelper,orderId);
	
	Contract contract=this.contractRepository.findOne(orderAddons.getContractId());
	final LocalDate endDate = this.orderAssembler.calculateEndDate(new LocalDate(orderAddons.getStartDate()),
			                    contract.getSubscriptionType(), contract.getUnits());
	
	List<ServiceMapping> serviceMapping=this.serviceMappingRepository.findOneByServiceId(orderAddons.getServiceId());
	String status=StatusTypeEnum.ACTIVE.toString();
	if(!"None".equalsIgnoreCase(serviceMapping.get(0).getProvisionSystem())){
		status=StatusTypeEnum.PENDING.toString();
	}
	
	orderAddons.setEndDate(endDate.toDate());
	orderAddons.setProvisionSystem(serviceMapping.get(0).getProvisionSystem());
	orderAddons.setStatus(status);
	
	
	return orderAddons; 
}


private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
	// TODO Auto-generated method stub
	
}

}

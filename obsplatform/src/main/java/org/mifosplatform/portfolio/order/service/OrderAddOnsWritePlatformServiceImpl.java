package org.mifosplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.finance.billingorder.service.InvoiceClient;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.association.domain.HardwareAssociation;
import org.mifosplatform.portfolio.contract.domain.Contract;
import org.mifosplatform.portfolio.contract.domain.ContractRepository;
import org.mifosplatform.portfolio.order.domain.HardwareAssociationRepository;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderAddons;
import org.mifosplatform.portfolio.order.domain.OrderAddonsRepository;
import org.mifosplatform.portfolio.order.domain.OrderPrice;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.domain.StatusTypeEnum;
import org.mifosplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.mifosplatform.portfolio.order.exceptions.AddonEndDateValidationException;
import org.mifosplatform.portfolio.order.serialization.OrderAddOnsCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.servicemapping.domain.ServiceMapping;
import org.mifosplatform.portfolio.servicemapping.domain.ServiceMappingRepository;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
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
	private final ProvisioningWritePlatformService provisioningWritePlatformService;
	private final OrderAssembler orderAssembler;
	private final InvoiceClient invoiceClient;
	private final OrderRepository orderRepository;
	private final HardwareAssociationRepository hardwareAssociationRepository;
	private final OrderAddonsRepository addonsRepository;
	
@Autowired
 public OrderAddOnsWritePlatformServiceImpl(final PlatformSecurityContext context,final OrderAddOnsCommandFromApiJsonDeserializer fromApiJsonDeserializer,
		 final FromJsonHelper fromJsonHelper,final ContractRepository contractRepository,final OrderAssembler orderAssembler,final OrderRepository orderRepository,
		 final ServiceMappingRepository serviceMappingRepository,final OrderAddonsRepository addonsRepository,final InvoiceClient invoiceClient,
		 final ProvisioningWritePlatformService provisioningWritePlatformService,final HardwareAssociationRepository associationRepository){
		
	this.context=context;
	this.fromJsonHelper=fromJsonHelper;
	this.fromApiJsonDeserializer=fromApiJsonDeserializer;
	this.contractRepository=contractRepository;
	this.orderRepository=orderRepository;
	this.provisioningWritePlatformService=provisioningWritePlatformService;
	this.orderAssembler=orderAssembler;
	this.hardwareAssociationRepository=associationRepository;
	this.invoiceClient=invoiceClient;
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
		final String planName=command.stringValueOfParameterNamed("planName");
	    Order order=this.orderRepository.findOne(orderId);
	    HardwareAssociation association=this.hardwareAssociationRepository.findOneByOrderId(orderId);
		for (JsonElement jsonElement : addonServices) {
			OrderAddons addons=assembleOrderAddons(jsonElement,fromJsonHelper,order);
			this.addonsRepository.saveAndFlush(addons);
			
			if(!"None".equalsIgnoreCase(addons.getProvisionSystem())){
				this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, planName, UserActionStatusTypeEnum.ACTIVATION.toString(),
						Long.valueOf(0), null,association.getSerialNo(),orderId, addons.getProvisionSystem(), null);
			}
		}
		
		if(order.getNextBillableDay() != null){
			this.invoiceClient.invoicingSingleClient(order.getClientId(),new LocalDate());
		}
		
	}catch(DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(command, dve);
	}
	return null;
}


private OrderAddons assembleOrderAddons(JsonElement jsonElement,FromJsonHelper fromJsonHelper, Order order) {
	
	OrderAddons orderAddons = OrderAddons.fromJson(jsonElement,fromJsonHelper,order.getId());
	final BigDecimal price=fromJsonHelper.extractBigDecimalWithLocaleNamed("price", jsonElement);
	Contract contract=this.contractRepository.findOne(orderAddons.getContractId());
	final LocalDate endDate = this.orderAssembler.calculateEndDate(new LocalDate(orderAddons.getStartDate()),
			                    contract.getSubscriptionType(), contract.getUnits());
	
	if(order.getEndDate() != null && endDate.isAfter(new LocalDate(order.getEndDate()))){
		throw new AddonEndDateValidationException(orderAddons.getServiceId());
	}
	
	List<OrderPrice> orderPrices = order.getPrice();
	OrderPrice orderPrice =new OrderPrice(orderAddons.getServiceId(),orderPrices.get(0).getChargeCode(),orderPrices.get(0).getChargeType(), price,null,
			orderPrices.get(0).getChargeType(),orderPrices.get(0).getChargeDuration(),orderPrices.get(0).getDurationType(),
			orderAddons.getStartDate(),endDate,orderPrices.get(0).isTaxInclusive());
	order.addOrderDeatils(orderPrice);
	
	this.orderRepository.saveAndFlush(order);
	
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

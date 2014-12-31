package org.mifosplatform.organisation.redemption.service;



import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.mifosplatform.finance.adjustment.service.AdjustmentWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.redemption.exception.PinNumberAlreadyUsedException;
import org.mifosplatform.organisation.redemption.exception.PinNumberNotFoundException;
import org.mifosplatform.organisation.redemption.serialization.RedemptionCommandFromApiJsonDeserializer;
import org.mifosplatform.organisation.voucher.domain.Voucher;
import org.mifosplatform.organisation.voucher.domain.VoucherDetails;
import org.mifosplatform.organisation.voucher.domain.VoucherDetailsRepository;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

@Service
public class RedemptionWritePlatformServiceImpl implements
		RedemptionWritePlatformService {

	private final static Logger LOGGER = LoggerFactory.getLogger(RedemptionWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;	
	private final FromJsonHelper fromJsonHelper;
	private final VoucherDetailsRepository voucherDetailsRepository;
	private final ClientRepository clientRepository;
	private final AdjustmentWritePlatformService adjustmentWritePlatformService;
	private final OrderWritePlatformService orderWritePlatformService;
	private final RedemptionReadPlatformService redemptionReadPlatformService;
	private final RedemptionCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;
	private final OrderRepository orderRepository;
	private final static String DATEFORMAT = "dd MMMM yyyy";
	private final String simpleDateFormat = new SimpleDateFormat(DATEFORMAT).format(new Date());
	private final static String VALUE_PINTYPE = "VALUE";
	private final static String PRODUCE_PINTYPE = "PRODUCT";
	private final static int RECONNECT_ORDER_STATUS = 3;
	private final static int RENEWAL_ORDER_STATUS = 1;
	private final static String USED = "USED";
	
	@Autowired
	public RedemptionWritePlatformServiceImpl(final PlatformSecurityContext context,final VoucherDetailsRepository voucherDetailsRepository,
		final ClientRepository clientRepository,final AdjustmentWritePlatformService adjustmentWritePlatformService,final FromJsonHelper fromJsonHelper,
		final OrderWritePlatformService orderWritePlatformService,final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
		final RedemptionReadPlatformService redemptionReadPlatformService,final OrderRepository orderRepository,final RedemptionCommandFromApiJsonDeserializer apiJsonDeserializer) {
		
		this.context = context;
		this.fromJsonHelper = fromJsonHelper;
		this.orderRepository=orderRepository;
		this.clientRepository = clientRepository;
		this.fromApiJsonDeserializer= apiJsonDeserializer;
		this.orderWritePlatformService = orderWritePlatformService;
		this.redemptionReadPlatformService=redemptionReadPlatformService;
		this.adjustmentWritePlatformService = adjustmentWritePlatformService;
		this.voucherDetailsRepository = voucherDetailsRepository;
		this.contractPeriodReadPlatformService = contractPeriodReadPlatformService;
		
	}
	
	/**
	 * Implementing createRedemption method
	 */
	@Transactional
	@Override
	public CommandProcessingResult createRedemption(final JsonCommand command) {
		try {
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final Long clientId = command.longValueOfParameterNamed("clientId");
			final String pinNum = command.stringValueOfParameterNamed("pinNumber");
			this.clientObjectRetrieveById(clientId);

			final VoucherDetails voucherDetails = retrieveRandomDetailsByPinNo(pinNum);
			final Voucher voucher = voucherDetails.getVoucher();
			final String pinType = voucher.getPinType();
			final String pinTypeValue =  voucher.getPinValue();
			 
			if(pinType.equalsIgnoreCase(VALUE_PINTYPE) && pinTypeValue != null){
				
				final BigDecimal pinValue = new BigDecimal(pinTypeValue);
				final JsonObject json = new JsonObject();
				json.addProperty("adjustment_type", "CREDIT");json.addProperty("adjustment_code", 123);
				json.addProperty("amount_paid",pinValue);json.addProperty("Remarks", "Adjustment Post By Redemption");
				json.addProperty("locale", "en");json.addProperty("dateFormat",DATEFORMAT);
				json.addProperty("adjustment_date", simpleDateFormat);
				final JsonCommand commd = new JsonCommand(null, json.toString(), json, fromJsonHelper, null, clientId, null, null, clientId, null, null, null, null, null, null,null);
				this.adjustmentWritePlatformService.createAdjustments(commd);
			}
			 
			if(pinType.equalsIgnoreCase(PRODUCE_PINTYPE) && pinTypeValue != null){
				 
				final Long planId = Long.parseLong(pinTypeValue);
				final List<Long> orderIds=this.redemptionReadPlatformService.retrieveOrdersData(clientId,planId);
				final JsonObject json = new JsonObject();
				final List<SubscriptionData> subscriptionDatas=this.contractPeriodReadPlatformService.retrieveSubscriptionDatabyContractType("Month(s)",1);
				
				if(orderIds.isEmpty()){
					 
					json.addProperty("billAlign", false);json.addProperty("planCode", planId);
					json.addProperty("contractPeriod", subscriptionDatas.get(0).getId());
					json.addProperty("isNewplan", true);
					json.addProperty("paytermCode", "Monthly");json.addProperty("locale", "en");
					json.addProperty("dateFormat",DATEFORMAT); json.addProperty("start_date", simpleDateFormat);
					final JsonCommand commd = new JsonCommand(null, json.toString(), json, fromJsonHelper, null,clientId, null, null, null, null, null, null, null, null, null,null);
					this.orderWritePlatformService.createOrder(clientId, commd);
				 
				}else {
					 
					final Long orderId = orderIds.get(0);
					 
					final Order order=this.orderRepository.findOne(orderId);
					 
						if(order.getStatus() == RECONNECT_ORDER_STATUS){					
							this.orderWritePlatformService.reconnectOrder(orderId);
						} else if(order.getStatus() == RENEWAL_ORDER_STATUS){
							
							json.addProperty("renewalPeriod", subscriptionDatas.get(0).getId());
							json.addProperty("description", "Order Renewal By Redemption");
							final JsonCommand commd = new JsonCommand(null, json.toString(), json, fromJsonHelper, null, clientId, null, null, clientId, null, null, null, null, null, null,null);
							this.orderWritePlatformService.renewalClientOrder(commd, orderId);				
						}
				}
			}
			  
			voucherDetails.setClientId(clientId);
			voucherDetails.setStatus(USED);
			
			this.voucherDetailsRepository.save(voucherDetails);
			 
			return new CommandProcessingResult(clientId);
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(dve);
	    	return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}
	

	private VoucherDetails retrieveRandomDetailsByPinNo(String pinNumber) {

		final VoucherDetails voucherDetails = this.voucherDetailsRepository.findOneByPinNumber(pinNumber);
		
		if (voucherDetails == null) {
			throw new PinNumberNotFoundException(pinNumber);
			
		}else if (voucherDetails.getClientId()!=null || voucherDetails.getStatus().equalsIgnoreCase("USED")) {
			throw new PinNumberAlreadyUsedException(pinNumber);
		}
		return voucherDetails;
	}

	private Client clientObjectRetrieveById(final Long clientId) {
		
		final Client client = this.clientRepository.findOne(clientId);
		if (client== null) { throw new ClientNotFoundException(clientId); }
		return client;
	}

	private void handleCodeDataIntegrityIssues(final DataIntegrityViolationException dve) {
		 final Throwable realCause = dve.getMostSpecificCause();

	        LOGGER.error(dve.getMessage(), dve);
	        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource: " + realCause.getMessage());
		
	}
	
}

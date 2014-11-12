package org.mifosplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.finance.billingorder.commands.BillingOrderCommand;
import org.mifosplatform.finance.billingorder.data.BillingOrderData;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReverseInvoice {
	
	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final GenerateReverseBillingOrderService generateReverseBillingOrderService;
	private final GenerateBillingOrderService generateBillingOrderService;
	private final BillingOrderWritePlatformService billingOrderWritePlatformService;
	
	
	@Autowired
	public ReverseInvoice(final BillingOrderReadPlatformService billingOrderReadPlatformService,final GenerateBillingOrderService generateBillingOrderService,
			final GenerateReverseBillingOrderService generateReverseBillingOrderService,final BillingOrderWritePlatformService billingOrderWritePlatformService){
		
		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.generateReverseBillingOrderService = generateReverseBillingOrderService;
		this.billingOrderWritePlatformService=billingOrderWritePlatformService;
		this.generateBillingOrderService=generateBillingOrderService;
	}
	
	 
	public BigDecimal reverseInvoiceServices(final Long orderId,final Long clientId,final LocalDate disconnectionDate){
		
	    Invoice invoice=null;
	    BigDecimal invoiceAmount=BigDecimal.ZERO;
	   
		List<BillingOrderData> billingOrderProducts = this.billingOrderReadPlatformService.getReverseBillingOrderData(clientId, disconnectionDate, orderId);
		
		List<BillingOrderCommand> billingOrderCommands = this.generateReverseBillingOrderService.generateReverseBillingOrder(billingOrderProducts,disconnectionDate);
		
		if(billingOrderCommands.get(0).getChargeType().equalsIgnoreCase("RC")){
			 invoice = this.generateBillingOrderService. generateInvoice(billingOrderCommands);
			 invoiceAmount=invoice.getInvoiceAmount();
		}else{
	        invoice = this.generateReverseBillingOrderService.generateNegativeInvoice(billingOrderCommands);
	        invoiceAmount=invoice.getInvoiceAmount();
		}
		
		//List<ClientBalanceData> clientBalancesDatas = clientBalanceReadPlatformService.retrieveAllClientBalances(clientId);
		
		this.billingOrderWritePlatformService.updateClientBalance(invoice,clientId);
		
		this.billingOrderWritePlatformService.updateBillingOrder(billingOrderCommands);
	    //this.billingOrderWritePlatformService.updateOrderPrice(billingOrderCommands);
		 
		return invoiceAmount;
	}

}

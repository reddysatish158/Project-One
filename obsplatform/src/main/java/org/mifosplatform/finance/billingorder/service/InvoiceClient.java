package org.mifosplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.finance.billingorder.commands.BillingOrderCommand;
import org.mifosplatform.finance.billingorder.data.BillingOrderData;
import org.mifosplatform.finance.billingorder.data.GenerateInvoiceData;
import org.mifosplatform.finance.billingorder.data.ProcessDate;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.mifosplatform.finance.billingorder.serialization.BillingOrderCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class InvoiceClient {

	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final GenerateBillingOrderService generateBillingOrderService;
	private final BillingOrderWritePlatformService billingOrderWritePlatformService;
	private final BillingOrderCommandFromApiJsonDeserializer apiJsonDeserializer;
	
	
	
	
	@Autowired
	InvoiceClient(final BillingOrderReadPlatformService billingOrderReadPlatformService,final GenerateBillingOrderService generateBillingOrderService,
			final BillingOrderWritePlatformService billingOrderWritePlatformService,final BillingOrderCommandFromApiJsonDeserializer apiJsonDeserializer) {

		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.generateBillingOrderService = generateBillingOrderService;
		this.billingOrderWritePlatformService = billingOrderWritePlatformService;
		this.apiJsonDeserializer=apiJsonDeserializer;
	}
	
	
	public BigDecimal invoicingSingleClient(Long clientId, LocalDate processDate) {

		     // Get list of qualified orders
    		List<BillingOrderData> billingOrderDatas= billingOrderReadPlatformService.retrieveOrderIds(clientId, processDate);
		         if (billingOrderDatas.size() == 0) {
			          throw new BillingOrderNoRecordsFoundException();
		          }
		             BigDecimal invoiceAmount=BigDecimal.ZERO;
		             Date nextBillableDate=null;
		               for (BillingOrderData billingOrderData : billingOrderDatas) {
		            	   
                                  nextBillableDate=billingOrderData.getNextBillableDate();		            	
		            	   while(processDate.toDate().after(nextBillableDate) || processDate.toDate().compareTo(nextBillableDate) == 0){
		            	  
          	                   GenerateInvoiceData invoice=invoiceServices(billingOrderData,clientId,processDate);
                 	             
          	                   if(invoice!=null){
          	            	
	                               invoiceAmount=invoiceAmount.add(invoice.getInvoiceAmount());
	                               nextBillableDate=invoice.getNextBillableDay();
          	            }
		}
		               }
		return invoiceAmount;
	}
	
	public GenerateInvoiceData invoiceServices(BillingOrderData billingOrderData,Long clientId,LocalDate processDate){
		
			
            // Get qualified order complete details			
		    List<BillingOrderData> products = this.billingOrderReadPlatformService.retrieveBillingOrderData(clientId, processDate,billingOrderData.getOrderId());
			
			List<BillingOrderCommand> billingOrderCommands = this.generateBillingOrderService.generatebillingOrder(products);

			// Invoice
			Invoice invoice = this.generateBillingOrderService.generateInvoice(billingOrderCommands);
			
			//Update Client Balance
			this.billingOrderWritePlatformService.updateClientBalance(invoice,clientId,false);

			// Update order-price
			 billingOrderWritePlatformService.updateBillingOrder(billingOrderCommands);
			 System.out.println("---------------------"+billingOrderCommands.get(0).getNextBillableDate());
			 
               if(invoice.getInvoiceAmount() == null){
            	   return null;
               }
		return new GenerateInvoiceData(clientId,billingOrderCommands.get(0).getNextBillableDate(),invoice.getInvoiceAmount());
	}
	
	public CommandProcessingResult createInvoiceBill(JsonCommand command) {
		try {
			// validation not written
			 this.apiJsonDeserializer.validateForCreate(command.json());
			LocalDate processDate = ProcessDate.fromJson(command);
			BigDecimal invoiceAmount=this.invoicingSingleClient(command.entityId(), processDate);
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withResourceIdAsString(invoiceAmount.toString()).build();

		} catch (DataIntegrityViolationException dve) {
			return CommandProcessingResult.empty();
		}

	}

}

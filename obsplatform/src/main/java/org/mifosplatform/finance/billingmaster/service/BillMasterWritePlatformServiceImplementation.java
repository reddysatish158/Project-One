package org.mifosplatform.finance.billingmaster.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.finance.adjustment.domain.Adjustment;
import org.mifosplatform.finance.adjustment.domain.AdjustmentRepository;
import org.mifosplatform.finance.billingmaster.domain.BillDetail;
import org.mifosplatform.finance.billingmaster.domain.BillDetailRepository;
import org.mifosplatform.finance.billingmaster.domain.BillMaster;
import org.mifosplatform.finance.billingmaster.domain.BillMasterRepository;
import org.mifosplatform.finance.billingmaster.serialize.BillMasterCommandFromApiJsonDeserializer;
import org.mifosplatform.finance.billingorder.domain.BillingOrder;
import org.mifosplatform.finance.billingorder.domain.BillingOrderRepository;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.domain.InvoiceRepository;
import org.mifosplatform.finance.billingorder.domain.InvoiceTax;
import org.mifosplatform.finance.billingorder.domain.InvoiceTaxRepository;
import org.mifosplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.mifosplatform.finance.financialtransaction.data.FinancialTransactionsData;
import org.mifosplatform.finance.payments.domain.Payment;
import org.mifosplatform.finance.payments.domain.PaymentRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.groupsDetails.domain.GroupsDetails;
import org.mifosplatform.organisation.groupsDetails.domain.GroupsDetailsRepository;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.domain.MessageDataRepository;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.service.service.ServiceMasterWritePlatformServiceImpl;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillMasterWritePlatformServiceImplementation implements
		BillMasterWritePlatformService {
	
	 private final static Logger logger = LoggerFactory.getLogger(ServiceMasterWritePlatformServiceImpl.class);
		private final PlatformSecurityContext context;
		private final BillMasterRepository billMasterRepository;
		private final BillMasterReadPlatformService billMasterReadPlatformService;
		private final BillWritePlatformService billWritePlatformService;
		private final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService;
	    private final BillMasterCommandFromApiJsonDeserializer  apiJsonDeserializer;
	    private final ClientRepository clientRepository;
	    private final GroupsDetailsRepository groupsDetailsRepository;
	    private final MessageDataRepository messageDataRepository;
	    private final BillingMessageTemplateRepository messageTemplateRepository;
		private final BillDetailRepository billDetailRepository;
		private final BillingOrderRepository billingOrderRepository;
		private final InvoiceTaxRepository invoiceTaxRepository;
		private final InvoiceRepository invoiceRepository;
		private final PaymentRepository paymentRepository;
		private final AdjustmentRepository adjustmentRepository;
		
	    
	   
	@Autowired
	 public BillMasterWritePlatformServiceImplementation(final PlatformSecurityContext context,final BillMasterRepository billMasterRepository,
				final BillMasterReadPlatformService billMasterReadPlatformService,final BillWritePlatformService billWritePlatformService,
				final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService,
				final BillMasterCommandFromApiJsonDeserializer apiJsonDeserializer,final ClientRepository clientRepository,
				final GroupsDetailsRepository groupsDetailsRepository,final MessageDataRepository messageDataRepository,
				final BillingMessageTemplateRepository messageTemplateRepository,final BillDetailRepository billDetailRepository,
				final BillingOrderRepository billingOrderRepository,InvoiceTaxRepository invoiceTaxRepository,
		        final InvoiceRepository invoiceRepository, final PaymentRepository paymentRepository,
		        final AdjustmentRepository adjustmentRepository){
		    this.context = context;
			this.billMasterRepository = billMasterRepository;
			this.clientRepository=clientRepository;
			this.billMasterReadPlatformService=billMasterReadPlatformService;
			this.billWritePlatformService=billWritePlatformService;
			this.transactionHistoryWritePlatformService = transactionHistoryWritePlatformService;
			this.apiJsonDeserializer=apiJsonDeserializer;
			this.groupsDetailsRepository=groupsDetailsRepository;
			this.messageDataRepository=messageDataRepository;
			this.messageTemplateRepository=messageTemplateRepository;
			this.billDetailRepository=billDetailRepository;
			this.billingOrderRepository=billingOrderRepository;
			this.invoiceRepository=invoiceRepository;
			this.invoiceTaxRepository=invoiceTaxRepository;
			this.adjustmentRepository=adjustmentRepository;
			this.paymentRepository=paymentRepository;
			
			
			
	}
	
	
	@Transactional
	@Override
	public CommandProcessingResult createBillMaster(JsonCommand command,Long clientId) {
		try
		{
	     Long parentId=null;
	     List<FinancialTransactionsData> financialTransactionsDatas=new ArrayList<FinancialTransactionsData>();
		 this.apiJsonDeserializer.validateForCreate(command.json());
		 financialTransactionsDatas = billMasterReadPlatformService.retrieveFinancialData(clientId);
		 if (financialTransactionsDatas.size() == 0) {
			String msg = "no Bills to Generate";
			throw new BillingOrderNoRecordsFoundException(msg);
		}
		 Client client=this.clientRepository.findOne(clientId);
		 if(client.getParentId() != null){
		// GroupsDetails groupsDetails=this.groupsDetailsRepository.findOne(client.getGroupName());//findOneByGroupName(client.getGroupName());
		  parentId=client.getParentId();
		 }else{
			parentId=clientId;
		 }
		
		BigDecimal	previousBal = this.billMasterReadPlatformService.retrieveClientBalance(clientId);
		
		LocalDate billDate = new LocalDate();
		BigDecimal previousBalance = BigDecimal.ZERO;
		BigDecimal chargeAmount = BigDecimal.ZERO;
		BigDecimal adjustmentAmount = BigDecimal.ZERO;
		BigDecimal taxAmount = BigDecimal.ZERO;
		BigDecimal paidAmount = BigDecimal.ZERO;
		BigDecimal dueAmount = BigDecimal.ZERO;
		final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
		final String message = command.stringValueOfParameterNamed("message");
		BillMaster  billMaster = new BillMaster(clientId, clientId,billDate.toDate(), null, null, dueDate.toDate(),
		previousBalance, chargeAmount, adjustmentAmount, taxAmount,paidAmount, dueAmount, null,message,parentId);
		
		List<BillDetail> listOfBillingDetail = new ArrayList<BillDetail>();
		
		for (FinancialTransactionsData financialTransactionsData : financialTransactionsDatas) {
			
			BillDetail billDetail = new BillDetail(null,financialTransactionsData.getTransactionId(),
					financialTransactionsData.getTransDate().toDate(),	financialTransactionsData.getTransactionType(),
					financialTransactionsData.getDebitAmount(),financialTransactionsData.getPlanCode(),financialTransactionsData.getDescription());
		
			listOfBillingDetail.add(billDetail);
		    billMaster.addBillDetails(billDetail);
		
		}
	
		billMaster = this.billMasterRepository.saveAndFlush(billMaster);
	//	this.billWritePlatformService.ireportPdf(billMaster);
		//List<BillDetail> billDetail = billWritePlatformService.createBillDetail(financialTransactionsDatas, billMaster);
		
		billWritePlatformService.updateBillMaster(listOfBillingDetail, billMaster,previousBal);
		billWritePlatformService.updateBillId(financialTransactionsDatas,billMaster.getId());
		//BillDetailsData billDetails = this.billMasterReadPlatformService.retrievebillDetails(billMaster.getId());
		
		transactionHistoryWritePlatformService.saveTransactionHistory(billMaster.getClientId(), "Statement", billMaster.getBillDate(),"DueAmount:"+billMaster.getDueAmount(),
				"AmountPaid:"+billMaster.getPaidAmount(),"AdjustmentAmount:"+billMaster.getAdjustmentAmount(),"PromotionDescription:"+billMaster.getPromotionDescription(),"BillNumber:"+billMaster.getBillNumber(),"StatementID:"+billMaster.getId());
       // this.billWritePlatformService.generatePdf(billDetails,financialTransactionsDatas);
        return new CommandProcessingResult(billMaster.getId());
	}   catch (DataIntegrityViolationException dve) {
		logger.error(dve.getLocalizedMessage());
		 handleCodeDataIntegrityIssues(command, dve);
		return  CommandProcessingResult.empty();
	}
}

	private void handleCodeDataIntegrityIssues(JsonCommand command,
			DataIntegrityViolationException dve) {
		Throwable realCause = dve.getMostSpecificCause(); 
		if(realCause.getMessage().contains("plan_code"))
		throw new PlatformDataIntegrityException("error.msg.data.truncation.issue",
                "Data truncation: Data too long for column 'plan_code'");
		
	}


	@Override
	public Long sendBillDetailFilePath(BillMaster billMaster) {
		
		context.authenticatedUser();
		Client client=this.clientRepository.findOne(billMaster.getClientId());
		String clientEmail=client.getEmail();
		if(clientEmail == null){
			String msg="Please provide email first";
			throw new BillingOrderNoRecordsFoundException(msg,client);
		}
		String filePath=billMaster.getFileName();
		BillingMessage billingMessage=null;
		List<BillingMessageTemplate> billingMessageTemplate=this.messageTemplateRepository.findAll();
		
		for(BillingMessageTemplate  msgTemplate:billingMessageTemplate){

			if(msgTemplate.getTemplateDescription().equalsIgnoreCase("Bill_EMAIL")){
		              
		    billingMessage=new BillingMessage(msgTemplate.getHeader(),msgTemplate.getBody(),msgTemplate.getFooter(),clientEmail,clientEmail,
		    		                    msgTemplate.getSubject(),"N",msgTemplate,msgTemplate.getMessageType(),filePath);
		   this.messageDataRepository.save(billingMessage);
			
	      }

		}
		
		return billMaster.getId();
	}


	@Override
	public CommandProcessingResult cancelBill(Long billId) {
		try{
		context.authenticatedUser();
		List<BillDetail> BillingDetails = new ArrayList<BillDetail>();
		
		BillMaster billMaster=this.billMasterRepository.findOne(billId);
		if(billMaster==null){
		 throw  new	BillingOrderNoRecordsFoundException();
		}//Get all billdetails for that billId 
		BillingDetails = billMaster.getBillDetails();//this.billDetailRepository.findOneByBillId(billMaster);
		for(BillDetail billDetail:BillingDetails){  
			if (billDetail.getTransactionType().equalsIgnoreCase("SERVICE_CHARGES")) {
				BillingOrder billingOrder = this.billingOrderRepository.findOne(billDetail.getTransactionId());
				billingOrder.updateBillId(null);
				this.billingOrderRepository.save(billingOrder);
				Invoice invoice = this.invoiceRepository.findOne(billingOrder.getInvoice().getId());
				invoice.updateBillId(null);
				this.invoiceRepository.save(invoice);
			} else if(billDetail.getTransactionType().equalsIgnoreCase("Taxes")){
				InvoiceTax tax = this.invoiceTaxRepository.findOne(billDetail.getTransactionId());
			    tax.updateBillId(null);
			    this.invoiceTaxRepository.save(tax);
		    }else if(billDetail.getTransactionType().equalsIgnoreCase("ADJUSTMENT")){
		    	Adjustment adjustment = this.adjustmentRepository.findOne(billDetail.getTransactionId());
				adjustment.updateBillId(null);
				this.adjustmentRepository.save(adjustment);
		   }else if(billDetail.getTransactionType().contains("PAYMENT")) {
				Payment payment = this.paymentRepository.findOne(billDetail.getTransactionId());
				payment.updateBillId(null);
				this.paymentRepository.save(payment);
			}else if (billDetail.getTransactionType().equalsIgnoreCase("ONETIME_CHARGES")) {
            	BillingOrder billingOrder = this.billingOrderRepository.findOne(billDetail.getTransactionId());
				billingOrder.updateBillId(null);
				this.billingOrderRepository.save(billingOrder);
				Invoice invoice = this.invoiceRepository.findOne(billingOrder.getInvoice().getId());
				invoice.updateBillId(null);
				this.invoiceRepository.save(invoice);
            }
			
		}
		 billMaster.delete();
        this.billMasterRepository.save(billMaster);
		return new CommandProcessingResult(billMaster.getId());
	   }catch(DataIntegrityViolationException dve) {
		logger.error(dve.getLocalizedMessage());
		 //handleCodeDataIntegrityIssues(command, dve);
		return  CommandProcessingResult.empty();
	}
   }
}	
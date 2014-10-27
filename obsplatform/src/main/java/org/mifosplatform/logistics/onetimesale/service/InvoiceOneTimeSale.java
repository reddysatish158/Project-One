package org.mifosplatform.logistics.onetimesale.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.billing.discountmaster.domain.DiscountMaster;
import org.mifosplatform.billing.discountmaster.domain.DiscountMasterRepository;
import org.mifosplatform.finance.adjustment.service.AdjustmentReadPlatformService;
import org.mifosplatform.finance.billingorder.commands.BillingOrderCommand;
import org.mifosplatform.finance.billingorder.data.BillingOrderData;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.service.BillingOrderReadPlatformService;
import org.mifosplatform.finance.billingorder.service.BillingOrderWritePlatformService;
import org.mifosplatform.finance.billingorder.service.GenerateBill;
import org.mifosplatform.finance.billingorder.service.GenerateBillingOrderService;
import org.mifosplatform.finance.clientbalance.data.ClientBalanceData;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.logistics.onetimesale.data.OneTimeSaleData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvoiceOneTimeSale {

	private final GenerateBill generateBill;
	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final BillingOrderWritePlatformService billingOrderWritePlatformService;
	private final GenerateBillingOrderService generateBillingOrderService;
	private final AdjustmentReadPlatformService adjustmentReadPlatformService;
	private final DiscountMasterRepository discountMasterRepository;
	@Autowired
	public InvoiceOneTimeSale(GenerateBill generateBill,BillingOrderReadPlatformService billingOrderReadPlatformService,
			BillingOrderWritePlatformService billingOrderWritePlatformService,GenerateBillingOrderService generateBillingOrderService,
			AdjustmentReadPlatformService adjustmentReadPlatformService,final DiscountMasterRepository discountMasterRepository) {
		this.generateBill = generateBill;
		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.billingOrderWritePlatformService = billingOrderWritePlatformService;
		this.generateBillingOrderService = generateBillingOrderService;
		this.adjustmentReadPlatformService = adjustmentReadPlatformService;
		this.discountMasterRepository=discountMasterRepository;
	}

	public void invoiceOneTimeSale(Long clientId, OneTimeSaleData oneTimeSaleData) {
		List<BillingOrderCommand> billingOrderCommands = new ArrayList<BillingOrderCommand>();

			// check whether one time sale is invoiced
			// N - not invoiced
			// y - invoiced
			if (oneTimeSaleData.getIsInvoiced().equalsIgnoreCase("N")) {
				BillingOrderData billingOrderData = new BillingOrderData(oneTimeSaleData.getId(),oneTimeSaleData.getClientId(),	new LocalDate().toDate(),
						oneTimeSaleData.getChargeCode(),oneTimeSaleData.getChargeType(),oneTimeSaleData.getTotalPrice(),oneTimeSaleData.getTaxInclusive());
				
				BigDecimal discountAmount = BigDecimal.ZERO; 
				DiscountMaster discountMaster=this.discountMasterRepository.findOne(oneTimeSaleData.getDiscountId());
				
				DiscountMasterData discountMasterData=new DiscountMasterData(discountMaster.getId(),discountMaster.getDiscountCode(),discountMaster.getDiscountDescription(),
						discountMaster.getDiscountType(),discountMaster.getDiscountRate(),null,null);
				
			    discountMasterData = this.calculateDiscount(discountMasterData, discountAmount, billingOrderData.getPrice());
			    
			    BillingOrderCommand   billingOrderCommand = this.generateBill.getOneTimeBill(billingOrderData, discountMasterData);
				/*this.createBillingOrderCommand(billingOrderData,new LocalDate(), new LocalDate(), new LocalDate(), new LocalDate(),
						billingOrderData.getPrice(), listOfTaxes, discountMasterData);
				
				BillingOrderCommand billingOrderCommand = new BillingOrderCommand(billingOrderData.getClientOrderId(),new Long(0),billingOrderData.getClientId(),
						new LocalDate().toDate(), null,new LocalDate().toDate(), null,billingOrderData.getChargeCode(),	billingOrderData.getChargeType(), null,
						billingOrderData.getDurationType(), null,billingOrderData.getPrice(), null, listOfTaxes,new LocalDate().toDate(), new LocalDate().toDate(),discountMasterData,billingOrderData.getTaxInclusive());
				       */
				        
			    billingOrderCommands.add(billingOrderCommand);
				//List<BillingOrder> listOfBillingOrders = billingOrderWritePlatformService.createBillingProduct(billingOrderCommands);
				// calculation of invoice
				Invoice invoice = this.generateBillingOrderService.generateInvoice(billingOrderCommands);

				// To fetch record from client_balance table
				List<ClientBalanceData> clientBalancesDatas = adjustmentReadPlatformService.retrieveAllAdjustments(clientId);
				
				this.billingOrderWritePlatformService.updateClientBalance(invoice,clientBalancesDatas);

			 } else {

			}
		}
	
	// Discount Applicable Logic
	public Boolean isDiscountApplicable(DiscountMasterData discountMasterData) {
		boolean isDiscountApplicable = true;
		
		return isDiscountApplicable;

	}

	// Discount End Date calculation if null
	@SuppressWarnings("deprecation")
	public Date getDiscountEndDateIfNull(DiscountMasterData discountMasterData) {
		LocalDate discountEndDate = discountMasterData.getDiscountEndDate();
		if (discountMasterData.getDiscountEndDate() == null) {
			discountEndDate = new LocalDate(2099, 0, 01);
		}
		return discountEndDate.toDate();

	}
	
	// if is percentage
	public boolean isDiscountPercentage(DiscountMasterData discountMasterData){
		boolean isDiscountPercentage = false;
		if(discountMasterData.getDiscountType().equalsIgnoreCase("percentage")){
																
			isDiscountPercentage = true;
		}
		return isDiscountPercentage;
	}
	
	// if is discount
	public boolean isDiscountFlat(DiscountMasterData discountMasterData){
		boolean isDiscountFlat = false;
		if(discountMasterData.getDiscountType().equalsIgnoreCase("flat")){
			
			isDiscountFlat = true;
		}
		return isDiscountFlat;
	}
	

	// Discount calculation 
	public DiscountMasterData calculateDiscount(DiscountMasterData discountMasterData,BigDecimal discountAmount,BigDecimal chargePrice){
		if(isDiscountPercentage(discountMasterData)){
			
			if(discountMasterData.getDiscountRate().compareTo(new BigDecimal(100)) ==-1 ||
			 discountMasterData.getDiscountRate().compareTo(new BigDecimal(100)) == 0){
				
			discountAmount = this.calculateDiscountPercentage(discountMasterData.getDiscountRate(), chargePrice);
			discountMasterData.setDiscountAmount(discountAmount);
			chargePrice = this.chargePriceNotLessThanZero(chargePrice, discountAmount);
			discountMasterData.setDiscountedChargeAmount(chargePrice);
			
			}
			
		}
		
		if(isDiscountFlat(discountMasterData)){
			
			BigDecimal netFlatAmount=this.calculateDiscountFlat(discountMasterData.getDiscountRate(), chargePrice);
			netFlatAmount=this.chargePriceNotLessThanZero(chargePrice, discountAmount);
			discountMasterData.setDiscountedChargeAmount(netFlatAmount);
			discountAmount = chargePrice.subtract(netFlatAmount);
			discountMasterData.setDiscountAmount(discountAmount);
			
		}
		return discountMasterData;
	
	}
	
	// Dicount Percent calculation
	public BigDecimal calculateDiscountPercentage(BigDecimal discountRate,BigDecimal chargePrice){
		
		return chargePrice.multiply(discountRate.divide(new BigDecimal(100)));
	}
	
	// Discount Flat calculation
	public BigDecimal calculateDiscountFlat(BigDecimal discountRate,BigDecimal chargePrice){
		
		BigDecimal calculateDiscountFlat=BigDecimal.ZERO;
		//check for chargeprice zero and discountrate greater than zero
		if(chargePrice.compareTo(BigDecimal.ZERO) == 1 ){
		    calculateDiscountFlat=chargePrice.subtract(discountRate).setScale(2,RoundingMode.HALF_UP);
		}
		return calculateDiscountFlat;
	}
	
	// to check price not less than zero
	public BigDecimal chargePriceNotLessThanZero(BigDecimal chargePrice,BigDecimal discountPrice){
		
		chargePrice = chargePrice.subtract(discountPrice);
		if(chargePrice.compareTo(discountPrice) < 0){
			chargePrice = BigDecimal.ZERO;
		}
		return chargePrice;
		
	}
	

}

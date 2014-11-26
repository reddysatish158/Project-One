
package org.mifosplatform.finance.payments.service;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.domain.InvoiceRepository;
import org.mifosplatform.finance.clientbalance.domain.ClientBalance;
import org.mifosplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.mifosplatform.finance.clientbalance.service.ClientBalanceReadPlatformService;
import org.mifosplatform.finance.clientbalance.service.UpdateClientBalance;
import org.mifosplatform.finance.payments.domain.ChequePayment;
import org.mifosplatform.finance.payments.domain.ChequePaymentRepository;
import org.mifosplatform.finance.payments.domain.Payment;
import org.mifosplatform.finance.payments.domain.PaymentRepository;
import org.mifosplatform.finance.payments.domain.PaypalEnquirey;
import org.mifosplatform.finance.payments.domain.PaypalEnquireyRepository;
import org.mifosplatform.finance.payments.exception.PaymentDetailsNotFoundException;
import org.mifosplatform.finance.payments.exception.ReceiptNoDuplicateException;
import org.mifosplatform.finance.payments.serialization.PaymentCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;

@Service
public class PaymentWritePlatformServiceImpl implements PaymentWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(PaymentWritePlatformServiceImpl.class);
    private final String Paypal_method="paypal";
    private final String CreditCard_method="credit_card";
    private final String CreditCard="creditCard";
    private final String CreditCardToken="creditCardToken";
	
	private final PlatformSecurityContext context;
	private final FromJsonHelper fromApiJsonHelper;
	private final PaymentRepository paymentRepository;
	private final InvoiceRepository invoiceRepository;
	private final UpdateClientBalance updateClientBalance;
	private final ClientBalanceRepository clientBalanceRepository;
	private final ChequePaymentRepository chequePaymentRepository;
	private final PaypalEnquireyRepository paypalEnquireyRepository;
	private final ConfigurationRepository globalConfigurationRepository;
	private final PaymentCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService; 
	private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;

	@Autowired
	public PaymentWritePlatformServiceImpl(final PlatformSecurityContext context,final PaymentRepository paymentRepository,
			final PaymentCommandFromApiJsonDeserializer fromApiJsonDeserializer,final ClientBalanceRepository clientBalanceRepository,
			final ChequePaymentRepository chequePaymentRepository,final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
			final UpdateClientBalance updateClientBalance,final ActiondetailsWritePlatformService actiondetailsWritePlatformService,
			final InvoiceRepository invoiceRepository,final ConfigurationRepository globalConfigurationRepository,
			final PaypalEnquireyRepository paypalEnquireyRepository,final FromJsonHelper fromApiJsonHelper) {
		
		this.context = context;
		this.fromApiJsonHelper=fromApiJsonHelper;
		this.invoiceRepository=invoiceRepository;
		this.paymentRepository = paymentRepository;
		this.updateClientBalance= updateClientBalance;
		this.clientBalanceRepository=clientBalanceRepository;
		this.chequePaymentRepository=chequePaymentRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.paypalEnquireyRepository=paypalEnquireyRepository;
		this.globalConfigurationRepository=globalConfigurationRepository;
		this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
		this.actiondetailsWritePlatformService=actiondetailsWritePlatformService; 
		
	}

	@Transactional
	@Override
	public CommandProcessingResult createPayment(final JsonCommand command) {
		
		try {
			
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			Payment payment  = Payment.fromJson(command,command.entityId());
			this.paymentRepository.saveAndFlush(payment);
			
			if(command.stringValueOfParameterNamed("isChequeSelected").equalsIgnoreCase("yes")){
				final ChequePayment chequePayment = ChequePayment.fromJson(command);
				chequePayment.setPaymentId(payment.getId());
				chequePaymentRepository.save(chequePayment);
			}
			
			ClientBalance clientBalance=this.clientBalanceRepository.findByClientId(payment.getClientId());
			
			if(clientBalance != null){
				clientBalance.updateBalance("CREDIT",payment.getAmountPaid(),payment.isWalletPayment());
			
			}else if(clientBalance == null){
                    BigDecimal balance=BigDecimal.ZERO.subtract(payment.getAmountPaid());
				clientBalance =ClientBalance.create(payment.getClientId(),balance,payment.isWalletPayment());
			}
			this.clientBalanceRepository.saveAndFlush(clientBalance);
			
			//Update Invoice Amount
			if(payment.getInvoiceId() != null){
				
				final Invoice invoice=this.invoiceRepository.findOne(payment.getInvoiceId());
				invoice.updateAmount(payment.getAmountPaid());
				this.invoiceRepository.save(invoice);
				
			}
			//Add New Action 
			final List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_PAYMENT);
				if(actionDetaislDatas.size() != 0){
					this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,command.entityId(),payment.getId().toString(),null);
				}
				return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(payment.getId()).withClientId(command.entityId()).build();

			
			
		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
			}
		}
	
	@Override
	public CommandProcessingResult cancelPayment(final JsonCommand command,final Long paymentId) {
		
		try{
			this.fromApiJsonDeserializer.validateForCancel(command.json());
			final Payment payment=this.paymentRepository.findOne(paymentId);
			if(payment == null){
				throw new PaymentDetailsNotFoundException(paymentId.toString());
			}
			
			payment.cancelPayment(command);
			this.paymentRepository.save(payment);
			final ClientBalance clientBalance = clientBalanceRepository.findByClientId(payment.getClientId());
			clientBalance.setBalanceAmount(payment.getAmountPaid(),payment.isWalletPayment());
			this.clientBalanceRepository.save(clientBalance);
			
			return new CommandProcessingResult(paymentId,payment.getClientId());
			
			
		}catch(DataIntegrityViolationException exception){
			handleDataIntegrityIssues(command, exception);
			return CommandProcessingResult.empty();
		}
		
	}

	private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause(); 
		if(realCause.getMessage().contains("receipt_no")){
		          throw new ReceiptNoDuplicateException(command.stringValueOfParameterNamed("receiptNo"));
		}
		
		logger.error(dve.getMessage(), dve);
	}
	
	private void DataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve, final String paymentid) {
		final Throwable realCause = dve.getMostSpecificCause(); 
		if(realCause.getMessage().contains("payment_id")){
		 throw new PlatformDataIntegrityException("error.msg.payments.paypalEnquirey.duplicate.paymentId", "A code with paymentId '"
                  + paymentid + "' already exists", "displayName", paymentid);
	}
		
		logger.error(dve.getMessage(), dve);
	}

	@Transactional
	@Override
	public CommandProcessingResult paypalEnquirey(final JsonCommand command) {

		try{
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForpaypalEnquirey(command.json());
					
			final JSONObject obj=new JSONObject(command.json()).getJSONObject("response");
			final SimpleDateFormat pattern= new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");	
			
			final String state=obj.getString("state");
			final String paymentid=obj.getString("id");
			final String create_time=obj.getString("create_time");
			final Date date= pattern.parse(create_time);
			
			final PaypalEnquirey  paypalEnquirey = new PaypalEnquirey(command.entityId(),state,paymentid,date);			
			this.paypalEnquireyRepository.save(paypalEnquirey);
			
			final JsonObject paymentobject = new JsonObject();
			final Map<String, Object> changes = new HashMap<String, Object>();
			final ClientBalance clientBalance = clientBalanceRepository.findByClientId(paypalEnquirey.getClientId());
			
			final PaypalEnquirey paypalEnquireyUpdate = this.paypalEnquireyRepository.findOne(paypalEnquirey.getId());
			
			try{
				
				 sendingDataToPaypal(paypalEnquirey.getId());
				 
			}catch (PayPalRESTException e) {
				
				final PaypalEnquirey paypalexceptionupdate=this.paypalEnquireyRepository.findOne(paypalEnquirey.getId());
				paypalexceptionupdate.setDescription(e.getLocalizedMessage());
				paypalexceptionupdate.setStatus("Fail");
				this.paypalEnquireyRepository.save(paypalexceptionupdate);
				changes.put("paymentId", new Long(-1));
				changes.put("paymentStatus", "Fail");
				changes.put("totalBalance", clientBalance.getBalanceAmount());
				changes.put("paypalException", e.getMessage());
				return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(paypalEnquirey.getId()).with(changes).build();
			} 
			 
			final String remarks=paypalEnquireyUpdate.getPayerEmailId()!=null?paypalEnquireyUpdate.getPayerEmailId():" "+
			                          paypalEnquireyUpdate.getCardNumber()!=null?paypalEnquireyUpdate.getCardNumber():" ";
				
			    final String paymentdate = new SimpleDateFormat("dd MMMM yyyy").format(paypalEnquireyUpdate.getPaymentDate());
				
				paymentobject.addProperty("txn_id", paypalEnquireyUpdate.getPaymentId());
				paymentobject.addProperty("dateFormat", "dd MMMM yyyy");
				paymentobject.addProperty("locale", "en");
				paymentobject.addProperty("paymentDate", paymentdate);
				paymentobject.addProperty("amountPaid", paypalEnquireyUpdate.getTotalAmount());
				paymentobject.addProperty("isChequeSelected", "no");
				paymentobject.addProperty("receiptNo", paypalEnquireyUpdate.getPaymentId());
				paymentobject.addProperty("remarks", remarks);
				paymentobject.addProperty("paymentCode", 27);
				final String entityName = "PAYMENT";
				
				final JsonElement element1 = fromApiJsonHelper.parse(paymentobject.toString());
				JsonCommand comm = new JsonCommand(null,paymentobject.toString(), element1,fromApiJsonHelper, entityName, 
						paypalEnquirey.getClientId(),null, null, null, null, null, null, null, null,null,null);

				final CommandProcessingResult result = createPayment(comm);

				if (result.resourceId() != null) {
					final int i = new Long(0).compareTo(result.resourceId());
					if (i == -1) {
						paypalEnquireyUpdate.setStatus("Success");
						paypalEnquireyUpdate.setObsPaymentId(result.resourceId());
						changes.put("paymentId", result.resourceId());
						changes.put("paymentStatus", "Success");
						changes.put("totalBalance",clientBalance.getBalanceAmount());
						changes.put("paypalException", "");
						changes.put("Errordescription", "");

					} else {
						paypalEnquireyUpdate.setStatus("Fail");
						paypalEnquireyUpdate.setObsPaymentId(result.resourceId());
						changes.put("paymentId", result.resourceId());
						changes.put("paymentStatus", "Fail");
						changes.put("totalBalance",clientBalance.getBalanceAmount());
						changes.put("paypalException", "");
						changes.put("Errordescription", "Payment Error");
					}
				} else {
					paypalEnquireyUpdate.setStatus("Failure");
					changes.put("paymentId", "");
					changes.put("paymentStatus", "Failure");
					changes.put("totalBalance",clientBalance.getBalanceAmount());
					changes.put("paypalException", "");
					changes.put("Errordescription", "Payment Not Processed");
				}
				
				this.paypalEnquireyRepository.save(paypalEnquireyUpdate);
			
	        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(paypalEnquirey.getId()).with(changes).build();
	        
		} catch (ParseException e) {
			
			return new CommandProcessingResultBuilder().withResourceIdAsString(e.getMessage()).build();
			
		} catch (JSONException e) {
			
			return new CommandProcessingResultBuilder().withResourceIdAsString(e.getMessage()).build();
			
		} catch (IOException e) {
			
			return new CommandProcessingResultBuilder().withResourceIdAsString(e.getMessage()).build();
			
		} catch (DataIntegrityViolationException dve){
			
			try {
				final JSONObject obj=new JSONObject(command.json()).getJSONObject("response");
				final String paymentid=obj.getString("id");
				DataIntegrityIssues(command, dve, paymentid);
				return CommandProcessingResult.empty();
			} catch (JSONException e) {
				return CommandProcessingResult.empty();
			}
			
		}
		
		
	}

	private void sendingDataToPaypal(final Long paypalEnquireyId) throws PayPalRESTException, IOException, JSONException {
		
		   try {
			   final Properties prop = new Properties();
			   final InputStream is = this.getClass().getClassLoader().getResourceAsStream("sdk_config.properties");
				prop.load(is);
				final PaypalEnquirey paypalEnquirey = this.paypalEnquireyRepository.findOne(paypalEnquireyId);
				com.paypal.api.payments.Payment.initConfig(prop);
				final Configuration paypalGlobalData = this.globalConfigurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_IS_PAYPAL_CHECK);
				final JSONObject object = new JSONObject(paypalGlobalData.getValue());
				final String paypalClientId = object.getString("clientId");
				final String paypalsecretCode = object.getString("secretCode");
			
				final OAuthTokenCredential tokenCredential = new OAuthTokenCredential(paypalClientId, paypalsecretCode);
				final com.paypal.api.payments.Payment payment = com.paypal.api.payments.Payment
						.get(tokenCredential.getAccessToken().trim(),paypalEnquirey.getPaymentId());

				final String paymentMethod = payment.getPayer().getPaymentMethod();
				
				if (paymentMethod.equalsIgnoreCase(Paypal_method)) {

					final String emailId = payment.getPayer().getPayerInfo().getEmail();
					final String payerId = payment.getPayer().getPayerInfo().getPayerId();
					final String amount = payment.getTransactions().get(0).getAmount().getTotal();
					final String currency = payment.getTransactions().get(0).getAmount().getCurrency();
					final String description = payment.getTransactions().get(0).getDescription();
					final String paymentState = payment.getTransactions().get(0).getRelatedResources().get(0).getSale().getState();
					final BigDecimal totalAmount = new BigDecimal(amount);

					paypalEnquirey.fromPaypalEnquireyTransaction(emailId,payerId, totalAmount, currency, description, paymentState, paymentMethod);		
					 
				}else if(paymentMethod.equalsIgnoreCase(CreditCard_method)){	
					final JSONObject obj=new JSONObject(payment.getPayer().getFundingInstruments().get(0));
					
					if(obj.has(CreditCard)){
						final String cardNumber = payment.getPayer().getFundingInstruments().get(0).getCreditCard().getNumber();
						final String cardType = payment.getPayer().getFundingInstruments().get(0).getCreditCard().getType();
						final int cardExpiryMonth = payment.getPayer().getFundingInstruments().get(0).getCreditCard().getExpireMonth();
						final int cardExpiryYear = payment.getPayer().getFundingInstruments().get(0).getCreditCard().getExpireYear();
						final String amount = payment.getTransactions().get(0).getAmount().getTotal();
						final String currency = payment.getTransactions().get(0).getAmount().getCurrency();
						final String description = payment.getTransactions().get(0).getDescription();
						final String paymentState = payment.getTransactions().get(0).getRelatedResources().get(0).getSale().getState();
						final BigDecimal totalAmount = new BigDecimal(amount);
						final String cardExpiryDate=Integer.toString(cardExpiryMonth)+"/"+Integer.toString(cardExpiryYear);

						paypalEnquirey.fromPaypalEnquireyTransaction(cardNumber, cardType, cardExpiryDate, totalAmount, currency, description,paymentState, paymentMethod);	
					
					}else if(obj.has(CreditCardToken)){
						
						final String cardNumber = payment.getPayer().getFundingInstruments().get(0).getCreditCardToken().getLast4();
						final String cardType = payment.getPayer().getFundingInstruments().get(0).getCreditCardToken().getType();
						final int cardExpiryMonth = payment.getPayer().getFundingInstruments().get(0).getCreditCardToken().getExpireMonth();
						final int cardExpiryYear = payment.getPayer().getFundingInstruments().get(0).getCreditCardToken().getExpireYear();
						final String amount = payment.getTransactions().get(0).getAmount().getTotal();
						final String currency = payment.getTransactions().get(0).getAmount().getCurrency();
						final String description = payment.getTransactions().get(0).getDescription();
						final String paymentState = payment.getTransactions().get(0).getRelatedResources().get(0).getSale().getState();
						final BigDecimal totalAmount = new BigDecimal(amount);
						final String cardExpiryDate=Integer.toString(cardExpiryMonth)+"/"+Integer.toString(cardExpiryYear);
						paypalEnquirey.fromPaypalEnquireyTransaction(cardNumber, cardType, cardExpiryDate, totalAmount, currency, description,paymentState, paymentMethod);	
					}else{
						return;
					}
					
				}
				
				this.paypalEnquireyRepository.save(paypalEnquirey);
				
				
			} catch (PayPalRESTException e) {	
				
				throw new PayPalRESTException(e.getMessage());		
				
			} catch (IOException e) {
				
				throw new IOException(e.getMessage());		
				
			} catch (JSONException e) {
				
				throw new JSONException(e.getMessage());	
			}
		
		
	}

}




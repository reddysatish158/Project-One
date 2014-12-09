package org.mifosplatform.finance.paymentsgateway.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.finance.payments.exception.ReceiptNoDuplicateException;
import org.mifosplatform.finance.payments.service.PaymentReadPlatformService;
import org.mifosplatform.finance.payments.service.PaymentWritePlatformService;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayConfiguration;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayConfigurationRepository;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.mifosplatform.finance.paymentsgateway.serialization.PaymentGatewayCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.configuration.exception.ConfigurationPropertyNotFoundException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.exception.EmailNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


@Service
public class PaymentGatewayWritePlatformServiceImpl implements PaymentGatewayWritePlatformService {

	
	    private final PlatformSecurityContext context;
	    private final PaymentGatewayRepository paymentGatewayRepository;
	    private final PaymentGatewayCommandFromApiJsonDeserializer paymentGatewayCommandFromApiJsonDeserializer;
	    private final FromJsonHelper fromApiJsonHelper;
	    private final PaymentGatewayReadPlatformService readPlatformService;
	    private final PaymentWritePlatformService paymentWritePlatformService;
	    private final PaymentReadPlatformService paymodeReadPlatformService;
	    private final PaymentGatewayReadPlatformService paymentGatewayReadPlatformService;
	    private final ConfigurationRepository configurationRepository;
	    private final PortfolioCommandSourceWritePlatformService writePlatformService;
	    private final PaymentGatewayConfigurationRepository paymentGatewayConfigurationRepository;
	    private final BillingMessageTemplateRepository billingMessageTemplateRepository;
		private final BillingMessageRepository messageDataRepository;
		private final ClientRepository clientRepository;
	   
	   
	    @Autowired
	    public PaymentGatewayWritePlatformServiceImpl(final PlatformSecurityContext context,
	    	    final PaymentGatewayRepository paymentGatewayRepository,final FromJsonHelper fromApiJsonHelper,
	    		final PaymentGatewayCommandFromApiJsonDeserializer paymentGatewayCommandFromApiJsonDeserializer,
	    		final PaymentGatewayReadPlatformService readPlatformService,
	    		final PaymentWritePlatformService paymentWritePlatformService,
	    		final PaymentReadPlatformService paymodeReadPlatformService,
	    		final PaymentGatewayReadPlatformService paymentGatewayReadPlatformService,
	    		final ConfigurationRepository configurationRepository,
	    		final PortfolioCommandSourceWritePlatformService writePlatformService,
	    		final PaymentGatewayConfigurationRepository paymentGatewayConfigurationRepository,
	    		final BillingMessageTemplateRepository billingMessageTemplateRepository,
	    		final BillingMessageRepository messageDataRepository,
	    		final ClientRepository clientRepository)
	    {
	    	this.context=context;
	    	this.paymentGatewayRepository=paymentGatewayRepository;
	    	this.fromApiJsonHelper=fromApiJsonHelper;
	    	this.paymentGatewayCommandFromApiJsonDeserializer=paymentGatewayCommandFromApiJsonDeserializer;
	    	this.readPlatformService=readPlatformService;
	    	this.paymentWritePlatformService=paymentWritePlatformService;
	    	this.paymodeReadPlatformService=paymodeReadPlatformService;
	    	this.paymentGatewayReadPlatformService=paymentGatewayReadPlatformService;
	    	this.configurationRepository = configurationRepository;
	    	this.writePlatformService = writePlatformService;
	    	this.paymentGatewayConfigurationRepository = paymentGatewayConfigurationRepository;
	    	this.billingMessageTemplateRepository = billingMessageTemplateRepository;
	    	this.messageDataRepository = messageDataRepository;
	    	this.clientRepository = clientRepository;
	    	
	    }
	    
	    private Long mPesaTransaction(JsonElement element) {

			try {
				CommandProcessingResult result = null;
				String serialNumberId = fromApiJsonHelper.extractStringNamed("reference", element);
				String paymentDate = fromApiJsonHelper.extractStringNamed("timestamp", element);
				BigDecimal amountPaid = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
				String phoneNo = fromApiJsonHelper.extractStringNamed("msisdn",element);
				String receiptNo = fromApiJsonHelper.extractStringNamed("receipt",element);
				//String source = fromApiJsonHelper.extractStringNamed("service",element);
				String details = fromApiJsonHelper.extractStringNamed("name",element);
				DateFormat readFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
				Date date;
				String source = ConfigurationConstants.PAYMENTGATEWAY_MPESA;

				date = readFormat.parse(paymentDate);

				PaymentGateway paymentGateway = new PaymentGateway(serialNumberId,phoneNo, date, amountPaid, receiptNo, source, details);

				Long clientId = this.readPlatformService.retrieveClientIdForProvisioning(serialNumberId);

				if (clientId != null && clientId>0) {
		
					Long paymodeId = this.paymodeReadPlatformService.getOnlinePaymode();
					if (paymodeId == null) {
						paymodeId = Long.valueOf(83);
					}
					String remarks = "customerName: " + details + " ,PhoneNo:"+ phoneNo + " ,Biller account Name : " + source;
					SimpleDateFormat daformat = new SimpleDateFormat("dd MMMM yyyy");
					String paymentdate = daformat.format(date);
					JsonObject object = new JsonObject();
					object.addProperty("dateFormat", "dd MMMM yyyy");
					object.addProperty("locale", "en");
					object.addProperty("paymentDate", paymentdate);
					object.addProperty("amountPaid", amountPaid);
					object.addProperty("isChequeSelected", "no");
					object.addProperty("receiptNo", receiptNo);
					object.addProperty("remarks", remarks);
					object.addProperty("paymentCode", paymodeId);
					String entityName = "PAYMENT";
					final JsonElement element1 = fromApiJsonHelper.parse(object.toString());
					JsonCommand comm = new JsonCommand(null, object.toString(),element1, fromApiJsonHelper,entityName,
							                            clientId,null, null, null, null, null, null, null, null, null,null);
					
					result = this.paymentWritePlatformService.createPayment(comm);
					if (result.resourceId() != null) {
						paymentGateway.setObsId(result.resourceId());
						paymentGateway.setPaymentId(result.resourceId().toString());
						paymentGateway.setStatus("Success");
						paymentGateway.setAuto(false);
						this.paymentGatewayRepository.save(paymentGateway);
					}else{
						paymentGateway.setStatus("Failure");
						paymentGateway.setRemarks("Payment is Not Processed .");
						this.paymentGatewayRepository.save(paymentGateway);
					}
					return result.resourceId();
				} else {
					paymentGateway.setStatus("Failure");
					paymentGateway.setRemarks("Hardware with this " + serialNumberId + " not Found.");
					this.paymentGatewayRepository.save(paymentGateway);
					return null;
				}
			} catch (ParseException e) {
				 return Long.valueOf(-1);
			}

		}
	    
	    private Long tigoPesaTransaction(JsonElement element) {
	    	CommandProcessingResult result;
			
			String serialNumberId = fromApiJsonHelper.extractStringNamed("CUSTOMERREFERENCEID", element);
			String txnId = fromApiJsonHelper.extractStringNamed("TXNID", element);
			BigDecimal amountPaid = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("AMOUNT", element);
			String phoneNo = fromApiJsonHelper.extractStringNamed("MSISDN", element);
			String type = fromApiJsonHelper.extractStringNamed("TYPE", element);
			String tStatus = fromApiJsonHelper.extractStringNamed("STATUS", element);
			String details = fromApiJsonHelper.extractStringNamed("COMPANYNAME", element);		 
			Date date = new Date();		
			String source = ConfigurationConstants.PAYMENTGATEWAY_TIGO;

			PaymentGateway paymentGateway = new PaymentGateway(serialNumberId, txnId, amountPaid, phoneNo, type, tStatus, details, date, source);

			Long clientId = this.readPlatformService.retrieveClientIdForProvisioning(serialNumberId);

			if (clientId != null && clientId>0) {
				Long paymodeId = this.paymodeReadPlatformService.getOnlinePaymode();
				if (paymodeId == null) {
					paymodeId = Long.valueOf(83);
				}
				String remarks = "companyName: " + details + " ,PhoneNo:"+ phoneNo + " ,Biller account Name : " + source + 
						       " ,Type:"+ type + " ,Status:" + tStatus;
				
				SimpleDateFormat daformat = new SimpleDateFormat("dd MMMM yyyy");
				String paymentdate = daformat.format(date);
				JsonObject object = new JsonObject();
				object.addProperty("dateFormat", "dd MMMM yyyy");
				object.addProperty("locale", "en");
				object.addProperty("paymentDate", paymentdate);
				object.addProperty("amountPaid", amountPaid);
				object.addProperty("isChequeSelected", "no");
				object.addProperty("receiptNo", txnId);
				object.addProperty("remarks", remarks);
				object.addProperty("paymentCode", paymodeId);
				String entityName = "PAYMENT";
				final JsonElement element1 = fromApiJsonHelper.parse(object.toString());
				JsonCommand comm = new JsonCommand(null, object.toString(),element1, fromApiJsonHelper,entityName,
						                            clientId,null, null, null, null, null, null, null, null, null,null);
				
				result = this.paymentWritePlatformService.createPayment(comm);
				if (result.resourceId() != null) {
					paymentGateway.setObsId(result.resourceId());
					paymentGateway.setStatus("Success");
					paymentGateway.setAuto(false);
					this.paymentGatewayRepository.save(paymentGateway);
				}else{
					paymentGateway.setStatus("Failure");
					paymentGateway.setRemarks("Payment is Not Processed .");
					this.paymentGatewayRepository.save(paymentGateway);
				}
				return result.resourceId();
			} else {
				paymentGateway.setStatus("Failure");
				paymentGateway.setRemarks("Hardware with this " + serialNumberId + " not Found.");
				this.paymentGatewayRepository.save(paymentGateway);
				return null;
			}

		}

	    @Transactional
		@Override
		public CommandProcessingResult createPaymentGateway(JsonCommand command) {
			  JsonElement element;
			  Long resourceId = null ;
			  String obsPaymentType = null;
			  element= fromApiJsonHelper.parse(command.json());
			try {
				   context.authenticatedUser();
				   this.paymentGatewayCommandFromApiJsonDeserializer.validateForCreate(command.json());
				  

				   if(element!=null){  
					   obsPaymentType  = fromApiJsonHelper.extractStringNamed("OBSPAYMENTTYPE", element);
					   if(obsPaymentType.equalsIgnoreCase("MPesa")){
						   resourceId = this.mPesaTransaction(element);
					   }else if (obsPaymentType.equalsIgnoreCase("TigoPesa")) {
						   resourceId= this.tigoPesaTransaction(element);
					   }  
					   
				   }	 
				   return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(resourceId).build();
			}catch (DataIntegrityViolationException  e) {

	    	  if(e.toString().contains("receipt_no")){
		    	  final String receiptNo=fromApiJsonHelper.extractStringNamed("receipt", element);	    	     	 
		    	  throw new ReceiptNoDuplicateException(receiptNo);	    	  	    	  
	    	  }else{
	    		  return null;

	    	  }
		   }catch (ReceiptNoDuplicateException  e) {
				  
			   String receiptNo = null;		   
			   if(obsPaymentType.equalsIgnoreCase("MPesa")){		   
				   receiptNo =fromApiJsonHelper.extractStringNamed("receipt", element);	   
			   }else if (obsPaymentType.equalsIgnoreCase("TigoPesa")) {		 
				   receiptNo=fromApiJsonHelper.extractStringNamed("TXNID", element);	  
			   } 
		 
			   String receiptNO=this.paymentGatewayReadPlatformService.findReceiptNo(receiptNo);
		    	 
			   if(receiptNO!=null){ 
				   throw new ReceiptNoDuplicateException(receiptNo);	    	 
			   } else{		    	
				   return null; 
			   }
			   
		   } catch (Exception dve) {	    
			   handleCodeDataIntegrityIssues(command, dve);	
			   return new CommandProcessingResult(Long.valueOf(-1));
	        }		
			
		}

		private void handleCodeDataIntegrityIssues(JsonCommand command,Exception dve) {
			String realCause=dve.toString();
			  final String receiptNo=command.stringValueOfParameterNamed("receipt");//fromApiJsonHelper.extractStringNamed("receipt", command);
		        if (realCause.contains("reference")) {
		        	
		            final String name =command.stringValueOfParameterNamed("reference");// fromApiJsonHelper.extractStringNamed("reference", command);
		            throw new PlatformDataIntegrityException("error.msg.code.reference", "A reference with this value '" + name + "' does not exists");
		        }else if(realCause.contains("receiptNo")){
		        	
		        	throw new PlatformDataIntegrityException("error.msg.payments.duplicate.receiptNo", "A code with receiptNo'"
		                    + receiptNo + "'already exists", "displayName",receiptNo);
		        	
		        }
		        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
		                "Unknown data integrity issue with resource: " + realCause);
			
		}

		@Override
		public CommandProcessingResult updatePaymentGateway(JsonCommand command) {
			
			this.context.authenticatedUser();
			this.paymentGatewayCommandFromApiJsonDeserializer.validateForUpdate(command.json());
			PaymentGateway gateway=this.paymentGatewayRepository.findOne(command.entityId());
			final Map<String, Object> changes =gateway.fromJson(command);
			this.paymentGatewayRepository.save(gateway);	   
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(gateway.getId()).with(changes).build();
		}
		
		//For Globalpay
		private String globalPayProcessing(String MerchantTxnRef, String pgConfig) throws JSONException, IOException {

			JSONObject pgConfigJsonObj = new JSONObject(pgConfig);
			String merchantId = pgConfigJsonObj.getString("merchantId");
			String userName = pgConfigJsonObj.getString("userName");
			String password = pgConfigJsonObj.getString("password");

			String data = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
					+ "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
					+ "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
					+ "<soap12:Body>"
					+ "<getTransactions xmlns=\"https://www.eazypaynigeria.com/globalpay_demo/\">"
					+ "<merch_txnref>" + MerchantTxnRef + "</merch_txnref>"
					+ "<channel></channel>" + "<merchantID>" + merchantId
					+ "</merchantID>" + "<start_date></start_date>"
					+ "<end_date></end_date>" + "<uid>" + userName + "</uid>"
					+ "<pwd>" + password + "</pwd>"
					+ "<payment_status></payment_status>" + "</getTransactions>"
					+ "</soap12:Body>" + "</soap12:Envelope>";

			URL oURL = new URL("https://demo.globalpay.com.ng/GlobalpayWebService_demo/service.asmx");
			HttpURLConnection soapConnection = (HttpURLConnection) oURL.openConnection();

			System.out.println("connect to server...");
			
			// Send SOAP Message to SOAP Server
			soapConnection.setRequestMethod("POST");
			soapConnection.setRequestProperty("Host", "demo.globalpay.com.ng");
			soapConnection.setRequestProperty("Content-Length", String.valueOf(data.toString().length()));
			soapConnection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
			soapConnection.setRequestProperty("SoapAction", "");
			soapConnection.setDoOutput(true);

			OutputStream reqStream = soapConnection.getOutputStream();
			reqStream.write(data.toString().getBytes());
			StringBuilder responseSB = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(soapConnection.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				responseSB.append(line);
			}

			responseSB.append(line);
			String responseSB1 = responseSB.toString().replaceAll("&lt;", "<");
			responseSB1 = responseSB1.replaceAll("&gt;", ">");

			JSONObject xmlJSONObj = XML.toJSONObject(responseSB1);

			JSONObject resultset = xmlJSONObj.getJSONObject("soap:Envelope")
					.getJSONObject("soap:Body")
					.getJSONObject("getTransactionsResponse")
					.getJSONObject("getTransactionsResult")
					.getJSONObject("resultset").getJSONObject("record");

			String paymentDesc = resultset.getString("payment_status_description");
			System.out.println("paymentDesc From Globalpay: "+ paymentDesc);
			Double amount = resultset.getDouble("amount");

			String paymentDate = resultset.getString("payment_date");
			Long txnref = resultset.getLong("txnref");
			String channel = resultset.getString("channel");
			String paymentStatus = resultset.getString("payment_status");

			JSONArray fieldArray = resultset.getJSONObject("field_values").getJSONObject("field_values").getJSONArray("field");
			String currency = fieldArray.getJSONObject(2).getString("currency");
			String emailAddress = fieldArray.getJSONObject(3).getString("email_address");
			String globalpayMerchanttxnref=null;
			
			if(fieldArray.getJSONObject(5).has("merch_txnref")){
				globalpayMerchanttxnref = fieldArray.getJSONObject(5).getString("merch_txnref");
			}else{
				globalpayMerchanttxnref = fieldArray.getJSONObject(5).getString("merchant_txnref");
			}
			/*else if(fieldArray.getJSONObject(5).has("merchant_txnref")){
				globalpayMerchanttxnref = fieldArray.getJSONObject(5).getString("merchant_txnref");
			}*/
			

			JSONObject otherDataObject = new JSONObject();
			otherDataObject.put("currency", currency);
			otherDataObject.put("paymentStatus", paymentStatus);
			otherDataObject.put("channel", channel);
			otherDataObject.put("paymentDate", paymentDate);
			otherDataObject.put("paymentDesc", paymentDesc);
			otherDataObject.put("globalpayMerchanttxnref", globalpayMerchanttxnref);

			String[] clientIdString = globalpayMerchanttxnref.split("-");
			String status = "SUCCESSFUL";

			if (!MerchantTxnRef.equals(globalpayMerchanttxnref)) {
				status = "FAILURE";
			}

			pgConfigJsonObj.put("clientId", clientIdString[0]);
			pgConfigJsonObj.put("emailId", emailAddress);
			pgConfigJsonObj.put("transactionId", txnref);
			pgConfigJsonObj.put("total_amount", String.valueOf(amount));
			pgConfigJsonObj.put("source", ConfigurationConstants.GLOBALPAY_PAYMENTGATEWAY);
			pgConfigJsonObj.put("otherData", otherDataObject);
			pgConfigJsonObj.put("device", "");
			pgConfigJsonObj.put("status", status);
			pgConfigJsonObj.put("currency", currency);
			
			return pgConfigJsonObj.toString();
		}
		
		private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
			final Throwable realCause = dve.getMostSpecificCause(); 
			if(realCause.getMessage().contains("receipt_no")){
			          throw new ReceiptNoDuplicateException(command.stringValueOfParameterNamed("transactionId"));
			}
		}

		@Override
		public CommandProcessingResult onlinePaymentGateway(JsonCommand command) {

		try {
			context.authenticatedUser();
			this.paymentGatewayCommandFromApiJsonDeserializer.validateForOnlinePayment(command.json());
			String commandJson = null;
			final String source = command.stringValueOfParameterNamed("source");
			final String transactionId = command.stringValueOfParameterNamed("transactionId");

			if (source.equalsIgnoreCase(ConfigurationConstants.GLOBALPAY_PAYMENTGATEWAY)) {

				PaymentGatewayConfiguration pgConfig = this.paymentGatewayConfigurationRepository.findOneByName(ConfigurationConstants.GLOBALPAY_PAYMENTGATEWAY);

				if (pgConfig != null && pgConfig.getValue() != null) {

					commandJson = globalPayProcessing(transactionId, pgConfig.getValue());

					if (commandJson == null) {
						return null;
					}
				}

			} else {
				commandJson = command.json();
			}

			return processOnlinePayment(commandJson);

		} catch (DataIntegrityViolationException dve) {
			handleDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
			
		} catch (JSONException e) {
			return new CommandProcessingResult(Long.valueOf(-1));
			
		} catch (IOException e) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	private CommandProcessingResult processOnlinePayment(String jsonData) throws JSONException {

		String deviceId = "";
		Map<String, Object> withChanges = new HashMap<String, Object>();

		final JSONObject json = new JSONObject(jsonData);
		final String currency = json.getString("currency");
		final Long clientId = json.getLong("clientId");
		final String txnId = json.getString("transactionId");
		final String amount = json.getString("total_amount");
		final String source = json.getString("source");
		final String data = json.get("otherData").toString();
		deviceId = json.getString("device");
		final BigDecimal totalAmount = new BigDecimal(amount);
		
		Date date = new Date();

		PaymentGateway paymentGateway = new PaymentGateway(deviceId, " ", date, totalAmount, txnId, source, data);
		this.paymentGatewayRepository.save(paymentGateway);
		
		withChanges.put("clientId", clientId);
		withChanges.put("txnId", txnId);
		withChanges.put("amount", amount);
		withChanges.put("pgId", paymentGateway.getId());
		withChanges.put("currency", currency);
		
		
		return new CommandProcessingResultBuilder().with(withChanges).build();

	}
	
	@Override
	public String payment(Long clientId, Long id, String txnId, String amount) throws JSONException{
		
		JSONObject withChanges = new JSONObject();
		
		try {
			PaymentGateway paymentGateway = this.paymentGatewayRepository.findOne(id);
			
			Configuration configuration = configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_ONLINEPAYMODE);

			if (configuration == null || configuration.getValue() == null || configuration.getValue() == "") {
				throw new ConfigurationPropertyNotFoundException(ConfigurationConstants.CONFIG_PROPERTY_ONLINEPAYMODE);
			}

			Long value = Long.parseLong(configuration.getValue());
			final BigDecimal totalAmount = new BigDecimal(amount);
			
			final String formattedDate = new SimpleDateFormat("dd MMMM yyyy").format(new Date());
			final JsonObject object = new JsonObject();
			object.addProperty("txn_id", txnId);
			object.addProperty("dateFormat", "dd MMMM yyyy");
			object.addProperty("locale", "en");
			object.addProperty("paymentDate", formattedDate);
			object.addProperty("amountPaid", totalAmount);
			object.addProperty("isChequeSelected", "no");
			object.addProperty("receiptNo", txnId);
			object.addProperty("remarks", "Payment Done");
			object.addProperty("paymentCode", value);
			
			final CommandWrapper commandRequest = new CommandWrapperBuilder().createPayment(clientId).withJson(object.toString()).build();
			CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);	

			if (result !=null && result.resourceId() != Long.valueOf(-1)) {
				paymentGateway.setObsId(result.getClientId());
				paymentGateway.setPaymentId(result.resourceId().toString());
				paymentGateway.setStatus("Success");
				paymentGateway.setAuto(false);
				withChanges.put("Result", "SUCCESS");
				withChanges.put("Description", "Transaction Successfully Completed");
				withChanges.put("Amount", amount);
				withChanges.put("ObsPaymentId", result.resourceId().toString());
				withChanges.put("TransactionId", txnId);
				
			} else {
				paymentGateway.setStatus("Failure");
				paymentGateway.setRemarks("Payment is Not Processed..");
				
				withChanges.put("Result", "FAILURE");
				withChanges.put("Description", "Transaction Rejected");
				withChanges.put("Amount", amount);
				withChanges.put("ObsPaymentId", "");
				withChanges.put("TransactionId", txnId);
			}
			
			this.paymentGatewayRepository.save(paymentGateway);
			return withChanges.toString();
		} catch (ReceiptNoDuplicateException e) {
			
			PaymentGateway paymentGateway = this.paymentGatewayRepository.findOne(id);
			paymentGateway.setStatus("Failure");
			paymentGateway.setRemarks("Transaction Already Exist with This Id:" + txnId + " in Payments");
			
			withChanges.put("Result", "FAILURE");
			withChanges.put("Description", "Transaction Already Exist with This Id : " + txnId);
			withChanges.put("Amount", amount);
			withChanges.put("ObsPaymentId", "");
			withChanges.put("TransactionId", txnId);
			this.paymentGatewayRepository.save(paymentGateway);
			return withChanges.toString();
		}
	}
	
	@Override
	public void emailSending(Long clientId, String Result, String Description, String orderId, String amount){
		
		Client client = this.clientRepository.findOne(clientId);
		if(client == null){
			throw new ClientNotFoundException(clientId);
		}
		
		if(client.getEmail() == null || client.getEmail().isEmpty()){
			throw new EmailNotFoundException(clientId);
		}
		BillingMessageTemplate messageDetails = this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_PAYMENT_RECEIPT);
		
		String subject=messageDetails.getSubject();
		String body=messageDetails.getBody();
		String header=messageDetails.getHeader();
		String footer=messageDetails.getFooter();
		
		header = header.replace("<PARAM1>", (client.getDisplayName()==null) || (client.getDisplayName()=="") ?client.getFirstname()+client.getLastname():client.getDisplayName());
		body = body.replace("<PARAM2>", Result);
		body = body.replace("<PARAM3>", Description);
		body = body.replace("<PARAM4>", amount);
		body = body.replace("<PARAM5>", orderId);
		
		
		BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, client.getEmail(),
				subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, messageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
		
		this.messageDataRepository.save(billingMessage);
	}
	
}

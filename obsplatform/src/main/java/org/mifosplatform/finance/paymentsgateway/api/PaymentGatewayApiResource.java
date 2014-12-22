
package org.mifosplatform.finance.paymentsgateway.api;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.finance.payments.exception.ReceiptNoDuplicateException;
import org.mifosplatform.finance.paymentsgateway.data.PaymentGatewayData;
import org.mifosplatform.finance.paymentsgateway.data.PaymentGatewayDownloadData;
import org.mifosplatform.finance.paymentsgateway.service.PaymentGatewayReadPlatformService;
import org.mifosplatform.finance.paymentsgateway.service.PaymentGatewayWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.MediaEnumoptionData;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Path("/paymentgateways")
@Component
@Scope("singleton")

/**
 * The class <code>PaymentGatewayApiResource</code> is developed for
 * Third party PaymentGateway systems.
 * Using the below API to Communicate OBS with Adapters/Third-Party servers. 
 * 
 * @author ashokreddy
 *
 */
public class PaymentGatewayApiResource {
	
	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private static final Set<String> RESPONSEPARAMETERS = new HashSet<String>(
			Arrays.asList("id","paymentId", "serialNo", "paymentDate", "receiptNo","status","phoneNo","clientName","amountPaid","remarks"));
	
	private final String resourceNameForPermissions = "PAYMENTGATEWAY";

	private final PlatformSecurityContext context;
	private final PaymentGatewayReadPlatformService readPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<PaymentGatewayData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService writePlatformService;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private String returnMessage;
	private String success;
	private String errorDesc;
	private String contentData;
	private CommandProcessingResult result;
	private JSONObject jsonData;
	private Long errorCode;
	private final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService;

	@Autowired
	public PaymentGatewayApiResource(final PlatformSecurityContext context,final PaymentGatewayReadPlatformService readPlatformService,
			final DefaultToApiJsonSerializer<PaymentGatewayData> toApiJsonSerializer,final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService writePlatformService,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
    		final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService) {

		this.toApiJsonSerializer = toApiJsonSerializer;
		this.writePlatformService = writePlatformService;
		this.context=context;
		this.readPlatformService=readPlatformService;
		this.apiRequestParameterHelper=apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    	this.paymentGatewayWritePlatformService = paymentGatewayWritePlatformService;
	}

	/**
	 * This method <code>onlinePayment</code> is 
	 * used for the Both M-pesa & Tigo-pesa PaymentGateways to Pay the Money.
	 * 
	 * @param requestData
	 * 			Containg input data in the Form of Xml/Soap . 
	 * @return
	 */
	@POST
	@Consumes({ MediaType.WILDCARD })
	@Produces({ MediaType.APPLICATION_XML })
	public String onlinePayment(final String requestData)  {
		
	     try{
			final JSONObject xmlJSONObj = XML.toJSONObject(requestData);
			jsonData=this.returnJsonFromXml(xmlJSONObj);
			final CommandWrapper commandRequest = new CommandWrapperBuilder().createPaymentGateway().withJson(jsonData.toString()).build();
			result = this.writePlatformService.logCommandSource(commandRequest);
			success = "SUCCESS";
			errorDesc = "";
			errorCode = Long.valueOf(0);	
			contentData = "OBSTRANSACTIONID=" + result.resourceId();	
			return this.returnToServer();	
		}catch(ReceiptNoDuplicateException e){
				success="DUPLICATE_TXN";
				errorDesc="DUPLICATE";
				errorCode=Long.valueOf(1);
				contentData="TXNID ALREADY EXIST";
				return this.returnToServer();
		} catch (JSONException e) {
			    return e.getCause().toString();	 
		} catch (PlatformDataIntegrityException e) {
		        return null;
	    }   
	}

	private String returnToServer() {
		
		try {
			final String obsPaymentType = jsonData.getString("OBSPAYMENTTYPE");
		
			
			if(obsPaymentType.equalsIgnoreCase("MPesa")){
				
					     String receipt=jsonData.getString("receipt");
						 StringBuilder builder = new StringBuilder();
				            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
				                .append("<response>")
				                .append("<receipt>"+receipt)
				                .append("</receipt>")
				                .append("<result>"+success)
				                .append("</result>")
				                .append("</response>");
				            returnMessage= builder.toString();
					
			}else if (obsPaymentType.equalsIgnoreCase("TigoPesa")) {
				
					//String TYPE = jsonData.getString("TYPE");
					String txnId = jsonData.getString("TXNID");
					String customerReferenceNumber = jsonData.getString("CUSTOMERREFERENCEID");	
					String msisdn = jsonData.getString("MSISDN");
					
						 StringBuilder builder = new StringBuilder();
				            builder.append("<?xml version=\"1.0\"?>")
				                .append("<!DOCTYPE COMMAND PUBLIC \"-//Ocam//DTD XML Command 1.0//EN\" \"xml/command.dtd\">")
				                .append("<COMMAND>")
				                .append("<TYPE>"+"SYNC_BILLPAY_RESPONSE")
				                .append("</TYPE>")
				                .append("<TXNID>"+txnId)
				                .append("</TXNID>")
				                .append("<REFID>"+customerReferenceNumber)
				                .append("</REFID>")
				                .append("<RESULT>"+success)
				                .append("</RESULT>")
				                .append("<ERRORCODE>"+errorCode)
				                .append("</ERRORCODE>")
				                .append("<ERRORDESC>"+errorDesc)
				                .append("</ERRORDESC>")
				                .append("<MSISDN>"+msisdn)
				                .append("</MSISDN>")
				                .append("<FLAG>"+"Y")
				                .append("</FLAG>")
				                .append("<CONTENT>"+contentData)
				                .append("</CONTENT>")
				                .append("</COMMAND>");
				            
				            returnMessage= builder.toString();			 
					
		}
		return returnMessage;
		} catch (JSONException e) {
			return e.getCause().toString();	 
		}
		
	}

	public JSONObject returnJsonFromXml(JSONObject xmlJSONObj){		
		try {
			JSONObject element=null;
			boolean b=xmlJSONObj.has("COMMAND");
			
			if(b==true){
			    element = xmlJSONObj.getJSONObject("COMMAND");
			    element.put("OBSPAYMENTTYPE", "TigoPesa");
			    element.put("locale", "en");
			}else{
				element = xmlJSONObj.getJSONObject("transaction");
				element.put("OBSPAYMENTTYPE", "MPesa");
				element.put("locale", "en");
			}
			return element;
		} catch (JSONException e) { 
			return null;
		}
		
	}
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDetailsForPayments(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch,@QueryParam("source") final String source,
			@QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset,@QueryParam("tabType") final String type) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final SearchSqlQuery searchItemDetails =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		Page<PaymentGatewayData> paymentData = readPlatformService.retrievePaymentGatewayData(searchItemDetails,type,source);
		return this.toApiJsonSerializer.serialize(paymentData);

	}
	
	@Path("download")
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response retriveDataForDownload(@Context final UriInfo uriInfo, @QueryParam("source") final String source, @QueryParam("status") final String status,
			@QueryParam("fromDate") final Long start, @QueryParam("toDate") final Long end) throws IOException {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		/**
		 * have to convert from and to date to format like 2014-06-15
		 * 
		 */
		
		Date fDate = new Date(start);
		Date tDate = new Date(end);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		String fromDate = df.format(fDate);
		String toDate = df.format(tDate);
		
		List<PaymentGatewayDownloadData> paymentData = readPlatformService.retriveDataForDownload(source,fromDate,toDate,status);
		
		/**
		 * 
		 * receiptNo serialNumber paymentDate amountPaid PhoneMSISDN Remarks  status 
		 */
		
		boolean statusSuccess = false;
		if(status.equalsIgnoreCase("Success"))
			statusSuccess = true;
		
		StringBuilder builder = new StringBuilder();
		if(statusSuccess){
			builder.append("Receipt No, Serial No, Payment Date, Amount Paid, Payment Id, Phone MSISDN, Remarks, Status \n");
		}else{
			builder.append("Receipt No, Serial No, Payment Date, Amount Paid, Phone MSISDN, Remarks, Status \n");
		}
		
		
		for(PaymentGatewayDownloadData data: paymentData){
			builder.append(data.getReceiptNo()+",");
			builder.append(data.getSerialNo()+",");
			builder.append(data.getPaymendDate()+",");
			builder.append(data.getAmountPaid()+",");
			if(statusSuccess){
				builder.append(data.getPaymentId()+",");
			}
			builder.append(data.getPhoneMSISDN()+",");
			builder.append(data.getRemarks()+",");
			builder.append(data.getStatus());
			builder.append("\n");
		}
		statusSuccess = false;
		String fileLocation = System.getProperty("java.io.tmpdir")+File.separator + "billing"+File.separator+""+source+""+System.currentTimeMillis()+status+".csv";
		
		String dirLocation = System.getProperty("java.io.tmpdir")+File.separator + "billing";
		File dir = new File(dirLocation);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		File file = new File(fileLocation);
		if(!file.exists()){
			file.createNewFile();
		}
		FileUtils.writeStringToFile(file, builder.toString());
		
        final ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.header("Content-Type", "application/csv");
        
        return response.build();
		
		/*String toJson = gson.toJson(paymentData);
		JSONArray arry  = null;
		try {
			arry = new JSONArray(toJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println(arry);
		String json = this.toApiJsonSerializer.serialize(paymentData);
		
		File file=new File("/home/rakesh/Desktop/demo.csv");
	    String csv = null;
		try {
			csv = CDL.toString(arry);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    FileUtils.writeStringToFile(file, csv);*/
	     
		//return this.toApiJsonSerializer.serialize(paymentData);

	}	
	
	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDetailsForPayments(@PathParam("id") final Long id,@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		PaymentGatewayData paymentData = readPlatformService.retrievePaymentGatewayIdData(id);
		List<MediaEnumoptionData> data=readPlatformService.retrieveTemplateData();
		paymentData.setStatusData(data);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, paymentData,RESPONSEPARAMETERS);

	}
	
	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateData(@PathParam("id") final Long id,final String apiRequestBodyAsJson) {
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaymentGateway(id).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		 return this.toApiJsonSerializer.serialize(result);

	}

	/**
	 * This method is used for Online Payment 
	 * Systems like Paypal,Dalpay,Korta etc...
	 * 
	 * Storing these payment details in 2 tables.
	 * 1) b_paymentgateway and 
	 * 2) b_payment.
	 */

	@PUT
	@Path("onlinepayment")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML})
	public String OnlinePaymentMethod(final String apiRequestBodyAsJson){
		
		try{
			
			final CommandWrapper commandRequest = new CommandWrapperBuilder().OnlinePaymentGateway().withJson(apiRequestBodyAsJson).build();
			final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
			
			Map<String, Object> output = result.getChanges();
			
			String client = String.valueOf(output.get("clientId"));
			String txnId = String.valueOf(output.get("txnId"));
			Long pgId = Long.valueOf(String.valueOf(output.get("pgId")));
			String amount = String.valueOf(output.get("amount"));
			String currency = String.valueOf(output.get("currency"));
			String status = String.valueOf(output.get("status"));
			Long clientId = Long.valueOf(client);
			String totalAmount =  amount + " " + currency;
			
			if(status.equalsIgnoreCase("SUCCESS")){
				
				if(currency.equalsIgnoreCase("ISK")){
					amount = amount.replace('.', ',');
				}
				
				String OutputData = this.paymentGatewayWritePlatformService.payment(clientId, pgId, txnId, amount);
				
				JSONObject object = new JSONObject(OutputData);
				
				this.paymentGatewayWritePlatformService.emailSending(clientId, object.getString("Result"), object.getString("Description"), txnId, totalAmount);
				
				return object.toString();
			
			}else{
				
				JSONObject withChanges = new JSONObject();
				if(status.equalsIgnoreCase("Pending")){
					status= "PENDING";
				}else{
					status= "FAILURE";
				}
				
				withChanges.put("Result", status);
				withChanges.put("Description", "Payment Not Successful with Globalpay, Payment Status is:"+status);
				withChanges.put("Amount", amount);
				withChanges.put("ObsPaymentId", "");
				withChanges.put("TransactionId", txnId);
				
				this.paymentGatewayWritePlatformService.emailSending(clientId, status, "Payment Not Successful with Globalpay, Payment Status is:"+status, 
						txnId, totalAmount);
				
				return withChanges.toString();	
			}
			
			
				
		}catch(JSONException e){
			
			return null;
		}		
	}
	
	/**
	 * This method is using for posting data to create payment using paypal
	 */
	 @POST
	 @Path("paypal")
	 @Consumes("application/x-www-form-urlencoded")
	 @Produces("text/html")
	 public String paypalOnlinePayment(@FormParam("txn_id") final String txnId,@FormParam("payment_date") final Date paymentDate,
			 @FormParam("mc_gross") final BigDecimal amount,
			 @FormParam("address_name") final String name,@FormParam("payer_email") final String payerEmail,
			 @FormParam("custom") final String customData, @FormParam("mc_currency") final String currency,
			 @FormParam("receiver_email") final String receiverEmail, @FormParam("payer_status") final String payerStatus){
		 //add "clientId" as supported parameter in create order validation. 
		try {
			 //in customData you should get the Parameters are clientId,locale,plancode,paytermcode,contractPeriod,returnUrl.
			
			final JSONObject jsonCustomData = new JSONObject(customData);
			
			final String dateFormat = "dd MMMM yyyy";

			final SimpleDateFormat daformat = new SimpleDateFormat(dateFormat);

			final String date = daformat.format(paymentDate);
			
			final String returnUrl = jsonCustomData.getString("returnUrl");
			
			final Long clientId = jsonCustomData.getLong("clientId");

			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("paymentDate", date);
			jsonObj.addProperty("payerEmail", payerEmail);
			jsonObj.addProperty("address_name", name);
			jsonObj.addProperty("receiverEmail", receiverEmail);
			jsonObj.addProperty("payerStatus", payerStatus);
			jsonObj.addProperty("currency", currency);

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("source", "paypal");
			jsonObject.put("transactionId", txnId);
			jsonObject.put("currency", currency);
			jsonObject.put("clientId", clientId);
			jsonObject.put("total_amount", amount);
			jsonObject.put("locale", "en");
			jsonObject.put("dateFormat", dateFormat);
			jsonObject.put("otherData", jsonObj.toString());
			
			String data = OnlinePaymentMethod(jsonObject.toString());
			
			JSONObject resultJsonObject = new JSONObject(data);
			
			String Result = resultJsonObject.getString("Result");
			
			if(Result.equalsIgnoreCase("SUCCESS")){
				/*{"billAlign":false,"isNewplan":true,"locale":"en",
					"dateFormat":"dd MMMM yyyy","start_date":"18 December 2014",
					"paytermCode":"Monthly","contractPeriod":5,"planCode":22}*/
				
				jsonCustomData.put("billAlign", false);
				jsonCustomData.put("isNewplan", true);
				jsonCustomData.put("dateFormat", dateFormat);
				jsonCustomData.put("start_date", date);
				jsonCustomData.remove("clientId");
				jsonCustomData.remove("returnUrl");
				
				
				CommandWrapper commandRequest = new CommandWrapperBuilder().createOrder(clientId).withJson(jsonCustomData.toString()).build();
				CommandProcessingResult resultOrder = this.writePlatformService.logCommandSource(commandRequest);

				if (resultOrder == null) {
					throw new PlatformDataIntegrityException("error.msg.client.order.creation",
							"Book Order Failed for ClientId:" + clientId, "Book Order Failed");
				}
			}
			
			String htmlData = "<a href=\""+returnUrl+"\"> Click On Me</a>";
			
			return htmlData;

		} 
	   catch(Exception e){
	    return e.getMessage();
	   }
	 }
}


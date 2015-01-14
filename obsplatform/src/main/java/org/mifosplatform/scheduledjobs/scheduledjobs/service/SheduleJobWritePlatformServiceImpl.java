package org.mifosplatform.scheduledjobs.scheduledjobs.service;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.crm.ticketmaster.api.TicketMasterApiResource;
import org.mifosplatform.crm.ticketmaster.service.TicketMasterReadPlatformService;
import org.mifosplatform.finance.billingmaster.api.BillingMasterApiResourse;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.mifosplatform.finance.billingorder.service.InvoiceClient;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.dataqueries.service.ReadReportingService;
import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.infrastructure.jobs.service.RadiusJobConstants;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.organisation.message.data.BillingMessageDataForProcessing;
import org.mifosplatform.organisation.message.service.BillingMessageDataWritePlatformService;
import org.mifosplatform.organisation.message.service.BillingMesssageReadPlatformService;
import org.mifosplatform.organisation.message.service.MessagePlatformEmailService;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.order.data.OrderData;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.provisioning.entitlements.data.ClientEntitlementData;
import org.mifosplatform.provisioning.entitlements.data.EntitlementsData;
import org.mifosplatform.provisioning.entitlements.service.EntitlementReadPlatformService;
import org.mifosplatform.provisioning.entitlements.service.EntitlementWritePlatformService;
import org.mifosplatform.provisioning.preparerequest.data.PrepareRequestData;
import org.mifosplatform.provisioning.preparerequest.service.PrepareRequestReadplatformService;
import org.mifosplatform.provisioning.processrequest.data.ProcessingDetailsData;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.processrequest.service.ProcessRequestReadplatformService;
import org.mifosplatform.provisioning.processrequest.service.ProcessRequestWriteplatformService;
import org.mifosplatform.provisioning.processscheduledjobs.service.SheduleJobReadPlatformService;
import org.mifosplatform.provisioning.processscheduledjobs.service.SheduleJobWritePlatformService;
import org.mifosplatform.scheduledjobs.scheduledjobs.data.EventActionData;
import org.mifosplatform.scheduledjobs.scheduledjobs.data.JobParameterData;
import org.mifosplatform.scheduledjobs.scheduledjobs.data.ScheduleJobData;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ProcessEventActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Service
public class SheduleJobWritePlatformServiceImpl implements SheduleJobWritePlatformService {



private final SheduleJobReadPlatformService sheduleJobReadPlatformService;
private final InvoiceClient invoiceClient;
private final BillingMasterApiResourse billingMasterApiResourse;
private final FromJsonHelper fromApiJsonHelper;
private final OrderReadPlatformService orderReadPlatformService;
private final BillingMessageDataWritePlatformService billingMessageDataWritePlatformService;
private final PrepareRequestReadplatformService prepareRequestReadplatformService;
private final ProcessRequestReadplatformService processRequestReadplatformService;
private final ProcessRequestWriteplatformService processRequestWriteplatformService;
private final ProcessRequestRepository processRequestRepository;
private final BillingMesssageReadPlatformService billingMesssageReadPlatformService;
private final MessagePlatformEmailService messagePlatformEmailService;
private final EntitlementReadPlatformService entitlementReadPlatformService;
private final EntitlementWritePlatformService entitlementWritePlatformService;
private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
private final ProcessEventActionService actiondetailsWritePlatformService;
private final ScheduleJob scheduleJob;
private final ReadReportingService readExtraDataAndReportingService;
private final ConfigurationRepository globalConfigurationRepository;
private final TicketMasterApiResource ticketMasterApiResource;
private final TicketMasterReadPlatformService ticketMasterReadPlatformService;
private final OrderRepository orderRepository;
private final MCodeReadPlatformService codeReadPlatformService;
private final JdbcTemplate jdbcTemplate;
private  String ReceiveMessage;

@Autowired
public SheduleJobWritePlatformServiceImpl(final InvoiceClient invoiceClient,final FromJsonHelper fromApiJsonHelper,
	   final BillingMasterApiResourse billingMasterApiResourse,final ProcessRequestRepository processRequestRepository,
	   final SheduleJobReadPlatformService sheduleJobReadPlatformService,final OrderReadPlatformService orderReadPlatformService,
	   final BillingMessageDataWritePlatformService billingMessageDataWritePlatformService,final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
	   final ProcessEventActionService actiondetailsWritePlatformService,final PrepareRequestReadplatformService prepareRequestReadplatformService,
	   final ProcessRequestReadplatformService processRequestReadplatformService,final ProcessRequestWriteplatformService processRequestWriteplatformService,
	   final BillingMesssageReadPlatformService billingMesssageReadPlatformService,final MessagePlatformEmailService messagePlatformEmailService,
	   final ScheduleJob scheduleJob,final EntitlementReadPlatformService entitlementReadPlatformService,
	   final EntitlementWritePlatformService entitlementWritePlatformService,final ReadReportingService readExtraDataAndReportingService,
	   final OrderRepository orderRepository,final ConfigurationRepository globalConfigurationRepository,final TicketMasterApiResource ticketMasterApiResource, 
	   final TicketMasterReadPlatformService ticketMasterReadPlatformService,final MCodeReadPlatformService codeReadPlatformService,
	   final TenantAwareRoutingDataSource dataSource) {

	this.sheduleJobReadPlatformService = sheduleJobReadPlatformService;
	this.invoiceClient = invoiceClient;
	this.billingMasterApiResourse = billingMasterApiResourse;
	this.fromApiJsonHelper = fromApiJsonHelper;
	this.orderReadPlatformService = orderReadPlatformService;
	this.billingMessageDataWritePlatformService = billingMessageDataWritePlatformService;
	this.prepareRequestReadplatformService = prepareRequestReadplatformService;
	this.processRequestReadplatformService = processRequestReadplatformService;
	this.processRequestWriteplatformService = processRequestWriteplatformService;
	this.processRequestRepository = processRequestRepository;
	this.billingMesssageReadPlatformService = billingMesssageReadPlatformService;
	this.messagePlatformEmailService = messagePlatformEmailService;
	this.entitlementReadPlatformService = entitlementReadPlatformService;
	this.entitlementWritePlatformService = entitlementWritePlatformService;
	this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
	this.actiondetailsWritePlatformService = actiondetailsWritePlatformService;
	this.scheduleJob = scheduleJob;
	this.orderRepository = orderRepository;
	this.readExtraDataAndReportingService = readExtraDataAndReportingService;
	this.globalConfigurationRepository = globalConfigurationRepository;
	this.ticketMasterApiResource = ticketMasterApiResource;
	this.ticketMasterReadPlatformService = ticketMasterReadPlatformService;
	this.codeReadPlatformService = codeReadPlatformService;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
	
}


@Override
@CronTarget(jobName = JobName.INVOICE)
public void processInvoice() {

try
	{
	JobParameterData data=this.sheduleJobReadPlatformService.getJobParameters(JobName.INVOICE.toString());
		if(data!=null){
			MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
			final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
			LocalTime date=new LocalTime(zone);
			String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
			String path=FileUtils.generateLogFileDirectory()+ JobName.INVOICE.toString() + File.separator +"Invoice_"+new LocalDate().toString().replace("-","")+
					"_"+dateTime+".log";
			File fileHandler = new File(path.trim());
			fileHandler.createNewFile();
			FileWriter fw = new FileWriter(fileHandler);
			FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
			List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());

			if(sheduleDatas.isEmpty()){
				fw.append("ScheduleJobData Empty \r\n");
			}
			for (ScheduleJobData scheduleJobData : sheduleDatas) {
				fw.append("ScheduleJobData id= "+scheduleJobData.getId()+" ,BatchName= "+scheduleJobData.getBatchName()+
						" ,query="+scheduleJobData.getQuery()+"\r\n");
				List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery());
				if(clientIds.isEmpty()){
					fw.append("Invoicing clients are not found \r\n");
				}
				else{
					fw.append("Invoicing the clients..... \r\n");
				}

// Get the Client Ids
				for (Long clientId : clientIds) {
					try {
						if(data.isDynamic().equalsIgnoreCase("Y")){
							Invoice  invoice=this.invoiceClient.invoicingSingleClient(clientId,new LocalDate());	
							fw.append("ClientId: "+clientId+"\tAmount: "+invoice.getInvoiceAmount().toString()+"\r\n");
						
						}else{
							Invoice invoice=this.invoiceClient.invoicingSingleClient(clientId,data.getProcessDate());
							fw.append("ClientId: "+clientId+"\tAmount: "+invoice.getInvoiceAmount().toString()+"\r\n");	
						}
					} catch (Exception dve) {
						handleCodeDataIntegrityIssues(null, dve);
					}
				}
			}
			fw.append("Invoices are Generated....."+ThreadLocalContextUtil.getTenant().getTenantIdentifier()+"\r\n");
			fw.flush();
			fw.close();
			System.out.println("Invoices are Generated....."+ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		}
	}catch(DataIntegrityViolationException exception){
		exception.printStackTrace();
	} catch (Exception exception) {	

		exception.printStackTrace();
	}
	}
	private void handleCodeDataIntegrityIssues(Object object, Exception dve) {
}

@Override
@CronTarget(jobName = JobName.REQUESTOR)
public void processRequest() {

	try {
		System.out.println("Processing Request Details.......");
		List<PrepareRequestData> data = this.prepareRequestReadplatformService.retrieveDataForProcessing();

			if(!data.isEmpty()){
			   MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
			   final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
			   LocalTime date=new LocalTime(zone);
	           String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
	           String path=FileUtils.generateLogFileDirectory()+JobName.REQUESTOR.toString()+ File.separator +"Requester_"+new LocalDate().toString().replace("-","")+"_"+dateTime+".log";
	         
	           File fileHandler = new File(path.trim());
	           fileHandler.createNewFile();
	           FileWriter fw = new FileWriter(fileHandler);
	           FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
	           fw.append("Processing Request Details....... \r\n");
           
	           		for (PrepareRequestData requestData : data) {

	           			fw.append("Prepare Request id="+requestData.getRequestId()+" ,clientId="+requestData.getClientId()+" ,orderId="
	           					+requestData.getOrderId()+" ,HardwareId="+requestData.getHardwareId()+" ,planName="+requestData.getPlanName()+
	           					" ,Provisiong system="+requestData.getProvisioningSystem()+"\r\n");
	           			
	           			this.prepareRequestReadplatformService.processingClientDetails(requestData);
	           		}
	           		
	           		fw.append(" Requestor Job is Completed...."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier()+"\r\n");
	           		fw.flush();
	           		fw.close();
			}
     
			System.out.println(" Requestor Job is Completed...."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier());
	}catch (Exception exception) {
		exception.printStackTrace();
    	}
	}



@Override
@CronTarget(jobName = JobName.SIMULATOR)
public void processSimulator() {
  try {
	System.out.println("Processing Simulator Details.......");
	JobParameterData data=this.sheduleJobReadPlatformService.getJobParameters(JobName.SIMULATOR.toString());
	if(data!=null){	
		List<ProcessingDetailsData> processingDetails = this.processRequestReadplatformService.retrieveUnProcessingDetails();
		if(data.getUpdateStatus().equalsIgnoreCase("Y")){ 
			if(!processingDetails.isEmpty()){
				MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
				final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
				LocalTime date=new LocalTime(zone);
				String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
				String path=FileUtils.generateLogFileDirectory()+JobName.SIMULATOR.toString()+ File.separator +"Simulator_"+new LocalDate().toString().
						replace("-","")+"_"+dateTime+".log";
				File fileHandler = new File(path.trim());
				fileHandler.createNewFile();
				FileWriter fw = new FileWriter(fileHandler);
				FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
				fw.append("Processing Simulator Details....... \r\n");
				
				for (ProcessingDetailsData detailsData : processingDetails) {
					fw.append("simulator Process Request id="+detailsData.getId()+" ,orderId="+detailsData.getOrderId()+" ,Provisiong System="
							+detailsData.getProvisionigSystem()+" ,RequestType="+detailsData.getRequestType()+"\r\n");
					ProcessRequest processRequest = this.processRequestRepository.findOne(detailsData.getId());
					processRequest.setProcessStatus('Y');
					this.processRequestRepository.saveAndFlush(processRequest);
					this.processRequestWriteplatformService.notifyProcessingDetails(processRequest,'Y');
				}
				fw.append("Simulator Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier()+" \r\n");
				fw.flush();
				fw.close();
			} 
		}
		if(data.getcreateTicket().equalsIgnoreCase("Y")){
			
			for (ProcessingDetailsData detailsData : processingDetails) {
				ProcessRequest processRequest = this.processRequestRepository.findOne(detailsData.getId());
				Order order=this.orderRepository.findOne(processRequest.getOrderId());
				Collection<MCodeData> problemsData = this.codeReadPlatformService.getCodeValue("Problem Code");
				List<EnumOptionData> priorityData = this.ticketMasterReadPlatformService.retrievePriorityData();
				Long userId=0L;
				JSONObject jsonobject = new JSONObject();
				DateTimeFormatter formatter1 = DateTimeFormat.forPattern("dd MMMM yyyy");
				DateTimeFormatter formatter2	=DateTimeFormat.fullTime();
				jsonobject.put("locale", "en");
				jsonobject.put("dateFormat", "dd MMMM yyyy");
				jsonobject.put("ticketTime"," "+new LocalTime().toString(formatter2));
				if(order != null){
				jsonobject.put("description","ClientId"+processRequest.getClientId()+" Order No:"+order.getOrderNo()+" Request Type:"+processRequest.getRequestType()
						+" Generated at:"+new LocalTime().toString(formatter2));
				}
							jsonobject.put("ticketDate",formatter1.print(new LocalDate()));
				jsonobject.put("sourceOfTicket","Phone");
				jsonobject.put("assignedTo", userId);
				jsonobject.put("priority",priorityData.get(0).getValue());
				jsonobject.put("problemCode", problemsData.iterator().next().getId());
				this.ticketMasterApiResource.createTicketMaster(processRequest.getClientId(), jsonobject.toString());
			}
		}
	}
		System.out.println("Simulator Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (DataIntegrityViolationException exception) {

	} catch (Exception exception) {
		System.out.println(exception.getMessage());
		exception.printStackTrace();
	}
}

@Override
@CronTarget(jobName = JobName.GENERATE_STATEMENT)
public void generateStatment() {

try {
	System.out.println("Processing statement Details.......");
	JobParameterData data=this.sheduleJobReadPlatformService.getJobParameters(JobName.GENERATE_STATEMENT.toString());
		
		if(data!=null){
			MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
			final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
			LocalTime date=new LocalTime(zone);
			String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
			String path=FileUtils.generateLogFileDirectory()+ JobName.GENERATE_STATEMENT.toString() + File.separator +"statement_"+new LocalDate().toString().replace("-","")+"_"+dateTime+".log";
			File fileHandler = new File(path.trim());
			fileHandler.createNewFile();
			FileWriter fw = new FileWriter(fileHandler);
			FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
			fw.append("Processing statement Details....... \r\n");
			List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());
       
			if(sheduleDatas.isEmpty()){
				fw.append("ScheduleJobData Empty \r\n");
			}
				for(ScheduleJobData scheduleJobData:sheduleDatas){
					
					fw.append("ScheduleJobData id= "+scheduleJobData.getId()+" ,BatchName= "+scheduleJobData.getBatchName()+
    			   " ,query="+scheduleJobData.getQuery()+"\r\n");
					List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery());

					if(clientIds.isEmpty()){
						fw.append("no records are available for statement generation \r\n");
					}else{
						fw.append("generate Statements for the clients..... \r\n");
					}
					for(Long clientId:clientIds){
						fw.append("processing clientId: "+clientId+ " \r\n");
						JSONObject jsonobject = new JSONObject();
						DateTimeFormatter formatter1 = DateTimeFormat.forPattern("dd MMMM yyyy");
						String formattedDate ;
							if(data.isDynamic().equalsIgnoreCase("Y")){
								formattedDate = formatter1.print(new LocalDate());	
							}else{
								formattedDate = formatter1.print(data.getDueDate());
							}
							jsonobject.put("dueDate",formattedDate);
							jsonobject.put("locale", "en");
							jsonobject.put("dateFormat", "dd MMMM YYYY");
							jsonobject.put("message", data.getPromotionalMessage());
							fw.append("sending jsonData for Statement Generation is: "+jsonobject.toString()+" . \r\n");
								try{
									this.billingMasterApiResourse.generateBillStatement(clientId,	jsonobject.toString()); 
								}catch(BillingOrderNoRecordsFoundException e){
									e.getMessage();
								}
					}
       }
				fw.append("statement Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier()+" . \r\n");
				fw.flush();
				fw.close();
		}
		System.out.println("statement Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier());
	} catch (Exception exception) {
		System.out.println(exception.getMessage());
		exception.printStackTrace();
		}	
		}

@Override
@CronTarget(jobName = JobName.MESSAGE_MERGE)
public void processingMessages()
{
try
{
JobParameterData data=this.sheduleJobReadPlatformService.getJobParameters(JobName.MESSAGE_MERGE.toString());
    
 if(data!=null){
         MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
         final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
         LocalTime date=new LocalTime(zone);
         String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
         String path=FileUtils.generateLogFileDirectory()+ JobName.MESSAGE_MERGE.toString() + File.separator +"Messanger_"+new LocalDate().toString().replace("-","")+"_"+dateTime+".log";
         File fileHandler = new File(path.trim());
         fileHandler.createNewFile();
         FileWriter fw = new FileWriter(fileHandler);
         FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
         fw.append("Processing the Messanger....... \r\n");
         List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobDetails(data.getBatchName());

         if(sheduleDatas.isEmpty()){
        	 fw.append("ScheduleJobData Empty \r\n");
         }
         
         for (ScheduleJobData scheduleJobData : sheduleDatas) {
        	 
        	 fw.append("ScheduleJobData id= "+scheduleJobData.getId()+" ,BatchName= "+scheduleJobData.getBatchName()+
        			 " ,query="+scheduleJobData.getQuery()+"\r\n");
        	 fw.append("Selected Message Template Name is :" +data.getMessageTemplate()+" \r\n");
        	 Long messageId = this.sheduleJobReadPlatformService.getMessageId(data.getMessageTemplate());
        	 fw.append("Selected Message Template id is :" +messageId+" \r\n");
        	 
        	 if(messageId!=null){
        		 fw.append("generating the message....... \r\n");
        		 this.billingMessageDataWritePlatformService.createMessageData(messageId,scheduleJobData.getQuery());
        		 fw.append("messages are generated successfully....... \r\n");

        	 }
         }	
         fw.append("Messanger Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier()+" . \r\n");
         fw.flush();
         fw.close();
}
          System.out.println("Messanger Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier()+" \r\n");
}
	catch (Exception dve)
	{
		System.out.println(dve.getMessage());
		handleCodeDataIntegrityIssues(null, dve);
	}
	}


@Override
@CronTarget(jobName = JobName.AUTO_EXIPIRY)
public void processingAutoExipryOrders() {	

try {

       System.out.println("Processing Auto Exipiry Details.......");
       JobParameterData data=this.sheduleJobReadPlatformService.getJobParameters(JobName.AUTO_EXIPIRY.toString());
        if(data!=null){
            MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
            final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
            LocalTime date=new LocalTime(zone);
            String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
            String path=FileUtils.generateLogFileDirectory()+ JobName.AUTO_EXIPIRY.toString() + File.separator +"AutoExipiry_"+new LocalDate().toString().replace("-","")+"_"+dateTime+".log";
            File fileHandler = new File(path.trim());
            fileHandler.createNewFile();
            FileWriter fw = new FileWriter(fileHandler);
            FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
            fw.append("Processing Auto Exipiry Details....... \r\n");
            List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());
            LocalDate exipirydate=null;
              if(sheduleDatas.isEmpty()){
            	  fw.append("ScheduleJobData Empty \r\n");
               	}
              if(data.isDynamic().equalsIgnoreCase("Y")){
            	  exipirydate=new LocalDate();
              }else{
               		exipirydate=data.getExipiryDate();
              }
              for (ScheduleJobData scheduleJobData : sheduleDatas){
                 fw.append("ScheduleJobData id= "+scheduleJobData.getId()+" ,BatchName= "+scheduleJobData.getBatchName()+
                    " ,query="+scheduleJobData.getQuery()+"\r\n");
                 List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery());
             
              if(clientIds.isEmpty()){
                fw.append("no records are available for Auto Expiry \r\n");
              }
              for(Long clientId:clientIds){
            	  
                fw.append("processing client id :"+clientId+"\r\n");
                List<OrderData> orderDatas = this.orderReadPlatformService.retrieveClientOrderDetails(clientId);
                	if(orderDatas.isEmpty()){
                		fw.append("No Orders are Found for :"+clientId+"\r\n");
                	}	
                	for (OrderData orderData : orderDatas){
                		this.scheduleJob.ProcessAutoExipiryDetails(orderData,fw,exipirydate,data,clientId);
                	}
              }
                fw.append("Auto Exipiry Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier()+" . \r\n");
                fw.flush();
                fw.close();
              }
              System.out.println("Auto Exipiry Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier());
        }
}catch(IOException exception){
       System.out.println(exception);
}catch (Exception dve) {
     System.out.println(dve.getMessage());
    handleCodeDataIntegrityIssues(null, dve);
}
}

@Override
@CronTarget(jobName = JobName.PUSH_NOTIFICATION)
public void processNotify() {

  try {
	  System.out.println("Processing Notify Details.......");
	  List<BillingMessageDataForProcessing> billingMessageDataForProcessings=this.billingMesssageReadPlatformService.retrieveMessageDataForProcessing();
	  
	  	if(!billingMessageDataForProcessings.isEmpty()){
	  		MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
	  		final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
	  		LocalTime date=new LocalTime(zone);
	  		String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
	  		String path=FileUtils.generateLogFileDirectory()+JobName.PUSH_NOTIFICATION.toString() + File.separator +"PushNotification_"+new LocalDate().toString().replace("-","")+"_"+dateTime+".log";
	  		File fileHandler = new File(path.trim());
	  		fileHandler.createNewFile();
	  		FileWriter fw = new FileWriter(fileHandler);
	  		FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
	  		fw.append("Processing Notify Details....... \r\n");
        
	  		for(BillingMessageDataForProcessing emailDetail : billingMessageDataForProcessings){
	  			fw.append("BillingMessageData id="+emailDetail.getId()+" ,MessageTo="+emailDetail.getMessageTo()+" ,MessageType="
	  					+emailDetail.getMessageType()+" ,MessageFrom="+emailDetail.getMessageFrom()+" ,Message="+emailDetail.getBody()+"\r\n");
	  			
	  			if(emailDetail.getMessageType()=='E'){
	  				String Result=this.messagePlatformEmailService.sendToUserEmail(emailDetail);
	  				fw.append("b_message_data processing id="+emailDetail.getId()+"-- and Result :"+Result+" ... \r\n");	
	  			}else if(emailDetail.getMessageType()=='M'){		
	  				String message = this.sheduleJobReadPlatformService.retrieveMessageData(emailDetail.getId());
	  				String Result=this.messagePlatformEmailService.sendToUserMobile(message,emailDetail.getId());	
	  				fw.append("b_message_data processing id="+emailDetail.getId()+"-- and Result:"+Result+" ... \r\n");	
	  			}else{
	  				fw.append("Message Type Unknown ..\r\n");
	  			}	
	  		}
	  		fw.append("Notify Job is Completed.... \r\n");
	  		fw.flush();
	  		fw.close();
	  	}else{
             System.out.println("push Notification data is empty...");
	  	}
	  	System.out.println("Notify Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier());
  	} catch (DataIntegrityViolationException exception) {
  	} catch (Exception exception) {
  		System.out.println(exception.getMessage());
  	}
}

	private static String processRadiusRequests(String url, String encodePassword, String data, FileWriter fw) throws IOException{
		
		HttpClient httpClient = new DefaultHttpClient();
		fw.append("data Sending to Server is: " + data + " \r\n");
		StringEntity se = new StringEntity(data.trim());
		fw.append("Request Send to :" + url + "\r\n");
		HttpPost postRequest = new HttpPost(url);
		postRequest.setHeader("Authorization", "Basic " + encodePassword);
		postRequest.setHeader("Content-Type", "application/json");
		postRequest.setEntity(se);
		HttpResponse response = httpClient.execute(postRequest);

		if (response.getStatusLine().getStatusCode() == 404) {
			System.out.println("ResourceNotFoundException : HTTP error code : "
							+ response.getStatusLine().getStatusCode());
			fw.append("ResourceNotFoundException : HTTP error code : " 		
					+ response.getStatusLine().getStatusCode() + ", Request url:" + url + "is not Found. \r\n");
			
			return "ResourceNotFoundException";

		} else if (response.getStatusLine().getStatusCode() == 401) {
			System.out.println(" Unauthorized Exception : HTTP error code : "
							+ response.getStatusLine().getStatusCode());
			fw.append(" Unauthorized Exception : HTTP error code : "
					+ response.getStatusLine().getStatusCode()
					+ " , The UserName or Password you entered is incorrect." + "\r\n");
			
			return "UnauthorizedException"; 

		} else if (response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + " \r\n");
		} else{
			fw.append("Request executed Successfully... \r\n");
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		String output,output1="";
		
		while ((output = br.readLine()) != null) {
			output1 = output1 + output;
		}
		
		System.out.println(output1);
		br.close();
		
		return output1;
		
	}
	
	@Override
	@CronTarget(jobName = JobName.RADIUS)
	public void processMiddleware() {

		try {
			System.out.println("Processing Radius Details.......");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());
			

			if (data != null) {
				MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
				final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
				LocalTime date = new LocalTime(zone);
				String dateTime = date.getHourOfDay() + "_" + date.getMinuteOfHour() + "_" + date.getSecondOfMinute();
				String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
				byte[] encoded = Base64.encodeBase64(credentials.getBytes());
				HttpClient httpClient = new DefaultHttpClient();
				List<EntitlementsData> entitlementDataForProcessings = this.entitlementReadPlatformService
						.getProcessingData(new Long(100), data.getProvSystem(), null);
				
				if (!entitlementDataForProcessings.isEmpty()) {
					
					String path = FileUtils.generateLogFileDirectory() + JobName.RADIUS.toString() + File.separator
							+ "radius_" + new LocalDate().toString().replace("-", "") + "_" + dateTime + ".log";
					
					File fileHandler = new File(path.trim());
					fileHandler.createNewFile();
					FileWriter fw = new FileWriter(fileHandler);
					FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
					
					fw.append("Processing Radius Details....... \r\n");
					fw.append("Radius Server Details.....\r\n");
					fw.append("UserName of Radius:" + data.getUsername() + " \r\n");
					fw.append("password of Radius: ************** \r\n");
					fw.append("url of Radius:" + data.getUrl() + " \r\n");
					
					for (EntitlementsData entitlementsData : entitlementDataForProcessings) {
						
						fw.append("EntitlementsData id=" + entitlementsData.getId() + " ,clientId="
								+ entitlementsData.getClientId() + " ,HardwareId=" + entitlementsData.getHardwareId()
								+ " ,RequestType=" + entitlementsData.getRequestType() + "\r\n");
						
						Long clientId = entitlementsData.getClientId();
					
					
						if(clientId == null || clientId==0){throw new ClientNotFoundException(clientId);}
						
						ReceiveMessage = "";
						ClientEntitlementData clientdata = this.entitlementReadPlatformService.getClientData(clientId);
						
						if(clientdata == null || clientdata.getSelfcareUsername() == null){
							
							String output = "Selfcare Not Created with this ClientId: " + clientId + " Properly.";
							fw.append(output + " \r\n");
							ReceiveMessage = RadiusJobConstants.FAILURE + output;
							
						} else if (entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.Client_Activation)) {
			
							try {
								JSONObject object = new JSONObject();	
								object.put("username", clientdata.getSelfcareUsername());					
								object.put("attribute", "Cleartext-Password");							
								object.put("op", ":=");							
								object.put("value", clientdata.getSelfcarePassword());

								String encodePassword = new String(encoded);
								String url = data.getUrl() + "radcheck";
								String output = processRadiusRequests(url, encodePassword, object.toString(), fw);
								
								if (output.equalsIgnoreCase("UnauthorizedException")
											|| output.equalsIgnoreCase("ResourceNotFoundException")) {
										
									return;	
								
								} else if (output.contains(RadiusJobConstants.RADCHECK_OUTPUT)) {	
									ReceiveMessage = "Success";
								
								} else {
									ReceiveMessage = RadiusJobConstants.FAILURE + output;
								}
								fw.append("Output from Server: " + output + " \r\n");
								
							} catch (JSONException e) {		
								
								fw.append("JSON Exeception throwing. StockTrace:" + e.getMessage() + " \r\n");	
								ReceiveMessage = RadiusJobConstants.FAILURE + e.getMessage();			
							}
							
						} else if (entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.Activation) || 
								entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.ReConnection) ||
								entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.RENEWAL_AE)) {

							try {
								JSONObject jsonObject = new JSONObject(entitlementsData.getProduct());
								String planIdentification = jsonObject.getString("planIdentification");
								
									
								if(planIdentification != null && !planIdentification.equalsIgnoreCase("")){
									
									JSONObject object = new JSONObject();
									object.put("username", clientdata.getSelfcareUsername());
									object.put("groupname", planIdentification);
									object.put("priority", Long.valueOf(1));
									
									String encodePassword = new String(encoded);
									String url = data.getUrl() + "raduser";
									
									String output = processRadiusRequests(url, encodePassword, object.toString(), fw);
									
									if(output.equalsIgnoreCase("UnauthorizedException") || output.equalsIgnoreCase("ResourceNotFoundException")){
										return;
									}else if(output.equalsIgnoreCase(RadiusJobConstants.RADUSER_CREATE_OUTPUT)){
										ReceiveMessage = "Success";
									}else{
										ReceiveMessage = RadiusJobConstants.FAILURE + output;
									}
									
									fw.append("Output from Server: " + output + " \r\n");

								}else{
										
									fw.append("Plan Identification should Not Mapped Properly, Plan Identification=" + planIdentification + " \r\n");
									ReceiveMessage = RadiusJobConstants.FAILURE + "Plan Identification " +
											" should Not Mapped Properly and Plan Identification=" + planIdentification;
								}
							
						
							} catch (JSONException e) {
								
								fw.append("JSON Exeception throwing. StockTrace:" + e.getMessage() + " \r\n");
								ReceiveMessage = RadiusJobConstants.FAILURE + e.getMessage();
							}
						

					
						} else if (entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.DisConnection)) {

							try {
								String userName = clientdata.getSelfcareUsername();
								String url = data.getUrl() + "raduser/" + userName;
								fw.append("Request Send to :" + url + "\r\n");
								
								HttpDelete deleteRequest = new HttpDelete(url);
								deleteRequest.setHeader("Authorization", "Basic " + new String(encoded));
								deleteRequest.setHeader("Content-Type", "application/json");
								
								HttpResponse response = httpClient.execute(deleteRequest);
								
								if (response.getStatusLine().getStatusCode() == 404) {
									System.out.println("ResourceNotFoundException : HTTP error code : "
													+ response.getStatusLine().getStatusCode());
									fw.append("ResourceNotFoundException : HTTP error code : " 		
											+ response.getStatusLine().getStatusCode() + ", Request url:" + url + "is not Found. \r\n");
									
									return;

								} else if (response.getStatusLine().getStatusCode() == 401) {
									System.out.println(" Unauthorized Exception : HTTP error code : "
													+ response.getStatusLine().getStatusCode());
									fw.append(" Unauthorized Exception : HTTP error code : "
											+ response.getStatusLine().getStatusCode()
											+ " , The UserName or Password you entered is incorrect." + "\r\n");
									
									return; 

								} else if (response.getStatusLine().getStatusCode() != 200) {
									System.out.println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
									fw.append("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + " \r\n");
								} else{
									fw.append("Request executed Successfully... \r\n");
								}
								
								BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
								String output,output1="";
								
								while ((output = br.readLine()) != null) {
									output1 = output1 + output;
								}
								
								System.out.println(output1);
								
								if (output1.trim().equalsIgnoreCase(RadiusJobConstants.RADUSER_DELETE_OUTPUT.trim())) {
									ReceiveMessage = "Success";
								} else {
									ReceiveMessage = RadiusJobConstants.FAILURE + output1;
								}

								fw.append("Output from Server: " + output1 + " \r\n");
								br.close();

							} catch (Exception e) {
								
								fw.append("Exeception throwing. StockTrace:" + e.getMessage() + " \r\n");
								ReceiveMessage = RadiusJobConstants.FAILURE + e.getMessage();
							}
						
						} else {
							
							try{
								fw.append("Request Type is:"+ entitlementsData.getRequestType());					
								fw.append("Unknown Request Type for Server (or) This Request Type is Not Handle in Radius Job");
								ReceiveMessage = RadiusJobConstants.FAILURE + "Unknown Request Type for Server";
								
							} catch (Exception e) {
								
								fw.append("Exeception throwing. StockTrace:" + e.getMessage() + " \r\n");
								ReceiveMessage = RadiusJobConstants.FAILURE + e.getMessage();
						
							}
						}
					
						// Updating the Response and status in b_process_request.
						JsonObject object = new JsonObject();
						object.addProperty("prdetailsId", entitlementsData.getPrdetailsId());
						object.addProperty("receivedStatus", new Long(1));
						object.addProperty("receiveMessage", ReceiveMessage);
						String entityName = "ENTITLEMENT";
						fw.append("sending json data to EntitlementApi is:" + object.toString() + "\r\n");
						
						final JsonElement element1 = fromApiJsonHelper.parse(object.toString());
						JsonCommand comm = new JsonCommand(null, object.toString(), element1, fromApiJsonHelper,
								entityName, entitlementsData.getId(), null, null, null, null, null, null, 
								null, null, null,null);
						CommandProcessingResult result = this.entitlementWritePlatformService.create(comm);
						fw.append("Result From the EntitlementApi is:" + result.resourceId() + " \r\n");
						
					}
				
					fw.append("Radius Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " /r/n");				
					fw.flush();
					fw.close();
							
				} else {
					System.out.println("Radius data is Empty...");
				}
				httpClient.getConnectionManager().shutdown();
				System.out.println("Radius Job is Completed...");
			}
			
		} catch (DataIntegrityViolationException exception) {
			System.out.println("catching the DataIntegrityViolationException, StockTrace: " + exception.getMessage());
	
		} catch (IOException e) {
			System.out.println("catching the IOException, StockTrace: " + e.getMessage());
	
		} catch (Exception exception) {
			System.out.println(exception.getMessage());
		}
	
	}

@Override
@CronTarget(jobName = JobName.EVENT_ACTION_PROCESSOR)
public void eventActionProcessor() {
	try {
		System.out.println("Processing Event Actions.....");
		MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
		final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
		LocalTime date=new LocalTime(zone);
		String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();

		//Retrieve Event Actions
		String path=FileUtils.generateLogFileDirectory()+ JobName.EVENT_ACTION_PROCESSOR.toString() + File.separator +"Activationprocess_"+new LocalDate().toString().replace("-","")+"_"+dateTime+".log";
		File fileHandler = new File(path.trim());
		fileHandler.createNewFile();
		FileWriter fw = new FileWriter(fileHandler);
		FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
		List<EventActionData> actionDatas=this.actionDetailsReadPlatformService.retrieveAllActionsForProccessing();
			
			for(EventActionData eventActionData:actionDatas){
				fw.append("Process Response id="+eventActionData.getId()+" ,orderId="+eventActionData.getOrderId()+" ,Provisiong System="+eventActionData.getActionName()+ " \r\n");
				System.out.println(eventActionData.getId());
				this.actiondetailsWritePlatformService.processEventActions(eventActionData);
			}
			System.out.println("Event Actions are Processed....");
			fw.append("Event Actions are Completed.... \r\n");
			fw.flush();
			fw.close();
		} catch (DataIntegrityViolationException e) {
			System.out.println(e.getMessage());
		}catch (Exception exception) {
			System.out.println(exception.getMessage());
		}
	}


@Override
@CronTarget(jobName = JobName.REPORT_EMAIL)
public void reportEmail() {

	System.out.println("Processing report email.....");
	try {
		JobParameterData data=this.sheduleJobReadPlatformService.getJobParameters(JobName.REPORT_EMAIL.toString());
          	if(data!=null){	
          		MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
          		final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
          		LocalTime date=new LocalTime(zone);
          		String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
          		String fileLocation=FileUtils.MIFOSX_BASE_DIR+ File.separator + JobName.REPORT_EMAIL.toString() + File.separator +"ReportEmail_"+new LocalDate().toString().replace("-","")+"_"+dateTime;
				//Retrieve Event Actions
				String path=FileUtils.generateLogFileDirectory()+ JobName.REPORT_EMAIL.toString() + File.separator +"ReportEmail_"+new LocalDate().toString().replace("-","")+"_"+dateTime+".log";
				File fileHandler = new File(path.trim());
				fileHandler.createNewFile();
				FileWriter fw = new FileWriter(fileHandler);
				FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
				List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobDetails(data.getBatchName());

				if(sheduleDatas.isEmpty()){
					fw.append("ScheduleJobData Empty with this Stretchy_report :" + data.getBatchName() + "\r\n");
				}
				for (ScheduleJobData scheduleJobData : sheduleDatas) {
					fw.append("Processing report email.....\r\n");
					fw.append("ScheduleJobData id= "+scheduleJobData.getId()+" ,BatchName= "+scheduleJobData.getBatchName()+
							" ,query="+scheduleJobData.getQuery()+"\r\n");
				     Map<String, String> reportParams = new HashMap<String, String>();
					 String pdfFileName = this.readExtraDataAndReportingService.generateEmailReport(scheduleJobData.getBatchName(), "report",reportParams,fileLocation);

					 if(pdfFileName!=null){
						 
						 fw.append("PDF file location is :" + pdfFileName +" \r\n");
						 fw.append("Sending the Email....... \r\n");
						 String result=this.messagePlatformEmailService.createEmail(pdfFileName,data.getEmailId());
						 
						 if(result.equalsIgnoreCase("Success")){
							 fw.append("Email sent successfully to "+data.getEmailId()+" \r\n");
						 }else{
							 fw.append("Email sending failed to "+data.getEmailId()+", \r\n");
						 
						 }if(pdfFileName.isEmpty()){
							 fw.append("PDF file name is Empty and PDF file doesnot Create Properly \r\n");
						 }
					 }else{
						 fw.append("PDF file Creation Failed \r\n");
					 }
				}	
				fw.append("Report Emails Job is Completed....\r\n");
				fw.flush();
				fw.close();
          	}
          	System.out.println("Report Emails are Processed....");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}	

/*@Transactional
@Override
@CronTarget(jobName = JobName.MESSAGE_MERGE)
public void processInstances() {
System.out.println("Just Instance of Message......");
}*/
@Override
@CronTarget(jobName = JobName.REPORT_STATMENT)
public void reportStatmentPdf() {
	try {
		System.out.println("Processing statement pdf files....");
		JobParameterData data=this.sheduleJobReadPlatformService.getJobParameters(JobName.REPORT_STATMENT.toString());
		
		if(data!=null){
			MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();	
			final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
			LocalTime date=new LocalTime(zone);
			String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
			String path=FileUtils.generateLogFileDirectory()+ JobName.REPORT_STATMENT.toString() + File.separator +"statement_"+new LocalDate().toString().replace("-","")+"_"+dateTime+".log";
			File fileHandler = new File(path.trim());
			fileHandler.createNewFile();
			FileWriter fw = new FileWriter(fileHandler);
			FileUtils.BILLING_JOB_PATH=fileHandler.getAbsolutePath();
	       fw.append("Processing statement pdf files....... \r\n");
	       List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());
	       
	       if(sheduleDatas.isEmpty()){
	    	   fw.append("ScheduleJobData Empty \r\n");
	       }
	       
	       for(ScheduleJobData scheduleJobData:sheduleDatas){
	    	   fw.append("ScheduleJobData id= "+scheduleJobData.getId()+" ,BatchName= "+scheduleJobData.getBatchName()+
	    			   " ,query="+scheduleJobData.getQuery()+"\r\n");
	    	   List<Long> billIds = this.sheduleJobReadPlatformService.getBillIds(scheduleJobData.getQuery());

	    	   if(billIds.isEmpty()){
	    		   fw.append("no records are available for generate statement pdf files \r\n");
	    	   
	    	   }else{
	    		   fw.append("generate statement pdf files for the  statment bills..... \r\n");
	    	   }
	    	   for(Long billId:billIds){
	    		   fw.append("processing statement  billId: "+billId+ " \r\n");
	    		   this.billingMasterApiResourse.printStatement(billId);
	    	   }
	       }
	       fw.append("statement pdf files Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier()+" . \r\n");
	       fw.flush();
	       fw.close();
		}
		System.out.println("statement  pdf file Job is Completed..."
				+ ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		
		} catch (Exception exception) {  
		System.out.println(exception.getMessage());
		exception.printStackTrace();
		}
		}


	@Override
	@CronTarget(jobName = JobName.EXPORT_DATA)
	public void processExportData() {

		try {
			System.out.println("Processing export data....");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.EXPORT_DATA.toString());
			if (data != null) {
				MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
				final DateTimeZone zone = DateTimeZone.forID(tenant.getTimezoneId());
				LocalTime date = new LocalTime(zone);
				String dateTime = date.getHourOfDay() + "_"+ date.getMinuteOfHour() + "_"+ date.getSecondOfMinute();
				String path = FileUtils.generateLogFileDirectory()+ JobName.EXPORT_DATA.toString() + File.separator	+ "ExportData_"+ new LocalDate().toString().replace("-", "") + "_"+ dateTime + ".log";
				File fileHandler = new File(path.trim());
				fileHandler.createNewFile();
				FileWriter fw = new FileWriter(fileHandler);
				FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				/*DriverManagerDataSource ds=new DriverManagerDataSource();
			    ds.setUrl(tenant.databaseURL());
			    ds.setUsername(tenant.getSchemaUsername());
			    ds.setPassword(tenant.getSchemaPassword());*/
				fw.append("Processing export data....\r\n");
			
				 SimpleJdbcCall simpleJdbcCall=new SimpleJdbcCall(this.jdbcTemplate);
					simpleJdbcCall.setProcedureName("p_int_fa");//p --> procedure int --> integration fa --> financial account s/w
					MapSqlParameterSource parameterSource = new MapSqlParameterSource();
					if (data.isDynamic().equalsIgnoreCase("Y")) {
					     parameterSource.addValue("p_todt", new LocalDate().toDate(), Types.DATE);
					   } else {
						   parameterSource.addValue("p_todt", data.getProcessDate().toDate(), Types.DATE);		
					 }
					Map<String, Object> output = simpleJdbcCall.execute(parameterSource);
					if(output.isEmpty()){
						fw.append("Exporting data failed....."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
					}else{
						fw.append("Exporting data successfully....."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
					}
				fw.flush();
				fw.close();
				System.out.println("Exporting data successfully....."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier());
			}
		} catch (DataIntegrityViolationException e) {
			System.out.println(e.getMessage());
			    e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			    e.printStackTrace();
		}
	}
	
}





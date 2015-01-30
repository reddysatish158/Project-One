package org.mifosplatform.scheduledjobs.scheduledjobs.domain;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.jobs.api.SchedulerJobApiConstants;
import org.mifosplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "job_parameters")
public class JobParameters extends AbstractPersistable<Long>{



	@ManyToOne
    @JoinColumn(name="job_id")
    private ScheduledJobDetail jobDetail;

	@Column(name ="param_name")
    private String paramName;
	
	@Column(name ="param_type")
    private String paramType;
	
	@Column(name ="param_default_value")
    private String paramDefaultValue;
	
	@Column(name ="param_value")
    private String paramValue;
	
	@Column(name ="query_values")
    private String queryValues;


	@Column(name = "is_dynamic")
	private String isDynamic;


	public JobParameters()
	{}


	public JobParameters(Long id, Long jobId, String paramName,String paramType, String defaultValue,
			String paramValue,String isDynamic, String queryValue) {
		
		this.paramName=paramName;
		this.paramType=paramType;
		this.paramDefaultValue=defaultValue;
		this.paramValue=paramValue;
		this.isDynamic=isDynamic;
		this.queryValues=queryValue;
		
		
	}


	public ScheduledJobDetail getJobDetail() {
		return jobDetail;
	}


	public String getParamName() {
		return paramName;
	}


	public String getParamType() {
		return paramType;
	}


	public String getParamDefaultValue() {
		return paramDefaultValue;
	}


	public String getParamValue() {
		return paramValue;
	}


	public String getQueryValues() {
		return queryValues;
	}


	public String isDynamic() {
		return isDynamic;
	}


	public void update(JsonCommand command) {
		
		SimpleDateFormat simpleDateFormat =
		        new SimpleDateFormat("dd MMMMM yyyy", new Locale("en"));
        final String processDateParam= "processDate";
	    final LocalDate processDate = command.localDateValueOfParameterNamed(processDateParam);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobProcessdate) && processDate !=null){

			String date = simpleDateFormat.format(processDate.toDate());
			this.paramValue=date;
	   }
	        
        final String descriptionParamName = "messageTemplate";
	    final String messagetemplate= command.stringValueOfParameterNamed(descriptionParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobMessageTemplate) && messagetemplate!=null){
	    	this.paramValue=messagetemplate;
	      }
	    
	    final String duedateParamName = "dueDate";
	    final LocalDate duedate = command.localDateValueOfParameterNamed(duedateParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobDueDate) && duedate!=null){
	    	String date = simpleDateFormat.format(duedate.toDate());
	    	this.paramValue=date;
	      }
	    
	    final String reportNameParamName = "reportName";
	    final String isDynamicParamName = "isDynamic";
	    final String reportName= command.stringValueOfParameterNamed(reportNameParamName);
	    final boolean isDynamic= command.booleanPrimitiveValueOfParameterNamed(isDynamicParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobReportName) && reportName!=null){
	    
	    	this.paramValue=reportName;
	    	if(isDynamic){
		    	this.isDynamic="Y";
		    	
		    	}else
		    		this.isDynamic="N";
	    	
	      }
	    
      
	   
	   /* if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobIsDynamic)){
	    	if(isDynamic){
	    	this.isDynamic="Y";
	    	
	    	}else
	    		this.isDynamic="N";
	    	
	    } */
	    	
	    	final String isAutoRenewalParamName = "isAutoRenewal";
		    final boolean isAutoRenewal= command.booleanPrimitiveValueOfParameterNamed(isAutoRenewalParamName);
		    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobisAutoRenewal)){
		    	if(isAutoRenewal){
		    	this.isDynamic="Y";
		    //	this.paramValue="Y";
		    	}else
		    		this.isDynamic="N";
		    	//this.paramValue="N";
		     
		    }
	    final String messageParamName = "promotionalMessage";
	    final String promotionalMessage= command.stringValueOfParameterNamed(messageParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobPromotionalMessage) && promotionalMessage!=null){
	    
	    	this.paramValue=promotionalMessage;
	      }
	    
	    final String exipiryDateParamName = "exipiryDate";
	    final LocalDate exipiryDate = command.localDateValueOfParameterNamed(exipiryDateParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobExipiryDate) && exipiryDate!=null){
	    	String date = simpleDateFormat.format(exipiryDate.toDate());
	    	this.paramValue=date;
	      }
	    
	    final String ReportEmailParamName = "emailId";
	    final String ReportEmail = command.stringValueOfParameterNamed(ReportEmailParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_EmailId) && ReportEmail!=null){
	    	this.paramValue=ReportEmail;
	      }
	    
	    final String ProvisioningParamName = "system";
	    final String Provisioning = command.stringValueOfParameterNamed(ProvisioningParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_ProvSystem) && Provisioning!=null){
	    	this.paramValue=Provisioning;
	      }
	    
	    final String UrlParamName = "URL";
	    final String url = command.stringValueOfParameterNamed(UrlParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_URL) && url!=null){
	    	this.paramValue=url;
	      }
	    
	    final String UsernameParamName = "Username";
	    final String Username = command.stringValueOfParameterNamed(UsernameParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_Username) && Username!=null){
	    	this.paramValue=Username;
	      }
	    
	    final String PasswordParamName = "Password";
	    final String Password = command.stringValueOfParameterNamed(PasswordParamName);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.JOB_Password) && Password!=null){
	    	this.paramValue=Password;
	      }
	    
	    final String createticket = "isCreateTicket";
	    final boolean ticket= command.booleanPrimitiveValueOfParameterNamed(createticket);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.jobTicket)){
	    	if(ticket){
	    	this.isDynamic="Y";
	    	}else
	    		this.isDynamic="N";
	        }
	    
	    final String  updateStatus = "isUpdateStatus";
	    final boolean status= command.booleanPrimitiveValueOfParameterNamed(updateStatus);
	    if(this.paramName.equalsIgnoreCase(SchedulerJobApiConstants.statusParamName)){
	    	if(status){
	    	this.isDynamic="Y";
	    	}else
	    		this.isDynamic="N";
	        }
	    
	}


	
	


}
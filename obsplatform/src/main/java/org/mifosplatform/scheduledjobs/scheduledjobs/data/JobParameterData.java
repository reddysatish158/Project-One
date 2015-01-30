package org.mifosplatform.scheduledjobs.scheduledjobs.data;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.mifosplatform.scheduledjobs.scheduledjobs.domain.JobParameters;
import org.mifosplatform.scheduledjobs.scheduledjobs.service.JobParametersConstants;

public class JobParameterData {
	
	private String batchName;
	private String isDynamic;
	private LocalDate dueDate;
	private LocalDate processDate;
	private LocalDate exipiryDate;
	private String defaultValue;
	private String url;
	private String username;
	private String password;
	private String provSystem;
	private String isAutoRenewal;
	private String promotionalMessage;
	private String messageTemplate;
	private String emailId;
	private String createTicket;
	private String updateStatus;
	private String mikrotikApi;
	
	public JobParameterData(List<JobParameters> jobParameters) {
              
		for(JobParameters parameter:jobParameters){
			
			if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_BATCH)){
				   this.batchName=parameter.getParamValue();
				   this.isDynamic=parameter.isDynamic();
			}

			
			else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_MESSAGETEMPLATE)){
			     this.messageTemplate=parameter.getParamValue();

		    }
			
			else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_PROMTIONALMESSAGE)){
			          this.promotionalMessage=parameter.getParamValue();	
			
			}else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_PROCESSDATE) && parameter.getParamValue()!=null){
				    this.processDate= DateTimeFormat.forPattern("dd MMMM yyyy").parseLocalDate(parameter.getParamValue());
				  //  this.isDynamic=parameter.isDynamic();
			
			}else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_DUEDATE) && parameter.getParamValue()!=null){
			    this.dueDate= DateTimeFormat.forPattern("dd MMMM yyyy")
		                 .parseLocalDate(parameter.getParamValue());
			  //  this.isDynamic=parameter.isDynamic();

			}else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_EXIPIRYDATE) && parameter.getParamValue()!=null){
			    this.exipiryDate= DateTimeFormat.forPattern("dd MMMM yyyy")
		                 .parseLocalDate(parameter.getParamValue());

			   // this.isDynamic=parameter.isDynamic();
			}else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_URL)){
			     this.url=parameter.getParamValue();
					
		    }else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_USERNAME)){
		         this.username=parameter.getParamValue();
				
	        }else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_PASSWORD)){
	             this.password=parameter.getParamValue();
			
            }else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_Prov_System)){
                 this.provSystem=parameter.getParamValue();
	
            }else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_MIKROTIK_API)){
			     this.mikrotikApi=parameter.getParamValue();
					
		    }else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_IS_RENEWAL)){
                
            	this.isAutoRenewal=parameter.getParamValue();
            	
           }else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_EMAIL)){
		          this.emailId=parameter.getParamValue();	
					
		   }else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_TICKET)){
			       this.createTicket=parameter.isDynamic();
			       
		   }else if(parameter.getParamName().equalsIgnoreCase(JobParametersConstants.PARAM_STATUS)){
		            this.updateStatus=parameter.isDynamic();
		   }else{
				 this.batchName=parameter.getParamValue();
				 this.defaultValue=parameter.getParamDefaultValue();
				 this.isDynamic=parameter.isDynamic();
				 
			}
			/*if(parameter.isDynamic() == "Y" && parameter.getParamValue() == null){
				
	    		  if(parameter.getParamValue().equalsIgnoreCase("+1")){
	    			  
	    			  this.dueDate=new LocalDate().plusDays(1);
	    			  this.processDate=new LocalDate().plusDays(1);
	    		  }else{
	    			  dueDate=new LocalDate().minusDays(1);
	    			  this.processDate=new LocalDate().minusDays(1);
	    		  }
	    	          	  
	    	  }*/
		}
		
		
	}

	public String getBatchName() {
		return batchName;
	}

	public String isDynamic() {
		return isDynamic;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public LocalDate getProcessDate() {
		return processDate;
	}



	public String getIsAutoRenewal() {
		return isAutoRenewal;
	}

	public String getIsDynamic() {

		return isDynamic;
	}

	public LocalDate getExipiryDate() {
		return exipiryDate;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getProvSystem() {
		return provSystem;
	}

	public String getPromotionalMessage() {
		return promotionalMessage;
	}


	public String getMessageTemplate() {
		return messageTemplate;

	}

	public String getEmailId() {
		return emailId;
	}

	public String getcreateTicket() {
		return createTicket;
	}

	public void setCreateTicket(String createTicket) {
		this.createTicket = createTicket;
	}

	public String getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(String updateStatus) {
		this.updateStatus = updateStatus;
	}

	public String getMikrotikApi() {
		return mikrotikApi;
	}
	
	
	
	
	
	

	

	
	
	
	

	

}

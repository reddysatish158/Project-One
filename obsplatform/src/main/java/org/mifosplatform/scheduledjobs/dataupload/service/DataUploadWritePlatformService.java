package org.mifosplatform.scheduledjobs.dataupload.service;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.scheduledjobs.dataupload.command.DataUploadCommand;


public interface DataUploadWritePlatformService {


	CommandProcessingResult addItem(DataUploadCommand command);
	
	//DataUploadCommand convertJsonToUploadStatusCommand(Object object,String jsonRequestBody);
	
	CommandProcessingResult processDatauploadFile(Long fileId);
	
	
	

}

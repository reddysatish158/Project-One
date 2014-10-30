package org.mifosplatform.scheduledjobs.dataupload.service;

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.codehaus.jettison.json.JSONArray;
import org.joda.time.LocalDate;
import org.mifosplatform.billing.paymode.data.McodeData;
import org.mifosplatform.billing.paymode.service.PaymodeReadPlatformService;
import org.mifosplatform.finance.adjustment.data.AdjustmentData;
import org.mifosplatform.finance.adjustment.exception.AdjustmentCodeNotFoundException;
import org.mifosplatform.finance.adjustment.service.AdjustmentReadPlatformService;
import org.mifosplatform.finance.payments.exception.PaymentCodeNotFoundException;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.scheduledjobs.dataupload.data.MRNErrorData;
import org.mifosplatform.scheduledjobs.dataupload.domain.DataUpload;
import org.mifosplatform.scheduledjobs.dataupload.domain.DataUploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;


@Service
public class DataUploadHelper {
	
	private final DataUploadRepository dataUploadRepository;
	private final AdjustmentReadPlatformService adjustmentReadPlatformService;
	private final PaymodeReadPlatformService paymodeReadPlatformService;
	
	@Autowired
	public DataUploadHelper(final DataUploadRepository dataUploadRepository,final PaymodeReadPlatformService paymodeReadPlatformService,
			final AdjustmentReadPlatformService adjustmentReadPlatformService){
		
		this.dataUploadRepository=dataUploadRepository;
		this.paymodeReadPlatformService=paymodeReadPlatformService;
		this.adjustmentReadPlatformService=adjustmentReadPlatformService;
	}
	
	
	final HashMap<String, String> map = new HashMap<>();
	
	
	public String buildJsonForHardwareItems(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i)  {
		
		if(currentLineData.length>=8){
			map.put("itemMasterId",currentLineData[0]);
			map.put("serialNumber",currentLineData[1]);
			map.put("grnId",currentLineData[2]);
			map.put("provisioningSerialNumber",currentLineData[3]);
			map.put("quality", currentLineData[4]);
			map.put("status",currentLineData[5]);
			map.put("warranty", currentLineData[6]);
			map.put("remarks", currentLineData[7]);
			map.put("itemModel", currentLineData[8]);
			map.put("locale", "en");
			return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
			
		}
	}


	public String buildJsonForMrn(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {
		
		if(currentLineData.length>=2){
			map.put("mrnId",currentLineData[0]);
			map.put("serialNumber",currentLineData[1]);
			map.put("type",currentLineData[2]);
			map.put("locale","en");
			return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
		}
		
	}


	public String buildJsonForMoveItems(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {
		
		if(currentLineData.length>=2){
			map.put("itemId",currentLineData[0]);
			map.put("serialNumber",currentLineData[1]);
			map.put("type",currentLineData[2]);
			map.put("locale","en");
			return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
		}
	}


	public String buildJsonForEpg(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) throws ParseException {
		
		if(currentLineData.length >=11){
			map.put("channelName",currentLineData[0]);
			map.put("channelIcon",currentLineData[1]);
			map.put("programDate",new SimpleDateFormat("dd/MM/yyyy").parse(currentLineData[2]).toString());
			map.put("startTime",currentLineData[3]);
			map.put("stopTime",currentLineData[4]);
			map.put("programTitle",currentLineData[5]);
			map.put("programDescription",currentLineData[6]);
			map.put("type",currentLineData[7]);
			map.put("genre",currentLineData[8]);
			map.put("locale",currentLineData[9]);
			map.put("dateFormat",currentLineData[10]);
			map.put("locale","en");
				return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
		}


	}


	public String buildJsonForAdjustment(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {

		if(currentLineData.length >= 6){
			List<AdjustmentData> adjustmentDataList=this.adjustmentReadPlatformService.retrieveAllAdjustmentsCodes();
			if(!adjustmentDataList.isEmpty()){
				 for(AdjustmentData adjustmentData:adjustmentDataList){
					   if( adjustmentData.getAdjustment_code().equalsIgnoreCase(currentLineData[2].toString())){ 
					        	 
						   map.put("adjustment_code", adjustmentData.getId().toString());
						   break;
					   }else{
						   map.put("adjustment_code", String.valueOf(-1));	
					   }
				    }
				 String adjustmentCode = map.get("adjustment_code");
				   if(adjustmentCode!=null && Long.valueOf(adjustmentCode)<=0){
				    	
				    	throw new AdjustmentCodeNotFoundException(currentLineData[2].toString());
				    }
				map.put("adjustment_date", currentLineData[1]);
				map.put("adjustment_type",currentLineData[3]);
				map.put("amount_paid",currentLineData[4]);
				map.put("Remarks",currentLineData[5]);
				map.put("locale", "en");
				map.put("dateFormat","dd MMMM yyyy");
				return new Gson().toJson(map);	
			}else{
				errorData.add(new MRNErrorData((long)i, "Adjustment Type list is empty"));
				return null;
			}
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
			return null;
		}
	}


	public String buildjsonForPayments(String[] currentLineData, ArrayList<MRNErrorData> errorData, int i) {
		
	
		if(currentLineData.length >=6){
			Collection<McodeData> paymodeDataList = this.paymodeReadPlatformService.retrievemCodeDetails("Payment Mode");
				if(!paymodeDataList.isEmpty()){
					for(McodeData paymodeData:paymodeDataList){
						if(paymodeData.getPaymodeCode().equalsIgnoreCase(currentLineData[2].toString())){
							map.put("paymentCode",paymodeData.getId().toString());
							break;
						}else{
							map.put("paymentCode","-1");
						}
					}
					String paymentCode = map.get("paymentCode");
					if(paymentCode!=null && Long.valueOf(paymentCode) <=0){
						throw new PaymentCodeNotFoundException(currentLineData[2].toString());
					}
					map.put("clientId", currentLineData[0]);
					map.put("paymentDate",currentLineData[1]);
					map.put("amountPaid", currentLineData[3]);
					map.put("remarks",  currentLineData[4]);
					map.put("locale", "en");
					map.put("dateFormat","dd MMMM yyyy");
					map.put("receiptNo",currentLineData[4]);
        	return new Gson().toJson(map);
        }else{
        	errorData.add(new MRNErrorData((long)i, "Paymode type list empty"));
			return null;
        }
		 
	}else{
		errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
		return null;
	}
	}


	public String buildForMediaAsset(Row mediaRow, Row mediaAttributeRow, Row mediaLocationRow) {
	
		map.put("mediaTitle",mediaRow.getCell(0).getStringCellValue());//-
		map.put("mediaType",mediaRow.getCell(1).getStringCellValue());//-
		map.put("mediaCategoryId",mediaRow.getCell(2).getStringCellValue());//-
		map.put("image",mediaRow.getCell(3).getStringCellValue());//- 
		map.put("duration",mediaRow.getCell(4).getStringCellValue());//-
		map.put("genre",mediaRow.getCell(5).getStringCellValue());//-
		map.put("subject",mediaRow.getCell(6).getStringCellValue());//-
		map.put("overview",mediaRow.getCell(7).getStringCellValue());//-
		map.put("contentProvider",mediaRow.getCell(8).getStringCellValue());//-
		map.put("rated",mediaRow.getCell(9).getStringCellValue());//-
		//map.put("rating",mediaRow.getCell(10).getNumericCellValue());//-
		map.put("rating",mediaRow.getCell(10).getStringCellValue());//-
		map.put("status",mediaRow.getCell(11).getStringCellValue());//-
		SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
		map.put("releaseDate",formatter.format(mediaRow.getCell(12).getDateCellValue()));
		map.put("dateFormat",mediaRow.getCell(13).getStringCellValue());
		map.put("locale",mediaRow.getCell(14).getStringCellValue());
				
			JSONArray a = new JSONArray();
			Map<String, Object> m = new LinkedHashMap<String, Object>();
			m.put("attributeType",mediaAttributeRow.getCell(0).getStringCellValue());
			m.put("attributeName", mediaAttributeRow.getCell(1).getNumericCellValue());
			m.put("attributevalue", mediaAttributeRow.getCell(2).getStringCellValue());
			m.put("attributeNickname", mediaAttributeRow.getCell(3).getStringCellValue());
			m.put("attributeImage", mediaAttributeRow.getCell(4).getStringCellValue());
			
			a.put(m);
			map.put("mediaassetAttributes",a.toString());
			
			
			JSONArray b = new JSONArray();
			Map<String, Object> n = new LinkedHashMap<String, Object>();
			n.put("languageId",mediaLocationRow.getCell(0).getNumericCellValue());	
			n.put("formatType",mediaLocationRow.getCell(1).getStringCellValue());
			n.put("location",mediaLocationRow.getCell(2).getStringCellValue());
			b.put(n);
			
			map.put("mediaAssetLocations",b.toString());
			return new Gson().toJson(map);
	}


	public String buildforitemSale(String[] currentLineData,ArrayList<MRNErrorData> errorData, int i)  throws ParseException {
		
		if(currentLineData.length>=6){
		map.put("agentId",currentLineData[0]);
		map.put("itemId",currentLineData[1]);
		map.put("orderQuantity",currentLineData[2]);
		map.put("chargeAmount",currentLineData[3]);
		map.put("taxPercantage", currentLineData[4]);
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMMM-yy");
    	Date date=formatter.parse(currentLineData[5]);
    	SimpleDateFormat formatter1 = new SimpleDateFormat("dd MMMM yyyy");
    	   
    	map.put("locale", "en");
    	map.put("dateFormat","dd MMMM yyyy");
    	map.put("purchaseDate",formatter1.format(date));
    	return new Gson().toJson(map);
		}else{
			errorData.add(new MRNErrorData((long)i, "Improper Data in this line"));
		return null;
		}
	}

	public CommandProcessingResult updateFile(DataUpload dataUpload,Long totalRecordCount, Long processRecordCount,
			ArrayList<MRNErrorData> errorData) {
		
		dataUpload.setProcessRecords(processRecordCount);
		dataUpload.setUnprocessedRecords(totalRecordCount-processRecordCount);
		dataUpload.setTotalRecords(totalRecordCount);
		writeCSVData(dataUpload.getUploadFilePath(), errorData,dataUpload);
		processRecordCount=0L;totalRecordCount=0L;
		this.dataUploadRepository.save(dataUpload);
		final String filelocation=dataUpload.getUploadFilePath();
		dataUpload=null;
		writeToFile(filelocation,errorData);
		return new CommandProcessingResult(Long.valueOf(-1));
	}


	private void writeToFile(String uploadFilePath,ArrayList<MRNErrorData> errorData) {
		
			FileWriter fw = null;
			try{
				File f = new File(uploadFilePath.replace(".csv", ".log"));
				if(!f.exists()){
					f.createNewFile();
				}
				fw = new FileWriter(f,true);
				for(int k=0;k<errorData.size();k++){
					if(!errorData.get(k).getErrorMessage().equalsIgnoreCase("Success.")){
						fw.append("Data at row: "+errorData.get(k).getRowNumber()+", Message: "+errorData.get(k).getErrorMessage()+"\n");
					}
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					if(fw!=null){
						fw.flush();
						fw.close();
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
	


	private void writeCSVData(String uploadFilePath,ArrayList<MRNErrorData> errorData,DataUpload uploadStatus) {
		

		
		try{
			
			long processRecords = uploadStatus.getProcessRecords();
			long totalRecords = uploadStatus.getTotalRecords();
			long unprocessRecords  = totalRecords-processRecords;
			if(unprocessRecords == 0){
				uploadStatus.setProcessStatus("Success");
				uploadStatus.setErrorMessage("Data successfully saved");
			}else if(unprocessRecords<totalRecords){
				uploadStatus.setProcessStatus("Completed");
				uploadStatus.setErrorMessage("Completed with some errors");
			}else if(unprocessRecords == totalRecords){
				uploadStatus.setProcessStatus("Failed");
				uploadStatus.setErrorMessage("Processing failed");
			}
			
			uploadStatus.setProcessDate(new LocalDate().toDate());
			this.dataUploadRepository.save(uploadStatus);
			uploadStatus = null;
		}catch(Exception  exception){
			exception.printStackTrace();
		}
		
		
	}

}

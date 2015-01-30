package org.mifosplatform.freeradius.radius.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
<<<<<<< HEAD
import org.mifosplatform.freeradius.radius.exception.RadiusDetailsNotFoundException;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.portfolio.order.exceptions.OrderNotFoundException;
=======
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.portfolio.order.exceptions.RadiusDetailsNotFoundException;
>>>>>>> upstream/obsplatform-2.03
import org.mifosplatform.provisioning.processscheduledjobs.service.SheduleJobReadPlatformService;
import org.mifosplatform.scheduledjobs.scheduledjobs.data.JobParameterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 * 
 */
@Service
public class RadiusReadPlatformServiceImp implements RadiusReadPlatformService {

	private final SheduleJobReadPlatformService sheduleJobReadPlatformService;
	
	@Autowired
	public RadiusReadPlatformServiceImp(final SheduleJobReadPlatformService sheduleJobReadPlatformService){
		this.sheduleJobReadPlatformService = sheduleJobReadPlatformService;
		
	}
	
	@SuppressWarnings("unused")
	@Override
	public String retrieveAllNasDetails() {

		try {
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());

			if(data == null){
				throw new RadiusDetailsNotFoundException();
			}
			String url = data.getUrl() + "nas";
			String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
			byte[] encoded = Base64.encodeBase64(credentials.getBytes());
			String encodedPassword = new String(encoded);
			String nasData = this.processRadiusGet(url, encodedPassword);
			return nasData;
		 
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}
	
	
	@SuppressWarnings("unused")
	@Override
	public String retrieveNasDetail(final Long nasId) {

		try {
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());


			if(data == null){
				throw new RadiusDetailsNotFoundException();
			}
			String url = data.getUrl() + "nas/"+nasId;
			String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
			byte[] encoded = Base64.encodeBase64(credentials.getBytes());
			String encodedPassword = new String(encoded);
			String nasData = this.processRadiusGet(url, encodedPassword);
			return nasData;
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}
	
	@Override
	public String createNas(final String jsonData) {
		
		try {
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());

			if(data == null){
				throw new RadiusDetailsNotFoundException();
			}
			String url = data.getUrl() + "nas";
			String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
			byte[] encoded = Base64.encodeBase64(credentials.getBytes());
			String encodedPassword = new String(encoded);
			String nasData = this.processRadiusPost(url, encodedPassword,jsonData);
			return nasData;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	
	@Override
	public String deleteNasDetail(final Long nasId) {

		try {
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());

			
			if(data == null){
				throw new RadiusDetailsNotFoundException();
			}
			String url = data.getUrl() + "nas/"+nasId;
			String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
			byte[] encoded = Base64.encodeBase64(credentials.getBytes());
			String encodedPassword = new String(encoded);
			String nasData = this.processRadiusDelete(url, encodedPassword);
			return nasData;
		
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}
	
	@Override
	public String retrieveAllRadServiceDetails(final String attribute) {

		try {
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());
			if(data == null){
				throw new RadiusDetailsNotFoundException();
			}
			String url ="";
			if(data.getProvSystem().equalsIgnoreCase("version-1")){
				if(attribute!=null){
					url= data.getUrl() + "radservice?attribute="+attribute;
				}else{
					url= data.getUrl() + "radservice";
				}
			}else if(data.getProvSystem().equalsIgnoreCase("version-2")){
				url= data.getUrl() + "service2";
			}
			String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
			byte[] encoded = Base64.encodeBase64(credentials.getBytes());
			String encodedPassword = new String(encoded);
			String radServiceData = this.processRadiusGet(url, encodedPassword);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("radiusVersion", data.getProvSystem().toLowerCase());
			jsonObj.put("radServiceData", new JSONArray(radServiceData));
			radServiceData = jsonObj.toString();
			return radServiceData;
			

		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (JSONException e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}
	
	@Override
	public String createRadService(final String Json) {
		
		try {
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());
			if(data == null){
				throw new RadiusDetailsNotFoundException();
			}
			String url = "";
			if(data.getProvSystem().equalsIgnoreCase("version-1")){
				 url = data.getUrl() + "radservice";
			}
			else if(data.getProvSystem().equalsIgnoreCase("version-2")){
				 url = data.getUrl() + "service2";
			}
			String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
			byte[] encoded = Base64.encodeBase64(credentials.getBytes());
			String encodedPassword = new String(encoded);
			String radServiceData = this.processRadiusPost(url, encodedPassword,Json);
			return radServiceData;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	
	@Override
	public String retrieveRadServiceDetail(final Long radServiceId) {

		try {
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());
			if(data == null){
				throw new RadiusDetailsNotFoundException();
			}
			String url = data.getUrl() + "radservice/"+radServiceId;
			String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
			byte[] encoded = Base64.encodeBase64(credentials.getBytes());
			String encodedPassword = new String(encoded);
			String radServiceData = this.processRadiusGet(url, encodedPassword);
			return radServiceData;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}
	
	@Override
	public String deleteRadService(final Long radServiceId) {

		try {
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());
			if(data == null){
				throw new RadiusDetailsNotFoundException();
			}
			String url = "";
			if(data.getProvSystem().equalsIgnoreCase("version-1")){
			  url = data.getUrl() + "radservice/"+radServiceId;
			}
			/*else if(data.getProvSystem().equalsIgnoreCase("version-2")){
				url = data.getUrl() + "radservice/"+radServiceId;
			}*/
			String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
			byte[] encoded = Base64.encodeBase64(credentials.getBytes());
			String encodedPassword = new String(encoded);
			String radServiceData = this.processRadiusDelete(url, encodedPassword);
			return radServiceData;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}

	}
	
	//get
	private  String processRadiusGet(String url, String encodePassword) throws ClientProtocolException, IOException{
		
		 HttpClient httpClient = new DefaultHttpClient();
		 HttpGet getRequest = new HttpGet(url);
		 getRequest.setHeader("Authorization", "Basic " +encodePassword);
		 getRequest.setHeader("Content-Type", "application/json");
		 HttpResponse response=httpClient.execute(getRequest);
		 
		 if (response.getStatusLine().getStatusCode() == 404) {
				return "ResourceNotFoundException";

			} else if (response.getStatusLine().getStatusCode() == 401) {	
				return "UnauthorizedException"; 

			} else if (response.getStatusLine().getStatusCode() != 200) {
				System.out.println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			} else{
				System.out.println("Execute Successfully:" + response.getStatusLine().getStatusCode());
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output,output1="";
			
			while ((output = br.readLine()) != null) {
				output1 = output1 + output;
			}
			br.close();
			return output1;
		 
		}
	
	//post
	private  String processRadiusPost(String url, String encodePassword, String data) throws IOException{
		
		HttpClient httpClient = new DefaultHttpClient();
		StringEntity se = new StringEntity(data.trim());
		HttpPost postRequest = new HttpPost(url);
		postRequest.setHeader("Authorization", "Basic " + encodePassword);
		postRequest.setHeader("Content-Type", "application/json");
		postRequest.setEntity(se);
		HttpResponse response = httpClient.execute(postRequest);

		if (response.getStatusLine().getStatusCode() == 404) {
			return "ResourceNotFoundException";

		} else if (response.getStatusLine().getStatusCode() == 401) {
			return "UnauthorizedException"; 

		} else if (response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		} else{
			System.out.println("Execute Successfully:" + response.getStatusLine().getStatusCode());
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
	
	//delete
	private  String processRadiusDelete(String url, String encodePassword) throws ClientProtocolException, IOException{
		
		 HttpClient httpClient = new DefaultHttpClient();
		 HttpDelete deleteRequest = new HttpDelete(url);
		 deleteRequest.setHeader("Authorization", "Basic " +encodePassword);
		 deleteRequest.setHeader("Content-Type", "application/json");
		 HttpResponse response=httpClient.execute(deleteRequest);
		 
		 if (response.getStatusLine().getStatusCode() == 404) {
				return "ResourceNotFoundException";

			} else if (response.getStatusLine().getStatusCode() == 401) {	
				return "UnauthorizedException"; 

			} else if (response.getStatusLine().getStatusCode() != 200) {
				System.out.println("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			} else{
				System.out.println("Execute Successfully:" + response.getStatusLine().getStatusCode());
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output,output1="";
			
			while ((output = br.readLine()) != null) {
				output1 = output1 + output;
			}
			br.close();
			return output1;
		 
		}

	@Override
	public String retrieveRadServiceTemplateData() {
		
		try {
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());
			if(data == null){
				throw new RadiusDetailsNotFoundException();
			}
			String url ="";
			url= data.getUrl() + "raduser2/template";
			String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
			byte[] encoded = Base64.encodeBase64(credentials.getBytes());
			String encodedPassword = new String(encoded);
			String radServiceTemplateData = this.processRadiusGet(url, encodedPassword);
			return radServiceTemplateData;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

}


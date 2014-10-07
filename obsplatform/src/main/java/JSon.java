import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;









public class JSon {

	public static void main(String []args) throws JSONException{
		JSONObject jsonObject=new JSONObject();
		//jsonObject.opoptJSONObject("'name':");
		    String  jString = "{" 
				   + "    \"geodata\": [" 
				   + "        {" 
				   + "                \"id\": \"1\"," 
				   + "                \"name\": \"Julie Sherman\","                  
				   + "                \"gender\" : \"female\"," 
				   + "                \"latitude\" : \"37.33774833333334\"," 
				   + "                \"longitude\" : \"-121.88670166666667\""            
				   + "                }" 
				   + "        }," 
				   + "        {" 
				   + "                \"id\": \"2\"," 
				   + "                \"name\": \"Johnny Depp\","          
				   + "                \"gender\" : \"male\"," 
				   + "                \"latitude\" : \"37.336453\"," 
				   + "                \"longitude\" : \"-121.884985\""            
				   + "                }" 
				   + "        }" 
				   + "    ]" 
				   + "}"; 
		
		String[] strings={"192.168.2.200"," 156.10.20.31 "};
		jsonObject.put("parmaname","IPADD");
		JSONArray array=new JSONArray();
		array.add("192.168.1.200");
		array.add("192.168.1.201");
		for(String ipAddress:strings){
			array.add(ipAddress);
		}
		String string=strings.toString();
		jsonObject.put("paramvalue",array.toString());
		System.out.println(array.toString());
		 
		
		
		
		System.out.println(jsonObject);
	}


}


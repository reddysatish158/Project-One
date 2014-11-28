

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;




import com.google.gson.Gson;





public class DemoJson {
	
	public static void main(String [] arg) throws  org.codehaus.jettison.json.JSONException{
		
		JSONArray newServiceArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		JSONObject subjson = new JSONObject();
		final HashMap<String, String> map = new HashMap<>();
		for(int i=0;i<2;i++){
			
			final HashMap<String, String> map2 = new HashMap<>();
			subjson.put("serviceName", "ser1");
			subjson.put("serviceIdentification","serID2");
			newServiceArray.add(subjson);
		}
		 
		// Map<String, JSONArray> m2 = new HashMap<String, JSONArray>();
		
		 String string=new Gson().toJson(newServiceArray);
		 
		 jsonObject.put("Name","values");
		 jsonObject.put("Services", string);
		 System.out.println(jsonObject);
		
	}

}

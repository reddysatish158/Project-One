
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class JsonData {
	
	public static void main(String [] arg) throws JSONException{
		String str="{name:'kiran','services':[{'addr1':'1/89'},{'addr1':'2/89'}]}";
		
		JSONObject jsonObject=new JSONObject(str);
		JSONArray array=jsonObject.getJSONArray("services");
		for(int i=0;i<array.length();i++){
			JSONObject jsonObjec=array.getJSONObject(i);
			System.out.println(jsonObjec.get("addr1"));	
		}
		
		
	}

}

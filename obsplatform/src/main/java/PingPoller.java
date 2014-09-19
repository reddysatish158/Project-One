import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PingPoller
{
    public static void main(String[] args) throws ParseException
    {
    	String str="action:Active,  orderid:12928,  planid:3,  contractperiod:5";
    	String[] resultdatas=str.split(",");
  		Map<String,String> map=new HashMap<String, String>();

  			for(String resultData:resultdatas){
  				String[] data=resultData.split(":");
  				map.put(data[0],data[1]);
  			}
  			System.out.println(map.get("  orderid"));
    }
    }

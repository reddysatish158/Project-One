import net.sf.json.JSONException;









public class JSon {

	public static void main(String []args) throws JSONException{
	
	String str="46.46.58.30";
	String str1="46.46.58.15";
	str=str.substring(str.lastIndexOf(".")+1);
    str1=str1.substring(str1.lastIndexOf(".")+1);
	System.out.println(str.substring(str.lastIndexOf(".")+1));
	System.out.println(str1.substring(str1.lastIndexOf(".")+1));
	// str=str.replaceAll(str.substring(str.lastIndexOf(".")+1),"5");
	System.out.println(str);
   if(Long.valueOf(str).compareTo(Long.valueOf(str1)) > 0){
	   System.out.println(str1);
}
	}

}


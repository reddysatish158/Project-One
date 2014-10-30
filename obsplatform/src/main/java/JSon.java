import java.util.HashMap;









public class JSon {

	public static void main(String []args) {
		final HashMap<String, String> map = new HashMap<>();
		
		String json=map.toString();
		System.out.println(json);
		if(json.isEmpty()){
			System.out.println(true);
		}
	}

}


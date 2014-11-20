

public class EmailSending {

	public static void main(String a[]) {
		String event=a[0];
		
		switch (event) {
		case "create":
			System.out.println("case is "+event);
			break;
			
		case "update":
			System.out.println("case is "+event);
			break;
		case "delete":
			System.out.println("case is "+event);
			break;
		case "get":
			System.out.println("case is "+event);
			break;

		default:
			break;
		}
		
		
	}
}

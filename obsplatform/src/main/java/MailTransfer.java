
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;





public class MailTransfer {

public static void main(String []args){

	try {
	Email email = new SimpleEmail();
	   Class.forName("com.mysql.jdbc.Driver");
	   System.out.println("sending mail....");
		// Very Important, Don't use email.setAuthentication()
	Connection   conn = DriverManager.getConnection("jdbc:mysql://192.168.1.200:3306/obs_beenius?useUnicode=true&characterEncoding=UTF8","root", "mysql");
	 Statement stmt = conn.createStatement();
	 ResultSet rs = stmt.executeQuery("select  subject, body from b_message_template where template_description='SELFCARE REGISTRATION'");
	email.setAuthenticator(new DefaultAuthenticator("mummi.digital@gmail.com","you.can.d0.1t"));
	email.setDebug(false); // true if you want to debug
	email.setHostName("smtp.gmail.com");
	//Translator translate = Translator.getInstance();
  //  String translatedText = Translator.DEFAULT.execute("Bonjour le monde", Lang.FRENCH, Language.ENGLISH);
	//String text = translate.translate("Halló !","IS","en");
	
		String sendToEmail = "kiran12b0@gmail.com";
		if(rs.next()){
			String subject=rs.getString("subject").toString().trim();
			String body=rs.getString("body").toString().trim();
		System.out.println("sub :"+subject);	//body=translate.translate(body,"en","IS");
		System.out.println("body :"+body);
			//subject=translate.translate(subject,"en","IS");
		StringBuilder messageBuilder = new StringBuilder().append(body);			
		email.getMailSession().getProperties().put("mail.smtp.starttls.enable", "true");
		email.setFrom("mummi.digital@gmail.com","mummi.digital@gmail.com");
		email.setSmtpPort(25);
		email.setSubject(subject);		
		
		email.addTo(sendToEmail, sendToEmail);
		email.setMsg(messageBuilder.toString());
		email.send();
		}
		System.out.println("mail sent");
		conn.close();
	} catch (Exception e) {
		e.printStackTrace();
	}finally{
		
	}
	
}
}

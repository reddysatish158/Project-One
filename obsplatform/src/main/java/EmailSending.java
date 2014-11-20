/*
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSending {

	public static void main(String a[]) {
		Properties properties = new Properties();
		String outgoingMailServer = "mail.spicenet.co.tz";
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		 properties.put("mail.smtp.host", outgoingMailServer);
         properties.put("mail.smtps.auth", "true");
         properties.put("mail.smtp.starttls.enable", "true");
	     properties.put("mail.smtp.starttls.required", "true");
	     properties.put("mail.smtp.ssl.trust","mail.spicenet.co.tz");

		Session session = Session.getDefaultInstance(properties,null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("ashokreddy556@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("ashokcse556@gmail.com"));
			message.setSubject("Testing Subject");
			message.setText("Dear Mail Crawler,"
					+ "\n\n No spam to my email, please!");
			
		    MimeMessage message=new MimeMessage(session);  
		    message.setFrom(new InternetAddress("system@spicenet.co.tz"));  
		    message.addRecipient(Message.RecipientType.TO,   
		    new InternetAddress("osbtest@streamingmedia.is"));  
		    message.setText("N�sta skref � skr�ningu er a� fara � sl��ina h�r fyrir ne�an og fylla �t n�nari uppl�singar: URL : <PARAM1>");  

			Transport.send(message);
			
			Transport tr = session.getTransport("smtp");
			// transport.connect(outgoingMailServer, 465, emailUserName, emailPassword)
			tr.connect(outgoingMailServer,587, "system@spicenet.co.tz", "sys@spice2014");
			message.saveChanges();
			tr.sendMessage(message, message.getAllRecipients());
			tr.close();

			System.out.println("Done");
			System.out.println("Don1");


		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}

	}
}
*/
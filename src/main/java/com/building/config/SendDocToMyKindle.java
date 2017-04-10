package com.building.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.building.mail.MailEntity;
import com.building.mail.SendMailProvider;

/**
 * Send Doc To My Kindle By Mail
 */
public class SendDocToMyKindle {

	public static void main(String[] args) {

		// [01]
		// Spring IOC 容器
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext();
		annotationConfigApplicationContext.register(ApplicationConfig.class);
		annotationConfigApplicationContext.refresh();

		annotationConfigApplicationContext.registerShutdownHook();

		// get bean
		SendMailProvider mailProvider = annotationConfigApplicationContext.getBean(SendMailProvider.class);

		String fileName = "Notes.txt";
		String filePath = "/root/Documents/" + fileName;

		// [02]
		MailEntity mail = new MailEntity();
		mail.setTo("qianshijinsheng@gmail.com");
		mail.setSubject(fileName);
		mail.setText("Timestamp : " + String.valueOf(System.currentTimeMillis()));
		// 为from字段赋值
		mail.setFrom(mailProvider.getJavaMailSender().getUsername());

		Map<String, String> attachment = new HashMap<String, String>();
		attachment.put(fileName, filePath);
		mail.setAttachment(attachment);

		try {
			// [03]
			mailProvider.sendMimeMail(mail);
		} catch (Exception e) {
			e.printStackTrace();
		}

		annotationConfigApplicationContext.close();

	}

}

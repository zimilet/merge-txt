package com.building;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.building.mail.MailEntity;
import com.building.mail.SendMailProvider;

/**
 * Send Doc To My Kindle By Mail
 */
public class SendDocToMyKindle {

	/**
	 * Spring IOC 容器
	 */
	private AbstractApplicationContext applicationContext;

	public AbstractApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * get bean
	 * 
	 * @return
	 */
	private SendMailProvider getMailProvider() {

		// Spring IOC 容器
		applicationContext = new ClassPathXmlApplicationContext(
				new String[] { "classpath*:/applicationContext.xml", "classpath*:/applicationContext-mail.xml" });

		applicationContext.registerShutdownHook();

		// get bean
		SendMailProvider mailProvider = (SendMailProvider) applicationContext.getBean("sendMailProvider");
		return mailProvider;

	}

	public static void main(String[] args) {

		SendDocToMyKindle sendDocToMyKindle = new SendDocToMyKindle();

		try {

			// [01]
			SendMailProvider mailProvider = sendDocToMyKindle.getMailProvider();

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

			// [03]
			mailProvider.sendMimeMail(mail);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != sendDocToMyKindle.getApplicationContext()) {
				sendDocToMyKindle.getApplicationContext().close();
			}
		}

	}

}

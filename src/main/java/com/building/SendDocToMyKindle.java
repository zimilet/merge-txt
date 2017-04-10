package com.building;

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

			SendMailProvider mailProvider = sendDocToMyKindle.getMailProvider();

			long time = System.currentTimeMillis();

			MailEntity mail = new MailEntity();
			mail.setTo("qianshijinsheng@gmail.com");
			mail.setSubject("Test Java Mail");
			mail.setText("Timestamp : " + String.valueOf(time));
			// 为from字段赋值
			mail.setFrom(mailProvider.getJavaMailSender().getUsername());

			mailProvider.sendSimpleMail(mail);

		} finally {
			if (null != sendDocToMyKindle.getApplicationContext()) {
				sendDocToMyKindle.getApplicationContext().close();
			}
		}

	}

}

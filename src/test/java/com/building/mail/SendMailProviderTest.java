package com.building.mail;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml", "classpath:applicationContext-mail.xml" })
public class SendMailProviderTest {

	@Resource
	private SendMailProvider mailProvider;

	@Value("${mail.kindle}")
	private String mailKindle;

	@Test
	public void testSendSimpleMail() {

		long time = System.currentTimeMillis();

		MailEntity mail = new MailEntity();
		mail.setTo(mailKindle);
		mail.setSubject("Test Java Mail");
		mail.setText("Timestamp : " + String.valueOf(time));
		// 为from字段赋值
		mail.setFrom(mailProvider.getJavaMailSender().getUsername());

		this.mailProvider.sendSimpleMail(mail);

	}

}

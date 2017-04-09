package com.building.mail;

import java.io.File;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * <ul>
 * <li>JavaMail 实际发送类</li>
 * <li>发送简单邮件</li>
 * <li>发送Mime邮件</li>
 * </ul>
 */
@Component
public class SendMailProvider {

	private static final Log log = LogFactory.getLog(SendMailProvider.class);

	/**
	 * <p>
	 * 默认编码[UTF-8]
	 * </p>
	 */
	private static final String DEFAULT_CHARACTOR = "UTF-8";

	/**
	 * 注入Spring Java Mail Sender
	 */
	@Autowired
	private JavaMailSenderImpl javaMailSender;

	public JavaMailSenderImpl getJavaMailSender() {
		return javaMailSender;
	}

	/**
	 * <p>
	 * 发送简单邮件
	 * </p>
	 * 
	 * @param email
	 */
	public void sendSimpleMail(MailEntity email) {

		if (null == email) {
			if (log.isDebugEnabled()) {
				log.debug("The mail content is null.");
			}
		} else {

			// 建立简单邮件消息
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
			simpleMailMessage.setFrom(email.getFrom());
			simpleMailMessage.setTo(email.getTo());
			simpleMailMessage.setSubject(email.getSubject());
			simpleMailMessage.setText(email.getText());

			javaMailSender.send(simpleMailMessage);

		}

	}

	/**
	 * 发送含有附件或图片的邮件
	 * 
	 * @param email
	 */
	public void sendMimeMail(MailEntity email) throws Exception {

		if (null == email) {
			if (log.isDebugEnabled()) {
				log.debug("The mail content is null.");
			}
		} else {

			// 建立邮件消息
			MimeMessage message = javaMailSender.createMimeMessage();
			MimeMessageHelper messageHelper = null;

			messageHelper = new MimeMessageHelper(message, true, DEFAULT_CHARACTOR);
			messageHelper.setSentDate(new Date());
			messageHelper.setTo(email.getTo());
			messageHelper.setFrom(email.getFrom());
			messageHelper.setSubject(email.getSubject());

			messageHelper.setText(email.getText(), true);
			Map<String, String> attachment = email.getAttachment();
			Map<String, String> img = email.getImg();

			if (attachment != null) {
				this.addAttachmentOrImg(messageHelper, attachment, true);
			}
			if (img != null) {
				this.addAttachmentOrImg(messageHelper, img, false);
			}

			javaMailSender.send(message);

		}

	}

	/**
	 * <p>
	 * 为邮件添加附件或图片
	 * </p>
	 * 
	 * @param messageHelper
	 * @param map
	 * @param isAttachment
	 *            添加附件还是图片
	 * @throws MessagingException
	 */
	private void addAttachmentOrImg(MimeMessageHelper messageHelper, Map<String, String> map, boolean isAttachment)
			throws MessagingException {

		for (String key : map.keySet()) {
			// 取出key对应的value
			String value = map.get(key);

			FileSystemResource file = new FileSystemResource(new File(value));

			if (!file.exists())
				continue;

			if (isAttachment) {
				// 添加附件
				messageHelper.addAttachment(key, file);
			} else {
				// 添加图片,需要一个cid值
				messageHelper.addInline(key, file);
			}

		}

	}

}
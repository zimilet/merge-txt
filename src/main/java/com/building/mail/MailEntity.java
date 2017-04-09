package com.building.mail;

import java.io.Serializable;
import java.util.Map;

/**
 * Java Mail Content
 */
public class MailEntity implements Serializable {

	private static final long serialVersionUID = -1529821625479477908L;

	/**
	 * 收件人
	 */
	private String to;

	/**
	 * 发件人
	 */
	private String from;

	/**
	 * 主题
	 */
	private String subject;

	/**
	 * 正文
	 */
	private String text;

	/**
	 * 邮件附件
	 */
	Map<String, String> attachment;

	/**
	 * 内嵌图片
	 */
	Map<String, String> img;

	public MailEntity() {
		super();
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Map<String, String> getAttachment() {
		return attachment;
	}

	public void setAttachment(Map<String, String> attachment) {
		this.attachment = attachment;
	}

	public Map<String, String> getImg() {
		return img;
	}

	public void setImg(Map<String, String> img) {
		this.img = img;
	}

}

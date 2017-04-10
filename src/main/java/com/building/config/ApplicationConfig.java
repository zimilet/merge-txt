package com.building.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * applicationContext.xml
 */
@Configuration
@ComponentScan(basePackages = { "com.building" })
@PropertySource(value = { "classpath:mail.properties" }, ignoreResourceNotFound = true)
public class ApplicationConfig {

	/**
	 * To resolve ${} in @Values, you must register a static
	 * PropertySourcesPlaceholderConfigurer in either XML or annotation
	 * configuration file.
	 * 
	 * @return
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Value("${mail.host}")
	private String host;

	@Value("${mail.port}")
	private int port;

	@Value("${mail.username}")
	private String username;

	@Value("${mail.password}")
	private String password;

	/**
	 * applicationContext-mail.xml
	 * 
	 * @return
	 */
	@Bean
	public JavaMailSenderImpl javaMailSender() {

		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setHost(host);
		javaMailSender.setPort(port);
		javaMailSender.setUsername(username);
		javaMailSender.setPassword(password);

		javaMailSender.setDefaultEncoding("UTF-8");

		Properties properties = new Properties();
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.timeout", "50000");
		javaMailSender.setJavaMailProperties(properties);

		return javaMailSender;

	}

}

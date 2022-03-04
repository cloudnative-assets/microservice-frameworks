package com.ibm.epricer.svclib.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "spring.mail.host", havingValue="")
public class EmailServerInfo {
	@Value("${spring.mail.host}")
	private String host;
	
	@Value("${spring.mail.port}")
	private Integer port;
	
	@Value("${spring.mail.protocol}")
	private String protocol;
	
	@Value("${spring.mail.properties.mail.smtp.auth}")
	private Boolean smtpAuth;
	
	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	private Boolean starttlsEnabled;
	

	String info() {
		return "\n" +
				"Mail Server Details: " + "\n" +
				"Mail Protocol: " + protocol + "\n" +
				"Mail Server: " + host + "\n" +
				"Mail Server Port: " + port + "\n" +
				"Mail Protocol Auth: " + smtpAuth + "\n" +
				"Mail Protocol STATTLS enabled: " + starttlsEnabled; 
	}
	
}

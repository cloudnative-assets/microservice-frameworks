package com.ibm.epricer.svclib.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:email.properties")
class EmailConfig {
	@Value("${epricer.mail.from.address}")
	private String defaultFromAddress;
	
	String getDefaultFromAddress() {
		return defaultFromAddress;
	}
}

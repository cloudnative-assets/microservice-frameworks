package com.ibm.epricer.svclib.email;

import java.util.UUID;

import org.springframework.mail.javamail.MimeMessagePreparator;

public class EpricerMail {
	
	public enum MailType {
		PLAIN,
		HTML
	}
	
	private String messageId;
		
	private MimeMessagePreparator preparator;
	
	private String summary;
	
	EpricerMail() {
		this.messageId = UUID.randomUUID().toString();
	}

	MimeMessagePreparator getPreparator() {
		return preparator;
	}

	void setPreparator(MimeMessagePreparator preparator) {
		this.preparator = preparator;
	}
	
	void setSummary(String to, String cc, String bcc, String subject, String from) {
		summary = "\n" +
				 "Email Message Header:" + "\n" +
				 "MESSAGE ID: " + messageId + "\n" +
				 "MAIL FROM: " + from + "\n" + 
				 "TO RECIPIENTS: " + to + "\n" +
				 "CC RECIPIENTS: " + cc + "\n" +
				 "BCC RECIPIENTS: " + bcc + "\n" +
				 "EMAIL SUBJECT: " + subject + "\n" ;
		 
	}
	// Making them public so that consumer can get the message id
	public String getMessageId() {
		return messageId;
	}
	// Making them public so that consumer can get the summary
	public String getSummary() {
		return summary;
	}

}

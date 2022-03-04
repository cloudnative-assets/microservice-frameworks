package com.ibm.epricer.svclib.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.mail.host", havingValue="")
public class EpricerMailer {
	
	private static final Logger logger = LoggerFactory.getLogger(EpricerMailer.class);
	
	@Autowired
	private EmailConfig emailConfig;
	
	@Autowired
	private EmailServerInfo emailServer;
	
	@Autowired
	private JavaMailSender mailSender;
	
	public EmailComposer composePlainTextEmail() {
		return new PlainMailComposer(emailConfig);
	}
	
	public EmailComposer composeHtmlTextEmail() {
		return new HTMLMailComposer(emailConfig);
	}
	
	public void sendEmail(EpricerMail email) throws SendMailException {
		logger.trace("\n**************************\nSending Mail" + email.getSummary() + emailServer.info()+"\n**************************\n");
		try {
			mailSender.send(email.getPreparator());
		} catch (MailException mex) {
			logger.error("\n**************************\nCan not send mail:" + email.getSummary() + emailServer.info()+"\n**************************\n");
			throw new SendMailException("Can not send email for mail message id " + email.getMessageId() , mex);
		}
		
	}

}

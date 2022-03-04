package com.ibm.epricer.svclib.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.ibm.epricer.svclib.email.EpricerMail.MailType;

class EmailComposerImpl implements EmailComposer {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailComposerImpl.class);

	
	private String toRecipients;
	
	private String ccRecipients;
	
	private String bccRecipients;
	
	private String fromAddress;
	
	private String subject;
	
	private AttachmentResource[] attachments;
	
	private String body;
	
	private MailType mailType;
	
	
	EmailComposerImpl(MailType mailType) {
		this.mailType = mailType;
	}

	@Override
	public EmailComposer to(String toRecipients) {
		this.toRecipients = toRecipients;
		return this;
	}

	@Override
	public EmailComposer cc(String ccRecipients) {
		this.ccRecipients = ccRecipients;
		return this;
	}

	@Override
	public EmailComposer bcc(String bccRecipients) {
		this.bccRecipients = bccRecipients;
		return this;
	}

	@Override
	public EmailComposer from(String fromAddress) {
		this.fromAddress = fromAddress;
		return this;
	}

	@Override
	public EmailComposer withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	@Override
	public EmailComposer withBody(String body) {
		this.body = body;
		return this;
	}

	@Override
	public EmailComposer withAttachments(AttachmentResource[] attachments) {
		this.attachments = attachments;
		return this;
	}


	@Override
	public EmailComposer withAttachment(String fileName, DataSource dataSource) {
		AttachmentResource attachment = new AttachmentResource(fileName, dataSource);
		attachments = new AttachmentResource[1];
		attachments[0] = attachment;
		return this;
	}

	@Override
	public EpricerMail build() throws InvalidEmailException {
		EpricerMail email = new EpricerMail();
		email.setSummary(toRecipients, ccRecipients, bccRecipients, subject, fromAddress);
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper helper  = new MimeMessageHelper(mimeMessage, (mailType.equals(MailType.HTML) || (attachments!=null && attachments.length > 0)));;
				
				// From 
				helper.setFrom(fromAddress);
				
				//Subject
				if (subject !=null) {
					helper.setSubject(subject);
				} else {
					logger.warn("Email will be sent with blank subject.");
				}
				// TO
				if (toRecipients !=null) {
					helper.setTo(getEmailAddresses(toRecipients));
				} else {
					throw new InvalidEmailException("TO recipient not provided.");
				}
				
				// CC
				if (ccRecipients != null) {
					helper.setCc(getEmailAddresses(ccRecipients));
				}
				
				// BCC
				if(bccRecipients != null) {
					helper.setBcc(getEmailAddresses(bccRecipients));
				}
				
				// BODY
				helper.setText(body, mailType.equals(MailType.HTML));
				
				// Attachment
				if (attachments != null) {
					Arrays.stream(attachments).forEach(attachment -> {
						try {
							helper.addAttachment(attachment.getAttachmentName(), new InputStreamSource() {
								
								@Override
								public InputStream getInputStream() throws IOException {
									return attachment.getDataSourceInputStream();
								}
							});
						} catch (MessagingException e) {
							throw new InvalidEmailException("Can not add attachments", e);
						}
					});
				}
				
			}
		};
		
		email.setPreparator(preparator);
		
		return email;
	}
	
	protected void configureDefault(EmailConfig config) {
		this.fromAddress = config.getDefaultFromAddress();
	}
	
	private InternetAddress[] getEmailAddresses(String emailAddresses) throws AddressException {
		try {
			return InternetAddress.parse(emailAddresses);
		} catch (AddressException e) {
			throw new InvalidEmailException("Invalid email.", e);
		}
	}
	




}

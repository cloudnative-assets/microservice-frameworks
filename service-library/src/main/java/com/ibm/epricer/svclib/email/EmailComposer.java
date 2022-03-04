package com.ibm.epricer.svclib.email;

import javax.activation.DataSource;

public interface EmailComposer {
	/**
	 * 
	 * Comma separated email addresses
	 * @param toRecipients
	 * @return EmailComposer
	 */
	public EmailComposer to(String toRecipients);
	
	/**
	 * 
	 * Comma separated email addresses
	 * @param ccRecipients
	 * @return EmailComposer
	 */
	public EmailComposer cc(String ccRecipients);
		
	/**
	 * 
	 * Comma separated email addresses
	 * @param bccRecipients
	 * @return EmailComposer
	 */	
	public EmailComposer bcc(String bccRecipients);
	
	public EmailComposer from(String fromAddress);
	
	public EmailComposer withSubject(String subject);
	
	public EmailComposer withBody(String body);
	
	public EmailComposer withAttachments(AttachmentResource[] attachments);
		
	public EmailComposer withAttachment(String fileName, DataSource dataSource);
	
	public EpricerMail build() throws InvalidEmailException;

}

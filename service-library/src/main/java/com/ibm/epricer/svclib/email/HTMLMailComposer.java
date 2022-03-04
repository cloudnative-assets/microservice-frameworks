package com.ibm.epricer.svclib.email;

import com.ibm.epricer.svclib.email.EpricerMail.MailType;

class HTMLMailComposer extends EmailComposerImpl {
	
	
	HTMLMailComposer(EmailConfig emailConfig) {
		super(MailType.HTML);
		configureDefault(emailConfig);
	}
	

}

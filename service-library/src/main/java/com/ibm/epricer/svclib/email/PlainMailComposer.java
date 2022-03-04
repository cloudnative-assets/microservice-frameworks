package com.ibm.epricer.svclib.email;

import com.ibm.epricer.svclib.email.EpricerMail.MailType;

class PlainMailComposer extends EmailComposerImpl {

	PlainMailComposer(EmailConfig emailConfig) {
		super(MailType.PLAIN);
		configureDefault(emailConfig);
	}

}

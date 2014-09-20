package org.mifosplatform.finance.payments.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class KortaRequestFailureException extends AbstractPlatformDomainRuleException {

	public KortaRequestFailureException(final Long user){
		 super("error.msg.finance.payment.korta.client.not.found", "Invalid clientId Parameter");
	}
	
}

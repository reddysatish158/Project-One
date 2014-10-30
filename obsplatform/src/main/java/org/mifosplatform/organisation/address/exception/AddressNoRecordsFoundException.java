package org.mifosplatform.organisation.address.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class AddressNoRecordsFoundException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddressNoRecordsFoundException() {
		super("error.msg.billing.address.city.not.found", "City Not Found");
	}

}

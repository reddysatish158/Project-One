package org.mifosplatform.freeradius.radius.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class RadiusDetailsNotFoundException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RadiusDetailsNotFoundException() {
		super("error.msg.radius.server.details.not.found","Radius server details are not found","");
		
	}

}

package org.mifosplatform.organisation.ippool.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * 
 * @author ashokreddy
 *
 */
@SuppressWarnings("serial")
public class IpAddresNotAvailableException extends AbstractPlatformDomainRuleException {

  
    public IpAddresNotAvailableException(final String msg) {
        super("error.msg.ipaddress.are.not.available.please.select.another.iprange", "Ipaddresses are not available please select another iprange", msg);
    }

	
}

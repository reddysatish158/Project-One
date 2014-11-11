package org.mifosplatform.finance.billingorder.service;

import java.util.List;

import org.mifosplatform.finance.billingorder.commands.InvoiceTaxCommand;
import org.mifosplatform.finance.billingorder.domain.InvoiceTax;

public interface InvoiceTaxPlatformService {

	List<InvoiceTax> createInvoiceTax(List<InvoiceTaxCommand> command);

}

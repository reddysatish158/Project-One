package org.mifosplatform.finance.billingorder.service;

import java.util.List;

import org.mifosplatform.finance.billingorder.commands.BillingOrderCommand;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface BillingOrderWritePlatformService {

	//List<BillingOrder> createBillingProduct(List<BillingOrderCommand> billingOrderCommands);
	CommandProcessingResult updateBillingOrder(List<BillingOrderCommand> billingOrderCommands);
	
	void updateClientBalance(Invoice invoice,Long clientId, boolean isWalletEnable);

}

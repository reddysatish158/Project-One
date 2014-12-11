package org.mifosplatform.finance.billingmaster.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.finance.billingmaster.domain.BillDetail;
import org.mifosplatform.finance.billingmaster.domain.BillMaster;
import org.mifosplatform.finance.billingorder.data.BillDetailsData;
import org.mifosplatform.finance.financialtransaction.data.FinancialTransactionsData;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface BillWritePlatformService {
	
	CommandProcessingResult updateBillMaster(List<BillDetail> billDetails,BillMaster billMaster, BigDecimal previousBal);
	
	String generatePdf(BillDetailsData billDetails,List<FinancialTransactionsData> data);
	
	void generateStatementPdf(Long billId) throws SQLException;

	String generateInovicePdf(Long billId) throws SQLException;

	void sendInvoiceToEmail(String printFileName, Long clientId);

	Long sendStatementToEmail(BillMaster billMaster);

}

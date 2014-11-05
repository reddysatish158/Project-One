package org.mifosplatform.finance.billingorder.service;

import java.util.List;

import org.mifosplatform.finance.billingorder.commands.BillingOrderCommand;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.clientbalance.data.ClientBalanceData;
import org.mifosplatform.finance.clientbalance.domain.ClientBalance;
import org.mifosplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.mifosplatform.finance.clientbalance.service.UpdateClientBalance;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderPrice;
import org.mifosplatform.portfolio.order.domain.OrderPriceRepository;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingOrderWritePlatformServiceImplementation implements BillingOrderWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(BillingOrderWritePlatformServiceImplementation.class);

	private final PlatformSecurityContext context;
	private final OrderRepository orderRepository;
	private final UpdateClientBalance updateClientBalance;
	private final ClientBalanceRepository clientBalanceRepository;
	private final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService;
	private final OrderPriceRepository orderPriceRepository;
	
	@Autowired
	public BillingOrderWritePlatformServiceImplementation(final PlatformSecurityContext context,
			final OrderPriceRepository orderPriceRepository,
			final OrderRepository orderRepository,
			final UpdateClientBalance updateClientBalance,
			final ClientBalanceRepository clientBalanceRepository,
			final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService) {

		this.context = context;
		this.orderRepository = orderRepository;
		this.updateClientBalance = updateClientBalance;
		this.clientBalanceRepository = clientBalanceRepository;
		this.transactionHistoryWritePlatformService=transactionHistoryWritePlatformService;
		this.orderPriceRepository=orderPriceRepository;
	}

	@Transactional
	@Override
	public CommandProcessingResult updateBillingOrder(List<BillingOrderCommand> commands) {
		Order clientOrder = null;
		
		for (BillingOrderCommand billingOrderCommand : commands) {
			
			clientOrder = this.orderRepository.findOne(billingOrderCommand.getClientOrderId());
				if (clientOrder != null) {
						clientOrder.setNextBillableDay(billingOrderCommand.getNextBillableDate());
						List<OrderPrice> orderPrices = clientOrder.getPrice();
						
						for (OrderPrice orderPriceData : orderPrices) {
						
							orderPriceData.setInvoiceTillDate(billingOrderCommand.getInvoiceTillDate());
							orderPriceData.setNextBillableDay(billingOrderCommand.getNextBillableDate());
						}
				}
				this.orderRepository.save(clientOrder);
		}
	
		return new CommandProcessingResult(Long.valueOf(clientOrder.getId()));
	}

/*	@Override
	public CommandProcessingResult updateOrderPrice(
			List<BillingOrderCommand> billingOrderCommands) {
		Order orderData = null;
		for (BillingOrderCommand billingOrderCommand : billingOrderCommands) {
			orderData = this.orderRepository.findOne(billingOrderCommand
					.getClientOrderId());
			List<OrderPrice> orderPrices = orderData.getPrice();

			for (OrderPrice orderPriceData : orderPrices) {
				if ((orderPriceData.getChargeType().equalsIgnoreCase("RC")&& billingOrderCommand.getChargeType().equalsIgnoreCase("RC"))
						|| ( orderPriceData.getChargeType().equalsIgnoreCase("RC") && billingOrderCommand.getChargeType().equalsIgnoreCase("DC"))){
 
					orderPriceData.setInvoiceTillDate(billingOrderCommand
							.getInvoiceTillDate());
					orderPriceData.setNextBillableDay(billingOrderCommand
							.getNextBillableDate());
					

				} else if (orderPriceData.getChargeType().equalsIgnoreCase(
						"NRC")
						&& billingOrderCommand.getChargeType()
								.equalsIgnoreCase("NRC")) {
					orderPriceData.setInvoiceTillDate(billingOrderCommand
							.getInvoiceTillDate());
					orderPriceData.setNextBillableDay(billingOrderCommand
							.getNextBillableDate());
				}
				//this.orderPriceRepository.saveAndFlush(orderPriceData);
			}
			this.orderRepository.saveAndFlush(orderData);
		}

		return new CommandProcessingResult(Long.valueOf(orderData.getId()));
	}*/

	@Override
	public void updateClientBalance(Invoice invoice,Long clientId) {
		
		
		
		ClientBalance clientBalance = this.clientBalanceRepository.findByClientId(clientId);
		
		if(clientBalance == null){
			clientBalance =new ClientBalance(clientId,invoice.getInvoiceAmount());
		}else{
			clientBalance.setBalanceAmount(clientBalance.getBalanceAmount().add(invoice.getInvoiceAmount()));
		}

		/*if (clientBalance != null) {

			clientBalance = updateClientBalance.calculateUpdateClientBalance("DEBIT",invoice.getInvoiceAmount(),clientBalance);
		} else if (clientBalance == null) {
			clientBalance = updateClientBalance.calculateCreateClientBalance("DEBIT",invoice.getInvoiceAmount(), clientBalance,invoice.getClientId());
		}*/

		this.clientBalanceRepository.save(clientBalance);
		
	}

}

package org.mifosplatform.finance.adjustment.service;

import java.util.List;

import org.mifosplatform.finance.adjustment.domain.Adjustment;
import org.mifosplatform.finance.adjustment.domain.AdjustmentRepository;
import org.mifosplatform.finance.adjustment.serializer.AdjustmentCommandFromApiJsonDeserializer;
import org.mifosplatform.finance.clientbalance.data.ClientBalanceData;
import org.mifosplatform.finance.clientbalance.domain.ClientBalance;
import org.mifosplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.mifosplatform.finance.clientbalance.service.ClientBalanceReadPlatformService;
import org.mifosplatform.finance.clientbalance.service.UpdateClientBalance;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdjustmentWritePlatformServiceImpl implements
		AdjustmentWritePlatformService {

	private final PlatformSecurityContext context;
	private final AdjustmentRepository adjustmentRepository;
	private final ClientBalanceRepository clientBalanceRepository;
	private final UpdateClientBalance updateClientBalance;
	private final ClientBalanceReadPlatformService clientBalanceReadPlatformService;
	private final AdjustmentReadPlatformService adjustmentReadPlatformService;
	private final AdjustmentCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService;

	@Autowired
	public AdjustmentWritePlatformServiceImpl(final PlatformSecurityContext context,final AdjustmentRepository adjustmentRepository,
			final ClientBalanceRepository clientBalanceRepository,final AdjustmentCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final UpdateClientBalance updateClientBalance,final ClientBalanceReadPlatformService clientBalanceReadPlatformService,
			final AdjustmentReadPlatformService adjustmentReadPlatformService,
			final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService) {
		this.context = context;
		this.adjustmentRepository = adjustmentRepository;
		this.clientBalanceRepository = clientBalanceRepository;
		this.updateClientBalance = updateClientBalance;
		this.clientBalanceReadPlatformService = clientBalanceReadPlatformService;
		this.adjustmentReadPlatformService = adjustmentReadPlatformService;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.transactionHistoryWritePlatformService = transactionHistoryWritePlatformService;
	}

	@Transactional
	@Override
	public Long createAdjustment(final Long id2, final Long id, final Long clientid, final JsonCommand command) {
		// TODO Auto-generated method stub

		try {
			this.context.authenticatedUser();
			Adjustment adjustment = null;
			adjustment = Adjustment.fromJson(command);
			ClientBalance clientBalance = null;
			if (id != null){
				clientBalance = clientBalanceRepository.findOne(id);
			}

			if (clientBalance != null) {
			clientBalance = updateClientBalance	.doUpdateAdjustmentClientBalance(command, clientBalance);

			} else if (clientBalance == null) {
				clientBalance = updateClientBalance.createAdjustmentClientBalance(command, clientBalance);
			}

			updateClientBalance.saveClientBalanceEntity(clientBalance);

			this.adjustmentRepository.saveAndFlush(adjustment);
			transactionHistoryWritePlatformService.saveTransactionHistory(adjustment.getClientId(), "Adjustment", adjustment.getAdjustmentDate(),"AmountPaid:"+adjustment.getAmountPaid(),"AdjustmentType:"+adjustment.getAdjustmentType(),"AdjustmentCode:"+adjustment.getAdjustmentCode(),"Remarks:"+adjustment.getRemarks(),"AdjustmentID:"+adjustment.getId());
			return adjustment.getId();
		} catch (DataIntegrityViolationException dve) {
			return Long.valueOf(-1);
		}
	}

	@Override
	public CommandProcessingResult createAdjustments(final JsonCommand command) {

		try {
			context.authenticatedUser();

			this.fromApiJsonDeserializer.validateForCreate(command.json());
			final List<ClientBalanceData> clientBalancedatas = clientBalanceReadPlatformService
					.retrieveAllClientBalances(command.entityId());
			final List<ClientBalanceData> adjustmentBalancesDatas = adjustmentReadPlatformService
					.retrieveAllAdjustments(command.entityId());
			Long id = Long.valueOf(-1);
			if (clientBalancedatas.size() == 1 && adjustmentBalancesDatas.size() == 1){
				id = createAdjustment(clientBalancedatas.get(0).getId(),
						adjustmentBalancesDatas.get(0).getId(),
						command.entityId(), command);
			}
			else{
				id = createAdjustment(command.entityId(), command.entityId(),
						command.entityId(), command);
			}

			return new CommandProcessingResultBuilder()
					.withCommandId(command.commandId()).withEntityId(id).withClientId(command.entityId())
					.build();
		} catch (DataIntegrityViolationException dve) {
			return CommandProcessingResult.empty();
		}
	}
}

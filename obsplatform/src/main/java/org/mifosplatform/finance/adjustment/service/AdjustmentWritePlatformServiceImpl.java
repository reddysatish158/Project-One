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
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeAdditionalInfo;
import org.mifosplatform.organisation.office.domain.OfficeAdditionalInfoRepository;
import org.mifosplatform.organisation.partner.domain.PartnerBalance;
import org.mifosplatform.organisation.partner.domain.PartnerBalanceRepository;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
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
	private final ClientRepository clientRepository;
	private final PartnerBalanceRepository partnerBalanceRepository;
	private final OfficeAdditionalInfoRepository infoRepository;

	@Autowired
	public AdjustmentWritePlatformServiceImpl(final PlatformSecurityContext context,final AdjustmentRepository adjustmentRepository,
			final ClientBalanceRepository clientBalanceRepository,final AdjustmentCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final UpdateClientBalance updateClientBalance,final ClientBalanceReadPlatformService clientBalanceReadPlatformService,
			final AdjustmentReadPlatformService adjustmentReadPlatformService,final ClientRepository clientRepository,
			final PartnerBalanceRepository partnerBalanceRepository,final OfficeAdditionalInfoRepository infoRepository) {
		
		this.context = context;
		this.adjustmentRepository = adjustmentRepository;
		this.clientBalanceRepository = clientBalanceRepository;
		this.updateClientBalance = updateClientBalance;
		this.clientBalanceReadPlatformService = clientBalanceReadPlatformService;
		this.adjustmentReadPlatformService = adjustmentReadPlatformService;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.clientRepository = clientRepository;
		this.partnerBalanceRepository = partnerBalanceRepository;
		this.infoRepository = infoRepository;
	}


	@Transactional
	@Override
	public Long createAdjustment(final Long id2, final Long id, final Long clientId, final JsonCommand command) {
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
			
			final Client client = this.clientRepository.findOne(clientId);
			final OfficeAdditionalInfo officeAdditionalInfo = this.infoRepository.findoneByoffice(client.getOffice());
			if (officeAdditionalInfo != null) {
				if (officeAdditionalInfo.getIsCollective()) {
					System.out.println(officeAdditionalInfo.getIsCollective());
					this.updatePartnerBalance(client.getOffice(), adjustment);
				}
			}
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
			final List<ClientBalanceData> clientBalancedatas = clientBalanceReadPlatformService.retrieveAllClientBalances(command.entityId());
			final List<ClientBalanceData> adjustmentBalancesDatas = adjustmentReadPlatformService.retrieveAllAdjustments(command.entityId());
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
	
	private void updatePartnerBalance(final Office office,final Adjustment adjustment) {

		final String accountType = "ADJUSTMENTS";
		PartnerBalance partnerBalance = this.partnerBalanceRepository.findOneWithPartnerAccount(office.getId(), accountType);
		if (partnerBalance != null) {
			if(adjustment.getAdjustmentType().equalsIgnoreCase("CREDIT")){
			partnerBalance.update(adjustment.getAmountPaid().negate(), office.getId());
			}else{
				partnerBalance.update(adjustment.getAmountPaid(), office.getId());
			}

		} else {
		  if(adjustment.getAdjustmentType().equalsIgnoreCase("CREDIT")){
			partnerBalance = PartnerBalance.create(adjustment.getAmountPaid().negate(), accountType,office.getId());
		  }else{
			  partnerBalance = PartnerBalance.create(adjustment.getAmountPaid(), accountType,office.getId());
		}
	}
		this.partnerBalanceRepository.save(partnerBalance);
	}
}

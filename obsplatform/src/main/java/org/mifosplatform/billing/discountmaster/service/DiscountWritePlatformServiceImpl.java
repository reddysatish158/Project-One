package org.mifosplatform.billing.discountmaster.service;

import java.util.Map;

import org.hibernate.exception.ConstraintViolationException;
import org.mifosplatform.billing.discountmaster.domain.DiscountMaster;
import org.mifosplatform.billing.discountmaster.domain.DiscountMasterRepository;
import org.mifosplatform.billing.discountmaster.exception.DiscountMasterNotFoundException;
import org.mifosplatform.billing.discountmaster.serialization.DiscountCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hugo
 * 
 */
@Service
public class DiscountWritePlatformServiceImpl implements
		DiscountWritePlatformService {

	private final static Logger LOGGER = LoggerFactory.getLogger(DiscountWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final DiscountCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final DiscountMasterRepository discountMasterRepository;

	/**
	 * @param context
	 * @param apiJsonDeserializer
	 * @param discountMasterRepository
	 */
	@Autowired
	public DiscountWritePlatformServiceImpl(final PlatformSecurityContext context,
			final DiscountCommandFromApiJsonDeserializer apiJsonDeserializer,
			final DiscountMasterRepository discountMasterRepository) {
		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.discountMasterRepository = discountMasterRepository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * #createNewDiscount(org.mifosplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override
	public CommandProcessingResult createNewDiscount(final JsonCommand command) {

		try {

			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			DiscountMaster discountMaster = DiscountMaster.fromJson(command);
			this.discountMasterRepository.save(discountMaster);
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					        .withEntityId(discountMaster.getId()).build();
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}

	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("discountcode")) {
			final String name = command.stringValueOfParameterNamed("discountCode");
			throw new PlatformDataIntegrityException(
					"error.msg.discount.duplicate.name",
					"A discount with Code'" + name + "'already exists",
					"discountCode", name);
		}

		LOGGER.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException(
				"error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #updateDiscount(java.lang.Long,
	 * org.mifosplatform.infrastructure.core.api.JsonCommand)
	 */
	@Transactional
	@Override
	public CommandProcessingResult updateDiscount(final Long entityId,final JsonCommand command) {

		try {

			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			DiscountMaster discountMaster = discountRetrieveById(entityId);
			final Map<String, Object> changes = discountMaster.update(command);
			if (!changes.isEmpty()) {
				this.discountMasterRepository.saveAndFlush(discountMaster);
			}
			
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					       .withEntityId(discountMaster.getId()).with(changes).build();

		} catch (DataIntegrityViolationException dve) {

			if (dve.getCause() instanceof ConstraintViolationException) {
				handleCodeDataIntegrityIssues(command, dve);
			}
			return new CommandProcessingResult(Long.valueOf(-1));

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #deleteDiscount(java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult deleteDiscount(final Long entityId) {

		DiscountMaster discountMaster = null;

		try {
			this.context.authenticatedUser();
			discountMaster = this.discountRetrieveById(entityId);
			discountMaster.delete();
			this.discountMasterRepository.save(discountMaster);
			return new CommandProcessingResult(entityId);

		} catch (final DataIntegrityViolationException dve) {
			throw new PlatformDataIntegrityException(
					"error.msg.could.unknown.data.integrity.issue",
					"Unknown data integrity issue with resource: "
							+ dve.getMessage());
		}

	}

	private DiscountMaster discountRetrieveById(final Long entityId) {

		DiscountMaster discountMaster = this.discountMasterRepository.findOne(entityId);
		if (discountMaster == null) {
			throw new DiscountMasterNotFoundException(entityId);
		}
		return discountMaster;
	}
}

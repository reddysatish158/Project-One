package org.mifosplatform.organisation.voucher.service;

import java.text.ParseException;

import org.apache.commons.lang.RandomStringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.voucher.domain.Voucher;
import org.mifosplatform.organisation.voucher.domain.VoucherDetails;
import org.mifosplatform.organisation.voucher.domain.VoucherDetailsRepository;
import org.mifosplatform.organisation.voucher.domain.VoucherRepository;
import org.mifosplatform.organisation.voucher.exception.AlreadyProcessedException;
import org.mifosplatform.organisation.voucher.serialization.VoucherCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author ashokreddy
 *
 */
@Service
public class VoucherWritePlatformServiceImpl implements
		VoucherWritePlatformService {
	
	private int remainingKeyLength;
	private char status = 0;
	private Voucher voucher;
	private String generatedKey;
	private Long quantity;
	private String type;
	private char enable = 'N';
	

	private static final String ALPHA = "Alpha";
	private static final String NUMERIC = "Numeric";
	private static final String ALPHANUMERIC = "AlphaNumeric";
	
	private final PlatformSecurityContext context;
	private final VoucherRepository randomGeneratorRepository;
	private final VoucherDetailsRepository randomGeneratorDetailsRepository;
	private final VoucherCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final VoucherReadPlatformService randomGeneratorReadPlatformService;
	
	@Autowired
	public VoucherWritePlatformServiceImpl(
			final PlatformSecurityContext context,
			final VoucherRepository randomGeneratorRepository,
			final VoucherReadPlatformService randomGeneratorReadPlatformService,
			final VoucherCommandFromApiJsonDeserializer fromApiJsonDeserializer,
			final VoucherDetailsRepository randomGeneratorDetailsRepository) {
		
		this.context = context;
		this.randomGeneratorRepository = randomGeneratorRepository;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.randomGeneratorReadPlatformService = randomGeneratorReadPlatformService;
		this.randomGeneratorDetailsRepository=randomGeneratorDetailsRepository;

	}

	@Transactional
	@Override
	public CommandProcessingResult createRandomGenerator(final JsonCommand command) {
		
		try {
			context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForCreate(command.json());
			Voucher voucherpin = Voucher.fromJson(command);
			this.randomGeneratorRepository.save(voucherpin);	
			return new CommandProcessingResult(voucherpin.getId());

		}  catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return CommandProcessingResult.empty();
		}  catch (ParseException e) {
			return CommandProcessingResult.empty();
		}
		
	}
	
	@Transactional
	@Override
	public CommandProcessingResult generateVoucherPinKeys(final Long batchId) {
			
		try{
			
			voucher = this.randomGeneratorRepository.findOne(batchId);
			
			if(voucher == null){
				
				throw new PlatformDataIntegrityException("error.msg.code.batchId.not.found", "VoucherBatch with id :'" + batchId + "'does not exists", "batchId", batchId);
				
			} 
			
			if(voucher.getIsProcessed() == enable){
				
				status = 'F';
				
				final Long voucherId = generateRandomNumbers();
				
				status = 'Y';
				
				return new CommandProcessingResult(voucherId);
				
			} else{	
				
				throw new AlreadyProcessedException("VoucherPin Already Generated with this " + voucher.getBatchName());
				
			}
			
		} finally{
			
			if(voucher != null && voucher.getIsProcessed() == 'N'){
				
				voucher.setIsProcessed(status);
				
				this.randomGeneratorRepository.save(voucher);
				
			}
			
		}
		
		
		
	}

	public Long generateRandomNumbers() {
		
		final Long lengthofVoucher = voucher.getLength();
		
		final int length = (int)lengthofVoucher.longValue();
		
		
		quantity = voucher.getQuantity();
		
		type = voucher.getPinCategory();
		
		final int beginKeyLength = voucher.getBeginWith().length();
		
		remainingKeyLength = length - beginKeyLength;
		
		final Long serialNo = voucher.getSerialNo();
		
		String minSerialSeries = "";
		String maxSerialSeries = "";
		
		for (int serialNoValidator = 0; serialNoValidator < serialNo; serialNoValidator++) {
			
			if (serialNoValidator > 0) {
				minSerialSeries = minSerialSeries + "0";
				maxSerialSeries = maxSerialSeries + "9";
			} else {
				minSerialSeries = minSerialSeries + "1";
				maxSerialSeries = maxSerialSeries + "9";
			}
		}

		final Long minNo = Long.parseLong(minSerialSeries);
		final Long maxNo = Long.parseLong(maxSerialSeries);

		long currentSerialNumber = this.randomGeneratorReadPlatformService.retrieveMaxNo(minNo, maxNo);

		if (currentSerialNumber == 0) {
			currentSerialNumber = minNo;
		}

		return randomValueGeneration(currentSerialNumber);

	}
	
	private Long randomValueGeneration(Long currentSerialNumber) {
		
		int quantityValidator;

		for (quantityValidator = 0; quantityValidator < quantity; quantityValidator++) {
			
			String name = voucher.getBeginWith() + generateRandomSingleCode();

			String value = this.randomGeneratorReadPlatformService.retrieveIndividualPin(name);
			
			if (value == null) {
				
				currentSerialNumber = currentSerialNumber + 1;
				
				VoucherDetails voucherDetails = new VoucherDetails(name, currentSerialNumber, voucher);
				
				this.randomGeneratorDetailsRepository.save(voucherDetails);

			} else {
				quantityValidator = quantityValidator - 1;
			}

		}

		return voucher.getId();
	}
	
	private String generateRandomSingleCode() {
		
		if (type.equalsIgnoreCase(ALPHA)) {			
			generatedKey = RandomStringUtils.randomAlphabetic(remainingKeyLength);			
		} 
		
		if (type.equalsIgnoreCase(NUMERIC)) {
			generatedKey = RandomStringUtils.randomNumeric(remainingKeyLength);
		}
		
		if (type.equalsIgnoreCase(ALPHANUMERIC)) {
			generatedKey = RandomStringUtils.randomAlphanumeric(remainingKeyLength);
		}
		
		return generatedKey;
		
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("batch_name")) {
			final String name = command
					.stringValueOfParameterNamed("batchName");
			throw new PlatformDataIntegrityException("error.msg.code.duplicate.batchname", "A batch with name'"
							+ name + "'already exists", "displayName", name);
		}
		if (realCause.getMessage().contains("serial_no_key")) {
			throw new PlatformDataIntegrityException(
					"error.msg.code.duplicate.serial_no_key", "A serial_no_key already exists", "displayName", "serial_no");
		}

		throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: " + realCause.getMessage());
	}

}

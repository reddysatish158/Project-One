package org.mifosplatform.logistics.onetimesale.service;

import java.math.BigDecimal;
import java.util.List;

import org.mifosplatform.billing.chargecode.data.ChargesData;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.billing.discountmaster.service.DiscountReadPlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.logistics.item.domain.ItemMaster;
import org.mifosplatform.logistics.item.domain.ItemRepository;
import org.mifosplatform.logistics.item.service.ItemReadPlatformService;
import org.mifosplatform.logistics.itemdetails.service.ItemDetailsWritePlatformService;
import org.mifosplatform.logistics.onetimesale.data.OneTimeSaleData;
import org.mifosplatform.logistics.onetimesale.domain.OneTimeSale;
import org.mifosplatform.logistics.onetimesale.domain.OneTimeSaleRepository;
import org.mifosplatform.logistics.onetimesale.serialization.OneTimesaleCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.transactionhistory.service.TransactionHistoryWritePlatformService;
import org.mifosplatform.workflow.eventvalidation.service.EventValidationReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author hugo
 *
 */
@Service
public class OneTimeSaleWritePlatformServiceImpl implements OneTimeSaleWritePlatformService {
	
	
	private final static Logger LOGGER = LoggerFactory.getLogger(OneTimeSaleWritePlatformServiceImpl.class);
	private final FromJsonHelper fromJsonHelper;
	private final PlatformSecurityContext context;
	private final ItemRepository itemMasterRepository;
	private final OneTimesaleCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final InvoiceOneTimeSale invoiceOneTimeSale;
	private final OneTimeSaleRepository oneTimeSaleRepository;
	private final ItemReadPlatformService itemReadPlatformService;
	private final DiscountReadPlatformService discountReadPlatformService;
	private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	private final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService;
	private final EventValidationReadPlatformService eventValidationReadPlatformService;

	@Autowired
	public OneTimeSaleWritePlatformServiceImpl(final PlatformSecurityContext context,final OneTimeSaleRepository oneTimeSaleRepository,
			final ItemRepository itemMasterRepository,final OneTimesaleCommandFromApiJsonDeserializer apiJsonDeserializer,
			final InvoiceOneTimeSale invoiceOneTimeSale,final ItemReadPlatformService itemReadPlatformService,
			final FromJsonHelper fromJsonHelper,final TransactionHistoryWritePlatformService transactionHistoryWritePlatformService,
			final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService,
			final EventValidationReadPlatformService eventValidationReadPlatformService,final DiscountReadPlatformService discountReadPlatformService) {

		
		this.context = context;
		this.fromJsonHelper = fromJsonHelper;
		this.invoiceOneTimeSale = invoiceOneTimeSale;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.itemMasterRepository = itemMasterRepository;
		this.oneTimeSaleRepository = oneTimeSaleRepository;
		this.itemReadPlatformService = itemReadPlatformService;
		this.discountReadPlatformService = discountReadPlatformService;
		this.oneTimeSaleReadPlatformService = oneTimeSaleReadPlatformService;
		this.inventoryItemDetailsWritePlatformService = inventoryItemDetailsWritePlatformService;
		this.eventValidationReadPlatformService = eventValidationReadPlatformService;

	}

	/* (non-Javadoc)
	 * @see #createOneTimeSale(org.mifosplatform.infrastructure.core.api.JsonCommand, java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult createOneTimeSale(final JsonCommand command,final Long clientId) {

		try {
			
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			final JsonElement element = fromJsonHelper.parse(command.json());
			final Long itemId = command.longValueOfParameterNamed("itemId");
			ItemMaster item = this.itemMasterRepository.findOne(itemId);

			// Check for Custome_Validation

			this.eventValidationReadPlatformService.checkForCustomValidations(clientId, "Rental",command.json());
			final OneTimeSale oneTimeSale = OneTimeSale.fromJson(clientId, command,item);



			this.oneTimeSaleRepository.saveAndFlush(oneTimeSale);
			final List<OneTimeSaleData> oneTimeSaleDatas = this.oneTimeSaleReadPlatformService.retrieveOnetimeSalesForInvoice(clientId);
			JsonObject jsonObject = new JsonObject();
			final String saleType = command.stringValueOfParameterNamed("saleType");
			if (saleType.equalsIgnoreCase("SALE")) {
				for (OneTimeSaleData oneTimeSaleData : oneTimeSaleDatas) {
					this.invoiceOneTimeSale.invoiceOneTimeSale(clientId,oneTimeSaleData);
					updateOneTimeSale(oneTimeSaleData);
				}
			}
			
			JsonArray serialData = fromJsonHelper.extractJsonArrayNamed("serialNumber", element);
			for (JsonElement je : serialData) {
				JsonObject serialNumber = je.getAsJsonObject();
				serialNumber.addProperty("clientId", oneTimeSale.getClientId());
				serialNumber.addProperty("orderId", oneTimeSale.getId());
			}
			jsonObject.addProperty("itemMasterId", oneTimeSale.getItemId());
			jsonObject.addProperty("quantity", oneTimeSale.getQuantity());
			jsonObject.add("serialNumber", serialData);
			JsonCommand jsonCommand = new JsonCommand(null,jsonObject.toString(), element, fromJsonHelper, null, null,
					null, null, null, null, null, null, null, null, null, null);
			this.inventoryItemDetailsWritePlatformService.allocateHardware(jsonCommand);
			return new CommandProcessingResult(Long.valueOf(oneTimeSale.getId()), clientId);
		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {
		
		LOGGER.error(dve.getMessage(), dve);
		final Throwable realCause=dve.getMostSpecificCause();
		throw new PlatformDataIntegrityException(
				"error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());
		

	}

	public void updateOneTimeSale(final OneTimeSaleData oneTimeSaleData) {

		OneTimeSale oneTimeSale = oneTimeSaleRepository.findOne(oneTimeSaleData.getId());
		oneTimeSale.setIsInvoiced('y');
		oneTimeSaleRepository.save(oneTimeSale);

	}

	/* (non-Javadoc)
	 * @see #calculatePrice(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonQuery)
	 */
	@Override
	public ItemData calculatePrice(final Long itemId, final JsonQuery query) {

		try {
			
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForPrice(query.parsedJson());
			final Integer quantity = fromJsonHelper.extractIntegerWithLocaleNamed("quantity",query.parsedJson());
			final BigDecimal unitprice = fromJsonHelper.extractBigDecimalWithLocaleNamed("unitPrice",query.parsedJson());
			BigDecimal itemprice = null;
			ItemMaster itemMaster = this.itemMasterRepository.findOne(itemId);
		    if (unitprice != null) {
				itemprice = unitprice;
			} else {
				itemprice = itemMaster.getUnitPrice();
			}
			BigDecimal totalPrice = itemprice.multiply(new BigDecimal(quantity));
			List<ItemData> itemCodeData = this.oneTimeSaleReadPlatformService.retrieveItemData();
			List<DiscountMasterData> discountdata = this.discountReadPlatformService.retrieveAllDiscounts();
			ItemData itemData = this.itemReadPlatformService.retrieveSingleItemDetails(itemId);
			List<ChargesData> chargesDatas = this.itemReadPlatformService.retrieveChargeCode();

			return new ItemData(itemCodeData, itemData, totalPrice, quantity,discountdata, chargesDatas);
		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return null;

		}
	}

	/* (non-Javadoc)
	 * @see #deleteOneTimeSale(org.mifosplatform.infrastructure.core.api.JsonCommand, java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult deleteOneTimeSale(final Long entityId) {

		OneTimeSale oneTimeSale = null;
		try {
			oneTimeSale = oneTimeSaleRepository.findOne(entityId);
			oneTimeSale.setIsDeleted('Y');
			this.oneTimeSaleRepository.save(oneTimeSale);

		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
		}
		return new CommandProcessingResult(Long.valueOf(oneTimeSale.getId()),
				oneTimeSale.getClientId());
	}
}
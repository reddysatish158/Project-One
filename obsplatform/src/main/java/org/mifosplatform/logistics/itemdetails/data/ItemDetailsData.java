package org.mifosplatform.logistics.itemdetails.data;

import java.util.Collection;

import org.mifosplatform.organisation.mcodevalues.data.MCodeData;


public class ItemDetailsData {
	
	private final Collection<InventoryGrnData> inventoryGrnDatas;
	private final Collection<MCodeData> qualityDatas;
	private final Collection<MCodeData> statusDatas;
	private final Long id;
	private final Long itemMasterId; 
	private final String serialNumber;
	private final Long grnId;
	private final String provisioningSerialNumber;
	private final String quality;
	private final String status;
	private final Long officeId;
	private final Long clientId;
	private final Long warranty;
	private final String remarks;
	private final String itemDescription;
	private final String supplier;
	private final String officeName;
	private final String accountNumber;

	public ItemDetailsData(Collection<InventoryGrnData> inventoryGrnData,Collection<MCodeData> qualityDatas,Collection<MCodeData> statusDatas,
			String serialNumber, String provisionSerialNumber) {
		
		this.inventoryGrnDatas=inventoryGrnData;
		this.qualityDatas=qualityDatas;
		this.statusDatas=statusDatas;
		this.officeId=null;
		this.id=null;
		this.itemMasterId=null;
		this.serialNumber=serialNumber;
		this.grnId=null;
		this.provisioningSerialNumber=provisionSerialNumber;
		this.quality=null;
		this.status=null;
		this.warranty=null;
		this.remarks=null;
		this.itemDescription = null;
		this.supplier = null;
		this.clientId = null;
		this.officeName = null;
		this.accountNumber = null;
		
		
	}

	public ItemDetailsData(final Long id,final  Long itemMasterId,final String serialNumber,final Long grnId,final  String provisioningSerialNumber,final  String quality,
			final String status,final Long warranty,final String remarks,final String itemDescription,final String supplier,final Long clientId,final String officeName, 
			final String accountNumber) {
		
		this.id=id;
		this.itemMasterId=itemMasterId;
		this.serialNumber=serialNumber;
		this.grnId=grnId;
		this.provisioningSerialNumber=provisioningSerialNumber;
		this.quality=quality;
		this.status=status;
		this.warranty=warranty;
		this.remarks=remarks;
		this.itemDescription = itemDescription;
		this.officeId=null;
		this.supplier = supplier;
		this.clientId = clientId;
		this.officeName = officeName;
		this.accountNumber = accountNumber;
		this.inventoryGrnDatas=null;
		this.qualityDatas=null;
		this.statusDatas=null;
	}

	public Collection<InventoryGrnData> getInventoryGrnDatas() {
		return inventoryGrnDatas;
	}

	public Collection<MCodeData> getQualityDatas() {
		return qualityDatas;
	}

	public Collection<MCodeData> getStatusDatas() {
		return statusDatas;
	}

	public Long getId() {
		return id;
	}

	public Long getItemMasterId() {
		return itemMasterId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public Long getGrnId() {
		return grnId;
	}

	public String getProvisioningSerialNumber() {
		return provisioningSerialNumber;
	}

	public String getQuality() {
		return quality;
	}

	public String getStatus() {
		return status;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public Long getClientId() {
		return clientId;
	}

	public Long getWarranty() {
		return warranty;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public String getSupplier() {
		return supplier;
	}

	public String getOfficeName() {
		return officeName;
	}

	public String getAccountNumber() {
		return accountNumber;
	}
	
	
	
}

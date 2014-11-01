package org.mifosplatform.logistics.mrn.data;

import org.joda.time.LocalDate;

public class InventoryTransactionHistoryData {
	
	private final Long id;
	private final LocalDate transactionDate;
	private final Long mrnId;
	private final String serialNumber;
	private final String itemDescription;
	private final String fromOffice;
	private final String toOffice;
	private final String refType;
	private final String movement;
	
	public InventoryTransactionHistoryData(final Long id,final LocalDate transactionDate, final Long mrnId, final String itemDescription, 
			final String fromOffice, final String toOffice, final String serialNumber, final String refType, final String movement){
		
		this.id = id;
		this.transactionDate = transactionDate;
		this.mrnId = mrnId;
		this.itemDescription = itemDescription;
		this.fromOffice = fromOffice;
		this.toOffice = toOffice;
		this.serialNumber = serialNumber;
		this.refType = refType;
		this.movement = movement;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public Long getMrnId() {
		return mrnId;
	}

	
	public String getItemDescription() {
		return itemDescription;
	}



	public String getFromOffice() {
		return fromOffice;
	}

	

	public String getToOffice() {
		return toOffice;
	}

	

	public Long getId() {
		return id;
	}

	

	public String getSerialNumber() {
		return serialNumber;
	}

	

	public String getRefType() {
		return refType;
	}

	

	public String getMovement() {
		return movement;
	}


	
}

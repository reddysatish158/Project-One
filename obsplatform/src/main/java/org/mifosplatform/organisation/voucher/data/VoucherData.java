package org.mifosplatform.organisation.voucher.data;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.office.data.OfficeData;

/**
 * The class <code>VoucherData</code> is a Bean class, contains only getter and setter methods to store and retrieve data.
 *
 *  @author ashokreddy
 */

public class VoucherData {
	
	private Long id;
	private String batchName;
	private Long officeId;
	private Long length;
	private String pinCategory;
	private String pinType;
	private Long quantity;
	private String serialNo;
	private LocalDate expiryDate;
	private String beginWith;
	private String pinValue;
    private List<EnumOptionData> pinCategoryData;
	private List<EnumOptionData> pinTypeData;
	private String isProcessed;
	private String planCode;
	private Collection<OfficeData> offices;
	private Long priceId;
	private String pinNo;
	private String status;
	private Long clientId;

	public VoucherData(final String batchName, final Long officeId,
			final Long length, final String pinCategory, final String pinType, final Long quantity,
			final String serial, final Date expiryDate, final String beginWith,
			final String pinValue, final Long id, final String planCode, final String isProcessed, 
			final String pinNo, final String status, final Long clientId) {

		this.batchName=batchName;
		this.officeId=officeId;
		this.length=length;
		this.pinCategory=pinCategory;
		this.pinType=pinType;
		this.quantity=quantity;
		this.beginWith=beginWith;
		this.serialNo=serial;
		this.expiryDate=new LocalDate(expiryDate);
		this.pinValue=pinValue;
		this.id=id;
		this.planCode=planCode;
		this.isProcessed=isProcessed;
		this.pinNo = pinNo;
		this.status = status;
		this.clientId = clientId;
	}

	/**
	 * Default/Zero-Parameterized Constructor.
	 */
	public VoucherData() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param pinCategoryData
	 * 			value containg the List of VoucherCategory types.
	 *  Category Like ALPHA/NUMERIC/ALPHANUMERIC
	 *  
	 * @param pinTypeData
	 * 			value containg the List of VoucherPin types. Like VALUE and PRODUCT
	 * @param offices 
	 */
	public VoucherData(final List<EnumOptionData> pinCategoryData,
			final List<EnumOptionData> pinTypeData, Collection<OfficeData> offices) {
		
		this.pinCategoryData=pinCategoryData;
		this.pinTypeData=pinTypeData;
		this.offices = offices;
		
	}

	public VoucherData(String pinType, String pinValue, Date expiryDate) {
		
		this.pinType = pinType;
		this.pinValue = pinValue;
		this.expiryDate = new LocalDate(expiryDate);
		
	}

	public VoucherData(Long id,String batchName) {
		
		this.id = id;
		this.batchName = batchName;
	}
	public VoucherData(Long priceId) {
		this.priceId = priceId;
	}

	public List<EnumOptionData> getPinCategoryData() {
		return pinCategoryData;
	}

	public void setPinCategoryData(List<EnumOptionData> pinCategoryData) {
		this.pinCategoryData = pinCategoryData;
	}

	public List<EnumOptionData> getPinTypeData() {
		return pinTypeData;
	}

	public void setPinTypeData(List<EnumOptionData> pinTypeData) {
		this.pinTypeData = pinTypeData;
	}

	public String getBatchName() {
		return batchName;
	}
	

	public Long getId() {
		return id;
	}

	public String getBeginWith() {
		return beginWith;
	}
    
	public String getPinValue() {
		return pinValue;
	}

	public void setPinValue(String pinValue) {
		this.pinValue = pinValue;
	}

	public void setBeginWith(String beginWith) {
		this.beginWith = beginWith;
	}

	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public String getPinCategory() {
		return pinCategory;
	}

	public void setPinCategory(String pinCategory) {
		this.pinCategory = pinCategory;
	}

	public String getPinType() {
		return pinType;
	}

	public void setPinType(String pinType) {
		this.pinType = pinType;
	}

	public Long getQuantity() {
		return quantity;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public String getIsProcessed() {
		return isProcessed;
	}

	public void setIsProcessed(String isProcessed) {
		this.isProcessed = isProcessed;
	}

	public String getPlanCode() {
		return planCode;
	}

	public void setPlanCode(String planCode) {
		this.planCode = planCode;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public Collection<OfficeData> getOffices() {
		return offices;
	}


}

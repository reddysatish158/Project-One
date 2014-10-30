package org.mifosplatform.organisation.voucher.domain;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Entity class, Used to Store the Voucher Group/Batch details from b_pin_master table.
 * 
 * @author ashokreddy
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "b_pin_master", uniqueConstraints = @UniqueConstraint(name = "batch_name", columnNames = { "batch_name" }))
public class Voucher extends  AbstractPersistable<Long>  {
	
	
	@Column(name = "batch_name", nullable = false)
	private String batchName;
	
	@Column(name = "batch_description", nullable = false)
	private String batchDescription;

	@Column(name = "length", nullable = false)
	private Long length;

	@Column(name = "begin_with")
	private String beginWith;

	@Column(name = "pin_category", nullable = false)
	private String pinCategory;

	@Column(name = "quantity", nullable = false)
	private Long quantity;
	
	@Column(name = "serial_no")
	private Long serialNo;

	@Column(name = "pin_type")
	private String pinType;
	
	@Column(name = "pin_value")
	private String pinValue;

	@Column(name = "expiry_date")
	private Date expiryDate;
	
	@Column(name = "is_processed")
	private char isProcessed;
	
	/*@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "randomGenerator", orphanRemoval = true)
	private List<RandomGeneratorDetails> randomGeneratorDetails = new ArrayList<RandomGeneratorDetails>();*/
	
	/**
	 * Default/Zero-Parameerized Constructor. 
	 */
	public Voucher() {
		super();
	}

	public Voucher(final String batchName, final String batchDescription,
			final Long length, final String beginWith,
			final String pinCategory, final Long quantity, final Long serialNo,
			final String pinType, final String pinValue, final Date date) {

		super();
		this.batchName = batchName;
		this.batchDescription = batchDescription;
		this.length = length;
		this.beginWith = beginWith;
		this.pinCategory = pinCategory;
		this.quantity = quantity;
		this.serialNo = serialNo;
		this.pinType = pinType;
		this.pinValue = pinValue;
		this.expiryDate = date;
		this.isProcessed = 'N';

	}

	/**
	 * Using this method, we can get the input data from JsonCommand object.
	 * @param command
	 * @return
	 * @throws ParseException
	 */
	public static Voucher fromJson(final JsonCommand command) throws ParseException {
		  	final String batchName = command.stringValueOfParameterNamed("batchName");
		    final String batchDescription = command.stringValueOfParameterNamed("batchDescription");
		    final BigDecimal length = command.bigDecimalValueOfParameterNamed("length");
		    final String beginWith = command.stringValueOfParameterNamed("beginWith");
		    final String pinCategory = command.stringValueOfParameterNamed("pinCategory");
		    final BigDecimal quantity = command.bigDecimalValueOfParameterNamed("quantity");
		    final BigDecimal serialNo = command.bigDecimalValueOfParameterNamed("serialNo");
		    final String pinType = command.stringValueOfParameterNamed("pinType");
		    final Long pinVal = command.longValueOfParameterNamed("pinValue");
		    final LocalDate expiryDate = command.localDateValueOfParameterNamed("expiryDate");
		    final String pinValue=String.valueOf(pinVal);
		    return new Voucher(batchName,batchDescription,length.longValue(),beginWith,pinCategory,quantity.longValue(),serialNo.longValue(),pinType,pinValue,expiryDate.toDate());
	}

	public String getBatchName() {
		return batchName;
	}


	public String getBatchDescription() {
		return batchDescription;
	}


	public Long getLength() {
		return length;
	}


	public String getBeginWith() {
		return beginWith;
	}


	public String getPinCategory() {
		return pinCategory;
	}


	public Long getQuantity() {
		return quantity;
	}


	public Long getSerialNo() {
		return serialNo;
	}


	public String getPinType() {
		return pinType;
	}


	public String getPinValue() {
		return pinValue;
	}


	public Date getExpiryDate() {
		return expiryDate;
	}


	public char getIsProcessed() {
		return isProcessed;
	}


	public void setIsProcessed(char isProcessed) {
		this.isProcessed = isProcessed;
	}

}

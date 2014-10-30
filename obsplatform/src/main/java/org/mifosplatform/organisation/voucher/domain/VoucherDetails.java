package org.mifosplatform.organisation.voucher.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Entity class, Used to Store the 
 * VoucherPin details to b_pin_details table.
 * 
 * @author ashokreddy
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "b_pin_details")
public class VoucherDetails extends AbstractPersistable<Long> {
	
	@ManyToOne
    @JoinColumn(name="pin_id", nullable = false)
    private Voucher voucher;
	
	@Column(name = "pin_no")
	private String pinNo;
	
	@Column(name = "serial_no", nullable = false)
	private Long serialNo;
	
	@Column(name = "client_id", nullable = true)
	private Long clientId;
	
	/**
	 * Default/Zero-Parameterized Constructor
	 */
	public VoucherDetails(){
		
	}

	/**
	 * Constructor, Used to assign the values 
	 * To entity class to Store Record in Database
	 * @param voucherpin
	 * 			Randomly Generated Voucher Pin
	 * @param serialNo
	 * 			Serial Number of the Generated Voucher Pin. 
	 * 			To Identify the Record in a DB.
	 * @param voucher
	 * 			Voucher(b_pin_master) class object, 
	 * 			This Voucher Object id Act as foreign reference
	 */
	public VoucherDetails(final String voucherpin, final Long serialNo, final Voucher voucher) {
		super();
		this.pinNo = voucherpin;
		this.serialNo = serialNo;
		this.voucher = voucher;
	}

	public String getPinNo() {
		return pinNo;
	}

	public Long getSerialNo() {
		return serialNo;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setPinNo(String pinNo) {
		this.pinNo = pinNo;
	}

	public void setSerialNo(Long serialNo) {
		this.serialNo = serialNo;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Voucher getVoucher() {
		return voucher;
	}

	public void setVoucher(Voucher voucher) {
		this.voucher = voucher;
	}	
	
	

}

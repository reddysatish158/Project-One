package org.mifosplatform.organisation.office.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_office_additional_info")
public class OfficeAdditionalInfo extends AbstractPersistable<Long> {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	@Column(name = "partner_name")
	private String partnerName;

	@Column(name = "partner_type")
	private String partnerType;
	
	@Column(name = "partner_currency")
	private String partnerCurrency;

	@OneToOne
	@JoinColumn(name = "office_id", insertable = true, updatable = true, nullable = true, unique = true)
	private Office office;

	public OfficeAdditionalInfo() {

	}
	
	public OfficeAdditionalInfo(final Office office,final String partnerName, final String partnerType,final String currency ) {
		
		this.office = office;
		this.partnerName = partnerName;
		this.partnerType = partnerType;
		this.partnerCurrency = currency;
	}


	public String getPartnerName() {
		return partnerName;
	}

	public String getPartnerType() {
		return partnerType;
	}

	public String getPartnerCurrency() {
		return partnerCurrency;
	}

	public Office getOffice() {
		return office;
	}

	public void setOffice(Office office) {
		this.office = office;
	}
	
		
}

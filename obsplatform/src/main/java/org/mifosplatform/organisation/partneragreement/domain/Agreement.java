package org.mifosplatform.organisation.partneragreement.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "b_agreement")
public class Agreement extends AbstractAuditableCustom<AppUser, Long> {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	@Column(name = "partner_account_id")
	private Long partnerId;

	@Column(name = "agreement_status")
	private String agreementStatus;

	@Column(name = "start_date")
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "is_deleted")
	private char isDeleted;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "agreements", orphanRemoval = true)
	private List<AgreementDetails> details = new ArrayList<AgreementDetails>();

	public Agreement() {

	}

	public static Agreement fromJosn(final JsonCommand command) {
		
		final String agreementStatus=command.stringValueOfParameterNamed("agreementStatus");
		final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
		final LocalDate endDate = command.localDateValueOfParameterNamed("endDate");
		final Long partnerId = command.entityId();
		return new Agreement(partnerId,agreementStatus,startDate,endDate);
	}
	
	public Agreement(final Long partnerId,final String agreementStatus, final LocalDate startDate,final LocalDate endDate) {
		
		this.partnerId = partnerId;
		this.agreementStatus =agreementStatus;
		this.startDate = startDate.toDate();
		this.endDate =endDate.toDate();
		this.isDeleted = 'N';
	}

	public Long getPartnerId() {
		return partnerId;
	}

	public String getAgreementStatus() {
		return agreementStatus;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public char getIsDeleted() {
		return isDeleted;
	}

	public List<AgreementDetails> getDetails() {
		return details;
	}

	public void setPartnerId(Long partnerId) {
		this.partnerId = partnerId;
	}

	public void setAgreementStatus(String agreementStatus) {
		this.agreementStatus = agreementStatus;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void setDetails(List<AgreementDetails> details) {
		this.details = details;
	}

	public void addAgreementDetails(AgreementDetails detail) {
		detail.update(this);
		this.details.add(detail);
		
	}


}

package org.mifosplatform.organisation.partneragreement.domain;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_office_agreement_detail", uniqueConstraints = @UniqueConstraint(columnNames = {
		"agreement_id", "source" }, name = "agreement_dtl_ai_ps_mc_uniquekey"))
public class AgreementDetails extends AbstractAuditableCustom<AppUser, Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "agreement_id", insertable = true, updatable = true, nullable = true, unique = true)
	private Agreement agreements;

	@Column(name = "source")
	private Long sourceType;

	@Column(name = "share_amount")
	private BigDecimal shareAmount;

	@Column(name = "share_type")
	private String shareType;

	@Column(name = "status")
	private Integer status;

	public AgreementDetails() {

	}

	public AgreementDetails(final Long source, final String shareType, final BigDecimal shareAmount, final Long status) {
		
		this.sourceType = source;
		this.shareType = shareType;
		this.shareAmount =shareAmount;
		this.status = Integer.valueOf(status.toString());
		
	}

	public Agreement getAgreements() {
		return agreements;
	}

	public Long getSourceType() {
		return sourceType;
	}

	public BigDecimal getShareAmount() {
		return shareAmount;
	}

	public String getShareType() {
		return shareType;
	}

	public Integer getStatus() {
		return status;
	}


	public void setSourceType(Long sourceType) {
		this.sourceType = sourceType;
	}

	public void setShareAmount(BigDecimal shareAmount) {
		this.shareAmount = shareAmount;
	}

	public void setShareType(String shareType) {
		this.shareType = shareType;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public void update (final Agreement agreement){
		this.agreements = agreement;
	}

}

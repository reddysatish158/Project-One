package org.mifosplatform.organisation.partneragreement.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;

public class AgreementData {
	
	private Long id;
	private String agreementStatus;
	private LocalDate startDate;
	private LocalDate endDate;
	private String shareType;
	private BigDecimal shareAmount;
	private String source;
	private EnumOptionData status;
	private Collection<MCodeData> shareTypes;
	private Collection<MCodeData> sourceData;
	private List<EnumOptionData> statusData;
	private Collection<MCodeData> agreementTypes;


	public AgreementData(Collection<MCodeData> shareTypes,Collection<MCodeData> sourceData, 
			List<EnumOptionData> statusData, Collection<MCodeData> agreementTypes) {

		this.shareTypes = shareTypes;
		this.statusData = statusData;
		this.sourceData = sourceData;
		this.agreementTypes = agreementTypes;

	}


	public AgreementData(Long id, String agreementStatus,  LocalDate startDate,
			 LocalDate endDate,  String shareType, BigDecimal shareAmount,String source, EnumOptionData status) {
		
		this.id=id;
		this.agreementStatus = agreementStatus;
		this.startDate = startDate;
		this.endDate = endDate;
		this.shareType = shareType;
		this.shareAmount = shareAmount;
		this.source = source;
		this.status = status;
		
	}

	public Long getId() {
		return id;
	}

	public String getAgreementStatus() {
		return agreementStatus;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public String getShareType() {
		return shareType;
	}

	public BigDecimal getShareAmount() {
		return shareAmount;
	}

	public String getSource() {
		return source;
	}

	public EnumOptionData getStatus() {
		return status;
	}

	public Collection<MCodeData> getShareTypes() {
		return shareTypes;
	}

	public Collection<MCodeData> getSourceData() {
		return sourceData;
	}

	public List<EnumOptionData> getStatusData() {
		return statusData;
	}

	public Collection<MCodeData> getAgreementTypes() {
		return agreementTypes;
	}
	
}

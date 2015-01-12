package org.mifosplatform.organisation.partneragreement.data;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;

public class AgreementData {

	private final Collection<MCodeData> shareTypes;
	private final Collection<MCodeData> sourceData;
	private final List<EnumOptionData> statusData;
	private final Collection<MCodeData> agreementTypes;

	public AgreementData(Collection<MCodeData> shareTypes,Collection<MCodeData> sourceData, 
			List<EnumOptionData> statusData, Collection<MCodeData> agreementTypes) {

		this.shareTypes = shareTypes;
		this.statusData = statusData;
		this.sourceData = sourceData;
		this.agreementTypes = agreementTypes;

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

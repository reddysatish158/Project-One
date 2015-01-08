package org.mifosplatform.organisation.partner.data;

import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.monetary.data.ApplicationCurrencyConfigurationData;
import org.mifosplatform.organisation.office.data.OfficeData;

public class PartnersData {
    
	//private Long id;
	private Long officeId;
	private Long additionalinfoId;
	private String partnerName; 
	private String partnerType;
	private String currency;
	private Long parentId;
	private String parentName;
	private String officeType;
	private LocalDate openingDate;
	private Collection<MCodeData> partnerTypes;
	private List<String> countryData;
	private List<String> statesData;
	private List<String> citiesData;
	private Collection<CodeValueData> officeTypes;
	private ApplicationCurrencyConfigurationData currencyData;
	private Collection<OfficeData> allowedParents;
	
	
	public PartnersData(Collection<MCodeData> partnerTypes, List<String> countryData, List<String> statesData,
			List<String> citiesData, Collection<CodeValueData> officeTypes,
			ApplicationCurrencyConfigurationData currencyData, Collection<OfficeData> allowedParents) {
        
		this.citiesData = citiesData;
		this.currencyData = currencyData;
		this.countryData = countryData;
		this.officeTypes = officeTypes;
		this.partnerTypes = partnerTypes;
		this.allowedParents = allowedParents;
		this.statesData = statesData;
		
		
	}

	public PartnersData(final Long officeId, final Long additionalinfoId,final String partnerName, final String partnerType, 
			final String currency,final Long parentId, final String parentName, final String officeType,
			final LocalDate openingDate) {
		
	this.officeId = officeId;
	this.additionalinfoId = additionalinfoId;
	this.partnerName = partnerName;
	this.partnerType = partnerType;
	this.currency = currency;
	this.parentId = parentId;
	this.parentName =parentName;
	this.officeType =officeType;
	this.openingDate = openingDate;
	
	}

	public Long getOfficeId() {
		return officeId;
	}

	public Long getAdditionalinfoId() {
		return additionalinfoId;
	}

	public String getPartnerName() {
		return partnerName;
	}

	public String getPartnerType() {
		return partnerType;
	}

	public String getCurrency() {
		return currency;
	}

	public Long getParentId() {
		return parentId;
	}

	
	public String getParentName() {
		return parentName;
	}

	public String getOfficeType() {
		return officeType;
	}

	public LocalDate getOpeningDate() {
		return openingDate;
	}

	public Collection<MCodeData> getPartnerTypes() {
		return partnerTypes;
	}

	public List<String> getCountryData() {
		return countryData;
	}

	public List<String> getStatesData() {
		return statesData;
	}

	public List<String> getCitiesData() {
		return citiesData;
	}


	public ApplicationCurrencyConfigurationData getCurrencyData() {
		return currencyData;
	}

	public Collection<OfficeData> getAllowedParents() {
		return allowedParents;
	}

	public Collection<CodeValueData> getOfficeTypes() {
		return officeTypes;
	}

}

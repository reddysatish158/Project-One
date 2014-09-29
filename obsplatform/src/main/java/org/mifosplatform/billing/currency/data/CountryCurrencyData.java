package org.mifosplatform.billing.currency.data;

import java.math.BigDecimal;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.ApplicationCurrencyConfigurationData;

public class CountryCurrencyData {
	
	private  Long id;
	private  String country;
	private  String currency;
	private  String baseCurrency;
	private  BigDecimal conversionRate;
	private  String status;
	private ApplicationCurrencyConfigurationData currencydata;
	private List<String> countryData;
	private List<EnumOptionData> currencystatus;
	

	public CountryCurrencyData(Long id, String country, String currency, String baseCurrency, BigDecimal conversionRate, String status) {
	    
		this.id=id;
		this.country=country;
		this.currency=currency;
		this.baseCurrency=baseCurrency;
		this.conversionRate=conversionRate;
		this.status=status;
           
	}


	public CountryCurrencyData(CountryCurrencyData currencyData,ApplicationCurrencyConfigurationData currency,
			List<String> countryData, List<EnumOptionData> status) {
                 
		if(currencyData!=null){
			this.id=currencyData.getId();
			this.country=currencyData.getCountry();
			this.currency=currencyData.getCurrency();
			this.status=currencyData.getStatus();
			this.baseCurrency=currencyData.getBaseCurrency();
			this.conversionRate=currencyData.getConversionRate();
		}

	          this.currencydata=currency;
	          this.countryData=countryData;
	          this.currencystatus=status;
	}


	public Long getId() {
		return id;
	}


	public String getBaseCurrency() {
		return baseCurrency;
	}


	public BigDecimal getConversionRate() {
		return conversionRate;
	}


	public ApplicationCurrencyConfigurationData getCurrencydata() {
		return currencydata;
	}


	public List<String> getCountryData() {
		return countryData;
	}


	public List<EnumOptionData> getCurrencystatus() {
		return currencystatus;
	}


	public String getCountry() {
		return country;
	}


	public String getCurrency() {
		return currency;
	}


	public String getStatus() {
		return status;
	}

	
}

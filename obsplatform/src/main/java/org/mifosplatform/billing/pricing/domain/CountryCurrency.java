package org.mifosplatform.billing.pricing.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity

@Table(name = "b_country_currency", uniqueConstraints = @UniqueConstraint(name = "country_key", columnNames = { "country" }))
public class CountryCurrency extends AbstractPersistable<Long> {

	

	@Column(name = "country")
	private String country;

	@Column(name = "currency")
	private String currency;

	@Column(name = "status")
	private String status;
	
	@Column(name = "base_currency")
	private String baseCurrency;
	
	@Column(name = "conversion_rate",scale = 6, precision = 19, nullable = false)
	private BigDecimal conversionRate;

	@Column(name = "is_deleted")
	private String isDeleted="N";

	public CountryCurrency() {
	}

	public CountryCurrency(final String country,final String currency,String basecurrency,BigDecimal conversionRate,final String status)

	{
        this.country=country;
        this.currency=currency;
        this.status=status;
        this.baseCurrency=basecurrency;
        this.conversionRate=conversionRate;
		
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

	public String getIsDeleted() {
		return isDeleted;
	}

	public void delete() {



			this.isDeleted = "y";


	}

	public static CountryCurrency fromJson(JsonCommand command) {
 		    
		    final String country = command.stringValueOfParameterNamed("country");
		    final String currency=command.stringValueOfParameterNamed("currency");
		    final String status = command.stringValueOfParameterNamed("status");
		    final String baseCurrency=command.stringValueOfParameterNamed("baseCurrency");
		    final BigDecimal conversionRate=command.bigDecimalValueOfParameterNamed("conversionRate");
		    return new CountryCurrency(country,currency,baseCurrency,conversionRate,status);
		 
	}

	public  Map<String, Object> update(JsonCommand command) {
		
		 final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		  final String firstnameParamName = "country";
	        if (command.isChangeInStringParameterNamed(firstnameParamName,this.country)) {
	            final String newValue = command.stringValueOfParameterNamed(firstnameParamName);
	            actualChanges.put(firstnameParamName, newValue);
	            this.country = StringUtils.defaultIfEmpty(newValue, null);
	        }
	        
	        final String currencyParamName = "currency";
	        if (command.isChangeInStringParameterNamed(currencyParamName, this.currency)) {
	            final String newValue = command.stringValueOfParameterNamed(currencyParamName);
	            actualChanges.put(currencyParamName, newValue);
	            this.currency = StringUtils.defaultIfEmpty(newValue, null);
	        }
	        
	        final String statusParamName = "status";
			if (command.isChangeInStringParameterNamed(statusParamName,this.status)) {
				final String newValue = command.stringValueOfParameterNamed(statusParamName);
				actualChanges.put(statusParamName, newValue);
				this.status=StringUtils.defaultIfEmpty(newValue,null);
			}
			
		    final String baseCurrencyParamName = "baseCurrency";
					if (command.isChangeInStringParameterNamed(baseCurrencyParamName,this.baseCurrency)) {
						final String newValue = command.stringValueOfParameterNamed(baseCurrencyParamName);
						actualChanges.put(baseCurrencyParamName, newValue);
						this.baseCurrency=StringUtils.defaultIfEmpty(newValue,null);
					}
					
		  final String conversionRateParamName = "conversionRate";
			    if (command.isChangeInBigDecimalParameterNamed(conversionRateParamName,this.conversionRate)) {
			    	final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(conversionRateParamName);
						actualChanges.put(conversionRateParamName, newValue);
						this.conversionRate=newValue;
					}
			
			
	        
	        return actualChanges;

	}
}

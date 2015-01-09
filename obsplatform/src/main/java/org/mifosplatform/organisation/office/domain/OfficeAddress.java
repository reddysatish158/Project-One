package org.mifosplatform.organisation.office.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_office_address")
public class OfficeAddress extends AbstractPersistable<Long> {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	@Column(name = "address_name")
	private String addressName;

	@Column(name = "line_1")
	private String line1;

	@Column(name = "line_2")
	private String line2;

	@Column(name = "city")
	private String city;

	@Column(name = "state")
	private String state;

	@Column(name = "country")
	private String country;

	@Column(name = "phone_number")
	private String phone;

	@Column(name = "email_id")
	private String email;

	@Column(name = "zip")
	private String zip;

	@Column(name = "company_logo")
	private String companyLogo;

	@Column(name = "VRN")
	private String vrn;

	@Column(name = "TIN")
	private String tin;

	@OneToOne
	@JoinColumn(name = "office_id", insertable = true, updatable = true, nullable = true, unique = true)
	private Office office;

	public OfficeAddress() {
		// TODO Auto-generated constructor stub
	}
	
	
	public static OfficeAddress fromJson(final JsonCommand command, Office office) {
		
		final String organization = command.stringValueOfParameterNamed("organization");
		final String phone = command.stringValueOfParameterNamed("phone");
		final String email = command.stringValueOfParameterNamed("email");
		final String city = command.stringValueOfParameterNamed("city");
		final String state = command.stringValueOfParameterNamed("state");
		final String country = command.stringValueOfParameterNamed("country");
		String companyLogo=null;
		if(command.parameterExists("companyLogo")){
			companyLogo  = command.stringValueOfParameterNamed("companyLogo");
		}
		
		return new OfficeAddress(organization,phone,email,city,state,country,companyLogo,office);
	}

	public OfficeAddress(final String organization, final String phone, final String email,final String city, 
			final String state, final String country, final String companyLogo,final Office office) {
		this.addressName = organization;
		this.phone = phone;
		this.email = email;
		this.city = city;
		this.state = state;
		this.country = country;
		this.companyLogo = (companyLogo!=null)? companyLogo : null;
		this.office = office;
		
	}
	
	

	public String getAddressName() {
		return addressName;
	}
	
	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getCountry() {
		return country;
	}

	
	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}


	public String getCompanyLogo() {
		return companyLogo;
	}


	public Office getOffice() {
		return office;
	}

	public void setOffice(Office office) {
		this.office = office;
	}

		
}
	


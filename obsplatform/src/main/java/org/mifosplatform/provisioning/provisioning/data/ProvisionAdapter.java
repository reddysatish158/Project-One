package org.mifosplatform.provisioning.provisioning.data;

public class ProvisionAdapter {

	private String dateName;
	private String dateValue;
	
	public ProvisionAdapter(String dateName, String dateValue){
		this.dateName = dateName;
		this.dateValue = dateValue;
	}

	public String getDateName() {
		return dateName;
	}

	public String getDateValue() {
		return dateValue;
	}
	
	
}

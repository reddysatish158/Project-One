/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.data;

/**
 * Immutable data object for global configuration property.
 */
public class ConfigurationPropertyData {

    private String name;
    private String value;
    private boolean enabled;
    private Long id;

    public ConfigurationPropertyData(final Long id, final String name, final boolean enabled, final String value) {
    	
        this.id = id;
    	this.name = name;
        this.enabled = enabled;
        this.value = value;
    }

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}
    
}
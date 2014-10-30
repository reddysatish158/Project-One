/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.data;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;

/**
 * Immutable data object for office data.
 */
public class OfficeData {

    private final Long id;
    private final String name;
    private final String nameDecorated;
    private final String externalId;
    private final LocalDate openingDate;
    private final String hierarchy;
    private final Long parentId;
    private final String parentName;
    private final String officeType;
    
    private final Collection<OfficeData> allowedParents;
    private final Collection<CodeValueData> officeTypes;

    public static OfficeData dropdown(final Long id, final String name, final String nameDecorated) {
    	
        return new OfficeData(id, name, nameDecorated, null, null, null, null, null, null,null,null);
    }

    public static OfficeData template(final Collection<OfficeData> parentLookups, final LocalDate defaultOpeningDate, final Collection<CodeValueData> officeTypes) {
    	
        return new OfficeData(null, null, null, null, defaultOpeningDate, null, null, null, parentLookups,officeTypes,null);
    }

    public static OfficeData appendedTemplate(final OfficeData office, final Collection<OfficeData> allowedParents, final Collection<CodeValueData> codeValueDatas) {
    	
        return new OfficeData(office.id, office.name, office.nameDecorated, office.externalId, office.openingDate, office.hierarchy,
                office.parentId, office.parentName, allowedParents,codeValueDatas,office.officeType);
    }

    public OfficeData(final Long id, final String name, final String nameDecorated, final String externalId, final LocalDate openingDate,
            final String hierarchy, final Long parentId, final String parentName, final Collection<OfficeData> allowedParents, 
            final Collection<CodeValueData> codeValueDatas, final String officeType) {
    	
        this.id = id;
        this.name = name;
        this.nameDecorated = nameDecorated;
        this.externalId = externalId;
        this.openingDate = openingDate;
        this.hierarchy = hierarchy;
        this.parentName = parentName;
        this.parentId = parentId;
        this.allowedParents = allowedParents;
        this.officeTypes = codeValueDatas;
        this.officeType = officeType;
        
    }

    public boolean hasIdentifyOf(final Long officeId) {
    	
        return this.id.equals(officeId);
    }

	public Collection<OfficeData> getAllowedParents() {
		return allowedParents;
	}

	public Collection<CodeValueData> getOfficeTypes() {
		return officeTypes;
	}
    
    
}
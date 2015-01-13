package org.mifosplatform.organisation.partner.service;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.organisation.partner.data.PartnersData;
import org.mifosplatform.organisation.partneragreement.data.AgreementData;

public interface PartnersReadPlatformService {

	Collection<PartnersData> retrieveAllPartners();

	PartnersData retrieveSinglePartnerDetails(Long partnerId);


}

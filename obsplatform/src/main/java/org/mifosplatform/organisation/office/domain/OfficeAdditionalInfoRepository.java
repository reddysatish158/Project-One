package org.mifosplatform.organisation.office.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OfficeAdditionalInfoRepository extends
		JpaRepository<OfficeAdditionalInfo, Long>,
		JpaSpecificationExecutor<OfficeAdditionalInfo> {
	// no added behaviour
}

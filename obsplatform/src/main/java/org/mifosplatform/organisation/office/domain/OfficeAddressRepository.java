package org.mifosplatform.organisation.office.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OfficeAddressRepository extends
		JpaRepository<OfficeAddress, Long>,
		JpaSpecificationExecutor<OfficeAddress> {
	// no added behaviour
}



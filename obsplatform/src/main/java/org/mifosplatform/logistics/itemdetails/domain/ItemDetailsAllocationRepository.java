package org.mifosplatform.logistics.itemdetails.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ItemDetailsAllocationRepository extends JpaRepository<ItemDetailsAllocation, Long>, JpaSpecificationExecutor<ItemDetailsAllocation>{

}

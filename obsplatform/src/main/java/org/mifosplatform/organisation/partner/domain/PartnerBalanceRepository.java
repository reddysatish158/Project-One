package org.mifosplatform.organisation.partner.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PartnerBalanceRepository extends JpaRepository<PartnerBalance, Long>,
JpaSpecificationExecutor<PartnerBalance> {

	
@Query("from PartnerBalance balance where balance.officeId = ?1 and balance.accountType= ?2")
PartnerBalance findOneWithPartnerAccount(Long officeId,String accountType);

}

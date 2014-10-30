package org.mifosplatform.billing.chargecode.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeCodeRepository extends JpaRepository<ChargeCodeMaster, Long>,
		JpaSpecificationExecutor<ChargeCodeMaster> {

	@Query("from ChargeCodeMaster charge where charge.chargeCode =:chargeCode")
	ChargeCodeMaster findOneByChargeCode(@Param("chargeCode") String chargeCode);

}

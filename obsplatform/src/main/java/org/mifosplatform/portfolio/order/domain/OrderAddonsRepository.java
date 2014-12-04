package org.mifosplatform.portfolio.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderAddonsRepository  extends JpaRepository<OrderAddons, Long>,
   JpaSpecificationExecutor<OrderAddons>{

    @Query("from OrderAddons order where order.orderId =:orderId ")
    OrderAddons findAddonsByOrderId(@Param("orderId")String orderId);

	
}

package org.mifosplatform.billing.promotioncodes.service;

import java.util.List;

import org.mifosplatform.billing.promotioncodes.data.PromotionCodeData;

/**
 * @author hugo
 *
 */
public interface PromotionCodeReadPlatformService {

	List<PromotionCodeData> retrieveAllPromotionCodes();

	PromotionCodeData retriveSinglePromotionCodeDetails(Long id);

}

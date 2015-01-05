package org.mifosplatform.organisation.voucher.service;

import java.util.List;

import javax.ws.rs.core.StreamingOutput;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.voucher.data.VoucherData;

/**
 * 
 * @author ashokreddy
 *
 */
public interface VoucherReadPlatformService {

	String retrieveIndividualPin(String pinId);
	
	List<VoucherData> getAllData();

	List<EnumOptionData> pinCategory();

	List<EnumOptionData> pinType();

	Long retrieveMaxNo(Long minNo, Long maxNo);

	StreamingOutput retrieveVocherDetailsCsv(Long batchId);

	List<VoucherData> retrivePinDetails(String pinNumber);

}

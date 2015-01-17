package org.mifosplatform.billing.partnerdisbursement.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Date;
import java.util.List;

import org.mifosplatform.billing.partnerdisbursement.data.PartnerDisbursementData;
import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.service.GenericDataService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PartnerDisbursementReadPlatformServiceImpl implements
PartnerDisbursementReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<PartnerDisbursementData> paginationHelper = new PaginationHelper<PartnerDisbursementData>();

	@Autowired
	public PartnerDisbursementReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource,
			final GenericDataService genericDataService) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	
	@Override
	public Page<PartnerDisbursementData> getAllData(SearchSqlQuery search, String sourceType, String partnerType) {
		try {

			context.authenticatedUser();
			RetrieveRandomMapper mapper = new RetrieveRandomMapper();
			StringBuilder sqlBuilder = new StringBuilder();
			
			sqlBuilder.append("SELECT ");
			sqlBuilder.append(mapper.schema());
			sqlBuilder.append(" where st.id IS NOT NULL ");
			
	        if(sourceType != null){
	        	sqlBuilder.append(" and (st.source_type ='"+sourceType+"') ");
		    }
	        if(partnerType != null){
	        	sqlBuilder.append(" and (o.partner_name ='"+partnerType+"') ");
		    }
			
			if (search.isLimited()) {
				sqlBuilder.append(" limit ").append(search.getLimit());
		    }

		    if (search.isOffset()) {
		        sqlBuilder.append(" offset ").append(search.getOffset());
		    }
		    
			return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()", sqlBuilder.toString(),
		            new Object[] {}, mapper);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class RetrieveRandomMapper implements
			RowMapper<PartnerDisbursementData> {

		public String schema() {

				return " st.id AS id,o.partner_name as partnerName,st.d_date as transDate,st.source_type as source," +
						"st.charge_amount as chargeAmount,st.commission_percentage as percentage," +
						"st.commission_amount as commissionAmount,st.net_amount as netAmount FROM b_partner_settlement st " +
						"LEFT JOIN b_office_additional_info o ON o.office_id = st.partner_id ";

		}

		@Override
		public PartnerDisbursementData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String partnerName = rs.getString("partnerName");
			Date transDate = rs.getDate("transDate");
			String source = rs.getString("source");
			String percentage = rs.getString("percentage");
			Double chargeAmount = rs.getDouble("chargeAmount");
			Double commissionAmount = rs.getDouble("commissionAmount");
			Double netAmount = rs.getDouble("netAmount");

			return new PartnerDisbursementData(id, partnerName, transDate,
					source, percentage, chargeAmount, commissionAmount, netAmount);

		}
	}

	@Override
	public List<PartnerDisbursementData> getPatnerData() {
		try {
			context.authenticatedUser();
			PartnerDataMapper mapper = new PartnerDataMapper();
			String sql="SELECT "+ mapper.schema();
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	private static final class PartnerDataMapper implements RowMapper<PartnerDisbursementData> {

		public String schema() {
			return " bo.id as id,bo.partner_name as partnerName from b_office_additional_info bo ";

		}

		@Override
		public PartnerDisbursementData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			Long id = rs.getLong("id");
			String partnerName = rs.getString("partnerName");
			return new PartnerDisbursementData(id, partnerName);
		}
	}
}

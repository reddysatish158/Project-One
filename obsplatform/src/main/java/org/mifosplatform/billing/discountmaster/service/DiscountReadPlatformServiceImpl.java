package org.mifosplatform.billing.discountmaster.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.billing.discountmaster.service.DiscountReadPlatformService;
import org.mifosplatform.finance.data.DiscountMasterData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 *
 */
@Service
public class DiscountReadPlatformServiceImpl implements DiscountReadPlatformService
{

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;


	@Autowired
	public DiscountReadPlatformServiceImpl(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<DiscountMasterData> retrieveAllDiscounts() {
   
	try{	
		context.authenticatedUser();
		final DiscountMapper mapper = new DiscountMapper();

		final String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		
	}catch(EmptyResultDataAccessException accessException){
		return null;
	}

	}

	/**
	 * @author hugo
	 *
	 */
	private static final class DiscountMapper implements RowMapper<DiscountMasterData> {

		public String schema() {
			return "ds.id as id, ds.discount_code as discountCode, ds.discount_description as discountdescription," +
				 "ds.discount_type as discountType, ds.discount_rate as discountRate, ds.start_date as startDate, " +
				 "ds.discount_status as status from b_discount_master ds  where ds.is_delete='N' ";

		}

		@Override
		public DiscountMasterData mapRow(final ResultSet resultSet,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final Long id = resultSet.getLong("id");
			final String discountCode = resultSet.getString("discountCode");
			final String discountdescription = resultSet.getString("discountdescription");
			final String discountType = resultSet.getString("discountType");
			final BigDecimal discountRate = resultSet.getBigDecimal("discountRate");
			final LocalDate startDate = JdbcSupport.getLocalDate(resultSet,"startDate");
			final String status = resultSet.getString("status");
			return new DiscountMasterData(id, discountCode, discountdescription, discountType, discountRate,startDate,status);

		}
	}

	@Override
	public DiscountMasterData retrieveDiscountDetails(Long discountId) {
		try{	
			context.authenticatedUser();
			DiscountMapper mapper = new DiscountMapper();
			String sql = "select " + mapper.schema()+" and ds.id=?";

			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { discountId });
		}catch(EmptyResultDataAccessException accessException){
			return null;
		}

		}
}


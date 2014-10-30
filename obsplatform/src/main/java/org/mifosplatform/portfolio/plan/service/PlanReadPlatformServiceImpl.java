package org.mifosplatform.portfolio.plan.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.billing.planprice.service.PriceReadPlatformService;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.order.data.OrderStatusEnumaration;
import org.mifosplatform.portfolio.order.data.VolumeTypeEnumaration;
import org.mifosplatform.portfolio.order.domain.StatusTypeEnum;
import org.mifosplatform.portfolio.plan.data.BillRuleData;
import org.mifosplatform.portfolio.plan.data.PlanData;
import org.mifosplatform.portfolio.plan.data.ServiceData;
import org.mifosplatform.portfolio.plan.domain.VolumeTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


/**
 * @author hugo
 *
 */
@Service
public class PlanReadPlatformServiceImpl implements PlanReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private  final  PriceReadPlatformService priceReadPlatformService;
	public final static String POST_PAID="postpaid";
	public final static String PREPAID="prepaid";
	
	

	@Autowired
	public PlanReadPlatformServiceImpl(final PlatformSecurityContext context,final PriceReadPlatformService priceReadPlatformService,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.priceReadPlatformService=priceReadPlatformService;
	}


	/*
	 *Retrieve billing Rules
	 */
	@Override
	public List<BillRuleData> retrievebillRules() {

		context.authenticatedUser();

		final BillRuleDataMapper mapper = new BillRuleDataMapper();

		final String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class BillRuleDataMapper implements RowMapper<BillRuleData> {

		public String schema() {
			return " b.enum_id AS id,b.enum_message_property AS billingRule,b.enum_value AS value FROM r_enum_value b" +
					" WHERE enum_name = 'billing_rules'";

		}

		@Override
		public BillRuleData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String billrules = rs.getString("billingRule");
			final String value = rs.getString("value");
			return new BillRuleData(id, billrules,value);
			
		}
	}

	@Override
	public List<PlanData> retrievePlanData(final String planType) {

		context.authenticatedUser();
		 String sql=null;
		PlanDataMapper mapper = new PlanDataMapper(this.priceReadPlatformService);
		
		if(planType!=null && PREPAID.equalsIgnoreCase(planType)){

		 sql = "select " + mapper.schema()+" AND pm.is_prepaid ='Y'";
		 
		}else if(planType!=null && planType.equalsIgnoreCase(POST_PAID)){
		
			sql = "select " + mapper.schema()+" AND pm.is_prepaid ='N'";
		}else{
			sql = "select " + mapper.schema();
		}

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class PlanDataMapper implements RowMapper<PlanData> {
         private final PriceReadPlatformService priceReadPlatformService;
		
         public PlanDataMapper(final PriceReadPlatformService priceReadPlatformService) {
			this.priceReadPlatformService=priceReadPlatformService;
		}

		public String schema() {
			return "  pm.id,pm.plan_code as planCode,pm.plan_description as planDescription,pm.start_date as startDate," +
					" pm.end_date as endDate,pm.plan_status as planStatus,pm.is_prepaid AS isprepaid," +
					" pm.provision_sys as provisionSystem  FROM  b_plan_master pm WHERE pm.is_deleted = 'n' ";

		}

		@Override
		public PlanData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String planCode = rs.getString("planCode");
			final String planDescription = rs.getString("planDescription");
			final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
		    final Long planStatus = rs.getLong("planStatus");
			final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
			final EnumOptionData enumstatus=OrderStatusEnumaration.OrderStatusType(planStatus.intValue());
			List<ServiceData> services=null;
			if(rs.getString("isprepaid").equalsIgnoreCase("Y")){
			services=priceReadPlatformService.retrieveServiceDetails(id);
			}
			final String provisionSystem=rs.getString("provisionSystem");
			return new PlanData(id, planCode, startDate, endDate,null,null, planStatus, planDescription, provisionSystem, enumstatus,
					null,null, null,null,null,services,null,null);
		}
	}

	@Override
	public List<SubscriptionData> retrieveSubscriptionData(final Long orderId,final String planType) {

		context.authenticatedUser();
		SubscriptionDataMapper mapper = new SubscriptionDataMapper();
		String sql =null;
		if(planType != null && orderId != null && PREPAID.equalsIgnoreCase(planType)){
			
			 sql = "select " + mapper.schemaForPrepaidPlans()+" and o.id="+orderId+" GROUP BY sb.contract_period order by sb.contract_period";
		}else{
		    sql = "select " + mapper.schema()+" order by contract_period";
		}
		
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	/**
	 * @author hugo
	 *
	 */
	private static final class SubscriptionDataMapper implements
			RowMapper<SubscriptionData> {

		public String schema() {
			return " sb.id as id,sb.contract_period as contractPeriod,sb.contract_duration as units,sb.contract_type as contractType "
					+ " from b_contract_period sb where is_deleted='N'";

		}
		
		public String schemaForPrepaidPlans() {
			return "  sb.id AS id,sb.contract_period AS contractPeriod,sb.contract_duration AS units,sb.contract_type AS contractType" +
					" FROM b_contract_period sb, b_orders o, b_plan_pricing p WHERE sb.is_deleted = 'N' and sb.contract_period=p.duration " +
					" and o.plan_id = p.plan_id  ";

		}

		@Override
		public SubscriptionData mapRow(final ResultSet rs,final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String contractPeriod = rs.getString("contractPeriod");
			final String subscriptionType = rs.getString("contractType");
			return new SubscriptionData(id,contractPeriod,subscriptionType);
		}

	}

	/*
	 *Method for Status Retrieval
	 */
	@Override
	public List<EnumOptionData> retrieveNewStatus() {
		
		final EnumOptionData active = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE);
		final EnumOptionData inactive = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.INACTIVE);
		return 	Arrays.asList(active, inactive);
			
	}

	@Override
	public PlanData retrievePlanData(final Long planId) {
		  context.authenticatedUser();
	        final String sql = "SELECT pm.id AS id,pm.plan_code AS planCode,pm.plan_description AS planDescription,pm.start_date AS startDate,pm.end_date AS endDate,"
	        		   +"pm.plan_status AS planStatus,pm.provision_sys AS provisionSys,pm.bill_rule AS billRule,pm.is_prepaid as isPrepaid,"
	        		  +" pm.allow_topup as allowTopup,v.volume_type as volumeType, v.units as units,pm.is_hw_req as isHwReq,v.units_type as unitType FROM b_plan_master pm  left join b_volume_details v" +
	        		  " on pm.id = v.plan_id WHERE pm.id = ? AND pm.is_deleted = 'n'";


	        RowMapper<PlanData> rm = new ServiceMapper();

	        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { planId });
	
		}


	 private static final class ServiceMapper implements RowMapper<PlanData> {

	        @Override
	        public PlanData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

	            final Long id = rs.getLong("id");
	            final String planCode = rs.getString("planCode");
	            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
	            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
	            final Long billRule = rs.getLong("billRule");
	            final Long planStatus = rs.getLong("planStatus");
	            final String planDescription = rs.getString("planDescription");
	            final String provisionSys=rs.getString("provisionSys");
	            final String isPrepaid=rs.getString("isPrepaid");
	            final String volume=rs.getString("volumeType");
	            final String allowTopup=rs.getString("allowTopup");
	            final String isHwReq=rs.getString("isHwReq");
	            final String units=rs.getString("units");
	            final String unitType=rs.getString("unitType");
	            
	            return new PlanData(id,planCode,startDate,endDate,billRule,null,planStatus,planDescription,
	            		provisionSys,null,isPrepaid,allowTopup,volume,units,unitType,null,null,isHwReq);
	        }
	}



	/* @param planId
	 * @return PlanDetails
	 */
	@Override
	public List<ServiceData> retrieveSelectedServices(final Long planId) {
		  context.authenticatedUser();

	        String sql = "SELECT sm.id AS id,sm.service_description AS serviceDescription,p.plan_code as planCode,"
			     +" pm.service_code AS serviceCode   FROM b_plan_detail pm, b_service sm,b_plan_master p"
				 +" WHERE pm.service_code = sm.service_code AND p.id = pm.plan_id and sm.is_deleted ='n' and  pm.plan_id=?";


	        RowMapper<ServiceData> rm = new PeriodMapper();

	        return this.jdbcTemplate.query(sql, rm, new Object[] { planId });
	}


	 private static final class PeriodMapper implements RowMapper<ServiceData> {

	        @Override
	        public ServiceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

	            final Long id = rs.getLong("id");
	            final String serviceCode = rs.getString("serviceCode");
	            final String serviceDescription = rs.getString("serviceDescription");
	        	return new ServiceData(id,null,null,null,serviceCode, serviceDescription,null,null,null,null);
	           
	        }
}
	 

		@Override
		public List<EnumOptionData> retrieveVolumeTypes() {
			
			final EnumOptionData iptv = VolumeTypeEnumaration.VolumeTypeEnum(VolumeTypeEnum.IPTV);
			final EnumOptionData vod = VolumeTypeEnumaration.VolumeTypeEnum(VolumeTypeEnum.VOD);
			return  Arrays.asList(iptv,vod);
				
		}

		


	}

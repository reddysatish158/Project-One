package org.mifosplatform.organisation.partner.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.partner.data.PartnersData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PartnersReadPlatformServiceImp implements PartnersReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public PartnersReadPlatformServiceImp(final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<PartnersData> retrieveAllPartners() {

	try {
		context.authenticatedUser();
		final PartnerMapper mapper = new PartnerMapper();
		final String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});

	} catch (EmptyResultDataAccessException accessException) {
		return null;
	}

}

private static final class PartnerMapper implements RowMapper<PartnersData> {

		public String schema() {
			return " a.id as infoId,a.partner_name as partnerName,a.partner_currency as currency,mc.code_value as partnerType,"
					+ "o.id as officeId,o.parent_id as parentId,o.external_id AS externalId,o.opening_date AS openingDate,parent.id AS parentId,"
					+ "parent.name AS parentName,c.code_value as officeType from m_office o left join m_office AS parent ON parent.id = o.parent_id "
					+ " inner join b_office_additional_info a ON o.id=a.office_id left join m_code_value c ON c.id = o.office_type "
					+ " left join m_code_value mc on mc.id = a.partner_type";
		}

	@Override
	public PartnersData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

		
	final Long additionalinfoId = rs.getLong("infoId");
	final Long officeId = rs.getLong("officeId");
	final String partnerName = rs.getString("partnerName");
	final String partnerType = rs.getString("partnerType");
	final String currency = rs.getString("currency");
	final Long parentId = rs.getLong("parentId");
	//final Long externalId = rs.getLong("externalId");
	final String parentName = rs.getString("parentName");
	final String officeType = rs.getString("officeType");
	final LocalDate openingDate = JdbcSupport.getLocalDate(rs, "openingDate");
	
	return new PartnersData(officeId,additionalinfoId,partnerName,partnerType,currency,parentId,parentName,officeType,openingDate);
	

	}
}

}

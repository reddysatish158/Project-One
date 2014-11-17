package org.mifosplatform.logistics.itemdetails.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.logistics.itemdetails.data.AllocationHardwareData;
import org.mifosplatform.logistics.itemdetails.data.ItemDetailsData;
import org.mifosplatform.logistics.itemdetails.data.ItemMasterIdData;
import org.mifosplatform.logistics.itemdetails.data.ItemSerialNumberData;
import org.mifosplatform.logistics.itemdetails.data.QuantityData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ItemDetailsReadPlatformServiceImp implements ItemDetailsReadPlatformService {

	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<ItemDetailsData> paginationHelper = new PaginationHelper<ItemDetailsData>();
	@Autowired
	ItemDetailsReadPlatformServiceImp(final TenantAwareRoutingDataSource dataSource,final PlatformSecurityContext context){
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.context = context;
	}
	
	private class ItemDetailsMapper implements RowMapper<ItemDetailsData>{

		@Override
		public ItemDetailsData mapRow(ResultSet rs, int rowNum) throws SQLException {
			Long id = rs.getLong("id");
			Long itemMasterId = rs.getLong("itemMasterId");
			String serialNumber = rs.getString("serialNumber");
			Long grnId = rs.getLong("grnId");
			String provisioningSerialNumber = rs.getString("provisioningSerialNumber");
			String quality= rs.getString("quality");
			String status = rs.getString("status");
			Long warranty = rs.getLong("warranty");
			String remarks = rs.getString("remarks");
			String itemDescription = rs.getString("itemDescription");
			String supplier = rs.getString("supplier");
			Long clientId = rs.getLong("clientId");
			String officeName = rs.getString("officeName");
			String accountNumber = rs.getString("accountNumber");
			return new ItemDetailsData(id,itemMasterId,serialNumber,grnId,provisioningSerialNumber,quality,status,warranty,remarks,itemDescription,supplier,clientId,officeName,accountNumber);
		}
		
		public String schema(){
			
			String sql = "SQL_CALC_FOUND_ROWS item.id as id, office.name as officeName,item.item_master_id as itemMasterId, "
					+ "item.serial_no as serialNumber, item.grn_id as grnId, "
					+ "(select supplier_description from b_supplier where id = (select supplier_id from b_grn where b_grn.id=item.grn_id)) as supplier,"
					+ "item.provisioning_serialno as provisioningSerialNumber, item.quality as quality, item.status as status, "
					+ "item.warranty as warranty, item.remarks as remarks, master.item_description as itemDescription, "
					+ "item.client_id as clientId, "
					+ "(select account_no from m_client where id = client_id) as accountNumber "
					+ "from b_item_detail item left outer join b_item_master master on item.item_master_id = master.id left outer join m_office office on item.office_id=office.id ";
			
					
			return sql;
		}
		
	}

	public Page<ItemDetailsData> retriveAllItemDetails(SearchSqlQuery searchItemDetails) {	
		// TODO Auto-generated method stub
		context.authenticatedUser();
		ItemDetailsMapper itemDetails = new ItemDetailsMapper();
		
		StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(itemDetails.schema());
        sqlBuilder.append(" where item.office_id = office.id and item.is_deleted='N' ");
        
        String sqlSearch = searchItemDetails.getSqlSearch();
        String extraCriteria = "";
	    if (sqlSearch != null) {
	    	sqlSearch=sqlSearch.trim();
	    	extraCriteria = " and (master.item_description like '%"+sqlSearch+"%' OR" 
	    			+ " item.serial_no like '%"+sqlSearch+"%' OR"
	    			+ " office.name like '%"+sqlSearch+"%' OR"
	    			+ " item.quality like '%"+sqlSearch+"%' OR"
	    			+ " item.status like '%"+sqlSearch+"%' )";
	    }
	   
	 
            sqlBuilder.append(extraCriteria);
       
        if (searchItemDetails.isLimited()) {
            sqlBuilder.append(" limit ").append(searchItemDetails.getLimit());
        }

        if (searchItemDetails.isOffset()) {
            sqlBuilder.append(" offset ").append(searchItemDetails.getOffset());
        }

		return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(),
                new Object[] {}, itemDetails);
	}

	/*
	 * this method is not implemented or being used, who ever wants to use this method please remove this comment and give a message.
	 * */
	
	private class SerialNumberMapper implements RowMapper<String>{

		@Override
		public String mapRow(ResultSet rs, int rowNum)throws SQLException {
			String serialNumber = rs.getString("serialNumber");
			return serialNumber;
		}
	}
	
	
	private final class ItemDetailMapper implements RowMapper<AllocationHardwareData>{
		
		@Override
		public AllocationHardwareData mapRow(ResultSet rs, int rowNum)throws SQLException{
			
			Long id = rs.getLong("id");
			Long clientId = rs.getLong("clientId");
			String quality = rs.getString("quality");
			String serialNo=rs.getString("serialNo");
			return new AllocationHardwareData(id,clientId,quality,serialNo);
		}
	}  
	

	
private final class SerialNumberForValidation implements RowMapper<String>{
		
		@Override
		public String mapRow(ResultSet rs, int rowNum)throws SQLException{
			String serialNumber = rs.getString("serialNumber");
			return serialNumber;
		}
	}
	
	@Override
	public List<String> retriveSerialNumbers() {
		context.authenticatedUser();
		SerialNumberForValidation rowMapper = new SerialNumberForValidation();
		String sql = "select serial_no as serialNumber from b_item_detail item";
		return jdbcTemplate.query(sql, rowMapper);
	}
	
/*	@Override
	public List<String> retriveSerialNumbers(Long oneTimeSaleId) {
		
		context.authenticatedUser();
		SerialNumberMapper rowMapper = new SerialNumberMapper();
		String sql = "select idt.serial_no as serialNumber from b_onetime_sale ots left join b_item_detail idt on idt.item_master_id = ots.item_id" +
				" where ots.id = ? and idt.client_id is null  limit 20";"select serial_no as serialNumber from b_item_detail where item_master_id=(select item_id from b_onetime_sale where id=?) and client_id is null";
		return this.jdbcTemplate.query(sql,rowMapper,new Object[]{oneTimeSaleId});
	}*/
	


	
	@Override
	public AllocationHardwareData retriveInventoryItemDetail(String serialNumber,Long officeId){
		
		try{
			
		context.authenticatedUser();
		ItemDetailMapper rowMapper = new ItemDetailMapper();
		String sql = "SELECT i.id,i.client_id AS clientId,i.quality as quality,i.serial_no as serialNo FROM b_item_detail i WHERE  i.serial_no = ? and i.status='Available'" +
				" and i.office_id =?";
		  return this.jdbcTemplate.queryForObject(sql,rowMapper,new Object[]{serialNumber,officeId});
		 }catch(EmptyResultDataAccessException accessException){
			 throw new PlatformDataIntegrityException("SerialNumber SerialNumber"+serialNumber+" doest not exist.","SerialNumber "+serialNumber+" doest not exist.","serialNumber"+serialNumber);
		}
	}
	
	@Override
	public ItemSerialNumberData retriveAllocationData(List<String> itemSerialNumbers,QuantityData quantityData, ItemMasterIdData itemMasterIdData){
		
		return new ItemSerialNumberData(itemSerialNumbers, quantityData.getQuantity(), itemMasterIdData.getItemMasterId());
	}




	@Override
	public List<String> retriveSerialNumbersOnKeyStroke(final Long oneTimeSaleId, final String query,final Long officeId) {
		
		try{
		
		context.authenticatedUser();
		SerialNumberMapper rowMapper = new SerialNumberMapper();
		String sql = "SELECT idt.serial_no AS serialNumber  FROM b_item_detail idt  where idt.client_id IS NULL" +
      " AND idt.serial_no like '%"+query+"%'  AND quality = 'Good' ";
		StringBuilder builder=new StringBuilder(sql);

		if(!oneTimeSaleId.equals(Long.valueOf(0)) || !officeId.equals(Long.valueOf(0))){
			builder.append(" AND idt.item_master_id="+oneTimeSaleId+" AND idt.office_id="+officeId);
		}
		builder.append(" AND idt.is_deleted='N' ORDER BY idt.id  LIMIT 20");
		return this.jdbcTemplate.query(builder.toString(),rowMapper,new Object[]{});
		
		}catch(EmptyResultDataAccessException accessException){
			return null; 			
		}
	}
	
	@Override
	public List<ItemDetailsData> retriveSerialNumbersOnKeyStroke(String query) {
		
		context.authenticatedUser();
		SerialNumberAndProvisionSerialMapper rowMapper = new SerialNumberAndProvisionSerialMapper();
	
		String sql="SELECT idt.serial_no AS serialNumber,idt.provisioning_serialno as provserialnumber FROM b_item_detail idt "+
					" WHERE  idt.client_id IS NULL AND (idt.serial_no LIKE '%"+query+"%' OR idt.provisioning_serialno like '%"+query+"%') "+
					" AND quality = 'Good' AND quality = 'Good' ORDER BY idt.id LIMIT 10";
	
		return this.jdbcTemplate.query(sql,rowMapper,new Object[]{});
	}
	
	private class SerialNumberAndProvisionSerialMapper implements RowMapper<ItemDetailsData>{

		@Override
		public ItemDetailsData mapRow(ResultSet rs, int rowNum)throws SQLException {
			String serialNumber = rs.getString("serialNumber");
			String provisionSerialNumber = rs.getString("provserialnumber");
			return new ItemDetailsData(null,null,null,serialNumber,provisionSerialNumber);
		}
	}

	@Override
	public ItemDetailsData retriveSingleItemDetail(Long itemId) {
	try{
	String sql = "select item.id as id,office.name as officeName, item.item_master_id as itemMasterId, item.serial_no as serialNumber, " +
	"item.grn_id as grnId, (select supplier_description from b_supplier where id = (select supplier_id from b_grn where b_grn.id=item.grn_id)) " +
	"as supplier,item.provisioning_serialno as provisioningSerialNumber, item.quality as quality, item.status as status, " +
	"(select warranty from b_item_master where id = item.item_master_id) as warranty, item.remarks as remarks, master.item_description as itemDescription, " +
	"item.client_id as clientId from b_item_detail item left outer join b_item_master master on item.item_master_id = master.id left outer join " +
	"m_office office on item.office_id = office.id where item.id=?";
	ItemDetailsMapper2 rowMapper = new ItemDetailsMapper2();
	return jdbcTemplate.queryForObject(sql, rowMapper,new Object[]{itemId});
	}catch(EmptyResultDataAccessException accessException){
	throw new PlatformDataIntegrityException("validation.error.msg.inventory.item.invalid.item.id", "validation.error.msg.inventory.item.invalid.item.id", "validation.error.msg.inventory.item.invalid.item.id","");
	}
	}
	
	private class ItemDetailsMapper2 implements RowMapper<ItemDetailsData>{
		@Override
		public ItemDetailsData mapRow(ResultSet rs, int rowNum) throws SQLException {
		Long id = rs.getLong("id");
		Long itemMasterId = rs.getLong("itemMasterId");
		String serialNumber = rs.getString("serialNumber");
		Long grnId = rs.getLong("grnId");
		String provisioningSerialNumber = rs.getString("provisioningSerialNumber");
		String quality= rs.getString("quality");
		String status = rs.getString("status");
		Long warranty = rs.getLong("warranty");
		String remarks = rs.getString("remarks");
		String itemDescription = rs.getString("itemDescription");
		String supplier = rs.getString("supplier");
		Long clientId = rs.getLong("clientId");
		String officeName = rs.getString("officeName");
		return new ItemDetailsData(id, itemMasterId, serialNumber, grnId, provisioningSerialNumber, quality, status, warranty, remarks,
				itemDescription, supplier, clientId, officeName, null);
		}
		
		}

	@Override
	public ItemData retriveItemDetailsDataBySerialNum(final String query) {

	  try{

		   	context.authenticatedUser();
			final ItemMastersDataMapper rowMapper = new ItemMastersDataMapper();
	
			final String sql="SELECT m.id AS id, m.item_code AS itemCode,m.item_description AS itemDescription,m.charge_code AS chargeCode,"
								+ "m.unit_price AS unitPrice FROM b_item_detail itd, b_item_master m "
								+ " WHERE itd.serial_no = '"+query+"' AND itd.client_id IS NULL and m.id=itd.item_master_id";
	
			return this.jdbcTemplate.queryForObject(sql,rowMapper,new Object[]{});

		}catch(EmptyResultDataAccessException e){
			return null;
		}
		
	}
	
	private class ItemMastersDataMapper implements RowMapper<ItemData>{

		@Override
		public ItemData mapRow(final ResultSet rs, final int rowNum)throws SQLException {
			final Long id = rs.getLong("id");
			final String itemCode = rs.getString("itemCode");
			final String itemDescription = rs.getString("itemDescription");
			final String chargeCode = rs.getString("chargeCode");
			final BigDecimal unitPrice = rs.getBigDecimal("unitPrice") ;
			
			return new ItemData(id,itemCode,itemDescription,chargeCode,unitPrice);
		}
	}

}

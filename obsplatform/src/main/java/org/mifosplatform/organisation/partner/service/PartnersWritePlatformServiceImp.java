package org.mifosplatform.organisation.partner.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeAdditionalInfo;
import org.mifosplatform.organisation.office.domain.OfficeAddress;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.organisation.partner.serialization.PartnersCommandFromApiJsonDeserializer;
import org.mifosplatform.useradministration.api.UsersApiResource;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.domain.Role;
import org.mifosplatform.useradministration.domain.RoleRepository;
import org.mifosplatform.useradministration.exception.RoleNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartnersWritePlatformServiceImp implements
		PartnersWritePlatformService {

	private final static Logger LOGGER = LoggerFactory.getLogger(PartnersWritePlatformServiceImp.class);
	private final PlatformSecurityContext context;
	private final PartnersCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final OfficeRepository officeRepository;
    private final RoleRepository roleRepository;
    private final UsersApiResource userApiResource;

	@Autowired
	public PartnersWritePlatformServiceImp(final PlatformSecurityContext context,
			final PartnersCommandFromApiJsonDeserializer apiJsonDeserializer,
			final OfficeRepository officeRepository,final RoleRepository roleRepository,
			final UsersApiResource userApiResource) {
		this.context = context;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.officeRepository = officeRepository;
		this.roleRepository = roleRepository;
		this.userApiResource = userApiResource;

	}

	
	@Transactional
	@Override
	public CommandProcessingResult createNewPartner(final JsonCommand command) {

		try {

			final AppUser currentUser = this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			Long parentId = null;
			if (command.parameterExists("parentId")) {
				parentId = command.longValueOfParameterNamed("parentId");
			}
			final Office parentOffice = this.validateUserPriviledgeOnOfficeAndRetrieve(currentUser,parentId);
			Office office = Office.fromPartner(parentOffice, command);
			final String partnerName = command.stringValueOfParameterNamed("partnerName");
			final String partnerType = command.stringValueOfParameterNamed("partnerType");
			final String loginName = command.stringValueOfParameterNamed("loginName");
			final String password = command.stringValueOfParameterNamed("password");
			final String currency = command.stringValueOfParameterNamed("currency");
			final String email = command.stringValueOfParameterNamed("email");
			OfficeAddress address =OfficeAddress.fromJson(command,office);
			OfficeAdditionalInfo additionalInfo = new OfficeAdditionalInfo(office,partnerName, partnerType,currency);
			office.setOfficeAddress(address);
			office.setOfficeAdditionalInfo(additionalInfo);
			this.officeRepository.save(office);
			office.generateHierarchy();
			this.officeRepository.saveAndFlush(office);
			
			//create user
		    final String roleName = command.stringValueOfParameterNamed("roleName");
		    final String[]  roles= arrayOfRole(roleName);
		    JSONObject json = new JSONObject();
		    json.put("username", loginName);
		    json.put("password", password);
		    json.put("repeatPassword", password);
		    json.put("firstname",partnerName);
		    json.put("lastname", partnerName);
		    json.put("sendPasswordToEmail",Boolean.FALSE);
		    json.put("email",email);
		    json.put("officeId", office.getId());
		    json.put("roles", new JSONArray(roles));
	        final String result=this.userApiResource.createUser(json.toString());
	        
			return new CommandProcessingResultBuilder().withCommandId(command.commandId())
					       .withEntityId(office.getId()).build();
		} catch (final DataIntegrityViolationException e) {
			handleDataIntegrityIssues(command, e);
			return new CommandProcessingResult(Long.valueOf(-1l));
		} catch (JSONException e) {
			e.printStackTrace();
			return new CommandProcessingResult(Long.valueOf(-1l));
		}

	}

	 private Office validateUserPriviledgeOnOfficeAndRetrieve(final AppUser currentUser, final Long officeId) {

	        final Long userOfficeId = currentUser.getOffice().getId();
	        final Office userOffice = this.officeRepository.findOne(userOfficeId);
	        if (userOffice == null) { throw new OfficeNotFoundException(userOfficeId); }

	        if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(officeId)) { throw new NoAuthorizationException(
	                "User does not have sufficient priviledges to act on the provided office."); }

	        Office officeToReturn = userOffice;
	        if (!userOffice.identifiedBy(officeId)) {
	            officeToReturn = this.officeRepository.findOne(officeId);
	            if (officeToReturn == null) { throw new OfficeNotFoundException(officeId); }
	        }

	        return officeToReturn;
	    }


	private String[] arrayOfRole(final String name) {
		
		  final Role role = this.roleRepository.findOneByName(name);
          if (role == null) { throw new RoleNotFoundException(Long.valueOf(name)); }
          String[] roles={role.getId().toString()};
          return roles;
	
	}
	

	private void handleDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {

		final Throwable realCause = dve.getMostSpecificCause();
		LOGGER.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException(
				"error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());
	}
}
package org.mifosplatform.organisation.partner.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.organisation.partner.service.PartnersWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

	@Service
	public class CreatePartnerCommandHandler implements NewCommandSourceHandler {

	    private final PartnersWritePlatformService writePlatformService;

	    @Autowired
	    public CreatePartnerCommandHandler(final PartnersWritePlatformService writePlatformService) {
	        this.writePlatformService = writePlatformService;
	    }

	    @Transactional
	    @Override
	    public CommandProcessingResult processCommand(final JsonCommand command) {

	        return this.writePlatformService.createNewPartner(command);
	    }
	}



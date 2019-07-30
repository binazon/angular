package com.orange.srs.refreport.provider.service.rest;

import javax.ejb.Stateless;
import javax.ws.rs.Path;

import com.orange.srs.statcommon.provider.service.rest.GlobalConfigurationService;

@Stateless
@Path("configuration")
public class ConfigurationService extends GlobalConfigurationService {

}
package com.orange.srs.refreport.provider.service.rest.inventory;

import java.io.FileNotFoundException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import com.orange.srs.refreport.business.SOA01InventoryFacade;
import com.orange.srs.refreport.technical.exception.BusinessException;
import com.orange.srs.statcommon.model.enums.InventoryFileTypeEnum;
import com.orange.srs.statcommon.model.parameter.SOAContext;
import com.orange.srs.statcommon.technical.SOATools;
import com.orange.srs.statcommon.technical.exception.rest.RestResponseFactory;

/**
 * Rest services to get inventories files
 * 
 * @author A128239
 */
@Stateless
@Path("inventoryFile")
public class InventoryFileService {

	private static final Logger LOGGER = Logger.getLogger(InventoryFileService.class);

	@EJB
	private SOA01InventoryFacade inventoryFacade;

	/**
	 * Retrieves the inventory entity file to return
	 * 
	 * @param origin
	 *            Origin
	 * @param reportingGroupRef
	 *            Reporting group ref
	 * @return {@link Status#OK} : The ouput stream correspond to the file <br>
	 *         {@link Status#NO_CONTENT} : The inventory file is not found <br>
	 *         {@link Status#PRECONDITION_FAILED} : The reporting group is not found or if there is multiple reporting
	 *         group in database that correspond to the given key
	 */
	@GET
	@Path("entity/{origin}/{reportingGroupRef}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getInventoryEntityFile(@PathParam("origin") String origin,
			@PathParam("reportingGroupRef") String reportingGroupRef) {
		return getInventoryFilePrivate(origin, reportingGroupRef, InventoryFileTypeEnum.ENTITY);
	}

	/**
	 * Retrieves the inventory report interactive file to return
	 * 
	 * @param origin
	 *            Origin
	 * @param reportingGroupRef
	 *            Reporting group ref
	 * @return {@link Status#OK} : The ouput stream correspond to the file <br>
	 *         {@link Status#NO_CONTENT} : The inventory file is not found <br>
	 *         {@link Status#PRECONDITION_FAILED} : The reporting group is not found or if there is multiple reporting
	 *         group in database that correspond to the given key
	 */
	@GET
	@Path("report/{origin}/{reportingGroupRef}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getInventoryReportInteractiveFile(@PathParam("origin") String origin,
			@PathParam("reportingGroupRef") String reportingGroupRef) {
		return getInventoryFilePrivate(origin, reportingGroupRef, InventoryFileTypeEnum.REPORT_INTERACTIVE);
	}

	/**
	 * Retrieves the inventory report template file to return
	 * 
	 * @param origin
	 *            Origin
	 * @param reportingGroupRef
	 *            Reporting group ref
	 * @return {@link Status#OK} : The ouput stream correspond to the file <br>
	 *         {@link Status#NO_CONTENT} : The inventory file is not found <br>
	 *         {@link Status#PRECONDITION_FAILED} : The reporting group is not found or if there is multiple reporting
	 *         group in database that correspond to the given key
	 */
	@GET
	@Path("report/template/{origin}/{reportingGroupRef}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getInventoryReportTemplateFile(@PathParam("origin") String origin,
			@PathParam("reportingGroupRef") String reportingGroupRef) {
		return getInventoryFilePrivate(origin, reportingGroupRef, InventoryFileTypeEnum.REPORT_TEMPLATE);
	}

	/**
	 * Retrieves the inventory file to return
	 * 
	 * @param origin
	 *            Origin
	 * @param reportingGroupRef
	 *            Reporting group ref
	 * @param inventoryFileType
	 *            {@link InventoryFileTypeEnum} used to distinguish which inventory file to get
	 * @return {@link Status#OK} : The output stream correspond to the file <br>
	 *         {@link Status#NO_CONTENT} : The inventory file is not found <br>
	 *         {@link Status#PRECONDITION_FAILED} : The reporting group is not found or if there is multiple reporting
	 *         group in database that correspond to the given key
	 */
	private Response getInventoryFilePrivate(String origin, String reportingGroupRef,
			InventoryFileTypeEnum inventoryFileType) {

		// build SOA Context for logging
		SOAContext soaContext = SOATools.buildSOAContext(null);

		Response response = null;
		try {
			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Get Inventory File service call"));

			StreamingOutput output = inventoryFacade.getInventoryFile(origin, reportingGroupRef, inventoryFileType,
					soaContext);
			response = Response.ok(output).type(MediaType.APPLICATION_OCTET_STREAM).build();

			LOGGER.debug(SOATools.buildSOALogMessage(soaContext, "Get Inventory File response built"));
		} catch (FileNotFoundException | BusinessException ex) {
			// It is an error if no inventory file is found, so return a Status.NO_CONTENT
			// response
			// With this status, the response can not have a entity error (no message-body
			// must be included)
			response = Response.noContent().build();
		} catch (RuntimeException e) {
			response = RestResponseFactory.makeInternalErrorResponseFactoryWithXmlType(e, LOGGER);
		}
		return response;
	}

}
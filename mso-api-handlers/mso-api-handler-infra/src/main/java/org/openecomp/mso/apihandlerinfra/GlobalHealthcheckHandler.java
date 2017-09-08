package org.openecomp.mso.apihandlerinfra;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.openecomp.mso.HealthCheckUtils;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/globalhealthcheck")
@Api(value="/globalhealthcheck",description="APIH Infra Global Health Check")
public class GlobalHealthcheckHandler {

	public final static String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

	@HEAD
    @GET
    @Produces("text/html")
	@ApiOperation(value="Performing global health check",response=Response.class)
    public Response globalHealthcheck (@DefaultValue("true") @QueryParam("enableBpmn") boolean enableBpmn) {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("GlobalHealthcheck");
        // Generate a Request Id
        String requestId = UUIDChecker.generateUUID(msoLogger);
        HealthCheckUtils healthCheck = new HealthCheckUtils ();
        if (!healthCheck.siteStatusCheck (msoLogger, startTime)) {
            return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
        }

        if (healthCheck.verifyGlobalHealthCheck(enableBpmn, requestId)) {
            msoLogger.debug("globalHealthcheck - Successful");
            return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
        } else {
            msoLogger.debug("globalHealthcheck - At leaset one of the sub-modules is not available");
            return  HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
        }
    } 
}

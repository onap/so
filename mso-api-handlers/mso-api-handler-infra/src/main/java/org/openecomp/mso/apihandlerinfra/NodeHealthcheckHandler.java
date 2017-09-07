package org.openecomp.mso.apihandlerinfra;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.openecomp.mso.HealthCheckUtils;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.UUIDChecker;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/nodehealthcheck")
@Api(value="/nodehealthcheck",description="API Handler Infra Node Health Check")
public class NodeHealthcheckHandler {

	public final static String MSO_PROP_APIHANDLER_INFRA = "MSO_PROP_APIHANDLER_INFRA";

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

	@HEAD
    @GET
    @Produces("text/html")
	@ApiOperation(value="Performing node health check",response=Response.class)
    public Response nodeHealthcheck () {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("NodeHealthcheck");
        // Generate a Request Id
        String requestId = UUIDChecker.generateUUID(msoLogger);
        HealthCheckUtils healthCheck = new HealthCheckUtils ();
        if (!healthCheck.siteStatusCheck (msoLogger, startTime)) {
            return HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
        }

        if (healthCheck.verifyNodeHealthCheck(HealthCheckUtils.NodeType.APIH, requestId)) {
            msoLogger.debug("nodeHealthcheck - Successful");
            return HealthCheckUtils.HEALTH_CHECK_RESPONSE;
        } else {
            msoLogger.debug("nodeHealthcheck - At leaset one of the sub-modules is not available.");
            return  HealthCheckUtils.HEALTH_CHECK_NOK_RESPONSE;
        }
    }
}

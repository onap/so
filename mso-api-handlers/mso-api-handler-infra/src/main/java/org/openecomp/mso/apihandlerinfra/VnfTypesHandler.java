/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.apihandlerinfra;


import org.openecomp.mso.apihandlerinfra.vnfbeans.ObjectFactory;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfType;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfTypes;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.utils.UUIDChecker;
import org.apache.http.HttpStatus;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import java.io.StringWriter;
import java.util.List;

@Path(Constants.VNF_TYPES_PATH)
@Api(value="/{version: v1|v2|v3}/vnf-types",description="API Requests of vnfTypes")
public class VnfTypesHandler {

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);


    @GET
    @ApiOperation(value="Finds Vnf Types",response=Response.class)
    public Response getVnfTypes (@QueryParam("vnf-role") String vnfRole, @PathParam("version") String version) {

        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("GetVnfTypes");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
        msoLogger.debug ("Incoming request received for getVnfTypes with vnf-role:" + vnfRole);

        List <VnfResource> vnfResources = null;
        try(CatalogDatabase db = CatalogDatabase.getInstance()) {
            if (vnfRole != null) {
                vnfResources = db.getVnfResourcesByRole (vnfRole);
            } else {
                vnfResources = db.getAllVnfResources ();
            }
        } catch (Exception e) {
            msoLogger.debug ("No connection to catalog DB", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "No connection to catalog DB");
            msoLogger.debug ("End of the transaction, the final response is: " + e.toString ());
            return Response.status (HttpStatus.SC_NOT_FOUND).entity (e.toString ()).build ();
        }

        if (vnfResources == null) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "Error:no vnf types found");
            msoLogger.debug ("End of the transaction. No VNF Types found. The final response status is: " + HttpStatus.SC_NOT_FOUND);
            return Response.status (HttpStatus.SC_NOT_FOUND).entity ("").build ();
        }

        ObjectFactory beansObjectFactory = new ObjectFactory ();
        VnfTypes vnfTypes = beansObjectFactory.createVnfTypes ();
        for (VnfResource vnfResource : vnfResources) {
            VnfType vnfType = beansObjectFactory.createVnfType();
            VnfResource vr = vnfResource;
            vnfType.setDescription(vr.getDescription());
            vnfType.setId(String.valueOf(vr.getModelUuid()));
            vnfTypes.getVnfType().add(vnfType);
        }

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VnfTypes.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal (vnfTypes, stringWriter);

        } catch (JAXBException e) {
            msoLogger.debug ("Error marshalling", e);
        }

        String response = stringWriter.toString ();
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        msoLogger.debug ("End of the transaction, the final response is: " + response);
        return Response.status (HttpStatus.SC_OK).entity (response).build ();
    }
}

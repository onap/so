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


import java.io.StringWriter;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.http.HttpStatus;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VfModuleModelName;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VfModuleModelNames;
import org.openecomp.mso.apihandlerinfra.vnfbeans.ObjectFactory;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.logger.MsoLogger;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path(Constants.VF_MODULE_MODEL_NAMES_PATH)
@Api(value="/{version: v2|v3}/vf-module-model-names",description="API Requests to find Vf Module model names")
public class VfModuleModelNamesHandler {

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);
    private static final String LOG_SERVICE_NAME = "InfrastructurePortal:MSO-APIH.";

    @GET
    @ApiOperation(value="Finds Vf Module Model Names",response=Response.class)
    public Response getVfModuleModelNames (@PathParam("version") String version) {
        long startTime = System.currentTimeMillis ();
        String methodName = "getVfModuleModelNames";
        MsoLogger.setServiceName (LOG_SERVICE_NAME + methodName);
        msoLogger.debug ("Incoming request received for vfModuleModelNames");
        List <VfModule> vfModules = null;
        try (CatalogDatabase db = CatalogDatabase.getInstance()){
            vfModules = db.getAllVfModules ();
        } catch (Exception e) {
            msoLogger.debug ("No connection to catalog DB", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "no connection to catalog DB");
            msoLogger.debug ("End of the transaction, the final response is: " + e.toString ());
            return Response.status (HttpStatus.SC_NOT_FOUND).entity (e.toString ()).build ();
        }

        if (vfModules == null) {
        	msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "VfModule not found");
            msoLogger.debug ("End of the transaction. VfModuleModelName not found the final response status: " + HttpStatus.SC_NOT_FOUND);
            return Response.status (HttpStatus.SC_NOT_FOUND).entity ("").build ();
        }

        ObjectFactory beansObjectFactory = new ObjectFactory ();
        VfModuleModelNames vfModuleModelNames = beansObjectFactory.createVfModuleModelNames ();
        for (VfModule vfModule : vfModules) {
            VfModuleModelName vfModuleModelName = beansObjectFactory.createVfModuleModelName();
            VfModule vm = vfModule;
            vfModuleModelName.setModelName(vm.getModelName());
            vfModuleModelName.setModelVersion(vm.getVersion());
            vfModuleModelName.setModelInvariantUuid(vm.getModelInvariantUuid());
            vfModuleModelName.setIsBase(vm.isBase());
            vfModuleModelName.setDescription(vm.getDescription());
            vfModuleModelName.setId(String.valueOf(vm.getModelUUID()));
            vfModuleModelName.setAsdcServiceModelVersion(vm.getVersion());
            vfModuleModelNames.getVfModuleModelName().add(vfModuleModelName);
        }

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (VfModuleModelNames.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal (vfModuleModelNames, stringWriter);

        } catch (JAXBException e) {
            msoLogger.debug ("Error marshalling", e);
        }

        String response = stringWriter.toString ();
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        msoLogger.debug ("End of the transaction, the final response is: " + response);
        return Response.status (HttpStatus.SC_OK).entity (response).build ();
    }
}

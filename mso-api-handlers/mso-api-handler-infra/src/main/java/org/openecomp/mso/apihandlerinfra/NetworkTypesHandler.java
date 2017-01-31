/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.http.HttpStatus;

import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkType;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkTypes;
import org.openecomp.mso.apihandlerinfra.networkbeans.ObjectFactory;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.UUIDChecker;

@Path(Constants.NETWORK_TYPES_PATH)
public class NetworkTypesHandler {

    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH);

    @GET
    public Response getNetworkTypes () {
        long startTime = System.currentTimeMillis ();
        MsoLogger.setServiceName ("getNetworkTypes");
        // Generate a Request Id
        UUIDChecker.generateUUID(msoLogger);
        msoLogger.debug ("Incoming request received for getNetworkTypes");

        List <NetworkResource> networkResources = null;
        try (CatalogDatabase db = new CatalogDatabase()){
            networkResources = db.getAllNetworkResources ();
        } catch (Exception e) {
            msoLogger.debug ("No connection to catalog DB", e);
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DBAccessError, "no connection to catalog DB");
            msoLogger.debug ("End of the transaction, the final response is: " + e.toString ());
            return Response.status (HttpStatus.SC_NOT_FOUND).entity (e.toString ()).build ();
        }

        if (networkResources == null) {
            msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.ERROR, MsoLogger.ResponseCode.DataNotFound, "NetworkType not found");
            msoLogger.debug ("End of the transaction. NetworkType not found the final response status: " + HttpStatus.SC_NOT_FOUND);
            return Response.status (HttpStatus.SC_NOT_FOUND).entity ("").build ();
        }

        ObjectFactory beansObjectFactory = new ObjectFactory ();
        NetworkTypes networkTypes = beansObjectFactory.createNetworkTypes ();
        for (int i = 0; i < networkResources.size (); i++) {
            NetworkType networkType = beansObjectFactory.createNetworkType ();
            NetworkResource vr = networkResources.get (i);
            networkType.setType (vr.getNetworkType ());
            networkType.setDescription (vr.getDescription ());
            networkType.setId (String.valueOf (vr.getId ()));
            networkTypes.getNetworkType ().add (networkType);
        }

        StringWriter stringWriter = new StringWriter ();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (NetworkTypes.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();

            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal (networkTypes, stringWriter);

        } catch (JAXBException e) {
            msoLogger.debug ("Error marshalling", e);
        }

        String response = stringWriter.toString ();
        msoLogger.recordAuditEvent (startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc, "Successful");
        msoLogger.debug ("End of the transaction, the final response is: " + response);
        return Response.status (HttpStatus.SC_OK).entity (response).build ();
    }
}

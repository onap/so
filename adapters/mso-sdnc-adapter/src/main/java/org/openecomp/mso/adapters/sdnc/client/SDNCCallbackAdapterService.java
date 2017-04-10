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

package org.openecomp.mso.adapters.sdnc.client;


import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;

/**
 * This class was generated by Apache CXF 2.7.11.redhat-3
 * 2015-01-28T11:07:02.074-05:00
 * Generated source version: 2.7.11.redhat-3
 *
 */
//SDNCAdapter to BPEL Async response WEB Service
@WebServiceClient(name = "SDNCCallbackAdapterService",
                  wsdlLocation = "main/resources/SDNCCallbackAdapter.wsdl",
                  targetNamespace = "http://org.openecomp/workflow/sdnc/adapter/callback/wsdl/v1")
public class SDNCCallbackAdapterService extends Service {

	private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://org.openecomp/workflow/sdnc/adapter/callback/wsdl/v1", "SDNCCallbackAdapterService");
    public final static QName SDNCCallbackAdapterSoapHttpPort = new QName("http://org.openecomp/workflow/sdnc/adapter/callback/wsdl/v1", "SDNCCallbackAdapterSoapHttpPort");
    static {
        URL wsdlUrl = null;
        try {
        	wsdlUrl = Thread.currentThread().getContextClassLoader().getResource("main/resources/SDNCCallbackAdapter.wsdl");
        	//wsdlUrl = SDNCCallbackAdapterService.class.getClassLoader().getResource("SDNCCallbackAdapter.wsdl");
        } catch (Exception e) {
            msoLogger.error(MessageEnum.RA_WSDL_NOT_FOUND, "SDNCCallbackAdapter.wsdl", "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception - WSDL not found", e);
        }
        if(wsdlUrl == null) {
        	msoLogger.error(MessageEnum.RA_WSDL_NOT_FOUND, "SDNCCallbackAdapter.wsdl", "SDNC", "", MsoLogger.ErrorCode.DataError, "WSDL not found");
    	} else {
    		try {
    			msoLogger.info(MessageEnum.RA_PRINT_URL, "SDNCCallbackAdapter.wsdl", wsdlUrl.toURI().toString(), "SDNC", "");
			} catch (Exception e) {
				msoLogger.error(MessageEnum.RA_WSDL_URL_CONVENTION_EXC, "SDNCCallbackAdapter.wsdl", "SDNC", "", MsoLogger.ErrorCode.DataError, "Exception - URL convention problem", e);
			}
    	}
        WSDL_LOCATION = wsdlUrl;
    }

    public SDNCCallbackAdapterService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public SDNCCallbackAdapterService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SDNCCallbackAdapterService() {
        super(WSDL_LOCATION, SERVICE);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public SDNCCallbackAdapterService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public SDNCCallbackAdapterService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public SDNCCallbackAdapterService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns SDNCCallbackAdapterPortType
     */
    @WebEndpoint(name = "SDNCCallbackAdapterSoapHttpPort")
    public SDNCCallbackAdapterPortType getSDNCCallbackAdapterSoapHttpPort() {
        return super.getPort(SDNCCallbackAdapterSoapHttpPort, SDNCCallbackAdapterPortType.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns SDNCCallbackAdapterPortType
     */
    @WebEndpoint(name = "SDNCCallbackAdapterSoapHttpPort")
    public SDNCCallbackAdapterPortType getSDNCCallbackAdapterSoapHttpPort(WebServiceFeature... features) {
        return super.getPort(SDNCCallbackAdapterSoapHttpPort, SDNCCallbackAdapterPortType.class, features);
    }

}

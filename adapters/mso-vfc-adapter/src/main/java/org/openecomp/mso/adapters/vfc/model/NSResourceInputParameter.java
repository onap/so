/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.adapters.vfc.model;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.openecomp.mso.logger.MsoLogger;

/**
 * NS Create Input Parameter For VFC Adapter<br>
 * <p>
 * </p>
 * 
 * @version ONAP Amsterdam Release 2017/1/7
 */
public class NSResourceInputParameter {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    
    private NsOperationKey nsOperationKey;

    private String nsServiceName;

    private String nsServiceDescription;

    private NsParameters nsParameters;



    
    /**
     * @return Returns the nsServiceName.
     */
    public String getNsServiceName() {
        return nsServiceName;
    }

    
    /**
     * @param nsServiceName The nsServiceName to set.
     */
    public void setNsServiceName(String nsServiceName) {
        this.nsServiceName = nsServiceName;
    }

    
    /**
     * @return Returns the nsServiceDescription.
     */
    public String getNsServiceDescription() {
        return nsServiceDescription;
    }

    
    /**
     * @param nsServiceDescription The nsServiceDescription to set.
     */
    public void setNsServiceDescription(String nsServiceDescription) {
        this.nsServiceDescription = nsServiceDescription;
    }

    /**
     * @return Returns the nsParameters.
     */
    public NsParameters getNsParameters() {
        return nsParameters;
    }

    /**
     * @param nsParameters The nsParameters to set.
     */
    public void setNsParameters(NsParameters nsParameters) {
        this.nsParameters = nsParameters;
    }

    public NsOperationKey getNsOperationKey() {
        return nsOperationKey;
    }

    public void setNsOperationKey(NsOperationKey nsOperationKey) {
        this.nsOperationKey = nsOperationKey;
    }
    public String toJsonString() {
        String jsonString = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
            jsonString = mapper.writeValueAsString(this);
        } catch (Exception e) {
            LOGGER.debug("Exception:", e);
        }
        return jsonString;
    }

    public String toXmlString() {
        try {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            JAXBContext context = JAXBContext.newInstance(this.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); //pretty print XML
            marshaller.marshal(this, bs);
            return bs.toString();
        } catch (Exception e) {
            LOGGER.debug("Exception:", e);
            return "";
        }
    }
}

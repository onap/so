/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.catalogdb.catalogrest;

import java.io.ByteArrayOutputStream;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class CatalogQueryExceptionCommon {
    private String messageId;
    protected static Logger logger = LoggerFactory.getLogger(CatalogQueryExceptionCommon.class);

    public CatalogQueryExceptionCommon() {
        messageId = null;
    }

    public CatalogQueryExceptionCommon(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String toJsonString() {
        try {
            String jsonString;
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
            jsonString = mapper.writeValueAsString(this);
            return jsonString;
        } catch (Exception e) {
            logger.error("Exception:", e);
            return "";
        }
    }

    public String toXmlString() {
        try {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            JAXBContext context = JAXBContext.newInstance(this.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // pretty print XML
            marshaller.marshal(this, bs);
            return bs.toString();
        } catch (Exception e) {
            logger.error("Exception:", e);
            return "";
        }
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}

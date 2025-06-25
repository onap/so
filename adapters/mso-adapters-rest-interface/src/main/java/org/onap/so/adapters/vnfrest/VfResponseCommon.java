/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.vnfrest;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Everything that is common between all VfModule and VolumeGroup Responses, except for QueryVfModuleResponse and
 * QueryVolumeGroupResponse.
 */
public abstract class VfResponseCommon {
    private static final Logger logger = LoggerFactory.getLogger(VfResponseCommon.class);
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
    }

    private String messageId;

    public VfResponseCommon() {
        messageId = null;
    }

    public VfResponseCommon(String messageId) {
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
            jsonString = mapper.writeValueAsString(this);
            return jsonString;
        } catch (Exception e) {
            logger.debug("Exception :", e);
            return "";
        }
    }

    public String toXmlString() {
        try {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            JAXBContext context = JAXBContext.newInstance(this.getClass(), ArrayList.class, HashMap.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); // pretty print XML
            marshaller.marshal(this, bs);
            return bs.toString();
        } catch (Exception e) {
            logger.debug("Exception :", e);
            return "";
        }
    }
}

/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.openecomp.mso.logger.MsoLogger;

public abstract class NetworkMessageCommon {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

    private String messageId;

    public NetworkMessageCommon() {
        messageId = null;
    }

    public NetworkMessageCommon(String messageId) {
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
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationConfig.Feature.WRAP_ROOT_VALUE);
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            LOGGER.debug("An Exception occurred during converting json to string : ", e);
        }
        return null;
    }

    public String toXmlString() {
        try {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            JAXBContext context = JAXBContext.newInstance(this.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this, bs);
            return bs.toString();
        } catch (Exception e) {
            LOGGER.debug("An Exception occurred during converting xml to string : ", e);
            return "";
        }
    }
}

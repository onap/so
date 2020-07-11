/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.vfc.util;

import java.io.IOException;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.adapters.vfc.constant.HttpCode;
import org.onap.so.adapters.vfc.exceptions.ApplicationException;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for json analyzing.<br/>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-9-6
 */
public class JsonUtil {

    /**
     * Log service
     */
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * Mapper.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String UNMARSHAL_FAIL_MSG = "fail to unMarshal json";
    static {
        MAPPER.setConfig(MAPPER.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        MAPPER.setSerializationInclusion(Include.NON_NULL);
    }

    /**
     * Constructor<br/>
     * <p>
     * </p>
     * 
     * @since ONAP Amsterdam Release 2017-9-6
     */
    private JsonUtil() {

    }

    /**
     * Parse the string in form of json.<br/>
     * 
     * @param jsonstr json string.
     * @param type that convert json string to
     * @return model object
     * @since ONAP Amsterdam Release 2017-9-6
     */
    public static <T> T unMarshal(String jsonstr, Class<T> type) throws ApplicationException {
        try {
            return MAPPER.readValue(jsonstr, type);
        } catch (IOException e) {
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_NS_EXC.toString(),
                    ErrorCode.BusinessProcessError.getValue(), UNMARSHAL_FAIL_MSG, e);
            throw new ApplicationException(HttpCode.BAD_REQUEST, UNMARSHAL_FAIL_MSG);
        }
    }

    /**
     * Parse the string in form of json.<br/>
     * 
     * @param jsonstr json string.
     * @param type that convert json string to
     * @return model object
     * @since ONAP Amsterdam Release 2017-9-6
     */
    public static <T> T unMarshal(String jsonstr, TypeReference<T> type) throws ApplicationException {
        try {
            return MAPPER.readValue(jsonstr, type);
        } catch (IOException e) {
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_NS_EXC.toString(),
                    ErrorCode.BusinessProcessError.getValue(), UNMARSHAL_FAIL_MSG, e);
            throw new ApplicationException(HttpCode.BAD_REQUEST, UNMARSHAL_FAIL_MSG);
        }
    }

    /**
     * Convert object to json string.<br/>
     * 
     * @param srcObj data object
     * @return json string
     * @since ONAP Amsterdam Release 2017-9-6
     */
    public static String marshal(Object srcObj) throws ApplicationException {
        try {
            return MAPPER.writeValueAsString(srcObj);
        } catch (IOException e) {
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_NS_EXC.toString(),
                    ErrorCode.BusinessProcessError.getValue(), "fail to marshal json", e);
            throw new ApplicationException(HttpCode.BAD_REQUEST, "srcObj marshal failed!");
        }
    }

    /**
     * Get mapper.<br/>
     * 
     * @return mapper
     * @since ONAP Amsterdam Release 2017-9-6
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}

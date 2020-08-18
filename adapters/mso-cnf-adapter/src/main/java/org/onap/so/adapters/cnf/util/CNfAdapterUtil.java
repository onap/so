/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.adapters.cnf.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.onap.so.adapters.cnf.exceptions.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onap.logging.filter.base.ErrorCode;
import static org.onap.so.logger.LoggingAnchor.THREE;
import static org.onap.so.logger.MessageEnum.RA_NS_EXC;

public class CNfAdapterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CNfAdapterUtil.class);

    public static final int BAD_REQUEST = 400;

    private static final String UNMARSHAL_FAIL_MSG = "Failed to unmarshal json";

    private static final String MARSHAL_FAIL_MSG = "Failed to marshal object";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static class StatusDesc {

        public static final String ALLOCATE_NSS_SUCCESS = "Allocating nss is " + "successful";

        public static final String CREATE_NSS_SUCCESS = "Creating nss is " + "successful";

        public static final String DEALLOCATE_NSS_SUCCESS = "Deallocate nss " + "is successful";

        public static final String ACTIVATE_NSS_SUCCESS = "Activate nss " + "is successful";

        public static final String DEACTIVATE_NSS_SUCCESS = "Deactivate nss " + "is successful";

        public static final String QUERY_JOB_STATUS_FAILED = "Query job " + "status failed";

        public static final String QUERY_JOB_STATUS_SUCCESS = "Query job " + "status is successful";

        private StatusDesc() {

        }
    }

    private CNfAdapterUtil() {

    }

    public static void assertObjectNotNull(Object object) throws ApplicationException {
        if (null == object) {
            LOGGER.error("Object is null.");
            throw new ApplicationException(BAD_REQUEST, "An object is null.");
        }
    }

    public static <T> T unMarshal(String jsonstr, Class<T> type) throws ApplicationException {
        try {
            return MAPPER.readValue(jsonstr, type);
        } catch (IOException e) {
            LOGGER.error(THREE, RA_NS_EXC.toString(), ErrorCode.BusinessProcessError.getValue(), UNMARSHAL_FAIL_MSG, e);
            throw new ApplicationException(BAD_REQUEST, UNMARSHAL_FAIL_MSG);
        }
    }

    public static String marshal(Object srcObj) throws ApplicationException {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(srcObj);
        } catch (IOException e) {
            LOGGER.error(THREE, RA_NS_EXC.toString(), ErrorCode.BusinessProcessError.getValue(), MARSHAL_FAIL_MSG, e);
            throw new ApplicationException(BAD_REQUEST, MARSHAL_FAIL_MSG);
        }
    }

}

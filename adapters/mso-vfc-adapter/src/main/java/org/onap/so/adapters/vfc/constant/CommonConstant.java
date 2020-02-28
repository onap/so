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

package org.onap.so.adapters.vfc.constant;

/**
 * CommonConstant <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-08-28
 */
public class CommonConstant {

    public static final String STR_EMPTY = "";

    public static final String NFVO_CREATE_URL = "/api/nslcm/v1/ns";
    public static final String SOL005_NFVO_CREATE_URL = "/api/nslcm/v1/ns_instances";

    public static final String NFVO_INSTANTIATE_URL = "/api/nslcm/v1/ns/%s/instantiate";
    public static final String SOL005_NFVO_INSTANTIATE_URL = "/api/nslcm/v1/ns_instances/%s/instantiate";

    public static final String NFVO_TERMINATE_URL = "/api/nslcm/v1/ns/%s/terminate";
    public static final String SOL005_NFVO_TERMINATE_URL = "/api/nslcm/v1/ns_instances/%s/terminate";

    public static final String NFVO_DELETE_URL = "/api/nslcm/v1/ns/%s";
    public static final String SOL005_NFVO_DELETE_URL = "/api/nslcm/v1/ns_instances/%s";

    public static final String NFVO_QUERY_URL = "/api/nslcm/v1/jobs/%s";
    public static final String SOL005_NFVO_QUERY_URL = "/api/nslcm/v1/ns_lcm_op_occs/%s";

    public static final String NFVO_SCALE_URL = "/api/nslcm/v1/ns/%s/scale";

    public enum operationState {
        PROCESSING, COMPLETED, PARTIALLY_COMPLETED, FAILED_TEMP, FAILED, ROLLING_BACK, ROLLED_BACK
    }
    public enum lcmOperationType {
        INSTANTIATE, SCALE, UPDATE, TERMINATE, HEAL
    };
    public enum cancelMode {
        GRACEFUL, FORCEFUL
    };

    /**
     * 
     * <br>
     * <p>
     * </p>
     * 
     * @author
     * @version ONAP Amsterdam Release 2017-08-28
     */
    public static class MethodType {

        public static final String POST = "post";

        public static final String DELETE = "delete";

        public static final String PUT = "put";

        public static final String GET = "get";

        private MethodType() {

        }
    }

    /**
     * 
     * <br>
     * <p>
     * </p>
     * 
     * @author
     * @version ONAP Amsterdam Release 2017-08-28
     */
    public static class Step {

        public static final String CREATE = "create";

        public static final String INSTANTIATE = "instantiate";

        public static final String STATUS = "status";

        public static final String TERMINATE = "terminate";

        public static final String QUERY = "query";

        public static final String DELETE = "delete";

        public static final String SCALE = "scale";

        private Step() {

        }

    }


    public static final String NSD_ID = "nsdId";

    public static final String NS_NAME = "nsName";

    public static final String DESC = "description";

    public static final String NS_INSTANCE_ID = "nsInstanceId";
    public static final String SOL005_NS_INSTANCE_ID = "id";


    public static final String JOB_ID = "jobId";
    public static final String JOB_URI = "Location";

    public static final String ADDITIONAL_PARAM_FOR_NS = "additionalParamForNs";

    public static final String LOCAL_HOST = "localhost";

    public static class StatusDesc {

        public static final String INSTANTIATE_NS_FAILED = "instantiate ns failed";

        public static final String QUERY_JOB_STATUS_FAILED = "query job status failed";

        public static final String TERMINATE_NS_FAILED = "terminate ns failed";

        public static final String DELETE_NS_FAILED = "delete ns failed";

        public static final String CREATE_NS_FAILED = "create ns failed";

        public static final String SCALE_NS_FAILED = "scale ns failed";

        private StatusDesc() {

        }
    }

    private CommonConstant() {

    }
}

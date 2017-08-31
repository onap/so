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
package org.openecomp.mso.adapters.vfc.constant;

/**
 * CommonConstant
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version     ONAP Amsterdam Release  2017-08-28
 */
public class CommonConstant {
    
    public static final String STR_EMPTY = "";

    public static final String CATALOGUE_QUERY_SVC_TMPL_NODETYPE_URL = "/openoapi/catalog/v1/servicetemplates/nesting";

    public static final String SDNO_CREATE_URL = "/openoapi/sdnonslcm/v1/ns";

    public static final String SDNO_INSTANTIATE_URL = "/openoapi/sdnonslcm/v1/ns/%s/instantiate";

    public static final String SDNO_TERMINATE_URL = "/openoapi/sdnonslcm/v1/ns/%s/terminate";

    public static final String SDNO_DELETE_URL = "/openoapi/sdnonslcm/v1/ns/%s";

    public static final String SDNO_QUERY_URL = "/openoapi/sdnonslcm/v1/jobs/%s";

    public static final String NFVO_CREATE_URL = "/openoapi/nslcm/v1/ns";

    public static final String NFVO_INSTANTIATE_URL = "/openoapi/nslcm/v1/ns/%s/instantiate";

    public static final String NFVO_TERMINATE_URL = "/openoapi/nslcm/v1/ns/%s/terminate";

    public static final String NFVO_DELETE_URL = "/openoapi/nslcm/v1/ns/%s";

    public static final String NFVO_QUERY_URL = "/openoapi/nslcm/v1/jobs/%s";

    public static final String GSO_CREATE_URL = "/openoapi/gso/v1/services";

    public static final String GSO_DELETE_URL = "/openoapi/gso/v1/services/%s";

    public static final String GSO_QUERY_URL = "/openoapi/gso/v1/services/%s/operations/%s";

    public static final String LEFT_QUOTE_LEFT_BRACE = "\"\\{";

    public static final String LEFT_BRACE = "\\{";

    public static final String RIGHT_BRACE_RIGHT_QUOTE = "\\}\"";

    public static final String RIGHT_BRACE = "\\}";

    public static final String LEFT_QUOTE_LEFT_BRACKET = "\"\\[";

    public static final String LEFT_BRACKET = "\\[";

    public static final String RIGHT_BRACKET_RIGHT_QUOTE = "\\]\"";

    public static final String RIGHT_BRACKET = "\\]";

    /**
     * HttpContext constant
     * <br>
     * <p>
     * </p>
     * 
     * @author
     * @version     ONAP Amsterdam Release  2017-08-28
     */
    public static class HttpContext {

        public static final String CONTENT_TYPE = "Content-Type";

        public static final String MEDIA_TYPE_JSON = "application/json;charset=UTF-8";

        public static final String URL = "url";

        public static final String METHOD_TYPE = "methodType";

        public static final String IP = "ip";

        public static final String PORT = "port";

        public static final String RAW_DATA = "rawData";

        private HttpContext() {

        }
    }

    /**
     * 
     * <br>
     * <p>
     * </p>
     * 
     * @author
     * @version     ONAP Amsterdam Release  2017-08-28
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
     * @version     ONAP Amsterdam Release  2017-08-28
     */
    public static class Step {

        public static final String CREATE = "create";

        public static final String INSTANTIATE = "instantiate";

        public static final String STATUS = "status";

        public static final String TERMINATE = "terminate";

        public static final String QUERY = "query";

        public static final String DELETE = "delete";

        private Step() {

        }

    }


    public static final String NSD_ID = "nsdId";

    public static final String NS_NAME = "nsName";

    public static final String DESC = "description";

    public static final String NS_INSTANCE_ID = "nsInstanceId";

    public static final String JOB_ID = "jobId";

    public static final String ADDITIONAL_PARAM_FOR_NS = "additionalParamForNs";

    public static final String LOCAL_HOST = "localhost";

    public static class StatusDesc {

        public static final String INSTANTIATE_NS_FAILED = "instantiate ns failed";

        public static final String QUERY_JOB_STATUS_FAILED = "query job status failed";

        public static final String TERMINATE_NS_FAILED = "terminate ns failed";

        public static final String DELETE_NS_FAILED = "delete ns failed";

        public static final String CREATE_NS_FAILED = "create ns failed";

        private StatusDesc() {

        }
    }
    
    private CommonConstant() {

    }
}

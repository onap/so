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

package org.onap.so.requestsdb;

/**
 * The constants of the request db <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-08-28
 */
public class RequestsDbConstant {

    public static class Progress {

        public static final String ONE_HUNDRED = "100";

        private Progress() {

        }
    }

    public static class Status {

        public static final String FINISHED = "finished";

        public static final String PROCESSING = "processing";

        public static final String ERROR = "error";

        private Status() {

        }
    }

    public static class OperationType {

        public static final String CREATE = "create";

        public static final String DELETE = "delete";

        private OperationType() {

        }
    }
}

/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logging.filter.base;

public class Constants {
    protected static final String REDACTED = "***REDACTED***";

    public static final class DefaultValues {
        public static final String UNKNOWN = "UNKNOWN";
        public static final String UNKNOWN_TARGET_ENTITY = "Unknown-Target-Entity";
    }

    public static final class HttpHeaders {
        public static final String HEADER_FROM_APP_ID = "X-FromAppId";
        public static final String ONAP_PARTNER_NAME = "X-ONAP-PartnerName";
        public static final String HEADER_REQUEST_ID = "X-RequestID";
        public static final String TRANSACTION_ID = "X-TransactionID";
        public static final String ECOMP_REQUEST_ID = "X-ECOMP-RequestID";
        public static final String ONAP_REQUEST_ID = "X-ONAP-RequestID";
        public static final String INVOCATION_ID_HEADER = "X-InvocationID";
        public static final String TARGET_ENTITY_HEADER = "X-Target-Entity";
    }

    public static final class Property {
        public static final String PARTNER_NAME = "partnerName";
    }
}

/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vnfmadapter;

/**
 * Adapter constants
 */
public class Constants {

    public static final String SERVICE_NAME = "vnfm-adapter";
    public static final String SERVICE_VERSION = "v1";
    public static final String BASE_URL = "/so/" + SERVICE_NAME + "/" + SERVICE_VERSION;
    public static final String PACKAGE_MANAGEMENT_BASE_URL = BASE_URL + "/vnfpkgm/v1";
    public static final String APPLICATION_ZIP = "application/zip";
    public static final String OPERATION_NOTIFICATION_ENDPOINT = "/lcn/VnfLcmOperationOccurrenceNotification";

    private Constants() {}
}

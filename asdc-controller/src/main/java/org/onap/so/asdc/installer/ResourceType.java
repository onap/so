/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.asdc.installer;

/**
 * This enum defines the resource type, it's used for tosca parsing, extraction and ingestion in SO.
 */
public enum ResourceType {

    /**
     * VF resource and the category is not allotted_resource.
     */
    VF_RESOURCE,

    /**
     * PNF resource.
     */
    PNF_RESOURCE,

    /**
     * Other resource type, including VF resource of allotted_resource category.
     */
    OTHER
}

/*
 * ============LICENSE_START==========================================
 * ===================================================================
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 * ECOMP and OpenECOMP are trademarks
 * and service marks of AT&T Intellectual Property.
 *
 */

package org.openecomp.mso.cloud.authentication.wrappers;

import org.openecomp.mso.cloud.CloudIdentity;

final class WrapperTestUtility {

    static final String CLOUD_IDENTITY_MSO_ID = "msoIdTest";
    static final String CLOUD_IDENTITY_MSO_PASS = "msoPassTest";
    static final String EXCEPTION_MESSAGE = "Provided cloud identity is null, cannot extract username and "
            + "password";

    private WrapperTestUtility() {
    }

    static CloudIdentity createCloudIdentity() {
        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setMsoId(CLOUD_IDENTITY_MSO_ID);
        cloudIdentity.setMsoPass(CloudIdentity.encryptPassword(CLOUD_IDENTITY_MSO_PASS));
        return cloudIdentity;
    }
}

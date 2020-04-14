/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.adapters.etsi.sol003.adapter.packagemanagement.subscriptionmanagement;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import com.google.gson.annotations.SerializedName;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
public class OAuthTokenResponse implements Serializable {

    private static final long serialVersionUID = -6455742984985959926L;

    @XmlElement(name = "access_token")
    @SerializedName("access_token")
    private String accessToken;

    /**
     * Get the Accees Token
     *
     * @return the Access Token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Set the Access Token
     *
     * @param accessToken
     */
    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

}

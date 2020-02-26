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
package org.onap.so.asdc.etsi.pkg.processor;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import org.apache.commons.codec.binary.Base64;
import org.onap.so.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class SdcBasicHttpConfigurationProvider {

    @Value("${sdc.endpoint:https://sdc-be.onap:8443}")
    private String endPoint;

    @Value("${sdc.username:mso}")
    private String username;

    @Value(value = "${sdc.password:76966BDD3C7414A03F7037264FF2E6C8EEC6C28F2B67F2840A1ED857C0260FEE731D73F47F828E5527125D29FD25D3E0DE39EE44C058906BF1657DE77BF897EECA93BDC07FA64F}")
    private String password;

    @Value(value = "${sdc.key:566B754875657232314F5548556D3665}")
    private String key;


    public String getBasicAuthorization() throws GeneralSecurityException {
        final String auth = username + ":" + CryptoUtils.decrypt(password, key);
        final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
        return "Basic " + new String(encodedAuth);
    }

    public String getEndPoint() {
        return endPoint;
    }


}

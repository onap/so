/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited. All rights reserved.
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

package org.onap.so.adapters.oof.utils;

import static org.mockito.Mockito.when;
import java.security.GeneralSecurityException;
import javax.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.utils.CryptoUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class OofUtilsTest {

    @InjectMocks
    OofUtils oofUtils;

    @Mock
    Environment env;

    @Test
    void testGetCamundaMsgUrl() {
        when(env.getRequiredProperty(Mockito.anyString())).thenReturn("dummyString");
        String camundamsgUrl = oofUtils.getCamundaMsgUrl("samplemessage", "sampleCorrelator");
        Assertions.assertNotNull(camundamsgUrl);
    }


    void testGetCamundaHeaders() throws GeneralSecurityException {
        when(env.getRequiredProperty(Mockito.anyString())).thenReturn("dummyString");
        when(CryptoUtils.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn("decryptedString");
        HttpHeaders headers = oofUtils.getCamundaHeaders();
        Assertions.assertNotNull(headers);
    }


    @Test
    void testGetOofHttpHeaders() throws Exception {
        when(env.getRequiredProperty(Mockito.anyString())).thenReturn("dummyString");
        HttpHeaders headers = oofUtils.getOofHttpHeaders();
        Assertions.assertNotNull(headers);
    }

    @Test
    void testGetOofurl() {
        when(env.getRequiredProperty(Mockito.anyString())).thenReturn("dummyString");
        String oofurl = oofUtils.getOofurl("/api/v1/");
        Assertions.assertNotNull(oofurl);
    }

}

/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.so.adapters.vevnfm.aai.AaiConnection;
import org.onap.so.adapters.vevnfm.configuration.ConfigProperties;

@RunWith(MockitoJUnitRunner.class)
public class StartupServiceTest {

    private static final String URL = "rt";
    private static final String ENDPOINT = "localhost";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private ConfigProperties configProperties;

    @Mock
    private AaiConnection aaiConnection;

    @InjectMocks
    private StartupService startupService;

    @Test
    public void testSuccess() throws Exception {
        // given
        final EsrSystemInfo info = new EsrSystemInfo();
        info.setServiceUrl(URL);
        final List<EsrSystemInfo> infos = Collections.singletonList(info);

        when(aaiConnection.receiveVnfm()).thenReturn(infos);

        // when
        final List<EsrSystemInfo> systemInfo = startupService.receiveVnfm();

        // then
        verify(aaiConnection).receiveVnfm();
        assertEquals(infos, systemInfo);
    }
}

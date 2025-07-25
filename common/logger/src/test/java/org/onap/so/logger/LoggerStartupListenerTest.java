/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.so.logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.net.UnknownHostException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ch.qos.logback.core.Context;

@ExtendWith(MockitoExtension.class)
public class LoggerStartupListenerTest {

    private LoggerStartupListener loggerStartupListener;

    @Mock
    private Context context;

    @BeforeEach
    public void setUp() {
        loggerStartupListener = new LoggerStartupListener();
        loggerStartupListener.setContext(context);
    }

    @Test
    public void thatServerNameIsSetOnStartup() throws UnknownHostException {
        loggerStartupListener.start();

        verify(context).putProperty(eq("server.name"), anyString());
        assertTrue(loggerStartupListener.isStarted());
    }
}

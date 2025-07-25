/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

@Component
public class LoggerStartupListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {

    private boolean started = false;
    private static final Logger logger = LoggerFactory.getLogger(LoggerStartupListener.class);

    @Override
    public void start() {
        if (started)
            return;
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.error("UnknownHostException", e);

        }
        Context context = getContext();
        if (addr != null) {
            context.putProperty("server.name", addr.getHostName());
        }
        started = true;
    }

    @Override
    public void stop() {}

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onReset(LoggerContext arg0) {}

    @Override
    public void onStart(LoggerContext arg0) {}

    @Override
    public void onStop(LoggerContext arg0) {}

    @Override
    public void onLevelChange(ch.qos.logback.classic.Logger logger, Level level) {}
}

/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logging.filter.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.onap.so.logging.filter.base.AbstractMDCSetupAspect;
import org.onap.so.logging.filter.base.ScheduledTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

// @Primary
@Aspect
@Component("SpringScheduledTasksMDCSetupAspectSO")
public class SpringScheduledTasksMDCSetupAspect extends AbstractMDCSetupAspect {

    protected static Logger logger = LoggerFactory.getLogger(SpringScheduledTasksMDCSetupAspect.class);

    @Around("@annotation(org.onap.so.logging.filter.base.ScheduledLogging)")
    public void logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        setupMDC(joinPoint.getSignature().getName());
        try {
            joinPoint.proceed();
        } catch (ScheduledTaskException e) {
            errorMDCSetup(e.getErrorCode(), e.getMessage());
            logger.error("ScheduledTaskException: ", e);
        }
        exitAndClearMDC();
    }
}

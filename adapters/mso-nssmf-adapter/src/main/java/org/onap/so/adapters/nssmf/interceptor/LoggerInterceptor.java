/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.adapters.nssmf.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.onap.so.adapters.nssmf.annotation.ServiceLogger;
import org.onap.so.adapters.nssmf.util.NssmfAdapterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;

/**
 * support to print logger of service method
 */
@Aspect
@Order(100)
@Component
public class LoggerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggerInterceptor.class);

    @Pointcut("execution(* org.onap.so.adapters.nssmf.service..*(..))")
    public void serviceLogger() {

    }

    @Around("serviceLogger()")
    public Object around(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            Class<?> targetClass = method.getDeclaringClass();

            StringBuilder classAndMethod = new StringBuilder();
            ServiceLogger classAnnotation = targetClass.getAnnotation(ServiceLogger.class);
            ServiceLogger methodAnnotation = method.getAnnotation(ServiceLogger.class);

            if (classAnnotation == null && methodAnnotation == null) {
                return joinPoint.proceed();
            }

            if (classAnnotation != null) {
                if (classAnnotation.ignore()) {
                    return joinPoint.proceed();
                }
                classAndMethod.append(classAnnotation.value()).append("-");
            }

            String target = targetClass.getName() + "#" + method.getName();

            String params = NssmfAdapterUtil.marshal(joinPoint.getArgs());

            logger.info("{} Start: Method = {} \nParams = {}", classAndMethod.toString(), target, params);

            long start = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long timeConsuming = System.currentTimeMillis() - start;

            logger.info("\n{} End: Method = {}, Spend time = {}ms \nResult = {}", classAndMethod.toString(), target,
                    timeConsuming, NssmfAdapterUtil.marshal(result));
            return result;

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}

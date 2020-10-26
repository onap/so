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

import lombok.Data;
import lombok.ToString;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.onap.so.adapters.nssmf.annotation.RequestLogger;
import org.onap.so.adapters.nssmf.annotation.ServiceLogger;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.util.NssmfAdapterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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

    @Pointcut("execution(* org.onap.so.adapters.nssmf.controller..*(..))")
    public void controllerLogger() {

    }

    @Around("controllerLogger()")
    public Object doAroundRequest(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();

        RequestLogger classAnnotation = targetClass.getAnnotation(RequestLogger.class);
        RequestLogger methodAnnotation = method.getAnnotation(RequestLogger.class);

        if ((classAnnotation == null && methodAnnotation == null)
                || (classAnnotation != null && classAnnotation.ignore())
                || (methodAnnotation != null && methodAnnotation.ignore())) {
            return point.proceed();
        }

        long start = System.currentTimeMillis();
        ServletRequestAttributes attributes
                = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        Object result = point.proceed();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setIp(request.getRemoteAddr());
        requestInfo.setUrl(request.getRequestURL().toString());
        requestInfo.setHttpMethod(request.getMethod());
        requestInfo.setClassMethod(String.format("%s.%s", signature.getDeclaringTypeName(),
                signature.getName()));
        requestInfo.setRequestParams(getRequestParamsByProceedingJoinPoint(point));
        requestInfo.setResult(result);
        requestInfo.setTimeCost(System.currentTimeMillis() - start);
        logger.info("Request Info      : {}", NssmfAdapterUtil.marshal(requestInfo));
        return result;
    }

    @AfterThrowing(pointcut = "controllerLogger()", throwing = "e")
    public void doAfterRequestThrow(JoinPoint joinPoint, RuntimeException e) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();

        RequestLogger classAnnotation = targetClass.getAnnotation(RequestLogger.class);
        RequestLogger methodAnnotation = method.getAnnotation(RequestLogger.class);

        if ((classAnnotation == null && methodAnnotation == null)
                || (classAnnotation != null && classAnnotation.ignore())
                || (methodAnnotation != null && methodAnnotation.ignore())) {
            return;
        }
        ServletRequestAttributes attributes
                = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        RequestErrorInfo requestErrorInfo = new RequestErrorInfo();
        requestErrorInfo.setIp(request.getRemoteAddr());
        requestErrorInfo.setUrl(request.getRequestURL().toString());
        requestErrorInfo.setHttpMethod(request.getMethod());
        requestErrorInfo.setClassMethod(String.format("%s.%s", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName()));
        requestErrorInfo.setRequestParams(getRequestParamsByJoinPoint(joinPoint));
        requestErrorInfo.setException(e);
        String res;
        try {
            res = NssmfAdapterUtil.marshal(requestErrorInfo);
            logger.info("Error Request Info      : {}", res);
        } catch (ApplicationException ex) {
            logger.info("Error Request Info      : {}", requestErrorInfo);
        }
    }

    private Map<String, Object> getRequestParamsByJoinPoint(JoinPoint joinPoint) {
        String[] paramNames = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
        Object[] paramValues = joinPoint.getArgs();

        return buildRequestParam(paramNames, paramValues);
    }


    private Map<String, Object> getRequestParamsByProceedingJoinPoint(ProceedingJoinPoint proceedingJoinPoint) {
        String[] paramNames = ((MethodSignature)proceedingJoinPoint.getSignature()).getParameterNames();
        Object[] paramValues = proceedingJoinPoint.getArgs();

        return buildRequestParam(paramNames, paramValues);
    }

    private Map<String, Object> buildRequestParam(String[] paramNames, Object[] paramValues) {
        Map<String, Object> requestParams = new HashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            Object value = paramValues[i];

            if (value instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) value;
                value = file.getOriginalFilename();
            }

            requestParams.put(paramNames[i], value);
        }

        return requestParams;
    }

    @Data
    @ToString
    private class RequestInfo {
        private String ip;
        private String url;
        private String httpMethod;
        private String classMethod;
        private Object requestParams;
        private Object result;
        private Long timeCost;
    }

    @Data
    @ToString
    private class RequestErrorInfo {
        private String ip;
        private String url;
        private String httpMethod;
        private String classMethod;
        private Object requestParams;
        private RuntimeException exception;
    }
}

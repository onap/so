/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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


package org.openecomp.mso.asdc.util;



import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.sdc.api.notification.INotificationData;


public class NotificationLogging implements InvocationHandler {
	
	private static Map<Object, List<Method>> objectMethodsToLog = new HashMap<>();

	protected static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC);
	
	private static InvocationHandler handler = (arg0, arg1, arg2) -> {
        List<Method> methods = objectMethodsToLog.get(arg0);
        if ((methods == null) || (methods.isEmpty())) {
            // Do nothing for now...
            return null;
        }
        methods.add(arg1);
        return arg1.invoke(arg0, arg2);
    };
	
	public static InvocationHandler getHandler() {
		return handler;
	}
	
	/**
	 * 
	 */
	private NotificationLogging() {}
	
	private static final String[] GETTER_PREFIXES = { "get", "is" };
	
	public static String logNotification(INotificationData iNotif) {
		if (iNotif == null) {
			return "NULL";
		}

		Class<? extends INotificationData> clazz = iNotif.getClass();
		
		Method[] declaredMethods = clazz.getDeclaredMethods();
		
		if (declaredMethods == null || declaredMethods.length == 0) {
			return "EMPTY"; // No declared methods in this class !!!
		}
		
		StringBuilder buffer = new StringBuilder("ASDC Notification:");
		buffer.append(System.lineSeparator());
		
		for (Method m : declaredMethods) {
			if ((m != null) && isGetter(m)) {
				for (String prefix : GETTER_PREFIXES) {
					if (m.getName().startsWith(prefix)) {
						buffer.append(m.getName().substring(prefix.length()));
						break;
					}
				}
				try {
					buffer.append(testNull(m.invoke(iNotif, (Object[])null)));
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					LOGGER.debug("Exception :"+e);
					buffer.append("UNREADABLE");
				}
				buffer.append(System.lineSeparator());
			}
		}
		
		return buffer.toString();
	}
	
	private static final boolean isGetter(Method method) {

		// Must start with a valid (and known) prefix
		boolean prefixFound = false;
		for (String prefix : GETTER_PREFIXES) {
			if (method.getName().startsWith(prefix)) {
				prefixFound = true;
				break;
			}
		}
		if (!prefixFound) {
			return false;
		}

		// Must not take any input arguments
		if (method.getParameterTypes().length != 0) {
			return false;  
		}
		
		// Must not have return type 'void'
		if (void.class.equals(method.getReturnType())) {
			return false;
		}
		
		// Must be public
		if (!Modifier.isPublic(method.getModifiers())) {
			return false;
		}
		
		return true;
	}
	
	private static String testNull(Object object) {
		if (object == null) {
			return "NULL";
		} else if (object instanceof Integer) {
			return object.toString();
		} else if (object instanceof String) {
			return (String) object;
		} else {
			return "Type not recognized";
		}
	}
	
	private static void registerForLog(INotificationData objectToLog) {
		INotificationData proxy = (INotificationData) Proxy.newProxyInstance(
				INotificationData.class.getClassLoader(),
				new Class[] { INotificationData.class },
				NotificationLogging.getHandler());
		objectMethodsToLog.put(proxy, new ArrayList<>());
	}
	
	private static <T> void methodToLog(T methodCall) {
		//
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

}

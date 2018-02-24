/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.client.appc;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import org.openecomp.appc.client.lcm.api.AppcClientServiceFactoryProvider;
import org.openecomp.appc.client.lcm.api.AppcLifeCycleManagerServiceFactory;
import org.openecomp.appc.client.lcm.api.ApplicationContext;
import org.openecomp.appc.client.lcm.api.LifeCycleManagerStateful;
import org.openecomp.appc.client.lcm.api.ResponseHandler;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;
import org.openecomp.appc.client.lcm.model.Status;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


@Component
public class ApplicationControllerSupport {

	private static final int ACCEPT_SERIES = 100;
	private static final int ERROR_SERIES = 200;
	private static final int REJECT_SERIES = 300;
	private static final int SUCCESS_SERIES = 400;
	private static final int SUCCESS_STATUS = SUCCESS_SERIES;
	private static final int PARTIAL_SERIES = 500;
	private static final int PARTIAL_SUCCESS_STATUS = PARTIAL_SERIES;
	private static final int PARTIAL_FAILURE_STATUS = PARTIAL_SERIES + 1;

	@Value("${lcm.model.package:org.openecomp.appc.client.lcm.model}")
	private String lcmModelPackage;

	public LifeCycleManagerStateful createService() throws AppcClientException, IOException {
		AppcLifeCycleManagerServiceFactory factory = AppcClientServiceFactoryProvider
				.getFactory(AppcLifeCycleManagerServiceFactory.class);
		return factory.createLifeCycleManagerStateful(new ApplicationContext(), getLCMProperties());
	}

	/**
	 * @param inputClass
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Object getInput(String action) {
		try {
			return getInputClass(action).newInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			throw new RuntimeException(
					String.format("%s : %s", "Unable to instantiate viable LCM Kit input class for action", action), e);
		}
	}

	/**
	 * @param action
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Method getAPIMethod(String action, LifeCycleManagerStateful lcmStateful, boolean async) {
		Method[] methods = lcmStateful.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equalsIgnoreCase(action)) {
				Class<?>[] methodParameterTypes = method.getParameterTypes();
				if (methodParameterTypes.length > 0) {
					if (getInputClass(action).equals(methodParameterTypes[0])) {
						if (async) {
							if (methodParameterTypes.length == 2
									&& ResponseHandler.class.isAssignableFrom(methodParameterTypes[1])) {
								return method;
							}
						} else if (methodParameterTypes.length == 1) {
							return method;
						}
					}
				}
			}
		}
		throw new RuntimeException(String.format("%s : %s, async=%b",
				"Unable to derive viable LCM Kit API method for action", action, async));
	}

	public Method getCommonHeaderSetterMethod(String action) {
		return getBeanPropertyMethodFor(getInputClass(action), "commonHeader", true);
	}

	public Method getPayloadSetterMethod(String action) {
		return getBeanPropertyMethodFor(getInputClass(action), "payload", true);
	}

	public Status getStatusFromGenericResponse(Object response) {
		Method statusReader = getBeanPropertyMethodFor(response.getClass(), "status", false);
		if (statusReader != null) {
			try {
				return (Status) statusReader.invoke(response);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("Unable to obtain status from LCM Kit response", e);
			}
		}
		return new Status();
	}
	
	public static StatusCategory getCategoryOf(Status status) {
		int codeSeries = status.getCode() - (status.getCode() % 100);
		switch (codeSeries) {
		case ACCEPT_SERIES:
			return StatusCategory.NORMAL;
		case ERROR_SERIES:
		case REJECT_SERIES:
			return StatusCategory.ERROR;
		case SUCCESS_SERIES:
			return status.getCode() == SUCCESS_STATUS ? StatusCategory.NORMAL : StatusCategory.ERROR;
		case PARTIAL_SERIES:
			switch (status.getCode()) {
			case PARTIAL_SUCCESS_STATUS:
				return StatusCategory.NORMAL;
			case PARTIAL_FAILURE_STATUS:
				return StatusCategory.ERROR;
			default:
				return StatusCategory.WARNING;
			}
		default:
			return StatusCategory.WARNING;
		}
	}
	
	public static boolean getFinalityOf(Status status) {
		int codeSeries = status.getCode() - (status.getCode() % 100);
		switch (codeSeries) {
		case ACCEPT_SERIES:
		case PARTIAL_SERIES:
			return false;
		case ERROR_SERIES:
		case REJECT_SERIES:
		case SUCCESS_SERIES:
			return true;
		default:
			return true;
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private Properties getLCMProperties() throws IOException {
		Resource resource = new ClassPathResource("/lcm.properties");
		Properties properties = PropertiesLoaderUtils.loadProperties(resource);
		return properties;
	}

	private Method getBeanPropertyMethodFor(Class<?> clazz, String propertyName, boolean isWriter) {
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(clazz, Object.class);
		} catch (IntrospectionException e) {
			throw new RuntimeException(
					String.format("Unable to produce bean property method for class : %s, property : %s, writer=%b",
							clazz.getName(), propertyName, isWriter),
					e);
		}
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if (propertyDescriptor.getName().equals(propertyName)) {
				return isWriter ? propertyDescriptor.getWriteMethod() : propertyDescriptor.getReadMethod();
			}
		}
		throw new RuntimeException(
				String.format("Unable to produce bean property method for class : %s, property : %s, writer=%b",
						clazz.getName(), propertyName, isWriter));
	}

	/**
	 * @param action
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?> getInputClass(String action) {
		try {
			return Class.forName(lcmModelPackage + '.' + action + "Input");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(String.format("%s : %s using package : ",
					"Unable to identify viable LCM Kit input class for action", action, lcmModelPackage), e);
		}
	}
	
	public static enum StatusCategory { 
	    NORMAL("normal"),
	    WARNING("warning"),
	    ERROR("error");

	    private final String category;

	    private StatusCategory(final String category) {
	        this.category = category;
	    } 

	    @Override 
	    public String toString() {
	        return category;
	    } 
	}
	
	public void logLCMMessage(Object message) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
		String inputAsJSON = writer.writeValueAsString(message);
		System.out.println("LCM Kit input message follows.");
		System.out.println(inputAsJSON);
	}
}

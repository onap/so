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

package org.openecomp.mso.adapter_utils.tests;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.woorea.openstack.base.client.HttpMethod;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.heat.model.Stack;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.utils.KeystoneUtils;
import mockit.Deencapsulation;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudConfigTest;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoIOException;
import org.openecomp.mso.openstack.exceptions.MsoStackAlreadyExists;
import org.openecomp.mso.openstack.exceptions.MsoTenantNotFound;
import org.openecomp.mso.openstack.utils.MsoCommonUtils;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import com.woorea.openstack.heat.model.CreateStackParam;

/**
 * This class implements test methods of the MsoHeatUtils
 *
 *
 */
public class MsoHeatUtilsTest extends MsoCommonUtils {
	public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
	public static CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();
	public static MsoHeatUtils msoHeatUtils;

	@BeforeClass
	public static final void loadClasses() throws MsoCloudIdentityNotFound, MsoPropertiesException {
		ClassLoader classLoader = MsoHeatUtilsTest.class.getClassLoader();
		String cloudConfigJson = classLoader.getResource("cloud_config.json").getPath();
		cloudConfigFactory.initializeCloudConfig(cloudConfigJson, 1);
		msoPropertiesFactory.initializeMsoProperties("NO_PROP", classLoader.getResource("mso.properties").getPath());
		msoHeatUtils = new MsoHeatUtils("NO_PROP", msoPropertiesFactory, cloudConfigFactory);
	}

	@Test
	public final void testCreateStackBadCloudConfig()
			throws MsoStackAlreadyExists, MsoTenantNotFound, MsoException, MsoCloudSiteNotFound {
		try {
			msoHeatUtils.createStack("DOESNOTEXIST", "test", "stackName", "test", new HashMap<>(),
					Boolean.TRUE, 10);
		} catch (MsoCloudSiteNotFound e) {

		} catch (java.lang.NullPointerException npe) {

		}

	}

	@Test
	public final void testCreateStackFailedConnectionHeatClient()
			throws MsoStackAlreadyExists, MsoTenantNotFound, MsoException, MsoCloudSiteNotFound {
		try {
			msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<>(), Boolean.TRUE,
					10);
		} catch (MsoIOException e) {

		}

	}

	@Test
	public final void testCreateStackFailedConnection()
			throws MsoStackAlreadyExists, MsoTenantNotFound, MsoException, MsoCloudSiteNotFound {
		try {
			msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<>(), Boolean.TRUE,
					10);
		} catch (MsoIOException e) {

		}

	}

	@Test
	public final void createStackSuccessWithEnvironment() throws MsoException {
		final MockUp<OpenStackRequest<Access>> mockRequest = new MockUp<OpenStackRequest<Access>>() {
			@Mock
			public Object execute(Invocation invocation) {
				final OpenStackRequest invokedInstance = invocation.getInvokedInstance();
				final Class<?> returnType = Deencapsulation.getField(invokedInstance, "returnType");

				try {
					if (returnType == Access.class) {
						ObjectMapper mapper = new ObjectMapper();
						String json = "{\"token\":{\"id\":\"tokenId\",\"tenant\":{\"id\":\"tenantId\",\"name\":\"tenantName\"}},\"serviceCatalog\":[{\"type\":\"orchestration\",\"name\":\"orchestration\",\"endpoints\":[{\"region\":\"region1\",\"publicURL\":\"http://localhost:5000\",\"internalURL\":\"http://localhost:5000\",\"adminURL\":\"http://localhost:5000\"}]}]}";
						return mapper.readValue(json, Access.class);
					} else if (returnType == Stack.class) {
						final Stack stack = new Stack();
						stack.setId("stackId");
						stack.setStackName("stackName");
						stack.setStackStatus("CREATE_COMPLETE");
						return stack;
					}
					return null;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		final MockUp<KeystoneUtils> mockKeystone = new MockUp<KeystoneUtils>() {
			@Mock
			String findEndpointURL(List<Access.Service> serviceCatalog, String type, String region, String facing) {
				return "http://localhost:5000";
			}
		};

		msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<>(), Boolean.TRUE, 10,
				"environment");

		mockRequest.tearDown();
		mockKeystone.tearDown();
	}

	@Test
	public final void createStackSuccessWithFiles() throws MsoException {
		final MockUp<OpenStackRequest<Access>> mockRequest = new MockUp<OpenStackRequest<Access>>() {
			@Mock
			public Object execute(Invocation invocation) {
				final OpenStackRequest invokedInstance = invocation.getInvokedInstance();
				final Class<?> returnType = Deencapsulation.getField(invokedInstance, "returnType");

				try {
					if (returnType == Access.class) {
						ObjectMapper mapper = new ObjectMapper();
						String json = "{\"token\":{\"id\":\"tokenId\",\"tenant\":{\"id\":\"tenantId\",\"name\":\"tenantName\"}},\"serviceCatalog\":[{\"type\":\"orchestration\",\"name\":\"orchestration\",\"endpoints\":[{\"region\":\"region1\",\"publicURL\":\"http://localhost:5000\",\"internalURL\":\"http://localhost:5000\",\"adminURL\":\"http://localhost:5000\"}]}]}";
						return mapper.readValue(json, Access.class);
					} else if (returnType == Stack.class) {
						final Stack stack = new Stack();
						stack.setId("stackId");
						stack.setStackName("stackName");
						stack.setStackStatus("CREATE_COMPLETE");
						return stack;
					}
					return null;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		final MockUp<KeystoneUtils> mockKeystone = new MockUp<KeystoneUtils>() {
			@Mock
			String findEndpointURL(List<Access.Service> serviceCatalog, String type, String region, String facing) {
				return "http://localhost:5000";
			}
		};

		msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<>(), Boolean.TRUE, 10,
				"environment", new HashMap<>());

		mockRequest.tearDown();
		mockKeystone.tearDown();
	}

	@Test
	public final void createStackSuccessWithHeatFiles() throws MsoException {

		final MockUp<OpenStackRequest<Access>> mockRequest = new MockUp<OpenStackRequest<Access>>() {
			@Mock
			public Object execute(Invocation invocation) {
				final OpenStackRequest invokedInstance = invocation.getInvokedInstance();
				final Class<?> returnType = Deencapsulation.getField(invokedInstance, "returnType");

				try {
					if (returnType == Access.class) {
						ObjectMapper mapper = new ObjectMapper();
						String json = "{\"token\":{\"id\":\"tokenId\",\"tenant\":{\"id\":\"tenantId\",\"name\":\"tenantName\"}},\"serviceCatalog\":[{\"type\":\"orchestration\",\"name\":\"orchestration\",\"endpoints\":[{\"region\":\"region1\",\"publicURL\":\"http://localhost:5000\",\"internalURL\":\"http://localhost:5000\",\"adminURL\":\"http://localhost:5000\"}]}]}";
						return mapper.readValue(json, Access.class);
					} else if (returnType == Stack.class) {
						final Stack stack = new Stack();
						stack.setId("stackId");
						stack.setStackName("stackName");
						stack.setStackStatus("CREATE_COMPLETE");
						return stack;
					}
					return null;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		final MockUp<KeystoneUtils> mockKeystone = new MockUp<KeystoneUtils>() {
			@Mock
			String findEndpointURL(List<Access.Service> serviceCatalog, String type, String region, String facing) {
				return "http://localhost:5000";
			}
		};

		msoHeatUtils.createStack("MT", "test", "stackName", "test", new HashMap<>(), Boolean.TRUE, 10,
				"environment", new HashMap<>(), new HashMap<>());

		mockRequest.tearDown();
		mockKeystone.tearDown();
	}

	@Test
	public final void requestToStringBuilderTest() {
		CreateStackParam param = new CreateStackParam();
		param.setDisableRollback(false);
		param.setEnvironment("environment");
		param.setFiles(new HashMap<>());
		param.setParameters(new HashMap<>());
		param.setStackName("stackName");
		param.setTemplate("template");
		param.setTemplateUrl("http://templateUrl");
		param.setTimeoutMinutes(1);

		msoHeatUtils.requestToStringBuilder(param);
	}

	@Test
	public final void heatCacheResetTest() {
		msoHeatUtils.heatCacheReset();
	}

	@Test
	public final void expireHeatClientTest() {
		msoHeatUtils.expireHeatClient("tenantId", "cloudId");
	}

	@Test
	public final void heatCacheCleanupTest() {
		msoHeatUtils.heatCacheCleanup();
	}

	@Test
	public void queryStackTest() throws MsoException {
		final MockUp<OpenStackRequest<Access>> mockRequest = new MockUp<OpenStackRequest<Access>>() {
			@Mock
			public Object execute(Invocation invocation) {
				final OpenStackRequest invokedInstance = invocation.getInvokedInstance();
				final Class<?> returnType = Deencapsulation.getField(invokedInstance, "returnType");

				try {
					if (returnType == Access.class) {
						ObjectMapper mapper = new ObjectMapper();
						String json = "{\"token\":{\"id\":\"tokenId\",\"tenant\":{\"id\":\"tenantId\",\"name\":\"tenantName\"}},\"serviceCatalog\":[{\"type\":\"orchestration\",\"name\":\"orchestration\",\"endpoints\":[{\"region\":\"region1\",\"publicURL\":\"http://localhost:5000\",\"internalURL\":\"http://localhost:5000\",\"adminURL\":\"http://localhost:5000\"}]}]}";
						return mapper.readValue(json, Access.class);
					} else if (returnType == Stack.class) {
						final Stack stack = new Stack();
						stack.setId("stackId");
						stack.setStackName("stackName");
						stack.setStackStatus("CREATE_COMPLETE");
						return stack;
					}
					return null;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		final MockUp<KeystoneUtils> mockKeystone = new MockUp<KeystoneUtils>() {
			@Mock
			String findEndpointURL(List<Access.Service> serviceCatalog, String type, String region, String facing) {
				return "http://localhost:5000";
			}
		};

		final StackInfo stackInfo = msoHeatUtils.queryStack("MT", "test", "stackName");

		mockRequest.tearDown();
		mockKeystone.tearDown();
	}

	@Test
	public void deleteStack() throws MsoException {
		final MockUp<OpenStackRequest<Access>> mockRequest = new MockUp<OpenStackRequest<Access>>() {
			@Mock
			public Object execute(Invocation invocation) {
				final OpenStackRequest invokedInstance = invocation.getInvokedInstance();
				final Class<?> returnType = Deencapsulation.getField(invokedInstance, "returnType");
				final String path = Deencapsulation.getField(invokedInstance, "endpoint");
//				final String stackName = path.substring(path.lastIndexOf("/"));

				try {
					if (returnType == Access.class) {
						ObjectMapper mapper = new ObjectMapper();
						String json = "{\"token\":{\"id\":\"tokenId\",\"tenant\":{\"id\":\"tenantId\",\"name\":\"tenantName\"}},\"serviceCatalog\":[{\"type\":\"orchestration\",\"name\":\"orchestration\",\"endpoints\":[{\"region\":\"region1\",\"publicURL\":\"http://localhost:5000\",\"internalURL\":\"http://localhost:5000\",\"adminURL\":\"http://localhost:5000\"}]}]}";
						return mapper.readValue(json, Access.class);
					} else if (returnType == Stack.class) {
						final Stack stack = new Stack();
						stack.setId("stackId");
						stack.setStackName("stackName");
						final String status = "DELETE_COMPLETE";
						stack.setStackStatus(status);
						return stack;
					}
					return null;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		final MockUp<KeystoneUtils> mockKeystone = new MockUp<KeystoneUtils>() {
			@Mock
			String findEndpointURL(List<Access.Service> serviceCatalog, String type, String region, String facing) {
				return "http://localhost:5000";
			}
		};

		final StackInfo stackInfo = msoHeatUtils.deleteStack("test", "MT", "stackName", true);

		mockRequest.tearDown();
		mockKeystone.tearDown();
	}

	@Test
	public void copyStringOutputsToInputsTest() {
		Map<String, String> inputs = new HashMap<String, String>(){{put("key41", "value41");}};
		Map<String, Object> outputs = new HashMap<String, Object>(){{
			put("key2", "val2");
			put("key3", new TextNode("val3"));
			put("key4", new LinkedHashMap<String, String>(){{put("key41", "value41");}});
		}};
		msoHeatUtils.copyStringOutputsToInputs(inputs, outputs, true);
	}
}

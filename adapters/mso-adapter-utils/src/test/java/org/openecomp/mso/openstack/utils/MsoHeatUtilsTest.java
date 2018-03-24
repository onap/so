/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
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

package org.openecomp.mso.openstack.utils;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.openstack.beans.MsoTenant;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Tenant;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoHeatUtils.class,CloudSite.class,CloudIdentity.class,Tenant.class,Keystone.class,MsoTenant.class,MsoJavaProperties.class})


public class MsoHeatUtilsTest {

    @Mock
}

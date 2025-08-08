/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.openstack.utils;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.TestDataSetup;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.exceptions.MsoException;
import org.springframework.core.env.Environment;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.heat.Heat;
import com.woorea.openstack.heat.model.Stack;

@RunWith(MockitoJUnitRunner.class)
public class MsoHeatUtilsWithUpdateTest extends TestDataSetup {
    @Mock
    private CloudConfig cloudConfig;

    @Mock
    private Environment environment;

    @Spy
    @InjectMocks
    private MsoHeatUtilsWithUpdate heatUtils;

    private String cloudOwner;
    private String cloudSiteId;
    private String tenantId;
    private String stackName;
    private String heatTemplate;
    private Map<String, Object> stackInputs;
    private boolean pollForCompletion;
    private int timeoutMinutes;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        cloudOwner = "cloudOwner";
        cloudSiteId = "cloudSiteId";
        tenantId = "tenantId";
        stackName = "stackName";
        heatTemplate = "heatTemplate";
        stackInputs = new HashMap<>();
        pollForCompletion = true;
        timeoutMinutes = 0;
    }

    @Test
    public void updateStackTest() throws MsoException, IOException {
        CloudSite cloudSite = new CloudSite();
        Heat heatClient = new Heat("endpoint");
        Stack heatStack = mapper.readValue(new File(RESOURCE_PATH + "HeatStack.json"), Stack.class);
        Stack updateStack = mapper.readValue(new File(RESOURCE_PATH + "UpdateStack.json"), Stack.class);

        StackInfo expectedStackInfo = new StackInfo("stackName", HeatStatus.UPDATED, "stackStatusReason", null);
        expectedStackInfo.setCanonicalName("stackName/id");

        doReturn(heatClient).when(heatUtils).getHeatClient(isA(String.class), isA(String.class));
        doReturn(null).when(heatUtils).executeAndRecordOpenstackRequest(isA(OpenStackRequest.class));
        doReturn("0").when(environment).getProperty(isA(String.class), isA(String.class));
        doReturn(updateStack).when(heatUtils).queryHeatStack(isA(Heat.class), isA(String.class));

        StackInfo actualStackInfo = heatUtils.updateStack(cloudSiteId, cloudOwner, tenantId, stackName, heatTemplate,
                stackInputs, pollForCompletion, timeoutMinutes);

        assertThat(actualStackInfo, sameBeanAs(expectedStackInfo));
    }

    @Test
    public void updateStackWithEnvironmentTest() throws IOException, MsoException {
        String environmentString = "environmentString";

        CloudSite cloudSite = new CloudSite();
        Heat heatClient = new Heat("endpoint");
        Stack heatStack = mapper.readValue(new File(RESOURCE_PATH + "HeatStack.json"), Stack.class);
        Stack updateStack = mapper.readValue(new File(RESOURCE_PATH + "UpdateStack.json"), Stack.class);

        StackInfo expectedStackInfo = new StackInfo("stackName", HeatStatus.UPDATED, "stackStatusReason", null);
        expectedStackInfo.setCanonicalName("stackName/id");

        doReturn(heatClient).when(heatUtils).getHeatClient(isA(String.class), isA(String.class));

        doReturn(null).when(heatUtils).executeAndRecordOpenstackRequest(isA(OpenStackRequest.class));
        doReturn("0").when(environment).getProperty(isA(String.class), isA(String.class));
        doReturn(updateStack).when(heatUtils).queryHeatStack(isA(Heat.class), isA(String.class));

        StackInfo actualStackInfo = heatUtils.updateStack(cloudSiteId, cloudOwner, tenantId, stackName, heatTemplate,
                stackInputs, pollForCompletion, timeoutMinutes, environmentString);

        assertThat(actualStackInfo, sameBeanAs(expectedStackInfo));
    }

    @Test
    public void updateStackWithFilesTest() throws MsoException, IOException {
        String environmentString = "environmentString";
        Map<String, Object> files = new HashMap<>();
        files.put("file1", new Object());

        CloudSite cloudSite = new CloudSite();
        Heat heatClient = new Heat("endpoint");
        Stack heatStack = mapper.readValue(new File(RESOURCE_PATH + "HeatStack.json"), Stack.class);
        Stack updateStack = mapper.readValue(new File(RESOURCE_PATH + "UpdateStack.json"), Stack.class);

        StackInfo expectedStackInfo = new StackInfo("stackName", HeatStatus.UPDATED, "stackStatusReason", null);
        expectedStackInfo.setCanonicalName("stackName/id");

        doReturn(heatClient).when(heatUtils).getHeatClient(isA(String.class), isA(String.class));
        doReturn(null).when(heatUtils).executeAndRecordOpenstackRequest(isA(OpenStackRequest.class));
        doReturn("0").when(environment).getProperty(isA(String.class), isA(String.class));
        doReturn(updateStack).when(heatUtils).queryHeatStack(isA(Heat.class), isA(String.class));

        StackInfo actualStackInfo = heatUtils.updateStack(cloudSiteId, cloudOwner, tenantId, stackName, heatTemplate,
                stackInputs, pollForCompletion, timeoutMinutes, environmentString, files);

        assertThat(actualStackInfo, sameBeanAs(expectedStackInfo));
    }
}

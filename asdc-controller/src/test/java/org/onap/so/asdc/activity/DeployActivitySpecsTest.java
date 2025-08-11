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
package org.onap.so.asdc.activity;

import java.nio.file.Files;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.asdc.activity.beans.ActivitySpec;
import org.onap.so.asdc.activity.beans.ActivitySpecCreateResponse;
import org.onap.so.db.catalog.data.repository.ActivitySpecRepository;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DeployActivitySpecsTest {
    @Mock
    protected Environment env;

    @Mock
    protected ActivitySpecRepository activitySpecRepository;

    @Mock
    protected ActivitySpecsActions activitySpecsActions;

    @InjectMocks
    @Spy
    private DeployActivitySpecs deployActivitySpecs;

    @Test
    public void deployActivitySpecs_Test() throws Exception {
        boolean deploymentSuccessful = true;
        ActivitySpecCreateResponse activitySpecCreateResponse = new ActivitySpecCreateResponse();
        activitySpecCreateResponse.setId("testActivityId");
        ObjectMapper mapper = new ObjectMapper();
        org.onap.so.db.catalog.beans.ActivitySpec catalogActivitySpec = mapper.readValue(
                new String(Files.readAllBytes(Paths.get("src/test/resources/ActivitySpecFromCatalog.json"))),
                org.onap.so.db.catalog.beans.ActivitySpec.class);
        List<org.onap.so.db.catalog.beans.ActivitySpec> catalogActivitySpecList =
                new ArrayList<org.onap.so.db.catalog.beans.ActivitySpec>();
        catalogActivitySpecList.add(catalogActivitySpec);
        when(env.getProperty("mso.asdc.config.activity.endpoint")).thenReturn("http://testEndpoint");
        doReturn(true).when(deployActivitySpecs).checkHttpServerUp("http://testEndpoint");
        when(activitySpecRepository.findAll()).thenReturn(catalogActivitySpecList);
        doReturn("testActivityId").when(activitySpecsActions).createActivitySpec(Mockito.any(), Mockito.any());
        doReturn(true).when(activitySpecsActions).certifyActivitySpec(Mockito.any(), Mockito.any());
        deployActivitySpecs.deployActivities();
        assertTrue(deploymentSuccessful);
    }

    @Test
    public void mapActivitySpecFromCatalogToSdc_Test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        org.onap.so.db.catalog.beans.ActivitySpec catalogActivitySpec = mapper.readValue(
                new String(Files.readAllBytes(Paths.get("src/test/resources/ActivitySpecFromCatalog.json"))),
                org.onap.so.db.catalog.beans.ActivitySpec.class);
        ActivitySpec activitySpec = deployActivitySpecs.mapActivitySpecFromCatalogToSdc(catalogActivitySpec);
        ActivitySpec expected = mapper.readValue(
                new String(Files.readAllBytes(Paths.get("src/test/resources/ActivitySpec.json"))), ActivitySpec.class);
        assertThat(expected, sameBeanAs(activitySpec));
    }
}

/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.so.bpmn.common;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.file.ResourcesFileSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import com.github.tomakehurst.wiremock.common.FileSource;

public class WireMockFixtureLoadingIT {
    @Test
    public void sharedAaiResponsesAreReadableThroughTheConfiguredWireMockRoot() {
        FileSource bodies = workflowBodies();

        for (String file : new String[] {"aai-volume-group-id-info.xml", "CRTGVNF_queryAAIResponseVolume.xml",
                "CRTGVNF_queryAAIResponseVolume_idsNotMatch.xml", "aaiFault.xml"}) {
            assertTrue(file, bodies.getBinaryFileNamed(file).readContents().length > 0);
        }
    }

    @Test
    public void infrastructureResponsesAreReadableThroughTheConfiguredWireMockRoot() {
        FileSource bodies = workflowBodies();
        for (String file : new String[] {"GenericFlows/getSIUrlById.xml",
                "VCPE/DoCreateAllottedResourceTXC/getArTxc2.xml", "VfModularity/UpdateAAIVfModuleRequest.xml"}) {
            assertTrue(file, bodies.getBinaryFileNamed(file).readContents().length > 0);
        }
    }

    private FileSource workflowBodies() {
        String[] roots = BaseIntegrationTest.class.getAnnotation(AutoConfigureWireMock.class).files();
        Resource[] resources = new Resource[roots.length];
        for (int index = 0; index < roots.length; index++) {
            resources[index] = new DefaultResourceLoader().getResource(roots[index]);
        }
        return new ResourcesFileSource(resources).child("__files");

    }
}

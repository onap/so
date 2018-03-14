/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
package org.openecomp.mso.bpmn.common.resource;

import org.junit.Test;

import java.util.HashMap;

public class ResourceRequestBuilderTest {

    @Test
    public void buildResouceRequestTest() throws Exception {

        ResourceRequestBuilder.buildResouceRequest("aa4535",
                "a1074969-944f-4ddc-b687-9550b0c8cd57", new HashMap<>());
    }

    @Test
    public void buildResouceRequestParametersTest() throws Exception {

        String parameters =
                "{            \"locationConstraints\":[            ],            \"resources\":[                {                    \"resourceName\":\"vEPC_ONAP01\",                    \"resourceInvariantUuid\":\"36ebe421-283a-4ee8-92f1-d09e7c44b911\",                    \"resourceUuid\":\"27a0e235-b67a-4ea4-a0cf-25761afed111\",                    \"resourceCustomizationUuid\":\"27a0e235-b67a-4ea4-a0cf-25761afed231\",                    \"parameters\":{                        \"locationConstraints\":[                            {                                \"vnfProfileId\":\"b244d433-8c9c-49ad-9c70-8e34b8dc8328\",                                \"locationConstraints\":{                                    \"vimId\":\"vmware_vio\"                                }                            },                            {                                \"vnfProfileId\":\"8a9f7c48-21ce-41b7-95b8-a8ac61ccb1ff\",                                \"locationConstraints\":{                                    \"vimId\":\"core-dc_RegionOne\"                                }                            }                        ],                        \"resources\":[                        ],                        \"requestInputs\":{                            \"sdncontroller\":\"\"                        }                    }                },                {                    \"resourceName\":\"VL OVERLAYTUNNEL\",                    \"resourceInvariantUuid\":\"184494cf-472f-436f-82e2-d83dddde21cb\",                    \"resourceUuid\":\"95bc3e59-c9c5-458f-ad6e-78874ab4b3cc\",                    \"resourceCustomizationUuid\":\"27a0e235-b67a-4ea4-a0cf-25761afed232\",                    \"parameters\":{                        \"locationConstraints\":[                        ],                        \"resources\":[                        ],                        \"requestInputs\":{                        }                    }                }            ],            \"requestInputs\":{                \"vlunderlayvpn0_name\":\"l3connect\",                \"vlunderlayvpn0_site1_id\":\"IP-WAN-Controller-1\",                \"vlunderlayvpn0_site2_id\":\"SPTNController\",                \"vlunderlayvpn0_site1_networkName\":\"network1,network2\",                \"vlunderlayvpn0_site2_networkName\":\"network3,network4\",                \"vlunderlayvpn0_site1_routerId\":\"a8098c1a-f86e-11da-bd1a-00112444be1a\",                \"vlunderlayvpn0_site2_routerId\":\"a8098c1a-f86e-11da-bd1a-00112444be1e\",                \"vlunderlayvpn0_site2_importRT1\":\"200:1,200:2\",                \"vlunderlayvpn0_site1_exportRT1\":\"300:1,300:2\",                \"vlunderlayvpn0_site2_exportRT1\":\"400:1,400:2\",                \"vlunderlayvpn0_site1_vni\":\"2000\",                \"vlunderlayvpn0_site2_vni\":\"3000\",                \"vlunderlayvpn0_tunnelType\":\"L3-DCI\"            }        }";
        ResourceRequestBuilder.buildResourceRequestParameters(null, "1bd0eae6-2dcc-4461-9ae6-56d641f369d6", "27a0e235-b67a-4ea4-a0cf-25761afed231", parameters);
    }
}
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

package org.onap.so.bpmn.common.scripts;

import static org.mockito.Mockito.*


import groovy.json.JsonSlurper


import static org.junit.Assert.*;

import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.junit.runner.RunWith
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Before;
import org.junit.Test;

@RunWith(MockitoJUnitRunner.class)

class VidUtilsTest {

	String vfModuleReqJson = """
{
  "requestDetails": {
    "modelInfo": {
      "modelType": "vfModule",
      "modelInvariantUuid": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
      "modelNameVersionId": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
      "modelName": "STMTN5MMSC21-MMSC::model-1-0",
      "modelVersion": "1",
      "modelCustomizationUuid": "ee6478e5-ea33-3346-ac12-ab121484a3fe"
    },
    "cloudConfiguration": {
      "lcpCloudRegionId": "MDTWNJ21",
      "tenantId": "fba1bd1e195a404cacb9ce17a9b2b421"
    },
    "requestInfo": {
      "instanceName": "PCRF::module-0-2",
      "source": "VID",
      "suppressRollback": true
    },
    "relatedInstanceList": [
      {
        "relatedInstance": {
          "instanceId": "17ef4658-bd1f-4ef0-9ca0-ea76e2bf122c",
          "instanceName": "MSOTESTVOL103a-vSAMP12_base_module-0_vol",
          "modelInfo": {
            "modelType": "volumeGroup",
            "modelInvariantUuid": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
            "modelNameVersionId": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
            "modelName": "vSAMP12..base..module-0",
            "modelVersion": "1"
          }
        }
      },
      {
        "relatedInstance": {
          "instanceId": "123456",
          "modelInfo": {
            "modelType": "service",
            "modelInvariantUuid": "ff3514e3-5a33-55df-13ab-12abad84e7ff",
            "modelNameVersionId": "fe6985cd-ea33-3346-ac12-ab121484a3fe",
            "modelName": "SERVICE_MODEL_NAME",
            "modelVersion": "1.0"
          }
        }
      },
      {
        "relatedInstance": {
          "instanceId": "skask",
          "instanceName": "skask-test",
          "modelInfo": {
            "modelType": "vnf",
            "modelInvariantUuid": "skask",
            "modelNameVersionId": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
            "modelName": "vSAMP12",
            "modelVersion": "1.0",
            "modelInstanceName": "vSAMP12 1"
          }
        }
      }
    ],
    "requestParameters": {
      "userParams": [
        {
          "name": "vnfName",
          "value": "STMTN5MMSC20"
        },
        {
          "name": "tenantId",
          "value": "vpe-tenant-123"
        },
        {
          "name": "aicCloudRegion",
          "value": "MDTWNJ21"
        },
        {
          "name": "isAvpnService",
          "value": "true"
        },
        {
          "name": "asn",
          "value": "asn-1234"
        },
        {
          "name": "releaseForAero",
          "value": "release-for-aero-something"
        },
        {
          "name": "aicClli",
          "value": "MTJWNJA4LCP"
        },
        {
          "name": "svcProviderPartNumber",
          "value": "svc-provide-number-1234"
        }
      ]
    }
  }
}
"""
	String bpmnReq1 = """
{
	"requestDetails": {
		"modelInfo": {
			"modelType": "volumeGroup",
			"modelId": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
			"modelNameVersionId": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
			"modelName": "vSAMP12::base::module-0",
			"modelVersion": "1"
		},
		"cloudConfiguration": {
			"lcpCloudRegionId": "mdt1",
			"tenantId": "88a6ca3ee0394ade9403f075db23167e"
		},
		"requestInfo": {
			"instanceName": "MSOTESTVOL101a-vSAMP12_base_vol_module-0",
			"source": "VID",
			"suppressRollback": false
		},
		"relatedInstanceList": [
			{
				"relatedInstance": {
					"instanceId": "{service-instance-id}",
					"modelInfo": {
						"modelType": "service",
						"modelId": "ff3514e3-5a33-55df-13ab-12abad84e7ff",
						"modelNameVersionId": "fe6985cd-ea33-3346-ac12-ab121484a3fe",
						"modelName": "Test",
						"modelVersion": "2.0"
					}
				}
			}, {
				"relatedInstance": {
					"instanceId": "{vnf-instance-id}",
					"modelInfo": {
						"modelType": "vnf",
						"modelId": "ff5256d1-5a33-55df-13ab-12abad84e7ff",
						"modelNameVersionId": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
						"modelName": "vSAMP12",
						"modelVersion": "1",
						"modelInstanceName": "vSAMP12"
					}
				}
			}
		],
		"requestParameters": {
			"serviceId": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb",
			"userParams": [
				{"name": "vnfName", "value": "STMTN5MMSC20" },
				{"name": "vnfName2", "value": "US1117MTSNJVBR0246" },
				{"name": "vnfNmInformation", "value": "" },
				{"name": "vnfType", "value": "pcrf-capacity" },
				{"name": "vnfId", "value": "skask" },
				{"name": "vnfStackId", "value": "slowburn" },
				{"name": "vnfStatus", "value": "created" },
				{"name": "aicCloudRegion", "value": "MDTWNJ21" },
				{"name": "availabilityZone", "value": "slcp3-esx-az01" },
				{"name": "oamNetworkName", "value": "VLAN-OAM-1323" },
				{"name": "vmName", "value": "slcp34246vbc246ceb" },
				{"name": "ipagNetworkId", "value": "970cd2b9-7f09-4a12-af47-182ea38ba1f0" },
				{"name": "vpeNetworkId", "value": "545cc2c3-1930-4100-b534-5d82d0e12bb6" }
			]
		}
	}
}
"""

	String vidUtilResp1 = """<volume-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <action>CREATE_VF_MODULE_VOL</action>
      <source>VID</source>
      <service-instance-id>test-service-instance-id-123</service-instance-id>
   </request-info>
   <volume-inputs>
      <volume-group-id/>
      <volume-group-name>MSOTESTVOL101a-vSAMP12_base_vol_module-0</volume-group-name>
      <vnf-type>Test/vSAMP12</vnf-type>
      <vf-module-model-name>vSAMP12::base::module-0</vf-module-model-name>
      <asdc-service-model-version>2.0</asdc-service-model-version>
      <aic-cloud-region>mdt1</aic-cloud-region>
      <tenant-id>88a6ca3ee0394ade9403f075db23167e</tenant-id>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <backout-on-failure>true</backout-on-failure>
      <model-customization-id/>
   </volume-inputs>
   <volume-params>
      <param name="vnf_name">STMTN5MMSC20</param>
      <param name="vnf_name2">US1117MTSNJVBR0246</param>
      <param name="vnf_nm_information"/>
      <param name="vnf_type">pcrf-capacity</param>
      <param name="vnf_id">skask</param>
      <param name="vnf_stack_id">slowburn</param>
      <param name="vnf_status">created</param>
      <param name="aic_cloud_region">MDTWNJ21</param>
      <param name="availability_zone">slcp3-esx-az01</param>
      <param name="oam_network_name">VLAN-OAM-1323</param>
      <param name="vm_name">slcp34246vbc246ceb</param>
      <param name="ipag_network_id">970cd2b9-7f09-4a12-af47-182ea38ba1f0</param>
      <param name="vpe_network_id">545cc2c3-1930-4100-b534-5d82d0e12bb6</param>
   </volume-params>
</volume-request>
"""
	
	String bpmnReq2 = """
{
	"requestDetails": {
		"modelInfo": {
			"modelType": "volumeGroup",
			"modelId": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
			"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
			"modelName": "vSAMP12::base::module-0",
			"modelVersion": "1"
		},
		"cloudConfiguration": {
			"lcpCloudRegionId": "mdt1",
			"tenantId": "88a6ca3ee0394ade9403f075db23167e"
		},
		"requestInfo": {
			"instanceName": "MSOTESTVOL101a-vSAMP12_base_vol_module-0",
			"source": "VID",
			"suppressRollback": false
		},
		"relatedInstanceList": [
			{
				"relatedInstance": {
					"instanceId": "{service-instance-id}",
					"modelInfo": {
						"modelType": "service",
						"modelId": "ff3514e3-5a33-55df-13ab-12abad84e7ff",
						"modelUuid": "fe6985cd-ea33-3346-ac12-ab121484a3fe",
						"modelName": "Test",
						"modelVersion": "2.0"
					}
				}
			}, {
				"relatedInstance": {
					"instanceId": "{vnf-instance-id}",
					"modelInfo": {
						"modelType": "vnf",
						"modelId": "ff5256d1-5a33-55df-13ab-12abad84e7ff",
						"modelUuid": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
						"modelName": "vSAMP12",
						"modelVersion": "1",
						"modelInstanceName": "vSAMP12"
					}
				}
			}
		]
	}
}
"""
	
	String bpmnReqJsonVolumeSuppressRollbackTrue = """
{
	"requestDetails": {
		"modelInfo": {
			"modelType": "volumeGroup",
			"modelId": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
			"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
			"modelName": "vSAMP12::base::module-0",
			"modelVersion": "1"
		},
		"cloudConfiguration": {
			"lcpCloudRegionId": "mdt1",
			"tenantId": "88a6ca3ee0394ade9403f075db23167e"
		},
		"requestInfo": {
			"instanceName": "MSOTESTVOL101a-vSAMP12_base_vol_module-0",
			"source": "VID",
			"suppressRollback": true
		},
		"relatedInstanceList": [
			{
				"relatedInstance": {
					"instanceId": "{service-instance-id}",
					"modelInfo": {
						"modelType": "service",
						"modelId": "ff3514e3-5a33-55df-13ab-12abad84e7ff",
						"modelUuid": "fe6985cd-ea33-3346-ac12-ab121484a3fe",
						"modelName": "Test",
						"modelVersion": "2.0"
					}
				}
			}, {
				"relatedInstance": {
					"instanceId": "{vnf-instance-id}",
					"modelInfo": {
						"modelType": "vnf",
						"modelId": "ff5256d1-5a33-55df-13ab-12abad84e7ff",
						"modelUuid": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
						"modelName": "vSAMP12",
						"modelVersion": "1",
						"modelInstanceName": "vSAMP12"
					}
				}
			}
		]
	}
}
"""

String bpmnReqJsonVolumeSuppressRollbackFalse = """
{
	"requestDetails": {
		"modelInfo": {
			"modelType": "volumeGroup",
			"modelId": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
			"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
			"modelName": "vSAMP12::base::module-0",
			"modelVersion": "1"
		},
		"cloudConfiguration": {
			"lcpCloudRegionId": "mdt1",
			"tenantId": "88a6ca3ee0394ade9403f075db23167e"
		},
		"requestInfo": {
			"instanceName": "MSOTESTVOL101a-vSAMP12_base_vol_module-0",
			"source": "VID",
			"suppressRollback": false
		},
		"relatedInstanceList": [
			{
				"relatedInstance": {
					"instanceId": "{service-instance-id}",
					"modelInfo": {
						"modelType": "service",
						"modelId": "ff3514e3-5a33-55df-13ab-12abad84e7ff",
						"modelUuid": "fe6985cd-ea33-3346-ac12-ab121484a3fe",
						"modelName": "Test",
						"modelVersion": "2.0"
					}
				}
			}, {
				"relatedInstance": {
					"instanceId": "{vnf-instance-id}",
					"modelInfo": {
						"modelType": "vnf",
						"modelId": "ff5256d1-5a33-55df-13ab-12abad84e7ff",
						"modelUuid": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
						"modelName": "vSAMP12",
						"modelVersion": "1",
						"modelInstanceName": "vSAMP12"
					}
				}
			}
		]
	}
}
"""

String bpmnReqJsonVolumeSuppressRollbackNone = """
{
	"requestDetails": {
		"modelInfo": {
			"modelType": "volumeGroup",
			"modelId": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
			"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
			"modelName": "vSAMP12::base::module-0",
			"modelVersion": "1"
		},
		"cloudConfiguration": {
			"lcpCloudRegionId": "mdt1",
			"tenantId": "88a6ca3ee0394ade9403f075db23167e"
		},
		"requestInfo": {
			"instanceName": "MSOTESTVOL101a-vSAMP12_base_vol_module-0",
			"source": "VID"
		},
		"relatedInstanceList": [
			{
				"relatedInstance": {
					"instanceId": "{service-instance-id}",
					"modelInfo": {
						"modelType": "service",
						"modelId": "ff3514e3-5a33-55df-13ab-12abad84e7ff",
						"modelUuid": "fe6985cd-ea33-3346-ac12-ab121484a3fe",
						"modelName": "Test",
						"modelVersion": "2.0"
					}
				}
			}, {
				"relatedInstance": {
					"instanceId": "{vnf-instance-id}",
					"modelInfo": {
						"modelType": "vnf",
						"modelId": "ff5256d1-5a33-55df-13ab-12abad84e7ff",
						"modelUuid": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
						"modelName": "vSAMP12",
						"modelVersion": "1",
						"modelInstanceName": "vSAMP12"
					}
				}
			}
		]
	}
}
"""

	String bpmnReqJsonVfModuleSuppressRollbackTrue = """
{
"requestDetails": {
"modelInfo": {
"modelType": "vfModule",
"modelInvariantUuid": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
"modelName": "STMTN5MMSC21-MMSC::model-1-0",
"modelVersion": "1"
},
"cloudConfiguration": {
"lcpCloudRegionId": "MDTWNJ21",
"tenantId": "fba1bd1e195a404cacb9ce17a9b2b421"
},
"requestInfo": {
"instanceName": "PCRF::module-0-2",
"source": "VID",
"suppressRollback": true
},
"relatedInstanceList": [
{
"relatedInstance": {
"instanceId": "17ef4658-bd1f-4ef0-9ca0-ea76e2bf122c",
"instanceName": "MSOTESTVOL103a-vSAMP12_base_module-0_vol",
"modelInfo": {
"modelType": "volumeGroup",
"modelInvariantUuid": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
"modelName": "vSAMP12..base..module-0",
"modelVersion": "1"
}
}
},
{
"relatedInstance": {
"instanceId": "123456",
"modelInfo": {
"modelType": "service",
"modelInvariantUuid": "ff3514e3-5a33-55df-13ab-12abad84e7ff",
"modelUuid": "fe6985cd-ea33-3346-ac12-ab121484a3fe",
"modelName": "SERVICE_MODEL_NAME",
"modelVersion": "1.0"
}
}
},
{
"relatedInstance": {
"instanceId": "skask",
"instanceName": "skask-test",
"modelInfo": {
"modelType": "vnf",
"modelInvariantUuid": "skask",
"modelUuid": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
"modelName": "vSAMP12",
"modelVersion": "1.0",
"modelInstanceName": "vSAMP12 1"
}
}
}
],
"requestParameters": {
"userParams": {}
}
}
}
"""

String bpmnReqJsonVfModuleSuppressRollbackFalse = """
{
"requestDetails": {
"modelInfo": {
"modelType": "vfModule",
"modelInvariantUuid": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
"modelName": "STMTN5MMSC21-MMSC::model-1-0",
"modelVersion": "1"
},
"cloudConfiguration": {
"lcpCloudRegionId": "MDTWNJ21",
"tenantId": "fba1bd1e195a404cacb9ce17a9b2b421"
},
"requestInfo": {
"instanceName": "PCRF::module-0-2",
"source": "VID",
"suppressRollback": false
},
"relatedInstanceList": [
{
"relatedInstance": {
"instanceId": "17ef4658-bd1f-4ef0-9ca0-ea76e2bf122c",
"instanceName": "MSOTESTVOL103a-vSAMP12_base_module-0_vol",
"modelInfo": {
"modelType": "volumeGroup",
"modelInvariantUuid": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
"modelName": "vSAMP12..base..module-0",
"modelVersion": "1"
}
}
},
{
"relatedInstance": {
"instanceId": "123456",
"modelInfo": {
"modelType": "service",
"modelInvariantUuid": "ff3514e3-5a33-55df-13ab-12abad84e7ff",
"modelUuid": "fe6985cd-ea33-3346-ac12-ab121484a3fe",
"modelName": "SERVICE_MODEL_NAME",
"modelVersion": "1.0"
}
}
},
{
"relatedInstance": {
"instanceId": "skask",
"instanceName": "skask-test",
"modelInfo": {
"modelType": "vnf",
"modelInvariantUuid": "skask",
"modelUuid": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
"modelName": "vSAMP12",
"modelVersion": "1.0",
"modelInstanceName": "vSAMP12 1"
}
}
}
],
"requestParameters": {
"userParams": {}
}
}
}
"""

String bpmnReqJsonVfModuleSuppressRollbackNone = """
{
"requestDetails": {
"modelInfo": {
"modelType": "vfModule",
"modelInvariantUuid": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
"modelName": "STMTN5MMSC21-MMSC::model-1-0",
"modelVersion": "1"
},
"cloudConfiguration": {
"lcpCloudRegionId": "MDTWNJ21",
"tenantId": "fba1bd1e195a404cacb9ce17a9b2b421"
},
"requestInfo": {
"instanceName": "PCRF::module-0-2",
"source": "VID"
},
"relatedInstanceList": [
{
"relatedInstance": {
"instanceId": "17ef4658-bd1f-4ef0-9ca0-ea76e2bf122c",
"instanceName": "MSOTESTVOL103a-vSAMP12_base_module-0_vol",
"modelInfo": {
"modelType": "volumeGroup",
"modelInvariantUuid": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
"modelUuid": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
"modelName": "vSAMP12..base..module-0",
"modelVersion": "1"
}
}
},
{
"relatedInstance": {
"instanceId": "123456",
"modelInfo": {
"modelType": "service",
"modelInvariantUuid": "ff3514e3-5a33-55df-13ab-12abad84e7ff",
"modelUuid": "fe6985cd-ea33-3346-ac12-ab121484a3fe",
"modelName": "SERVICE_MODEL_NAME",
"modelVersion": "1.0"
}
}
},
{
"relatedInstance": {
"instanceId": "skask",
"instanceName": "skask-test",
"modelInfo": {
"modelType": "vnf",
"modelInvariantUuid": "skask",
"modelUuid": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
"modelName": "vSAMP12",
"modelVersion": "1.0",
"modelInstanceName": "vSAMP12 1"
}
}
}
],
"requestParameters": {
"userParams": {}
}
}
}
"""
	String vidUtilResp2 = """<volume-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <action>CREATE_VF_MODULE_VOL</action>
      <source>VID</source>
      <service-instance-id>test-service-instance-id-123</service-instance-id>
   </request-info>
   <volume-inputs>
      <volume-group-id/>
      <volume-group-name>MSOTESTVOL101a-vSAMP12_base_vol_module-0</volume-group-name>
      <vnf-type>Test/vSAMP12</vnf-type>
      <vf-module-model-name>vSAMP12::base::module-0</vf-module-model-name>
      <asdc-service-model-version>2.0</asdc-service-model-version>
      <aic-cloud-region>mdt1</aic-cloud-region>
      <tenant-id>88a6ca3ee0394ade9403f075db23167e</tenant-id>
      <service-id/>
      <backout-on-failure>true</backout-on-failure>
      <model-customization-id/>
   </volume-inputs>
   <volume-params/>
</volume-request>
"""
	
String vidUtilVolumeRespBackoutOnFailureFalse = """<volume-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <action>CREATE_VF_MODULE_VOL</action>
      <source>VID</source>
      <service-instance-id>test-service-instance-id-123</service-instance-id>
   </request-info>
   <volume-inputs>
      <volume-group-id/>
      <volume-group-name>MSOTESTVOL101a-vSAMP12_base_vol_module-0</volume-group-name>
      <vnf-type>Test/vSAMP12</vnf-type>
      <vf-module-model-name>vSAMP12::base::module-0</vf-module-model-name>
      <asdc-service-model-version>2.0</asdc-service-model-version>
      <aic-cloud-region>mdt1</aic-cloud-region>
      <tenant-id>88a6ca3ee0394ade9403f075db23167e</tenant-id>
      <service-id/>
      <backout-on-failure>false</backout-on-failure>
      <model-customization-id/>
   </volume-inputs>
   <volume-params/>
</volume-request>
"""

String vidUtilVolumeRespBackoutOnFailureTrue = """<volume-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <action>CREATE_VF_MODULE_VOL</action>
      <source>VID</source>
      <service-instance-id>test-service-instance-id-123</service-instance-id>
   </request-info>
   <volume-inputs>
      <volume-group-id/>
      <volume-group-name>MSOTESTVOL101a-vSAMP12_base_vol_module-0</volume-group-name>
      <vnf-type>Test/vSAMP12</vnf-type>
      <vf-module-model-name>vSAMP12::base::module-0</vf-module-model-name>
      <asdc-service-model-version>2.0</asdc-service-model-version>
      <aic-cloud-region>mdt1</aic-cloud-region>
      <tenant-id>88a6ca3ee0394ade9403f075db23167e</tenant-id>
      <service-id/>
      <backout-on-failure>true</backout-on-failure>
      <model-customization-id/>
   </volume-inputs>
   <volume-params/>
</volume-request>
"""

String vidUtilVolumeRespBackoutOnFailureEmpty = """<volume-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <action>CREATE_VF_MODULE_VOL</action>
      <source>VID</source>
      <service-instance-id>test-service-instance-id-123</service-instance-id>
   </request-info>
   <volume-inputs>
      <volume-group-id/>
      <volume-group-name>MSOTESTVOL101a-vSAMP12_base_vol_module-0</volume-group-name>
      <vnf-type>Test/vSAMP12</vnf-type>
      <vf-module-model-name>vSAMP12::base::module-0</vf-module-model-name>
      <asdc-service-model-version>2.0</asdc-service-model-version>
      <aic-cloud-region>mdt1</aic-cloud-region>
      <tenant-id>88a6ca3ee0394ade9403f075db23167e</tenant-id>
      <service-id/>
      <backout-on-failure/>
      <model-customization-id/>
   </volume-inputs>
   <volume-params/>
</volume-request>
"""

String vidUtilVfModuleRespBackoutOnFailureFalse = """<vnf-request>
   <request-info>
      <request-id>test-request-id-123</request-id>
      <action>CREATE_VF_MODULE</action>
      <source>VID</source>
      <service-instance-id>test-service-instance-id-123</service-instance-id>
   </request-info>
   <vnf-inputs><!-- not in use in 1610 -->
      <vnf-name>skask-test</vnf-name>
      <vnf-type>test-vnf-type-123</vnf-type>
      <vnf-id>test-vnf-id-123</vnf-id>
      <volume-group-id>test-volume-group-id-123</volume-group-id>
      <vf-module-id>test-vf-module-id-123</vf-module-id>
      <vf-module-name>PCRF::module-0-2</vf-module-name>
      <vf-module-model-name>STMTN5MMSC21-MMSC::model-1-0</vf-module-model-name>
      <model-customization-id/>
      <is-base-vf-module>false</is-base-vf-module>
      <asdc-service-model-version>1.0</asdc-service-model-version>
      <aic-cloud-region>MDTWNJ21</aic-cloud-region>
      <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>
      <service-id/>
      <backout-on-failure>false</backout-on-failure>
      <persona-model-id>ff5256d2-5a33-55df-13ab-12abad84e7ff</persona-model-id>
      <persona-model-version>fe6478e5-ea33-3346-ac12-ab121484a3fe</persona-model-version>
   </vnf-inputs>
   <vnf-params/>
</vnf-request>
"""

String vidUtilVfModuleRespBackoutOnFailureTrue = """<vnf-request>
   <request-info>
      <request-id>test-request-id-123</request-id>
      <action>CREATE_VF_MODULE</action>
      <source>VID</source>
      <service-instance-id>test-service-instance-id-123</service-instance-id>
   </request-info>
   <vnf-inputs><!-- not in use in 1610 -->
      <vnf-name>skask-test</vnf-name>
      <vnf-type>test-vnf-type-123</vnf-type>
      <vnf-id>test-vnf-id-123</vnf-id>
      <volume-group-id>test-volume-group-id-123</volume-group-id>
      <vf-module-id>test-vf-module-id-123</vf-module-id>
      <vf-module-name>PCRF::module-0-2</vf-module-name>
      <vf-module-model-name>STMTN5MMSC21-MMSC::model-1-0</vf-module-model-name>
      <model-customization-id/>
      <is-base-vf-module>false</is-base-vf-module>
      <asdc-service-model-version>1.0</asdc-service-model-version>
      <aic-cloud-region>MDTWNJ21</aic-cloud-region>
      <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>
      <service-id/>
      <backout-on-failure>true</backout-on-failure>
      <persona-model-id>ff5256d2-5a33-55df-13ab-12abad84e7ff</persona-model-id>
      <persona-model-version>fe6478e5-ea33-3346-ac12-ab121484a3fe</persona-model-version>
   </vnf-inputs>
   <vnf-params/>
</vnf-request>
"""

String vidUtilVfModuleRespBackoutOnFailureEmpty = """<vnf-request>
   <request-info>
      <request-id>test-request-id-123</request-id>
      <action>CREATE_VF_MODULE</action>
      <source>VID</source>
      <service-instance-id>test-service-instance-id-123</service-instance-id>
   </request-info>
   <vnf-inputs><!-- not in use in 1610 -->
      <vnf-name>skask-test</vnf-name>
      <vnf-type>test-vnf-type-123</vnf-type>
      <vnf-id>test-vnf-id-123</vnf-id>
      <volume-group-id>test-volume-group-id-123</volume-group-id>
      <vf-module-id>test-vf-module-id-123</vf-module-id>
      <vf-module-name>PCRF::module-0-2</vf-module-name>
      <vf-module-model-name>STMTN5MMSC21-MMSC::model-1-0</vf-module-model-name>
      <model-customization-id/>
      <is-base-vf-module>false</is-base-vf-module>
      <asdc-service-model-version>1.0</asdc-service-model-version>
      <aic-cloud-region>MDTWNJ21</aic-cloud-region>
      <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>
      <service-id/>
      <backout-on-failure/>
      <persona-model-id>ff5256d2-5a33-55df-13ab-12abad84e7ff</persona-model-id>
      <persona-model-version>fe6478e5-ea33-3346-ac12-ab121484a3fe</persona-model-version>
   </vnf-inputs>
   <vnf-params/>
</vnf-request>
"""

	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
		
	}
	
	@Test
	public void test() {

		def jsonSlurper = new JsonSlurper()
		Map reqMap = jsonSlurper.parseText(bpmnReq1)
		
		VidUtils vidUtils = new VidUtils()
		def xmlReq = vidUtils.createXmlVolumeRequest(reqMap, 'CREATE_VF_MODULE_VOL', 'test-service-instance-id-123')
		print xmlReq
		assertEquals(vidUtilResp1, xmlReq)
	}
	
	//@Test
	public void testVfModule() {

		def jsonSlurper = new JsonSlurper()
		Map reqMap = jsonSlurper.parseText(vfModuleReqJson)
		
		VidUtils vidUtils = new VidUtils()
		def xmlReq = vidUtils.createXmlVfModuleRequest(null, reqMap, "CREATE_VF_MODULE", "test-service-instance-id-123")
		print xmlReq
		assertTrue(true)
	}
	
	@Test
	public void testNoRequestParams() {

		def jsonSlurper = new JsonSlurper()
		Map reqMap = jsonSlurper.parseText(bpmnReq2)
		
		VidUtils vidUtils = new VidUtils()
		def xmlReq = vidUtils.createXmlVolumeRequest(reqMap, 'CREATE_VF_MODULE_VOL', 'test-service-instance-id-123')
		
		assertEquals(vidUtilResp2, xmlReq)
	}

	@Test
	public void testVfModuleVolumeRollbackTrue() {

		def jsonSlurper = new JsonSlurper()
		Map reqMap = jsonSlurper.parseText(bpmnReqJsonVolumeSuppressRollbackTrue)
		
		VidUtils vidUtils = new VidUtils()
		def xmlReq = vidUtils.createXmlVolumeRequest(reqMap, 'CREATE_VF_MODULE_VOL', 'test-service-instance-id-123')
		
		assertEquals(vidUtilVolumeRespBackoutOnFailureFalse, xmlReq)
	}

	@Test
	public void testVfModuleVolumeRollbackFalse() {

		def jsonSlurper = new JsonSlurper()
		Map reqMap = jsonSlurper.parseText(bpmnReqJsonVolumeSuppressRollbackFalse)
		
		VidUtils vidUtils = new VidUtils()
		def xmlReq = vidUtils.createXmlVolumeRequest(reqMap, 'CREATE_VF_MODULE_VOL', 'test-service-instance-id-123')
		
		assertEquals(vidUtilVolumeRespBackoutOnFailureTrue, xmlReq)
	}

	@Test
	public void testVfModuleVolumeRollbackNone() {

		def jsonSlurper = new JsonSlurper()
		Map reqMap = jsonSlurper.parseText(bpmnReqJsonVolumeSuppressRollbackNone)
		
		VidUtils vidUtils = new VidUtils()
		def xmlReq = vidUtils.createXmlVolumeRequest(reqMap, 'CREATE_VF_MODULE_VOL', 'test-service-instance-id-123')

		assertEquals(vidUtilVolumeRespBackoutOnFailureEmpty, xmlReq)
	}
	
	@Test
	public void testVfModuleRollbackTrue() {

		def jsonSlurper = new JsonSlurper()
		Map reqMap = jsonSlurper.parseText(bpmnReqJsonVfModuleSuppressRollbackTrue)
		
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("vnfType")).thenReturn('test-vnf-type-123')
		when(mockExecution.getVariable("vnfId")).thenReturn('test-vnf-id-123')
		when(mockExecution.getVariable("vfModuleId")).thenReturn('test-vf-module-id-123')
		when(mockExecution.getVariable("volumeGroupId")).thenReturn('test-volume-group-id-123')
		when(mockExecution.getVariable("isBaseVfModule")).thenReturn('false')
		when(mockExecution.getVariable("mso-request-id")).thenReturn('test-request-id-123')
		
		VidUtils vidUtils = new VidUtils()
		def xmlReq = vidUtils.createXmlVfModuleRequest(mockExecution, reqMap, 'CREATE_VF_MODULE', 'test-service-instance-id-123')
		
		println 'ggg->' + xmlReq
		assertEquals(vidUtilVfModuleRespBackoutOnFailureFalse, xmlReq)
	}
	
	@Test
	public void testVfModuleRollbackFalse() {

		def jsonSlurper = new JsonSlurper()
		Map reqMap = jsonSlurper.parseText(bpmnReqJsonVfModuleSuppressRollbackFalse)
		
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("vnfType")).thenReturn('test-vnf-type-123')
		when(mockExecution.getVariable("vnfId")).thenReturn('test-vnf-id-123')
		when(mockExecution.getVariable("vfModuleId")).thenReturn('test-vf-module-id-123')
		when(mockExecution.getVariable("volumeGroupId")).thenReturn('test-volume-group-id-123')
		when(mockExecution.getVariable("isBaseVfModule")).thenReturn('false')
		when(mockExecution.getVariable("mso-request-id")).thenReturn('test-request-id-123')
		
		VidUtils vidUtils = new VidUtils()
		def xmlReq = vidUtils.createXmlVfModuleRequest(mockExecution, reqMap, 'CREATE_VF_MODULE', 'test-service-instance-id-123')
		
		println 'ggg->' + xmlReq
		assertEquals(vidUtilVfModuleRespBackoutOnFailureTrue, xmlReq)
	}
	
	@Test
	public void testVfModuleRollbackNone() {

		def jsonSlurper = new JsonSlurper()
		Map reqMap = jsonSlurper.parseText(bpmnReqJsonVfModuleSuppressRollbackNone)
		
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("vnfType")).thenReturn('test-vnf-type-123')
		when(mockExecution.getVariable("vnfId")).thenReturn('test-vnf-id-123')
		when(mockExecution.getVariable("vfModuleId")).thenReturn('test-vf-module-id-123')
		when(mockExecution.getVariable("volumeGroupId")).thenReturn('test-volume-group-id-123')
		when(mockExecution.getVariable("isBaseVfModule")).thenReturn('false')
		when(mockExecution.getVariable("mso-request-id")).thenReturn('test-request-id-123')
		
		VidUtils vidUtils = new VidUtils()
		def xmlReq = vidUtils.createXmlVfModuleRequest(mockExecution, reqMap, 'CREATE_VF_MODULE', 'test-service-instance-id-123')
		
		println 'ggg->' + xmlReq
		assertEquals(vidUtilVfModuleRespBackoutOnFailureEmpty, xmlReq)
	}
}

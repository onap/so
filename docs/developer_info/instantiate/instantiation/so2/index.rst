.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright 2019 ONAP Contributors.  All rights reserved.

.. _doc_guide_user_ser_inst_so2:


Macro mode Service Instantiation via ONAP SO API
================================================

Using Macro mode, you have to build and send only one request to ONAP SO

In that request you need to indicate all object instances
that you want to be instantiated.

Reminder : ONAP SO in Macro mode will perform the VNF parameters/values
assignment based on CDS Blueprint templates
that are supposed to be defined during Design and Onboard steps.
That means ONAP has all information
to be able to get all necessary values by itself (there is no longer need
for an Operator to provide "SDNC preload").

Additional info in:

.. toctree::
   :maxdepth: 1
   :titlesonly:

   CDS Documentation <../../../../../submodules/ccsdk/cds.git/docs/index.rst>
   CDS vDNS E2E Automation <https://wiki.onap.org/display/DW/vDNS+CDS+Dublin>


Request Example :

::

  curl -X POST \
    'http://{{k8s}}:30277/onap/so/infra/serviceInstantiation/v7/serviceInstances' \
    -H 'Content-Type: application/json' \
    -H 'cache-control: no-cache' \
    -d '{
    "requestDetails": {
      "subscriberInfo": {
        "globalSubscriberId": "Demonstration"
      },
      "requestInfo": {
        "suppressRollback": false,
        "productFamilyId": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb",
        "requestorId": "adt",
        "instanceName": "{{cds-instance-name}}",
        "source": "VID"
      },
      "cloudConfiguration": {
        "lcpCloudRegionId": "fr1",
        "tenantId": "6270eaa820934710960682c506115453"
      },
      "requestParameters": {
        "subscriptionServiceType": "vFW",
        "userParams": [
          {
            "Homing_Solution": "none"
          },
          {
            "service": {
              "instanceParams": [

              ],
              "instanceName": "{{cds-instance-name}}",
              "resources": {
                "vnfs": [
                  {
                    "modelInfo": {
                  "modelName": "{{vnf-modelinfo-modelname}}",
                  "modelVersionId": "{{vnf-modelinfo-modeluuid}}",
                  "modelInvariantUuid": "{{vnf-modelinfo-modelinvariantuuid}}",
                  "modelVersion": "1.0",
                  "modelCustomizationId": "{{vnf-modelinfo-modelcustomizationuuid}}",
                  "modelInstanceName": "{{vnf-modelinfo-modelinstancename}}"
                    },
                    "cloudConfiguration": {
                      "lcpCloudRegionId": "fr1",
                      "tenantId": "6270eaa820934710960682c506115453"
                    },
                    "platform": {
                      "platformName": "test"
                    },
                    "lineOfBusiness": {
                      "lineOfBusinessName": "someValue"
                    },
                    "productFamilyId": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb",
                    "instanceName": "{{vnf-modelinfo-modelinstancename}}",
                    "instanceParams": [
                      {
                        "onap_private_net_id": "olc-private",
                        "onap_private_subnet_id": "olc-private",
                        "pub_key": "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCwj7uJMyKiP1ogEsZv5kKDFw9mFNhxI+woR3Tuv8vjfNnqdB1GfSnvTFyNbdpyNdR8BlljkiZ1SlwJLEkvPk0HpOoSVVek/QmBeGC7mxyRcpMB2cNQwjXGfsVrforddXOnOkj+zx1aNdVGMc52Js3pex8B/L00H68kOcwP26BI1o77Uh+AxjOkIEGs+wlWNUmXabLDCH8l8IJk9mCTruKEN9KNj4NRZcaNC+XOz42SyHV9RT3N6efp31FqKzo8Ko63QirvKEEBSOAf9VlJ7mFMrGIGH37AP3JJfFYEHDdOA3N64ZpJLa39y25EWwGZNlWpO/GW5bNjTME04dl4eRyd",
                        "image_name": "Ubuntu 14.04",
                        "flavor_name":"s1.cw.small-1"
                      }
                    ],
                    "vfModules": [
                      {
                        "modelInfo": {
                          "modelName": "{{vnf-vfmodule-0-modelinfo-modelname}}",
                          "modelVersionId": "{{vnf-vfmodule-0-modelinfo-modeluuid}}",
                         "modelInvariantUuid": "{{vnf-vfmodule-0-modelinfo-modelinvariantuuid}}",
                          "modelVersion": "1",
                          "modelCustomizationId": "{{vnf-vfmodule-0-modelinfo-modelcustomizationuuid}}"
                         },
                        "instanceName": "{{vnf-vfmodule-0-modelinfo-modelname}}",
                        "instanceParams": [
                                                   {
                            "sec_group": "olc-open",
                            "public_net_id": "olc-net"
                          }
                        ]
                      },
                      {
                        "modelInfo": {
                          "modelName": "{{vnf-vfmodule-1-modelinfo-modelname}}",
                          "modelVersionId": "{{vnf-vfmodule-1-modelinfo-modeluuid}}",
                          "modelInvariantUuid": "{{vnf-vfmodule-1-modelinfo-modelinvariantuuid}}",
                          "modelVersion": "1",
                          "modelCustomizationId": "{{vnf-vfmodule-1-modelinfo-modelcustomizationuuid}}"
                         },
                        "instanceName": "{{vnf-vfmodule-1-modelinfo-modelname}}",
                        "instanceParams": [
                          {
                            "sec_group": "olc-open",
                            "public_net_id": "olc-net"
                          }
                        ]
                      },
                      {
                        "modelInfo": {
                          "modelName": "{{vnf-vfmodule-2-modelinfo-modelname}}",
                          "modelVersionId": "{{vnf-vfmodule-2-modelinfo-modeluuid}}",
                          "modelInvariantUuid": "{{vnf-vfmodule-2-modelinfo-modelinvariantuuid}}",
                          "modelVersion": "1",
                          "modelCustomizationId": "{{vnf-vfmodule-2-modelinfo-modelcustomizationuuid}}"
                         },
                        "instanceName": "{{vnf-vfmodule-2-modelinfo-modelname}}",
                        "instanceParams": [
                          {
                            "sec_group": "olc-open",
                            "public_net_id": "olc-net"
                          }
                        ]
                      },
                      {
                        "modelInfo": {
                          "modelName": "{{vnf-vfmodule-3-modelinfo-modelname}}",
                          "modelVersionId": "{{vnf-vfmodule-3-modelinfo-modeluuid}}",
                          "modelInvariantUuid": "{{vnf-vfmodule-3-modelinfo-modelinvariantuuid}}",
                          "modelVersion": "1",
                          "modelCustomizationId": "{{vnf-vfmodule-3-modelinfo-modelcustomizationuuid}}"
                        },
                        "instanceName": "{{vnf-vfmodule-3-modelinfo-modelname}}",
                        "instanceParams": [
                          {
                            "sec_group": "olc-open",
                            "public_net_id": "olc-net"
                          }
                        ]
                      }
                    ]
                  }
                ]
              },
              "modelInfo": {
                "modelVersion": "1.0",
          "modelVersionId": "{{service-uuid}}",
          "modelInvariantId": "{{service-invariantUUID}}",
          "modelName": "{{service-name}}",
                "modelType": "service"
              }
            }
          }
        ],
        "aLaCarte": false
      },
      "project": {
        "projectName": "Project-Demonstration"
      },
      "owningEntity": {
        "owningEntityId": "24ef5425-bec4-4fa3-ab03-c0ecf4eaac96",
        "owningEntityName": "OE-Demonstration"
      },
      "modelInfo": {
        "modelVersion": "1.0",
          "modelVersionId": "{{service-uuid}}",
          "modelInvariantId": "{{service-invariantUUID}}",
          "modelName": "{{service-name}}",
       "modelType": "service"
      }
    }
  }'

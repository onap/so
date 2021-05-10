.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2021 Ericsson Software Technologies

SO ETSI VNF LCM Setup & Functionality
=================================================

Introduction
------------

This guide will describe the steps required to execute the ETSI “Instantiate VNF” and “Terminate VNF” workflows using ONAP. The initial requirements you need for this guide are as follows:

- A stable ONAP Deployment
- ESR component enabled in ONAP installation.
- SO-ETSI-SOL003-Adapter component enabled ONAP in installation.
- A VNFM that is aligned to the Sol003 Interface (https://www.etsi.org/deliver/etsi_gs/NFV-SOL/001_099/003/02.03.01_60/gs_NFV-SOL003v020301p.pdf)
- A Sol004 Aligned VNF package (https://www.etsi.org/deliver/etsi_gs/NFV-SOL/001_099/004/02.03.01_60/gs_nfv-sol004v020301p.pdf)


Initial Configurations
----------------------

The following configurations need to be completed in order to execute the ETSI workflows and instantiate through the SO-ETSI-SOL003-Adapter.

**Log into the MariaDB Database**

Find your Mariadb pod:

.. code-block:: bash

    kubectl -n onap get pods | grep maria

Exec into a Mariadb-Galera pod (replace <PODNAME> with the Mariadb pod name):

.. code-block:: bash

    kubectl -n onap -it exec <PODNAME> bash

Log into the SQL database and connect to the "catalogdb" database. Follow the next steps in order to configure your ONAP's Mariadb database.

**Enable the ETSI “Instantiate/Create” & “Terminate/Delete” building blocks.**

Firstly, you will need to add the ETSI Create & Delete building blocks, this is done by inserting them into the “building_block_detail” table in the Mariadb’s “catalogdb” database.

**Insert the ETSI Create Building Block:**

.. code-block:: bash

    insert into building_block_detail(building_block_name, resource_type, target_action) values (“EtsiVnfInstantiateBB”, “VNF”, “ACTIVATE”);

**Insert ETSI Delete Building Block:**

.. code-block:: bash

    insert into building_block_detail(building_block_name, resource_type, target_action) values (“EtsiVnfDeleteBB”, “VNF”, “DEACTIVATE”);

**View the “building_block_detail” table:**

.. code-block:: bash

    select * from building_block_detail;

You should now have the entries in your “building_block_detail” table.



Update the orchestration_flow_reference table
---------------------------------------------

Note: Standard VNF instantiation is unlikely to work once this step has been completed.

The next step is to set which building blocks are triggered on a VNF instantiate request. We will also be setting the correct sequence for these building blocks.

**View the VNF Create/Delete sequences from the “orchestration_flow_reference” table:**

.. code-block:: bash

    select * from orchestration_flow_reference where COMPOSITE_ACTION="VNF-Create";
    select * from orchestration_flow_reference where COMPOSITE_ACTION="VNF-Delete";

**Remove/Update current entries for “VNF-Create” & “VNF-Delete”:**

Retrieve “ID” from “northbound_request_ref_lookup” table. Take note of the “ID” value for “VNF-Create” and “VNF-Delete”:

.. code-block:: bash

    select * from northbound_request_ref_lookup where REQUEST_SCOPE='Vnf' and IS_ALACARTE is true;

Remove current VNF-Insert and insert ETSI VNF-Create, replace <ID> with the corresponding value retrieved from the “northbound_request_ref_lookup” table:

.. code-block:: bash

    delete from orchestration_flow_reference where COMPOSITE_ACTION = "VNF-Create";
    insert into orchestration_flow_reference (COMPOSITE_ACTION,SEQ_NO,FLOW_NAME,FLOW_VERSION,NB_REQ_REF_LOOKUP_ID ) values ("VNF-Create",1,"AssignVnfBB",1,<ID>);
    insert into orchestration_flow_reference (COMPOSITE_ACTION,SEQ_NO,FLOW_NAME,FLOW_VERSION,NB_REQ_REF_LOOKUP_ID ) values ("VNF-Create",2,"EtsiVnfInstantiateBB",1,<ID>);
    insert into orchestration_flow_reference (COMPOSITE_ACTION,SEQ_NO,FLOW_NAME,FLOW_VERSION,NB_REQ_REF_LOOKUP_ID ) values ("VNF-Create",3,"ActivateVnfBB",1,<ID>);

Remove current VNF-Delete and insert ETSI VNF-Delete, replace <ID> with the corresponding value retrieved from the “northbound_request_ref_lookup” table:

.. code-block:: bash

    delete from orchestration_flow_reference where COMPOSITE_ACTION = "VNF-Delete";
    insert into orchestration_flow_reference (COMPOSITE_ACTION,SEQ_NO,FLOW_NAME,FLOW_VERSION,NB_REQ_REF_LOOKUP_ID ) values ("VNF-Delete",1,"EtsiVnfDeleteBB",1,<ID>);
    insert into orchestration_flow_reference (COMPOSITE_ACTION,SEQ_NO,FLOW_NAME,FLOW_VERSION,NB_REQ_REF_LOOKUP_ID ) values ("VNF-Delete",2,"UnassignVnfBB",1,<ID>);


You have now enabled the ETSI building blocks and configured the sequence of building blocks to execute.

**Update the “orchestration_status_state_transition_directive” table**

The last step that needs to take in the MariaDB, is to update the state transition table, in order to allow our ETSI Create building blocks to correctly change the operation status of a VNF. If the operation status is not allowed to change correctly, then our ETSI building block will be skipped and will not be executed.

View the current “orchestration_status_state_transition_directive” setup.

.. code-block:: bash

    select * from orchestration_status_state_transition_directive where RESOURCE_TYPE='VNF' and ORCHESTRATION_STATUS='Created';

Update the row that decides when a “VNF” with an orchestration status of “CREATED” has a target action of “ACTIVATE” to “CONTINUE” instead of “FAIL” using the following command:

.. code-block:: bash

    update orchestration_status_state_transition_directive set FLOW_DIRECTIVE='CONTINUE' where RESOURCE_TYPE='VNF' and ORCHESTRATION_STATUS='CREATED' and TARGET_ACTION='ACTIVATE' and FLOW_DIRECTIVE='FAIL';

The transition directive is now set up correctly, allowing all of your ETSI building blocks to be executed correctly.


Adding your VNFM to ONAP ESR
----------------------------

Now you will need to send a curl command to A&AI, in order to add the VNFM to ESR/A&AI.

Please ensure you have ESR added to your ONAP installation before attempting this step. Next, you will need to populate the ESR VNFM List with information relating to the VNFM that you want to instantiate your VNFs through.

**Adding your VNFM to ONAP ESR using CURL:**

In order to use the curl command method, you will need to log into an ONAP pod, that is within your ONAP network. (This prevents us needing to go and get the AAI service IP and external port.)

You can log into one of your pods with the following command (this example will use the BPMN-INFRA pod):

.. code-block:: bash

    kubectl -n onap get pods | grep bpmn

Then take the full pod name and put it into this command instead of <PODNAME>:

.. code-block:: bash

    kubectl -n onap exec -it <PODNAME> sh

Once Exec'ed into the pod you can run the following command which creates a VNFM, in ESR, with ID “ExampleVnfm”. (Edit this curl command to your needs before using it)

.. code-block:: bash

    curl -X PUT -H 'Accept: application/json' -H 'Authorization: Basic YWFpQGFhaS5vbmFwLm9yZzpkZW1vMTIzNDU2IQ==' -H 'Content-Type: application/json' -H 'X-FromAppId:12' -H 'X-TransactionId: 12' https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/ExampleVnfm -d '{"vnfmId": "ExampleVnfm", "name": "ExampleVnfmName", "type": "ExampleVnfmType", "vendor": "est"}'

One important thing to note in this curl command is the type: "ExampleVnfmType". This will be used in a later step for specifying which VNFM you want to instantiate through, take note of this.

Once you have entered the previous information you need to add the “service-url” to your “esr-system-info” section of this VNFM you just added. Please note, that the “service-url” in the following curl command was designed to work with the “so-vnfm-simulator”, you will need to change this to match your specific VNFM’s “service-url”.

You will need to put this data into the "external-system" and "cloud-infrastructure" API paths listed below. This is done with the following curl commands:

**AAI Cloud-Infrastructure**

.. code-block:: bash

    curl -X PUT -H 'Accept: application/json' -H 'Authorization: Basic YWFpQGFhaS5vbmFwLm9yZzpkZW1vMTIzNDU2IQ==' -H 'Content-Type: application/json' -H 'X-FromAppId:12' -H 'X-TransactionId: 12' https://aai.onap:8443/aai/v15/cloud-infrastructure/cloud-regions/cloud-region/<CLOUD_OWNER>/<CLOUD_REGION_ID>/esr-system-info-list/esr-system-info/ExampleVnfm -d '{"name": "ExampleVnfm", "system-type": "ExampleVnfmType", "vimId": "myCloud", "vendor": "EST", "version": "V1.0", "certificateUrl": "", "url": "http://so-vnfm-simulator.onap:9095/vnflcm/v1/", "user-name": "testUser", "password": ""}'

Please note you will need to replace <CLOUD_OWNER> and <CLOUD_REGION_ID> with their respective values in your ONAP deployment.

**AAI External-System**

.. code-block:: bash

    curl -X PUT -H 'Accept: application/json' -H 'Authorization: Basic YWFpQGFhaS5vbmFwLm9yZzpkZW1vMTIzNDU2IQ==' -H 'Content-Type: application/json' -H 'X-FromAppId:12' -H 'X-TransactionId: 12' https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/ExampleVnfm/esr-system-info-list/esr-system-info/ExampleEsrSystemInfo -d '{"esr-system-info-id": "ExampleEsrSystemInfo", "type": "ExampleVnfmType", "user-name": "user", "password": "password", "system-type": "VNFM", "service-url": "http://so-vnfm-simulator.onap:9095/vnflcm/v1"}'

You have now entered your VNFM into the ESR/AAI components.

Here are the equivalent GET commands for checking what is currently in your ESR/AAI list (change the IDs to match the IDs you used earlier):

.. code-block:: bash

    curl -H 'Accept: application/json' -H 'Authorization: Basic YWFpQGFhaS5vbmFwLm9yZzpkZW1vMTIzNDU2IQ==' -H 'Content-Type: application/json' -H 'X-FromAppId:12' -H 'X-TransactionId: 12' https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/

.. code-block:: bash

    curl -H 'Accept: application/json' -H 'Authorization: Basic YWFpQGFhaS5vbmFwLm9yZzpkZW1vMTIzNDU2IQ==' -H 'Content-Type: application/json' -H 'X-FromAppId:12' -H 'X-TransactionId: 12' https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/ExampleVnfmId/esr-system-info-list/esr-system-info


Upload VNF Image to VNFM
------------------------

Currently, there is no implementation of the package management interface in the SO-ETSI-SOL003-Adapter, this means that the VNF image needs to be onboarded to your VNFM before instantiation. The VNF image will then be selected by using the VNF descriptor, found in one of the artifacts within the SDC onboarding package, mentioned later in this guide ("descriptor.yaml").

This is an important step, which unfortunately can be drastically different depending on the specific vendor's VNFM.


Onboarding a Virtual Software Product (VSP) with an ETSI HEAT Template.
-----------------------------------------------------------------------

Onboard a VNF package along with a Network Service CSAR into SDC.
A video demonstration of this process "Onboarding ETSI Based NS" can be found under SO NFVO NS Onboarding and LCM Demonstrations
It is recommended that you view this video before/alongside this section of the guide.

**Step 1:**

Login to the ONAP Portal. (Link: https://portal.api.simpledemo.onap.org:30225/ONAPPORTAL/login.htm (where "portal.api.simpledemo.onap.org" is set as the IP of your VM/host of your Portal component.)

Login: cs0008
Password: demo123456!

**Step 2:**

Select the SDC App. It is possible that your browser will block the scripts run by the Portal, you will need to enable them if it does.

**Step 3:**

Onboarding your VNF:
1: Click the “ONBOARD” tab near the top of the window.
2: Create a “VLM” if you have not created a "VLM" before.

- From the “ONBOARD” page, click create VLM.
- Enter name and description, then select create.
- Click on the plus button beside Entitlement Pool.
- Add Name and Manufacturing Reference Number, then click save.
- Click on the plus button beside License Key Groups.
- Add Name and select type, then click save.
- Click on the plus button beside Feature Groups.
- In the general tab, add name, Description and Part Number. Move to the Entitlements Pools tab, select the entitlement pool you just created and click the rightward arrow. Move to the License Key Group tab, select the license key group you just created and click the rightward arrow. Once these three things are done, click save.
- Click on the plus button beside License Agreement.
- Add Name and select License Term. Next move to the Feature Groups tab, select the Feature Group you just created and click the rightward arrow. Lastly click save.
- On the overview page, select submit in the top right corner of the screen.
- Enter a commit comment and click Commit & Submit.

3: Create a Virtual Service Product (VSP)

- Click the “ONBOARD” tab near the top of the window.
- Click “CREATE NEW VSP” and fill in the required information. Make sure to select “Network Package” for the “ONBOARDING PROCEDURE” section. Then click “CREATE”.
- Click where it shows “! Missing” underneath “License Agreement”. Simply select a “Licensing Version” and “License Agreement/Feature Group” from the drop downs.
- Click the “Overview” tab on the left hand side. Then press “Select File” in the “SOFTWARE PRODUCT ATTACHMENTS” section and select your prepared VNF Package. If you are onboarding a supported zip, then click “PROCEED TO VALIDATION” once you can see your added files. You can safely ignore any warnings that come up at this step, but not any errors. (Note: The package validation does not support CSARs currently, they will still work however, providing they meet SDC requirements.)
- Now click “Submit”.

4: Then click “ONBOARD” in the top left. Hover your mouse over the small grey triangle that is just to the right of the “ONBOARD” tab and select “HOME”.

5: Hover over the “IMPORT” square and select “Import VSP”. Find your VSP, click the drop-down arrow beside it and then press the “Import VSP” icon at the far right of the line that drops down.

6: You will now be brought to the draft page of your VF. You can now Certify your VF, by clicking "Certify" in the top-right of the VF Page.

Creating/Configuring your SDC Service:
--------------------------------------

**Step 1:**

In the “HOME” tab of the SDC ONAP Portal, hover over the “ADD” square and select “ADD SERVICE”. Fill in the required fields, select the "Category" "Network Service" and press “Create” in the top right-hand corner.

**Step 2:**

You will be brought to the draft page of your Service. Go to the “Composition” tab on the left-hand side and drag/drop the VF, that you just created, into this service (you can search for the VF by name in the top left).

**Step 3:**

Now you will need to add the Network Service CSAR package to this service. You can do this by clicking in the blank whitespace of the composition, then on the right hand side of the page select the 2nd tab in order to add a deployment artifact. Click "ADD ARTIFACT" here, give an "Artifact Label" of "ns", any description value and ensure you choose a "Type" of "OTHER". Then click "DONE".

**Step 4:**

Finally you can click "Certify" in the top right hand corner, followed by "Distribute". Your Service will now distributed across ONAP.

Preloading SDNC (Optional)
--------------------------

This next step is optional, and is only required if a user needs to add "addiional-params" or "virtual-link" information. You will need to preload SDNC with the required attributes for your VNF. You will need to access the SDNC OpenDaylight RestConf API Documentation in order to add these attributes.

You will then be required to sign in once you access this site, the credentials are as follows, but may change in the future:

Username:     admin

Password:      Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U

Next click on VNF-API.

Then use the following endpoint to post the preload JSON, found below.

Endpoint: restconf/operations/VNF-API:vnf-topology-operation

The following section of code is an example of the JSON that needs to be uploaded to the SDNC OpenDaylight RestConf API Documentation site.

Please note that you will need to set the attributes "generic-vnf-name" and "vnf-name" to the exact name that you will use when instantiating the VNF through VID. The attributes "generic-vnf-type" and "vnf-type" need to have the exact same name as the VSP that you imported to SDC, to create the VF.

**Preload for SDNC:**

.. code-block:: json

    {
        "input": {
            "request-information": {
                "notification-url": "openecomp.org",
                "order-number": "1",
                "order-version": "1",
                "request-action": "PreloadVNFRequest",
                "request-id": "robot21"
            },
            "sdnc-request-header": {
                "svc-action": "reserve",
                "svc-notification-url": "http://openecomp.org:8080/adapters/rest/SDNCNotify",
                "svc-request-id": "robot21"
            },
            "vnf-topology-information": {
                "vnf-assignments": {
                    "availability-zones": [],
                    "vnf-networks": [],
                    "vnf-vms": []
                },
                "vnf-parameters": [{
                    "vnf-parameter-name": "additionalParams",
                    "vnf-parameter-value": "{\"key_1\": \"value_1\"}"
                }, {
                    "vnf-parameter-name": "extVirtualLinks",
                    "vnf-parameter-value": "{}"
                }],
                "vnf-topology-identifier": {
                    "generic-vnf-name": "VnfInstantiateName",
                    "generic-vnf-type": "VspPackageName",
                    "service-type": "vCPE",
                    "vnf-name": "VnfInstantiateName",
                    "vnf-type": "VspPackageName"
                }
            }
        }
    }


The datatype of "additionalParams" and "extVirtualLinks" can be found in the Sol003 Specifications.

The data must be JSON and contain only escaped strings. Here are examples of both:

**Example of additionalParameters parameter:**

.. code-block:: bash

    {\"enableRollback\": \"false\"}


**Example of extVirtualLinks Parameter:**

.. code-block:: bash

    [{\"id\":\"3b94d0be-6e37-4a01-920f-512e96803fc9\",\"tenant\":{\"cloudOwner\":\"CloudOwner\",\"regionName\":\"RegionOne\",\"tenantId\":\"f3d66580-7eff-4da5-8d27-91f984ad0c0b\"},\"resourceId\":\"e6e1a04d-c599-4b09-bc16-688834d0ac50\",\"extCps\":[{\"cpdId\":\"a83f86e0-7e9b-4514-9198-2d9eba91bd8e\",\"cpConfig\":[{\"cpInstanceId\":\"f966673d-fb96-41d4-8e5c-659f1c8c6bcc\",\"linkPortId\":null,\"cpProtocolData\":null}]}],\"extLinkPorts\":null}]


Using VID to send Instantiate Request
-------------------------------------

In order to access the VID (Virtual Infrastructure Deployment) component through the portal, you will need to login with the id “demo”. Once logged in to VID, first ensure that the GR-API is set. First we will need to instantiate the service, once this is done we can then instantiate the VNF. This will be when the ETSI Workflows are run.

**Deploy SDC Service**

You will need to select “Deploy an SDC Service” on the left-hand side of the GUI. You should see your distributed service in the list of services shown here. (Note: if you cannot see your services here then you will need to go back to SDC, to check the status of the distribution.)

- Press "Deploy" on the left-hand side of the service you have distributed.
- Fill out the required fields and press "Confirm".
- Wait for the Service to be instantiated.
- Press "Close" at bottom of pop-up window.

Now you should be brought to the "View/Edit Service Instance" page, focused on the service you just instantiated.

**Instantiate VNF:**

- Press "Add node instance" and select your VNF.
- Fill out the required fields and press "Confirm".
- Leave this VID page open, as this step can take quite some time, depending on a number of factors.
- Monitor the VNF instantiation through your VNFM GUI and back through the SO-ETSI-SOL003-Adapter logs and finally the BPMN logs.
- Upon success, your VNF should be instantiated correctly.


**Delete VNF:**

- Travel back to the service instance that you instantiated your VNF through.
- Simply select the red X on the right-hand side of the VNF instance.
- The VNF should begin terminating now, it may take quite some time, depending on a number of factors.
- Monitor the VNFM GUI and other logs until success.


Monitoring Logs (BPMN, SO-ETSI-SOL003-ADAPTER and VNFM)
-------------------------------------------------------

There are 3 stages of logs to monitor throughout the process of instantiating your service, and sending your request through the SO-ETSI-SOL003-Adapter, to your VNFM.

The initial service instantiation request will be recorded in the BPMN-INFRA pod’s logs. Logging into this pod will enable you to view them through the "debug.log" file.

The VNF instantiation request will appear first in the BPMN-INFRA pod’s logs, then once the ETSI Building Block is being executed you will see entries going through the SO-ETSI-SOL003-Adapter pod’s logs. Followed finally by the VNFM itself receiving a request from the SO-ETSI-SOL003-Adapter. This should all be recorded throughout the “debug.logs” on each of the mentioned pods.

The other areas to monitor would be your VNFM’s GUI (if applicable), your Openstack Tenant’s logs as well as it’s server list and the SO-Admin-Cockpit tool (in order to see the BPMN flow’s progress).

Example Zip VNF Package
-----------------------

Please follow the structure laid out below for creating your onboarding package.

**Structure:**

5 files (2 .yaml, 1 .meta, 1 .json, 1 .env)

- base.yaml
- descriptor.yaml
- base.env
- MANIFEST.json
- TOSCA.meta
- Compressed in a Zip folder.
- No directories. (Flat structure)

**Files:**

base.yaml - This file will be a very simple HEAT template, as it is just required in order to be able to instantiate the Service once its distributed.

descriptor.yaml - This file will contain the VNFD (Virtual Network Function Descriptor). It must be structured to match what the SO-ETSI-SOL003-Adapter searches for.

base.env - This file simply contains some environment variables for the base.yaml.

MANIFEST.json - This file lists all of the other files contained within it's package.

TOSCA.meta - This important file contains the path of the VNFD, which will be used by the SO-ETSI-SOL003-Adapter.


Please find example versions of the files below:

**base.yaml**

.. code-block:: bash

    heat_template_version: 2013-05-23
    description: Simple template to deploy a single compute instance

    parameters:
      simple_name_0:
        type: string
        label: Key Name
        description: Name of key-pair to be used for compute instance
      simple_key:
        type: string
        label: Key Name
        description: Name of key-pair to be used for compute instance
      simple_image_name:
        type: string
        label: Image ID
        description: Image to be used for compute instance
      simple_flavor_name:
        type: string
        label: Instance Type
        description: Type of instance (flavor) to be used
      vnf_id:
        type: string
        label: VNF ID
        description: The VNF ID is provided by ONAP
      vf_module_id:
        type: string
        label: vFirewall module ID
        description: The vFirewall Module ID is provided by ONAP
      simple_netid:
        type: string
        label: Netid
        description: netid
      public_net_id:
        type: string
        label: Netid
        description: public NetId
      ves_ip:
        type: string
        label: Netid
        description: public ves_ip
      node_ip:
        type: string
        label: Netid
        description: public ves_ip

    resources:

      simple_0_private_port:
        type: OS::Neutron::Port
        properties:
          network: { get_param: simple_netid }
          fixed_ips:
          - ip_address: { get_param: node_ip }

      simple_0:
        type: OS::Nova::Server
        properties:
          availability_zone: nova
          key_name: { get_param: simple_key }
          image: { get_param: simple_image_name }
          flavor: { get_param: simple_flavor_name }
          name: { get_param: simple_name_0 }
          metadata: {vnf_id: { get_param: vnf_id }, vf_module_id: { get_param: vf_module_id }}
          networks:
          - port: { get_resource: simple_0_private_port }
          user_data_format: RAW
          user_data:
            str_replace:
              params:
                __ves_ip__: { get_param: ves_ip }
                __vnfId__: { get_param: vnf_id }

              template: |
                #!/bin/bash

                echo "the value we got for vndID was : __vnfId__" >> /tmp/vnfid.log

    outputs:
      oam_management_v4_address:
        description: The IP address of the oam_management_v4_address
        value: { get_param: node_ip  }


**descriptor.yaml**

.. code-block:: bash

    tosca_definitions_version: tosca_simple_yaml_1_1

    imports:
        - etsi_nfv_sol001_vnfd_0_10_0_type.yaml

    node_types:
        Wiki.Demo.VnfmImageId:
            derived_from: tosca.nodes.nfv.VNF
            properties:
                descriptor_id:
                    type: string
                    constraints: [ valid_values: [ VnfmImageId ] ]
                    default: VnfmImageId


The "descriptor.yaml" is the most important file within the package, as it provides the ID/Name of the VNF package for the VNFM to use when instantiating. It must follow the structure above, or the SO-ETSI-SOL003-Adapter will not be able to locate the VNFD. 

Don't forget to replace "VnfmImageId" with the ID of your VNF package.

**base.env**

.. code-block:: bash

    parameters:
      simple_image_name: UBUNTU16
      simple_flavor_name: m1.small
      simple_name_0: SIMPLEUBU
      simple_key: demo-key
      vnf_id: VESMED
      vf_module_id: vfModuleId
      simple_netid:  onap_vip
      public_net_id: nova_floating
      ves_ip: 172.55.10.10
      node_ip: 172.55.10.10


**MANIFEST.json**

.. code-block:: json

    {
        "name": "MMEPackage",
        "description": "Test",
        "version": "0.0",
        "data": [{
            "isBase": true,
            "file": "base.yaml",
            "type": "HEAT",
            "data": [{
                "file": "base.env",
                "type": "HEAT_ENV"
            }]
        },
        {
            "file": "descriptor.yaml",
            "type": "OTHER"
        },
        {
            "file": "TOSCA.meta",
            "type": "OTHER"
        }]
    }


**TOSCA.meta**

.. code-block:: bash

    TOSCA-Meta-File-Version: 1.0
    CSAR-Version: 1.1
    Created-by: Demo
    Entry-Definitions: Artifacts/Deployment/OTHER/descriptor.yaml


The MANIFEST.json and TOSCA.meta are extremely important, if either are incorrectly formatted it will either fail to onboard or fail to distribute when you get to that step.

Ensure that the file names all match and your indentation/quotes are all correct, as it will save you a lot of time.



.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2021 Ericsson Software Technologies

ETSI NFVO Network Service LCM Setup & Functionality
===================================================
This guide will go through the setup required and the use of the ETSI NFVO NS LCM Functionality: Create, Instantiate, Terminate and Delete a Network Service

Ensure you have a healthy ONAP Deployment running. The following components will be required/used as part of this guide:

- SO
- SDC
- AAI
- DMAAP
- Modeling
- UUI

Add Entries to ESR System Info List and ESR VNFM Lists
------------------------------------------------------
This first section of the guide will involve adding values to AAI under the following 3 paths:

- /aai/v15/cloud-infrastructure/cloud-regions/cloud-region/<cloud-owner>/<cloud-region-id>/esr-system-info-list/esr-system-info/<yourVnfmId>
- /aai/v15/external-system/esr-vnfm-list/esr-vnfm/<yourVnfmId>
- /aai/v15/external-system/esr-vnfm-list/esr-vnfm/<yourVnfmId>/esr-system-info-list/esr-system-info/

For the purposes of this guide, the VNFM Simulator will be used as the VNFM throughout. You will need to edit the below CURLs in order to match your specific VNFM's requirements, if using one other than the VNFM Simulator.


**Step 1:**

Exec into any pod within your ONAP deployment that is connected to the primary ONAP network. This will give you access to using the internal service hostnames.

**Step 2:**

Send the following CURL to add the AAI Cloud Infrastructure ESR System Info List Entry. Ensuring you alter the values to match your deployment's AAI Data:

*AAI Cloud Infrastructure ESR System Info List Entry*

.. code-block:: bash

    curl -X PUT -H 'Accept: application/json' -H 'Authorization: Basic YWFpQGFhaS5vbmFwLm9yZzpkZW1vMTIzNDU2IQ==' -H 'Content-Type: application/json' -H 'X-FromAppId:12' -H 'X-TransactionId: 12' https://aai.onap:8443/aai/v15/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/regionOne/esr-system-info-list/esr-system-info/vnfmSimulator -d '{"name": "vnfmSimulatorName", "system-type": "simulator", "vimId": "myCloud", "vendor": "EST", "version": "V1.0", "certificateUrl": "", "url": "http://so-vnfm-simulator.onap:9093/vnflcm/v1/", "user-name": "vnfm", "password": "password1$"}' -k

**Step 3:**

Next you will need to send the following 2 CURLs in order to add entries into the AAI External System ESR VNFM List:

*AAI External System ESR VNFM List*

.. code-block:: bash

    curl -X PUT -H 'Accept: application/json' -H 'Authorization: Basic YWFpQGFhaS5vbmFwLm9yZzpkZW1vMTIzNDU2IQ==' -H 'Content-Type: application/json' -H 'X-FromAppId:12' -H 'X-TransactionId: 12' https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/vnfmSimulator -d '{"vnfmId": "vnfmSimulator", "name": "vnfmSimulatorName", "type": "simulator", "vendor": "est"}'

*AAI External System ESR VNFM System Info List*

.. code-block:: bash

    curl -X PUT -H 'Accept: application/json' -H 'Authorization: Basic YWFpQGFhaS5vbmFwLm9yZzpkZW1vMTIzNDU2IQ==' -H 'Content-Type: application/json' -H 'X-FromAppId:12' -H 'X-TransactionId: 12' https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/vnfmSimulator/esr-system-info-list/esr-system-info/vnfmSimulatorId -d '{"esr-system-info-id": "vnfmSimulatorId", "type": "simulator", "user-name": "vnfm", "password": "password1$", "system-type": "simulator", "service-url": "http://so-vnfm-simulator.onap:9093/vnflcm/v1"}'

**Step 4:**

Take note of the "system-type" and "type" values you have added with these CURLs. This will be used at a later stage when creating your VF in SDC.


Onboard a VNF package along with a Network Service CSAR into SDC
----------------------------------------------------------------
A video demonstration of this process "Onboarding ETSI Based NS" can be found here: SO NFVO NS Onboarding and LCM Demonstrations
It is recommended that you view this video before/alongside this section of the guide.

**Step 1:**

Login to the ONAP Portal. (Link: https://portal.api.simpledemo.onap.org:30225/ONAPPORTAL/login.htm (where "portal.api.simpledemo.onap.org" is set as the IP of your VM/host of your Portal component.)

Login: cs0008
Password: demo123456!

**Step 2:**

Select the SDC App. It is possible that your browser will block the scripts run by the Portal, you will need to enable them if it does.

**Step 3:**

Follow the onboarding guide below:

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

6: You will now be brought to the draft page of your VF. Follow the instructions below in order to certify it.

- First, give your VF a name you will remember, as we will be using this in the following section, then click "Create" in the top-right hand corner.
- You will need to edit the properties of the VF in order to add a property value, that will be required at a later point.
- Go to the “Properties Assignment” tab on the left-hand side, then select your VF on the right-hand side.
- Now go to the "Inputs" tab within the "Properties Assignment”.
- Find the "nf_type" input and enter a value matching the value you entered for "system-type" and "type" in the previous ESR Entries section.
- Click "Save" in the top-right of the "Properties Assignment” tab and then you can Certify your VF, by clicking "Certify" in the top-right of the VF Page.

**Creating/Configuring your SDC Service**

1: In the “HOME” tab of the SDC ONAP Portal, hover over the “ADD” square and select “ADD SERVICE”. Fill in the required fields, select the "Category" "Network Service" and press “Create” in the top right-hand corner.

2: You will be brought to the draft page of your Service. Go to the “Composition” tab on the left-hand side and drag/drop the VF, that you just created, into this service (you can search for the VF by name in the top left).

3: Now you will need to add the Network Service CSAR package to this service. You can do this by clicking in the blank whitespace of the composition, then on the right hand side of the page select the 2nd tab in order to add a deployment artifact. Click "ADD ARTIFACT" here, give an "Artifact Label" of "ns", any description value and ensure you choose a "Type" of "OTHER". Then click "DONE".

4: Finally you can click "Certify" in the top right hand corner, followed by "Distribute". Your Service will now distributed across ONAP.

Onboarding your Network Service to ETSI Catalog through UUI
-----------------------------------------------------------
This next small section (Onboarding your NS through UUI) can also been seen in the "Onboarding ETSI Based NS" video mentioned earlier.

**Step 1:**

Go to the following link, in order to access the UUI Portal: https://msb.api.simpledemo.onap.org:30283/iui/usecaseui/#/home (where "msb.api.simpledemo.onap.org" is set as the IP of your VM/host of your UUI component.)

**Step 2:**

Click "Package Management", you will be brought to the "NS" tab, you should see your Service name in the list. Simply click the "Operation button" on the right hand side of your Service in order to onboard it into the ETSI Catalog.

**Step 3:**

Wait for the confirmation that your Network Service has been Onboarded Successfully.



Triggering the ETSI NFVO NS LCM Functionality
---------------------------------------------

The next section of this guide will go through the actual triggering of the ETSI NFVO NS LCM Functionalities in the following order: Create, Instantiate, Terminate and then Delete.

This section of this guide can be seen in video format at the following link: https://wiki.onap.org/display/DW/SO+NFVO+NS+Onboarding+and+LCM+Demonstrations

It is recommended that you view the "ETSI Based NS Orchestration Demo" video before/alongside this section of the guide.

**Create NS**

**Step 1:**

First you will need to retrieve the NSD ID from the ETSI Catalog Database in order to populate the Create Network Service CURL. The ETSI Catalog Database will be found within the Modeling DB container.

Log into the ETSI Catalog Database and retrieve the data from the "catalog_nspackage" table. The value under the column "NSPACKAGEID" will be the NSD ID required as part of the Create Network Service CURL.

**Step 2:**

Exec into any pod within your ONAP deployment that is connected to the primary ONAP network. This will give you access to using the internal service hostnames.

**Step 3:**

Send the following Create Network Service CURL Command, ensuring your deployment's Global Customer ID (from AAI) and your NSDID (from ETSI Catalog Database) replace the values within the <>:

*Create NS Curl*

.. code-block:: bash

    curl -k -X POST -H "accept:application/json" -H "Content-Type:application/json" -H "HTTP_GLOBALCUSTOMERID: <GLOBALCUSTOMERID>" -d '{"nsdId": "<NSDID>", "nsName": "demo", "nsDescription": "demo"}' -v http://so-etsi-nfvo-ns-lcm.onap:9095/so/so-etsi-nfvo-ns-lcm/v1/api/nslcm/v1/ns_instances -H "Authorization: Basic c28tZXRzaS1uZnZvLW5zLWxjbTpEdXJnMSREYWxhWG95ZA=="

You should receive a synchronous response back with a 201 Create HTTP Response. This response will contain the NS Instance ID of the NS Instance we just created, take note of this as it will be needed in the following steps.

After this step has been completed you should log into the Marie-Db-Galera-0 Pod on your ONAP deployment, then connect to the "NFVO" database. Retrieving all data from the "ns_inst" table will show that your initial NS has been created.

**Instantiate NS**

**Step 1:**

Next you will need to create a JSON file on the pod, which you are exec'ed into, named "Payload.json" in the following example.

*Payload.json*

.. code-block:: json

    {
        "nsFlavourId": "default",
            "locationConstraints": [{
            "vnfProfileId": "b1bb0ce7-2222-4fa7-95ed-4840d70a1177"
        }],
        "additionalParamsForVnf": [{
            "vnfProfileId": "b1bb0ce7-2222-4fa7-95ed-4840d70a1177",
            "vnfInstanceName": "vgwVnf1",
            "vnfInstanceDescription": "test",
            "additionalParams": {
                "vim_id": "<cloud-owner>_<cloud-region-id>_<tenant_id>"
            }
        }]
    }


In the above Payload.json, you must ensure that the "vnfProfileId" matches the VNF Descriptor ID found in the VNF CSAR which you onboarded as part of the VNF Onboarding section of this guide. The "vnfInstanceName" must match your VNF's name, and finally the "vim_id" must be replaced with your AAI Data values for your deployment's Cloud-Owner, Cloud-Region-Id and Tenant-Id respectively.

**Step 2:**

Next you will need to send the following CURL command in order to trigger the instantiation of the Network Service. Ensure that you add the NS Instance ID received in the Create NS response to the URL Path (replacing <NS_INSTANCE_ID>), and match the Global Customer ID as before:

*Instantiate NS Curl*

.. code-block:: bash

    curl -k -X POST -H "Authorization: Basic c28tZXRzaS1uZnZvLW5zLWxjbTpEdXJnMSREYWxhWG95ZA==" -H "accept: application/json" -H "Content-Type:application/json" -H "HTTP_GLOBALCUSTOMERID: ADemoCustomerInXcloud" -d @Payload.json -v http://so-etsi-nfvo-ns-lcm.onap:9095/so/so-etsi-nfvo-ns-lcm/v1/api/nslcm/v1/ns_instances/<NS_INSTANCE_ID>/instantiate

This will trigger the Asynchronous Instantiate NS Functionality. You will receive a response confirming that the process has begun with a 202 Accepted HTTP Response Header. You can view the debug.log files on the following pods in order to view the progress:

- SO-ETSI-NFVO-NS-LCM
- SO-ETSI-SOL003-ADAPTER
- SO-VNFM-SIMULATOR (If making use of the VNFM-Simulator, alternatively the logs of the VNFM you are using)

Once the full instantiation has been completed, reconnect to the "NFVO" database, within the Marie-Db-Galera-0 pod on your ONAP deployment. Retrieving all data from the "ns_inst" table again, you should see that the "STATUS" field for your NS has changed to "INSTANTIATED".

**Terminate NS**

**Step 1:**

Send the following CURL command in order to trigger the Termination of the Network Service. Ensure that you add the NS Instance ID received in the Create NS response to the URL Path (replacing <NS_INSTANCE_ID>):

*Terminate NS Curl*

.. code-block:: bash

    curl -k -X POST -H "Authorization: Basic c28tZXRzaS1uZnZvLW5zLWxjbTpEdXJnMSREYWxhWG95ZA==" -H "accept: application/json" -H "Content-Type: application/json" -v http://so-etsi-nfvo-ns-lcm.onap:9095/so/so-etsi-nfvo-ns-lcm/v1/api/nslcm/v1/ns_instances/<NS_INSTANCE_ID>/terminate

Similar to the Instantiate functionality, this is an asynchronous call, so you will receive a response confirming that the process has begun with a 202 Accepted HTTP Response Header. As above, view the specified debug.log files in order to view the progress.

Once the full termination has been completed, reconnect to the "NFVO" database, within the Marie-Db-Galera-0 pod on your ONAP deployment. Retrieving all data from the "ns_inst" table again, you should see that the "STATUS" field for your NS has changed back to "NOT_INSTANTIATED".

**Delete NS**

**Step 1:**

Finally send the following CURL command in order to trigger the Deletion of the Network Service. Ensure that you add the NS Instance ID received in the Create NS response to the URL Path (replacing <NS_INSTANCE_ID>):

*Delete NS Curl*

.. code-block:: bash

    curl -k -X DELETE -H "Authorization: Basic c28tZXRzaS1uZnZvLW5zLWxjbTpEdXJnMSREYWxhWG95ZA==" -H "accept: application/json" -H "Content-Type: application/json" -v http://so-etsi-nfvo-ns-lcm.onap:9095/so/so-etsi-nfvo-ns-lcm/v1/api/nslcm/v1/ns_instances/<NS_INSTANCE_ID>

Similar to the Create functionality, this is a synchronous call and as such you will receive a 204 No Content Http Response to your CURL command.

Once the full deletion has been completed, reconnect to the "NFVO" database, within the Marie-Db-Galera-0 pod on your ONAP deployment. Retrieving all data from the "ns_inst" table again, you should no longer be able to see the NS Instance in the NS_INST table.

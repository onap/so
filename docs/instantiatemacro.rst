.. This work is licensed under a Creative Commons Attribution 4.0
International License.
.. http://creativecommons.org/licenses/by/4.0
.. _instantiatemacro:

Instantiate Service, Macro mode for PNF
================================================


Overview
--------

Using VID with Macro method means that the user needs to performed
by himself the instantiation of each required object: service, VNF(s), PNF(s).

Using Macro mode, all object instances will be send to ONAP SO in one request.


`Pre-requisites`_
--------------

.. _Pre-requisites: https://docs.onap.org/projects/onap-vid/en/latest/instantiate.html#pre-requisites

`Access to VID portal`_
--------------------

.. _Access to VID portal: https://docs.onap.org/projects/onap-vid/en/latest/instantiate.html#access-to-vid-portal

Instantiate Service in "Macro" mode
-----------------------------------

A Service Model, which contains a PNF resource will be used to demonstrate the "Macro" orchestration mode.

Click Browse SDC Service Models and search for the service to instantiate.

The view show only service models in the DISTRIBUTED state.

.. figure:: images/browse-service-models-pnf.png
   :align: center

Select a service and click Deploy.

Complete the mandatory fields indicated by the red star and click Confirm.

.. figure:: images/new-service-instance-popup-pnf.png
   :align: center

Add a new object to the service instance.

.. note::
   The blue button for adding objects to the service instance appears when you hover over the object model.

.. figure:: images/service-instance-pnf.png
   :align: center

Complete the mandatory fields indicated by the red star and click Confirm.

.. note::
   The PNF instance name must be unique

.. figure:: images/new-pnf-instance.png
   :align: center

To deploy the selected configuration, click "DEPLOY" button in the top right corner.

.. figure:: images/service-instance-pnf-deploy.png
   :align: center

If the service instance with selected objects is deployed correctly, you will be redirected to the page where you can check the instantiation status.

.. figure:: images/instantiation-status.png
   :align: center

To complete the deployment of the service instance with PNF object, send a "pnfRegistration" event to VES.

.. note::
   Without sending the request to VES the process will get stuck at WaitingForPnfReadyBB

Here is an example of pnfRegistration event (enter a valid pnf_instance_name in sourceName field) :

::
    curl --location --request POST 'https://{{worker_ip}}:30417/eventListener/v7' \
    --header 'Content-Type: application/json' \
    --header 'Authorization: Basic c2FtcGxlMTpzYW1wbGUx' \
    --data-raw '{
      "event": {
        "commonEventHeader": {
        "startEpochMicrosec": 1538407540940,
        "sourceId": "val13",
          "eventId": "registration_38407540",
         "nfcNamingCode": "oam",
         "internalHeaderFields": {},
         "eventType": "pnfRegistration",
         "priority": "Normal",
          "version": "4.0.1",
          "reportingEntityName": "NOK6061ZW3",
          "sequence": 0,
          "domain": "pnfRegistration",
          "lastEpochMicrosec": 1538407540940,
          "eventName": "test_PNF_PNF",
          "vesEventListenerVersion": "7.0.1",
          "sourceName": {{pnf_instance_name}},
          "nfNamingCode": "gNB"
        },
        "pnfRegistrationFields": {
          "unitType": "val8",
          "serialNumber": "6061ZW3",
          "pnfRegistrationFieldsVersion": "2.0",
          "manufactureDate": "1538407540942",
          "modelNumber": "val6",
          "lastServiceDate": "1538407540942",
          "unitFamily": "BBU",
          "vendorName": "Nokia",
          "oamV4IpAddress": "10.11.12.13",
          "oamV6IpAddress": "1::::9",
          "softwareVersion": "val7"
        }
      }
    }'


Instantiation Status
-----------------------------------

The page Instantiation Status contains information about all deployed service instances.

To check detailed information, click the ellipsis on the right side of the selected service instance.

.. figure:: images/instantiation-status-complete.png
   :align: center

.. figure:: images/service-information.png
   :align: center



Deleting Network, VF module, VNF, Service
-----------------------------------------

To delete a previously created "Macro" instance using VID, the entire service instance must be deleted.

To proceed those deletion, from VID Home screen:

- search for existing service instance
- edit/view the service instance you want to delete
- click on red button with white cross
- confirm deletion of the service instance

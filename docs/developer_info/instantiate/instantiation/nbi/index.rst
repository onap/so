.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright 2019 ONAP Contributors.  All rights reserved.

.. _doc_guide_user_ser_inst_nbi:


Service Instantiation via ONAP NBI API (TM Forum)
=================================================

ONAP NBI allow you to use a TM Forum standardized API (serviceOrder API)

Additional info in:

.. toctree::
   :maxdepth: 1
   :titlesonly:

   NBI Guide <../../../../../submodules/externalapi/nbi.git/docs/offeredapis/offeredapis.rst>


ONAP NBI will convert that request to ONAP SO request.


ServiceOrder management in NBI will support 2 modes:

* E2E integration - NBI calls SO API to perform an End-To-end integration
* Service-level only integration - NBI will trigger only SO request at
  serviceInstance level (not at VNF, not at Vf-module level and nothing will
  be created on cloud platform)

ONAP SO prerequisite: SO must be able to find a BPMN to process service
fulfillment (integrate VNF, VNF activation in SDNC, VF module)

The choice of the mode is done by NBI depending on information retrieved
in SDC. If the serviceSpecification is within a Category "E2E Service" ,
NBI will use E2E SO API, if not only API at service instance level
will be used.

There is no difference or specific expectation in the service order API
used by NBI user.


Example of serviceOrder to instantiate (=add) a service based on model
with id=0d463b0c-e559-4def-8d7b-df64cfbd3159


::

  curl -X POST \
    http://nbi.api.simpledemo.onap.org:30274/nbi/api/v4/serviceOrder \
    -H 'Accept: application/json' \
    -H 'Content-Type: application/json' \
    -H 'cache-control: no-cache' \
    -d '{
    "externalId": "BSS_order_001",
    "priority": "1",
    "description": "this is a service order to instantiate a service",
    "category": "Consumer",
    "requestedStartDate": "",
    "requestedCompletionDate": "",
    "relatedParty": [
      {
        "id": "JohnDoe",
        "role": "ONAPcustomer",
        "name": "JohnDoe"
      }
    ],
    "orderItem": [
      {
        "id": "1",
        "action": "add",
        "service": {
          "name": "my_service_model_instance_01",
          "serviceState": "active",
          "serviceSpecification": {
            "id": "0d463b0c-e559-4def-8d7b-df64cfbd3159"
          }
        }
      }
    ]
  }'

In the response, you will obtain the serviceOrderId value.

Then you have the possibility to check about the serviceorder
(here after the serviceOrderId=5d06309da0e46400017b1123).

This will allow you to get the serviceOrder Status (completed, failed...)

::

  curl -X GET \
    http://nbi.api.simpledemo.onap.org:30274/nbi/api/v4/serviceOrder/5d06309da0e46400017b1123 \
    -H 'Accept: application/json' \
    -H 'Content-Type: application/json' \
    -H 'cache-control: no-cache'

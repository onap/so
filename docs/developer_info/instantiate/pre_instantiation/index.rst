.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright 2019 ONAP Contributors.  All rights reserved.

.. _doc_guide_user_pre_ser-inst:


Pre Service instantiation Operations
====================================

Several operations need to be performed after Service model distribution,
but before instantiating a service.

Those operations are only available via REST API requests.

Various tools can be used to send REST API requests.

Here after are examples using "curl" command line tool that you can use in
a Unix Terminal.


Declare owningEntity, lineOfBusiness, Platform and Project
----------------------------------------------------------

At one point during Service Instantiation, the user need to select values for
those 4 parameters

* Owning Entity
* Line Of Business
* Platform
* Project


Those parameters and values must be pre-declared in ONAP VID component
using REST API

Those informations will be available to all service instantiation
(you only need to declare them once in ONAP)


Example for "Owning Entity" named "Test"

::

  curl -X POST \
    http://vid.api.simpledemo.onap.org:30238/vid/maintenance/category_parameter/owningEntity \
    -H 'Accept-Encoding: gzip, deflate' \
    -H 'Content-Type: application/json' \
    -H 'cache-control: no-cache' \
    -d '{
      "options": ["Test"]
  }'

Example for "platform" named "Test_Platform"

::

  curl -X POST \
    http://vid.api.simpledemo.onap.org:30238/vid/maintenance/category_parameter/platform \
    -H 'Content-Type: application/json' \
    -H 'cache-control: no-cache' \
    -d '{
      "options": [""Test_Platform"]
  }'

Example for "line of business" named "Test_LOB"

::

  curl -X POST \
  http://vid.api.simpledemo.onap.org:30238/vid/maintenance/category_parameter/lineOfBusiness \
  -H 'Content-Type: application/json' \
  -H 'cache-control: no-cache' \
  -d '{
    "options": ["Test_LOB"]
  }'

Example for "project" named "Test_project"

::

  curl -X POST \
    http://vid.api.simpledemo.onap.org:30238/vid/maintenance/category_parameter/project \
    -H 'Content-Type: application/json' \
    -H 'cache-control: no-cache' \
    -d '{
      "options": ["Test_project"]
  }'




Declare a customer
------------------

Each time you have a new customer, you will need to perform those operations

This operation is using ONAP AAI REST API

Any service instance need to be linked to a customer

in the query path, you put the customer_name

in the query body you put the customer name again

Here after an example to declare a customer named "my_customer_name"


::

  curl -X PUT \
    https://aai.api.sparky.simpledemo.onap.org:30233/aai/v16/business/customers/customer/my_customer_name \
    -H 'Accept: application/json' \
    -H 'Authorization: Basic QUFJOkFBSQ==' \
    -H 'Content-Type: application/json' \
    -H 'X-FromAppId: AAI' \
    -H 'X-TransactionId: 808b54e3-e563-4144-a1b9-e24e2ed93d4f' \
    -H 'cache-control: no-cache' \
    -d '{
      "global-customer-id": "my_customer_name",
      "subscriber-name": "my_customer_name",
      "subscriber-type": "INFRA"
  }' -k


check customers in ONAP AAI (you should see if everything ok in the response)

::

  curl -X GET \
    https://aai.api.sparky.simpledemo.onap.org:30233/aai/v16/business/customers \
    -H 'Accept: application/json' \
    -H 'Authorization: Basic QUFJOkFBSQ==' \
    -H 'Content-Type: application/json' \
    -H 'X-FromAppId: AAI' \
    -H 'X-TransactionId: 808b54e3-e563-4144-a1b9-e24e2ed93d4f' \
    -H 'cache-control: no-cache' -k


Associate Service Model to Customer
-----------------------------------


This operation is using ONAP AAI REST API

in the query path, you put the customer_name and the service model name

in the query body you put the service model UUID

::

  curl -X PUT \
    https://aai.api.sparky.simpledemo.onap.org:30233/aai/v16/business/customers/customer/my_customer_name/service-subscriptions/service-subscription/my_service_model_name \
    -H 'Accept: application/json' \
    -H 'Authorization: Basic QUFJOkFBSQ==' \
    -H 'Content-Type: application/json' \
    -H 'Postman-Token: d4bc4991-a518-4d75-8a87-674ba44bf13a' \
    -H 'X-FromAppId: AAI' \
    -H 'X-TransactionId: 808b54e3-e563-4144-a1b9-e24e2ed93d4f' \
    -H 'cache-control: no-cache' \
    -d '{
      "service-id": "11265d8c-2cc2-40e5-95d8-57cad81c18da"
  }' -k




Associate Cloud Site to Customer
--------------------------------

in the query path, you put the customer_name and the service model name

in the query body you put the cloud owner name, the cloud site name,
the tenant id and the tenant name


::

  curl -X PUT \
    https://aai.api.sparky.simpledemo.onap.org:30233/aai/v16/business/customers/customer/my_customer_name/service-subscriptions/service-subscription/my_service_model_name/relationship-list/relationship \
    -H 'Accept: application/json' \
    -H 'Authorization: Basic QUFJOkFBSQ==' \
    -H 'Content-Type: application/json' \
    -H 'Postman-Token: 11ea9a9e-0dc8-4d20-8a78-c75cd6928916' \
    -H 'X-FromAppId: AAI' \
    -H 'X-TransactionId: 808b54e3-e563-4144-a1b9-e24e2ed93d4f' \
    -H 'cache-control: no-cache' \
    -d '{
      "related-to": "tenant",
      "related-link": "/aai/v16/cloud-infrastructure/cloud-regions/cloud-region/my_cloud_owner_name/my_cloud_site_name/tenants/tenant/234a9a2dc4b643be9812915b214cdbbb",
      "relationship-data": [
          {
              "relationship-key": "cloud-region.cloud-owner",
              "relationship-value": "my_cloud_owner_name"
          },
          {
              "relationship-key": "cloud-region.cloud-region-id",
              "relationship-value": "my_cloud_site_name"
          },
          {
              "relationship-key": "tenant.tenant-id",
              "relationship-value": "234a9a2dc4b643be9812915b214cdbbb"
          }
      ],
      "related-to-property": [
          {
              "property-key": "tenant.tenant-name",
              "property-value": "my_tenant_name"
          }
      ]
  }' -k


check (you should see if everything ok in the response)

::

  curl -X GET \
    'https://aai.api.sparky.simpledemo.onap.org:30233/aai/v16/business/customers/customer/my_customer_name/service-subscriptions?depth=all' \
    -H 'Accept: application/json' \
    -H 'Authorization: Basic QUFJOkFBSQ==' \
    -H 'Content-Type: application/json' \
    -H 'X-FromAppId: AAI' \
    -H 'X-TransactionId: 808b54e3-e563-4144-a1b9-e24e2ed93d4f' \
    -H 'cache-control: no-cache' -k

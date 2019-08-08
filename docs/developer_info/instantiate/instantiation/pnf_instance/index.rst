.. This work is licensed under a Creative Commons Attribution 4.0
.. International License. http://creativecommons.org/licenses/by/4.0
.. Copyright 2019 ONAP Contributors. All rights reserved.



Declare PNF instances in ONAP
=============================

PNF instances can be declared in ONAP inventory (AAI) using REST API


An example:

::

  curl -X PUT \
    https://{{ONAP_LB_IP@}}:30233/aai/v16/network/pnfs/pnf/my_pnf_instance_001 \
    -H 'Accept: application/json' \
    -H 'Authorization: Basic QUFJOkFBSQ==' \
    -H 'Cache-Control: no-cache' \
    -H 'Content-Type: application/json' \
    -H 'Postman-Token: f5e2aae0-dc1c-4edb-b9e9-a93b05aee5e8' \
    -H 'X-FromAppId: AAI' \
    -H 'X-TransactionId: 999' \
    -H 'depth: all' \
    -d '{
    "pnf-name":" my_pnf_instance_001",
    "equip-type":" router",
    "nf-role":"primary",
    "p-interfaces": {
        "p-interface": [
            {
                "interface-name": "ae1",
                "port-description": "Link aggregate for trunk between switches"
            },
            {
                "interface-name": "xe-0/0/6",
                "port-description": "to PNF_instance_002 trunk1"
            },
            {
                "interface-name": "xe-0/0/2",
                "port-description": "to PNF_instance_003 trunk1"
            },
            {
                "interface-name": "xe-0/0/10",
                "port-description": "to PNF_instance_004 trunk1"
            },
            {
                "interface-name": "xe-0/0/0",
                "port-description": "firewall trunk"
            },
            {
                "interface-name": "xe-0/0/14",
                "port-description": "to PNF_instance_005 trunk1"
            }
        ]
    }
  }' -k


It is possible to declare the location where is deployed the PNF
(called a "complex" in ONAP AAI)

::

  curl -X PUT \
    https:// {{ONAP_LB_IP@}}:30233/aai/v11/cloud-infrastructure/complexes/complex/my_complex_name \
    -H 'Accept: application/json' \
    -H 'Authorization: Basic QUFJOkFBSQ==' \
    -H 'Cache-Control: no-cache' \
    -H 'Content-Type: application/json' \
    -H 'Postman-Token: 43523984-db01-449a-8a58-8888871110bc' \
    -H 'X-FromAppId: AAI' \
    -H 'X-TransactionId: 999' \
    -H 'depth: all' \
    -d '{
    "physical-location-type":"PoP",
    "physical-location-id":"my_complex_name",
    "complex-name":"Name of my Complex",
    "city":"LANNION",
    "postal-code":"22300",
    "country":"FRANCE",
    "street1":"Avenue Pierre Marzin",
    "region":"Europe"
  }' -k



To indicate that a PNF instance is located in a complex, we create a relation

::

  curl -X PUT \
    https:// {{ONAP_LB_IP@}}:30233/aai/v14/network/pnfs/pnf/my_pnf_instance_001/relationship-list/relationship \
    -H 'Accept: application/json' \
    -H 'Authorization: Basic QUFJOkFBSQ==' \
    -H 'Cache-Control: no-cache' \
    -H 'Content-Type: application/json' \
    -H 'Postman-Token: 15315304-17c5-4e64-aada-bb149f1af915' \
    -H 'X-FromAppId: AAI' \
    -H 'X-TransactionId: 999' \
    -H 'depth: all' \
    -d '{
      "related-to": "complex",
      "related-link": "/aai/v11/cloud-infrastructure/complexes/complex/my_complex_name"
  }' -k

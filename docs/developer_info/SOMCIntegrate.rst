.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2018 Huawei Technologies Co., Ltd.

Integrate SO with MultiCloud
=============================

There are 2 SO tables that you need to modify if you want to use Multicloud. They are in the MariaDB container in the dev-so service. Here are the credentials to access the DB (through mysql command line): cataloguser/catalog123. The table you need to use is called catalogdb.

 

The 2 tables are cloud_sites and identity_services. cloud_sites contains information about the cloud (region name and keystone for example). The keystone name (IDENTITY_SERVICE_ID) is the key of identity_services table, which contains specific information about cloud authentication. In the example below, you can see my configuration for a cloud region called RegionTwo, in which SO uses Multicoud for talking to the underlying cloud platfrorm. Note indeed that the IDENTITY_URL in identity_services redirects to Multicloud. In practice, SO reads cloud and authentication information from this two tables, and uses the provided keystone authentication given an identity URL.

 

MariaDB [catalogdb]> select * from cloud_sites;

+-------------------+-----------+---------------------+---------------+-----------+-------------+----------+--------------+-----------------+---------------------+---------------------+

| ID                | REGION_ID | IDENTITY_SERVICE_ID | CLOUD_VERSION | CLLI      | CLOUDIFY_ID | PLATFORM | ORCHESTRATOR | LAST_UPDATED_BY | CREATION_TIMESTAMP  | UPDATE_TIMESTAMP    |

+-------------------+-----------+---------------------+---------------+-----------+-------------+----------+--------------+-----------------+---------------------+---------------------+

| Chicago           | ORD       | RAX_KEYSTONE        | 2.5           | ORD       | NULL        | NULL     | NULL         | FLYWAY          | 2018-12-28 22:58:34 | 2018-12-28 22:58:34 |

| Dallas            | DFW       | RAX_KEYSTONE        | 2.5           | DFW       | NULL        | NULL     | NULL         | FLYWAY          | 2018-12-28 22:58:34 | 2018-12-28 22:58:34 |

| DEFAULT           | RegionOne | DEFAULT_KEYSTONE    | 2.5           | RegionOne | NULL        | NULL     | NULL         | FLYWAY          | 2018-12-28 22:58:34 | 2018-12-28 22:58:34 |

| Northern Virginia | IAD       | RAX_KEYSTONE        | 2.5           | IAD       | NULL        | NULL     | NULL         | FLYWAY          | 2018-12-28 22:58:34 | 2018-12-28 22:58:34 |

| RegionOne         | RegionOne | DEFAULT_KEYSTONE    | 2.5           | RegionOne | NULL        | NULL     | NULL         | FLYWAY          | 2018-12-28 22:58:34 | 2018-12-28 22:58:34 |

| RegionTwo         | RegionTwo | KEYSTONE_REGION_TWO | 2.5           | RegionTwo | NULL        | NULL     | NULL         | FLYWAY          | 2019-01-02 20:07:28 | 2019-01-02 20:07:28 |

+-------------------+-----------+---------------------+---------------+-----------+-------------+----------+--------------+-----------------+---------------------+---------------------+

 

 

MariaDB [catalogdb]> select * from identity_services;

+---------------------+--------------------------------------------------------------------------------+----------------------+----------------------------------+--------------+-------------+-----------------+----------------------+------------------------------+-----------------+---------------------+---------------------+

| ID                  | IDENTITY_URL                                                                   | MSO_ID               | MSO_PASS                         | ADMIN_TENANT | MEMBER_ROLE | TENANT_METADATA | IDENTITY_SERVER_TYPE | IDENTITY_AUTHENTICATION_TYPE | LAST_UPDATED_BY | CREATION_TIMESTAMP  | UPDATE_TIMESTAMP    |

+---------------------+--------------------------------------------------------------------------------+----------------------+----------------------------------+--------------+-------------+-----------------+----------------------+------------------------------+-----------------+---------------------+---------------------+

| DEFAULT_KEYSTONE    | http://135.197.225.10:5000/v2.0                                                | admin                | a83e2b8446193c5ac450d84f0f1dc711 | service      | admin       |               1 | KEYSTONE             | USERNAME_PASSWORD            | FLYWAY          | 2018-12-28 22:58:34 | 2018-12-28 22:58:34 |

| KEYSTONE_REGION_TWO | http://10.43.117.142:9001/api/multicloud/v0/CloudOwner_RegionTwo/identity/v2.0 | username                | <encrypted pwd> | service      | admin       |               1 | KEYSTONE             | USERNAME_PASSWORD            | FLYWAY          | 2019-01-02 20:03:26 | 2019-01-02 20:03:26 |

| RAX_KEYSTONE        | https://identity.api.rackspacecloud.com/v2.0                                   | RACKSPACE_ACCOUNT_ID | RACKSPACE_ACCOUNT_APIKEY         | service      | admin       |               1 | KEYSTONE             | RACKSPACE_APIKEY             | FLYWAY          | 2018-12-28 22:58:34 | 2018-12-28 22:58:34 |

+---------------------+--------------------------------------------------------------------------------+----------------------+----------------------------------+--------------+-------------+-----------------+----------------------+------------------------------+-----------------+---------------------+---------------------+

 

One thing to know is that the actual IP 10.43.117.142:9001 is the MSB (iag) container. Multicloud registers with MSB, so you can use MSB to fetch the Multicloud endpoint (I think you can use the K8S cluster IP and MSB node port for that instead of the actual MSB container IP and port).

 

One final thing: you may need to add identity URL to the AAI cloud region as well, like this:

 

curl -X PUT \

  https://135.197.220.117:30233/aai/v11/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/RegionTwo \

  -H 'Accept: application/json' \

  -H 'Content-Type: application/json' \

  -H 'Postman-Token: b05ff02d-78c7-4e1e-9457-d9fa9cc5da65' \

  -H 'X-FromAppId: AAI' \

  -H 'X-TransactionId: get_aai_subscr' \

  -H 'cache-control: no-cache' \

  -d '{

    "cloud-owner": "CloudOwner",

    "cloud-region-id": "RegionTwo",

    "cloud-type": "openstack",

    "cloud-region-version": "v2.5",

    "identity-url": "http://10.43.111.6/api/multicloud/v0/CloudOwner_RegionTwo/identity/v2.0/tokens",

    "cloud-zone": "bm-2",

    "complex-name": "complex-2",

    "tenants": {

        "tenant": [{

            "tenant-id": "c236140a3dff4911bb4c7c86940616cc",

            "tenant-name": "ONAP_Casablanca"

        }]

    },

    "esr-system-info-list": {

      "esr-system-info": [{

        "esr-system-info-id": "1",

        "system-name": "OpenStack-2",

            "type": "vim",

            "service-url": "http://XXX:5000/v3",

            "user-name": "username",

            "password": "password",

            "system-type": "VIM",

            "ssl-insecure": true,

            "cloud-domain": "default",

            "default-tenant": "ONAP_Casablanca"

      }]

    }

}'

 
.. _config:

Openstack Configuration
=======================

The Openstack plugin requires credentials and endpoint setup information in order to authenticate and interact with Openstack.

This information will be gathered by the plugin from the following sources,
each source possibly partially or completely overriding values gathered from previous ones:

1. environment variables for each of the configuration parameters.
2. JSON file at ``~/openstack_config.json`` or at a path specified by the value of an environment variable named ``OPENSTACK_CONFIG_PATH``
3. values specified in the ``openstack_config`` property for the node whose operation is currently getting executed (in the case of relationship operations, the ``openstack_config`` property of either the **source** or **target** nodes will be used if available, with the **source**'s one taking precedence).

The structure of the JSON file in section (2), as well as of the ``openstack_config`` property in section (3), is as follows:

.. highlight:: json

::

    {
        "username": "",
        "password": "",
        "tenant_name": "",
        "auth_url": "",
        "region": "",
        "nova_url": "",
        "neutron_url": "",
        "custom_configuration": ""
    }

* ``username`` username for authentication with Openstack Keystone service.
* ``password`` password for authentication with Openstack Keystone service.
* ``tenant_name`` name of the tenant to be used.
* ``auth_url`` URL of the Openstack Keystone service.

    .. attention:: New in 2.0

        ``auth_url`` must include the full keystone auth URL, including the version number.

* ``region`` Openstack region to be used. This may be optional when there's but a single region.
* ``nova_url`` (**DEPRECATED** - instead, use ``custom_configuration`` to pass ``endpoint_override`` directly to the Nova client) explicit URL for the Openstack Nova service. This may be used to override the URL for the Nova service that is listed in the Keystone service.
* ``neutron_url`` (**DEPRECATED** - instead, use ``custom_configuration`` to pass ``endpoint_url`` directly to the Neutron client) explicit URL for the Openstack Neutron service. This may be used to override the URL for the Neutron service that is listed in the Keystone service.
* ``custom_configuration`` a dictionary which allows overriding or directly passing custom configuration parameter to each of the Openstack clients, by using any of the relevant keys: ``keystone_client``, ``nova_client``, ``neutron_client`` or ``cinder_client``.
  * Parameters passed directly to Openstack clients using the ``custom_configuration`` mechanism will override other definitions (e.g. any of the common Openstack configuration parameters listed above, such as ``username`` and ``tenant_name``)
  * The following is an example for the usage of the ``custom_configuration`` section in a blueprint:

.. highlight:: yaml

::

    custom_configuration:
      nova_client:
        endpoint_override: nova-endpoint-url
        nova_specific_key_1: value_1
        nova_specific_key_2: value_2
      neutron_client:
        endpoint_url: neutron-endpoint-url
      keystone_client:
        ..
      cinder_client:
        ..


The environment variables mentioned in (1) are the standard Openstack environment variables equivalent to the ones in the JSON file or ``openstack_config`` property. In their respective order, they are:

* ``OS_USERNAME``
* ``OS_PASSWORD``
* ``OS_TENANT_NAME``
* ``OS_AUTH_URL``
* ``OS_REGION_NAME``
* ``NOVACLIENT_BYPASS_URL``
* ``OS_URL``

**Note**: ``custom_configuration`` doesn't have an equivalent standard Openstack environment variable.


    The Openstack manager blueprint stores the Openstack configuration used for the bootstrap process in a JSON file as described in (2) at
    ``~/openstack-config.json``.
    Therefore, if they've been used for bootstrap,
    the Openstack configuration for applications isn't required as the plugin will default to these same settings.


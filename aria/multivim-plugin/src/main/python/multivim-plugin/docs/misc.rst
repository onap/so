
.. highlight:: yaml

Tips
====

* It is highly recommended to **ensure that Openstack names are unique** (for a given type): While Openstack allows for same name objects, having identical names for objects of the same type might lead to ambiguities and errors.

* To set up DNS servers for Openstack servers (whether it's the Cloudify Manager or application VMs), one may use the Openstack ``dns_nameservers`` parameter for the [Subnet type](#cloudifyopenstacknodessubnet) - that is, pass the parameter directly to Neutron by using the ``args`` input of the operations in Subnet node, e.g.::

    my_subnet_node:
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                dns_nameservers: [1.2.3.4]
        cloudify.interfaces.validation:
          creation:
            inputs:
              args:
                dns_nameservers: [1.2.3.4]

  This will set up ``1.2.3.4`` as the DNS server for all servers on this subnet.

* Public keys, unlike the rest of the Openstack resources, are user-based rather than tenant-based. When errors indicate a missing keypair, make sure you're using the correct user rather than tenant.

* ICMP rules show up on Horizon (Openstack GUI) as ones defined using ``type`` and ``code`` fields, rather than a port range. However, in the actual Neutron (and Nova, in case of Nova-net security groups) service, these fields are represented using the standard port range fields (i.e., ``type`` and ``code`` correspond to ``port_range_min`` and ``port_range_max`` (respectively) on Neutron security groups, and to ``from_port`` and ``to_port`` (respectively) on Nova-net security groups).

  ** For example, to set a security group rule which allows **ping** from anywhere, the following setting may be declared in the blueprint:
    * ``protocol``: ``icmp``
    * ``port_range_min``: ``0`` (type)
    * ``port_range_max``: ``0`` (code)
    * ``remote_ip_prefix``: ``0.0.0.0/0``

* To use Openstack Neutron's ML2 extensions, use the ``args`` input for the Network's ``create`` operation. For example, the `provider network <http://developer.openstack.org/api-ref-networking-v2-ext.html#createProviderNetwork>`_ may be set in the following way::

    my_network:
      type: cloudify.openstack.nodes.Network
      ...
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                # Note that for this parameter to work, OpenStack must be configured to use Neutron's ML2 extensions
                provider:network_type: vxlan

* Ordering NICs in the Openstack plugin can be done in the 1.4 version of the Openstack plugin by simply stating the relationships to the various networks (or ports) in the desired order, e.g.::

    node_templates:
      server:
        type: cloudify.openstack.nodes.Server
        relationships:
          - target: network1
            type: cloudify.relationships.connected_to
          - target: network2
            type: cloudify.relationships.connected_to

      network1:
        type: cloudify.openstack.nodes.Network
        properties:
          resource_id: network1

      network2:
        type: cloudify.openstack.nodes.Network
        properties:
          resource_id: network2

  In the example above, network1 will be connected to a NIC preceding the one network2 will - however these wont be eth0/eth1, but rather eth1/eth2 - because by default, the management network will be prepended to the networks list (i.e. it'll be assigned to eth0).
  To avoid this prepending, one should explicitly declare a relationship to the management network, where the network's represented in the blueprint by an existing resource (using the "use_external_resource" property).
  This will cause the management network adhere the NICs ordering as the rest of them.
  Example::

    node_templates:
      server:
        type: cloudify.openstack.nodes.Server
        properties:
          management_network_name: network2
        relationships:
          - target: network1
            type: cloudify.relationships.connected_to
          - target: network2
            type: cloudify.relationships.connected_to
          - target: network3
            type: cloudify.relationships.connected_to

      network1:
        type: cloudify.openstack.nodes.Network
        properties:
          resource_id: network1

      network2:
        type: cloudify.openstack.nodes.Network
        properties:
          use_external_resource: true
          resource_id: network2

      network3:
        type: cloudify.openstack.nodes.Network
        properties:
          use_external_resource: true
          resource_id: network3

  In this example, "network2" represents the management network, yet it'll be connected to eth1, while "network1" will take eth0, and "network3" (which also happened to already exist) will get connected to eth2.

      The server's property "management_network_name: network2" is not mandatory for this to work - this was just to make the example clear - yet the management network can also be inferred from the provider context (which is what happens when this property isn't explicitly set). Were the provider context to have "network2" set as the management network, this example would've worked just the same with this property omitted.

Misc
====

* The plugin's operations are each **transactional**
  (and therefore also retryable on failures),
  yet not **idempotent**.
  Attempting to execute the same operation twice is likely to fail.

* Over this documentation, it's been mentioned multiple times that some configuration-saving information may be available in the Provider Context.
  The Openstack manager blueprint and Openstack provider both create this relevant information,
  and therefore if either was used for bootstrapping, the Provider Context will be available for the Openstack plugin to use.

The exact details of the structure of the Openstack Provider Context are not documented since this feature is going through deprecation and will be replaced with a more advanced one.

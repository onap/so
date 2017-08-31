
Nova-net Support
================

The Openstack plugin includes support for Nova-net mode -
i.e. an Openstack installation which does not have the Networking API
(Neutron service).

In such an environment, there is but a single preconfigured private network,
which all servers make use of automatically.
There are no subnets, networks, routers or ports.
Since these resource types don't exist,
the plugin's equivalent types aren't valid to use in such an environment.

There are, however, some resource types whose API is available via both the Nova and Neutron services - These had originally been on the Nova service,
and later were moved and got extended implementation in the Neutron one,
but were also kept in the Nova service for backward compatibility.

For these resource types, the Openstack plugin defines two separate types - one in the plugin's standard types namespace (``cloudify.openstack.nodes.XXX``),
which uses the newer and extended API via the Neutron service;
and Another in a special namespace (``cloudify.openstack.nova_net.nodes.XXX``),
which uses the older API via the Nova service.
This is why you may notice two separate types defined for [Floating](#cloudifyopenstacknodesfloatingip) [IP](#cloudifyopenstacknovanetnodesfloatingip),
as well as for [Security](#cloudifyopenstacknodessecuritygroup) [Group](#cloudifyopenstacknovanetnodessecuritygroup).


To summarize, ensure that when working in a Nova-net Openstack environment,
Neutron types aren't used - these include all types whose resources' APIs are natively available only via the Network API,
as well as the types which are in the ``cloudify.openstack.nova_net.Nodes`` namespace.

On the opposite side, when using an Openstack environment which supports Neutron,
it's recommended to use the Neutron-versions of the relevant types
(i.e. avoid any types defined under the
``cloudify.openstack.nova_net.Nodes`` namespace),
as they offer more advanced capabilities.
However, it's important to mention that this is not required,
and using the Nova-versions of some types in a Neutron-enabled environment is possible and will work as well.


Nova-net Node Types
-------------------


.. cfy:node:: cloudify.openstack.nova_net.nodes.FloatingIP


.. cfy:node:: cloudify.openstack.nova_net.nodes.SecurityGroup


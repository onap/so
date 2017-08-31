
.. highlight:: yaml

Examples
========

Example I
---------

This example will show how to use most of the types in this plugin,
as well as how to make the relationships between them.

We'll see how to create a server with a security group set on it and a floating_ip associated to it,
on a subnet in a network.


The following is an excerpt from the blueprint's `blueprint`.`nodes` section::

    my_floating_ip:
      type: cloudify.openstack.nodes.FloatingIP
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                floating_network_name: Ext-Net


    my_network:
      type: cloudify.openstack.nodes.Network
      properties:
        resource_id: my_network_openstack_name


    my_subnet:
      type: cloudify.openstack.nodes.Subnet
      properties:
        resource_id: my_subnet_openstack_name
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                cidr: 1.2.3.0/24
                ip_version: 4
        cloudify.interfaces.validation:
          creation:
            inputs:
              args:
                cidr: 1.2.3.0/24
                ip_version: 4
      relationships:
        - target: my_network
          type: cloudify.relationships.contained_in


    my_security_group:
      type: cloudify.openstack.nodes.SecurityGroup
      properties:
        resource_id: my_security_group_openstack_name
        rules:
          - remote_ip_prefix: 0.0.0.0/0
            port: 8080


    my_server:
      type: cloudify.openstack.nodes.Server
      properties:
        resource_id: my_server_openstack_name
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                image: 8672f4c6-e33d-46f5-b6d8-ebbeba12fa02
                flavor: 101
        cloudify.interfaces.validation:
          creation:
            inputs:
              args:
                image: 8672f4c6-e33d-46f5-b6d8-ebbeba12fa02
                flavor: 101
      relationships:
        - target: my_network
          type: cloudify.relationships.connected_to
        - target: my_subnet
          type: cloudify.relationships.depends_on
        - target: my_floating_ip
          type: cloudify.openstack.server_connected_to_floating_ip
        - target: my_security_group
          type: cloudify.openstack.server_connected_to_security_group


1. Creates a floating IP, whose node name is ``my_floating_ip``, and whose floating_network_name is ``Ext-Net`` (This value represents the name of the external network).
2. Creates a network, whose node name is ``my_network``, and whose name on Openstack is ``my_network_openstack_name``.
3. Creates a subnet, whose node name is ``my_subnet``, and whose name on Openstack is ``my_subnet_openstack_name``. The subnet's address range is defined to be 1.2.3.0 - 1.2.3.255 using the ``cidr`` parameter, and the subnet's IP version is set to version 4. The subnet will be set on the ``my_network_openstack_name`` network because of the relationship to the ``my_network`` node.
4. Creates a security_group, whose node name is ``my_security_group``, and whose name on Openstack is ``my_security_group_openstack_Name``. The security group is set with a single rule, which allows all traffic (since we use the address range ``0.0.0.0/0``) to port ``8080`` (default direction is *ingress*).
5. Creates a server, whose node name is ``my_server``, and whose name on openstack is ``my_server_openstack_name``. The server is set with an image and flavor IDs. The server is set with multiple relationships:

  - A relationship to the ``my_network`` node: Through this relationship,
    the server will be automatically placed on the ``my_network_openstack_name`` network.
  - A relationship to the ``my_subnet`` node:
    This relationship is strictly for ensuring the order of creation is correct,
    as the server requires the ``my_subnet_openstack_name`` subnet to exist before it can be created on it.
  - A relationship to the ``my_floating_ip`` node:
    This designated relationship type will take care of associating the server with the floating IP represented by the ``my_floating_ip`` node.
  - A relationship with the ``my_security_group`` node:
    This relationship will take care of setting the server up with the security group represented by the ``my_security_group`` node.


Example II
----------

This example will show how to use the ``router`` and ``port`` types, as well as some of the relationships that were missing from Example I.

We'll see how to create a server connected to a port, where the port is set on a subnet in a network, and has a security group set on it. Finally, we'll see how this subnet connects to a router and from there to the external network.


The following is an excerpt from the blueprint's ``blueprint``.``node_templates`` section::

    my_network:
      type: cloudify.openstack.nodes.Network
      properties:
        resource_id: my_network_openstack_name


    my_security_group:
      type: cloudify.openstack.nodes.SecurityGroup
      properties:
        resource_id: my_security_group_openstack_name
        rules:
          - remote_ip_prefix: 0.0.0.0/0
            port: 8080


    my_subnet:
      type: cloudify.openstack.nodes.Subnet
      properties:
        resource_id: my_subnet_openstack_name
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                cidr: 1.2.3.0/24
                ip_version: 4
        cloudify.interfaces.validation:
          creation:
            inputs:
              args:
                cidr: 1.2.3.0/24
                ip_version: 4
      relationships:
        - target: my_network
          type: cloudify.relationships.contained_in
        - target: my_router
          type: cloudify.openstack.subnet_connected_to_router


    my_port:
      type: cloudify.openstack.nodes.Port
      properties:
        resource_id: my_port_openstack_name
      relationships:
        - target: my_network
          type: cloudify.relationships.contained_in
        - target: my_subnet
          type: cloudify.relationships.depends_on
        - target: my_security_group
          type: cloudify.openstack.port_connected_to_security_group


    my_router:
      type: cloudify.openstack.nodes.Router
      properties:
        resource_id: my_router_openstack_Name


    my_server:
      type: cloudify.openstack.nodes.Server
      properties:
        cloudify_agent:
          user: ubuntu
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                image: 8672f4c6-e33d-46f5-b6d8-ebbeba12fa02
                flavor: 101
        cloudify.interfaces.validation:
          creation:
            inputs:
              args:
                image: 8672f4c6-e33d-46f5-b6d8-ebbeba12fa02
                flavor: 101
      relationships:
        - target: my_port
          type: cloudify.openstack.server_connected_to_port


1. Creates a network. See Example I for more information.

2. Creates a security group. See Example I for more information.

3. Creates a subnet. This is again similar to what we've done in Example I. The difference here is that the subnet has an extra relationship set towards a router.

4. Creates a port, whose node name is ``my_port``, and whose name on Openstack is ``my_port_openstack_name``. The port is set with multiple relationships:

  - A relationship to the ``my_network`` node: Through this relationship, the port will be automatically placed on the ``my_network_openstack_name`` network.
  - A relationship to the ``my_subnet`` node: This relationship is strictly for ensuring the order of creation is correct, as the port requires the ``my_subnet_openstack_name`` subnet to exist before it can be created on it.
  - A relationship to the ``my_security_group`` node: This designated relationship type will take care of setting the ``my_security_group_openstack_name`` security group on the port.

5. Creates a router, whose node name is ``my_router``, and whose name on Openstack is ``my_router_openstack_name``. The router will automatically have an interface in the external network.

6. Creates a server, whose node name is ``my_server``, and whose name on Openstack is **the node's ID** (since no ``name`` parameter was supplied under the ``server`` property). The server is set with an image and flavor IDs. It also overrides the ``cloudify_agent`` property of its parent type to set the username that will be used to connect to the server for installing the Cloudify agent on it. Finally, it is set with a relationship to the ``my_port`` node: This designated relationship type will take care of connecting the server to ``my_port_openstack_name``.


Example III
-----------

This example will show how to use the ``volume`` type, as well as ``volume_attached_to_server`` relationship.

The following is an excerpt from the blueprint's ``blueprint``.``node_templates`` section::

    my_server:
      type: cloudify.openstack.nodes.Server
      properties:
        cloudify_agent:
          user: ubuntu
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                image: 8672f4c6-e33d-46f5-b6d8-ebbeba12fa02
                flavor: 101
        cloudify.interfaces.validation:
          creation:
            inputs:
              args:
                image: 8672f4c6-e33d-46f5-b6d8-ebbeba12fa02
                flavor: 101

    my_volume:
      type: cloudify.openstack.nodes.Volume
      properties:
        resource_id: my_openstack_volume_name
        device_name: /dev/vdb
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                size: 1
      relationships:
        - target: my_server
          type: cloudify.openstack.volume_attached_to_server


1. Creates a server, with name ``my_server``, and with name on Openstack **the node's ID** (since no ``name`` parameter was supplied under the ``server`` property). The server is set with an image and flavor IDs.
2. Creates a volume. It is set with a relationship to the ``my_server`` node: This designated relationship type will take care of attaching the volume to Openstack server node.



Example IV
----------

This example will show how to use a Windows server with a Cloudify agent on it.


The following is an excerpt from the blueprint's ``blueprint``.``node_templates`` section::

    my_keypair:
      type: cloudify.openstack.nodes.KeyPair
      properties:
        private_key_path: /tmp/windows-test.pem

    my_server:
      type: cloudify.openstack.nodes.WindowsServer
      relationships:
        - type: cloudify.openstack.server_connected_to_keypair
          target: keypair
      interfaces:
        cloudify.interfaces.lifecycle:
          create:
            inputs:
              args:
                server:
                  image: 8672f4c6-e33d-46f5-b6d8-ebbeba12fa02
                  flavor: 101
                  name: my-server
                  userdata: |
                    #ps1_sysnative
                    winrm quickconfig -q
                    winrm set winrm/config/winrs '@{MaxMemoryPerShellMB="300"}'
                    winrm set winrm/config '@{MaxTimeoutms="1800000"}'
                    winrm set winrm/config/service '@{AllowUnencrypted="true"}'
                    winrm set winrm/config/service/auth '@{Basic="true"}'
                    &netsh advfirewall firewall add rule name="WinRM 5985" protocol=TCP dir=in localport=5985 action=allow
                    &netsh advfirewall firewall add rule name="WinRM 5986" protocol=TCP dir=in localport=5986 action=allow

                    msiexec /i https://www.python.org/ftp/python/2.7.6/python-2.7.6.msi TARGETDIR=C:\Python27 ALLUSERS=1 /qn
        cloudify.interfaces.validation:
          creation:
            inputs:
              args:
                server:
                  image: 8672f4c6-e33d-46f5-b6d8-ebbeba12fa02
                  flavor: 101
                  name: my-server
                  userdata: |
                    #ps1_sysnative
                    winrm quickconfig -q
                    winrm set winrm/config/winrs '@{MaxMemoryPerShellMB="300"}'
                    winrm set winrm/config '@{MaxTimeoutms="1800000"}'
                    winrm set winrm/config/service '@{AllowUnencrypted="true"}'
                    winrm set winrm/config/service/auth '@{Basic="true"}'
                    &netsh advfirewall firewall add rule name="WinRM 5985" protocol=TCP dir=in localport=5985 action=allow
                    &netsh advfirewall firewall add rule name="WinRM 5986" protocol=TCP dir=in localport=5986 action=allow

                    msiexec /i https://www.python.org/ftp/python/2.7.6/python-2.7.6.msi TARGETDIR=C:\Python27 ALLUSERS=1 /qn
        cloudify.interfaces.worker_installer:
          install:
            inputs:
              cloudify_agent:
                user: Admin
                password: { get_attribute: [SELF, password] }


1. Creates a keypair. the private key will be saved under ``/tmp/windows-test.pem``.
2. Creates a Windows server:

  * It is set with a relationship to the ``my_keypair`` node, which will make the server use the it as a public key for authentication, and also use this public key to encrypt its password before posting it to the Openstack metadata service.
  * The worker-installer interface operations are given values for the user and password for the ``cloudify_agent`` input - the password uses the [get_attribute]({{< relref "blueprints/spec-intrinsic-functions.md#get-attribute" >}}) feature to retrieve the decrypted password from the Server's runtime properties (Note that in this example, only the ``install`` operation was given with this input, but all of the worker installer operations as well as the plugin installer operations should be given with it).
  * We define custom userdata which configures WinRM and installs Python on the machine (Windows Server 2012 in this example) once it's up. This is required for the Cloudify agent to be installed on the machine.



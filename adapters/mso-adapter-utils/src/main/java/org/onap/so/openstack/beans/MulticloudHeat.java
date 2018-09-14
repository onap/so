package org.onap.so.openstack.beans;

import com.woorea.openstack.base.client.OpenStackClientConnector;
import com.woorea.openstack.heat.Heat;
import com.woorea.openstack.heat.ResourcesResource;
import com.woorea.openstack.heat.StackResource;

public class MulticloudHeat extends Heat {
    private final StackResource stacks;
    private final ResourcesResource resources;

    public MulticloudHeat(String endpoint, OpenStackClientConnector connector) {
        super(endpoint, connector);
        this.stacks = new StackResource(this);
        this.resources = new ResourcesResource(this);
    }

    public MulticloudHeat(String endpoint) {
        this(endpoint, (OpenStackClientConnector)null);
    }

    public StackResource getStacks() {
        return this.stacks;
    }

    public ResourcesResource getResources() {
        return this.resources;
    }
}

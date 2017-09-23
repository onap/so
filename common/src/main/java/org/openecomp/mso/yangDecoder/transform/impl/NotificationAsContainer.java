package org.openecomp.mso.yangDecoder.transform.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

final class NotificationAsContainer implements ContainerSchemaNode {
    private final NotificationDefinition delegate;

    public String getDescription() {
        return this.delegate.getDescription();
    }

    public String getReference() {
        return this.delegate.getReference();
    }

    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return this.delegate.getTypeDefinitions();
    }

    public Set<GroupingDefinition> getGroupings() {
        return this.delegate.getGroupings();
    }

    public Status getStatus() {
        return this.delegate.getStatus();
    }

    public ContainerSchemaNode getInput() {
        return null;
    }

    public ContainerSchemaNode getOutput() {
        return null;
    }

    NotificationAsContainer(NotificationDefinition parentNode) {
        this.delegate = parentNode;
    }

    public QName getQName() {
        return this.delegate.getQName();
    }

    public SchemaPath getPath() {
        return this.delegate.getPath();
    }

    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    public DataSchemaNode getDataChildByName(QName name) {
        return this.getDataChildByName(name.getLocalName());
    }

    public DataSchemaNode getDataChildByName(String name) {
        byte var3 = -1;
        switch(name.hashCode()) {
            case -1005512447:
                if(name.equals("output")) {
                    var3 = 1;
                }
                break;
            case 100358090:
                if(name.equals("input")) {
                    var3 = 0;
                }
        }

        switch(var3) {
            case 0:
                return null;
            case 1:
                return null;
            default:
                return null;
        }
    }

    public Set<UsesNode> getUses() {
        return Collections.emptySet();
    }

    public Set<AugmentationSchema> getAvailableAugmentations() {
        return Collections.emptySet();
    }

    public boolean isPresenceContainer() {
        return false;
    }

    public Collection<DataSchemaNode> getChildNodes() {
        ArrayList ret = new ArrayList();
        ret.addAll(this.delegate.getChildNodes());
        return ret;
    }

    public boolean isAugmenting() {
        return false;
    }

    public boolean isAddedByUses() {
        return false;
    }

    public boolean isConfiguration() {
        return false;
    }

    public ConstraintDefinition getConstraints() {
        return null;
    }
}

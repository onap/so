/*-
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.de
 */

package org.onap.so.heatbridge.aai.factory;

import java.util.Map;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.PhysicalLink;
import org.onap.aai.domain.yang.RelatedToProperty;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;

/**
 * Create AAI objects.
 */
public class AaiObjectBuilder {

    /**
     * Builds the physical link
     * @param linkName - The link name.
     * @return PhysicalLink - Physical Link object.
     */
    public PhysicalLink buildPhysicalLink(String linkName) {
        // At the moment I am setting only required field to create physical link.
        PhysicalLink physicalLink = new PhysicalLink();
        physicalLink.setLinkName(linkName);
        return physicalLink;
    }

    /**
     * Build the Physical interface.
     * @param interfaceName - interface name.
     * @param mode - maintenance mode.
     * @param address - MAC address.
     * @return pinterface
     */
    public PInterface buildPInterface(final String interfaceName, final boolean mode, final String address) {
        PInterface pInterface = new PInterface();
        pInterface.setInterfaceName(interfaceName);
        pInterface.setMacAddresss(address);
        pInterface.setInMaint(mode); // To indicate the object is in maintenance mode default value is false.
        return pInterface;
    }

    /**
     * Build the Relationship.
     *
     * @param relationshipKey   - Refers to the type of entity the relationship is 'to'.
     * @param relatedToValue - Refers to the name/id of the entity the relationship is 'to'.
     *
     * @return Relationship - Relationship object.
     */
    public Relationship buildRelationship(String relatedTo, String relationshipKey, String relatedToValue) {
        final Relationship relationship = new Relationship();
        final RelationshipData relationshipData = buildRelationshipData(relationshipKey, relatedToValue);
        relationship.setRelatedTo(relatedTo);
        relationship.getRelationshipData().add(relationshipData);
        return relationship;
    }

    /**
     * Build the Relationship with multiple relationship key values
     * @return Relationship - Relationship object.
     */
    public Relationship buildRelationship(String relatedTo, Map<String, String> relationshipKeyValues) {
        final Relationship relationship = new Relationship();

        relationshipKeyValues.forEach((key, value) -> {
            final RelationshipData relationshipData = buildRelationshipData(key, value);
            relationship.getRelationshipData().add(relationshipData);
        });

        relationship.setRelatedTo(relatedTo);
        return relationship;
    }

    /**
     * Build the RelationshipData.
     *
     * @param relationshipKey - Refers to the type of entity the relationship is 'to'.
     * @param relationshipValue - Refers to the name/id of the entity the relationship is 'to'.
     *
     * @return RelationshipData - Relationship data object.
     */
    public RelationshipData buildRelationshipData(String relationshipKey, String relationshipValue) {
        final RelationshipData relationshipData = new RelationshipData();
        relationshipData.setRelationshipKey(relationshipKey);
        relationshipData.setRelationshipValue(relationshipValue);
        return relationshipData;
    }

    public RelatedToProperty buildRelatedToProperty(String propertyKey, String propertyValue) {
        final RelatedToProperty relatedToProperty = new RelatedToProperty();
        relatedToProperty.setPropertyKey(propertyKey);
        relatedToProperty.setPropertyValue(propertyValue);
        return relatedToProperty;
    }
}

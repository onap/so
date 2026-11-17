/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
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
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.asdc.installer.heat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Map;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.installer.PnfResourceStructure;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.data.repository.PnfCustomizationRepository;
import org.onap.so.db.catalog.data.repository.PnfResourceRepository;

public class PnfResourceInstallerTest {

    private PnfResourceRepository pnfResourceRepository;
    private PnfCustomizationRepository pnfCustomizationRepository;
    private PnfResourceInstaller installer;

    @Before
    public void setUp() {
        pnfResourceRepository = mock(PnfResourceRepository.class);
        pnfCustomizationRepository = mock(PnfCustomizationRepository.class);
        installer = new PnfResourceInstaller(pnfResourceRepository, pnfCustomizationRepository);
    }

    private static Property prop(Object value) {
        Property p = mock(Property.class);
        when(p.getValue()).thenReturn(value);
        return p;
    }

    @Test
    public void defaultSoftwareVersionWinsOverSoftwareVersions() {
        assertEquals("2.0", installer.extractDefaultSoftwareVersionFromSwVersions(
                Map.of("default_software_version", prop("2.0"), "software_versions", prop(List.of("1.0")))));
    }

    @Test
    public void firstSoftwareVersionIsUsedWhenNoDefault() {
        assertEquals("1.0", installer
                .extractDefaultSoftwareVersionFromSwVersions(Map.of("software_versions", prop(List.of("1.0", "1.1")))));
    }

    @Test
    public void scalarSoftwareVersionsIsUsedAsIs() {
        assertEquals("3.1",
                installer.extractDefaultSoftwareVersionFromSwVersions(Map.of("software_versions", prop("3.1"))));
    }

    @Test
    public void noVersionYieldsEmptyString() {
        assertEquals("", installer.extractDefaultSoftwareVersionFromSwVersions(Map.of()));
    }

    @Test
    public void missingSkipPostInstConfDefaultsToTrue() {
        assertTrue(installer.getBooleanValue(null));
    }

    @Test
    public void repositoryExceptionsPropagateUnwrapped() throws Exception {
        ConstraintViolationException cve = new ConstraintViolationException("dup", null, "PK");
        ToscaResourceStructure tosca = mock(ToscaResourceStructure.class);
        ISdcCsarHelper csar = mock(ISdcCsarHelper.class);
        when(tosca.getSdcCsarHelper()).thenReturn(csar);
        PnfResourceStructure pnf = mock(PnfResourceStructure.class, Answers.RETURNS_DEEP_STUBS);
        when(pnf.getResourceInstance().getResourceCustomizationUUID()).thenReturn("cust-uuid");
        IEntityDetails entity = mock(IEntityDetails.class);
        Metadata metadata = mock(Metadata.class);
        when(entity.getMetadata()).thenReturn(metadata);
        when(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)).thenReturn("cust-uuid");
        when(csar.getEntity(any(), any(), anyBoolean())).thenReturn(List.of(entity));
        when(pnfCustomizationRepository.findById("cust-uuid")).thenThrow(cve);
        try {
            installer.processPnfResources(tosca, new Service(), pnf);
            fail("expected exception");
        } catch (ConstraintViolationException e) {
            assertSame(cve, e);
        }
    }
}

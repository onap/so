/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.db.catalog.test;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.VfModule;

/**
 */

public class VfModuleTest {

	@Test
	public final void vfModuleDataTest() {
		VfModule vfModule = new VfModule();
		vfModule.setCreated(new Timestamp(System.currentTimeMillis()));
		assertTrue(vfModule.getCreated() != null);
		vfModule.setDescription("description");
		assertTrue(vfModule.getDescription().equalsIgnoreCase("description"));

		vfModule.setModelInvariantUUID("action");
		assertTrue(vfModule.getModelInvariantUUID().equalsIgnoreCase("action"));

		vfModule.setModelName("modelName");
		assertTrue(vfModule.getModelName().equalsIgnoreCase("modelName"));

		vfModule.setModelUUID("modelUUID");
		assertTrue(vfModule.getModelUUID().equalsIgnoreCase("modelUUID"));
		vfModule.setModelVersion("modelVersion");
		assertTrue(vfModule.getModelVersion().equalsIgnoreCase("modelVersion"));
		vfModule.setHeatTemplateArtifactUUId("heatTemplateArtifactUUId");
		assertTrue(vfModule.getHeatTemplateArtifactUUId().equalsIgnoreCase("heatTemplateArtifactUUId"));
		vfModule.setVnfResourceModelUUId("vnfResourceModelUUId");
		assertTrue(vfModule.getVnfResourceModelUUId().equalsIgnoreCase("vnfResourceModelUUId"));
		vfModule.setIsBase(1);
		assertTrue(vfModule.isBase());
//		assertTrue(vfModule.toString() == null);

	}

}

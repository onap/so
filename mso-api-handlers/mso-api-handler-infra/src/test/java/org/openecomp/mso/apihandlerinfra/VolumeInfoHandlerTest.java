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

package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.assertTrue;

import mockit.Mock;
import mockit.MockUp;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.apihandler.common.CamundaClient;
import org.openecomp.mso.apihandlerinfra.volumebeans.ActionType;
import org.openecomp.mso.apihandlerinfra.volumebeans.RequestStatusType;
import org.openecomp.mso.apihandlerinfra.volumebeans.VolumeRequest;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.NetworkRecipe;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.InfraRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class VolumeInfoHandlerTest {

	VolumeInfoHandler handler = new VolumeInfoHandler();

	private static MockUp<RequestsDatabase> mockRDB;

	@BeforeClass
	public static void setUp() throws Exception {
		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		msoPropertiesFactory.removeAllMsoProperties();
		msoPropertiesFactory.initializeMsoProperties(Constants.MSO_PROP_APIHANDLER_INFRA, "src/test/resources/mso.apihandler-infra.properties");

		mockRDB = new MockUp<RequestsDatabase>() {
			@Mock
			public List<InfraActiveRequests> getRequestListFromInfraActive (String queryAttributeName,
																			String queryValue,
																			String requestType) {
				final InfraActiveRequests requests = new InfraActiveRequests();
				requests.setAction(ActionType.CREATE.name());
				requests.setRequestStatus(RequestStatusType.IN_PROGRESS.name());
				requests.setStartTime(Timestamp.valueOf(LocalDateTime.now()));
				return Collections.singletonList(requests);
			}
			@Mock
			public InfraActiveRequests getRequestFromInfraActive (String requestId, String requestType) {
				final InfraActiveRequests requests = new InfraActiveRequests();
				requests.setAction(ActionType.CREATE.name());
				requests.setRequestStatus(RequestStatusType.IN_PROGRESS.name());
				requests.setStartTime(Timestamp.valueOf(LocalDateTime.now()));
				return requests;
			}
		};

	}

	@AfterClass
	public static void tearDown() {
		mockRDB.tearDown();
	}

	@Test
	public void fillVnfRequestTestV3(){
		VolumeRequest qr = new VolumeRequest();
		InfraRequests ar = new InfraRequests();
		ar.setVnfId("2990102");
		ar.setVnfParams("test");
		handler.fillVolumeRequest(qr, ar, "v3");
		String vnfid = (String)qr.getVolumeParams();
		assertTrue(vnfid.equals("test"));
	}
	
	@Test
	public void fillVnfRequestTestV2(){
		VolumeRequest qr = new VolumeRequest();
		InfraRequests ar = new InfraRequests();
		ar.setVnfId("2990102");
		ar.setVnfParams("test");
		handler.fillVolumeRequest(qr, ar, "v2");
		String vnfid = (String)qr.getVolumeParams();
		assertTrue(vnfid.equals("test"));
	}

	@Test
	public void queryFilters() {
		final Response response = handler.queryFilters("vnf-type", "svc-type", "aicNode", "tenant-id",
				"vg-id", "vg-name", "v3");
	}

	@Test
	public void queryFilters2() {
		final Response response = handler.queryFilters(null, "svc-type", "aicNode", "tenant-id",
				"vg-id", "vg-name", "v3");
	}

	@Test
	public void getRequest() {
		final Response response = handler.getRequest("request-id", "v3");
	}
}

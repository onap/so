package org.openecomp.mso.client.aai;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class AAITransactionalClientTest {

	
	private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/aai/bulkprocess/";
	
	@Test
	public void run() throws IOException {
		
		
		AAIResourcesClient client = new AAIResourcesClient();
		AAIResourceUri uriA = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test1");
		AAIResourceUri uriB = AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, "test2");
		AAIResourceUri uriC = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test3");
		AAIResourceUri uriD = AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, "test4");
		AAIResourceUri uriE = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test5");
		AAIResourceUri uriF = AAIUriFactory.createResourceUri(AAIObjectType.PSERVER, "test6");
		
		AAIResourceUri uriAClone = uriA.clone();
		AAITransactionalClient transactions = client
				.beginTransaction().connect(uriA, uriB).connect(uriC, uriD)
				.beginNewTransaction().connect(uriE, uriF);
		ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		String serializedTransactions = mapper.writeValueAsString(transactions.getTransactions());
		Map<String, Object> map1 = mapper.readValue(serializedTransactions, new TypeReference<Map<String, Object>>(){});
		Map<String, Object> map2 = mapper.readValue(getJson("test-request.json"), new TypeReference<Map<String, Object>>(){});
		assertEquals("payloads are equal", map2, map1);
		assertEquals("uri not manipulated", uriAClone.build().toString(), uriA.build().toString());
	}
	
	@Test
	public void verifyResponse() throws IOException {
		AAIResourcesClient client = new AAIResourcesClient();
		AAITransactionalClient transactions = client
				.beginTransaction();
		assertEquals("success status", Optional.empty(), transactions.locateErrorMessages(getJson("response-success.json")));
		assertEquals(transactions.locateErrorMessages(getJson("response-failure.json")).get(), "another error message\nmy great error");

		
	}
	
	private String getJson(String filename) throws IOException {
		 return new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + filename)));
	}
}

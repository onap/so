package org.openecomp.mso.client.aai.entities;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

public class RelationshipsTest {

	private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/aai/resources/";

	@Test
	public void run() throws IOException {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "e2e-complex.json")));
		
		AAIResultWrapper wrapper = new AAIResultWrapper(content);
		Relationships relationships = wrapper.getRelationships().get();
		
		List<AAIResourceUri> test = relationships.getRelatedAAIUris(AAIObjectType.VCE);
		
		System.out.println(test.get(0).build());
		List<AAIResourceUri> uris = Arrays.asList(
				AAIUriFactory.createResourceUri(AAIObjectType.VCE, "a9fec18e-1ea3-40e4-a6c0-a89b3de07053"),
				AAIUriFactory.createResourceUri(AAIObjectType.VCE, "8ae1e5f8-61f1-4c71-913a-b40cc4593cb9"),
				AAIUriFactory.createResourceUri(AAIObjectType.VCE, "a2935fa9-b743-49f4-9813-a127f13c4e93"),
				AAIUriFactory.createResourceUri(AAIObjectType.VCE, "c7fe7698-8063-4e26-8bd3-ca3edde0b0d4"));
		
		
		assertTrue(uris.containsAll(test) && test.containsAll(uris));
		
	}
	
	@Test
	public void runGenericVnfTest() throws IOException {
		final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "e2e-generic-vnf.json")));
		
		AAIResultWrapper wrapper = new AAIResultWrapper(content);
		Relationships relationships = wrapper.getRelationships().get();
		
		List<AAIResourceUri> test = relationships.getRelatedAAIUris(AAIObjectType.VSERVER);
		
		System.out.println(test.get(0).build());
		System.out.println(test.get(0).getURIKeys());
		
		
	}
}

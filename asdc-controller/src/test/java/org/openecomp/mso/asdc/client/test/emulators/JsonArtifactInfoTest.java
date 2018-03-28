package org.openecomp.mso.asdc.client.test.emulators;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class JsonArtifactInfoTest {
    JsonArtifactInfo jsonArtifactInfo = new JsonArtifactInfo();

    List<JsonArtifactInfo> artifactList = new ArrayList<>();

    @Test
    public final void addArtifactToUUIDMap()
    {
        jsonArtifactInfo.addArtifactToUUIDMap(artifactList);
    }

    @Test
    public final void setAttribute()
    {
        jsonArtifactInfo.setAttribute("artifactName", "test");
    }


    @Test
    public final void getArtifactDescription()
    {
        final String artifactDescription = jsonArtifactInfo.getArtifactDescription();
        final String artifactName = jsonArtifactInfo.getArtifactName();
        final String artifactChecksumfinal = jsonArtifactInfo.getArtifactChecksum();
        final String artifactChecksum = jsonArtifactInfo.getArtifactChecksum();
        final Integer artifactTimeout = jsonArtifactInfo.getArtifactTimeout();
        final String artifactType =  jsonArtifactInfo.getArtifactType();
        final String artifactURL = jsonArtifactInfo.getArtifactURL();
        final String artifactUUID = jsonArtifactInfo.getArtifactUUID();
        final String artifactVersion = jsonArtifactInfo.getArtifactVersion();
        jsonArtifactInfo.getGeneratedArtifact();
        jsonArtifactInfo.getRelatedArtifacts();

    }
}

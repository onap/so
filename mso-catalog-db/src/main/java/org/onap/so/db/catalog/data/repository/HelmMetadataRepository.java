package org.onap.so.db.catalog.data.repository;

import org.onap.so.db.catalog.beans.HelmMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "helm_metadata", path = "helm_metadata")
public interface HelmMetadataRepository extends JpaRepository<HelmMetadata, String> {


    public HelmMetadata findByArtifactUuid(String artifactUUId);
}

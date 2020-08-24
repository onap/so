package org.onap.so.db.catalog.data.repository;

import org.onap.so.db.catalog.beans.CnfResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "helm_metadata", path = "helm_metadata")
public interface CnfResourceRepository extends JpaRepository<CnfResource, String> {


    public CnfResource findByArtifactUuid(String artifactUUId);
}

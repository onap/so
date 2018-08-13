package org.onap.so.db.catalog.data.repository;

import org.onap.so.db.catalog.beans.CloudIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "cloudIdentity", path = "cloudIdentity")
public interface CloudIdentityRepository extends JpaRepository<CloudIdentity, String> {

}

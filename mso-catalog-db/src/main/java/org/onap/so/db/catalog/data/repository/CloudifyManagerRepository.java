package org.onap.so.db.catalog.data.repository;

import org.onap.so.db.catalog.beans.CloudifyManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "cloudifyManager", path = "cloudifyManager")
public interface CloudifyManagerRepository extends JpaRepository<CloudifyManager, String> {

}

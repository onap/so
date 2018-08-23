package org.onap.so.db.catalog.data.repository;

import org.onap.so.db.catalog.beans.CloudSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.transaction.Transactional;

@RepositoryRestResource(collectionResourceRel = "cloudSite", path = "cloudSite")
@Transactional
public interface CloudSiteRepository extends JpaRepository<CloudSite, String> {

    CloudSite findByClliAndCloudVersion(@Param("CLLI") String clli,@Param("CLOUD_VERSION") String cloudVersion);
}

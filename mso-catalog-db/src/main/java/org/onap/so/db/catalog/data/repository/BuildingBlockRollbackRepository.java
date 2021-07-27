package org.onap.so.db.catalog.data.repository;

import javax.transaction.Transactional;
import org.onap.so.db.catalog.beans.BuildingBlockRollback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "buildingBlockRollback", path = "buildingBlockRollback")
public interface BuildingBlockRollbackRepository extends JpaRepository<BuildingBlockRollback, String> {
    // only needed for findlAll()
}

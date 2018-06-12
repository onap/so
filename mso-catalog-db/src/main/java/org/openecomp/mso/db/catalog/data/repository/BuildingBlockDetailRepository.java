package org.openecomp.mso.db.catalog.data.repository;

import org.openecomp.mso.db.catalog.beans.BuildingBlockDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "buildingBlockDetail", path = "buildingBlockDetail")
public interface BuildingBlockDetailRepository extends JpaRepository<BuildingBlockDetail, String> {
	BuildingBlockDetail findOneByBuildingBlockName(@Param("buildingBlockName") String buildingBlockName);
}

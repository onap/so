package org.openecomp.mso.db.catalog.data.repository;

import java.util.List;

import org.openecomp.mso.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "collectionResourceInstanceGroupCustomization", path = "collectionResourceInstanceGroupCustomization")
public interface CollectionResourceInstanceGroupCustomizationRepository
		extends JpaRepository<CollectionResourceInstanceGroupCustomization, String> {

	List<CollectionResourceInstanceGroupCustomization> findByModelCustomizationUUID(String modelCustomizationlUUID);
}

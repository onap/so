package org.openecomp.mso.db.catalog.data.repository;

import java.util.List;

import org.openecomp.mso.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "vnfcInstanceGroupCustomization", path = "vnfcInstanceGroupCustomization")
public interface VnfcInstanceGroupCustomizationRepository
		extends JpaRepository<VnfcInstanceGroupCustomization, String> {
	List<VnfcInstanceGroupCustomization> findByModelCustomizationUUID(String modelCustomizationlUUID);
}
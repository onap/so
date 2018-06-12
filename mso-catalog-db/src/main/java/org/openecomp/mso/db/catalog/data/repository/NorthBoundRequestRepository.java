package org.openecomp.mso.db.catalog.data.repository;

import org.openecomp.mso.db.catalog.beans.macro.NorthBoundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "northbound_request_ref_lookup", path = "northbound_request_ref_lookup")
public interface NorthBoundRequestRepository extends JpaRepository<NorthBoundRequest, Integer> {
	NorthBoundRequest findOneByActionAndRequestScopeAndIsAlacarte(@Param("ACTION") String action,
			@Param("REQUEST_SCOPE") String requestScope, @Param("IS_ALACARTE") Boolean isALaCarte);
}
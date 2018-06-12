package org.openecomp.mso.db.catalog.data.repository;

import org.openecomp.mso.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "rainy_day_handler_macro", path = "rainy_day_handler_macro")
public interface RainyDayHandlerStatusRepository extends JpaRepository<RainyDayHandlerStatus, Integer> {
	RainyDayHandlerStatus findOneByFlowNameAndServiceTypeAndVnfTypeAndErrorCodeAndWorkStep(
			@Param("FLOW_NAME") String flowName, @Param("SERVICE_TYPE") String serviceType,
			@Param("VNF_TYPE") String vnfType, @Param("ERROR_CODE") String errorCode,
			@Param("WORK_STEP") String workStep);
}
package org.openecomp.mso.db.catalog.data.repository;

import org.openecomp.mso.db.catalog.beans.OrchestrationAction;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatusStateTransitionDirective;
import org.openecomp.mso.db.catalog.beans.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "orchestrationStatusStateTransitionDirective", path = "orchestrationStatusStateTransitionDirective")
public interface OrchestrationStatusStateTransitionDirectiveRepository extends JpaRepository<OrchestrationStatusStateTransitionDirective, String> {
	OrchestrationStatusStateTransitionDirective findOneByResourceTypeAndOrchestrationStatusAndTargetAction(@Param("resourceType") ResourceType resourceType, @Param("orchestrationStatus") OrchestrationStatus orchestrationStatus, @Param("targetAction") OrchestrationAction targetAction);
}

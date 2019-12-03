package org.onap.so.db.catalog.data.repository;

import org.onap.so.db.catalog.beans.BBNameSelectionReference ;

@RepositoryRestResource(collectionResourceRel = "bbNameSelectionReference ", path = "bbNameSelectionReference ")
public interface BBNameSelectionReferenceRepository extends JpaRepository<BBNameSelectionReference, String> {

    public BBNameSelectionReference  findBBNameSelectionReferenceByActorAndScopeAndAction(
            @Param("ACTOR") String actor, @Param("SCOPE") String scope, @Param("ACTION") String action);
}

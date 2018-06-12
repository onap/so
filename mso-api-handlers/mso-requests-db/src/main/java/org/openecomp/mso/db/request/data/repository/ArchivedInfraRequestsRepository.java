package org.openecomp.mso.db.request.data.repository;

import org.openecomp.mso.db.request.beans.ArchivedInfraRequests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchivedInfraRequestsRepository extends JpaRepository<ArchivedInfraRequests, String> {	

}

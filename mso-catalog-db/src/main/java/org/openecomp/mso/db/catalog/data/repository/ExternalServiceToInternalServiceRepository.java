package org.openecomp.mso.db.catalog.data.repository;

import org.openecomp.mso.db.catalog.beans.ExternalServiceToInternalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "externalServiceToInternalService", path = "externalServiceToInternalService")
public interface ExternalServiceToInternalServiceRepository
		extends JpaRepository<ExternalServiceToInternalService, Integer> {
	ExternalServiceToInternalService findByServiceName(String serviceName);
	ExternalServiceToInternalService findByServiceNameAndSubscriptionServiceType(String serviceName , String subscriptionServiceType);
}

/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.db.request.data.repository;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional(readOnly = true)
public class InfraActiveRequestsRepositoryImpl implements InfraActiveRequestsRepositoryCustom {


    @Qualifier("requestEntityManagerFactory")
    @Autowired
    private EntityManager entityManager;

    protected static MsoLogger msoLogger =
            MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL, InfraActiveRequestsRepositoryImpl.class);

    protected static final String REQUEST_STATUS = "requestStatus";
    protected static final String SOURCE = "source";
    protected static final String START_TIME = "startTime";
    protected static final String END_TIME = "endTime";
    protected static final String REQUEST_TYPE = "requestType";
    protected static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    protected static final String SERVICE_INSTANCE_NAME = "serviceInstanceName";
    protected static final String VNF_INSTANCE_NAME = "vnfName";
    protected static final String VNF_INSTANCE_ID = "vnfId";
    protected static final String VOLUME_GROUP_INSTANCE_NAME = "volumeGroupName";
    protected static final String VOLUME_GROUP_INSTANCE_ID = "volumeGroupId";
    protected static final String VFMODULE_INSTANCE_NAME = "vfModuleName";
    protected static final String VFMODULE_INSTANCE_ID = "vfModuleId";
    protected static final String NETWORK_INSTANCE_NAME = "networkName";
    protected static final String CONFIGURATION_INSTANCE_ID = "configurationId";
    protected static final String CONFIGURATION_INSTANCE_NAME = "configurationName";
    protected static final String OPERATIONAL_ENV_ID = "operationalEnvId";
    protected static final String OPERATIONAL_ENV_NAME = "operationalEnvName";
    protected static final String NETWORK_INSTANCE_ID = "networkId";
    protected static final String GLOBAL_SUBSCRIBER_ID = "globalSubscriberId";
    protected static final String SERVICE_NAME_VERSION_ID = "serviceNameVersionId";
    protected static final String SERVICE_ID = "serviceId";
    protected static final String SERVICE_VERSION = "serviceVersion";
    protected static final String REQUEST_ID = "requestId";
    protected static final String REQUESTOR_ID = "requestorId";
    protected static final String ACTION = "action";

    private static final List<String> VALID_COLUMNS =
            Arrays.asList(REQUEST_ID, SERVICE_INSTANCE_ID, SERVICE_INSTANCE_NAME, ACTION, REQUEST_STATUS);


    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#healthCheck()
     */
    @Override
    public boolean healthCheck() {

        final Query query = entityManager.createNativeQuery(" show tables ");

        final List<?> list = query.getResultList();

        return true;
    }

    private List<InfraActiveRequests> executeInfraQuery(final CriteriaQuery<InfraActiveRequests> crit,
            final List<Predicate> predicates, final Order order) {

        final long startTime = System.currentTimeMillis();
        msoLogger.debug("Execute query on infra active request table");

        List<InfraActiveRequests> results = new ArrayList<InfraActiveRequests>();

        try {
            final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            crit.where(cb.and(predicates.toArray(new Predicate[0])));
            crit.orderBy(order);
            results = entityManager.createQuery(crit).getResultList();

        } finally {
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "getInfraActiveRequest", null);
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#getRequestFromInfraActive(java.
     * lang.String)
     */
    @Override
    public InfraActiveRequests getRequestFromInfraActive(final String requestId) {
        final long startTime = System.currentTimeMillis();
        msoLogger.debug("Get request " + requestId + " from InfraActiveRequests DB");

        InfraActiveRequests ar = null;
        try {
            final Query query = entityManager.createQuery(
                    "from InfraActiveRequests where requestId = :requestId OR clientRequestId = :requestId");
            query.setParameter(REQUEST_ID, requestId);
            ar = this.getSingleResult(query);
        } finally {

            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "InfraRequestDB", "getRequestFromInfraActive", null);
        }
        return ar;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#checkInstanceNameDuplicate(java.
     * util.HashMap, java.lang.String, java.lang.String)
     */
    @Override
    public InfraActiveRequests checkInstanceNameDuplicate(final HashMap<String, String> instanceIdMap,
            final String instanceName, final String requestScope) {

        final List<Predicate> predicates = new LinkedList<>();
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<InfraActiveRequests> crit = cb.createQuery(InfraActiveRequests.class);
        final Root<InfraActiveRequests> tableRoot = crit.from(InfraActiveRequests.class);
        InfraActiveRequests infraActiveRequests = null;

        if (instanceName != null && !instanceName.equals("")) {

            if ("service".equals(requestScope)) {
                predicates.add(cb.equal(tableRoot.get(SERVICE_INSTANCE_NAME), instanceName));
            } else if ("vnf".equals(requestScope)) {
                predicates.add(cb.equal(tableRoot.get(VNF_INSTANCE_NAME), instanceName));
            } else if ("volumeGroup".equals(requestScope)) {
                predicates.add(cb.equal(tableRoot.get(VOLUME_GROUP_INSTANCE_NAME), instanceName));
            } else if ("vfModule".equals(requestScope)) {
                predicates.add(cb.equal(tableRoot.get(VFMODULE_INSTANCE_NAME), instanceName));
            } else if ("network".equals(requestScope)) {
                predicates.add(cb.equal(tableRoot.get(NETWORK_INSTANCE_NAME), instanceName));
            } else if (requestScope.equals("configuration")) {
                predicates.add(cb.equal(tableRoot.get(CONFIGURATION_INSTANCE_NAME), instanceName));
            } else if (requestScope.equals("operationalEnvironment")) {
                predicates.add(cb.equal(tableRoot.get(OPERATIONAL_ENV_NAME), instanceName));
            }

        } else {
            if (instanceIdMap != null) {
                if ("service".equals(requestScope) && instanceIdMap.get("serviceInstanceId") != null) {
                    predicates
                            .add(cb.equal(tableRoot.get(SERVICE_INSTANCE_ID), instanceIdMap.get("serviceInstanceId")));
                }

                if ("vnf".equals(requestScope) && instanceIdMap.get("vnfInstanceId") != null) {
                    predicates.add(cb.equal(tableRoot.get(VNF_INSTANCE_ID), instanceIdMap.get("vnfInstanceId")));
                }

                if ("vfModule".equals(requestScope) && instanceIdMap.get("vfModuleInstanceId") != null) {
                    predicates.add(
                            cb.equal(tableRoot.get(VFMODULE_INSTANCE_ID), instanceIdMap.get("vfModuleInstanceId")));
                }

                if ("volumeGroup".equals(requestScope) && instanceIdMap.get("volumeGroupInstanceId") != null) {
                    predicates.add(cb.equal(tableRoot.get(VOLUME_GROUP_INSTANCE_ID),
                            instanceIdMap.get("volumeGroupInstanceId")));
                }

                if ("network".equals(requestScope) && instanceIdMap.get("networkInstanceId") != null) {
                    predicates
                            .add(cb.equal(tableRoot.get(NETWORK_INSTANCE_ID), instanceIdMap.get("networkInstanceId")));
                }

                if (requestScope.equals("configuration") && instanceIdMap.get("configurationInstanceId") != null) {
                    predicates.add(cb.equal(tableRoot.get(CONFIGURATION_INSTANCE_ID),
                            instanceIdMap.get("configurationInstanceId")));
                }

                if (requestScope.equals("operationalEnvironment")
                        && instanceIdMap.get("operationalEnvironmentId") != null) {
                    predicates.add(
                            cb.equal(tableRoot.get(OPERATIONAL_ENV_ID), instanceIdMap.get("operationalEnvironmentId")));
                }
            }
        }
        if(!predicates.isEmpty()){
        	predicates.add(tableRoot.get(REQUEST_STATUS)
                .in(Arrays.asList("PENDING", "IN_PROGRESS", "TIMEOUT", "PENDING_MANUAL_TASK")));

	        final Order order = cb.desc(tableRoot.get(START_TIME));
	
	        final List<InfraActiveRequests> dupList = executeInfraQuery(crit, predicates, order);
	
	        if (dupList != null && !dupList.isEmpty()) {
	            infraActiveRequests = dupList.get(0);
	        }
        }

        return infraActiveRequests;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#
     * getOrchestrationFiltersFromInfraActive(java.util.Map)
     */
    @Override
    public List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive(
            final Map<String, List<String>> orchestrationMap) {


        final List<Predicate> predicates = new LinkedList<>();
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<InfraActiveRequests> crit = cb.createQuery(InfraActiveRequests.class);
        final Root<InfraActiveRequests> tableRoot = crit.from(InfraActiveRequests.class);
        for (final Map.Entry<String, List<String>> entry : orchestrationMap.entrySet()) {
            String mapKey = entry.getKey();
            if ("serviceInstanceId".equalsIgnoreCase(mapKey)) {
                mapKey = "serviceInstanceId";
            } else if ("serviceInstanceName".equalsIgnoreCase(mapKey)) {
                mapKey = "serviceInstanceName";
            } else if ("vnfInstanceId".equalsIgnoreCase(mapKey)) {
                mapKey = "vnfId";
            } else if ("vnfInstanceName".equalsIgnoreCase(mapKey)) {
                mapKey = "vnfName";
            } else if ("vfModuleInstanceId".equalsIgnoreCase(mapKey)) {
                mapKey = "vfModuleId";
            } else if ("vfModuleInstanceName".equalsIgnoreCase(mapKey)) {
                mapKey = "vfModuleName";
            } else if ("volumeGroupInstanceId".equalsIgnoreCase(mapKey)) {
                mapKey = "volumeGroupId";
            } else if ("volumeGroupInstanceName".equalsIgnoreCase(mapKey)) {
                mapKey = "volumeGroupName";
            } else if ("networkInstanceId".equalsIgnoreCase(mapKey)) {
                mapKey = "networkId";
            } else if ("networkInstanceName".equalsIgnoreCase(mapKey)) {
                mapKey = "networkName";
            } else if (mapKey.equalsIgnoreCase("configurationInstanceId")) {
                mapKey = "configurationId";
            } else if (mapKey.equalsIgnoreCase("configurationInstanceName")) {
                mapKey = "configurationName";
            } else if ("lcpCloudRegionId".equalsIgnoreCase(mapKey)) {
                mapKey = "aicCloudRegion";
            } else if ("tenantId".equalsIgnoreCase(mapKey)) {
                mapKey = "tenantId";
            } else if ("modelType".equalsIgnoreCase(mapKey)) {
                mapKey = "requestScope";
            } else if ("requestorId".equalsIgnoreCase(mapKey)) {
                mapKey = "requestorId";
            } else if ("requestExecutionDate".equalsIgnoreCase(mapKey)) {
                mapKey = "startTime";
            }

            final String propertyValue = entry.getValue().get(1);
            if ("startTime".equals(mapKey)) {
                final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
                try {
                    final Date thisDate = format.parse(propertyValue);
                    final Timestamp minTime = new Timestamp(thisDate.getTime());
                    final Timestamp maxTime = new Timestamp(thisDate.getTime() + TimeUnit.DAYS.toMillis(1));

                    if ("DOES_NOT_EQUAL".equalsIgnoreCase(entry.getValue().get(0))) {
                        predicates.add(cb.or(cb.lessThan(tableRoot.get(mapKey), minTime),
                                cb.greaterThanOrEqualTo(tableRoot.get(mapKey), maxTime)));
                    } else {
                        predicates.add(cb.between(tableRoot.get(mapKey), minTime, maxTime));
                    }
                } catch (final Exception e) {
                    msoLogger.debug("Exception in getOrchestrationFiltersFromInfraActive(): + " + e.getMessage(), e);
                    return null;
                }
            } else if ("DOES_NOT_EQUAL".equalsIgnoreCase(entry.getValue().get(0))) {
                predicates.add(cb.notEqual(tableRoot.get(mapKey), propertyValue));
            } else {
                predicates.add(cb.equal(tableRoot.get(mapKey), propertyValue));
            }

        }

        final Order order = cb.asc(tableRoot.get(START_TIME));

        return executeInfraQuery(crit, predicates, order);
    }

    // Added this method for Tenant Isolation project ( 1802-295491a) to query the mso_requests DB
    // (infra_active_requests table) for operationalEnvId and OperationalEnvName
    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#
     * getCloudOrchestrationFiltersFromInfraActive(java.util.Map)
     */
    @Override
    public List<InfraActiveRequests> getCloudOrchestrationFiltersFromInfraActive(
            final Map<String, String> orchestrationMap) {
        final List<Predicate> predicates = new LinkedList<>();
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<InfraActiveRequests> crit = cb.createQuery(InfraActiveRequests.class);
        final Root<InfraActiveRequests> tableRoot = crit.from(InfraActiveRequests.class);

        // Add criteria on OperationalEnvironment RequestScope when requestorId is only specified in
        // the filter
        // as the same requestorId can also match on different API methods
        final String resourceType = orchestrationMap.get("resourceType");
        if (resourceType == null) {
            predicates.add(cb.equal(tableRoot.get("requestScope"), "operationalEnvironment"));
        }

        for (final Map.Entry<String, String> entry : orchestrationMap.entrySet()) {
            String mapKey = entry.getKey();
            if (mapKey.equalsIgnoreCase("requestorId")) {
                mapKey = "requestorId";
            } else if (mapKey.equalsIgnoreCase("requestExecutionDate")) {
                mapKey = "startTime";
            } else if (mapKey.equalsIgnoreCase("operationalEnvironmentId")) {
                mapKey = "operationalEnvId";
            } else if (mapKey.equalsIgnoreCase("operationalEnvironmentName")) {
                mapKey = "operationalEnvName";
            } else if (mapKey.equalsIgnoreCase("resourceType")) {
                mapKey = "requestScope";
            }

            final String propertyValue = entry.getValue();
            if (mapKey.equals("startTime")) {
                final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
                try {
                    final Date thisDate = format.parse(propertyValue);
                    final Timestamp minTime = new Timestamp(thisDate.getTime());
                    final Timestamp maxTime = new Timestamp(thisDate.getTime() + TimeUnit.DAYS.toMillis(1));

                    predicates.add(cb.between(tableRoot.get(mapKey), minTime, maxTime));
                } catch (final Exception e) {
                    msoLogger.debug("Exception in getCloudOrchestrationFiltersFromInfraActive(): + " + e.getMessage());
                    return null;
                }
            } else {
                predicates.add(cb.equal(tableRoot.get(mapKey), propertyValue));
            }
        }

        final Order order = cb.asc(tableRoot.get(START_TIME));
        return executeInfraQuery(crit, predicates, order);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#getRequestListFromInfraActive(java
     * .lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<InfraActiveRequests> getRequestListFromInfraActive(final String queryAttributeName,
            final String queryValue, final String requestType) {
        msoLogger.debug("Get list of infra requests from DB with " + queryAttributeName + " = " + queryValue);


        try {
            final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            final CriteriaQuery<InfraActiveRequests> crit = cb.createQuery(InfraActiveRequests.class);
            final Root<InfraActiveRequests> candidateRoot = crit.from(InfraActiveRequests.class);
            final Predicate isEqual = cb.equal(candidateRoot.get(queryAttributeName), queryValue);
            final Predicate equalRequestType = cb.equal(candidateRoot.get(REQUEST_TYPE), requestType);
            final Predicate isNull = cb.isNull(candidateRoot.get(REQUEST_TYPE));
            final Predicate orClause = cb.or(equalRequestType, isNull);
            final Order orderDesc = cb.desc(candidateRoot.get(START_TIME));
            final Order orderAsc = cb.asc(candidateRoot.get(SOURCE));
            crit.where(cb.and(isEqual, orClause)).orderBy(orderDesc, orderAsc);

            final List<InfraActiveRequests> arList = entityManager.createQuery(crit).getResultList();
            if (arList != null && !arList.isEmpty()) {
                return arList;
            }
        } catch (final Exception exception) {
            msoLogger.error("Unable to execute query", exception);
        }
        return Collections.emptyList();
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#getRequestFromInfraActive(java.
     * lang.String, java.lang.String)
     */
    @Override
    public InfraActiveRequests getRequestFromInfraActive(final String requestId, final String requestType) {
        final long startTime = System.currentTimeMillis();
        msoLogger.debug("Get infra request from DB with id " + requestId);

        InfraActiveRequests ar = null;
        try {
            final Query query = entityManager.createQuery(
                    "from InfraActiveRequests where (requestId = :requestId OR clientRequestId = :requestId) and requestType = :requestType");
            query.setParameter(REQUEST_ID, requestId);
            query.setParameter(REQUEST_TYPE, requestType);
            ar = this.getSingleResult(query);
        } finally {
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "getRequestFromInfraActive", null);
        }
        return ar;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#checkDuplicateByVnfName(java.lang.
     * String, java.lang.String, java.lang.String)
     */
    @Override
    public InfraActiveRequests checkDuplicateByVnfName(final String vnfName, final String action,
            final String requestType) {

        final long startTime = System.currentTimeMillis();
        msoLogger.debug("Get infra request from DB for VNF " + vnfName + " and action " + action + " and requestType "
                + requestType);

        InfraActiveRequests ar = null;
        try {
            final Query query = entityManager.createQuery(
                    "from InfraActiveRequests where vnfName = :vnfName and action = :action and (requestStatus = 'PENDING' or requestStatus = 'IN_PROGRESS' or requestStatus = 'TIMEOUT' or requestStatus = 'PENDING_MANUAL_TASK') and requestType = :requestType ORDER BY startTime DESC");
            query.setParameter("vnfName", vnfName);
            query.setParameter("action", action);
            query.setParameter(REQUEST_TYPE, requestType);
            @SuppressWarnings("unchecked")
            final List<InfraActiveRequests> results = query.getResultList();
            if (!results.isEmpty()) {
                ar = results.get(0);
            }
        } finally {
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "checkDuplicateByVnfName", null);
        }

        return ar;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#checkDuplicateByVnfId(java.lang.
     * String, java.lang.String, java.lang.String)
     */
    @Override
    public InfraActiveRequests checkDuplicateByVnfId(final String vnfId, final String action,
            final String requestType) {

        final long startTime = System.currentTimeMillis();
        msoLogger.debug("Get list of infra requests from DB for VNF " + vnfId + " and action " + action);

        InfraActiveRequests ar = null;
        try {
            final Query query = entityManager.createQuery(
                    "from InfraActiveRequests where vnfId = :vnfId and action = :action and (requestStatus = 'PENDING' or requestStatus = 'IN_PROGRESS' or requestStatus = 'TIMEOUT' or requestStatus = 'PENDING_MANUAL_TASK') and requestType = :requestType ORDER BY startTime DESC");
            query.setParameter("vnfId", vnfId);
            query.setParameter("action", action);
            query.setParameter(REQUEST_TYPE, requestType);
            @SuppressWarnings("unchecked")
            final List<InfraActiveRequests> results = query.getResultList();
            if (!results.isEmpty()) {
                ar = results.get(0);
            }
        } finally {
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "checkDuplicateByVnfId", null);
        }

        return ar;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.onap.so.requestsdb.InfraActiveRequestsRepositoryCustom#checkVnfIdStatus(java.lang.String)
     */
    @Override
    public InfraActiveRequests checkVnfIdStatus(final String operationalEnvironmentId) {
        final long startTime = System.currentTimeMillis();
        msoLogger.debug("Get Infra request from DB for OperationalEnvironmentId " + operationalEnvironmentId);

        InfraActiveRequests ar = null;
        try {
            final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            final CriteriaQuery<InfraActiveRequests> crit = cb.createQuery(InfraActiveRequests.class);
            final Root<InfraActiveRequests> candidateRoot = crit.from(InfraActiveRequests.class);
            final Predicate operationalEnvEq =
                    cb.equal(candidateRoot.get("operationalEnvId"), operationalEnvironmentId);
            final Predicate requestStatusNotEq = cb.notEqual(candidateRoot.get(REQUEST_STATUS), "COMPLETE");
            final Predicate actionEq = cb.equal(candidateRoot.get("action"), "create");
            final Order startTimeOrder = cb.desc(candidateRoot.get("startTime"));
            crit.select(candidateRoot);
            crit.where(cb.and(operationalEnvEq, requestStatusNotEq, actionEq));
            crit.orderBy(startTimeOrder);
            final TypedQuery<InfraActiveRequests> query = entityManager.createQuery(crit);
            final List<InfraActiveRequests> results = query.getResultList();
            if (!results.isEmpty()) {
                ar = results.get(0);
            }
        } finally {
            msoLogger.recordMetricEvent(startTime, MsoLogger.StatusCode.COMPLETE, MsoLogger.ResponseCode.Suc,
                    "Successfully", "RequestDB", "checkDuplicateByVnfName", null);
        }

        return ar;
    }

    protected <T> T getSingleResult(final Query query) {
        query.setMaxResults(1);
        final List<T> list = query.getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw new NonUniqueResultException();
        }

    }

    @Override
    public List<InfraActiveRequests> getInfraActiveRequests(final Map<String, String[]> filters, final long startTime,
            final long endTime, final Integer maxResult) {
        if (filters == null) {
            return Collections.emptyList();
        }
        try {
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            final CriteriaQuery<InfraActiveRequests> criteriaQuery =
                    criteriaBuilder.createQuery(InfraActiveRequests.class);
            final Root<InfraActiveRequests> tableRoot = criteriaQuery.from(InfraActiveRequests.class);
            final List<Predicate> predicates = getPredicates(filters, criteriaBuilder, tableRoot);

            final Timestamp minTime = new Timestamp(startTime);
            final Timestamp maxTime = new Timestamp(endTime);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(tableRoot.get(START_TIME), minTime));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(tableRoot.get(END_TIME), maxTime));

            criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
            if (maxResult != null) {
                return entityManager.createQuery(criteriaQuery).setMaxResults(maxResult).getResultList();
            }
            return entityManager.createQuery(criteriaQuery).getResultList();
        } catch (final Exception exception) {
            msoLogger.error("Unable to execute query using filters: " + filters, exception);
            return Collections.emptyList();
        }
    }

    private List<Predicate> getPredicates(final Map<String, String[]> filters, final CriteriaBuilder criteriaBuilder,
            final Root<InfraActiveRequests> tableRoot) {
        final List<Predicate> predicates = new LinkedList<>();
        for (final Entry<String, String[]> entry : filters.entrySet()) {
            final String[] params = entry.getValue();
            if (VALID_COLUMNS.contains(entry.getKey()) && params.length == 2) {
                final QueryOperationType operationType = QueryOperationType.getQueryOperationType(params[0]);
                final Predicate predicate =
                        operationType.getPredicate(criteriaBuilder, tableRoot, entry.getKey(), params[1]);
                predicates.add(predicate);
            }
        }
        return predicates;
    }
}

/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.db.catalog.data.repository;

import org.onap.so.db.catalog.beans.macro.RainyDayHandlerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "rainy_day_handler_macro", path = "rainy_day_handler_macro")
public interface RainyDayHandlerStatusRepository extends JpaRepository<RainyDayHandlerStatus, Integer> {

    @Query(value = "SELECT * FROM rainy_day_handler_macro WHERE (FLOW_NAME = :flowName ) AND SERVICE_TYPE IN (:serviceType ,'*') "
            + " AND VNF_TYPE IN ( :vnfType , '*') AND ERROR_CODE IN (:errorCode  ,'*') AND WORK_STEP IN (:workStep , '*' ) "
            + " AND ( :errorMessage REGEXP rainy_day_handler_macro.REG_EX_ERROR_MESSAGE OR REG_EX_ERROR_MESSAGE = '*') "
            + " ORDER BY CASE WHEN 'asdf' REGEXP REG_EX_ERROR_MESSAGE THEN 0 WHEN ERROR_CODE != '*' THEN 1 WHEN VNF_TYPE != '*' THEN 2 WHEN SERVICE_TYPE != '*' THEN 3 ELSE 4 END LIMIT 1;",
            nativeQuery = true)
    RainyDayHandlerStatus findRainyDayHandler(@Param("flowName") String flowName,
            @Param("serviceType") String serviceType, @Param("vnfType") String vnfType,
            @Param("errorCode") String errorCode, @Param("workStep") String workStep,
            @Param("errorMessage") String errorMessage);
}

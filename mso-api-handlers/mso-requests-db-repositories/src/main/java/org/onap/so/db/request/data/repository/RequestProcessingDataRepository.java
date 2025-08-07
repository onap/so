/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.onap.so.db.request.beans.RequestProcessingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "requestProcessingData", path = "requestProcessingData")
public interface RequestProcessingDataRepository extends JpaRepository<RequestProcessingData, Integer> {
    RequestProcessingData findOneBySoRequestIdAndGroupingIdAndNameAndTag(@Param("SO_REQUEST_ID") String soRequestId,
            @Param("GROUPING_ID") String groupingId, @Param("NAME") String name, @Param("TAG") String tag);

    RequestProcessingData findOneBySoRequestIdAndGroupingIdAndName(@Param("SO_REQUEST_ID") String soRequestId,
            @Param("GROUPING_ID") String groupingId, @Param("NAME") String name);

    RequestProcessingData findOneBySoRequestIdAndName(@Param("SO_REQUEST_ID") String soRequestId,
            @Param("NAME") String name);

    RequestProcessingData[] findBySoRequestIdOrderByGroupingIdDesc(@Param("SO_REQUEST_ID") String soRequestId);

    RequestProcessingData[] findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc(
            @Param("SO_REQUEST_ID") String soRequestId, @Param("IS_INTERNAL_DATA") Boolean isDataInternal);

    RequestProcessingData[] findByGroupingIdAndNameAndTag(@Param("GROUPING_ID") String groupingId,
            @Param("NAME") String name, @Param("TAG") String tag);

    RequestProcessingData[] findBySoRequestIdAndNameAndTagOrderByCreatedDesc(@Param("SO_REQUEST_ID") String soRequestId,
            @Param("NAME") String name, @Param("TAG") String tag);
}

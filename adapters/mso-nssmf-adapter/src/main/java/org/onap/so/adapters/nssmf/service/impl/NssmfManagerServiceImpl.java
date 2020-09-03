/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.nssmf.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.onap.so.adapters.nssmf.annotation.ServiceLogger;
import org.onap.so.adapters.nssmf.config.NssmfAdapterConfig;
import org.onap.so.adapters.nssmf.enums.ActionType;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.manager.NssmfManagerBuilder;
import org.onap.so.adapters.nssmf.entity.RestResponse;
import org.onap.so.adapters.nssmf.manager.NssmfManager;
import org.onap.so.adapters.nssmf.service.NssmfManagerService;
import org.onap.so.adapters.nssmf.util.RestUtil;
import org.onap.so.beans.nsmf.*;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@ServiceLogger
public class NssmfManagerServiceImpl implements NssmfManagerService {

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private ResourceOperationStatusRepository repository;

    @Autowired
    private NssmfAdapterConfig nssmfAdapterConfig;

    @Override
    public ResponseEntity allocateNssi(NssmfAdapterNBIRequest request) {
        try {

            if (StringUtils.isNotBlank(request.getServiceInfo().getNssiId())) {
                return buildResponse(buildNssmfManager(request, ActionType.MODIFY).modifyNssi(request));
            }

            return buildResponse(buildNssmfManager(request, ActionType.ALLOCATE).allocateNssi(request));

        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @Override
    public ResponseEntity deAllocateNssi(NssmfAdapterNBIRequest request, String sliceProfileId) {
        try {
            return buildResponse(
                    buildNssmfManager(request, ActionType.DEALLOCATE).deAllocateNssi(request, sliceProfileId));
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @Override
    public ResponseEntity activateNssi(NssmfAdapterNBIRequest request, String snssai) {
        try {
            return buildResponse(buildNssmfManager(request, ActionType.ACTIVATE).activateNssi(request, snssai));
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @Override
    public ResponseEntity deActivateNssi(NssmfAdapterNBIRequest request, String snssai) {
        try {
            return buildResponse(buildNssmfManager(request, ActionType.DEACTIVATE).deActivateNssi(request, snssai));
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @Override
    public ResponseEntity queryJobStatus(NssmfAdapterNBIRequest jobReq, String jobId) {
        try {
            return buildResponse(buildNssmfManager(jobReq, ActionType.QUERY_JOB_STATUS).queryJobStatus(jobReq, jobId));
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @Override
    public ResponseEntity queryNSSISelectionCapability(NssmfAdapterNBIRequest nbiRequest) {
        EsrInfo esrInfo = nbiRequest.getEsrInfo();
        try {
            return buildResponse(buildNssmfManager(esrInfo, ActionType.QUERY_NSSI_SELECTION_CAPABILITY, null)
                    .queryNSSISelectionCapability(nbiRequest));
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    @Override
    public ResponseEntity querySubnetCapability(NssmfAdapterNBIRequest nbiRequest) {
        EsrInfo esrInfo = nbiRequest.getEsrInfo();
        try {
            return buildResponse(
                    buildNssmfManager(esrInfo, ActionType.QUERY_SUB_NET_CAPABILITY, null).querySubnetCapability(nbiRequest));
        } catch (ApplicationException e) {
            return e.buildErrorResponse();
        }
    }

    private ResponseEntity buildResponse(RestResponse rsp) {
        return ResponseEntity.status(rsp.getStatus()).body(rsp.getResponseContent());
    }


    private NssmfManager buildNssmfManager(NssmfAdapterNBIRequest request, ActionType actionType)
            throws ApplicationException {
        return buildNssmfManager(request.getEsrInfo(), actionType, request.getServiceInfo());
    }

    private NssmfManager buildNssmfManager(EsrInfo esrInfo, ActionType actionType, ServiceInfo serviceInfo)
            throws ApplicationException {

        return new NssmfManagerBuilder(esrInfo).setActionType(actionType).setRepository(repository)
                .setRestUtil(restUtil).setAdapterConfig(nssmfAdapterConfig).setServiceInfo(serviceInfo).build();
    }
}

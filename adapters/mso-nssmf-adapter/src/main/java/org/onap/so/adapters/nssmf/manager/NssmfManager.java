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

package org.onap.so.adapters.nssmf.manager;

import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.entity.RestResponse;
import org.onap.so.beans.nsmf.*;

public interface NssmfManager {

    RestResponse allocateNssi(NssmfAdapterNBIRequest nssmfRequest) throws ApplicationException;

    RestResponse deAllocateNssi(NssmfAdapterNBIRequest nssmfRequest, String sliceId) throws ApplicationException;

    RestResponse activateNssi(NssmfAdapterNBIRequest nssmfRequest, String snssai) throws ApplicationException;

    RestResponse deActivateNssi(NssmfAdapterNBIRequest nssmfRequest, String snssai) throws ApplicationException;

    RestResponse queryJobStatus(NssmfAdapterNBIRequest jobReq, String jobId) throws ApplicationException;

    RestResponse queryNSSISelectionCapability(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException;

    RestResponse querySubnetCapability(NssmfAdapterNBIRequest nbiRequest) throws ApplicationException;

    RestResponse modifyNssi(NssmfAdapterNBIRequest modifyRequest) throws ApplicationException;
}

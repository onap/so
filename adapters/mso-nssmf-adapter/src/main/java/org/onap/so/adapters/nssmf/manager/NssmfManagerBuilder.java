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

import org.onap.so.adapters.nssmf.config.NssmfAdapterConfig;
import org.onap.so.adapters.nssmf.consts.NssmfAdapterConsts;
import org.onap.so.adapters.nssmf.enums.ActionType;
import org.onap.so.adapters.nssmf.enums.ExecutorType;
import org.onap.so.adapters.nssmf.exceptions.ApplicationException;
import org.onap.so.adapters.nssmf.manager.impl.external.ExternalAnNssmfManager;
import org.onap.so.adapters.nssmf.manager.impl.external.ExternalCnNssmfManager;
import org.onap.so.adapters.nssmf.manager.impl.internal.InternalAnNssmfManager;
import org.onap.so.adapters.nssmf.manager.impl.internal.InternalCnNssmfManager;
import org.onap.so.adapters.nssmf.manager.impl.internal.InternalTnNssmfManager;
import org.onap.so.adapters.nssmf.manager.impl.*;
import org.onap.so.adapters.nssmf.util.RestUtil;
import org.onap.so.beans.nsmf.EsrInfo;
import org.onap.so.beans.nsmf.NetworkType;
import org.onap.so.beans.nsmf.ServiceInfo;
import org.onap.so.db.request.data.repository.ResourceOperationStatusRepository;

public class NssmfManagerBuilder {

    private BaseNssmfManager nssmfManger;

    private RestUtil restUtil;

    private ActionType actionType;

    private ResourceOperationStatusRepository repository;

    private ServiceInfo serviceInfo;

    private NssmfAdapterConfig adapterConfig;

    public NssmfManagerBuilder(EsrInfo esrInfo) throws ApplicationException {

        ExecutorType executorType = getExecutorType(esrInfo);
        NetworkType networkType = esrInfo.getNetworkType();

        if (ExecutorType.INTERNAL.equals(executorType) && NetworkType.CORE.equals(networkType)) {
            this.nssmfManger = new InternalCnNssmfManager().setEsrInfo(esrInfo).setExecutorType(executorType);
            return;
        }

        if (ExecutorType.INTERNAL.equals(executorType) && NetworkType.TRANSPORT.equals(networkType)) {
            this.nssmfManger = new InternalTnNssmfManager().setEsrInfo(esrInfo).setExecutorType(executorType);
            return;
        }

        if (ExecutorType.INTERNAL.equals(executorType) && NetworkType.ACCESS.equals(networkType)) {
            this.nssmfManger = new InternalAnNssmfManager().setEsrInfo(esrInfo).setExecutorType(executorType);
            return;
        }

        if (ExecutorType.EXTERNAL.equals(executorType) && NetworkType.CORE.equals(networkType)) {
            this.nssmfManger = new ExternalCnNssmfManager().setEsrInfo(esrInfo).setExecutorType(executorType)
                    .setInitStatus("deactivated");
            return;
        }

        if (ExecutorType.EXTERNAL.equals(executorType) && NetworkType.ACCESS.equals(networkType)) {
            this.nssmfManger = new ExternalAnNssmfManager().setEsrInfo(esrInfo).setExecutorType(executorType)
                    .setInitStatus("activated");
            return;
        }

        throw new ApplicationException(404, "invalid domain and simulator");
    }

    private ExecutorType getExecutorType(EsrInfo esrInfo) {
        if (NssmfAdapterConsts.ONAP_INTERNAL_TAG.equals(esrInfo.getVendor())) {
            return ExecutorType.INTERNAL;
        }
        return ExecutorType.EXTERNAL;
    }

    public NssmfManagerBuilder setRestUtil(RestUtil restUtil) {
        this.restUtil = restUtil;
        return this;
    }

    public NssmfManagerBuilder setActionType(ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public NssmfManagerBuilder setRepository(ResourceOperationStatusRepository repository) {
        this.repository = repository;
        return this;
    }

    public NssmfManagerBuilder setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
        return this;
    }

    public NssmfManagerBuilder setAdapterConfig(NssmfAdapterConfig adapterConfig) {
        this.adapterConfig = adapterConfig;
        return this;
    }

    public NssmfManager build() {
        return this.nssmfManger.setRestUtil(restUtil).setAdapterConfig(adapterConfig).setRepository(repository)
                .setActionType(actionType).setServiceInfo(serviceInfo);
    }
}

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

package org.onap.so.db.catalog.rest.beans;

import com.openpojo.business.annotation.BusinessKey;
import org.onap.so.db.catalog.beans.*;
import java.io.Serializable;
import java.util.ArrayList;

/*
 * A simple holder for Service and its associated elements: VnfResource, 1-n VfModule, Network TBD
 */

public class ServiceMacroHolder implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;
    @BusinessKey
    private Service service;
    private ArrayList<VnfResource> vnfResources;
    private ArrayList<NetworkResourceCustomization> networkResourceCustomizations;
    private ArrayList<AllottedResourceCustomization> allottedResourceCustomizations;
    private ArrayList<VnfResourceCustomization> vnfResourceCustomizations;
    private ArrayList<ServiceProxyResourceCustomization> serviceProxyResourceCustomizations;

    private ArrayList<PnfResourceCustomization> pnfResourceCustomizations;
    private ArrayList<PnfResource> pnfResources;


    public ServiceMacroHolder() {
        super();
        this.service = null;
        this.vnfResources = new ArrayList<VnfResource>();
        this.networkResourceCustomizations = new ArrayList<>();
        this.allottedResourceCustomizations = new ArrayList<>();
        this.vnfResourceCustomizations = new ArrayList<>();
        this.serviceProxyResourceCustomizations = new ArrayList<>();
        this.pnfResourceCustomizations = new ArrayList<>();
        this.pnfResources = new ArrayList<PnfResource>();
    }

    public ServiceMacroHolder(Service service) {
        this();
        this.service = service;
    }

    public Service getService() {
        return this.service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setVnfResources(ArrayList<VnfResource> vnfResources) {
        this.vnfResources = vnfResources;
    }

    public ArrayList<VnfResource> getVnfResources() {
        return this.vnfResources;
    }

    public void addVnfResource(VnfResource vr) {
        if (vr != null) {
            if (this.vnfResources != null) {
                this.vnfResources.add(vr);
            } else {
                this.vnfResources = new ArrayList<>();
                this.vnfResources.add(vr);
            }
        }
    }

    public void setPnfResources(ArrayList<PnfResource> pnfResources) {
        this.pnfResources = pnfResources;
    }

    public ArrayList<PnfResource> getPnfResources() {
        return this.pnfResources;
    }

    public void addPnfResource(PnfResource pr) {
        if (pr != null) {
            if (this.pnfResources != null) {
                this.pnfResources.add(pr);
            } else {
                this.pnfResources = new ArrayList<>();
                this.pnfResources.add(pr);
            }
        }
    }

    public void setVnfResourceCustomizations(ArrayList<VnfResourceCustomization> vnfResourceCustomizations) {
        this.vnfResourceCustomizations = vnfResourceCustomizations;
    }

    public ArrayList<VnfResourceCustomization> getVnfResourceCustomizations() {
        return this.vnfResourceCustomizations;
    }

    public void addVnfResourceCustomizations(VnfResourceCustomization vrc) {
        if (vrc != null) {
            if (this.vnfResourceCustomizations != null) {
                this.vnfResourceCustomizations.add(vrc);
            } else {
                this.vnfResourceCustomizations = new ArrayList<>();
                this.vnfResourceCustomizations.add(vrc);
            }
        }
    }

    public void setPnfResourceCustomizations(ArrayList<PnfResourceCustomization> pnfResourceCustomizations) {
        this.pnfResourceCustomizations = pnfResourceCustomizations;
    }

    public ArrayList<PnfResourceCustomization> getPnfResourceCustomizations() {
        return this.pnfResourceCustomizations;
    }

    public void addPnfResourceCustomizations(PnfResourceCustomization prc) {
        if (prc != null) {
            if (this.pnfResourceCustomizations != null) {
                this.pnfResourceCustomizations.add(prc);
            } else {
                this.pnfResourceCustomizations = new ArrayList<>();
                this.pnfResourceCustomizations.add(prc);
            }
        }
    }

    public void setNetworkResourceCustomization(ArrayList<NetworkResourceCustomization> networkResourceCustomizations) {
        this.networkResourceCustomizations = networkResourceCustomizations;
    }

    public ArrayList<NetworkResourceCustomization> getNetworkResourceCustomization() {
        return this.networkResourceCustomizations;
    }

    public void addNetworkResourceCustomization(NetworkResourceCustomization nrc) {
        if (this.networkResourceCustomizations != null) {
            this.networkResourceCustomizations.add(nrc);
        } else {
            this.networkResourceCustomizations = new ArrayList<>();
            this.networkResourceCustomizations.add(nrc);
        }
    }

    public void setAllottedResourceCustomization(
            ArrayList<AllottedResourceCustomization> allottedResourceCustomizations) {
        this.allottedResourceCustomizations = allottedResourceCustomizations;
    }

    public ArrayList<AllottedResourceCustomization> getAllottedResourceCustomization() {
        return this.allottedResourceCustomizations;
    }

    public void addAllottedResourceCustomization(AllottedResourceCustomization arc) {
        if (this.allottedResourceCustomizations != null) {
            this.allottedResourceCustomizations.add(arc);
        } else {
            this.allottedResourceCustomizations = new ArrayList<>();
            this.allottedResourceCustomizations.add(arc);
        }
    }

    public void addServiceProxyResourceCustomization(ServiceProxyResourceCustomization sprc) {
        if (this.serviceProxyResourceCustomizations != null) {
            this.serviceProxyResourceCustomizations.add(sprc);
        } else {
            this.serviceProxyResourceCustomizations = new ArrayList<>();
            this.serviceProxyResourceCustomizations.add(sprc);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ServicePlus: ");
        if (this.service != null) {
            sb.append("service: " + this.service.toString());
        } else {
            sb.append("service: null");
        }
        if (this.vnfResourceCustomizations != null && this.vnfResourceCustomizations.size() > 0) {
            int i = 0;
            sb.append("VnfResources: ");
            for (VnfResourceCustomization vrc : this.vnfResourceCustomizations) {
                sb.append(", vnfResourceCustomization[" + i++ + "]:" + vrc.toString());
            }
        } else {
            sb.append("none");
        }
        if (this.pnfResourceCustomizations != null && this.pnfResourceCustomizations.size() > 0) {
            int i = 0;
            sb.append("PnfResources: ");
            for (PnfResourceCustomization prc : this.pnfResourceCustomizations) {
                sb.append(", pnfResourceCustomization[" + i++ + "]:" + prc.toString());
            }
        } else {
            sb.append("none");
        }
        if (this.pnfResources != null && this.pnfResources.size() > 0) {
            int i = 0;
            sb.append("PnfResources: ");
            for (PnfResource pr : this.pnfResources) {
                sb.append(", pnfResource[" + i++ + "]:" + pr.toString());
            }
        } else {
            sb.append("none");
        }

        if (this.vnfResources != null && this.vnfResources.size() > 0) {
            int i = 0;
            sb.append("VnfResources: ");
            for (VnfResource vr : this.vnfResources) {
                sb.append(", vnfResource[" + i++ + "]:" + vr.toString());
            }
        } else {
            sb.append("none");
        }
        if (this.networkResourceCustomizations != null && this.networkResourceCustomizations.size() > 0) {
            int i = 0;
            sb.append("NetworkResourceCustomizations:");
            for (NetworkResourceCustomization nrc : this.networkResourceCustomizations) {
                sb.append("NRC[" + i++ + "]: " + nrc.toString());
            }
        }
        if (this.allottedResourceCustomizations != null && this.allottedResourceCustomizations.size() > 0) {
            int i = 0;
            sb.append("AllottedResourceCustomizations:");
            for (AllottedResourceCustomization arc : this.allottedResourceCustomizations) {
                sb.append("ARC[" + i++ + "]: " + arc.toString());
            }
        }
        if (this.serviceProxyResourceCustomizations != null && this.serviceProxyResourceCustomizations.size() > 0) {
            int i = 0;
            sb.append("ServiceProxyResourceCustomizations:");
            for (ServiceProxyResourceCustomization sprc : this.serviceProxyResourceCustomizations) {
                sb.append("SPRC[" + i++ + "]: " + sprc.toString());
            }
        }

        return sb.toString();
    }

    public ArrayList<NetworkResourceCustomization> getNetworkResourceCustomizations() {
        return networkResourceCustomizations;
    }

    public void setNetworkResourceCustomizations(
            ArrayList<NetworkResourceCustomization> networkResourceCustomizations) {
        this.networkResourceCustomizations = networkResourceCustomizations;
    }

    public ArrayList<AllottedResourceCustomization> getAllottedResourceCustomizations() {
        return allottedResourceCustomizations;
    }

    public void setAllottedResourceCustomizations(
            ArrayList<AllottedResourceCustomization> allottedResourceCustomizations) {
        this.allottedResourceCustomizations = allottedResourceCustomizations;
    }

    public ArrayList<ServiceProxyResourceCustomization> getServiceProxyResourceCustomizations() {
        return serviceProxyResourceCustomizations;
    }

    public void setServiceProxyResourceCustomizations(
            ArrayList<ServiceProxyResourceCustomization> serviceProxyResourceCustomizations) {
        this.serviceProxyResourceCustomizations = serviceProxyResourceCustomizations;
    }
}

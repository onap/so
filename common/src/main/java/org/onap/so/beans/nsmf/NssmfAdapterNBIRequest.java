package org.onap.so.beans.nsmf;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;


@Data
public class NssmfAdapterNBIRequest implements Serializable {

    private static final long serialVersionUID = -454145891489457960L;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EsrInfo esrInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ServiceInfo serviceInfo;

    private AllocateCnNssi allocateCnNssi;

    private AllocateTnNssi allocateTnNssi;

    private AllocateAnNssi allocateAnNssi;

    private ActDeActNssi actDeActNssi;

    private DeAllocateNssi deAllocateNssi;

    private String subnetCapabilityQuery;

    private String responseId;
}

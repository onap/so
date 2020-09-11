package org.onap.so.beans.nsmf.oof;

import lombok.Getter;
import org.onap.so.beans.nsmf.NetworkType;

@Getter
public enum DomainType {

    AN_NF("AN-NF", NetworkType.ACCESS),

    CN("CN", NetworkType.CORE),

    TN_FH("TN-FH", NetworkType.TRANSPORT),

    TN_MH("TN-MH", NetworkType.TRANSPORT),

    TN_BH("TN-BH", NetworkType.TRANSPORT),;

    private NetworkType networkType;

    private String domainType;

    DomainType(String domainType, NetworkType networkType) {
        this.domainType = domainType;
        this.networkType = networkType;
    }
}

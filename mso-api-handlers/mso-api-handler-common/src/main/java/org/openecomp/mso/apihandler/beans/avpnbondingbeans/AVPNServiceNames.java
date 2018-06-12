package org.openecomp.mso.apihandler.beans.avpnbondingbeans;

public enum AVPNServiceNames {
  AVPN_BONDING_TO_COLLABORATE("AVPNBondingToCollaborate"),
  AVPN_BONDING_TO_IP_FLEX_REACH("AVPNBondingToIPFlexReach"),
  AVPN_BONDING_TO_IP_TOLL_FREE("AVPNBondingToIPTollFree");

  private String serviceName;

  AVPNServiceNames(String serviceName){
    this.serviceName=serviceName;
  }

  public String getServiceName() {
    return serviceName;
  }

  @Override
  public String toString() {
    return "AVPNServiceNames{" +
            "serviceName='" + serviceName + '\'' +
            '}';
  }
}

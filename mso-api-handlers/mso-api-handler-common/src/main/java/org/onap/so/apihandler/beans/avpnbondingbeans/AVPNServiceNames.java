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

package org.onap.so.apihandler.beans.avpnbondingbeans;

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

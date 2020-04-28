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

package org.onap.so.client.aai;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Pnf;
import org.onap.aai.domain.yang.Pserver;

public interface AAIRestClientI {

    List<Pserver> getPhysicalServerByVnfId(String vnfId) throws IOException;

    void updateMaintenceFlagVnfId(String vnfId, boolean inMaint);

    GenericVnf getVnfByName(String vnfId);

    Optional<Pnf> getPnfByName(String pnfId);

    void createPnf(String pnfId, Pnf pnf);

    void updatePnf(String pnfId, Pnf pnf);
}

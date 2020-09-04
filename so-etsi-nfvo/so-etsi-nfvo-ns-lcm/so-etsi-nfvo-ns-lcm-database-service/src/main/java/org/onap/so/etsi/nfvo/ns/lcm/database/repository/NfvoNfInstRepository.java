/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.database.repository;

import java.util.List;
import java.util.Optional;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNfInst;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author mukeshsharma(mukeshsharma@est.tech)
 */
public interface NfvoNfInstRepository extends CrudRepository<NfvoNfInst, String> {

    Optional<NfvoNfInst> findByNfInstId(final String nfInstId);

    List<NfvoNfInst> findByNsInstNsInstId(final String nsInstId);

    List<NfvoNfInst> findByNsInstNsInstIdAndName(final String nsInstId, final String name);
}

/**
 * ============LICENSE_START==================================================== org.onap.so
 * =========================================================================== Copyright (c) 2018 AT&T Intellectual
 * Property. All rights reserved. =========================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.so.security.cadi.taf;

import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.Taf;

/**
 * EpiTAF
 *
 * Short for "Epic TAF". Be able to run through a series of TAFs to obtain the validation needed.
 *
 * OK, the name could probably be better as "Tafs", like it was originally, but the pun was too irresistible for this
 * author to pass up.
 *
 * @author Jonathan
 *
 */
public class EpiTaf implements Taf {
    private Taf[] tafs;

    /**
     * EpiTaf constructor
     *
     * Construct the EpiTaf from variable TAF parameters
     * 
     * @param tafs
     * @throws CadiException
     */
    public EpiTaf(Taf... tafs) throws CadiException {
        this.tafs = tafs;
        if (tafs.length == 0)
            throw new CadiException("Need at least one Taf implementation in constructor");
    }

    /**
     * validate
     *
     * Respond with the first TAF to authenticate user based on variable info and "LifeForm" (is it a human behind an
     * interface, or a server behind a protocol).
     *
     * If there is no TAF that can authenticate, respond with the first TAF that suggests it can establish an
     * Authentication conversation (TRY_AUTHENTICATING).
     *
     * If no TAF declares either, respond with NullTafResp (which denies all questions)
     */
    public TafResp validate(LifeForm reading, String... info) {
        TafResp tresp, firstTryAuth = null;
        for (Taf taf : tafs) {
            tresp = taf.validate(reading, info);
            switch (tresp.isAuthenticated()) {
                case TRY_ANOTHER_TAF:
                    break;
                case TRY_AUTHENTICATING:
                    if (firstTryAuth == null)
                        firstTryAuth = tresp;
                    break;
                default:
                    return tresp;
            }
        }

        // No TAFs configured, at this point. It is safer at this point to be "not validated",
        // rather than "let it go"
        return firstTryAuth == null ? NullTafResp.singleton() : firstTryAuth;
    }

}

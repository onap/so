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

package org.onap.so.security.cadi.lur;

import java.security.Principal;
import java.util.List;
import org.onap.so.security.cadi.CachingLur;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.CredVal;
import org.onap.so.security.cadi.Lur;
import org.onap.so.security.cadi.Permission;

/**
 * EpiLUR
 *
 * Short for "Epic LUR". Be able to run through a series of LURs to obtain the validation needed.
 *
 * The pun is better for the other pattern... "TAF" (aka EpiTaf), but it's still the larger picture of LURs that will be
 * accomplished.
 *
 * FYI, the reason we separate LURs, rather than combine, is that Various User Repository Resources have different
 * Caching requirements. For instance, the Local User Repo (with stand alone names), never expire, but might be
 * refreshed with a change in Configuration File, while the Remote Service based LURs will need to expire at prescribed
 * intervals
 *
 * @author Jonathan
 *
 */
public final class EpiLur implements Lur {
    private final Lur[] lurs;

    /**
     * EpiLur constructor
     *
     * Construct the EpiLur from variable TAF parameters
     * 
     * @param lurs
     * @throws CadiException
     */
    public EpiLur(Lur... lurs) throws CadiException {
        this.lurs = lurs;
        if (lurs.length == 0)
            throw new CadiException("Need at least one Lur implementation in constructor");
    }

    public boolean fish(Principal bait, Permission... pond) {
        if (pond == null) {
            return false;
        }
        boolean rv = false;
        Lur lur;
        for (int i = 0; !rv && i < lurs.length; ++i) {
            rv = (lur = lurs[i]).fish(bait, pond);
            if (!rv && lur.handlesExclusively(pond))
                break;
        }
        return rv;
    }

    public void fishAll(Principal bait, List<Permission> permissions) {
        for (Lur lur : lurs) {
            lur.fishAll(bait, permissions);
        }
    }

    public void destroy() {
        for (Lur lur : lurs) {
            lur.destroy();
        }
    }

    /**
     * Return the first Lur (if any) which also implements UserPass
     * 
     * @return
     */
    public CredVal getUserPassImpl() {
        for (Lur lur : lurs) {
            if (lur instanceof CredVal) {
                return (CredVal) lur;
            }
        }
        return null;
    }

    // Never needed... Only EpiLur uses...
    public boolean handlesExclusively(Permission... pond) {
        return false;
    }

    /**
     * Get Lur for index. Returns null if out of range
     * 
     * @param idx
     * @return
     */
    public Lur get(int idx) {
        if (idx >= 0 && idx < lurs.length) {
            return lurs[idx];
        }
        return null;
    }

    public boolean handles(Principal p) {
        for (Lur l : lurs) {
            if (l.handles(p)) {
                return true;
            }
        }
        return false;
    }

    public void remove(String id) {
        for (Lur l : lurs) {
            if (l instanceof CachingLur) {
                ((CachingLur<?>) l).remove(id);
            }
        }
    }

    public Lur subLur(Class<? extends Lur> cls) {
        for (Lur l : lurs) {
            if (l.getClass().isAssignableFrom(cls)) {
                return l;
            }
        }
        return null;
    }

    @Override
    public Permission createPerm(String p) {
        return new LocalPermission(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.Lur#clear(java.security.Principal, java.lang.StringBuilder)
     */
    @Override
    public void clear(Principal p, StringBuilder report) {
        for (Lur lur : lurs) {
            lur.clear(p, report);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Lur lur : lurs) {
            sb.append(lur.getClass().getSimpleName());
            sb.append(": Report\n");
            sb.append(lur.toString());
            sb.append('\n');
        }
        return sb.toString();
    }
}

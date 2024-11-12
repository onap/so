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

package org.onap.so.security.cadi;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.so.security.cadi.lur.LocalPermission;

/**
 * Class to hold info from the User Perspective.
 *
 * @author Jonathan
 *
 */
public final class User<PERM extends Permission> {
    private static final Map<String, Permission> NULL_MAP = new HashMap<>();
    public String name;
    private byte[] cred;
    public Principal principal;
    Map<String, Permission> perms;
    long permExpires;
    private final long interval;
    int count;

    // Note: This should only be used for Local RBAC (in memory)
    public User(Principal principal) {
        this.principal = principal;
        name = principal.getName();
        perms = NULL_MAP;
        permExpires = Long.MAX_VALUE; // Never. Well, until 64 bits of millis since 1970 expires...
        interval = 0L;
        count = 0;
    }

    public User(String name, byte[] cred) {
        this.principal = null;
        this.name = name;
        this.cred = cred;
        perms = NULL_MAP;
        permExpires = Long.MAX_VALUE; // Never. Well, until 64 bits of millis since 1970 expires...
        interval = 0L;
        count = 0;
    }

    public User(Principal principal, long expireInterval) {
        this.principal = principal;
        this.name = principal.getName();
        perms = NULL_MAP;
        expireInterval = Math.max(expireInterval, 0); // avoid < 1
        interval = Math.max(AbsUserCache.MIN_INTERVAL, Math.min(expireInterval, AbsUserCache.MAX_INTERVAL));
        count = 0;
        renewPerm();
        renewPerm();
    }

    public User(String name, byte[] cred, long expireInterval) {
        this.principal = null;
        this.name = name;
        this.cred = cred;
        perms = NULL_MAP;
        expireInterval = Math.max(expireInterval, 0); // avoid < 1
        interval = Math.max(AbsUserCache.MIN_INTERVAL, Math.min(expireInterval, AbsUserCache.MAX_INTERVAL));
        count = 0;
        renewPerm();
    }

    public void renewPerm() {
        permExpires = System.currentTimeMillis() + interval;
    }

    public long permExpires() {
        return permExpires;
    }

    public boolean permExpired() {
        return System.currentTimeMillis() > permExpires;
    }

    public boolean noPerms() {
        return perms == null || perms == NULL_MAP || perms.values().size() == 0;
    }

    public synchronized void setNoPerms() {
        perms = NULL_MAP;
        renewPerm();
    }

    public boolean permsUnloaded() {
        return perms == null || perms == NULL_MAP;
    }

    public synchronized void incCount() {
        ++count;
    }

    public synchronized void resetCount() {
        count = 0;
    }

    public Map<String, Permission> newMap() {
        return new ConcurrentHashMap<>();
    }

    public void add(LocalPermission permission) {
        if (perms == NULL_MAP) {
            perms = newMap();
        }
        perms.put(permission.getKey(), permission);
    }

    public void add(Map<String, Permission> newMap, PERM permission) {
        newMap.put(permission.getKey(), permission);
    }

    public synchronized void setMap(Map<String, Permission> newMap) {
        perms = newMap;
        renewPerm();
    }

    public boolean contains(Permission perm) {
        for (Permission p : perms.values()) {
            if (p.match(perm))
                return true;
        }
        return false;
    }

    public void copyPermsTo(List<Permission> sink) {
        sink.addAll(perms.values());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(principal.getName());
        sb.append('|');
        boolean first = true;
        synchronized (perms) {
            for (Permission gp : perms.values()) {
                if (first) {
                    first = false;
                    sb.append(':');
                } else {
                    sb.append(',');
                }
                sb.append(gp.getKey());
            }
        }
        return sb.toString();
    }

    public byte[] getCred() {
        return cred;
    }

}

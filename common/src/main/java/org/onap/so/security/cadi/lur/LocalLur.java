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

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.onap.so.security.cadi.AbsUserCache;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.CredVal;
import org.onap.so.security.cadi.Hash;
import org.onap.so.security.cadi.Lur;
import org.onap.so.security.cadi.Permission;
import org.onap.so.security.cadi.User;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.config.Config;

/**
 * An in-memory Lur that can be configured locally with User info via properties, similar to Tomcat-users.xml
 * mechanisms.
 *
 * @author Jonathan
 *
 */
public final class LocalLur extends AbsUserCache<LocalPermission> implements Lur, CredVal {
    public static final String SEMI = "\\s*;\\s*";
    public static final String COLON = "\\s*:\\s*";
    public static final String COMMA = "\\s*,\\s*";
    public static final String PERCENT = "\\s*%\\s*";

    // Use to quickly determine whether any given group is supported by this LUR
    private final Set<String> supportingGroups;
    private String supportedRealm;

    /**
     * Construct by building structure, see "build"
     *
     * Reconstruct with "build"
     *
     * @param userProperties
     * @param groupProperties
     * @param decryptor
     * @throws IOException
     */
    public LocalLur(Access access, String userProperties, String groupProperties) throws IOException {
        super(access, 0, 0, Integer.MAX_VALUE); // data doesn't expire
        supportedRealm = access.getProperty(Config.BASIC_REALM, "localized");
        supportingGroups = new TreeSet<>();

        if (userProperties != null) {
            parseUserProperties(userProperties);
        }

        if (groupProperties != null) {
            parseGroupProperties(groupProperties);
        }
    }

    public boolean validate(String user, CredVal.Type type, byte[] cred, Object state) {
        if (cred == null) {
            return false;
        }
        User<LocalPermission> usr = getUser(user, cred);
        if (usr == null) {
            return false;
        }
        // covers null as well as bad pass
        if ((type == Type.PASSWORD) && (usr.principal instanceof ConfigPrincipal)) {
            ;
            return Hash.isEqual(cred, ((ConfigPrincipal) usr.principal).getCred());
        }
        return false;
    }

    // @Override
    public boolean fish(Principal bait, Permission... pond) {
        if (pond == null) {
            return false;
        }
        for (Permission p : pond) {
            if (handles(bait) && p instanceof LocalPermission) { // local Users only have LocalPermissions
                User<LocalPermission> user = getUser(bait);
                if (user != null) {
                    return user.contains((LocalPermission) p);
                }
            }
        }
        return false;
    }

    // We do not want to expose the actual Group, so make a copy.
    public void fishAll(Principal bait, List<Permission> perms) {
        if (handles(bait)) {
            User<LocalPermission> user = getUser(bait);
            if (user != null) {
                user.copyPermsTo(perms);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.Lur#handles(java.security.Principal)
     */
    @Override
    public boolean handles(Principal principal) {
        if (principal == null) {
            return false;
        }
        return principal.getName().endsWith(supportedRealm);
    }

    @Override
    public boolean handlesExclusively(Permission... pond) {
        boolean rv = false;
        for (Permission p : pond) {
            if (rv = supportingGroups.contains(p.getKey())) {
                break;
            }
        }
        return rv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.onap.so.security.cadi.Lur#createPerm(java.lang.String)
     */
    @Override
    public Permission createPerm(String p) {
        return new LocalPermission(p);
    }

    private void parseUserProperties(String userProperties) throws IOException {
        // For each User name...
        for (String userProperty : userProperties.trim().split(SEMI)) {
            String[] userInfo = userProperty.split(COLON, 2);
            String[] userPass = userInfo[0].split(PERCENT, 2);
            String userName = userPass[0];

            byte[] password = null;
            if (userPass.length > 1) {
                password = access.decrypt(userPass[1], true).getBytes();
                if (userName.indexOf('@') < 0) {
                    userName += '@' + access.getProperty(Config.AAF_DEFAULT_REALM, Config.getDefaultRealm());
                }
            }
            User<LocalPermission> usr;
            usr = new User<>(new ConfigPrincipal(userName, password));
            addUser(usr);
            access.log(Level.INIT, "Local User:", usr.principal);

            if (userInfo.length > 1) {
                Map<String, Permission> newMap = usr.newMap();
                for (String group : userInfo[1].split(COMMA)) {
                    supportingGroups.add(group);
                    usr.add(newMap, new LocalPermission(group));
                }
                usr.setMap(newMap);
            }
        }
    }


    private void parseGroupProperties(String groupProperties) throws IOException {
        // For each Group name...
        for (String group : groupProperties.trim().split(SEMI)) {
            String[] groups = group.split(COLON, 2);
            if (groups.length <= 1) {
                continue;
            }
            supportingGroups.add(groups[0]);
            LocalPermission p = new LocalPermission(groups[0]);

            // Add all users (known by comma separators)
            for (String groupMember : groups[1].split(COMMA)) {
                // look for password, if so, put in passMap
                String[] userPass = groupMember.split(PERCENT, 2);
                String userName = userPass[0];
                if (userName.indexOf('@') < 0) {
                    userName += '@' + access.getProperty(Config.AAF_DEFAULT_REALM, Config.getDefaultRealm());
                }

                User<LocalPermission> usr = null;
                byte[] password = null;
                if (userPass.length > 1) {
                    password = access.decrypt(userPass[1], true).getBytes();
                }
                usr = getUser(userName, password);
                if (usr == null) {
                    usr = new User<>(new ConfigPrincipal(userName, password));
                    addUser(usr);
                } else {
                    usr.principal = new ConfigPrincipal(userName, password);
                }
                usr.add(p);
                access.log(Level.INIT, "Local User:", usr.principal);
            }
        }
    }

}

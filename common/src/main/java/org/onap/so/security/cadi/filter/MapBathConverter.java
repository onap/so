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

package org.onap.so.security.cadi.filter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.Symm;
import org.onap.so.security.cadi.util.CSV;
import org.onap.so.security.cadi.util.CSV.Visitor;
import org.onap.so.security.cadi.util.Holder;

/**
 * This Filter is designed to help MIGRATE users from systems that don't match the FQI style.
 *
 * Style 1, where just the ID is translated, i.e. OLD => new@something.onap.org, that is acceptable longer term, because
 * it does not store Creds locally. The passwords are in appropriate systems, but it's still painful operationally,
 * though it does ease migration.
 *
 * Style 3, however, which is Direct match of Authorization Header to replacement, is only there because some passwords
 * are simply not acceptable for AAF, (too easy, for instance), and it is not feasible to break Organization Password
 * rules for a Migration. Therefore, this method should not considered something that is in any way a permanent
 *
 *
 * 
 * It goes without saying that any file with the password conversion should be protected by "400", etc.
 *
 * @author Instrumental (Jonathan)
 *
 */
public class MapBathConverter {
    private static final String BASIC = "Basic ";
    private final Map<String, String> map;

    /**
     * Create with colon separated name value pairs Enter the entire "Basic dXNlcjpwYXNz" "Authorization" header, where
     * "dXNlcjpwYXNz" is base64 encoded, which can be created with "cadi" tool (in jar)
     *
     * The replacement should also be an exact replacement of what you want. Recognize that this should be TEMPORARY as
     * you are storing credentials outside the users control.
     *
     * @param value
     * @throws IOException
     * @throws CadiException
     */
    public MapBathConverter(final Access access, final CSV csv) throws IOException, CadiException {
        map = new TreeMap<>();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final Date now = new Date();
        csv.visit(new Visitor() {
            @Override
            public void visit(List<String> row) throws CadiException {
                if (row.size() < 3) {
                    throw new CadiException("CSV file " + csv
                            + " must have at least 2 Basic Auth columns and an Expiration Date(YYYY-MM-DD) in each row");
                }
                try {
                    Date date = sdf.parse(row.get(2));
                    String oldID = row.get(0);
                    String newID = row.get(1);
                    if (date.after(now)) {
                        if (!oldID.startsWith(BASIC) && newID.startsWith(BASIC)) {
                            throw new CadiException(
                                    "CSV file " + csv + ": Uncredentialed ID " + idFromBasic(oldID, null)
                                            + " may not transfer to credentialed ID " + idFromBasic(newID, null));
                        } else {
                            map.put(oldID, newID);
                            access.printf(Level.INIT, "ID Conversion from %s to %s enabled", idFromBasic(oldID, null),
                                    idFromBasic(newID, null));
                        }
                    } else {
                        access.printf(Level.INIT, "ID Conversion from %s to %s has expired.", idFromBasic(oldID, null),
                                idFromBasic(newID, null));
                    }
                } catch (ParseException e) {
                    throw new CadiException("Cannot Parse Date: " + row.get(2));
                } catch (IOException e) {
                    throw new CadiException(e);
                }
            }
        });
    }

    private static String idFromBasic(String bath, Holder<String> hpass) throws IOException, CadiException {
        if (bath.startsWith(BASIC)) {
            String cred = Symm.base64noSplit.decode(bath.substring(6));
            int colon = cred.indexOf(':');
            if (colon < 0) {
                throw new CadiException("Invalid Authentication Credential for " + cred);
            }
            if (hpass != null) {
                hpass.set(cred.substring(colon + 1));
            }
            return cred.substring(0, colon);
        } else {
            return bath;
        }
    }

    /**
     * use to instantiate entries
     *
     * @return
     */
    public Map<String, String> map() {
        return map;
    }

    public String convert(Access access, final String bath) {
        String rv = map.get(bath);

        String cred;
        String tcred = null;
        Holder<String> hpass = null;
        try {
            if (bath.startsWith(BASIC)) {
                cred = idFromBasic(bath, (hpass = new Holder<String>(null)));
                if (rv == null) {
                    rv = map.get(cred);
                }
            } else {
                cred = bath;
            }

            if (rv == null) {
                // Nothing here, just return original
                rv = bath;
            } else {
                if (rv.startsWith(BASIC)) {
                    tcred = idFromBasic(rv, null);
                } else {
                    if (hpass != null) {
                        tcred = rv;
                        rv = BASIC + Symm.base64noSplit.encode(rv + ':' + hpass.get());
                    }
                }
                if (tcred != null) {
                    access.printf(Level.AUDIT, "ID %s converted to %s", cred, tcred);
                }
            }
        } catch (IOException | CadiException e) {
            access.log(e, "Invalid Authorization");
        }
        return rv == null ? bath : rv;
    }
}

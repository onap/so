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

package org.onap.so.security.cadi.config;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.onap.so.security.cadi.AbsUserCache;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.CachingLur;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.Connector;
import org.onap.so.security.cadi.CredVal;
import org.onap.so.security.cadi.CredValDomain;
import org.onap.so.security.cadi.Locator;
import org.onap.so.security.cadi.LocatorException;
import org.onap.so.security.cadi.Lur;
import org.onap.so.security.cadi.PropAccess;
import org.onap.so.security.cadi.Symm;
import org.onap.so.security.cadi.TrustChecker;
import org.onap.so.security.cadi.lur.EpiLur;
import org.onap.so.security.cadi.lur.LocalLur;
import org.onap.so.security.cadi.lur.NullLur;
import org.onap.so.security.cadi.taf.HttpEpiTaf;
import org.onap.so.security.cadi.taf.HttpTaf;
import org.onap.so.security.cadi.taf.basic.BasicHttpTaf;
import org.onap.so.security.cadi.taf.cert.X509Taf;
import org.onap.so.security.cadi.taf.dos.DenialOfServiceTaf;
import org.onap.so.security.cadi.util.FixURIinfo;
import org.onap.so.security.cadi.util.Split;

/**
 * Create a Consistent Configuration mechanism, even when configuration styles are as vastly different as Properties vs
 * JavaBeans vs FilterConfigs...
 *
 * @author Jonathan
 *
 */
public class Config {

    private static final String AAF_V2_0 = "org.onap.so.security.cadi.aaf.v2_0";
    private static final String AAF_V2_0_AAFCON = AAF_V2_0 + ".AAFCon";
    private static final String AAF_V2_0_AAF_LUR_PERM = AAF_V2_0 + ".AAFLurPerm";
    public static final String AAF_V2_0_AAF_CON_HTTP = AAF_V2_0 + ".AAFConHttp";

    private static final String OAUTH = "org.onap.auth.oauth";
    private static final String OAUTH_TOKEN_MGR = OAUTH + ".TokenMgr";
    private static final String OAUTH_HTTP_TAF = OAUTH + ".OAuth2HttpTaf";
    private static final String OAUTH_DIRECT_TAF = OAUTH + ".OAuthDirectTAF";
    public static final String UTF_8 = "UTF-8";

    // Property Names associated with configurations.
    // As of 1.0.2, these have had the dots removed so as to be compatible with
    // JavaBean style
    // configurations as well as property list style.
    public static final String HOSTNAME = "hostname";
    public static final String CADI_PROP_FILES = "cadi_prop_files"; // Additional Properties files (separate with ;)
    public static final String CADI_LOGLEVEL = "cadi_loglevel";
    public static final String CADI_LOGDIR = "cadi_log_dir";
    public static final String CADI_ETCDIR = "cadi_etc_dir";
    public static final String CADI_LOGNAME = "cadi_logname";
    // public static final String CADI_LOGFMT="cad_logging_format";
    // public static final String CADI_LOGFMT_UTC="UTC";
    // public static final String CADI_LOGFMT_ISO8601="ISO-8601";
    public static final String CADI_KEYFILE = "cadi_keyfile";
    public static final String CADI_KEYSTORE = "cadi_keystore";
    public static final String CADI_KEYSTORE_PASSWORD = "cadi_keystore_password";
    public static final String CADI_ALIAS = "cadi_alias";
    public static final String CADI_CLIENT_ALIAS = "cadi_client_alias";
    public static final String CADI_LOGINPAGE_URL = "cadi_loginpage_url";
    public static final String CADI_LATITUDE = "cadi_latitude";
    public static final String CADI_LONGITUDE = "cadi_longitude";

    public static final String CADI_KEY_PASSWORD = "cadi_key_password";
    public static final String CADI_TRUSTSTORE = "cadi_truststore";
    public static final String CADI_TRUSTSTORE_PASSWORD = "cadi_truststore_password";
    public static final String CADI_X509_ISSUERS = "cadi_x509_issuers";
    public static final String CADI_TRUST_MASKS = "cadi_trust_masks";
    public static final String CADI_TRUST_PERM = "cadi_trust_perm"; // IDs with this perm can utilize the "AS " user
                                                                    // concept
    public static final String CADI_PROTOCOLS = "cadi_protocols";
    public static final String CADI_NOAUTHN = "cadi_noauthn";
    public static final String CADI_LOC_LIST = "cadi_loc_list";

    // Special Behaviors
    public static final String CADI_BATH_CONVERT = "cadi_bath_convert";
    public static final String CADI_API_ENFORCEMENT = "cadi_api_enforcement";
    public static final String CADI_ADD_TAFS = "cadi_add_tafs";
    public static final String CADI_ADD_LURS = "cadi_add_lurs";

    public static final String CADI_USER_CHAIN_TAG = "cadi_user_chain";
    public static final String CADI_USER_CHAIN = "USER_CHAIN";

    public static final String CADI_OAUTH2_URL = "cadi_oauth2_url";
    public static final String CADI_TOKEN_DIR = "cadi_token_dir";

    public static final String HTTPS_PROTOCOLS = "https.protocols";
    public static final String HTTPS_CLIENT_PROTOCOLS = "jdk.tls.client.protocols";
    public static final String HTTPS_PROTOCOLS_DEFAULT = "TLSv1.1,TLSv1.2";
    public static final String HTTPS_CIPHER_SUITES = "https.cipherSuites";
    public static final String HTTPS_CIPHER_SUITES_DEFAULT =
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,"
                    + "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,"
                    + "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,"
                    + "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,TLS_ECDHE_RSA_WITH_RC4_128_SHA,TLS_ECDH_ECDSA_WITH_RC4_128_SHA,"
                    + "TLS_ECDH_RSA_WITH_RC4_128_SHA,TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,"
                    + "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA,TLS_EMPTY_RENEGOTIATION_INFO_SCSV";

    public static final String LOCALHOST_ALLOW = "localhost_allow";
    public static final String LOCALHOST_DENY = "localhost_deny";

    public static final String BASIC_REALM = "basic_realm"; // what is sent to the client
    public static final String BASIC_WARN = "basic_warn"; // Warning of insecure channel
    public static final String USERS = "local_users";
    public static final String GROUPS = "local_groups";
    public static final String WRITE_TO = "local_writeto"; // dump RBAC to local file in Tomcat Style (some apps use)

    public static final String OAUTH_CLIENT_ID = "client_id";
    public static final String OAUTH_CLIENT_SECRET = "client_secret";

    public static final String AAF_ENV = "aaf_env";
    public static final String AAF_ROOT_NS = "aaf_root_ns";
    public static final String AAF_ROOT_NS_DEF = "org.osaaf.aaf";
    public static final String AAF_ROOT_COMPANY = "aaf_root_company";
    /**
     * Use Config.getAAFLocateUrl(access) to get correct property in/out of container
     */
    public static final String AAF_LOCATE_URL = "aaf_locate_url"; // URL for AAF locator
    public static final String AAF_LOCATE_URL_TAG = "AAF_LOCATE_URL"; // Name of Above for use in Config Variables.
    public static final String AAF_DEFAULT_API_VERSION = "2.1";
    public static final String AAF_DEPLOYED_VERSION = "aaf_deployed_version";
    public static final String AAF_API_VERSION = "aaf_api_version";
    public static final String AAF_URL = "aaf_url"; // URL for AAF... Use to trigger AAF configuration
    public static final String AAF_LOCATOR_CLASS = "aaf_locator_class";
    // AAF Locator Entries are ADDITIONAL entries, which also gives the Property
    // ability
    // to set these entries manually
    // example: adding a K8S name like "oom"
    // this will allow Registrations to pick up
    // locator_ns.oom for onap's "OOM" based k8s entries, etc.
    public static final String AAF_LOCATOR_CONTAINER = "aaf_locator_container";
    // An ID for another Container, to be used to avoid picking up the wrong
    // internal info
    // for another container.
    public static final String AAF_LOCATOR_CONTAINER_ID = "aaf_locator_container_id";
    public static final String AAF_LOCATOR_CONTAINER_NS = "aaf_locator_container_ns";
    public static final String AAF_LOCATOR_VERSION = "aaf_locator_version";
    public static final String AAF_LOCATOR_PROTOCOL = "aaf_locator_protocol";
    public static final String AAF_LOCATOR_SUBPROTOCOL = "aaf_locator_subprotocol";
    public static final String AAF_LOCATOR_APP_NS = "aaf_locator_app_ns";
    public static final String AAF_LOCATOR_ENTRIES = "aaf_locator_entries";
    public static final String AAF_LOCATOR_FQDN = "aaf_locator_fqdn";
    public static final String AAF_LOCATOR_NAME = "aaf_locator_name";
    public static final String AAF_LOCATOR_PUBLIC_PORT = "aaf_locator_public_port";
    public static final String AAF_LOCATOR_PUBLIC_FQDN = "aaf_locator_public_fqdn";
    public static final String AAF_LOCATOR_PUBLIC_NAME = "aaf_locator_public_name";

    // AAF Service will write to the Audit Log if a past due AAF stored Password
    // is being used within # of days specified.
    public static final String AAF_CRED_WARN_DAYS = "aaf_cred_warn_days";
    public static final String AAF_CRED_WARN_DAYS_DFT = "7";

    public static final String AAF_APPID = "aaf_id";
    public static final String AAF_APPPASS = "aaf_password";
    public static final String AAF_LUR_CLASS = "aaf_lur_class";
    public static final String AAF_TAF_CLASS = "aaf_taf_class";
    public static final String AAF_CONNECTOR_CLASS = "aaf_connector_class";
    public static final String AAF_CONN_TIMEOUT = "aaf_conn_timeout";
    public static final String AAF_CONN_TIMEOUT_DEF = "3000";
    public static final String AAF_CONN_IDLE_TIMEOUT = "aaf_conn_idle_timeout"; // only for Direct Jetty Access.
    public static final String AAF_CONN_IDLE_TIMEOUT_DEF = "10000"; // only for Direct Jetty Access.

    // Default Classes: These are for Class loading to avoid direct compile links
    public static final String AAF_TAF_CLASS_DEF = "org.onap.so.security.cadi.aaf.v2_0.AAFTaf";
    public static final String AAF_LOCATOR_CLASS_DEF = "org.onap.so.security.cadi.aaf.v2_0.AAFLocator";
    public static final String AAF_LOCATOR_CLASS_SINGLE = "org.onap.so.security.cadi.locator.SingleEndpointLocator";

    public static final String CADI_OLUR_CLASS_DEF = "org.onap.so.security.cadi.olur.OLur";
    public static final String CADI_OBASIC_HTTP_TAF_DEF = "org.onap.so.security.cadi.obasic.OBasicHttpTaf";
    public static final String CADI_AAF_CON_DEF = "org.onap.so.security.cadi.aaf.v2_0.AAFCon";

    public static final String AAF_CALL_TIMEOUT = "aaf_timeout";
    public static final String AAF_CALL_TIMEOUT_DEF = "5000";
    public static final String AAF_USER_EXPIRES = "aaf_user_expires";
    public static final String AAF_USER_EXPIRES_DEF = "600000"; // Default is 10 mins
    public static final String AAF_CLEAN_INTERVAL = "aaf_clean_interval";
    public static final String AAF_CLEAN_INTERVAL_DEF = "30000"; // Default is 30 seconds
    public static final String AAF_REFRESH_TRIGGER_COUNT = "aaf_refresh_trigger_count";
    public static final String AAF_REFRESH_TRIGGER_COUNT_DEF = "3"; // Default is 10 mins

    public static final String AAF_HIGH_COUNT = "aaf_high_count";
    public static final String AAF_HIGH_COUNT_DEF = "1000"; // Default is 1000 entries
    public static final String AAF_PERM_MAP = "aaf_perm_map";
    // public static final String AAF_COMPONENT = "aaf_component";
    public static final String AAF_CERT_IDS = "aaf_cert_ids";
    public static final String AAF_DEBUG_IDS = "aaf_debug_ids"; // comma delimited
    public static final String AAF_DATA_DIR = "aaf_data_dir"; // AAF processes and Components only.

    public static final String AAF_URL_OAUTH = "aaf_url_oauth";
    public static final String AAF_URL_GUI = "aaf_url_gui";
    public static final String AAF_URL_FS = "aaf_url_fs";
    public static final String AAF_URL_CM = "aaf_url_cm";
    public static final String AAF_URL_CM_DEF = "https://AAF_LOCATE_URL/AAF_NS.cm:" + AAF_DEFAULT_API_VERSION;
    public static final String AAF_URL_HELLO = "aaf_url_hello";
    public static final String CM_TRUSTED_CAS = "cm_trusted_cas";
    // let NS Owners choose with <ns>.certman aaf ignoreIPs" to ignoreIP Check for
    // Configs
    // Probably only want to allow in a DEV Env.
    public static final String CM_ALLOW_IGNORE_IPS = "cm_allow_ignore_ips";
    // Docker doesn't have a default DNS. The property turns off IP Checking of DNSs
    // before creating.
    public static final String CM_ALWAYS_IGNORE_IPS = "cm_always_ignore_ips";

    public static final String PATHFILTER_URLPATTERN = "pathfilter_urlpattern";
    public static final String PATHFILTER_STACK = "pathfilter_stack";
    public static final String PATHFILTER_NS = "pathfilter_ns";
    public static final String PATHFILTER_NOT_AUTHORIZED_MSG = "pathfilter_not_authorized_msg";

    // This one should go unpublic
    public static final String AAF_DEFAULT_REALM = "aaf_default_realm";
    private static String defaultRealm = "people.osaaf.org";

    public static final String AAF_DOMAIN_SUPPORT = "aaf_domain_support";
    public static final String AAF_DOMAIN_SUPPORT_DEF = ".com:.org";

    // OAUTH2
    public static final String AAF_OAUTH2_TOKEN_URL = "aaf_oauth2_token_url";
    public static final String AAF_OAUTH2_INTROSPECT_URL = "aaf_oauth2_introspect_url";
    public static final String AAF_ALT_OAUTH2_TOKEN_URL = "aaf_alt_oauth2_token_url";
    public static final String AAF_ALT_OAUTH2_INTROSPECT_URL = "aaf_alt_oauth2_introspect_url";
    public static final String AAF_ALT_OAUTH2_DOMAIN = "aaf_alt_oauth2_domain";
    public static final String AAF_ALT_CLIENT_ID = "aaf_alt_oauth2_client_id";
    public static final String AAF_ALT_CLIENT_SECRET = "aaf_alt_oauth2_client_secret";
    public static final String AAF_OAUTH2_HELLO_URL = "aaf_oauth2_hello_url";

    public static void setDefaultRealm(Access access) {
        try {
            defaultRealm = logProp(access, Config.AAF_DEFAULT_REALM, logProp(access, Config.BASIC_REALM,
                    logProp(access, HOSTNAME, InetAddress.getLocalHost().getHostName())));
        } catch (UnknownHostException e) {
            access.log(Level.INIT, "Unable to determine Hostname", e);
        }
    }

    public static HttpTaf configHttpTaf(Connector con, SecurityInfoC<HttpURLConnection> si, TrustChecker tc, CredVal up,
            Lur lur, Object... additionalTafLurs) throws CadiException, LocatorException {
        Access access = si.access;
        RegistrationPropHolder rph;
        try {
            rph = new RegistrationPropHolder(access, 0);
        } catch (UnknownHostException e2) {
            throw new CadiException(e2);
        }
        /////////////////////////////////////////////////////
        // Setup AAFCon for any following
        /////////////////////////////////////////////////////
        Class<?> aafConClass = loadClass(access, CADI_AAF_CON_DEF);
        Object aafcon = null;
        if (con != null && aafConClass != null && aafConClass.isAssignableFrom(con.getClass())) {
            aafcon = con;
        } else if (lur != null) {
            Field f;
            try {
                f = lur.getClass().getField("aaf");
                aafcon = f.get(lur);
            } catch (Exception e) {
                access.log(Level.INIT, e);
            }
        }

        boolean hasDirectAAF = hasDirect("DirectAAFLur", additionalTafLurs);
        // IMPORTANT! Don't attempt to load AAF Connector if there is no AAF URL
        String aafURL = logProp(rph, AAF_URL, null);
        if (!hasDirectAAF && aafcon == null && aafURL != null) {
            aafcon = loadAAFConnector(si, aafURL);
        }

        HttpTaf taf;
        // Setup Host, in case Network reports an unusable Hostname (i.e. VTiers, VPNs,
        // etc)
        String hostname = logProp(access, HOSTNAME, null);
        if (hostname == null) {
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e1) {
                throw new CadiException("Unable to determine Hostname", e1);
            }
        }

        access.log(Level.INIT, "Hostname set to", hostname);
        // Get appropriate TAFs
        ArrayList<Priori<HttpTaf>> htlist = new ArrayList<>();

        /////////////////////////////////////////////////////
        // Add a Denial of Service TAF
        // Note: how IPs and IDs are added are up to service type.
        // They call "DenialOfServiceTaf.denyIP(String) or denyID(String)
        /////////////////////////////////////////////////////
        htlist.add(new Priori<HttpTaf>(new DenialOfServiceTaf(access), 0));

        /////////////////////////////////////////////////////
        // Configure Client Cert TAF
        /////////////////////////////////////////////////////
        X509Taf x509TAF = null;
        String truststore = logProp(access, CADI_TRUSTSTORE, null);
        if (truststore != null) {
            String truststorePwd = access.getProperty(CADI_TRUSTSTORE_PASSWORD, null);
            if (truststorePwd != null) {
                if (truststorePwd.startsWith(Symm.ENC)) {
                    try {
                        access.decrypt(truststorePwd, false);
                    } catch (IOException e) {
                        throw new CadiException(CADI_TRUSTSTORE_PASSWORD + " cannot be decrypted", e);
                    }
                }
                try {
                    x509TAF = new X509Taf(access, lur);
                    htlist.add(new Priori<HttpTaf>(x509TAF, 10));
                    access.log(Level.INIT, "Certificate Authorization enabled");
                } catch (SecurityException | IllegalArgumentException e) {
                    access.log(Level.INIT,
                            "AAFListedCertIdentity cannot be instantiated. Certificate Authorization is now disabled",
                            e);
                } catch (CertificateException e) {
                    access.log(Level.INIT, "Certificate Authorization failed, it is disabled", e);
                } catch (NoSuchAlgorithmException e) {
                    access.log(Level.INIT, "Certificate Authorization failed, wrong Security Algorithm", e);
                }
            }
        } else {
            access.log(Level.INIT, "Certificate Authorization not enabled");
        }

        /////////////////////////////////////////////////////
        // Configure Basic Auth (local content)
        /////////////////////////////////////////////////////
        boolean hasOAuthDirectTAF = hasDirect("DirectOAuthTAF", additionalTafLurs);
        String basicRealm = logProp(access, BASIC_REALM, null);
        String aafCleanup = logProp(access, AAF_USER_EXPIRES, AAF_USER_EXPIRES_DEF); // Default is 10 mins
        long userExp = Long.parseLong(aafCleanup);
        boolean basicWarn = "TRUE".equals(access.getProperty(BASIC_WARN, "FALSE"));

        if (!hasDirectAAF) {
            HttpTaf aaftaf = null;
            if (!hasOAuthDirectTAF) {
                if (basicRealm != null) {
                    @SuppressWarnings("unchecked")
                    Class<HttpTaf> obasicCls = (Class<HttpTaf>) loadClass(access, CADI_OBASIC_HTTP_TAF_DEF);
                    if (obasicCls != null) {
                        try {
                            String tokenurl = logProp(rph, Config.AAF_OAUTH2_TOKEN_URL, null);
                            String introspecturl = logProp(rph, Config.AAF_OAUTH2_INTROSPECT_URL, null);
                            if (tokenurl == null || introspecturl == null) {
                                access.log(Level.INIT,
                                        "Both tokenurl and introspecturl are required. Oauth Authorization is disabled.");
                            } else {
                                // try to construct the TAF instance. Try without the CredVal first (original code),
                                // change
                                // to try with a CredVal paramater if it fails as the newer ONAP code contains this in
                                // the OBasicHttpTaf constructor
                                System.out.println("TokenURL=" + tokenurl + "; IntrospectURL=" + introspecturl);
                                Constructor<HttpTaf> obasicConst = null;
                                try {
                                    obasicConst = obasicCls.getConstructor(PropAccess.class, String.class, String.class,
                                            String.class);
                                    htlist.add(new Priori<HttpTaf>(
                                            obasicConst.newInstance(access, basicRealm, tokenurl, introspecturl), 20));
                                } catch (Exception e) {
                                    obasicConst = obasicCls.getConstructor(PropAccess.class, CredVal.class,
                                            String.class, String.class, String.class);
                                    htlist.add(new Priori<HttpTaf>(
                                            obasicConst.newInstance(access, up, basicRealm, tokenurl, introspecturl),
                                            20));
                                }

                                access.log(Level.INIT, "Oauth supported Basic Authorization is enabled");
                            }
                        } catch (NoSuchMethodException | SecurityException | InstantiationException
                                | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            access.log(Level.INIT, e);
                        }
                    } else if (up != null) {
                        access.log(Level.INIT, "Basic Authorization is enabled using realm", basicRealm);
                        // Allow warning about insecure channel to be turned off
                        if (!basicWarn) {
                            access.log(Level.INIT, "WARNING! The basicWarn property has been set to false.",
                                    " There will be no additional warning if Basic Auth is used on an insecure channel");
                        }
                        BasicHttpTaf bht = new BasicHttpTaf(access, up, basicRealm, userExp, basicWarn);
                        for (Object o : additionalTafLurs) {
                            if (o instanceof CredValDomain) {
                                bht.add((CredValDomain) o);
                            }
                        }
                        if (x509TAF != null) {
                            x509TAF.add(bht);
                        }
                        htlist.add(new Priori<HttpTaf>(bht, 20));
                        access.log(Level.INIT, "Basic Authorization is enabled");
                    }
                } else {
                    access.log(Level.INIT,
                            "Local Basic Authorization is disabled.  Enable by setting basicRealm=<appropriate realm, i.e. my.att.com>");
                }

                /////////////////////////////////////////////////////
                // Configure AAF Driven Basic Auth
                /////////////////////////////////////////////////////
                if (aafcon == null) {
                    access.log(Level.INIT, "AAF Connection (AAFcon) is null.  Cannot create an AAF TAF");
                } else if (aafURL == null) {
                    access.log(Level.INIT, "No AAF URL in properties, Cannot create an AAF TAF");
                } else {// There's an AAF_URL... try to configure an AAF
                    String aafTafClassName = logProp(access, AAF_TAF_CLASS, AAF_TAF_CLASS_DEF);
                    // Only 2.0 available at this time
                    if (AAF_TAF_CLASS_DEF.equals(aafTafClassName)) {
                        try {
                            Class<?> aafTafClass = loadClass(access, aafTafClassName);
                            if (aafTafClass != null) {
                                Constructor<?> cstr =
                                        aafTafClass.getConstructor(Connector.class, boolean.class, AbsUserCache.class);
                                if (cstr != null) {
                                    if (lur instanceof AbsUserCache) {
                                        aaftaf = (HttpTaf) cstr.newInstance(aafcon, basicWarn, lur);
                                    } else {
                                        cstr = aafTafClass.getConstructor(Connector.class, boolean.class);
                                        if (cstr != null) {
                                            aaftaf = (HttpTaf) cstr.newInstance(aafcon, basicWarn);
                                        }
                                    }
                                    if (aaftaf == null) {
                                        access.log(Level.INIT, "ERROR! AAF TAF Failed construction.  NOT Configured");
                                    } else {
                                        access.log(Level.INIT, "AAF TAF Configured to ", aafURL);
                                        // Note: will add later, after all others configured
                                    }
                                }
                            } else {
                                access.log(Level.INIT,
                                        "There is no AAF TAF class available: %s. AAF TAF not configured.",
                                        aafTafClassName);
                            }
                        } catch (Exception e) {
                            access.log(Level.INIT, "ERROR! AAF TAF Failed construction.  NOT Configured", e);
                        }
                    }
                }
            }

            /////////////////////////////////////////////////////
            // Configure OAuth TAF
            /////////////////////////////////////////////////////
            if (!hasOAuthDirectTAF) {
                String oauthTokenUrl = logProp(rph, Config.AAF_OAUTH2_TOKEN_URL, null);
                Class<?> oadtClss;
                try {
                    oadtClss = Class.forName(OAUTH_DIRECT_TAF);
                } catch (ClassNotFoundException e1) {
                    oadtClss = null;
                    access.log(Level.DEBUG, e1);
                }
                if (additionalTafLurs != null && additionalTafLurs.length > 0
                        && (oadtClss != null && additionalTafLurs[0].getClass().isAssignableFrom(oadtClss))) {
                    htlist.add(new Priori<HttpTaf>((HttpTaf) additionalTafLurs[0], 30));
                    String[] array = new String[additionalTafLurs.length - 1];
                    if (array.length > 0) {
                        System.arraycopy(htlist, 1, array, 0, array.length);
                    }
                    additionalTafLurs = array;
                    access.log(Level.INIT, "OAuth2 Direct is enabled");
                } else if (oauthTokenUrl != null) {
                    String oauthIntrospectUrl = logProp(rph, Config.AAF_OAUTH2_INTROSPECT_URL, null);
                    @SuppressWarnings("unchecked")
                    Class<HttpTaf> oaTCls = (Class<HttpTaf>) loadClass(access, OAUTH_HTTP_TAF);
                    if (oaTCls != null) {
                        Class<?> oaTTmgrCls = loadClass(access, OAUTH_TOKEN_MGR);
                        if (oaTTmgrCls != null) {
                            try {
                                Method oaTTmgrGI = oaTTmgrCls.getMethod("getInstance", PropAccess.class, String.class,
                                        String.class);
                                Object oaTTmgr = oaTTmgrGI.invoke(null /* this is static method */, access,
                                        oauthTokenUrl, oauthIntrospectUrl);
                                Constructor<HttpTaf> oaTConst = oaTCls.getConstructor(Access.class, oaTTmgrCls);
                                htlist.add(new Priori<HttpTaf>(oaTConst.newInstance(access, oaTTmgr), 30));
                                access.log(Level.INIT, "OAuth2 TAF is enabled");
                            } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                                    | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                                access.log(Level.INIT, "OAuth2HttpTaf cannot be instantiated. OAuth2 is disabled", e);
                            }
                        }
                    }
                } else {
                    access.log(Level.INIT, "OAuth TAF is not configured");
                }
            }

            /////////////////////////////////////////////////////
            // Adding BasicAuth (AAF) last, after other primary Cookie Based
            // Needs to be before Cert... see below
            /////////////////////////////////////////////////////
            if (aaftaf != null) {
                htlist.add(new Priori<HttpTaf>(aaftaf, 40));
            }
        }

        /////////////////////////////////////////////////////
        // Any Additional Tafs passed in Constructor
        /////////////////////////////////////////////////////
        if (additionalTafLurs != null) {
            int i = 0;
            for (Object additional : additionalTafLurs) {
                if (additional instanceof BasicHttpTaf) {
                    BasicHttpTaf ht = (BasicHttpTaf) additional;
                    for (Object cv : additionalTafLurs) {
                        if (cv instanceof CredValDomain) {
                            ht.add((CredValDomain) cv);
                            access.printf(Level.INIT, "%s Authentication is enabled", cv);
                        }
                    }
                    htlist.add(new Priori<HttpTaf>(ht, 50 + i++));
                } else if (additional instanceof HttpTaf) {
                    HttpTaf ht = (HttpTaf) additional;
                    htlist.add(new Priori<HttpTaf>(ht, 50 + i++));
                    access.printf(Level.INIT, "%s Authentication is enabled", additional.getClass().getSimpleName());
                } else if (hasOAuthDirectTAF) {
                    Class<?> daupCls;
                    try {
                        daupCls = Class.forName("org.onap.so.auth.direct.DirectAAFUserPass");
                    } catch (ClassNotFoundException e) {
                        daupCls = null;
                        access.log(Level.INIT, e);
                    }
                    if (daupCls != null && additional.getClass().isAssignableFrom(daupCls)) {
                        htlist.add(new Priori<HttpTaf>(
                                new BasicHttpTaf(access, (CredVal) additional, basicRealm, userExp, basicWarn),
                                50 + i++));
                        access.printf(Level.INIT, "Direct BasicAuth Authentication is enabled",
                                additional.getClass().getSimpleName());
                    }
                }
            }
        }

        // Add BasicAuth, if any, to x509Taf
        if (x509TAF != null) {
            for (Priori<HttpTaf> ht : htlist) {
                if (ht.t instanceof BasicHttpTaf) {
                    x509TAF.add((BasicHttpTaf) ht.t);
                }
            }
        }

        /////////////////////////////////////////////////////
        // Additional TAFs by Plugin
        /////////////////////////////////////////////////////
        Priori.add(access, CADI_ADD_TAFS, htlist);

        /////////////////////////////////////////////////////
        // Create EpiTaf from configured TAFs
        /////////////////////////////////////////////////////
        if (htlist.size() == 1) {
            // just return the one
            taf = htlist.get(0).t;
        } else {
            Collections.sort(htlist);
            HttpTaf[] htarray = new HttpTaf[htlist.size()];
            int i = -1;
            StringBuilder sb = new StringBuilder("Tafs processed in this order:\n");
            for (Priori<HttpTaf> pht : htlist) {
                htarray[++i] = pht.t;
                sb.append("    ");
                sb.append(pht.t.getClass().getName());
                sb.append('(');
                sb.append(pht.priority);
                sb.append(")\n");
            }
            access.log(Level.INIT, sb);

            Locator<URI> locator = loadLocator(si, aafURL);

            taf = new HttpEpiTaf(access, locator, tc, htarray); // ok to pass locator == null
            String level = logProp(access, CADI_LOGLEVEL, null);
            if (level != null) {
                access.setLogLevel(Level.valueOf(level));
            }
        }

        return taf;
    }

    public static String logProp(RegistrationPropHolder rph, String tag, String def) {
        String rv = rph.access().getProperty(tag, def);
        if (rv == null) {
            rph.access().log(Level.INIT, tag, "is not explicitly set");
        } else {
            rv = rph.replacements("Config.logProp", rv, null, null);
            rph.access().log(Level.INIT, tag, "is set to", rv);
        }
        return rv;

    }

    public static String logProp(Access access, String tag, String def) {
        String rv = access.getProperty(tag, def);
        if (rv == null) {
            access.log(Level.INIT, tag, "is not explicitly set");
        } else {
            access.log(Level.INIT, tag, "is set to", rv);
        }
        return rv;
    }

    public static Lur configLur(SecurityInfoC<HttpURLConnection> si, Connector con, Object... additionalTafLurs)
            throws CadiException {
        Access access = si.access;
        RegistrationPropHolder rph;
        try {
            rph = new RegistrationPropHolder(access, 0);
        } catch (UnknownHostException e2) {
            throw new CadiException(e2);
        }

        List<Priori<Lur>> lurs = new ArrayList<>();

        /////////////////////////////////////////////////////
        // Configure a Local Property Based RBAC/LUR
        /////////////////////////////////////////////////////
        try {
            String users = access.getProperty(USERS, null);
            String groups = access.getProperty(GROUPS, null);

            if (groups != null || users != null) {
                LocalLur ll = new LocalLur(access, users, groups); // note b64==null is ok.. just means no encryption.
                lurs.add(new Priori<Lur>(ll, 10));

                String writeto = access.getProperty(WRITE_TO, null);
                if (writeto != null) {
                    String msg = UsersDump.updateUsers(writeto, ll);
                    if (msg != null) {
                        access.log(Level.INIT, "ERROR! Error Updating ", writeto, "with roles and users:", msg);
                    }
                }
            }
        } catch (IOException e) {
            throw new CadiException(e);
        }

        /////////////////////////////////////////////////////
        // Configure the OAuth Lur (if any)
        /////////////////////////////////////////////////////
        String tokenUrl = logProp(rph, AAF_OAUTH2_TOKEN_URL, null);
        String introspectUrl = logProp(rph, AAF_OAUTH2_INTROSPECT_URL, null);
        if (tokenUrl != null && introspectUrl != null) {
            try {
                Class<?> olurCls = loadClass(access, CADI_OLUR_CLASS_DEF);
                if (olurCls != null) {
                    Constructor<?> olurCnst = olurCls.getConstructor(PropAccess.class, String.class, String.class);
                    Lur olur = (Lur) olurCnst.newInstance(access, tokenUrl, introspectUrl);
                    lurs.add(new Priori<Lur>(olur, 20));
                    access.log(Level.INIT, "OAuth2 LUR enabled");
                } else {
                    access.log(Level.INIT, "AAF/OAuth LUR plugin is not available.");
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                String msg = e.getMessage();
                if (msg == null && e.getCause() != null) {
                    msg = e.getCause().getMessage();
                }
                access.log(Level.INIT, "AAF/OAuth LUR is not instantiated.", msg, e);
            }
        } else {
            access.log(Level.INIT, "OAuth2 Lur disabled");
        }

        if (con != null) { // try to reutilize connector
            lurs.add(new Priori<Lur>(con.newLur(), 30));
        } else {
            /////////////////////////////////////////////////////
            // Configure the AAF Lur (if any)
            /////////////////////////////////////////////////////
            String aafURL = logProp(rph, AAF_URL, null); // Trigger Property
            String aafEnv = access.getProperty(AAF_ENV, null);
            if (aafEnv == null && aafURL != null && access instanceof PropAccess) { // set AAF_ENV from AAF_URL
                int ec = aafURL.indexOf("envContext=");
                if (ec > 0) {
                    ec += 11; // length of envContext=
                    int slash = aafURL.indexOf('/', ec);
                    if (slash > 0) {
                        aafEnv = aafURL.substring(ec, slash);
                        ((PropAccess) access).setProperty(AAF_ENV, aafEnv);
                        access.printf(Level.INIT, "Setting aafEnv to %s from aaf_url value", aafEnv);
                    }
                }
            }

            // Don't configure AAF if it is using DirectAccess
            if (!hasDirect("DirectAAFLur", additionalTafLurs)) {
                if (aafURL == null) {
                    access.log(Level.INIT, "No AAF LUR properties, AAF will not be loaded");
                } else {// There's an AAF_URL... try to configure an AAF
                    String aafLurClassStr = logProp(access, AAF_LUR_CLASS, AAF_V2_0_AAF_LUR_PERM);
                    //////////// AAF Lur 2.0 /////////////
                    if (aafLurClassStr != null && aafLurClassStr.startsWith(AAF_V2_0)) {
                        try {
                            Object aafcon = loadAAFConnector(si, aafURL);
                            if (aafcon == null) {
                                access.log(Level.INIT, "AAF LUR class,", aafLurClassStr,
                                        "cannot be constructed without valid AAFCon object.");
                            } else {
                                Class<?> aafAbsAAFCon = loadClass(access, AAF_V2_0_AAFCON);
                                if (aafAbsAAFCon != null) {
                                    Method mNewLur = aafAbsAAFCon.getMethod("newLur");
                                    Object aaflur = mNewLur.invoke(aafcon);

                                    if (aaflur == null) {
                                        access.log(Level.INIT, "ERROR! AAF LUR Failed construction.  NOT Configured");
                                    } else {
                                        access.log(Level.INIT, "AAF LUR Configured to ", aafURL);
                                        lurs.add(new Priori<Lur>((Lur) aaflur, 40));
                                        String debugIDs = logProp(access, Config.AAF_DEBUG_IDS, null);
                                        if (debugIDs != null && aaflur instanceof CachingLur) {
                                            ((CachingLur<?>) aaflur).setDebug(debugIDs);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            access.log(e, "AAF LUR class,", aafLurClassStr,
                                    "could not be constructed with given Constructors.");
                        }
                    }
                }
            }
        }

        /////////////////////////////////////////////////////
        // Any Additional passed in Constructor
        /////////////////////////////////////////////////////
        if (additionalTafLurs != null) {
            int i = 0;
            for (Object additional : additionalTafLurs) {
                if (additional instanceof Lur) {
                    lurs.add(new Priori<Lur>((Lur) additional, 50 + i++));
                    access.log(Level.INIT, additional);
                }
            }
        }

        /////////////////////////////////////////////////////
        // Additional LURs by Plugin
        /////////////////////////////////////////////////////
        Priori.add(access, CADI_ADD_LURS, lurs);

        /////////////////////////////////////////////////////
        // Return a Lur based on how many there are...
        /////////////////////////////////////////////////////
        switch (lurs.size()) {
            case 0:
                access.log(Level.INIT, "WARNING! No CADI LURs configured");
                // Return a NULL Lur that does nothing.
                return new NullLur();
            case 1:
                return lurs.get(0).t; // Only one, just return it, save processing
            default:
                // Multiple Lurs, use EpiLUR to handle
                Collections.sort(lurs);
                Lur[] la = new Lur[lurs.size()];
                int i = -1;
                StringBuilder sb = new StringBuilder("Lurs processed in this order:\n");
                for (Priori<Lur> pht : lurs) {
                    la[++i] = pht.t;
                    sb.append("    ");
                    sb.append(pht.t.getClass().getName());
                    sb.append('(');
                    sb.append(pht.priority);
                    sb.append(")\n");
                }
                access.log(Level.INIT, sb);
                return new EpiLur(la);
        }
    }

    private static boolean hasDirect(String simpleClassName, Object[] additionalTafLurs) {
        if (additionalTafLurs != null) {
            for (Object tf : additionalTafLurs) {
                if (tf.getClass().getSimpleName().equals(simpleClassName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static Object loadAAFConnector(SecurityInfoC<?> si, String aafURL) {
        Access access = si.access;
        Object aafcon = null;
        Class<?> aafConClass = null;

        try {
            if (aafURL != null) {
                String aafConnector = access.getProperty(AAF_CONNECTOR_CLASS, AAF_V2_0_AAF_CON_HTTP);
                if (AAF_V2_0_AAF_CON_HTTP.equals(aafConnector)) {
                    aafConClass = loadClass(access, AAF_V2_0_AAF_CON_HTTP);
                    if (aafConClass != null) {
                        for (Constructor<?> c : aafConClass.getConstructors()) {
                            List<Object> lo = new ArrayList<>();
                            for (Class<?> pc : c.getParameterTypes()) {
                                if (pc.equals(Access.class)) {
                                    lo.add(access);
                                } else if (pc.equals(Locator.class)) {
                                    lo.add(loadLocator((SecurityInfoC<HttpURLConnection>) si, aafURL));
                                }
                            }
                            if (c.getParameterTypes().length != lo.size()) {
                                continue; // back to another Constructor
                            } else {
                                aafcon = c.newInstance(lo.toArray());
                            }
                            break;
                        }
                    }
                }
                if (aafcon != null) {
                    String mechid = logProp(access, Config.AAF_APPID, null);
                    String pass = access.getProperty(Config.AAF_APPPASS, null);
                    if (mechid != null && pass != null) {
                        try {
                            Method basicAuth = aafConClass.getMethod("basicAuth", String.class, String.class);
                            basicAuth.invoke(aafcon, mechid, pass);
                        } catch (NoSuchMethodException nsme) {
                            access.log(Level.NONE, nsme);
                            // it's ok, don't use
                        }
                    }
                }
            }
        } catch (Exception e) {
            access.log(e, "AAF Connector could not be constructed with given Constructors.");
        }

        return aafcon;
    }

    public static Class<?> loadClass(Access access, String className) {
        Class<?> cls = null;
        try {
            cls = access.classLoader().loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            access.log(Level.NONE, cnfe);
            try {
                cls = access.getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException cnfe2) {
                access.log(Level.NONE, cnfe2);
                // just return null
            }
        }
        return cls;
    }

    @SuppressWarnings("unchecked")
    public static Locator<URI> loadLocator(SecurityInfoC<HttpURLConnection> si, final String _url)
            throws LocatorException {
        Access access = si.access;
        Locator<URI> locator = null;
        if (_url == null) {
            access.log(Level.INIT, "No URL passed to 'loadLocator'. Disabled");
        } else {
            try {
                Class<?> aalCls = Class.forName("org.onap.so.security.cadi.aaf.v2_0.AbsAAFLocator");
                Method aalMth = aalCls.getMethod("create", String.class, String.class);
                int colon = _url.lastIndexOf(':');
                if (colon >= 0) {
                    int slash = _url.indexOf('/', colon);
                    String version;
                    if (slash < 0) {
                        version = _url.substring(colon + 1);
                    } else {
                        version = _url.substring(colon + 1, slash);
                    }
                    slash = _url.lastIndexOf('/', colon);
                    if (slash >= 0) {
                        Object aal = aalMth.invoke(null/* static */, _url.substring(slash + 1, colon), version);
                        return (Locator<URI>) aal;
                    }
                }
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                String msg;
                char quote;
                if (e.getCause() != null) {
                    msg = e.getCause().getMessage();
                    quote = '"';
                } else {
                    msg = "-";
                    quote = ' ';
                }
                access.printf(Level.DEBUG, "Configured AbsAAFLocator not found%c%s%cContinuing Locator creation ",
                        quote, msg, quote);
            }
            // String url = _url.replace("/AAF_NS.", "/%C%CID%AAF_NS.");
            // String root_ns = access.getProperty(Config.AAF_ROOT_NS, null);
            String url;
            RegistrationPropHolder rph;
            try {
                rph = new RegistrationPropHolder(access, 0);
                url = rph.replacements("Config.loadLocator", _url, null, null);
                access.printf(Level.INFO, "loadLocator URL is %s", url);
            } catch (UnknownHostException | CadiException e1) {
                throw new LocatorException(e1);
            }

            /**
             * Simplify logic - if we have a URL with /locate/ in it, we use the default locator. If we have an
             * explicitly set locator from configuration, we use that one. Otherwise we fall back to the
             * SingleEndpointLocator, basically default normal HTTP client behavior.
             */
            String aaf_locator_class = null;
            if (url.contains("/locate/")) {
                aaf_locator_class = AAF_LOCATOR_CLASS_DEF;
            } else if (si.access.getProperty(Config.AAF_LOCATOR_CLASS, null) != null) {
                aaf_locator_class = si.access.getProperty(Config.AAF_LOCATOR_CLASS, null);
            }
            if (aaf_locator_class == null) {
                aaf_locator_class = Config.AAF_LOCATOR_CLASS_SINGLE;
            }

            try {
                Class<?> lcls = loadClass(access, aaf_locator_class);
                if (lcls == null) {
                    throw new CadiException("Need to include aaf-cadi-aaf jar for AAFLocator");
                }
                // First check for preloaded
                try {
                    Method meth = lcls.getMethod("create", Access.class, String.class);
                    locator = (Locator<URI>) meth.invoke(null, access, url);
                } catch (Exception e) {
                    access.log(Level.NONE, "(Not fatal) Cannot load by create(String)", e);
                }
                if (locator == null) {
                    URI locatorURI = new URI(url);
                    FixURIinfo fui = new FixURIinfo(locatorURI);
                    Constructor<?> cnst = lcls.getConstructor(SecurityInfoC.class, URI.class);
                    locator = (Locator<URI>) cnst.newInstance(new Object[] {si, locatorURI});
                    int port = fui.getPort();
                    String portS = port < 0 ? "" : (":" + port);

                    access.log(Level.INFO, "AAFLocator [" + locator.getClass().getSimpleName() + "] enabled using "
                            + locatorURI.getScheme() + "://" + fui.getHost() + portS);
                } else {
                    access.log(Level.INFO,
                            "AAFLocator [" + locator.getClass().getSimpleName() + "] enabled using " + url);
                }
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof LocatorException) {
                    throw (LocatorException) e.getTargetException();
                }
                access.log(Level.INIT, e.getTargetException().getMessage(), "AAFLocator for", url,
                        "could not be created.", e);
            } catch (Exception e) {
                access.log(Level.INIT, "AAFLocator for", url, "could not be created.", e);
            }
        }
        return locator;
    }

    // Set by CSP, or is hostname.
    public static String getDefaultRealm() {
        return defaultRealm;
    }

    public static String getAAFLocateUrl(Access access) {
        String rv = null;
        String cont = access.getProperty(AAF_LOCATOR_CONTAINER, null);
        if (cont != null) {
            rv = access.getProperty(AAF_LOCATE_URL + '.' + cont, null);
        }
        if (rv == null) {
            rv = access.getProperty(AAF_LOCATE_URL, null);
        }
        return rv;
    }

    private static class Priori<T> implements Comparable<Priori<T>> {
        public final T t;
        public final int priority;

        public Priori(final T t, final int priority) {
            this.t = t;
            this.priority = priority;
        }

        @Override
        public int compareTo(Priori<T> o) {
            if (priority == o.priority) {
                return 0;
            } else if (priority < o.priority) {
                return -1;
            } else {
                return 1;
            }
        }

        public static <T> void add(Access access, final String tag, List<Priori<T>> list) {
            String plugins = access.getProperty(tag, null);
            if (plugins != null) {
                access.log(Level.INIT, "Adding TAF Plugins: ", plugins);
                for (String tafs : Split.splitTrim(';', plugins)) {
                    String[] pluginArray = Split.splitTrim(',', tafs);
                    String clssn = null;
                    int priority = 60;
                    switch (pluginArray.length) {
                        case 0:
                            break;
                        case 1:
                            clssn = tafs;
                            break;
                        default:
                            clssn = pluginArray[0];
                            try {
                                priority = Integer.parseInt(pluginArray[1]);
                            } catch (NumberFormatException nfe) {
                                access.printf(Level.ERROR, "%s format is <classname>,priority[;...]\n", CADI_ADD_TAFS);
                            }
                    }

                    if (clssn != null) {
                        Class<?> cls = loadClass(access, clssn);
                        if (cls != null) {
                            try {
                                @SuppressWarnings("unchecked")
                                Constructor<T> cnst = (Constructor<T>) cls.getConstructor(Access.class);
                                try {
                                    list.add(new Priori<T>(cnst.newInstance(access), priority));
                                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                        | InvocationTargetException e) {
                                    String hostname = access.getProperty(Config.HOSTNAME, null);
                                    if (hostname == null) {
                                        access.printf(Level.ERROR,
                                                "%s cannot be constructed on this machine.  Set valid 'hostname' in your properties\n",
                                                clssn);
                                    } else {
                                        access.printf(Level.ERROR, "%s cannot be constructed on %s with Access.\n",
                                                clssn, hostname);
                                    }
                                }
                            } catch (NoSuchMethodException | SecurityException e) {
                                access.printf(Level.ERROR, "%s needs a Constructor taking Access as sole param.\n",
                                        clssn);
                            }
                        }
                    }
                }
            }
        }
    }
}


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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import org.onap.so.security.cadi.Access;
import org.onap.so.security.cadi.CadiException;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.util.MaskFormatException;
import org.onap.so.security.cadi.util.NetMask;
import org.onap.so.security.cadi.util.Split;

public class SecurityInfo {
    private static final String SECURITY_ALGO = "RSA";
    private static final String HTTPS_PROTOCOLS = "https.protocols";
    private static final String JDK_TLS_CLIENT_PROTOCOLS = "jdk.tls.client.protocols";
    private static final String INITIALIZING_ERR_FMT = "Error initializing %s: %s";
    private static final String LOADED_FROM_CADI_PROPERTIES = "%s loaded from CADI Properties";
    private static final String LOADED_FROM_SYSTEM_PROPERTIES = "%s loaded from System Properties";

    public static final String SSL_KEY_MANAGER_FACTORY_ALGORITHM;

    private SSLSocketFactory socketFactory;
    private X509KeyManager[] x509KeyManager;
    private X509TrustManager[] x509TrustManager;
    public final String defaultAlias;
    public final String defaultClientAlias;
    private NetMask[] trustMasks;
    private SSLContext context;
    private HostnameVerifier maskHV;
    public final Access access;

    // Change Key Algorithms for IBM's VM. Could put in others, if needed.
    static {
        if ("IBM Corporation".equalsIgnoreCase(System.getProperty("java.vm.vendor"))) {
            SSL_KEY_MANAGER_FACTORY_ALGORITHM = "IbmX509";
        } else {
            SSL_KEY_MANAGER_FACTORY_ALGORITHM = "SunX509";
        }
    }


    public SecurityInfo(final Access access) throws CadiException {
        String msgHelp = "";
        try {
            this.access = access;
            // reuse DME2 Properties for convenience if specific Properties don't exist

            String str = access.getProperty(Config.CADI_ALIAS, null);
            if (str == null || str.isEmpty()) {
                defaultAlias = null;
            } else {
                defaultAlias = str;
            }

            str = access.getProperty(Config.CADI_CLIENT_ALIAS, null);
            if (str == null) {
                defaultClientAlias = defaultAlias;
            } else if (str.isEmpty()) {
                // intentionally off, i.e. cadi_client_alias=
                defaultClientAlias = null;
            } else {
                defaultClientAlias = str;
            }

            msgHelp = String.format(INITIALIZING_ERR_FMT, "Keystore", access.getProperty(Config.CADI_KEYSTORE, ""));
            initializeKeyManager();

            msgHelp = String.format(INITIALIZING_ERR_FMT, "Truststore", access.getProperty(Config.CADI_TRUSTSTORE, ""));
            initializeTrustManager();


            msgHelp =
                    String.format(INITIALIZING_ERR_FMT, "Trustmasks", access.getProperty(Config.CADI_TRUST_MASKS, ""));
            initializeTrustMasks();

            msgHelp = String.format(INITIALIZING_ERR_FMT, "HTTP Protocols", "access properties");
            setHTTPProtocols(access);

            msgHelp = String.format(INITIALIZING_ERR_FMT, "Context", "TLS");
            context = SSLContext.getInstance("TLS");
            context.init(x509KeyManager, x509TrustManager, null);
            SSLContext.setDefault(context);
            socketFactory = context.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException e) {
            throw new CadiException(msgHelp, e);
        }
    }

    public static void setHTTPProtocols(Access access) {
        String httpsProtocols = System.getProperty(Config.HTTPS_PROTOCOLS);
        if (httpsProtocols != null) {
            access.printf(Level.INIT, LOADED_FROM_SYSTEM_PROPERTIES, HTTPS_PROTOCOLS);
        } else {
            httpsProtocols = access.getProperty(Config.HTTPS_PROTOCOLS, null);
            if (httpsProtocols != null) {
                access.printf(Level.INIT, LOADED_FROM_CADI_PROPERTIES, HTTPS_PROTOCOLS);
            } else {
                httpsProtocols = access.getProperty(HTTPS_PROTOCOLS, Config.HTTPS_PROTOCOLS_DEFAULT);
                access.printf(Level.INIT, "%s set by %s in CADI Properties", Config.HTTPS_PROTOCOLS,
                        Config.CADI_PROTOCOLS);
            }
            // This needs to be set when people do not.
            System.setProperty(HTTPS_PROTOCOLS, httpsProtocols);
        }
        String httpsClientProtocols = System.getProperty(JDK_TLS_CLIENT_PROTOCOLS, null);
        if (httpsClientProtocols != null) {
            access.printf(Level.INIT, LOADED_FROM_SYSTEM_PROPERTIES, JDK_TLS_CLIENT_PROTOCOLS);
        } else {
            httpsClientProtocols = access.getProperty(Config.HTTPS_CLIENT_PROTOCOLS, null);
            if (httpsClientProtocols != null) {
                access.printf(Level.INIT, LOADED_FROM_CADI_PROPERTIES, Config.HTTPS_CLIENT_PROTOCOLS);
            } else {
                httpsClientProtocols = Config.HTTPS_PROTOCOLS_DEFAULT;
                access.printf(Level.INIT, "%s set from %s", Config.HTTPS_CLIENT_PROTOCOLS, "Default Protocols");
            }
            System.setProperty(JDK_TLS_CLIENT_PROTOCOLS, httpsClientProtocols);
        }
    }

    /**
     * @return the scf
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return socketFactory;
    }

    public SSLContext getSSLContext() {
        return context;
    }

    /**
     * @return the km
     */
    public X509KeyManager[] getKeyManagers() {
        return x509KeyManager;
    }

    public void checkClientTrusted(X509Certificate[] certarr) throws CertificateException {
        for (X509TrustManager xtm : x509TrustManager) {
            xtm.checkClientTrusted(certarr, SECURITY_ALGO);
        }
    }

    public void checkServerTrusted(X509Certificate[] certarr) throws CertificateException {
        for (X509TrustManager xtm : x509TrustManager) {
            xtm.checkServerTrusted(certarr, SECURITY_ALGO);
        }
    }

    public void setSocketFactoryOn(HttpsURLConnection hsuc) {
        hsuc.setSSLSocketFactory(socketFactory);
        if (maskHV != null && !maskHV.equals(hsuc.getHostnameVerifier())) {
            hsuc.setHostnameVerifier(maskHV);
        }
    }

    protected void initializeKeyManager() throws CadiException, IOException, NoSuchAlgorithmException,
            KeyStoreException, CertificateException, UnrecoverableKeyException {
        String keyStore = access.getProperty(Config.CADI_KEYSTORE, null);
        if (keyStore == null) {
            return;
        } else if (!new File(keyStore).exists()) {
            throw new CadiException(keyStore + " does not exist");
        }

        String keyStorePasswd = access.getProperty(Config.CADI_KEYSTORE_PASSWORD, null);
        keyStorePasswd = (keyStorePasswd == null) ? null : access.decrypt(keyStorePasswd, false);
        if (keyStore == null || keyStorePasswd == null) {
            x509KeyManager = new X509KeyManager[0];
            return;
        }

        String keyPasswd = access.getProperty(Config.CADI_KEY_PASSWORD, null);
        keyPasswd = (keyPasswd == null) ? keyStorePasswd : access.decrypt(keyPasswd, false);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(SSL_KEY_MANAGER_FACTORY_ALGORITHM);

        ArrayList<X509KeyManager> keyManagers = new ArrayList<>();
        File file;
        for (String ksname : Split.splitTrim(',', keyStore)) {
            String keystoreFormat;
            if (ksname.endsWith(".p12") || ksname.endsWith(".pkcs12")) {
                keystoreFormat = "PKCS12";
            } else {
                keystoreFormat = "JKS";
            }

            file = new File(ksname);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                try {
                    KeyStore ks = KeyStore.getInstance(keystoreFormat);
                    ks.load(fis, keyStorePasswd.toCharArray());
                    keyManagerFactory.init(ks, keyPasswd.toCharArray());
                } finally {
                    fis.close();
                }
            }
        }

        StringBuilder sb = null;
        for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
            if (keyManager instanceof X509KeyManager) {
                X509KeyManager xkm = (X509KeyManager) keyManager;
                keyManagers.add(xkm);
                if (defaultAlias != null) {
                    sb = new StringBuilder("X509 Chain\n");
                    x509Info(sb, xkm.getCertificateChain(defaultAlias));
                }
                if (defaultClientAlias != null && !defaultClientAlias.equals(defaultAlias)) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    } else {
                        sb.append('\n');
                    }
                    sb.append("X509 Client Chain\n");
                    x509Info(sb, xkm.getCertificateChain(defaultAlias));
                }
            }
        }
        x509KeyManager = new X509KeyManager[keyManagers.size()];
        keyManagers.toArray(x509KeyManager);

        if (sb != null) {
            access.log(Level.INIT, sb);
        }
    }

    private void x509Info(StringBuilder sb, X509Certificate[] chain) {
        if (chain != null) {
            int i = 0;
            for (X509Certificate x : chain) {
                sb.append("  ");
                sb.append(i++);
                sb.append(')');
                sb.append("\n    Subject: ");
                sb.append(x.getSubjectDN());
                sb.append("\n    Issuer : ");
                sb.append(x.getIssuerDN());
                sb.append("\n    Expires: ");
                sb.append(x.getNotAfter());
                sb.append('\n');
            }
        }
    }

    protected void initializeTrustManager()
            throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, CadiException {
        String trustStore = access.getProperty(Config.CADI_TRUSTSTORE, null);
        if (trustStore == null) {
            return;
        } else if (!new File(trustStore).exists()) {
            throw new CadiException(trustStore + " does not exist");
        }

        String trustStorePasswd = access.getProperty(Config.CADI_TRUSTSTORE_PASSWORD, null);
        trustStorePasswd = (trustStorePasswd == null) ? "changeit"
                /* defacto Java Trust Pass */ : access.decrypt(trustStorePasswd, false);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(SSL_KEY_MANAGER_FACTORY_ALGORITHM);
        File file;
        for (String trustStoreName : Split.splitTrim(',', trustStore)) {
            file = new File(trustStoreName);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                try {
                    KeyStore ts = KeyStore.getInstance("JKS");
                    ts.load(fis, trustStorePasswd.toCharArray());
                    trustManagerFactory.init(ts);
                } finally {
                    fis.close();
                }
            }
        }

        TrustManager trustManagers[] = trustManagerFactory.getTrustManagers();
        if (trustManagers == null || trustManagers.length == 0) {
            return;
        }

        x509TrustManager = new X509TrustManager[trustManagers.length];
        for (int i = 0; i < trustManagers.length; ++i) {
            try {
                x509TrustManager[i] = (X509TrustManager) trustManagers[i];
            } catch (ClassCastException e) {
                access.log(Level.WARN, "Non X509 TrustManager", x509TrustManager[i].getClass().getName(),
                        "skipped in SecurityInfo");
            }
        }
    }

    protected void initializeTrustMasks() throws AccessException {
        String tips = access.getProperty(Config.CADI_TRUST_MASKS, null);
        if (tips == null) {
            return;
        }

        access.log(Level.INIT, "Explicitly accepting valid X509s from", tips);
        String[] ipsplit = Split.splitTrim(',', tips);
        trustMasks = new NetMask[ipsplit.length];
        for (int i = 0; i < ipsplit.length; ++i) {
            try {
                trustMasks[i] = new NetMask(ipsplit[i]);
            } catch (MaskFormatException e) {
                throw new AccessException("Invalid IP Mask in " + Config.CADI_TRUST_MASKS, e);
            }
        }

        final HostnameVerifier origHV = HttpsURLConnection.getDefaultHostnameVerifier();
        maskHV = new HostnameVerifier() {
            @Override
            public boolean verify(final String urlHostName, final SSLSession session) {
                try {
                    // This will pick up /etc/host entries as well as DNS
                    InetAddress ia = InetAddress.getByName(session.getPeerHost());
                    for (NetMask tmask : trustMasks) {
                        if (tmask.isInNet(ia.getHostAddress())) {
                            return true;
                        }
                    }
                } catch (UnknownHostException e) {
                    // It's ok. do normal Verify
                }
                return origHV.verify(urlHostName, session);
            };
        };
        HttpsURLConnection.setDefaultHostnameVerifier(maskHV);
    }

}

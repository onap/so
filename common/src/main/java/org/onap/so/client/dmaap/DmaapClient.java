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

package org.onap.so.client.dmaap;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.onap.so.client.defaultproperties.DefaultDmaapPropertiesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public abstract class DmaapClient {

    protected static Logger logger = LoggerFactory.getLogger(DmaapClient.class);
    protected final Map<String, String> msoProperties;
    protected final Properties properties;

    public DmaapClient(String filepath) throws IOException {
        Resource resource = new ClassPathResource(filepath);
        DmaapProperties dmaapProperties = DmaapPropertiesLoader.getInstance().getNewImpl();
        if (dmaapProperties == null) {
            logger.error("No RestProperty implementation found on classpath, loading default");
            dmaapProperties = new DefaultDmaapPropertiesImpl();
        }
        this.msoProperties = dmaapProperties.getProperties();
        this.properties = new Properties();
        this.properties.load(resource.getInputStream());
        if (this.getAuth() != null && this.getKey() != null) {
            this.properties.put("auth", this.getAuth());
            this.properties.put("key", this.getKey());
        } else {
            logger.error("Dmaap auth or key is null");
        }
        this.properties.put("topic", this.getTopic());
        Optional<String> host = this.getHost();
        if (host.isPresent()) {
            this.properties.put("host", host.get());
        }
    }

    protected String deobfuscatePassword(String decrypted_key) {

        try {
            return new String(Base64.getDecoder().decode(decrypted_key.getBytes()));
        } catch (IllegalArgumentException iae) {
            logger.error("llegal Arguments", iae);
            return decrypted_key;
        }
    }

    public abstract String getKey();

    public abstract String getAuth();

    public abstract String getTopic();

    public abstract Optional<String> getHost();
}

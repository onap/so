/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
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
 */
package org.onap.so.heatbridge.aai.client;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.jaxb.XmlJaxbAnnotationIntrospector;
import feign.Body;
import feign.Client;
import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.Response;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.codec.ErrorDecoder.Default;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.jaxb.JacksonJaxbJsonDecoder;
import feign.jackson.jaxb.JacksonJaxbJsonEncoder;
import feign.slf4j.Slf4jLogger;
import java.util.Optional;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.Complex;
import org.onap.aai.domain.yang.Flavor;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Image;
import org.onap.aai.domain.yang.L3InterfaceIpv4AddressList;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.LagInterface;
import org.onap.aai.domain.yang.Metadatum;
import org.onap.aai.domain.yang.PInterface;
import org.onap.aai.domain.yang.PInterfaces;
import org.onap.aai.domain.yang.PhysicalLink;
import org.onap.aai.domain.yang.Pnf;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.SriovPf;
import org.onap.aai.domain.yang.Tenant;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.Vserver;

/**
 * This is an implementation of a REST client for AAI using OpenFeign.
 */
@Headers({"Content-Type: application/json", "Accept: application/json", "X-FromAppId: MSO", "X-TransactionId: MSO"})
public interface ActiveAndAvailableInventoryClient {

    /**
     * OpenFeign specific note: There are client function definitions, but only interface definition,
     * which Feign converts into the REST calls using @RequestLine which includes the HTTP method and the
     * URL. The URL portion can be parametrized using parameters in braces {parameterName}.
     * Annotation <code>@Param("parameterName")</code> specifies parameters that can be made available for
     * substitution in @RequestLine annotation.
     * Note 2. When the interface parameter is not annotated with @Parameter OpenFeign places it into REST body.
     * To format the body of the REST request, use @Body annotation; it accepts parameters annotated with @Param.
     * Note 3. Parameters in the URL need to be encoded. If the parameter is already encoded, and specify
     * <code> @Param(value = "myParam", encoded=true) String myParam</code>
     * Note 4. In certain cases, the request should contain slashes, p-interface for example, @RequestLine
     * should be added additional parameter decodeSlashes=false.
     * Note 5. For Patch method, need to specify <code>@Headers("Content-Type: application/merge-patch+json")</code>
     */

    String url = "https://aai:8443";
    String version = "/aai/v14";
    String username = "aai@aai.onap.org";
    String password = "demo123456!";

    /*
     * IMAGES
     */
    @RequestLine("GET /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/images/image/{image-id}")
    Response getImage(@Param("cloud-owner") String cloudOwner, @Param("cloud-region") String cloudSiteId,
        @Param("image-id") String imageId);

    @RequestLine("DELETE /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/images/image/{image-id}")
    Response deleteImage(@Param("cloud-owner") String cloudOwner, @Param("cloud-region") String cloudSiteId,
        @Param("image-id") String imageId);

    @RequestLine("PUT /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/images/image/{image-id}")
    Response addImage(Image image, @Param("cloud-owner") String cloudOwner, @Param("cloud-region") String cloudSiteId,
        @Param("image-id") String imageId);

    /*
     * FLAVORS
     */
    @RequestLine("GET /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/flavors/flavor/{flavor-id}")
    Response getFlavor(@Param("cloud-owner") String cloudOwner, @Param("cloud-region") String
        cloudSiteId, @Param("flavor-id") String flavorId);

    @RequestLine("DELETE /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/flavors/flavor/{flavor-id}")
    Response deleteFlavor(@Param("cloud-owner") String cloudOwner, @Param("cloud-region") String
        cloudSiteId, @Param("flavor-id") String flavorId);

    @RequestLine("PUT /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/flavors/flavor/{flavor-id}")
    Response addFlavor(Flavor flavor, @Param("cloud-owner") String cloudOwner, @Param("cloud-region") String
        cloudSiteId, @Param("flavor-id") String flavorId);

    /*
     * VSERVER
     */
    @RequestLine("PUT /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/tenants/tenant/{tenant-id}/vservers/vserver/{vserver-id}")
    void addVserver(Vserver vserver, @Param("cloud-owner") String cloudOwner,
        @Param("cloud-region") String cloudSiteId, @Param("tenant-id") String tenantId,
        @Param("vserver-id") String vServerId);

    @RequestLine("DELETE /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/tenants/tenant/{tenant-id}/vservers/vserver/{vserver-id}")
    Response deleteVserver(@Param("cloud-owner") String cloudOwner, @Param("cloud-region") String cloudSiteId,
        @Param("tenant-id") String tenantId, @Param("vserver-id") String vServerId);

    @RequestLine("GET {uri}?depth=all")
    Response getObjectFromUriAsFeignResponse(@Param("uri") String uri);

    @RequestLine("DELETE {uri}")
    Response deleteByUri(@Param("uri") String uri);

    @RequestLine("PUT /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/tenants/tenant/{tenant-id}/vservers/vserver/{vserver-id}/l-interfaces/l-interface/{l-interface-name}")
    Response addLInterfaceToVserver(LInterface lInterface, @Param("cloud-owner") String cloudOwner,
        @Param("cloud-region") String cloudSiteId, @Param("tenant-id") String tenantId,
        @Param("vserver-id") String vServerId, @Param("l-interface-name") String lInterfaceName);

    @RequestLine("DELETE /aai/v14/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region}/tenants/tenant/{tenant-id}/vservers/vserver/{vserver-id}/l-interfaces/l-interface/{l-interface-name}")
    Response deleteLInterfaceFromVserver(LInterface lInterface, @Param("cloud-owner") String cloudOwner,
        @Param("cloud-region") String cloudSiteId, @Param("tenant-id") String tenantId,
        @Param("vserver-id") String vServerId, @Param("l-interface-name") String lInterfaceName);

    /*
     * PSERVER
     */
    @RequestLine("GET /aai/v14/cloud-infrastructure/pservers/pserver/{hostname}?depth=all")
    Pserver getPserver(@Param("hostname") String hostname);

    /*
     * Physical Interface.
     */
    @RequestLine(value = "GET /aai/v14/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces/p-interface/{interface-name}", decodeSlash = false)
    PInterface getPhysicalInterfaceForPserver(@Param("hostname") String hostName,
        @Param(value = "interface-name", encoded = true) String interfaceName);

    @RequestLine(value = "PUT /aai/v14/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces/p-interface/{interface-name}/sriov-pfs/sriov-pf/{pf-pci-id}", decodeSlash = false)
    Response createSriovPfForPserverPInterface(SriovPf sriovPf, @Param("hostname") String hostName,
        @Param(value = "interface-name", encoded = true) String interfaceName, @Param("pf-pci-id") String pfPciId);

    @RequestLine(value = "DELETE /aai/v14/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces/p-interface/{interface-name"
        + "}/sriov-pfs/sriov-pf/{pf-pci-id}", decodeSlash = false)
    Response deleteSriovPfFromPserverPInterface(@Param("hostname") String hostName,
        @Param(value = "interface-name", encoded = true) String interfaceName, @Param("pf-pci-id") String pfPciId);

    /**
     * Default AAI client connect method. Should be adequate for most uses.
     * All parameters come from application.conf
     * @return {@link ActiveAndAvailableInventoryClient} client
     */
    static ActiveAndAvailableInventoryClient connect() {
        return connect(url);
    }

    static ActiveAndAvailableInventoryClient connect(final String baseUrl) {
       return connect(baseUrl, username, password);
    }

    /**
     * Get AAI client from URI/User/Pass
     * @param baseUrl AAI base URL in https://aai-service:8443 format
     * @param user AAI username
     * @param pass AAI password
     * @return {@link ActiveAndAvailableInventoryClient} client
     */
    static ActiveAndAvailableInventoryClient connect(final String baseUrl, final String user, final String pass) {
        Client client = new ApacheHttpClient(HttpClientBuilder.create().setSSLHostnameVerifier
            (NoopHostnameVerifier.INSTANCE).build()); //TODO: Fix SSL certs in AAI + Dockerfile maybe?
        // This is to properly encode AAI POJO to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setAnnotationIntrospector(new XmlJaxbAnnotationIntrospector());
        Encoder encoder = new JacksonJaxbJsonEncoder(objectMapper); //Should we move to GSON?
        Decoder decoder = new JacksonJaxbJsonDecoder();
        ErrorDecoder errorDecoder = new Default();
        return connect(baseUrl, user, pass, client, encoder, decoder, errorDecoder);
    }

    /**
     * Get AAI client with most customizations.
     * @param baseUrl AAI base URL
     * @param user AAI username
     * @param pass AAI password
     * @param client HttpClient
     * @param encoder Encoder
     * @param decoder Decoder
     * @param errorDecoder errorDecoder
     * @return {@link ActiveAndAvailableInventoryClient} client
     */
    static ActiveAndAvailableInventoryClient connect(final String baseUrl, final String user, final String pass,
        Client client, Encoder encoder, Decoder decoder, ErrorDecoder errorDecoder) {
        final Feign.Builder feignBuilder = Feign.builder()
            .client(client)
            .logger(new Slf4jLogger())
            .requestInterceptor(new BasicAuthRequestInterceptor(user, pass))
            .decoder(decoder)
            .encoder(encoder)
            .errorDecoder(errorDecoder);
        return feignBuilder.target(ActiveAndAvailableInventoryClient.class,
                Optional.ofNullable(baseUrl).orElse(url)); //TODO: I think we should get rid of defaults
    }
}

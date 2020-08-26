/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.cloud.authentication;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.onap.so.config.beans.PoConfig;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.utils.MsoTenantUtils;
import org.onap.so.openstack.utils.MsoTenantUtilsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponse;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.keystone.v3.Keystone;
import com.woorea.openstack.keystone.v3.model.Authentication;
import com.woorea.openstack.keystone.v3.model.Token;
import com.woorea.openstack.keystone.v3.model.Token.Service;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;


@Component
public class KeystoneV3Authentication {

    @Autowired
    private AuthenticationMethodFactory authenticationMethodFactory;

    @Autowired
    private MsoTenantUtilsFactory tenantUtilsFactory;

    @Autowired
    private PoConfig poConfig;

    public KeystoneAuthHolder getToken(CloudSite cloudSite, String tenantId, String type) throws MsoException {

        String cloudId = cloudSite.getId();
        String region = cloudSite.getRegionId();

        CloudIdentity cloudIdentity = cloudSite.getIdentityService();
        MsoTenantUtils tenantUtils =
                tenantUtilsFactory.getTenantUtilsByServerType(cloudIdentity.getIdentityServerType());
        String keystoneUrl = tenantUtils.getKeystoneUrl(cloudId, cloudIdentity);
        Keystone keystoneTenantClient = new Keystone(keystoneUrl);
        Authentication v3Credentials = authenticationMethodFactory.getAuthenticationForV3(cloudIdentity, tenantId);


        OpenStackRequest<Token> v3Request = keystoneTenantClient.tokens().authenticate(v3Credentials);

        return makeRequest(v3Request, type, region);
    }

    protected KeystoneAuthHolder makeRequest(OpenStackRequest<Token> v3Request, String type, String region) {

        OpenStackResponse response = Failsafe.with(createRetryPolicy()).get(() -> {
            return v3Request.request();
        });
        String id = response.header("X-Subject-Token");
        Token token = response.getEntity(Token.class);
        KeystoneAuthHolder result = new KeystoneAuthHolder();
        result.setId(id);
        result.setexpiration(token.getExpiresAt());
        result.setServiceUrl(findEndpointURL(token.getCatalog(), type, region, "public"));
        return result;
    }

    protected RetryPolicy<OpenStackResponse> createRetryPolicy() {
        List<Predicate<Throwable>> result = new ArrayList<>();
        result.add(e -> {
            return e.getCause() instanceof OpenStackResponseException
                    && Arrays.asList(poConfig.getRetryCodes().split(","))
                            .contains(Integer.toString(((OpenStackResponseException) e).getStatus()));
        });
        result.add(e -> {
            return e.getCause() instanceof OpenStackConnectException;
        });

        Predicate<Throwable> pred = result.stream().reduce(Predicate::or).orElse(x -> false);
        RetryPolicy<OpenStackResponse> policy = new RetryPolicy<OpenStackResponse>().handleIf(error -> pred.test(error))
                .withDelay(Duration.ofSeconds(poConfig.getRetryDelay())).withMaxRetries(poConfig.getRetryCount());

        return policy;
    }

    public String findEndpointURL(List<Service> serviceCatalog, String type, String region, String facing) {
        for (Service service : serviceCatalog) {
            if (type.equals(service.getType())) {
                for (Service.Endpoint endpoint : service.getEndpoints()) {
                    if (region == null || region.equals(endpoint.getRegion())) {
                        if (facing.equals(endpoint.getInterface())) {
                            return endpoint.getUrl();
                        }
                    }
                }
            }
        }
        throw new ServiceEndpointNotFoundException(
                "endpoint url not found: type:" + type + " region: " + region + " facing: " + facing);
    }
}

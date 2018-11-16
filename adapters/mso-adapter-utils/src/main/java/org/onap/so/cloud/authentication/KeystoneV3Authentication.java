package org.onap.so.cloud.authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
		MsoTenantUtils tenantUtils = tenantUtilsFactory.getTenantUtilsByServerType(cloudIdentity.getIdentityServerType());
        String keystoneUrl = tenantUtils.getKeystoneUrl(cloudId, cloudIdentity);
        Keystone keystoneTenantClient = new Keystone (keystoneUrl);
        Authentication v3Credentials = authenticationMethodFactory.getAuthenticationForV3(cloudIdentity, tenantId);


    	OpenStackRequest<Token> v3Request = keystoneTenantClient.tokens ()
                .authenticate(v3Credentials);
    	
    	KeystoneAuthHolder holder = makeRequest(v3Request, type, region);

		return holder;
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
		result.setHeatUrl(findEndpointURL(token.getCatalog(), type, region, "public"));
		return result;
	}
	
	protected RetryPolicy createRetryPolicy() {
		RetryPolicy policy = new RetryPolicy();
		List<Predicate<Throwable>> result = new ArrayList<>();
		result.add(e -> {
			return e.getCause() instanceof OpenStackResponseException 
					&& Arrays.asList(poConfig.getRetryCodes().split(","))
					.contains(Integer.toString(((OpenStackResponseException)e).getStatus()));
		});
		result.add(e -> {
			return e.getCause() instanceof OpenStackConnectException;
		});
		
		Predicate<Throwable> pred = result.stream().reduce(Predicate::or).orElse(x -> false);

		policy.retryOn(error -> pred.test(error));
		
		policy.withDelay(poConfig.getRetryDelay(), TimeUnit.SECONDS)
		.withMaxRetries(poConfig.getRetryCount());
		
		return policy;
	}
	
	protected String findEndpointURL(List<Service> serviceCatalog, String type, String region, String facing) {
		for(Service service : serviceCatalog) {
			if(type.equals(service.getType())) {
				for(Service.Endpoint endpoint : service.getEndpoints()) {
					if(region == null || region.equals(endpoint.getRegion())) {
						if(facing.equals(endpoint.getInterface())) {
							return endpoint.getUrl();
						}
					}
				}
			}
		}
		throw new ServiceEndpointNotFoundException("endpoint url not found");
	}
}

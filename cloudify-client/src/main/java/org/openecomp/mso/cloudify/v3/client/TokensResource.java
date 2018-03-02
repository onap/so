package org.openecomp.mso.cloudify.v3.client;

import org.openecomp.mso.cloudify.v3.model.Token;
import org.openecomp.mso.cloudify.base.client.HttpMethod;
import org.openecomp.mso.cloudify.base.client.CloudifyClient;
import org.openecomp.mso.cloudify.base.client.CloudifyRequest;

public class TokensResource {

    private final CloudifyClient client;

    public TokensResource(CloudifyClient client) {
        this.client = client;
    }

    /*
     * Get a new token for a user
     * TODO:  User ID/Password logic need to be in the Client.
     * Results of a token query should also be able to add to the Client
     */
    public GetToken token() {
        return new GetToken();
    }

    public class GetToken extends CloudifyRequest<Token> {
        public GetToken() {
            super(client, HttpMethod.GET, "/api/v3/tokens", null, Token.class);
        }
    }
}

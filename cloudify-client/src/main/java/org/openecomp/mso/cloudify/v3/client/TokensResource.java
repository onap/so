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

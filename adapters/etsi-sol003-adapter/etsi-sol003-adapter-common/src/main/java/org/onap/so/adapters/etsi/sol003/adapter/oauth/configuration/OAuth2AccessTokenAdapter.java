/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.etsi.sol003.adapter.oauth.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class OAuth2AccessTokenAdapter implements JsonSerializer<OAuth2AccessToken> {

    @Override
    public JsonElement serialize(final OAuth2AccessToken src, final Type typeOfSrc,
            final JsonSerializationContext context) {
        final JsonObject obj = new JsonObject();
        obj.addProperty(OAuth2AccessToken.ACCESS_TOKEN, src.getValue());
        obj.addProperty(OAuth2AccessToken.TOKEN_TYPE, src.getTokenType());
        if (src.getRefreshToken() != null) {
            obj.addProperty(OAuth2AccessToken.REFRESH_TOKEN, src.getRefreshToken().getValue());
        }
        obj.addProperty(OAuth2AccessToken.EXPIRES_IN, src.getExpiresIn());
        final JsonArray scopeObj = new JsonArray();
        for (final String scope : src.getScope()) {
            scopeObj.add(scope);
        }
        obj.add(OAuth2AccessToken.SCOPE, scopeObj);

        return obj;
    }
}

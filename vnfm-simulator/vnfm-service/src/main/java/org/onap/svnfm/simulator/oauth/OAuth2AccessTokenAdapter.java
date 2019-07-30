package org.onap.svnfm.simulator.oauth;

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

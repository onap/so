/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.onap.so.heatbridge.decoder;

import static feign.Util.valuesOrEmpty;

import feign.Request;
import feign.Response;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import org.onap.so.heatbridge.utils.FeignUtils;

public class ResponseWithStringBody {

    private final int status;
    private final String reason;
    private final Map<String, Collection<String>> headers;
    private final String body;
    private final Request request;

    private ResponseWithStringBody(final Builder builder) {
        this.status = builder.status;
        this.reason = builder.reason;
        this.headers = builder.headers;
        this.body = builder.body;
        this.request = builder.request;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int status() {
        return this.status;
    }

    public String reason() {
        return this.reason;
    }

    public Map<String, Collection<String>> headers() {
        return this.headers;
    }

    public String body() {
        return this.body;
    }

    public Request request() {
        return this.request;
    }

    public static final class Builder {
        private int status;
        private String reason;
        private Map<String, Collection<String>> headers;
        private String body;
        private Request request;

        Builder() {
        }

        Builder(Response source) {
            this.status = source.status();
            this.reason = source.reason();
            this.headers = source.headers();
            this.body = FeignUtils.extractResponseBody(source);
            this.request = source.request();
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder headers(Map<String, Collection<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(String text, Charset charset) {
            this.body = new String(text.getBytes(), charset);
            return this;
        }


        public Builder request(Request request) {
            this.request = request;
            return this;
        }

        public ResponseWithStringBody build() {
            return new ResponseWithStringBody(this);
        }

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("HTTP/1.1 ").append(status);
        if (reason != null) builder.append(' ').append(reason);
        builder.append('\n');
        for (String field : headers.keySet()) {
            for (String value : valuesOrEmpty(headers, field)) {
                builder.append(field).append(": ").append(value).append('\n');
            }
        }
        if (body != null) builder.append('\n').append(body);
        return builder.toString();
    }
}

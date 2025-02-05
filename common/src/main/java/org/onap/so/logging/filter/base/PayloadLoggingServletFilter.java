/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.logging.filter.base;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.zip.GZIPInputStream;

public class PayloadLoggingServletFilter extends AbstractServletFilter implements Filter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PayloadLoggingServletFilter.class);
    private static final int defaultMaxSize = 100000;
    private static Integer maxResponseSize;
    private static Integer maxRequestSize;
    protected static Boolean LOG_INVOKE;

    public PayloadLoggingServletFilter() {
        String maxRequestSizeOverride = System.getProperty("FILTER_MAX_REQUEST_SIZE");
        if (maxRequestSizeOverride != null) {
            maxRequestSize = Integer.valueOf(maxRequestSizeOverride);
        } else {
            maxRequestSize = defaultMaxSize;
        }

        String maxResponseSizeOverride = System.getProperty("FILTER_MAX_RESPONSE_SIZE");
        if (maxResponseSizeOverride != null) {
            maxResponseSize = Integer.valueOf(maxResponseSizeOverride);
        } else {
            maxResponseSize = defaultMaxSize;
        }
    }

    private static class ByteArrayServletStream extends ServletOutputStream {
        ByteArrayOutputStream baos;

        ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        @Override
        public void write(int param) throws IOException {
            baos.write(param);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener arg0) {
            // this method does nothing
        }
    }


    private static class ByteArrayPrintWriter extends PrintWriter {
        private ByteArrayOutputStream baos;
        private int errorCode = -1;
        private String errorMsg = "";
        private boolean errored = false;

        public ByteArrayPrintWriter(ByteArrayOutputStream out) {
            super(out);
            this.baos = out;
        }

        public ServletOutputStream getStream() {
            return new ByteArrayServletStream(baos);
        }

        public Boolean hasErrored() {
            return errored;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setError(int code) {
            errorCode = code;
            errored = true;
        }

        public void setError(int code, String msg) {
            errorMsg = msg;
            errorCode = code;
            errored = true;
        }

    }


    private class BufferedServletInputStream extends ServletInputStream {
        ByteArrayInputStream bais;

        public BufferedServletInputStream(ByteArrayInputStream bais) {
            this.bais = bais;
        }

        @Override
        public int available() {
            return bais.available();
        }

        @Override
        public int read() {
            return bais.read();
        }

        @Override
        public int read(byte[] buf, int off, int len) {
            return bais.read(buf, off, len);
        }

        @Override
        public boolean isFinished() {
            return available() < 1;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener arg0) {
            // this method does nothing
        }

    }


    private class BufferedRequestWrapper extends HttpServletRequestWrapper {
        ByteArrayInputStream bais;
        ByteArrayOutputStream baos;
        BufferedServletInputStream bsis;
        byte[] buffer;

        public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
            super(req);

            InputStream is = req.getInputStream();
            baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int letti;
            while ((letti = is.read(buf)) > 0) {
                baos.write(buf, 0, letti);
            }
            buffer = baos.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() {
            try {
                bais = new ByteArrayInputStream(buffer);
                bsis = new BufferedServletInputStream(bais);
            } catch (Exception ex) {
                log.error("Exception in getInputStream", ex);
            }
            return bsis;
        }

        public byte[] getBuffer() {
            return buffer;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // this method does nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpRequest);

        StringBuilder requestHeaders = new StringBuilder("REQUEST|");
        requestHeaders.append(httpRequest.getMethod());
        requestHeaders.append(":");
        requestHeaders.append(httpRequest.getRequestURL().toString());
        requestHeaders.append("|");
        requestHeaders.append(getSecureRequestHeaders(httpRequest));

        log.info(requestHeaders.toString());

        byte[] buffer = bufferedRequest.getBuffer();
        if (buffer.length < maxRequestSize) {
            log.info("REQUEST BODY|{}", new String(buffer));
        } else {
            log.info("REQUEST BODY|{}", new String(buffer, 0, maxRequestSize));
        }

        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ByteArrayPrintWriter pw = new ByteArrayPrintWriter(baos);

        HttpServletResponse wrappedResp = new HttpServletResponseWrapper(response) {
            @Override
            public PrintWriter getWriter() {
                return pw;
            }

            @Override
            public ServletOutputStream getOutputStream() {
                return pw.getStream();
            }

            @Override
            public void sendError(int sc) throws IOException {
                super.sendError(sc);
                pw.setError(sc);
            }

            @Override
            public void sendError(int sc, String msg) throws IOException {
                super.sendError(sc, msg);
                pw.setError(sc, msg);
            }
        };

        try {
            filterChain.doFilter(bufferedRequest, wrappedResp);
        } catch (Exception e) {
            log.error("Chain Exception", e);
            throw e;
        } finally {
            try {
                byte[] bytes = baos.toByteArray();
                StringBuilder responseHeaders = new StringBuilder();
                responseHeaders.append("RESPONSE HEADERS|").append(formatResponseHeaders(response));
                responseHeaders.append("Status:").append(response.getStatus());
                responseHeaders.append(";IsCommitted:").append(wrappedResp.isCommitted());

                log.info(responseHeaders.toString());

                if ("gzip".equals(response.getHeader("Content-Encoding"))) {
                    log.info("UNGZIPED RESPONSE BODY|{}", decompressGZIPByteArray(bytes));
                } else {
                    if (bytes.length < maxResponseSize) {
                        log.info("RESPONSE BODY|{}", new String(bytes));
                    } else {
                        log.info("RESPONSE BODY|{}", new String(bytes, 0, maxResponseSize));
                    }
                }

                if (pw.hasErrored()) {
                    log.info("ERROR RESPONSE|{}:{}", pw.getErrorCode(), pw.getErrorMsg());
                } else if (!wrappedResp.isCommitted()) {
                    response.getOutputStream().write(bytes);
                    response.getOutputStream().flush();
                }
            } catch (Exception e) {
                log.error("Exception in response filter", e);
            }
        }
    }

    @Override
    public void destroy() {
        // this method does nothing
    }

    private String decompressGZIPByteArray(byte[] bytes) {
        StringBuilder str = new StringBuilder();
        try (BufferedReader in =
                new BufferedReader(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(bytes))))) {
            String content;
            while ((content = in.readLine()) != null) {
                str.append(content);
            }
        } catch (Exception e) {
            log.error("Failed get read GZIPInputStream", e);
        }
        return str.toString();
    }

}

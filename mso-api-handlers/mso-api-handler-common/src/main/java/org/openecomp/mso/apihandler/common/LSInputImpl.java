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

package org.openecomp.mso.apihandler.common;


import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

public class LSInputImpl implements LSInput {

    protected String fPublicId;
    protected String fSystemId;
    protected String fBaseSystemId;
    protected InputStream fByteStream;
    protected Reader fCharStream;
    protected String fData;
    protected String fEncoding;
    protected boolean fCertifiedText;

    @Override
    public InputStream getByteStream () {
        return fByteStream;
    }

    @Override
    public void setByteStream (InputStream byteStream) {
        fByteStream = byteStream;
    }

    @Override
    public Reader getCharacterStream () {
        return fCharStream;
    }

    @Override
    public void setCharacterStream (Reader characterStream) {
        fCharStream = characterStream;
    }

    @Override
    public String getStringData () {
        return fData;
    }

    @Override
    public void setStringData (String stringData) {
        fData = stringData;
    }

    @Override
    public String getEncoding () {
        return fEncoding;
    }

    @Override
    public void setEncoding (String encoding) {
        fEncoding = encoding;
    }

    @Override
    public String getPublicId () {
        return fPublicId;
    }

    @Override
    public void setPublicId (String publicId) {
        fPublicId = publicId;
    }

    @Override
    public String getSystemId () {
        return fSystemId;
    }

    @Override
    public void setSystemId (String systemId) {
        fSystemId = systemId;
    }

    @Override
    public String getBaseURI () {
        return fBaseSystemId;
    }

    @Override
    public void setBaseURI (String baseURI) {
        fBaseSystemId = baseURI;
    }

    @Override
    public boolean getCertifiedText () {
        return fCertifiedText;
    }

    @Override
    public void setCertifiedText (boolean certifiedText) {
        fCertifiedText = certifiedText;
    }
}

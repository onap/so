/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2017 Cloudify.co.  All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END====================================================
*/
package com.gigaspaces.aria.rest.client;

import java.net.URI;

/**
 * CSAR based implementation
 *
 * Created by DeWayne on 7/17/2017.
 */
public class ServiceTemplateImpl implements ServiceTemplate {
    public static final String DEFAULT_TEMPLATE_NAME = "service-template.yaml";
    private String name;
    private int id;
    private URI uri;
    private String filename = DEFAULT_TEMPLATE_NAME;
    private String description;
    private byte[] csar_blob; // for opaque binary

    public ServiceTemplateImpl(){}

    public ServiceTemplateImpl(String name, URI uri){
        this.name=name;
        this.uri=uri;
    }

    /**
     * Construct service template from CSAR byte array
     */
    public ServiceTemplateImpl(String name, byte[] csar){
	    this.csar_blob = csar;
	    this.name = name;
    }

    /**
     * Construct an instance based on CSAR
     * @param name a textual name for the template
     * @param uri a URI to a CSAR
     * @param filename the filename in the CSAR representing main yaml template
     */
    public ServiceTemplateImpl(String name, URI uri, String filename, String description){
        this.name=name;
        this.uri=uri;
        this.filename=filename;
        this.description=description;
    }
    
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id=id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public URI getURI() {
        return uri;
    }
    public void setPath(String path){
        this.uri=uri;
    }
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename){
        this.filename=filename;
    }
    public byte[] getCSARBytes() {
    	return csar_blob;
    }

    public String getDescription(){ return description;}
}

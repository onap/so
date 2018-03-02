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

import java.io.InputStream;

import org.openecomp.mso.cloudify.v3.model.Blueprint;
import org.openecomp.mso.cloudify.v3.model.Blueprints;
import org.openecomp.mso.cloudify.base.client.Entity;
import org.openecomp.mso.cloudify.base.client.HttpMethod;
import org.openecomp.mso.cloudify.base.client.CloudifyClient;
import org.openecomp.mso.cloudify.base.client.CloudifyRequest;

public class BlueprintsResource {

    private final CloudifyClient client;

    public BlueprintsResource(CloudifyClient client) {
        this.client = client;
    }

    /*
     * Upload a blueprint package directly.  The blueprint must be a ZIP archive.
     * However, this method will not validate this.  
     */
    public UploadBlueprint uploadFromStream (String blueprintId, String mainFileName, InputStream blueprint) {
        return new UploadBlueprint (blueprintId, mainFileName, blueprint, null);
    }

    public UploadBlueprint uploadFromUrl (String blueprintId, String mainFileName, String blueprintUrl) {
        return new UploadBlueprint (blueprintId, mainFileName, null, blueprintUrl);
    }

    public ListBlueprints list() {
        return new ListBlueprints();
    }

    public GetBlueprint getById(String id) {
        return new GetBlueprint(id, null);
    }

    // Return all of the metadata, but not the plan
    public GetBlueprint getMetadataById(String id) {
        return new GetBlueprint(id, "?_include=id,main_file_name,description,tenant_name,created_at,updated_at");
    }

    public DeleteBlueprint deleteById(String id) {
        return new DeleteBlueprint(id);
    }

    public class UploadBlueprint extends CloudifyRequest<Blueprint> {
        public UploadBlueprint(String blueprintId, String mainFileName, InputStream blueprint, String blueprintUrl) {
        	// Initialize the request elements dynamically.
        	// Either a blueprint input stream or a URL will be provided.
        	// If a URL is provided, add it to the query string
        	// If a Stream is provided, set it as the Entity body
        	super(client, HttpMethod.PUT,
        			"/api/v3/blueprints/" + blueprintId + "?application_file_name=" + mainFileName + ((blueprintUrl != null) ? "&blueprint_archive=" + blueprintUrl : ""),
        			((blueprint != null) ? Entity.stream(blueprint) : null),
        			Blueprint.class);
        }
    }

    public class DeleteBlueprint extends CloudifyRequest<Blueprint> {
        public DeleteBlueprint(String blueprintId) {
            super(client, HttpMethod.DELETE, "/api/v3/blueprints/" + blueprintId, null, Blueprint.class);
        }
    }

    public class GetBlueprint extends CloudifyRequest<Blueprint> {
        public GetBlueprint(String id, String queryArgs) {
            super(client, HttpMethod.GET, "/api/v3/blueprints/" + id + queryArgs, null, Blueprint.class);
        }
    }

    public class ListBlueprints extends CloudifyRequest<Blueprints> {
        public ListBlueprints() {
            super(client, HttpMethod.GET, "/api/v3/blueprints", null, Blueprints.class);
       }
    }

    // TODO:  DownloadBlueprint is not supported, as it needs to return an input stream
    //        containing the full blueprint ZIP.
    //        For a full client library, this will require returning an open stream as the entity...
}

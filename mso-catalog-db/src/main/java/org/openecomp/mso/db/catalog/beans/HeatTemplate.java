/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.db.catalog.beans;


import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import org.openecomp.mso.db.catalog.utils.MavenLikeVersioning;
import org.openecomp.mso.logger.MsoLogger;

public class HeatTemplate extends MavenLikeVersioning {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);

    private int id;
    private String templateName;
    private String templatePath = null;
    private String templateBody = null;
    private int timeoutMinutes;
    private Set <HeatTemplateParam> parameters;
    private Set <HeatNestedTemplate> files;
    private String description;
    private String asdcUuid;
    private String asdcResourceName;
    private String asdcLabel;
    private String artifactChecksum;

    private Timestamp created;

    public enum TemplateStatus {
                                PARENT, CHILD, PARENT_COMPLETE
    }

    public HeatTemplate () {
    }

    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public String getTemplateName () {
        return templateName;
    }

    public void setTemplateName (String templateName) {
        this.templateName = templateName;
    }

    public String getTemplatePath () {
        return templatePath;
    }

    public void setTemplatePath (String templatePath) {
        this.templatePath = templatePath;
    }

    public String getTemplateBody () {
        return templateBody;
    }

    public void setTemplateBody (String templateBody) {
        this.templateBody = templateBody;
    }

    public int getTimeoutMinutes () {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes (int timeout) {
        this.timeoutMinutes = timeout;
    }

    public Set <HeatTemplateParam> getParameters () {
        return parameters;
    }

    public void setParameters (Set <HeatTemplateParam> parameters) {
        this.parameters = parameters;
    }

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHeatTemplate () {
        if (templateBody != null && !templateBody.isEmpty ()) {
            // The template body is in the DB table. Just return it.
            return templateBody;
        }

        String body = null;

        try (BufferedReader reader = new BufferedReader (new FileReader (templatePath)))
        {
            String line = null;
            StringBuilder stringBuilder = new StringBuilder ();
            while ((line = reader.readLine ()) != null) {
                stringBuilder.append (line);
            }
            body = stringBuilder.toString ();
        } catch (Exception e) {
            LOGGER.debug ("Error reading template file " + templatePath, e);
        }

        return body;
    }

    public void setFiles (Set <HeatNestedTemplate> files) {
        this.files = files;
    }

    public Set <HeatNestedTemplate> getFiles () {
        return this.files;
    }

	public String getAsdcUuid() {
		return asdcUuid;
	}

	public void setAsdcUuid(String asdcUuidp) {
		this.asdcUuid = asdcUuidp;
	}

    public String getAsdcResourceName() {
		return asdcResourceName;
	}

	public void setAsdcResourceName(String asdcResourceName) {
		this.asdcResourceName = asdcResourceName;
	}
	public String getAsdcLabel() {
		return this.asdcLabel;
	}
	public void setAsdcLabel(String asdcLabel) {
		this.asdcLabel = asdcLabel;
	}

    public String getArtifactChecksum() {
        return artifactChecksum;
    }

    public void setArtifactChecksum(String artifactChecksum) {
        this.artifactChecksum = artifactChecksum;
    }
    public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	@Override
    public String toString () {
        String body = (templateBody != null) ? "(" + templateBody.length () + " chars)" : "(Not defined)";
        StringBuilder sb = new StringBuilder ();
        sb.append ("Template=")
          .append (templateName)
          .append (",version=")
          .append (version)
          .append (",path=")
          .append (templatePath)
          .append (",body=")
          .append (body)
          .append (",timeout=")
          .append (timeoutMinutes)
          .append (",asdcUuid=")
          .append (asdcUuid)
          .append (",asdcResourceName=")
          .append (asdcResourceName)
          .append (",description=")
          .append (description);
        if (created != null) {
        	sb.append (",created=");
        	sb.append (DateFormat.getInstance().format(created));
        }


        if (parameters != null && !parameters.isEmpty ()) {
            sb.append (",params=[");
            for (HeatTemplateParam param : parameters) {
                sb.append (param.getParamName ());
                if (param.isRequired ()) {
                    sb.append ("(reqd)");
                }
                sb.append (",");
            }
            sb.replace (sb.length () - 1, sb.length (), "]");
        }
        return sb.toString ();
    }


}

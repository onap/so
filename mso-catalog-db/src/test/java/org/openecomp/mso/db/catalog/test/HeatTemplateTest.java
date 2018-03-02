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

package org.openecomp.mso.db.catalog.test;


import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Test;

import org.openecomp.mso.db.catalog.beans.HeatTemplate;
import org.openecomp.mso.db.catalog.beans.HeatTemplateParam;

/**
 */

public class HeatTemplateTest {
	
    @Test
    public final void heatTemplateTest () {
        HeatTemplate heatTemplate = new HeatTemplate ();
        heatTemplate.setTemplateBody ("testBody");
        heatTemplate.setArtifactUuid(UUID.randomUUID().toString());
        assertTrue (heatTemplate.getHeatTemplate ().equals ("testBody"));
        assertTrue (heatTemplate.toString ().contains ("8 chars"));
        heatTemplate.setTemplateBody (null);
        assertTrue (heatTemplate.toString ().contains ("Not defined"));
        HashSet<HeatTemplateParam> set = new HashSet<> ();
        HeatTemplateParam param = new HeatTemplateParam ();
        param.setParamName ("param name");
        param.setParamType ("string");
        param.setRequired (false);
        param.setHeatTemplateArtifactUuid(UUID.randomUUID().toString());
        set.add (param);
        HeatTemplateParam param2 = new HeatTemplateParam ();
        param2.setParamName ("param 2");
        param2.setParamType ("string");
        param2.setRequired (true);
        param2.setHeatTemplateArtifactUuid(UUID.randomUUID().toString());
        set.add (param2);
        heatTemplate.setParameters (set);
        String heatStr = heatTemplate.toString (); 
        assertTrue (heatStr.contains ("param name"));
        assertTrue (heatStr.contains ("param 2(reqd)"));

        File tempFile;
        try {
            tempFile = File.createTempFile ("heatTemplate", "test");
            tempFile.deleteOnExit ();
            try (Writer writer = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (tempFile),
                                                                             "utf-8"))) {
                writer.write ("something\n");
                writer.write ("something2\n");
            }
            heatTemplate.setTemplateBody(tempFile.getAbsolutePath ());
            assertTrue (heatTemplate.getHeatTemplate ().contains ("test"));
        } catch (IOException e) {
            e.printStackTrace ();
            fail ("Exception caught");
        }
    }

}

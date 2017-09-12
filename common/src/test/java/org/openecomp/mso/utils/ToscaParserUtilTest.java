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
package org.openecomp.mso.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;
import org.onap.tosca.checker.Catalog;
import org.onap.tosca.checker.CheckerException;
import org.onap.tosca.checker.Target;
import org.openecomp.mso.utils.ToscaParserUtil;

public class ToscaParserUtilTest {

    @Test
    public final void testGetResults () throws IOException, URISyntaxException, CheckerException {

        String path = "src\\test\\resources\\testPackage.csar";
        File file = new File (path);

        Catalog cat = ToscaParserUtil.parserCSAR (path, file.toURI ());
        for (Target t : cat.targets ()) {

            System.err.println (t.getLocation () + "\n" + cat.importString (t) + "\n" + t.getReport ());
        }
    }

}

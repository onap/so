/*-
 * ============LICENSE_START=======================================================
 * ONAP SO
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights reserved.
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

import java.io.IOException;
import java.net.URI;

import org.onap.tosca.checker.CSARRepository;
import org.onap.tosca.checker.Catalog;
import org.onap.tosca.checker.Checker;
import org.onap.tosca.checker.CheckerException;

public class ToscaParserUtil {

   
    public static Catalog parserCSAR (String theName, URI theRoot) throws IOException, CheckerException {
        CSARRepository repo = new CSARRepository (theName, theRoot);

        Checker checker = new Checker ();

        checker.setTargetLocator (repo.getTargetLocator ());

        checker.check (repo.mainTarget ());

        Catalog cat = checker.catalog ();

        return cat;

    }

}

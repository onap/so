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

package org.onap.so.asdc.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.fasterxml.jackson.databind.JsonNode;


public class ASDCLoggingRunner {
    public static void main(String[] args) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get("src/test/resources/Heat_Nested_Notification.txt")));

        ASDCLoggingLexer lexer = new ASDCLoggingLexer(CharStreams.fromString(content));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ASDCLoggingParser parser = new ASDCLoggingParser(tokens);

        ParseTree tree = parser.doc();

        System.out.println(TreeUtils.printTree(tree, parser)); // print LISP-style tree

        ASDCLoggingVisitorImpl v = new ASDCLoggingVisitorImpl();

        JsonNode node = v.visit(tree);

        System.out.println(node.toString());

    }
}

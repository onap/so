/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

import java.util.Arrays;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


public class TreeUtils {

    /**
     * Print the tree, splitting at appropriate points, instead of rendering a single long line.
     * 
     * @param parseTree
     * @param parser
     * @return
     */
    public static String printTree(final ParseTree parseTree, final Parser parser) {
        final TreePrinterListener listener = new TreePrinterListener(Arrays.asList(parser.getRuleNames()));
        ParseTreeWalker.DEFAULT.walk(listener, parseTree);
        return listener.toString();
    }

    public static String normalizeWhiteSpace(String input) {
        return input.replaceAll("\\s+", " ").replaceAll("([^\\w])\\s+", "$1");
    }
}

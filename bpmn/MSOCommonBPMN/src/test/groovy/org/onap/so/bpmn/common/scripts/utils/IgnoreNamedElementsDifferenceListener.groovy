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

package org.onap.so.bpmn.common.scripts.utils

import org.custommonkey.xmlunit.Difference
import org.custommonkey.xmlunit.DifferenceConstants
import org.custommonkey.xmlunit.DifferenceListener
import org.w3c.dom.Node

class IgnoreNamedElementsDifferenceListener implements DifferenceListener {
    private Set<String> blackList = new HashSet<String>();

    public IgnoreNamedElementsDifferenceListener(String ... ignoreTags) {
        for (String name : ignoreTags) {
            blackList.add(name);
        }
    }

    public int differenceFound(Difference difference) {
        if (difference.getId() == DifferenceConstants.TEXT_VALUE_ID) {
            if (blackList.contains(difference.getControlNodeDetail().getNode().getParentNode().getNodeName())) {
                return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
        }

        return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
    }


    public void skippedComparison(Node node, Node node1) {

    }
}

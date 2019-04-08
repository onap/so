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

package org.onap.so.utils;



import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "checkresults")
public class CheckResults {

    @XmlElement(name = "checkresult")
    private List<CheckResult> results;

    public CheckResults() {
        results = new ArrayList<>();
    }

    public List<CheckResult> getResults() {
        return results;
    }

    public void addHostCheckResult(String hostname, int state, String output) {
        CheckResult newResult = new CheckResult();
        newResult.setType("host");
        newResult.setHostname(hostname);
        newResult.setState(state);
        newResult.setOutput(output);
        results.add(newResult);
    }

    public void addServiceCheckResult(String hostname, String servicename, int state, String output) {
        CheckResult newResult = new CheckResult();
        newResult.setType("service");
        newResult.setHostname(hostname);
        newResult.setServicename(servicename);
        newResult.setState(state);
        newResult.setOutput(output);
        results.add(newResult);
    }

    public static class CheckResult {

        private String type;
        private String hostname;
        private String servicename;
        private int state;
        private String output;

        @XmlAttribute(required = true)
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @XmlElement(required = true)
        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        @XmlElement(required = false)
        public String getServicename() {
            return servicename;
        }

        public void setServicename(String servicename) {
            this.servicename = servicename;
        }

        @XmlElement(required = true)
        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        @XmlElement(required = true)
        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }
    }
}

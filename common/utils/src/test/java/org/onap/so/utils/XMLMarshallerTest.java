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

import org.junit.Assert;
import org.junit.Test;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Tests the XMLMarshaller to ensure that it's able to marshal and unmarshall a POJO
 */

public class XMLMarshallerTest {

    @Test
    public void testMarshal() throws Exception {
        Assert.assertEquals(getXML(), XmlMarshaller.marshal(getPOJO()));
    }

    @Test
    public void testUnMarshal() throws Exception {
        Assert.assertEquals(XmlMarshaller.unMarshal(getXML(), new TestPOJO()), getPOJO());
    }

    private TestPOJO getPOJO() {
        TestPOJO testPOJO = new TestPOJO();
        testPOJO.setFirstName("FN");
        testPOJO.setLastName("LN");
        return testPOJO;
    }

    private String getXML() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><testPOJO><firstName>FN</firstName><lastName>LN</lastName></testPOJO>";
    }

    @XmlRootElement
    static class TestPOJO {
        String firstName;
        String lastName;

        public TestPOJO() {}


        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TestPOJO))
                return false;

            TestPOJO testPOJO = (TestPOJO) o;

            if (getFirstName() != null ? !getFirstName().equals(testPOJO.getFirstName())
                    : testPOJO.getFirstName() != null)
                return false;
            return getLastName() != null ? getLastName().equals(testPOJO.getLastName())
                    : testPOJO.getLastName() == null;
        }

        @Override
        public int hashCode() {
            int result = getFirstName() != null ? getFirstName().hashCode() : 0;
            result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
            return result;
        }
    }


}

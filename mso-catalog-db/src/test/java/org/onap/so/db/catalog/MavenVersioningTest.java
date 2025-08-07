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

package org.onap.so.db.catalog;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.onap.so.db.catalog.utils.MavenLikeVersioningComparator;
import org.onap.so.db.catalog.utils.MavenLikeVersioning;


public class MavenVersioningTest {

    @Test
    public final void testVersion() {
        MavenLikeVersioning mavenVersioning = new MavenLikeVersioning();
        assertFalse(mavenVersioning.isMoreRecentThan("0.0.0"));
        assertFalse(mavenVersioning.isMoreRecentThan(null));
        mavenVersioning.setVersion("0.0.1");

        assertFalse(mavenVersioning.isMoreRecentThan(null));
        assertTrue(mavenVersioning.isMoreRecentThan("0.0.0"));
        assertTrue(mavenVersioning.isMoreRecentThan("0.0.0.1"));

        assertFalse(mavenVersioning.isMoreRecentThan("0.0.2"));
        assertFalse(mavenVersioning.isMoreRecentThan("0.0.1"));
        assertFalse(mavenVersioning.isMoreRecentThan("00.00.01"));

        assertFalse(mavenVersioning.isMoreRecentThan("0.1"));
        assertFalse(mavenVersioning.isMoreRecentThan("1"));
        assertFalse(mavenVersioning.isMoreRecentThan("0.1.0.2"));

        assertFalse(mavenVersioning.isMoreRecentThan("0.1.1"));
        assertFalse(mavenVersioning.isMoreRecentThan("2.1.1"));

        mavenVersioning.setVersion("1.0.1");
        assertTrue(mavenVersioning.isMoreRecentThan("0.0.0"));
        assertTrue(mavenVersioning.isMoreRecentThan("0.5.2"));
        assertTrue(mavenVersioning.isMoreRecentThan("1.0.0"));

        assertFalse(mavenVersioning.isMoreRecentThan("2.1.1"));
        assertFalse(mavenVersioning.isMoreRecentThan("02.001.0001"));
        assertFalse(mavenVersioning.isMoreRecentThan("1.0.1"));
        assertFalse(mavenVersioning.isMoreRecentThan("1.0.2"));
        assertFalse(mavenVersioning.isMoreRecentThan("1.1.1"));
        assertFalse(mavenVersioning.isMoreRecentThan("1.0.10"));


        mavenVersioning.setVersion("100.0.1");
        assertTrue(mavenVersioning.isMoreRecentThan("0.0.0"));
        assertTrue(mavenVersioning.isMoreRecentThan("0.5.2"));
        assertTrue(mavenVersioning.isMoreRecentThan("1.0.0"));

        assertFalse(mavenVersioning.isMoreRecentThan("101.1.1"));
        assertFalse(mavenVersioning.isMoreRecentThan("100.0.1"));
        assertFalse(mavenVersioning.isMoreRecentThan("100.0.2"));
        assertFalse(mavenVersioning.isMoreRecentThan("100.1.1"));

        assertFalse(mavenVersioning.isMoreRecentThan("100.0.1.4"));
    }

    @Test
    public final void testOneDigitVersion() {
        MavenLikeVersioning oneDigit = new MavenLikeVersioning();
        oneDigit.setVersion("1");
        assertFalse(oneDigit.isMoreRecentThan("2"));
        assertFalse(oneDigit.isMoreRecentThan("2.0"));
        assertFalse(oneDigit.isMoreRecentThan("1.0"));

        oneDigit.setVersion("1.0");
        assertTrue(oneDigit.isMoreRecentThan("1"));

        oneDigit.setVersion("1");
        assertFalse(oneDigit.isTheSameVersion("1.1"));
        assertFalse(oneDigit.isTheSameVersion("1.0"));
        assertFalse(oneDigit.isTheSameVersion("1.0.0"));

        oneDigit.setVersion("2");
        assertTrue(oneDigit.isMoreRecentThan("1"));
        assertTrue(oneDigit.isMoreRecentThan("1.0"));
        assertTrue(oneDigit.isMoreRecentThan("1.1"));
        assertTrue(oneDigit.isMoreRecentThan("0.1"));
        assertFalse(oneDigit.isMoreRecentThan("2.0"));

    }

    @Test
    public final void testVersionEquals() {

        MavenLikeVersioning heatTemplate = new MavenLikeVersioning();
        assertFalse(heatTemplate.isTheSameVersion("100.0"));
        assertTrue(heatTemplate.isTheSameVersion(null));
        heatTemplate.setVersion("100.0.1");
        assertFalse(heatTemplate.isTheSameVersion(null));
        assertFalse(heatTemplate.isTheSameVersion("100.0"));
        assertFalse(heatTemplate.isTheSameVersion("100"));
        assertFalse(heatTemplate.isTheSameVersion("100.0.1.1"));
        assertTrue(heatTemplate.isTheSameVersion("100.0.1"));
        assertTrue(heatTemplate.isTheSameVersion("00100.000.0001"));
        assertFalse(heatTemplate.isTheSameVersion("0.0.1"));
        assertTrue(heatTemplate.isTheSameVersion("100.0.01"));

    }

    @Test
    public final void testListSort() {
        MavenLikeVersioning test1 = new MavenLikeVersioning();
        test1.setVersion("1.1");
        MavenLikeVersioning test2 = new MavenLikeVersioning();
        test2.setVersion("1.10");
        MavenLikeVersioning test3 = new MavenLikeVersioning();
        test3.setVersion("1.2");
        MavenLikeVersioning test4 = new MavenLikeVersioning();
        test4.setVersion("1.20");
        MavenLikeVersioning test5 = new MavenLikeVersioning();
        test5.setVersion("1.02");
        MavenLikeVersioning test6 = new MavenLikeVersioning();
        test6.setVersion("2.02");
        MavenLikeVersioning test7 = new MavenLikeVersioning();
        test7.setVersion("0.02");
        MavenLikeVersioning test8 = new MavenLikeVersioning();
        test8.setVersion("2.02");
        MavenLikeVersioning test9 = new MavenLikeVersioning();
        test9.setVersion("10.2004");
        MavenLikeVersioning test10 = new MavenLikeVersioning();
        test10.setVersion("2");
        MavenLikeVersioning test11 = new MavenLikeVersioning();
        test11.setVersion("12");
        MavenLikeVersioning test12 = new MavenLikeVersioning();
        test12.setVersion("2.0");

        List<MavenLikeVersioning> list = new LinkedList<MavenLikeVersioning>();
        list.add(test1);
        list.add(test2);
        list.add(test3);
        list.add(test4);
        list.add(test5);
        list.add(test6);
        list.add(test7);
        list.add(test8);
        list.add(test9);
        list.add(test10);
        list.add(test11);
        list.add(test12);

        Collections.sort(list, new MavenLikeVersioningComparator());
        // Collections.reverse(list);
        assertTrue(list.get(0).getVersion().equals("0.02"));
        assertTrue(list.get(1).getVersion().equals("1.1"));
        assertTrue(list.get(2).getVersion().equals("1.02") || list.get(3).getVersion().equals("1.02"));
        assertTrue(list.get(3).getVersion().equals("1.2") || list.get(2).getVersion().equals("1.2"));
        assertTrue(list.get(4).getVersion().equals("1.10"));
        assertTrue(list.get(5).getVersion().equals("1.20"));
        assertTrue(list.get(6).getVersion().equals("2"));
        assertTrue(list.get(7).getVersion().equals("2.0"));
        assertTrue(list.get(8).getVersion().equals("2.02"));
        assertTrue(list.get(9).getVersion().equals("2.02"));
        assertTrue(list.get(10).getVersion().equals("10.2004"));
        assertTrue(list.get(11).getVersion().equals("12"));

    }
}


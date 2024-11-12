/**
 * ============LICENSE_START==================================================== org.onap.so
 * =========================================================================== Copyright (c) 2018 AT&T Intellectual
 * Property. All rights reserved. =========================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.so.security.cadi.wsse;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Match Class allows you to build an automatic Tree of StAX (or StAX like) Objects for frequent use.
 *
 * OBJECT is a type which you which to do some end Actions on, similar to a Visitor pattern, see Action
 *
 * Note: We have implemented with XReader and XEvent, rather than StAX for performance reasons.
 *
 * @see Action
 * @see Match
 * @see XEvent
 * @see XReader
 *
 * @author Jonathan
 *
 * @param <OUTPUT>
 */
// @SuppressWarnings("restriction")
public class Match<OUTPUT> {
    private QName qname;
    private Match<OUTPUT>[] next;
    private Match<OUTPUT> prev;
    private Action<OUTPUT> action = null;
    private boolean stopAfter;
    private boolean exclusive;


    @SafeVarargs
    public Match(String ns, String name, Match<OUTPUT>... next) {
        this.qname = new QName(ns, name);
        this.next = next;
        stopAfter = exclusive = false;
        for (Match<OUTPUT> m : next) { // add the possible tags to look for
            if (!m.stopAfter)
                m.prev = this;
        }
    }

    public Match<OUTPUT> onMatch(OUTPUT output, XReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            XEvent event = reader.nextEvent();
            switch (event.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    QName e_qname = event.asStartElement().getName();
                    // System.out.println("Start - " + e_qname);
                    boolean match = false;
                    for (Match<OUTPUT> m : next) {
                        if (e_qname.equals(m.qname)) {
                            match = true;
                            if (m.onMatch(output, reader) == null) {
                                return null; // short circuit Parsing
                            }
                            break;
                        }
                    }
                    if (exclusive && !match) // When Tag MUST be present, i.e. the Root Tag, versus info we're not
                                             // interested in
                        return null;
                    break;
                case XMLEvent.CHARACTERS:
                    // System.out.println("Data - " +event.asCharacters().getData());
                    if (action != null) {
                        if (!action.content(output, event.asCharacters().getData())) {
                            return null;
                        }
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    // System.out.println("End - " + event.asEndElement().getName());
                    if (event.asEndElement().getName().equals(qname)) {
                        return prev;
                    }
                    break;
                case XMLEvent.END_DOCUMENT:
                    return null; // Exit Chain
            }
        }
        return this;
    }

    /**
     * When this Matched Tag has completed, Stop parsing and end
     * 
     * @return
     */
    public Match<OUTPUT> stopAfter() {
        stopAfter = true;
        return this;
    }

    /**
     * Mark that this Object MUST be matched at this level or stop parsing and end
     *
     * @param action
     * @return
     */
    public Match<OUTPUT> exclusive() {
        exclusive = true;
        return this;
    }

    public Match<OUTPUT> set(Action<OUTPUT> action) {
        this.action = action;
        return this;
    }
}

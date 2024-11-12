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
import javax.xml.stream.events.XMLEvent;

/**
 * XEvent
 *
 * This mechanism mimics a minimal portion of StAX "XMLEvent", enough to work with minimal XReader.
 *
 * We implement the same interface, as much as minimally necessary, as XMLEvent for these small usages so as to be
 * interchangeable in the future, if so desired
 *
 * @author Jonathan
 *
 */
// @SuppressWarnings("restriction")
public abstract class XEvent {

    public abstract int getEventType();

    public StartElement asStartElement() {
        return (StartElement) this;
    }

    public Characters asCharacters() {
        return (Characters) this;
    }

    public EndElement asEndElement() {
        return (EndElement) this;
    }

    public static abstract class NamedXEvent extends XEvent {
        private QName qname;

        public NamedXEvent(QName qname) {
            this.qname = qname;
        }

        public QName getName() {
            return qname;
        }
    }
    public static class StartElement extends NamedXEvent {

        public StartElement(String ns, String tag) {
            super(new QName(ns, tag));
        }

        @Override
        public int getEventType() {
            return XMLEvent.START_ELEMENT;
        }
    }

    public static class EndElement extends NamedXEvent {
        public EndElement(String ns, String tag) {
            super(new QName(ns, tag));
        }

        @Override
        public int getEventType() {
            return XMLEvent.END_ELEMENT;
        }
    }

    public static class Characters extends XEvent {
        private String data;

        public Characters(String data) {
            this.data = data;
        }

        @Override
        public int getEventType() {
            return XMLEvent.CHARACTERS;
        }

        public String getData() {
            return data;
        }
    }

    public static class StartDocument extends XEvent {

        @Override
        public int getEventType() {
            return XMLEvent.START_DOCUMENT;
        }

    }

    public static class EndDocument extends XEvent {

        @Override
        public int getEventType() {
            return XMLEvent.END_DOCUMENT;
        }

    }
    public static class Comment extends XEvent {
        public final String value;

        public Comment(String value) {
            this.value = value;
        }

        @Override
        public int getEventType() {
            return XMLEvent.COMMENT;
        }

    }

}

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.stream.XMLStreamException;

/**
 * XReader This class works similarly as StAX, except StAX has more behavior than is needed. That would be ok, but StAX
 * also was Buffering in their code in such as way as to read most if not all the incoming stream into memory, defeating
 * the purpose of pre-reading only the Header
 *
 * This Reader does no back-tracking, but is able to create events based on syntax and given state only, leaving the
 * Read-ahead mode of the InputStream up to the other classes.
 *
 * At this time, we only implement the important events, though if this is good enough, it could be expanded, perhaps to
 * replace the original XMLReader from StAX.
 *
 * @author Jonathan
 *
 */
// @SuppressWarnings("restriction")
public class XReader {
    private XEvent curr, another;
    private InputStream is;
    private ByteArrayOutputStream baos;
    private int state, count, last;

    private Stack<Map<String, String>> nsses;

    public XReader(InputStream is) {
        this.is = is;
        curr = another = null;
        baos = new ByteArrayOutputStream();
        state = BEGIN_DOC;
        count = 0;
        nsses = new Stack<Map<String, String>>();
    }

    public boolean hasNext() throws XMLStreamException {
        if (curr == null) {
            curr = parse();
        }
        return curr != null;
    }

    public XEvent nextEvent() {
        XEvent xe = curr;
        curr = null;
        return xe;
    }

    //
    // State Flags
    //
    // Note: The State of parsing XML can be complicated. There are too many to cleanly keep in "booleans".
    // Additionally,
    // there are certain checks that can be better made with Bitwise operations within switches
    // Keeping track of state this way also helps us to accomplish logic without storing any back characters except one
    private final static int BEGIN_DOC = 0x000001;
    private final static int DOC_TYPE = 0x000002;
    private final static int QUESTION_F = 0x000004;
    private final static int QUESTION = 0x000008;
    private final static int START_TAG = 0x000010;
    private final static int END_TAG = 0x000020;
    private final static int VALUE = 0x000040;
    private final static int COMMENT = 0x001000;
    private final static int COMMENT_E = 0x002000;
    private final static int COMMENT_D1 = 0x010000;
    private final static int COMMENT_D2 = 0x020000;
    private final static int COMMENT_D3 = 0x040000;
    private final static int COMMENT_D4 = 0x080000;
    // useful combined Comment states
    private final static int IN_COMMENT = COMMENT | COMMENT_E | COMMENT_D1 | COMMENT_D2;
    private final static int COMPLETE_COMMENT = COMMENT | COMMENT_E | COMMENT_D1 | COMMENT_D2 | COMMENT_D3 | COMMENT_D4;


    private XEvent parse() throws XMLStreamException {
        Map<String, String> nss = nsses.isEmpty() ? null : nsses.peek();

        XEvent rv;
        if ((rv = another) != null) { // "another" is a tag that may have needed to be created, but not
                                      // immediately returned. Save for next parse. If necessary, this could be turned
                                      // into
                                      // a FIFO storage, but a single reference is enough for now.
            another = null; // "rv" is now set for the Event, and will be returned. Set to Null.
        } else {
            boolean go = true;
            int c = 0;

            try {
                while (go && (c = is.read()) >= 0) {
                    ++count;
                    switch (c) {
                        case '<': // Tag is opening
                            state |= ~BEGIN_DOC; // remove BEGIN_DOC flag, this is possibly an XML Doc
                            XEvent cxe = null;
                            if (baos.size() > 0) { // If there are any characters between tags, we send as Character
                                                   // Event
                                String chars = baos.toString().trim(); // Trim out WhiteSpace before and after
                                if (chars.length() > 0) { // don't send if Characters were only whitespace
                                    cxe = new XEvent.Characters(chars);
                                    baos.reset();
                                    go = false;
                                }
                            }
                            last = c; // make sure "last" character is set for use in "ParseTag"
                            Tag t = parseTag(); // call subroutine to process the tag as a unit
                            String ns;
                            switch (t.state & (START_TAG | END_TAG)) {
                                case START_TAG:
                                    nss = getNss(nss, t); // Only Start Tags might have NS Attributes
                                                          // Get any NameSpace elements from tag. If there are, nss will
                                                          // become
                                                          // a new Map with all the previous NSs plus the new. This
                                                          // provides
                                                          // scoping behavior when used with the Stack
                                    // drop through on purpose
                                case END_TAG:
                                    ns = t.prefix == null || nss == null ? "" : nss.get(t.prefix); // Get the namespace
                                                                                                   // from prefix (if
                                                                                                   // exists)
                                    break;
                                default:
                                    ns = "";
                            }
                            if (ns == null)
                                throw new XMLStreamException("Invalid Namespace Prefix at " + count);
                            go = false;
                            switch (t.state) { // based on
                                case DOC_TYPE:
                                    rv = new XEvent.StartDocument();
                                    break;
                                case COMMENT:
                                    rv = new XEvent.Comment(t.value);
                                    break;
                                case START_TAG:
                                    rv = new XEvent.StartElement(ns, t.name);
                                    nsses.push(nss); // Change potential scope for Namespace
                                    break;
                                case END_TAG:
                                    rv = new XEvent.EndElement(ns, t.name);
                                    nss = nsses.pop(); // End potential scope for Namespace
                                    break;
                                case START_TAG | END_TAG: // This tag is both start/end aka <myTag/>
                                    rv = new XEvent.StartElement(ns, t.name);
                                    if (last == '/')
                                        another = new XEvent.EndElement(ns, t.name);
                            }
                            if (cxe != null) { // if there is a Character Event, it actually should go first. ow.
                                another = rv; // Make current Event the "another" or next event, and
                                rv = cxe; // send Character Event now
                            }
                            break;
                        case ' ':
                        case '\t':
                        case '\n':
                            if ((state & BEGIN_DOC) == BEGIN_DOC) { // if Whitespace before doc, just ignore
                                break;
                            }
                            // fallthrough on purpose
                        default:
                            if ((state & BEGIN_DOC) == BEGIN_DOC) { // if there is any data at the start other than XML
                                                                    // Tag, it's not XML
                                throw new XMLStreamException("Parse Error: This is not an XML Doc");
                            }
                            baos.write(c); // save off Characters
                    }
                    last = c; // Some processing needs to know what the last character was, aka Escaped characters... ex
                              // \"
                }
            } catch (IOException e) {
                throw new XMLStreamException(e); // all errors parsing will be treated as XMLStreamErrors (like StAX)
            }
            if (c == -1 && (state & BEGIN_DOC) == BEGIN_DOC) { // Normally, end of stream is ok, however, we need to
                                                               // know if the
                throw new XMLStreamException("Premature End of File"); // document isn't an XML document, so we throw
                                                                       // exception if it
            } // hasn't yet been determined to be an XML Doc
        }
        return rv;
    }

    /**
     * parseTag
     *
     * Parsing a Tag is somewhat complicated, so it's helpful to separate this process from the higher level Parsing
     * effort
     * 
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    private Tag parseTag() throws IOException, XMLStreamException {
        Tag tag = null;
        boolean go = true;
        state = 0;
        int c, quote = 0; // If "quote" is 0, then we're not in a quote. We set ' (in pretag) or " in attribs
                          // accordingly to denote quoted
        String prefix = null, name = null, value = null;
        baos.reset();

        while (go && (c = is.read()) >= 0) {
            ++count;
            if (quote != 0) { // If we're in a quote, we only end if we hit another quote of the same time, not preceded
                              // by \
                if (c == quote && last != '\\') {
                    quote = 0;
                } else {
                    baos.write(c);
                }
            } else if ((state & COMMENT) == COMMENT) { // similar to Quote is being in a comment
                switch (c) {
                    case '-':
                        switch (state) { // XML has a complicated Quote set... <!-- --> ... we keep track if each has
                                         // been met with flags.
                            case COMMENT | COMMENT_E:
                                state |= COMMENT_D1;
                                break;
                            case COMMENT | COMMENT_E | COMMENT_D1:
                                state |= COMMENT_D2;
                                baos.reset(); // clear out "!--", it's a Comment
                                break;
                            case COMMENT | COMMENT_E | COMMENT_D1 | COMMENT_D2:
                                state |= COMMENT_D3;
                                baos.write(c);
                                break;
                            case COMMENT | COMMENT_E | COMMENT_D1 | COMMENT_D2 | COMMENT_D3:
                                state |= COMMENT_D4;
                                baos.write(c);
                                break;
                        }
                        break;
                    case '>': // Tag indicator has been found, do we have all the comment characters in line?
                        if ((state & COMPLETE_COMMENT) == COMPLETE_COMMENT) {
                            byte ba[] = baos.toByteArray();
                            tag = new Tag(null, null, new String(ba, 0, ba.length - 2));
                            baos.reset();
                            go = false;
                            break;
                        }
                        // fall through on purpose
                    default:
                        state &= ~(COMMENT_D3 | COMMENT_D4);
                        if ((state & IN_COMMENT) != IN_COMMENT)
                            state &= ~IN_COMMENT; // false alarm, it's not actually a comment
                        baos.write(c);
                }
            } else { // Normal Tag Processing loop
                switch (c) {
                    case '?':
                        switch (state & (QUESTION_F | QUESTION)) { // Validate the state of Doc tag... <?xml ... ?>
                            case QUESTION_F:
                                state |= DOC_TYPE;
                                state &= ~QUESTION_F;
                                break;
                            case 0:
                                state |= QUESTION_F;
                                break;
                            default:
                                throw new IOException("Bad character [?] at " + count);
                        }
                        break;
                    case '!':
                        if (last == '<') {
                            state |= COMMENT | COMMENT_E; // likely a comment, continue processing in Comment Loop
                        }
                        baos.write(c);
                        break;
                    case '/':
                        state |= (last == '<' ? END_TAG : (END_TAG | START_TAG)); // end tag indicator </xxx>, ,or both
                                                                                  // <xxx/>
                        break;
                    case ':':
                        prefix = baos.toString(); // prefix indicator
                        baos.reset();
                        break;
                    case '=': // used in Attributes
                        name = baos.toString();
                        baos.reset();
                        state |= VALUE;
                        break;
                    case '>': // end the tag, which causes end of this subprocess as well as formulation of the found
                              // data
                        go = false;
                        // passthrough on purpose
                    case ' ':
                    case '\t':
                    case '\n': // white space indicates change in internal tag state, ex between name and between
                               // attributes
                        if ((state & VALUE) == VALUE) {
                            value = baos.toString(); // we're in VALUE state, add characters to Value
                        } else if (name == null) {
                            name = baos.toString(); // we're in Name state (default) add characters to Name
                        }
                        baos.reset(); // we've assigned chars, reset buffer
                        if (name != null) { // Name is not null, there's a tag in the offing here...
                            Tag t = new Tag(prefix, name, value);
                            if (tag == null) { // Set as the tag to return, if not exists
                                tag = t;
                            } else { // if we already have a Tag, then we'll treat this one as an attribute
                                tag.add(t);
                            }
                        }
                        prefix = name = value = null; // reset these values in case we loop for attributes.
                        break;
                    case '\'': // is the character one of two kinds of quote?
                    case '"':
                        if (last != '\\') {
                            quote = c;
                            break;
                        }
                        // Fallthrough ok
                    default:
                        baos.write(c); // write any unprocessed bytes into buffer

                }
            }
            last = c;
        }
        int type = state & (DOC_TYPE | COMMENT | END_TAG | START_TAG); // get just the Tag states and turn into Type for
                                                                       // Tag
        if (type == 0) {
            type = START_TAG;
        }
        if (tag != null) {
            tag.state |= type; // add the appropriate Tag States
        }
        return tag;
    }

    /**
     * getNSS
     *
     * If the tag contains some Namespace attributes, create a new nss from the passed in one, copy all into it, then
     * add This provides Scoping behavior
     *
     * if Nss is null in the first place, create an new nss, so we don't have to deal with null Maps.
     *
     * @param nss
     * @param t
     * @return
     */
    private Map<String, String> getNss(Map<String, String> nss, Tag t) {
        Map<String, String> newnss = null;
        if (t.attribs != null) {
            for (Tag tag : t.attribs) {
                if ("xmlns".equals(tag.prefix)) {
                    if (newnss == null) {
                        newnss = new HashMap<>();
                        if (nss != null)
                            newnss.putAll(nss);
                    }
                    newnss.put(tag.name, tag.value);
                }
            }
        }
        // return newnss==null?(nss==null?new HashMap<String,String>():nss):newnss;
        if (newnss == null) {
            if (nss == null) {
                newnss = new HashMap<>();
            } else {
                newnss = nss;
            }
        }
        return newnss;
    }

    /**
     * The result of the parseTag method
     *
     * Data is split up into prefix, name and value portions. "Tags" with Values that are inside a Tag are known in XLM
     * as Attributes.
     *
     * @author Jonathan
     *
     */
    public class Tag {
        public int state;
        public String prefix, name, value;
        public List<Tag> attribs;

        public Tag(String prefix, String name, String value) {
            this.prefix = prefix;
            this.name = name;
            this.value = value;
            attribs = null;
        }

        /**
         * add an attribute Not all tags need attributes... lazy instantiate to save time and memory
         * 
         * @param tag
         */
        public void add(Tag attrib) {
            if (attribs == null) {
                attribs = new ArrayList<>();
            }
            attribs.add(attrib);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (prefix != null) {
                sb.append(prefix);
                sb.append(':');
            }
            sb.append(name == null ? "!!ERROR!!" : name);

            char quote = ((state & DOC_TYPE) == DOC_TYPE) ? '\'' : '"';
            if (value != null) {
                sb.append('=');
                sb.append(quote);
                sb.append(value);
                sb.append(quote);
            }
            return sb.toString();
        }
    }

}

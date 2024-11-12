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

package org.onap.so.security.cadi;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Capacitor
 *
 * Storage mechanism for read data, specifically designed for InputStreams.
 *
 * The Standard BufferedInputStream requires a limit to be set for buffered reading, which is impractical for reading
 * SOAP headers, which can be quite large.
 * 
 * @author Jonathan
 *
 */
public class Capacitor {
    private static final int DEFAULT_CHUNK = 256;
    private ArrayList<ByteBuffer> bbs = new ArrayList<>();
    private ByteBuffer curr = null;
    private int idx;

    // Maintain a private RingBuffer for Memory, for efficiency
    private static ByteBuffer[] ring = new ByteBuffer[16];
    private static int start, end;


    public void put(byte b) {
        if (curr == null || curr.remaining() == 0) { // ensure we have a "curr" buffer ready for data
            curr = ringGet();
            bbs.add(curr);
        }
        curr.put(b);
    }

    public int read() {
        if (curr != null) {
            if (curr.remaining() > 0) { // have a buffer, use it!
                return curr.get();
            } else if (idx < bbs.size()) { // Buffer not enough, get next one from array
                curr = bbs.get(idx++);
                return curr.get();
            }
        } // if no curr buffer, treat as end of stream
        return -1;
    }

    /**
     * read into an array like Streams
     *
     * @param array
     * @param offset
     * @param length
     * @return
     */
    public int read(byte[] array, int offset, int length) {
        if (curr == null)
            return -1;
        int len;
        int count = 0;
        while (length > 0) { // loop through while there's data needed
            if ((len = curr.remaining()) > length) { // if enough data in curr buffer, use this code
                curr.get(array, offset, length);
                count += length;
                length = 0;
            } else { // get data from curr, mark how much is needed to fulfil, and loop for next curr.
                curr.get(array, offset, len);
                count += len;
                offset += len;
                length -= len;
                if (idx < bbs.size()) {
                    curr = bbs.get(idx++);
                } else {
                    length = 0; // stop, and return the count of how many we were able to load
                }
            }
        }
        return count;
    }

    /**
     * Put an array of data into Capacitor
     *
     * @param array
     * @param offset
     * @param length
     */
    public void put(byte[] array, int offset, int length) {
        if (curr == null || curr.remaining() == 0) {
            curr = ringGet();
            bbs.add(curr);
        }

        int len;
        while (length > 0) {
            if ((len = curr.remaining()) > length) {
                curr.put(array, offset, length);
                length = 0;
            } else {
                // System.out.println(new String(array));
                curr.put(array, offset, len);
                length -= len;
                offset += len;
                curr = ringGet();
                bbs.add(curr);
            }
        }
    }

    /**
     * Move state from Storage mode into Read mode, changing all internal buffers to read mode, etc
     */
    public void setForRead() {
        for (ByteBuffer bb : bbs) {
            bb.flip();
        }
        if (bbs.isEmpty()) {
            curr = null;
            idx = 0;
        } else {
            curr = bbs.get(0);
            idx = 1;
        }
    }

    /**
     * reuse all the buffers
     */
    public void done() {
        for (ByteBuffer bb : bbs) {
            ringPut(bb);
        }
        bbs.clear();
        curr = null;
    }

    /**
     * Declare amount of data available to be read at once.
     *
     * @return
     */
    public int available() {
        int count = 0;
        for (ByteBuffer bb : bbs) {
            count += bb.remaining();
        }
        return count;
    }

    /**
     * Returns how many are left that were not skipped
     * 
     * @param n
     * @return
     */
    public long skip(long n) {
        long skipped = 0L;
        int skip;
        if (curr == null) {
            return 0;
        }
        while (n > 0) {
            if (n < (skip = curr.remaining())) {
                curr.position(curr.position() + (int) n);
                skipped += skip;
                n = 0;
            } else {
                curr.position(curr.limit());

                skipped -= skip;
                if (idx < bbs.size()) {
                    curr = bbs.get(idx++);
                    n -= skip;
                } else {
                    n = 0;
                }
            }
        }
        return skipped > 0 ? skipped : 0;
    }

    /**
     * Be able to re-read data that is stored that has already been re-read. This is not a standard Stream behavior, but
     * can be useful in a standalone mode.
     */
    public void reset() {
        for (ByteBuffer bb : bbs) {
            bb.position(0);
        }
        if (bbs.isEmpty()) {
            curr = null;
            idx = 0;
        } else {
            curr = bbs.get(0);
            idx = 1;
        }
    }

    /*
     * Ring Functions. Reuse allocated memory
     */
    private ByteBuffer ringGet() {
        ByteBuffer bb = null;
        synchronized (ring) {
            bb = ring[start];
            ring[start] = null;
            if (bb != null && ++start > 15)
                start = 0;
        }
        if (bb == null) {
            bb = ByteBuffer.allocate(DEFAULT_CHUNK);
        } else {
            bb.clear();// refresh reused buffer
        }
        return bb;
    }

    private void ringPut(ByteBuffer bb) {
        synchronized (ring) {
            ring[end] = bb; // if null or not, BB will just be Garbage collected
            if (++end > 15)
                end = 0;
        }
    }

}

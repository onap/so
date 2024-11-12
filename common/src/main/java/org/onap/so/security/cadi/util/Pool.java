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

/*
 * Pool
 *
 * Author: Jonathan 5/27/2011
 */
package org.onap.so.security.cadi.util;

import java.util.Iterator;
import java.util.LinkedList;
import org.onap.so.security.cadi.CadiException;

/**
 * This Class pools on an As-Needed-Basis any particular kind of class, which is quite suitable for expensive
 * operations.
 *
 * The user calls "get" on a Pool, and if a waiting resource (T) is available, it will be returned. Otherwise, one will
 * be created with the "Creator" class (must be defined for (T)).
 *
 * You can Prime the instances to avoid huge startup costs
 *
 * The returned "Pooled" object simply has to call "done()" and the object is returned to the pool. If the developer
 * does not return the object, a memory leak does not occur. There are no references to the object once "get" is called.
 * However, the developer who does not return the object when done obviates the point of the pool, as new Objects are
 * created in place of the Object not returned when another call to "get" is made.
 *
 * There is a cushion of extra objects, currently defaulted to MAX_RANGE. If the items returned become higher than the
 * MAX_RANGE, the object is allowed to go out of scope, and be cleaned up. the default can be changed on a per-pool
 * basis.
 *
 * Class revamped for CadiExceptions and Access logging 10/4/2017
 *
 * @author Jonathan
 *
 * @param <T>
 */
public class Pool<T> {
    /**
     * This is a constant which specified the default maximum number of unused objects to be held at any given time.
     */
    public static final int MAX_RANGE = 6; // safety

    /**
     * Maximum objects, in use or waiting
     */
    public static final int MAX_OBJECTS = 20; // assumption for thread

    /**
     * only Simple List needed.
     *
     * NOTE TO MAINTAINERS: THIS OBJECT DOES IT'S OWN SYNCHRONIZATION. All changes that touch list must account for
     * correctly synchronizing list.
     */
    private LinkedList<Pooled<T>> list;

    /**
     * keep track of how many elements are currently available to use, to avoid asking list.
     */
    private int count;

    /**
     * how many objects have been asked for, but not returned or tossed
     */
    private int used;

    /**
     * Actual MAX number of spares allowed to hang around. Can be set to something besides the default MAX_RANGE.
     */
    private int max_range = MAX_RANGE;

    /**
     * Actual MAX number of Objects both in use, or waiting. This does not actually affect the Pool, because the
     * objects, once they leave the pool, are not known until they are put back with done (offer). It only affects the
     * "overLimit()" function.
     * 
     * Important... this information is only valid if PooledObjects call "done()" or "toss()".
     */
    private int max_objects = MAX_OBJECTS;

    /**
     * The Creator for this particular pool. It must work for type T.
     */
    private Creator<T> creator;

    private Log logger;

    /**
     * Create a new Pool, given the implementation of Creator<T>, which must be able to create/destroy T objects at
     * will.
     *
     * @param creator
     */
    public Pool(Creator<T> creator) {
        count = used = 0;
        this.creator = creator;
        list = new LinkedList<>();
        logger = Log.NULL;
    }

    /**
     * Attach Pool Logging activities to any other Logging Mechanism.
     * 
     * @param logger
     */
    public void setLogger(Log logger) {
        this.logger = logger;
        // Also reset existing Pooled objects
        for (Pooled<?> p : list) {
            if (p.content instanceof LogAware) {
                ((LogAware) p.content).setLog(logger);
            } else {
                break;
            }
        }
    }

    public void log(Log.Type type, Object... objects) {
        logger.log(type, objects);
    }

    /**
     * Preallocate a certain number of T Objects. Useful for services so that the first transactions don't get hit with
     * all the Object creation costs
     * 
     * It is assumed that priming also means that it is the minimum desired available resources. Therefore, max_range is
     * set to prime, if less than current max_range, if it is default.
     * 
     * @param lt
     * @param prime
     * @throws CadiException
     */
    public Pool<T> prime(int prime) throws CadiException {
        if (max_range == MAX_RANGE && prime < max_range) {
            max_range = prime;
        }
        for (int i = 0; i < prime; ++i) {
            Pooled<T> pt = new Pooled<T>(creator.create(), this);
            synchronized (list) {
                list.addFirst(pt);
                ++count;
                ++used;
            }
        }
        return this;
    }

    /**
     * Destroy and remove all remaining objects. This is valuable for closing down all Allocated objects cleanly for
     * exiting. It is also a good method for removing objects when, for instance, all Objects are invalid because of
     * broken connections, etc.
     * 
     * Use in conjunction with setMaxRange to no longer store objects, i.e.
     * 
     * pool.setMaxRange(0).drain();
     */
    public synchronized void drain() {
        while (list.size() > 0) {
            Pooled<T> pt = list.remove();
            --used;
            String name = pt.content.toString();
            creator.destroy(pt.content);
            logger.log(Log.Type.debug, "Pool destroyed", name);
        }
        count = 0;
    }

    /**
     * This is the essential function for Pool. Get an Object "T" inside a "Pooled<T>" object. If there is a spare
     * Object, then use it. If not, then create and pass back.
     *
     * This one uses a Null LogTarget
     *
     * IMPORTANT: When the use of this object is done (and the object is still in a valid state), then "done()" should
     * be called immediately to allow the object to be reused. That is the point of the Pool...
     *
     * If the Object is in an invalid state, then "toss()" should be used so the Pool doesn't pass on invalid objects to
     * others.
     *
     * @param lt
     * @return
     * @throws CadiException
     */
    public Pooled<T> get() throws CadiException {
        Pooled<T> pt;
        synchronized (list) {
            pt = list.pollLast();
        }
        if (pt == null) {
            pt = new Pooled<T>(creator.create(), this);
            ++used;
        } else {
            --count;
            creator.reuse(pt.content);
        }
        return pt;
    }

    /**
     * This function will validate whether the Objects are still in a usable state. If not, they are tossed from the
     * Pool. This is valuable to have when Remote Connections go down, and there is a question on whether the Pooled
     * Objects are still functional.
     *
     * @return
     */
    public boolean validate() {
        boolean rv = true;
        synchronized (list) {
            for (Iterator<Pooled<T>> iter = list.iterator(); iter.hasNext();) {
                Pooled<T> t = iter.next();
                if (!creator.isValid(t.content)) {
                    rv = false;
                    t.toss();
                    iter.remove();
                }
            }
        }
        return rv;
    }

    /**
     * This is an internal method, used only by the Internal Pooled<T> class.
     *
     * The Pooled<T> class "offers" it's Object back after use. It is an "offer", because Pool will simply destroy and
     * remove the object if it has more than enough spares.
     *
     * @param lt
     * @param used
     * @return
     */
    // Used only by Pooled<T>
    private boolean offer(Pooled<T> usedP) {
        if (count < max_range) {
            synchronized (list) {
                list.addFirst(usedP);
                ++count;
            }
            logger.log(Log.Type.trace, "Pool recovered ", creator);
        } else {
            destroy(usedP.content);
        }
        return false;
    }

    /**
     * Destroy, using Creator's specific semantics, the Object, and decrement "used"
     * 
     * @param t
     */
    private void destroy(T t) {
        creator.destroy(t);
        synchronized (list) {
            --used;
        }
        logger.log(Log.Type.debug, "Pool destroyed ", creator);
    }

    /**
     * The Creator Interface give the Pool the ability to Create, Destroy and Validate the Objects it is maintaining.
     * Thus, it is a specially written Implementation for each type.
     *
     * @author Jonathan
     *
     * @param <T>
     */
    public interface Creator<T> {
        public T create() throws CadiException;

        public void destroy(T t);

        public boolean isValid(T t);

        public void reuse(T t);
    }

    /**
     * Pooled Classes can be "Log Aware", which means they can tie into the same Logging element that the Pool is using.
     * To do this, the Object must implement "LogAware"
     * 
     * @author Jonathan
     *
     */
    public interface LogAware {
        public void setLog(Log log);
    }

    /**
     * The "Pooled<T>" class is the transient class that wraps the actual Object T for API use/ It gives the ability to
     * return ("done()", or "toss()") the Object to the Pool when processing is finished.
     *
     * For Safety, i.e. to avoid memory leaks and invalid Object States, there is a "finalize" method. It is strictly
     * for when coder forgets to return the object, or perhaps hasn't covered the case during Exceptions or Runtime
     * Exceptions with finally (preferred). This should not be considered normal procedure, as finalize() is called at
     * an undetermined time during garbage collection, and is thus rather useless for a Pool. However, we don't want
     * Coding Mistakes to put the whole program in an invalid state, so if something happened such that "done()" or
     * "toss()" were not called, the resource is still cleaned up as well as possible.
     *
     * @author Jonathan
     *
     * @param <T>
     */
    public static class Pooled<T> {
        public final T content;
        private Pool<T> pool;

        /**
         * Create the Wrapping Object Pooled<T>.
         *
         * @param t
         * @param pool
         * @param logTarget
         */
        public Pooled(T t, Pool<T> pool) {
            content = t;
            if (t instanceof LogAware) {
                ((LogAware) t).setLog(pool.logger);
            }
            this.pool = pool;
        }

        /**
         * This is the key API for the Pool, as calling "done()" offers this object back to the Pool for reuse.
         *
         * Do not use the Pooled<T> object again after calling "done()".
         */
        public void done() {
            if (pool != null) {
                pool.offer(this);
            }
        }

        /**
         * The user of the Object may discover that the Object t is no longer in a valid state. Don't put Garbage back
         * in the Refrigerator... Toss it, if it's no longer valid.
         *
         * toss() is also used for draining the Pool, etc.
         *
         * toss() will attempt to destroy the Object by using the Creator Interface.
         *
         */
        public void toss() {
            if (pool != null) {
                pool.destroy(content);
            }
            // Don't allow finalize to put it back in.
            pool = null;
        }

        /**
         * Just in case someone neglected to offer back object... Do not rely on this, as there is no specific time when
         * finalize is called, which rather defeats the purpose of a Pool.
         */
        @Override
        protected void finalize() throws Throwable {
            if (pool != null) {
                done();
                pool = null;
            }
        }

        @Override
        public String toString() {
            return content.toString();
        }
    }

    /**
     * Set a Max Range for numbers of spare objects waiting to be used.
     *
     * No negative numbers are allowed
     * 
     * Use in conjunction with drain to no longer store objects, i.e.
     * 
     * pool.setMaxRange(0).drain();
     *
     * @return
     */
    public Pool<T> setMaxRange(int max_range) {
        // Do not allow negative numbers
        this.max_range = Math.max(0, max_range);
        return this;
    }

    /**
     * Set a Max Range for numbers of spare objects waiting to be used.
     *
     * No negative numbers are allowed
     *
     * @return
     */
    public Pool<T> setMaxObjects(int max_objects) {
        // Do not allow negative numbers
        this.max_objects = Math.max(0, max_objects);
        return this;
    }

    /**
     * return whether objects in use or waiting are beyond max allowed
     * 
     * Pool does not actually stop new creations, but allows this to be used by other entities to limit number of
     * creations of expensive Objects, like Thread Pooling
     *
     */
    public boolean tooManyObjects() {
        return used > max_objects;
    }

    public String toString() {
        return String.format("Pool: count(%d), used(%d), max_range(%d), max_objects(%d)", count, used, max_range,
                max_objects);
    }
}

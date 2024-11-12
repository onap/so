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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.so.security.cadi.Access.Level;
import org.onap.so.security.cadi.CachedPrincipal.Resp;
import org.onap.so.security.cadi.principal.CachedBasicPrincipal;

/**
 * Implement Fast lookup and Cache for Local User Info
 *
 * Include ability to add and remove Users
 *
 * Also includes a Timer Thread (when necessary) to invoke cleanup on expiring Credentials
 *
 * @author Jonathan
 *
 */
public abstract class AbsUserCache<PERM extends Permission> {
    // Need an obvious key for when there is no Authentication Cred
    private static final String NO_CRED = "NoCred";
    static final int MIN_INTERVAL = 1000 * 60; // Min 1 min
    static final int MAX_INTERVAL = 1000 * 60 * 60 * 4; // 4 hour max
    private static Timer timer;
    // Map of userName to User
    private final Map<String, User<PERM>> userMap;
    private static final Map<String, Miss> missMap = new TreeMap<>();
    private final Symm missEncrypt;

    private Clean clean;
    protected Access access;

    protected AbsUserCache(Access access, long cleanInterval, int highCount, int usageCount) {
        this.access = access;
        Symm s;
        try {
            byte[] gennedKey = Symm.keygen();
            s = Symm.obtain(new ByteArrayInputStream(gennedKey));
        } catch (IOException e) {
            access.log(e);
            s = Symm.base64noSplit;
        }
        missEncrypt = s;

        userMap = new ConcurrentHashMap<>();


        if (cleanInterval > 0) {
            cleanInterval = Math.max(MIN_INTERVAL, cleanInterval);
            synchronized (AbsUserCache.class) { // Lazy instantiate.. in case there is no cleanup needed
                if (timer == null) {
                    timer = new Timer("CADI Cleanup Timer", true);
                }

                timer.schedule(clean = new Clean(access, cleanInterval, highCount, usageCount), cleanInterval,
                        cleanInterval);
                access.log(Access.Level.INIT, "Cleaning Thread initialized with interval of", cleanInterval,
                        "ms and max objects of", highCount);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public AbsUserCache(AbsUserCache<PERM> cache) {
        this.access = cache.access;
        userMap = cache.userMap;
        missEncrypt = cache.missEncrypt;

        synchronized (AbsUserCache.class) {
            if (cache.clean != null && cache.clean.lur == null && this instanceof CachingLur) {
                cache.clean.lur = (CachingLur<PERM>) this;
            }
        }
    }

    protected void setLur(CachingLur<PERM> lur) {
        if (clean != null)
            clean.lur = lur;

    }

    protected void addUser(User<PERM> user) {
        Principal p = user.principal;
        String key;
        try {
            if (p instanceof GetCred) {
                key = missKey(p.getName(), ((GetCred) p).getCred());
            } else {
                byte[] cred;
                if ((cred = user.getCred()) == null) {
                    key = user.name + NO_CRED;
                } else {
                    key = missKey(user.name, cred);
                }
            }
        } catch (IOException e) {
            access.log(e);
            return;
        }
        userMap.put(key, user);
    }

    // Useful for looking up by WebToken, etc.
    protected void addUser(String key, User<PERM> user) {
        userMap.put(key, user);
    }

    /**
     * Add miss to missMap. If Miss exists, or too many tries, returns false.
     *
     * otherwise, returns true to allow another attempt.
     *
     * @param key
     * @param bs
     * @return
     * @throws IOException
     */
    protected synchronized boolean addMiss(String key, byte[] bs) {
        String mkey;
        try {
            mkey = missKey(key, bs);
        } catch (IOException e) {
            access.log(e);
            return false;
        }
        Miss miss = missMap.get(mkey);
        if (miss == null) {
            missMap.put(mkey, new Miss(bs, clean == null ? MIN_INTERVAL : clean.timeInterval, key));
            return true;
        }
        return miss.mayContinue();
    }

    protected Miss missed(String key, byte[] bs) throws IOException {
        return missMap.get(missKey(key, bs));
    }

    protected User<PERM> getUser(Principal principal) {
        String key;
        if (principal instanceof GetCred) {
            GetCred gc = (GetCred) principal;
            try {
                key = missKey(principal.getName(), gc.getCred());
            } catch (IOException e) {
                access.log(e, "Error getting key from Principal");
                key = principal.getName();
            }
        } else {
            key = principal.getName() + NO_CRED;
        }
        User<PERM> u = userMap.get(key);
        if (u != null) {
            u.incCount();
        }
        return u;
    }

    protected User<PERM> getUser(CachedBasicPrincipal cbp) {
        return getUser(cbp.getName(), cbp.getCred());
    }

    protected User<PERM> getUser(String user, byte[] cred) {
        User<PERM> u;
        String key = null;
        try {
            key = missKey(user, cred);
        } catch (IOException e) {
            access.log(e);
            return null;
        }
        u = userMap.get(key);
        if (u != null) {
            if (u.permExpired()) {
                userMap.remove(key);
                u = null;
            } else {
                u.incCount();
            }
        }
        return u;
    }

    /**
     * Removes User from the Cache
     * 
     * @param user
     */
    protected void remove(User<PERM> user) {
        userMap.remove(user.principal.getName());
    }

    /**
     * Removes user from the Cache
     *
     * @param user
     */
    public void remove(String user) {
        Object o = userMap.remove(user);
        if (o != null) {
            access.log(Level.INFO, user, "removed from Client Cache by Request");
        }
    }

    /**
     * Clear all Users from the Client Cache
     */
    public void clearAll() {
        userMap.clear();
    }

    public final List<DumpInfo> dumpInfo() {
        List<DumpInfo> rv = new ArrayList<>();
        for (User<PERM> user : userMap.values()) {
            rv.add(new DumpInfo(user));
        }
        return rv;
    }

    /**
     * The default behavior of a LUR is to not handle something exclusively.
     */
    public boolean handlesExclusively(Permission... pond) {
        return false;
    }

    /**
     * Container calls when cleaning up...
     *
     * If overloading in Derived class, be sure to call "super.destroy()"
     */
    public void destroy() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
    }



    // Simple map of Group name to a set of User Names
    // private Map<String, Set<String>> groupMap = new HashMap<>();

    /**
     * Class to hold a small subset of the data, because we don't want to expose actual Permission or User Objects
     */
    public final class DumpInfo {
        public String user;
        public List<String> perms;

        public DumpInfo(User<PERM> user) {
            this.user = user.principal.getName();
            perms = new ArrayList<>(user.perms.keySet());
        }
    }

    /**
     * Clean will examine resources, and remove those that have expired.
     *
     * If "highs" have been exceeded, then we'll expire 10% more the next time. This will adjust after each run without
     * checking contents more than once, making a good average "high" in the minimum speed.
     *
     * @author Jonathan
     *
     */
    private final class Clean extends TimerTask {
        private final Access access;
        private CachingLur<PERM> lur;

        // The idea here is to not be too restrictive on a high, but to Expire more items by
        // shortening the time to expire. This is done by judiciously incrementing "advance"
        // when the "highs" are exceeded. This effectively reduces numbers of cached items quickly.
        private final int high;
        private long advance;
        private final long timeInterval;
        private final int usageTriggerCount;

        public Clean(Access access, long cleanInterval, int highCount, int usageTriggerCount) {
            this.access = access;
            lur = null;
            high = highCount;
            timeInterval = cleanInterval;
            advance = 0;
            this.usageTriggerCount = usageTriggerCount;
        }

        public void run() {
            int renewed = 0;
            int count = 0;
            int total = 0;
            try {
                // look at now. If we need to expire more by increasing "now" by "advance"
                ArrayList<User<PERM>> al = new ArrayList<>(userMap.values().size());
                al.addAll(0, userMap.values());
                long now = System.currentTimeMillis() + advance;
                for (User<PERM> user : al) {
                    ++total;
                    if (user.count > usageTriggerCount) {
                        boolean touched = false, removed = false;
                        if (user.principal instanceof CachedPrincipal) {
                            CachedPrincipal cp = (CachedPrincipal) user.principal;
                            if (cp.expires() < now) {
                                switch (cp.revalidate(null)) {
                                    case INACCESSIBLE:
                                        access.log(Level.AUDIT, "AAF Inaccessible.  Keeping credentials");
                                        break;
                                    case REVALIDATED:
                                        user.resetCount();
                                        touched = true;
                                        break;
                                    default:
                                        user.resetCount();
                                        remove(user);
                                        ++count;
                                        removed = true;
                                        break;
                                }
                            }
                        }

                        if (!removed && lur != null && user.permExpires <= now) {
                            if (lur.reload(user).equals(Resp.REVALIDATED)) {
                                user.renewPerm();
                                access.log(Level.DEBUG, "Reloaded Perms for", user);
                                touched = true;
                            }
                        }
                        user.resetCount();
                        if (touched) {
                            ++renewed;
                        }

                    } else {
                        if (user.permExpired()) {
                            remove(user);
                            ++count;
                        }
                    }
                }

                // Clean out Misses
                int missTotal = missMap.keySet().size();
                int miss = 0;
                if (missTotal > 0) {
                    ArrayList<String> keys = new ArrayList<>(missTotal);
                    keys.addAll(missMap.keySet());
                    for (String key : keys) {
                        Miss m = missMap.get(key);
                        if (m != null) {
                            long timeLeft = m.timestamp - System.currentTimeMillis();
                            if (timeLeft < 0) {
                                synchronized (missMap) {
                                    missMap.remove(key);
                                }
                                access.log(Level.INFO, m.name,
                                        " has been removed from Missed Credential Map (" + m.tries + " invalid tries)");
                                ++miss;
                            } else {
                                access.log(Level.INFO, m.name, " remains in Missed Credential Map (" + m.tries
                                        + " invalid tries) for " + (timeLeft / 1000) + " more seconds");
                            }
                        }
                    }
                }

                if (count + renewed + miss > 0) {
                    access.log(Level.INFO, (lur == null ? "Cache" : lur.getClass().getSimpleName()), "removed", count,
                            "and renewed", renewed, "expired Permissions out of", total, "and removed", miss,
                            "password misses out of", missTotal);
                }

                // If High (total) is reached during this period, increase the number of expired services removed for
                // next time.
                // There's no point doing it again here, as there should have been cleaned items.
                if (total > high) {
                    // advance cleanup by 10%, without getting greater than timeInterval.
                    advance = Math.min(timeInterval, advance + (timeInterval / 10));
                } else {
                    // reduce advance by 10%, without getting lower than 0.
                    advance = Math.max(0, advance - (timeInterval / 10));
                }
            } catch (Exception e) {
                access.log(Level.ERROR, e.getMessage());
            }
        }
    }


    private String missKey(String name, byte[] bs) throws IOException {
        return name + Hash.toHex(missEncrypt.encode(bs));
    }

    protected static class Miss {
        private static final int MAX_TRIES = 3;

        long timestamp;

        private long timetolive;

        private long tries;

        private final String name;

        public Miss(final byte[] first, final long timeInterval, final String name) {
            timestamp = System.currentTimeMillis() + timeInterval;
            this.timetolive = timeInterval;
            tries = 0L;
            this.name = name;
        }


        public synchronized boolean mayContinue() {
            long ts = System.currentTimeMillis();
            if (ts > timestamp) {
                tries = 0;
                timestamp = ts + timetolive;
            } else if (MAX_TRIES <= ++tries) {
                return false;
            }
            return true;
        }

    }

    /**
     * Report on state
     */
    public String toString() {
        return getClass().getSimpleName() + " Cache:\n  Users Cached: " + userMap.size() + "\n  Misses Saved: "
                + missMap.size() + '\n';

    }

    public void clear(Principal p, StringBuilder sb) {
        sb.append(toString());
        userMap.clear();
        missMap.clear();
        access.log(Level.AUDIT, p.getName(), "has cleared User Cache in", getClass().getSimpleName());
        sb.append("Now cleared\n");
    }

}

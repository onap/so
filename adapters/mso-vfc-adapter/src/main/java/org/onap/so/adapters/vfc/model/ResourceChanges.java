/***
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import java.util.List;

public class ResourceChanges {
    private List<AffectedVnf> affectedVnfs;
    private List<AffectedPnf> affectedPnfs;
    private List<AffectedVirtualLink> affectedVls;
    private List<AffectedVnffg> affectedVnffgs;
    private List<AffectedNs> affectedNss;
    private List<AffectedSap> affectedSaps;

    public List<AffectedVnf> getAffectedVnfs() {
        return affectedVnfs;
    }

    public void setAffectedVnfs(List<AffectedVnf> affectedVnfs) {
        this.affectedVnfs = affectedVnfs;
    }

    public List<AffectedPnf> getAffectedPnfs() {
        return affectedPnfs;
    }

    public void setAffectedPnfs(List<AffectedPnf> affectedPnfs) {
        this.affectedPnfs = affectedPnfs;
    }

    public List<AffectedVirtualLink> getAffectedVls() {
        return affectedVls;
    }

    public void setAffectedVls(List<AffectedVirtualLink> affectedVls) {
        this.affectedVls = affectedVls;
    }

    public List<AffectedVnffg> getAffectedVnffgs() {
        return affectedVnffgs;
    }

    public void setAffectedVnffgs(List<AffectedVnffg> affectedVnffgs) {
        this.affectedVnffgs = affectedVnffgs;
    }

    public List<AffectedNs> getAffectedNss() {
        return affectedNss;
    }

    public void setAffectedNss(List<AffectedNs> affectedNss) {
        this.affectedNss = affectedNss;
    }

    public List<AffectedSap> getAffectedSaps() {
        return affectedSaps;
    }

    public void setAffectedSaps(List<AffectedSap> affectedSaps) {
        this.affectedSaps = affectedSaps;
    }
}

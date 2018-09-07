/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.db.request.beans;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.openpojo.business.annotation.BusinessKey;

/**
 * persist the request identifiers created when MSO POSTs a request to PINC 
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version 
 */

@Entity
@Table(name = "request_processing_data")
public class RequestProcessingData implements Serializable{

	/**
     * 
     */
	private static final long serialVersionUID = -3497593687393936143L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Integer id;
	
	@BusinessKey
   	@Column(name = "SO_REQUEST_ID", length=50, unique=true)
	private String soRequestId;
	
   	@BusinessKey
   	@Column(name = "GROUPING_ID", length=100, unique=true)
	private String groupingId;
   	
   	@BusinessKey
   	@Column(name = "NAME", length=200)
	private String name;
   	
   	@Column(name = "VALUE", columnDefinition = "LONGTEXT")
	private String value;
   	
   	@BusinessKey
   	@Column(name = "TAG", length=200)
	private String tag;
   	
	@Column(name = "CREATE_TIME", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created = null;

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof RequestProcessingData)) {
			return false;
		}
		RequestProcessingData castOther = (RequestProcessingData) other;
		return new EqualsBuilder().append(soRequestId, castOther.soRequestId).append(groupingId, castOther.groupingId)
				.append(name, castOther.name).append(tag, castOther.tag).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(soRequestId).append(groupingId).append(name).append(tag).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("soRequestId", soRequestId)
				.append("groupingId", groupingId).append("name", name).append("value", value).append("tag", tag)
				.toString();
	}

	@PrePersist
	protected void createdAt() {
		this.created = new Date();
	}
	
	public Integer getId() {
 		return id;
 	}

 	public void setId(Integer id) {
 		this.id = id;
 	}

 	public String getSoRequestId() {
 		return soRequestId;
 	}

 	public void setSoRequestId(String soRequestId) {
 		this.soRequestId = soRequestId;
 	}

 	public String getGroupingId() {
 		return groupingId;
 	}

 	public void setGroupingId(String groupingId) {
 		this.groupingId = groupingId;
 	}

 	public String getName() {
 		return name;
 	}

 	public void setName(String name) {
 		this.name = name;
 	}

 	public String getValue() {
 		return value;
 	}

 	public void setValue(String value) {
 		this.value = value;
 	}

 	public String getTag() {
 		return tag;
 	}

 	public void setTag(String tag) {
 		this.tag = tag;
 	}
 	
    public Date getCreated() {
		return created;
	}
}
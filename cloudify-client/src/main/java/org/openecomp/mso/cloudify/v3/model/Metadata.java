package org.openecomp.mso.cloudify.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * This class represents a generic Cloudify response to a GET command.
 * These responses have a common format:
 * {
 *     "items": [
 *          List of objects of the requested type
 *     ],
 *     "metadata": {
 *     }
 * }
 * 
 * @author jc1348
 *
 */
public class Metadata implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("pagination")
	private Pagination pagination;
	

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public class Pagination {
		@JsonProperty("total")
		private int total;
		@JsonProperty("offset")
		private int offset;
		@JsonProperty("size")
		private int size;
		
		public int getTotal() {
			return total;
		}
		public void setTotal(int total) {
			this.total = total;
		}
		public int getOffset() {
			return offset;
		}
		public void setOffset(int offset) {
			this.offset = offset;
		}
		public int getSize() {
			return size;
		}
		public void setSize(int size) {
			this.size = size;
		}
	}
}

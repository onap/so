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

package org.openecomp.mso.openstack.exceptions;



public abstract class MsoException extends Exception
{
	private static final long serialVersionUID = 1L;

	protected MsoExceptionCategory category = MsoExceptionCategory.INTERNAL;
	protected String context = null;
	
	protected MsoException (String message) {
		super(message);
	}
	
	protected MsoException (String message, Throwable t) {
		super(message,t);
	}
	
	public MsoExceptionCategory getCategory() {
		return category;
	}
	public void setCategory (MsoExceptionCategory category) {
		this.category = category;
	}
	
	public String getContext () {
		return context;
	}
	public void setContext (String context) {
		this.context = context;
	}
	public void addContext (String ctx) {
		if (this.context != null)
			this.context = ctx + ":" + this.context;
		else
			this.context = ctx;
	}
	
	public String getContextMessage () {
		if (this.context == null)
			return getMessage();
		else
			return "[" + context + "] " + getMessage();
	}
}

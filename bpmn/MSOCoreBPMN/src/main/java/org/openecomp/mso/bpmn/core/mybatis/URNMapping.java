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

package org.openecomp.mso.bpmn.core.mybatis;

/**
 * A bean that represents a single URN mapping.
 */
public class URNMapping {
	private String name;
	private String value;
	private String rev;

	/**
	 * Get the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name.
	 * @param name the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the value mapped to the name.
	 * @return the value mapped to the name
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the value mapped to the name.
	 * @param value the value mapped to the name
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Get the revision attribute (currently unused).
	 * @return the revision attribute
	 */
	public String getRev() {
		return rev;
	}

	/**
	 * Set the revision attribute (currently unused).
	 * @param rev the revision attribute
	 */
	public void setRev(String rev) {
		this.rev = rev;
	}
	
	/**
	 * Converts a URN to "normal" form so it can used as a java or groovy
	 * variable identifier.  This is done in a way that makes the identifier
	 * as readable as possible, but note that it might result in a loss of
	 * uniqueness.
	 * <ol>
	 * <li> URN_ is prepended </li>
	 * <li> All characters that are not letters or digits are converted to
	 *      underscore characters </li>
	 * <li> Sequences of multiple underscores are collapsed to a single
	 *      underscore character </li>
	 * </ol>
	 * Examples:
	 * <p>
	 * aai:endpoint becomes URN_aai_endpoint <br/>
	 * ae:internal-reporting becomes URN_ae_internal_reporting <br/>
	 * 
	 * @param urn the URN
	 * @return a normalized identifier
	 */
	public static String createIdentifierFromURN(String urn) {
		StringBuilder builder = new StringBuilder();
		builder.append("URN_");
		char last = builder.charAt(builder.length() - 1);

		int len = urn.length();

		for (int i = 0; i < len; i++) {
			char c = urn.charAt(i);

			if (!Character.isLetterOrDigit(c) && c != '_') {
				c = '_';
			}

			if (!(c == '_' && last == '_')) {
				builder.append(c);
			}

			last = c;
		}

		return builder.toString();
	}
}
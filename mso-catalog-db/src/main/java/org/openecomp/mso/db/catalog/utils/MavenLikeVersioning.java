/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.db.catalog.utils;



/**
 * This class is the base class for object that requires a Version in Catalog DB.
 * The version is built on a string as ASDC provides a number like 1.2 or 2.0 ...
 * This class supports also 1.2.3.4...  (Maven like version)
 * 
 *
 */
public class MavenLikeVersioning {

	protected String version;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * This method is used to compare the current object version to a specified one
	 * It is assumed that the version is like the maven one, eg: 2.0.1.5.6
	 *
	 * @param versionToCompare The version that will be used for comparison
	 * @return True if the current object is more recent than the specified version, False otherwise
	 *
	 */
	public Boolean isMoreRecentThan (String versionToCompare) {
		if (versionToCompare == null || this.version == null) {
			return Boolean.FALSE;
		}
		String [] currentVersionArray = this.version.split("\\.");
		String [] specifiedVersionArray = versionToCompare.split("\\.");

		int smalestStringLength = 0;

		if (currentVersionArray.length > specifiedVersionArray.length) {
			smalestStringLength = specifiedVersionArray.length;
		} else {
			smalestStringLength = currentVersionArray.length;
		}

		for (int currentVersionIndex=0;currentVersionIndex < smalestStringLength;++currentVersionIndex) {

			if (Integer.valueOf(currentVersionArray[currentVersionIndex]) < Integer.valueOf(specifiedVersionArray[currentVersionIndex])) {
				return Boolean.FALSE;
			} else if (Integer.valueOf(currentVersionArray[currentVersionIndex]) > Integer.valueOf(specifiedVersionArray[currentVersionIndex])) {
				return Boolean.TRUE;
			}
		}

		// Even if versionToCompare has more digits, it means versionToCompare is more recent
		if (Integer.valueOf(currentVersionArray[smalestStringLength-1]).intValue () == Integer.valueOf(specifiedVersionArray[smalestStringLength-1]).intValue ()) {
			if (currentVersionArray.length > specifiedVersionArray.length) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	/**
	 * This method is used to compare the current object version to a specified one
	 * It is assumed that the version is like the maven one, eg: 2.0.1.5.6
	 *
	 * @param versionToCompare The version that will be used for comparison
	 * @return True if the current object is equal to the specified version, False otherwise
	 *
	 */
	public Boolean isTheSameVersion (String versionToCompare) {
		if (versionToCompare == null && this.version == null) {
			return Boolean.TRUE;
		} else if (versionToCompare == null || this.version == null) {
			return Boolean.FALSE;
		}
		String [] currentVersionArray = this.version.split("\\.");
		String [] specifiedVersionArray = versionToCompare.split("\\.");

		if (currentVersionArray.length != specifiedVersionArray.length) {
			return Boolean.FALSE;
		}

		for (int currentVersionIndex=0;currentVersionIndex < currentVersionArray.length;++currentVersionIndex) {

			if (Integer.valueOf(currentVersionArray[currentVersionIndex]).intValue () != Integer.valueOf(specifiedVersionArray[currentVersionIndex]).intValue ()) {
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}
}

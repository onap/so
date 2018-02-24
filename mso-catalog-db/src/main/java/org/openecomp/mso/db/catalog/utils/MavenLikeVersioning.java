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

package org.openecomp.mso.db.catalog.utils;


import java.io.Serializable;

/**
 * This class is the base class for object that requires a Version in Catalog DB.
 * The version is built on a string as ASDC provides a number like 1.2 or 2.0 ...
 * This class supports also 1.2.3.4...  (Maven like version)
 *
 *
 */
public class MavenLikeVersioning implements Serializable {

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
	public boolean isMoreRecentThan (String versionToCompare) {
		if (versionToCompare == null || versionToCompare.trim().equals("") || this.version == null || this.version
			.trim().equals("")) {
			return false;
		}
		String[] currentVersionArray = this.version.split("\\.");
		String[] specifiedVersionArray = versionToCompare.split("\\.");

		int smalestStringLength = Math.min(currentVersionArray.length, specifiedVersionArray.length);

		for (int currentVersionIndex = 0; currentVersionIndex < smalestStringLength; ++currentVersionIndex) {

			if (Integer.parseInt(currentVersionArray[currentVersionIndex]) < Integer
				.parseInt(specifiedVersionArray[currentVersionIndex])) {
				return false;
			} else if (Integer.parseInt(currentVersionArray[currentVersionIndex]) > Integer
				.parseInt(specifiedVersionArray[currentVersionIndex])) {
				return true;
			}
		}

		// Even if versionToCompare has more digits, it means versionToCompare is more recent
		return Integer.parseInt(currentVersionArray[smalestStringLength - 1]) != Integer
			.parseInt(specifiedVersionArray[smalestStringLength - 1])
			|| currentVersionArray.length > specifiedVersionArray.length;

	}

	/**
	 * This method is used to compare the current object version to a specified one
	 * It is assumed that the version is like the maven one, eg: 2.0.1.5.6
	 *
	 * @param versionToCompare The version that will be used for comparison
	 * @return True if the current object is equal to the specified version, False otherwise
	 *
	 */
	public boolean isTheSameVersion (String versionToCompare) {
		if (versionToCompare == null && this.version == null) {
			return true;
		} else if (versionToCompare == null || versionToCompare.trim().equals("") || this.version == null || this.version.trim().equals("")) {
			return false;
		}
		String [] currentVersionArray = this.version.split("\\.");
		String [] specifiedVersionArray = versionToCompare.split("\\.");

		if (currentVersionArray.length != specifiedVersionArray.length) {
			return false;
		}

		for (int currentVersionIndex=0;currentVersionIndex < currentVersionArray.length;++currentVersionIndex) {

			if (Integer.parseInt(currentVersionArray[currentVersionIndex]) != Integer.parseInt(specifiedVersionArray[currentVersionIndex])) {
				return false;
			}
		}

		return true;
	}
}

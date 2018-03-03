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

package org.openecomp.mso.asdc.installer;


public interface IVfModuleData {

	  // Method descriptor #4 ()Ljava/lang/String;
	  public abstract java.lang.String getVfModuleModelName();

	  // Method descriptor #4 ()Ljava/lang/String;
	  public abstract java.lang.String getVfModuleModelInvariantUUID();

	  // Method descriptor #4 ()Ljava/lang/String;
	  public abstract java.lang.String getVfModuleModelCustomizationUUID();

	  // Method descriptor #4 ()Ljava/lang/String;
	  public abstract java.lang.String getVfModuleModelVersion();

	  // Method descriptor #4 ()Ljava/lang/String;
	  public abstract java.lang.String getVfModuleModelUUID();

	  // Method descriptor #4 ()Ljava/lang/String;
	  public abstract java.lang.String getVfModuleModelDescription();

	  // Method descriptor #10 ()Z
	  public abstract boolean isBase();

	  // Method descriptor #12 ()Ljava/util/List;
	  // Signature: ()Ljava/util/List<Ljava/lang/String;>;
	  public abstract java.util.List<String> getArtifacts();

	  public abstract java.util.Map<String,String> getProperties();
}

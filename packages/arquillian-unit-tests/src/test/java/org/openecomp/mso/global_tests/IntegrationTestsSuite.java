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

package org.openecomp.mso.global_tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

//import org.openecomp.mso.global_tests.appc.AppCAdapterITCase;
import org.openecomp.mso.global_tests.asdc.ASDCITCase;
import org.openecomp.mso.global_tests.logging.LogsCheckerITCase;
import org.openecomp.mso.global_tests.soapui.SoapUiITCase;


@RunWith(Suite.class)
@SuiteClasses({

//	MSOUiITCase.class,
//	AppCAdapterITCase.class,
//	SoapUiITCase.class,
//	ASDCITCase.class,
//	LogsCheckerITCase.class

})
public class IntegrationTestsSuite {

}

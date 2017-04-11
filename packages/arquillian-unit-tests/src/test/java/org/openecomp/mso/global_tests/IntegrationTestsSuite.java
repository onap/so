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

//    MSOUiITCase.class,
//	AppCAdapterITCase.class,
	SoapUiITCase.class,
    ASDCITCase.class,
    LogsCheckerITCase.class

})
public class IntegrationTestsSuite {

}

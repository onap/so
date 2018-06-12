package org.openecomp.mso.asdc;

import static org.mockito.Mockito.reset;

import org.junit.After;
import org.junit.runner.RunWith;
import org.openecomp.mso.asdc.installer.ToscaResourceStructure;
import org.openecomp.mso.asdc.installer.VfResourceStructure;
import org.openecomp.mso.asdc.installer.heat.ToscaResourceInstaller;
import org.openecomp.mso.asdc.tenantIsolation.SpringContextHelper;
import org.openecomp.mso.asdc.tenantIsolation.WatchdogDistribution;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = SpringContextHelper.class, initializers = ConfigFileApplicationContextInitializer.class)
public abstract class BaseTest {
	@MockBean
	protected VfResourceStructure vfResourceStructure;
	@MockBean
	protected ToscaResourceStructure toscaResourceStruct;
	@SpyBean
	protected WatchdogDistribution watchdogDistributionSpy;
	@SpyBean
	protected ToscaResourceInstaller toscaInstaller;

	@After
	public void after() {
		reset(vfResourceStructure);
		reset(toscaResourceStruct);
	}
}

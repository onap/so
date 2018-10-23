package org.onap.so.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.onap.so.spring.SpringContextHelper;
import org.onap.so.test.categories.SpringAware;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SpringContextHelper.class, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@Category(SpringAware.class)
public class DefaultsTest {

	@Test
	public void checkValue() {
		
		assertEquals("CloudOwner", Defaults.CLOUD_OWNER.toString());
	}
}

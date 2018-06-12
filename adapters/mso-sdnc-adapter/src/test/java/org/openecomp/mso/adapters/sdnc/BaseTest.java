package org.openecomp.mso.adapters.sdnc;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openecomp.mso.cloud.Application;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8089));

    @After
    public void after() {

    }
}
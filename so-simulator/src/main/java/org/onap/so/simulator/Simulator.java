
package org.onap.so.simulator;

import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.citrusframework.simulator.http.SimulatorRestAdapter;
import org.citrusframework.simulator.http.SimulatorRestConfigurationProperties;


// SecurityAutoConfiguration is intentionally NOT excluded: the bundled citrus-simulator-ui ships a
// SecurityConfiguration whose filterChain(HttpSecurity) bean requires the prototype HttpSecurity bean that
// SecurityAutoConfiguration (via HttpSecurityConfiguration) provides. The UI's filter chain permits all
// requests, so the simulator stays open. ManagementWebSecurityAutoConfiguration stays excluded (it backs
// off on its own once a custom SecurityFilterChain is present) to keep actuator endpoints unsecured.
@SpringBootApplication(exclude = {ManagementWebSecurityAutoConfiguration.class})
public class Simulator extends SimulatorRestAdapter {

    public static void main(String[] args) {
        SpringApplication.run(Simulator.class, args);
    }

    @Override
    public List<String> urlMappings(SimulatorRestConfigurationProperties simulatorRestConfiguration) {
        return List.of("/sim/**");
    }

    @Bean
    public SaajSoapMessageFactory messageFactory() {
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        messageFactory.setSoapVersion(SoapVersion.SOAP_12);
        return messageFactory;
    }
}

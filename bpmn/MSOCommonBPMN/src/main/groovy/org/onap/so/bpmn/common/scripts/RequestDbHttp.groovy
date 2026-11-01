package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.client.ClientObservationRegistry
import org.onap.so.db.request.client.RequestDbOperationsClient
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter
import java.net.http.HttpClient
import java.time.Duration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

class RequestDbHttp {

    static RequestDbOperationsClient createClient(DelegateExecution execution) {
        int connectMillis = Integer.parseInt(UrnPropertiesReader.getVariable(
                'mso.adapters.requestDb.http.connectTimeoutMillis', execution) ?: '10000')
        int readMillis = Integer.parseInt(UrnPropertiesReader.getVariable(
                'mso.adapters.requestDb.http.readTimeoutMillis', execution) ?: '60000')
        if (connectMillis <= 0 || readMillis <= 0) {
            throw new IllegalArgumentException('RequestDB HTTP timeouts must be positive')
        }
        def httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(connectMillis))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()
        def factory = new JdkClientHttpRequestFactory(httpClient)
        factory.setReadTimeout(readMillis)
        def template = new RestTemplate(factory)
        template.setObservationRegistry(ClientObservationRegistry.get())
        template.getInterceptors().add(new SOSpringClientFilter())
        return new RequestDbOperationsClient(template,
                UrnPropertiesReader.getVariable('mso.adapters.requestDb.endpoint', execution),
                UrnPropertiesReader.getSoBasicAuthHeader(execution))
    }
}
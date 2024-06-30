package io.helidon.webserver.tests;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;

import org.junit.jupiter.api.Test;

abstract class MainTestCases {

    abstract Http1Client client();

    @Test
    void testManyConcurrentRequests() throws Exception {
        int numberOfConcurrentRequests = 100;
        int timeToSleepInMs = 100;
        try (var scope = new CollectAndShutdownOnFailureTaskScope<Integer>("search-scope", Thread.ofVirtual().factory())) {
            for (int i = 0; i < numberOfConcurrentRequests; i++) {
                scope.fork(() -> {
                    var response = client().get("/sleep/" + timeToSleepInMs).request(String.class);
                    return response.status().code();
                });
            }

            scope.join().throwIfFailed();

            Map<Integer, Long> result = scope.results()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            assertThat(result.get(200)).as(result.toString()).isGreaterThan(0).isLessThan(numberOfConcurrentRequests);
            assertThat(result.get(429)).as(result.toString()).isGreaterThan(0).isLessThan(numberOfConcurrentRequests);
        }
    }

    @Test
    void testMetricsObserver() {
        try (Http1ClientResponse response = client().get("/observe/metrics").request()) {
            assertThat(response.status()).isEqualTo(Status.OK_200);
        }
    }
}

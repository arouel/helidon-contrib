package io.helidon.webserver.limiter;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.helidon.common.concurrency.limits.limit.FixedLimitConfig;
import io.helidon.common.concurrency.limits.limiter.BasicLimiterConfig;
import io.helidon.common.concurrency.limits.limiter.Limiter;
import io.helidon.common.concurrency.limits.limiter.LimiterConfig;
import io.helidon.http.Method;
import io.helidon.http.Status;
import io.helidon.webclient.api.ClientResponseTyped;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.RoutingRequest;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import org.junit.jupiter.api.Test;

@ServerTest
class LimiterHttpFeatureTest {

    @SetUpRoute
    static void routing(HttpRouting.Builder builder) {
        LimiterConfig limiterConfig = BasicLimiterConfig.builder()
                .limit(FixedLimitConfig.builder().limit(1).build())
                .build();
        Limiter<RoutingRequest> limiter = limiterConfig.toLimiter(RoutingRequest.class);
        builder.addFeature(new LimiterHttpFeature(1, limiter, 429));
        builder.route(Method.GET, "/sleep/{millis}", (req, res) -> {
            long millis = Long.parseLong(req.path().pathParameters().get("millis"));
            Thread.sleep(millis);
            res.send("slept " + millis + " milliseconds");
        });
    }

    @Test
    void test_concurrency(Http1Client client) throws Exception {
        int numberOfConcurrentRequests = 100;
        int timeToSleepInMs = 100;
        try (var scope = new CollectAndShutdownOnFailureTaskScope<Integer>("search-scope", Thread.ofVirtual().factory())) {
            for (int i = 0; i < numberOfConcurrentRequests; i++) {
                scope.fork(() -> {
                    var response = client.get("/sleep/" + timeToSleepInMs).request(String.class);
                    return response.status().code();
                });
            }

            scope.join().throwIfFailed();

            Map<Integer, Long> result = scope.results()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            assertThat(result.get(200)).as(result.toString()).isGreaterThanOrEqualTo(1).isLessThan(numberOfConcurrentRequests);
            assertThat(result.get(429)).as(result.toString()).isGreaterThan(90).isLessThan(numberOfConcurrentRequests);
        }
    }

    @Test
    void test_one(Http1Client client) {
        ClientResponseTyped<String> response = client.get("/sleep/0").request(String.class);
        assertThat(response.status()).isEqualTo(Status.OK_200);
        assertThat(response.entity()).isEqualTo("slept 0 milliseconds");
    }
}

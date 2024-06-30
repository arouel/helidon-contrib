package io.helidon.webserver.tests;

import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.SetUpRoute;

abstract class AbstractMainTest extends MainTestCases {

    private final Http1Client client;

    AbstractMainTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void routing(HttpRouting.Builder builder) {
        Main.routing(builder);
    }

    @Override
    Http1Client client() {
        return client;
    }
}

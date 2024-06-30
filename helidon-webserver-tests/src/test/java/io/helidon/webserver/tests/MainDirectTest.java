package io.helidon.webserver.tests;

import io.helidon.webserver.testing.junit5.DirectClient;
import io.helidon.webserver.testing.junit5.RoutingTest;

@RoutingTest
class MainDirectTest extends AbstractMainTest {
    MainDirectTest(DirectClient client) {
        super(client);
    }
}
package io.helidon.webserver.tests;

import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

@ServerTest
class MainHttpTest extends AbstractMainTest {
    MainHttpTest(Http1Client client) {
        super(client);
    }
}
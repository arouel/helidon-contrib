package io.helidon.webserver.tests;

import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientConfig;
import io.helidon.webserver.WebServer;

import org.junit.jupiter.api.BeforeAll;

class MainServerTest extends MainTestCases {

    static int port;

    @BeforeAll
    static void beforeAll() {
        WebServer server = Main.startServer();
        port = server.port();
    }

    @Override
    Http1Client client() {
        return Http1Client.create(Http1ClientConfig.builder()
                .baseUri("http://localhost:" + port)
                .buildPrototype());
    }
}

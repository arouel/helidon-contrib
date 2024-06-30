package io.helidon.webserver.tests;

import java.lang.System.Logger.Level;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class Main {

    private static final System.Logger logger = System.getLogger(Main.class.getName());

    public static void main(String[] args) {
        WebServer server = startServer();
        logger.log(Level.INFO, "WEB server is up! http://localhost:{0,number,#####}/sleep/0", server.port());
    }

    static void routing(HttpRouting.Builder routing) {
        routing.get("/sleep/{millis}", Main::sleepAndRespond);
    }

    static void sleepAndRespond(ServerRequest req, ServerResponse res) throws InterruptedException {
        long millis = Long.parseLong(req.path().pathParameters().get("millis"));
        Thread.sleep(millis);
        res.send("slept " + millis + " milliseconds");
    }

    static WebServer startServer() {
        // load logging configuration
        LogConfig.configureRuntime();

        // initialize global config from default configuration
        Config config = Config.create();
        Config.global(config);

        return WebServer.builder()
                .config(config.get("server"))
                .routing(Main::routing)
                .build()
                .start();
    }
}

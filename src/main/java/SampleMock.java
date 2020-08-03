import com.sun.net.httpserver.HttpServer;
import config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

// Class for implementing mock logic
public class SampleMock extends AbstractMock {
    private static final Logger log = LoggerFactory.getLogger(SampleMock.class);
    private static Config config;

    // Mock name for metrics label
    public static String mockName = "Sample Mock";

    public SampleMock(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        log.info("Starting {}", mockName);

        // Initial setup for default features
        new SampleMock(args);

        config = ConfigFactory.getConfig();

        DB.create(config.getPool());

        // Start handler for periodic activity as a thread
        Runnable r = new SampleHandler();
        new Thread(r).start();

        // Start HTTP server for mock logic
        log.info("Starting SearchHandler on port {} path {}", config.getSearchConfig().getPort(), config.getSearchConfig().getPath());
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(config.getSearchConfig().getPort()), 0);
        } catch (IOException e) {
            log.error("Failed to start http server on port {}",  config.getSearchConfig().getPort());
            e.printStackTrace();
            System.exit(-1);
        }
        // Add handler
        server.createContext(config.getSearchConfig().getPath(), new SearchHandler());
        server.setExecutor(null);
        server.start();

        // Keep running until shutdown triggered
        while (keepRunning) {}

        // Cleanup activities
        server.stop(5);
        DB.close();
    }
}

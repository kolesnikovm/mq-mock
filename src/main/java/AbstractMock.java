import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.HTTPServer;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// Class that contains general mock methods and elements
public abstract class AbstractMock implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(AbstractMock.class);

    // Cmd options
    private static final String CONFIG_FILE = "configFile";
    private static final String METRICS_PORT = "metricsPort";

    // Default values for cmd options
    private static String configFile = "config.yml";
    private static int metricsPort = 9092;

    public static volatile boolean keepRunning = true;
    public static String ip;

    // Metrics server
    private HTTPServer server;

    private static double startTime = System.currentTimeMillis();

    // Prometheus collectors
    static final Gauge uptime = Gauge.build()
            .name("mock_uptime")
            .help("Mock uptime.")
            .labelNames("mock", "host")
            .register();
    public static final Summary handlingTime = Summary.build()
            .name("handling_time")
            .help("Handler processing time.")
            .labelNames("mock", "host", "handler")
            .quantile(0.9, 0.01)
            .maxAgeSeconds(10)
            .register();
    public static final Summary producerTime = Summary.build()
            .name("producer_time")
            .help("Handler processing time.")
            .labelNames("mock", "host", "handler")
            .quantile(0.9, 0.01)
            .maxAgeSeconds(10)
            .register();

    public AbstractMock(String[] args) {
        parseOptions(args);

        ConfigFactory.createConfig(configFile);

        // Get IP for metrics label
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Failed to resolve hostname into address");
            e.printStackTrace();
        }

        // Start exposing Prometheus metrics
        serveMetrics(metricsPort);

        // Shutdown hook
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.debug("Mock is shutting down...");
            keepRunning = false;
            server.stop();
            try {
                mainThread.join();
                log.info("Shutdown complete");
            } catch (InterruptedException e) {
                log.error("Failed to shutdown gracefully");
                e.printStackTrace();
                System.exit(-1);
            }
        }));
    }

    @Override
    public void run() {
        double now = System.currentTimeMillis();
        uptime.labels(SampleMock.mockName, ip).set(now - startTime);
    }

    static void parseOptions(String[] args) {
        Options options = new Options();

        Option configFileOption = new Option(CONFIG_FILE, true, "config file, default: " + configFile);
        options.addOption(configFileOption);

        Option portOption = new Option(METRICS_PORT, true, "metrics port, default: " + metricsPort);
        options.addOption(portOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption(CONFIG_FILE)) {
                configFile = cmd.getOptionValue(CONFIG_FILE);
            }
            if (cmd.hasOption(METRICS_PORT)) {
                metricsPort = Integer.parseInt(cmd.getOptionValue(METRICS_PORT));
            }
        } catch (ParseException e) {
            log.error(e.getMessage());
            formatter.printHelp("jar [options]", options);
            System.exit(-1);
        }
    }

    private void serveMetrics(int port) {
        try {
            server = new HTTPServer(port);
        } catch (IOException e) {
            log.error("Failed to start metrics server");
            e.printStackTrace();
            System.exit(-1);
        }
        log.info("Serving metrics on port {}", port);

        // Update mock uptime
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }
}

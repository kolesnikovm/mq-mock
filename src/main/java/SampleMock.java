import com.sun.net.httpserver.HttpServer;
import config.Config;
import config.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

// Class for implementing mock logic
public class SampleMock extends AbstractMock {
    private static final Logger log = LoggerFactory.getLogger(SampleMock.class);

    private static Config config;
    private static HashMap<String, Class> classesMap = new HashMap<>();

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

        registerHandlers();

        MQ mq = new MQ(config.getMqConfig().getHost(),
                config.getMqConfig().getPort(),
                config.getMqConfig().getChannel(),
                config.getMqConfig().getQueueManager());
        mq.connect(config.getMqConfig().getLogin(), config.getMqConfig().getPassword());

        // Start handlers
        for (Service service : config.getServices()) {
            log.info("Starting handler for {}", service.getName());

            Class c = classesMap.get(service.getName());
            if (c == null) {
                log.warn("No handler for {}", service.getName());
                continue;
            }
            Runnable r = null;

            Class[] cArg = new Class[3];
            cArg[0] = Session.class;
            cArg[1] = MessageConsumer.class;
            cArg[2] = BlockingQueue.class;

            BlockingQueue<DelayedMessage> delayQueue = new DelayQueue<>();

            for (int i = 0; i < service.getConsumerThreads(); i++) {
                Session session = mq.createSession();
                MessageConsumer consumer = mq.createConsumer(session, service.getRequestQueue());

                try {
                    r = (Runnable) c.getDeclaredConstructor(cArg).newInstance(session, consumer, delayQueue);
                } catch (Exception e) {
                    log.error("Failed to start consumer {}", service.getName());
                    e.printStackTrace();
                }
                new Thread(r).start();
                // todo add thread counter
            }


            for (int i = 0; i < service.getProducerThreads(); i++) {
                Session session = mq.createSession();
                MessageProducer producer = mq.createProducer(session, service.getResponseQueue());

                Runnable runnableProducer = new Producer(delayQueue, producer);
                new Thread(runnableProducer).start();
            }
        }

        // Keep running until shutdown triggered
        while (keepRunning) {}
    }

    private static void registerHandlers() {
        classesMap.put("create", CreateHandler.class);
        classesMap.put("search", CreateHandler.class);
    }
}

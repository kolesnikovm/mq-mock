package com.github.kolesnikovm;

import com.github.kolesnikovm.config.Config;
import com.github.kolesnikovm.config.Service;
import com.github.kolesnikovm.handlers.Create;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

// Class for implementing mock logic
public class Mock extends AbstractMock {
    private static final Logger log = LoggerFactory.getLogger(Mock.class);

    private static Config config;
    private static HashMap<String, Class> classesMap = new HashMap<>();

    // Mock name for metrics label
    public static String mockName = "Sample Mock";

    public Mock(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        log.info("Starting {}", mockName);

        // Initial setup for default features
        new Mock(args);

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

            Handler handler = null;
            try {
                handler = (Handler) c.newInstance();
            } catch (Exception e) {
                log.error("Failed to instantiate handler");
                e.printStackTrace();
            }

            BlockingQueue<DelayedMessage> delayQueue = new DelayQueue<>();

            for (int i = 0; i < service.getConsumerThreads(); i++) {
                Session session = mq.createSession();
                MessageConsumer consumer = mq.createConsumer(session, service.getRequestQueue());

                Runnable runnableConsumer = new Consumer(session, consumer, delayQueue, handler, service.getResponseDelay());
                new Thread(runnableConsumer).start();
                threads.labels(Mock.mockName, AbstractMock.ip, "consumer").inc();
            }

            for (int i = 0; i < service.getProducerThreads(); i++) {
                Session session = mq.createSession();
                MessageProducer producer = mq.createProducer(session, service.getResponseQueue());

                Runnable runnableProducer = new Producer(delayQueue, producer);
                new Thread(runnableProducer).start();
                threads.labels(Mock.mockName, AbstractMock.ip, "producer").inc();
            }
        }

        // Keep running until shutdown triggered
        while (keepRunning) {}
    }

    private static void registerHandlers() {
        classesMap.put("create", Create.class);
        classesMap.put("search", Create.class);
    }
}

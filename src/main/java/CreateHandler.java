import config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Random;
import java.util.concurrent.BlockingQueue;


public class CreateHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(CreateHandler.class);

    private Session session;
    private MessageConsumer consumer;
    private Message message;
    private BlockingQueue<DelayedMessage> delayQueue;
    private Random r = new Random();

    public CreateHandler(Session session, MessageConsumer consumer, BlockingQueue delayQueue) {
        log.debug("Creating new CreateHandler");

        this.session = session;
        this.consumer = consumer;
        this.delayQueue = delayQueue;
    }

    @Override
    public void run() {
        log.debug("Running CreateHandler");

        while (true) {
            long start = System.currentTimeMillis();

            try {
                message = consumer.receiveNoWait();
            } catch (JMSException e) {
                log.error("Failed to receive message");
                e.printStackTrace();
                System.exit(-1);
            }

            if (message != null) {
//                long start = System.currentTimeMillis();

                Message response = null;
                try {
                    response = session.createTextMessage("======");
                    response.setJMSCorrelationID(message.getJMSCorrelationID());
                    response.setStringProperty("src_systemID", "RKK2");
                    response.setStringProperty("version", "2");
                } catch (JMSException e) {
                    log.error("Failed to create response message");
                    e.printStackTrace();
                    System.exit(-1);
                }

                DelayedMessage delayedMessage = new DelayedMessage(response, getDelay());
                try {
                    delayQueue.put(delayedMessage);
                    log.debug("Response sent to delayed queue");
                } catch (InterruptedException e) {
                    log.error("Failed to send response to delayed queue");
                    e.printStackTrace();
                    System.exit(-1);
                }

                long stop = System.currentTimeMillis();
                SampleMock.handlingTime.labels(SampleMock.mockName, AbstractMock.ip, this.getClass().getName()).observe(stop - start);
            }
        }
    }

    private long getDelay() {
        // todo add delay from config
        long d = Math.round(r.nextGaussian()*1000 + 5000);
        if (d < 0) {
            d *= -1;
        }

        return d;
    }
}

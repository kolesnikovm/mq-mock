import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import javax.jms.MessageProducer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Producer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Producer.class);

    private BlockingQueue<DelayedMessage> queue;
    private MessageProducer producer;

    public Producer(BlockingQueue queue, MessageProducer producer) {
        log.debug("Creating new response producer");

        this.queue = queue;
        this.producer = producer;
    }

    @Override
    public void run() {
        log.debug("Starting response producer");

        DelayedMessage delayedMessage;
        Message message;

        while (true) {
            try {
                // todo разбить на две части
                // todo add timer
                delayedMessage = queue.take();
                message = delayedMessage.getMessage();

                long start = System.currentTimeMillis();
                producer.send(message);
                long stop = System.currentTimeMillis();
                SampleMock.producerTime.labels(SampleMock.mockName, AbstractMock.ip, this.getClass().getName()).observe(stop - start);


                log.debug("Response sent to MQ {}", producer.getDestination().toString());
            } catch (Exception e) {
                // add fail timer
                log.error("Failed to send response to MQ");
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}

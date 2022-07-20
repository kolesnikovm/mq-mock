package com.github.kolesnikovm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.Random;
import java.util.concurrent.BlockingQueue;


public class Consumer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    private Session session;
    private MessageConsumer consumer;
    private BlockingQueue<DelayedMessage> delayQueue;
    private Handler handler;

    private Message message, response;
    private Random r = new Random();
    private long consumeStart, consumeStop;
    private long delay;


    public Consumer(Session session, MessageConsumer consumer, BlockingQueue delayQueue, Handler handler, long delay) {
        log.debug("Creating new consumer");

        this.session = session;
        this.consumer = consumer;
        this.delayQueue = delayQueue;
        this.handler = handler;
        this.delay = delay;
    }

    @Override
    public void run() {
        log.debug("Running consumer");

        while (true) {
            long start = System.currentTimeMillis();

            try {
                consumeStart = System.currentTimeMillis();
                message = consumer.receiveNoWait();
                consumeStop = System.currentTimeMillis();

                Mock.consumeTime.labels(Mock.mockName, AbstractMock.ip, this.getClass().getName(), "PASS").observe(consumeStop - consumeStart);
            } catch (JMSException e) {
                consumeStop = System.currentTimeMillis();
                Mock.consumeTime.labels(Mock.mockName, AbstractMock.ip, this.getClass().getName(), "FAIL").observe(consumeStop - consumeStart);

                log.error("Failed to receive message");
                e.printStackTrace();
            }

            if (message != null) {
                response = handler.createResponse(message, session);

                DelayedMessage delayedMessage = new DelayedMessage(response, getDelay());
                try {
                    delayQueue.put(delayedMessage);
                    log.debug("Response sent to delayed queue");
                } catch (InterruptedException e) {
                    log.error("Failed to send response to delayed queue");
                    e.printStackTrace();
                }

                long stop = System.currentTimeMillis();
                Mock.handlingTime.labels(Mock.mockName, AbstractMock.ip, this.getClass().getName()).observe(stop - start);
            }
        }
    }

    private long getDelay() {
        long d = Math.round(r.nextGaussian()*1000 + delay);
        if (d < 0) {
            d *= -1;
        }

        return d;
    }
}

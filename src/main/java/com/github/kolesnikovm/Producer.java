package com.github.kolesnikovm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import java.util.concurrent.BlockingQueue;


public class Producer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Producer.class);

    private BlockingQueue<DelayedMessage> delayQueue;
    private MessageProducer producer;

    private long start, stop;


    public Producer(BlockingQueue delayQueue, MessageProducer producer) {
        log.debug("Creating new response producer");

        this.delayQueue = delayQueue;
        this.producer = producer;
    }

    @Override
    public void run() {
        log.debug("Running response producer");

        DelayedMessage delayedMessage = null;
        Message message;

        while (true) {

            try {
                delayedMessage = delayQueue.take();
            } catch (InterruptedException e) {
                log.error("Failed to take message from delay queue");
                e.printStackTrace();
            }

            message = delayedMessage.getMessage();
            try {
                start = System.currentTimeMillis();
                producer.send(message);
                stop = System.currentTimeMillis();
                Mock.produceTime.labels(Mock.mockName, AbstractMock.ip, this.getClass().getName(), "PASS").observe(stop - start);

                log.debug("Response sent to com.github.kolesnikovm.config.MQ {}", producer.getDestination().toString());
            } catch (JMSException e) {
                long stop = System.currentTimeMillis();
                Mock.produceTime.labels(Mock.mockName, AbstractMock.ip, this.getClass().getName(), "FAIL").observe(stop - start);

                log.error("Failed to send response to com.github.kolesnikovm.config.MQ");
                e.printStackTrace();
            }
        }
    }
}

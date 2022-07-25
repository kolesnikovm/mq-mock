package com.github.kolesnikovm.handlers;

import com.github.kolesnikovm.Handler;
import com.github.kolesnikovm.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

@Method(name="create")
public class Create implements Handler {
    private static final Logger log = LoggerFactory.getLogger(Create.class);

    @Override
    public Message createResponse(Message request, Session session) {
        Message response = null;
        try {
            response = session.createTextMessage("======");
            response.setJMSCorrelationID(request.getJMSCorrelationID());
            response.setStringProperty("src_systemID", "RKK2");
            response.setStringProperty("version", "2");
        } catch (JMSException e) {
            log.error("Failed to create response message");
            e.printStackTrace();
        }

        return response;
    }
}

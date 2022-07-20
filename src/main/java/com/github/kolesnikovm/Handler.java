package com.github.kolesnikovm;

import javax.jms.Message;
import javax.jms.Session;

public interface Handler {

    public Message createResponse(Message request, Session session);
}

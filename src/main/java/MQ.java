import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.jms.JmsQueue;
import com.ibm.msg.client.wmq.WMQConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;


public class MQ {
    private static final Logger log = LoggerFactory.getLogger(MQ.class);

    private JmsFactoryFactory ff;
    private JmsConnectionFactory cf;
    private Connection connection;

    public MQ(String host, int port, String channel, String queueManager) {
        try {
            ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);

            cf = ff.createConnectionFactory();
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
            cf.setIntProperty(WMQConstants.WMQ_PORT, port);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManager);
        } catch (JMSException e) {
            log.error("Cant create MQ");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void connect(String login, String password) {
        try {
            connection = cf.createConnection(login, password);
            connection.start();
        } catch (JMSException e) {
            log.error("Cant connect to MQ");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (JMSException e) {
            log.error("Cant close MQ connection");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Session createSession() {
        try {
            return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            log.error("Cant create MQ session");
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }

    public MessageConsumer createConsumer(Session session, String queue) {
        try {
            JmsQueue q = ff.createQueue(queue);
            return session.createConsumer(q);
        } catch (JMSException e) {
            log.error("Cant create MQ consumer");
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }

    public MessageProducer createProducer(Session session, String queue) {
        try {
            JmsQueue q = ff.createQueue(queue);
            return session.createProducer(q);
        } catch (JMSException e) {
            log.error("Cant create MQ producer");
            e.printStackTrace();
            System.exit(-1);
        }

        return null;
    }

}

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import messages.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

// Handler for checking user count in db
public class SearchHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(SearchHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long start = System.currentTimeMillis();
        log.info("Received new request");

        // Parse query parameters to get user name
        Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());
        log.debug("Query parameters: {}", queryParams);
        log.info("Search for user: {}", queryParams.get("surname"));

        // Check user in db by name
        int userCount = DB.getUserCount(queryParams.get("surname"));
        // Create response message
        Response response = new Response(userCount);

        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/xml");
        exchange.sendResponseHeaders(200, 0);

        // Marshall response and send it to user
        try (OutputStream os = exchange.getResponseBody()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.marshal(response, os);
        } catch (JAXBException e) {
            log.error("Failed to create response");
            e.printStackTrace();
        }

        long stop = System.currentTimeMillis();
        SampleMock.handlingTime.labels(SampleMock.mockName, AbstractMock.ip, this.getClass().getName()).observe(stop - start);
    }

    public Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) {
            return result;
        }
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else{
                result.put(entry[0], "");
            }
        }
        return result;
    }
}

package messages;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.text.SimpleDateFormat;
import java.util.Date;

@XmlRootElement
@XmlType(propOrder = { "message", "date" })
public class Response {

    private String message;
    private String date;

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public Response(int count) {
        StringBuilder sb = new StringBuilder("Users found: ");
        sb.append(count);
        this.message = sb.toString();
    }

    public Response() {}

    @XmlElement(name = "message")
    public String getMessage() {
        return message;
    }

    @XmlElement(name = "date")
    public String getDate() {
        String date = sdf.format(new Date());
        return date;
    }
}

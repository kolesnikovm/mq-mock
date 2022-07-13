import com.google.common.primitives.Ints;

import javax.jms.Message;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedMessage implements Delayed {

    private Message message;
    private long startTime;

    public DelayedMessage(Message message, long delayInMilliseconds) {
        this.message = message;
        this.startTime = System.currentTimeMillis() + delayInMilliseconds;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Ints.saturatedCast(
                this.startTime - ((DelayedMessage) o).startTime);
    }

    public Message getMessage() {
        return message;
    }
}

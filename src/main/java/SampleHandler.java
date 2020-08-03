import config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Sample handler for multithread usage
public class SampleHandler implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SampleHandler.class);
    private static Config config = ConfigFactory.getConfig();

    @Override
    public void run() {
        while (AbstractMock.keepRunning) {
            log.debug("Do something...");
            long start = System.currentTimeMillis();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long stop = System.currentTimeMillis();
            SampleMock.handlingTime.labels(SampleMock.mockName, AbstractMock.ip, this.getClass().getName()).observe(stop - start);
        }
    }
}

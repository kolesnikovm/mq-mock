import config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

// Class for work with configuration parameters defined via file
public class ConfigFactory {
    private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);

    private static Yaml yaml = new Yaml();
    private static Config config;

    // Create config object from file
    public static void createConfig(String pathToConfig) {
        log.info("Using config file: {}", pathToConfig);

        try (InputStream in = Files.newInputStream(Paths.get(pathToConfig))) {
            config = yaml.loadAs(in, Config.class);
        } catch (Exception e) {
            log.error("Failed to parse config file: {}", pathToConfig);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    // Retrieve config object
    public static Config getConfig() {
        return config;
    }
}

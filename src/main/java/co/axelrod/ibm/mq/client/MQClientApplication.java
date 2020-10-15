package co.axelrod.ibm.mq.client;

import co.axelrod.ibm.mq.client.configuration.MQConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MQConfiguration.class)
public class MQClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(MQClientApplication.class, args);
    }
}

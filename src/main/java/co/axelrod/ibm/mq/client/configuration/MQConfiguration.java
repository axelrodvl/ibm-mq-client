package co.axelrod.ibm.mq.client.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "mq")
@Data
@Validated
public class MQConfiguration {
    @NotNull
    private String host;
    @NotNull
    private Integer port;
    @NotNull
    private String queueManager;
    @NotNull
    private String channel;
    @NotNull
    private String user;
    @NotNull
    private String password;

    private String sslCipherSuite;
}

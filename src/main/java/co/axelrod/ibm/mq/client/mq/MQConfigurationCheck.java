package co.axelrod.ibm.mq.client.mq;

import co.axelrod.ibm.mq.client.configuration.MQConfiguration;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;

@Component
@AllArgsConstructor
@Slf4j
public class MQConfigurationCheck {
    private final MQConfiguration mqConfiguration;

    @PostConstruct
    public void checkConfiguration() {
        log.info("Welcome to IBM MQ Client");
        log.info("This utility will help you ensure, that your IBM MQ connection configuration is correct.");
        log.info("NOTICE! Please, provide the whole log to support.");

        printNetworkConfiguration();
        printJavaVersion();
        printIbmMqVersion();
        printIbmMqConnectionConfiguration();
        printSslConfiguration();

        log.info("");
        log.info("Type \"help\" for list of available commands");
    }

    @SneakyThrows
    private void printNetworkConfiguration() {
        log.info("");
        log.info("Network configuration");
        log.info("----------------------------------------");
        log.info("Hostname: " + InetAddress.getLocalHost().getHostName());
        log.info("Address: " + InetAddress.getLocalHost().getHostAddress());
        log.info("----------------------------------------");
    }

    private void printJavaVersion() {
        log.info("");
        log.info("Java version");
        log.info("----------------------------------------");
        log.info(System.getProperty("java.vm.name"));
        log.info(System.getProperty("java.home"));
        log.info(System.getProperty("java.vendor"));
        log.info(System.getProperty("java.version"));
        log.info("----------------------------------------");
    }

    private void printIbmMqVersion() {
        log.info("");
        log.info("IBM MQ client version");
        log.info("----------------------------------------");
        log.info("com.ibm.mq.allclient.jar");
        log.info(com.ibm.mq.MQJavaComponent.class.getPackage().getImplementationTitle());
        log.info(com.ibm.mq.MQJavaComponent.class.getPackage().getImplementationVersion());
        log.info(com.ibm.mq.MQJavaComponent.class.getPackage().getImplementationVendor());
        log.info("----------------------------------------");
    }

    private void printIbmMqConnectionConfiguration() {
        log.info("");
        log.info("IBM MQ connection configuration");
        log.info("----------------------------------------");
        log.info(mqConfiguration.toString());
        log.info("----------------------------------------");
        log.info("NOTICE! If you are using SSL, you have to set proper ssl-cipher-suite, f.e. TLS_RSA_WITH_AES_256_CBC_SHA256.");
        log.info("NOTICE! Visit \"TLS CipherSpecs and CipherSuites in IBM MQ classes for JMS\" (https://www.ibm.com/support/knowledgecenter/SSFKSJ_9.2.0/com.ibm.mq.dev.doc/q113220_.htm) for further info.");
        log.info("----------------------------------------");
        log.info("NOTICE! Your user may not have full access to IBM MQ objects (f.e. specific queue or their attributes).");
        log.info("NOTICE! You cannot read from or write to the queue if the user is not authorized to do so.");
        log.info("----------------------------------------");
    }

    private void printSslConfiguration() {
        log.info("");
        log.info("SSL configuration");
        log.info("----------------------------------------");
        log.info("TrustStore path (javax.net.ssl.trustStore): " + System.getProperty("javax.net.ssl.trustStore"));
        log.info("TrustStore password (javax.net.ssl.trustStorePassword): " + System.getProperty("javax.net.ssl.trustStorePassword"));
        log.info("Use IBM Cipher Mappings (com.ibm.mq.cfg.useIBMCipherMappings): " + System.getProperty("com.ibm.mq.cfg.useIBMCipherMappings"));
        log.info("----------------------------------------");
        log.info("NOTICE! If you are using SSL, you have to pass -Djavax.net.ssl.trustStore=/your/path/keyFileName.jks as Java system property.");
        log.info("NOTICE! If you are using SSL, you have to pass -javax.net.ssl.trustStorePassword=<keyFileNamePassword> as Java system property.");
        log.info("NOTICE! If you are using Oracle/openJDK JVM instead of IBM JVM, you have to pass -Dcom.ibm.mq.cfg.useIBMCipherMappings=false as Java system property.");
        log.info("----------------------------------------");
    }
}

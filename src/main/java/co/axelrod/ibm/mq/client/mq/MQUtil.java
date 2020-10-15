package co.axelrod.ibm.mq.client.mq;

import co.axelrod.ibm.mq.client.configuration.MQConfiguration;
import com.ibm.mq.MQEnvironment;

public class MQUtil {
    private MQUtil() {
        // Utility class
    }

    public static void initializeMQEnvironment(MQConfiguration mqConfiguration) {
        MQEnvironment.hostname = mqConfiguration.getHost();
        MQEnvironment.port = mqConfiguration.getPort();
        MQEnvironment.channel = mqConfiguration.getChannel();
        MQEnvironment.userID = mqConfiguration.getUser();
        MQEnvironment.password = mqConfiguration.getPassword();

        if (mqConfiguration.getSslCipherSuite() != null) {
            MQEnvironment.sslCipherSuite = mqConfiguration.getSslCipherSuite();
        }
    }
}

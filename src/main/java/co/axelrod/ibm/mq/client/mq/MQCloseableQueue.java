package co.axelrod.ibm.mq.client.mq;

import co.axelrod.ibm.mq.client.configuration.MQConfiguration;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MQCloseableQueue implements AutoCloseable {
    private final MQQueueManager queueManager;

    @Getter
    private final MQQueue queue;

    public MQCloseableQueue(MQConfiguration mqConfiguration, String queueName, int openOptions) throws MQException {
        log.info("Connecting to queue manager: " + mqConfiguration.getQueueManager());
        queueManager = new MQQueueManager(mqConfiguration.getQueueManager());

        log.info("Accessing queue: " + queueName);
        queue = queueManager.accessQueue(queueName, openOptions);
    }

    @Override
    public void close() throws MQException {
        log.info("Closing the queue");
        queue.close();

        log.info("Disconnecting from the Queue Manager");
        queueManager.disconnect();
    }
}

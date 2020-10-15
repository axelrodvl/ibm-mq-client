package co.axelrod.ibm.mq.client.mq;

import co.axelrod.ibm.mq.client.configuration.MQConfiguration;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Scope(value = "prototype")
public class MQPCFClient implements AutoCloseable {
    private final MQConfiguration mqConfiguration;
    private final PCFMessageAgent agent;

    public MQPCFClient(MQConfiguration mqConfiguration) throws MQDataException, MQException {
        this.mqConfiguration = mqConfiguration;

        log.info("Creating MQ client with configuration: " + mqConfiguration.toString());

        try {
            MQUtil.initializeMQEnvironment(mqConfiguration);
            MQQueueManager mqQueueManager = new MQQueueManager(mqConfiguration.getQueueManager());
            agent = new PCFMessageAgent(mqQueueManager);
        } catch (MQDataException mqde) {
            if (mqde.reasonCode == CMQC.MQRC_Q_MGR_NAME_ERROR) {
                log.info("Either could not find the " +
                        "default queue manager at \"" + mqConfiguration.getHost() + "\", " +
                        "port \"" + mqConfiguration.getPort() + "\" or could not find the default channel \""
                        + mqConfiguration.getChannel()
                        + "\" on the queue manager.");
            }
            log.info("Unable to create MQ client", mqde);
            throw mqde;
        } catch (MQException e) {
            log.info("Unable to create MQ client" + e.getMessage());
            throw e;
        }
        log.info("MQ client has been successfully created");
    }

    @Override
    public void close() {
        if (agent != null) {
            try {
                agent.disconnect();
                log.info("MQ client with configuration" + mqConfiguration.toString() + " has been successfully closed");
            } catch (MQDataException e) {
                log.error("MQ client with configuration" + mqConfiguration.toString() + " has been closed with error" + e);
            }
        }
    }

    public void createQueue(String queueName, String queueDescription) throws Exception {
        try {
            if (isQueueExists(queueName)) {
                throw new Exception(queueName + " is already exists at queue manager " + mqConfiguration.getQueueManager());
            }

            int queueType = MQConstants.MQQT_LOCAL;
            PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_CREATE_Q);
            pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, queueName);
            pcfCmd.addParameter(MQConstants.MQIA_Q_TYPE, queueType);
            pcfCmd.addParameter(MQConstants.MQCA_Q_DESC, queueDescription);

            try {
                agent.send(pcfCmd);
            } catch (PCFException pcfe) {
                if (pcfe.reasonCode == MQConstants.MQRCCF_OBJECT_ALREADY_EXISTS) {
                    log.error("The queue \"" + queueName + "\" already exists on the queue manager.");
                } else {
                    throw pcfe;
                }
            }

            log.info("Queue " + queueName + " with description \"" + queueDescription + "\" has been successfully created");
        } catch (MQDataException | IOException e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    public void deleteQueue(String queueName) throws Exception {
        try {
            PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_DELETE_Q);
            pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, queueName);
            agent.send(pcfCmd);
            log.info("Queue " + queueName + " has been successfully deleted");
        } catch (MQDataException | IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public boolean isQueueExists(String queueName) throws IOException, MQDataException {
        return listQueueNames(queueName).contains(queueName);
    }

    public void listQueueNames() throws IOException, MQDataException {
        listQueueNames("*");
    }

    private List<String> listQueueNames(String queueName) throws IOException, MQDataException {
        PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_NAMES);

        pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, queueName);
        pcfCmd.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALL);

        PCFMessage[] pcfResponse = agent.send(pcfCmd);
        // For each returned message, extract the message from the array and display the
        // required information.
        log.info("+-----+------------------------------------------------+");
        log.info("|Index|                    Queue Name                  |");
        log.info("+-----+------------------------------------------------+");

        String[] names = (String[]) pcfResponse[0].getParameterValue(MQConstants.MQCACF_Q_NAMES);

        for (int index = 0; index < names.length; index++) {
            log.info("|" + "|"
                    + names[index].substring(0, 48) + "|");
        }
        log.info("+-----+------------------------------------------------+");

        return Arrays.asList(names);
    }
}

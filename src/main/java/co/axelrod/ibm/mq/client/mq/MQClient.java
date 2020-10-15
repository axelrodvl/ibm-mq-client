package co.axelrod.ibm.mq.client.mq;

import co.axelrod.ibm.mq.client.configuration.MQConfiguration;
import co.axelrod.ibm.mq.client.util.HeaderUtil;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.MQHeaderList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class MQClient {
    private final MQConfiguration mqConfiguration;

    public MQClient(MQConfiguration mqConfiguration) {
        this.mqConfiguration = mqConfiguration;
        MQUtil.initializeMQEnvironment(mqConfiguration);
    }

    public void readFromQueue(String queueName) {
        try (MQCloseableQueue closeableQueue = new MQCloseableQueue(mqConfiguration, queueName, MQConstants.MQOO_INPUT_AS_Q_DEF)) {
            MQMessage rcvMessage = new MQMessage();
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            closeableQueue.getQueue().get(rcvMessage, gmo);

            log.info("");
            log.info("Received message info");
            log.info("----------------------------------------");
            log.info("Message ID: " + HeaderUtil.bytesToHex(rcvMessage.messageId));
            log.info("Correlation ID: " + HeaderUtil.bytesToHex(rcvMessage.correlationId));
            log.info("Format: " + rcvMessage.format);
            log.info("Character set: " + rcvMessage.characterSet);
            log.info("Put date/time: " + rcvMessage.putDateTime.toZonedDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            log.info("Put application name: " + rcvMessage.putApplicationName);
            log.info("----------------------------------------");

//            String msgText = rcvMessage.readStringOfCharLength(rcvMessage.getTotalMessageLength());
            log.info("Message body:");
            log.info("----------------------------------------");
            log.info(new MQHeaderList(rcvMessage, true).toString());
            log.info("----------------------------------------");
        } catch (MQException ex) {
            processMqException(ex);

        } catch (IOException | MQDataException ex) {
            log.info("An IOException occurred whilst writing to the message buffer: " + ex);
        }
    }

    public void writeToQueue(String queueName, String message) {
        try (MQCloseableQueue closeableQueue = new MQCloseableQueue(mqConfiguration, queueName, MQConstants.MQOO_OUTPUT)) {
            MQMessage mqMessage = new MQMessage();
            mqMessage.writeUTF(message);
            MQPutMessageOptions pmo = new MQPutMessageOptions();

            log.info("Sending a message with body:");
            log.info("----------------------------------------");
            log.info(message);
            log.info("----------------------------------------");
            closeableQueue.getQueue().put(mqMessage, pmo);
        } catch (MQException ex) {
            processMqException(ex);
        } catch (IOException ex) {
            log.info("An IOException occurred whilst writing to the message buffer: " + ex);
        }
    }

    private void processMqException(MQException ex) {
        log.info("Reason: " + MQConstants.lookupReasonCode(ex.reasonCode));
        log.info("An IBM MQ Error occurred : Completion Code " + ex.completionCode
                + " Reason Code " + ex.reasonCode);
        for (Throwable t = ex.getCause(); t != null; t = t.getCause()) {
            log.info("... Caused by ");
            t.printStackTrace();
        }
    }
}

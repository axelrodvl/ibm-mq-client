package co.axelrod.ibm.mq.client.mq;

import co.axelrod.ibm.mq.client.configuration.MQConfiguration;
import co.axelrod.ibm.mq.client.util.HeaderUtil;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQRFH2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

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
            MQMessage mqMessage = new MQMessage();
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            closeableQueue.getQueue().get(mqMessage, gmo);

            log.info("");
            log.info("Received message info");
            printMessageInfo(mqMessage);

            log.info("Message headers:");
            log.info("----------------------------------------");
            mqMessage.getPropertyNames("usr.%").asIterator().forEachRemaining((propertyName) -> {
                try {
                    log.info(propertyName + "=" + mqMessage.getStringProperty(propertyName));
                } catch (MQException e) {
                    e.printStackTrace();
                }
            });

            log.info("----------------------------------------");
            log.info("Message body:");
            log.info("----------------------------------------");
            log.info(mqMessage.readStringOfByteLength(mqMessage.getMessageLength()));
            log.info("----------------------------------------");
        } catch (MQException ex) {
            processMqException(ex);

        } catch (IOException ex) {
            log.info("An IOException occurred whilst writing to the message buffer: " + ex);
        }
    }

    public void writeToQueue(String queueName, String message) {
        writeToQueue(queueName, new HashMap<String, String>(), message);
    }

    public void writeToQueue(String queueName, Map<String, String> headers, String message) {
        try (MQCloseableQueue closeableQueue = new MQCloseableQueue(mqConfiguration, queueName, MQConstants.MQOO_OUTPUT)) {
            MQMessage mqMessage = new MQMessage();

            // Set the RFH2 Values
            MQRFH2 rfh2 = new MQRFH2();
            rfh2.setEncoding(CMQC.MQENC_NATIVE);
            rfh2.setCodedCharSetId(CMQC.MQCCSI_INHERIT);
            rfh2.setFormat(CMQC.MQFMT_NONE); // rfh2.setFormat(CMQC.MQFMT_STRING);
            rfh2.setFlags(0);
            rfh2.setNameValueCCSID(1208);

            // jms_none, jms_text, jms_bytes, jms_map, jms_stream & jms_object
            rfh2.setFieldValue("mcd", "Msd", "jms_text");
            rfh2.setFieldValue("jms", "Dst", "queue:///" + queueName);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                rfh2.setFieldValue("usr", entry.getKey(), entry.getValue());
            }

            // Set the MQRFH2 structure to the message
            rfh2.write(mqMessage);

            // Write message data
            mqMessage.write(message.getBytes(StandardCharsets.UTF_8));

            // Set MQMD values
            mqMessage.messageId = CMQC.MQMI_NONE;
            mqMessage.correlationId = CMQC.MQCI_NONE;
            mqMessage.messageType = CMQC.MQMT_DATAGRAM;
            // IMPORTANT: Set the format to MQRFH2 aka JMS Message.
            mqMessage.format = CMQC.MQFMT_RF_HEADER_2;
            mqMessage.characterSet = 1208;

            // put the message on the queue
            MQPutMessageOptions pmo = new MQPutMessageOptions();

            closeableQueue.getQueue().put(mqMessage, pmo);
            log.info("");
            log.info("Sent message info:");
            printMessageInfo(mqMessage);
            log.info("----------------------------------------");
            log.info(message);
            log.info("----------------------------------------");

            log.info("Message headers:");
            log.info("----------------------------------------");
            headers.entrySet().forEach(entry -> log.info(entry.getKey() + "=" + entry.getValue()));
            log.info("----------------------------------------");
            log.info("Message body:");
            log.info("----------------------------------------");
            log.info(message);
            log.info("----------------------------------------");
        } catch (MQException ex) {
            processMqException(ex);
        } catch (IOException ex) {
            log.info("An IOException occurred whilst writing to the message buffer: " + ex);
        }
    }

    private void printMessageInfo(MQMessage mqMessage) {
        log.info("----------------------------------------");
        log.info("Message ID: " + HeaderUtil.bytesToHex(mqMessage.messageId));
        log.info("Correlation ID: " + HeaderUtil.bytesToHex(mqMessage.correlationId));
        log.info("Format: " + mqMessage.format);
        log.info("Character set: " + mqMessage.characterSet);
        log.info("Put date/time: " + mqMessage.putDateTime.toZonedDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        log.info("Put application name: " + mqMessage.putApplicationName);
        log.info("----------------------------------------");
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

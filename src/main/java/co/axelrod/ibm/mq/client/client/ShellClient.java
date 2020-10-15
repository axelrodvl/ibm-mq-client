package co.axelrod.ibm.mq.client.client;

import co.axelrod.ibm.mq.client.mq.MQClient;
import co.axelrod.ibm.mq.client.mq.MQPCFClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@ShellComponent
@AllArgsConstructor
@Slf4j
public class ShellClient {
    private final ObjectProvider<MQPCFClient> mqClientProvider;
    private final MQClient mqClient;

//    @ShellMethod("list-queues")
//    public void listQueues() {
//        try (MQPCFClient mqPcfClient = mqClientProvider.getObject()) {
//            mqPcfClient.listQueueNames();
//        }
//    }

    @ShellMethod("read-queue")
    public void readQueue(@ShellOption(help = "queue-name") String queueName) {
        mqClient.readFromQueue(queueName);
    }

    @ShellMethod("write-queue")
    public void writeQueue(@ShellOption(help = "queue-name") String queueName, @ShellOption(help = "message") String message) {
        mqClient.writeToQueue(queueName, message);
    }

    @ShellMethod("write-queue")
    public void writeQueueFromFile(@ShellOption(help = "queue-name") String queueName, @ShellOption(help = "file-path") String filePath) throws IOException {
        mqClient.writeToQueue(queueName, Files.readString(Path.of(filePath), StandardCharsets.UTF_8));
    }
}
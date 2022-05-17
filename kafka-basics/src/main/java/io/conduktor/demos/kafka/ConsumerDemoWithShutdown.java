package io.conduktor.demos.kafka;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoWithShutdown {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class.getSimpleName());

    public static void main(String[] args) {
        log.info("I am a kafka consumer");

        String bootstrapServer = "127.0.0.1:9092";
        String groupId = "my-third-application";
        String topic = "demo_java";

        //create consumer config
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        //get a reference to the current thread
        final Thread mainThread = Thread.currentThread();

        //add the shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...");
                consumer.wakeup(); //this is called so consumer.poll catches a wakeup exception

                //join the main thread to allow the execution of the code in the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            //subscribe consumer to our topic(s)
            consumer.subscribe(Arrays.asList(topic));

            //poll for new data
            while(true) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(1000)); //wait for 1000 ms to receive info from kafka

                for (ConsumerRecord<String, String> record : records) {
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                }

            }
        } catch (WakeupException e) {
            log.info("Wakeup exception");
            //we ignore this since this is expected when closing a consumer
        } catch (Exception e) {
            log.error("Unexpected exception");
        } finally {
            consumer.close(); //this will also commit the offsets if needed
            log.info("The consumer has closed");
        }
    }
}

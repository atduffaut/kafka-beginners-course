package io.conduktor.demos.kafka;


import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoKeys {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemoKeys.class.getSimpleName());

    public static void main(String[] args) {
        log.info("I am a kafka producer");

        //create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        //create producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i = 0; i < 10; i++) {

            String topic = "demo_java";
            String value = "hello world " + i;
            String key = "id_" + i;

            //create a producer record
            ProducerRecord<String, String> producerRecord =
                    new ProducerRecord<>(topic, key, value);
            //send data - async operation
            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    //executes every time a record is successfully sent or an exception is thrown
                    if (e == null) {
                        //the record was successfully sent
                        log.info("Received new metadata/ \n" +
                                "Topic: " + recordMetadata.topic() + "\n" +
                                "Key: " + producerRecord.key() + "\n" +
                                "Partition: " + recordMetadata.partition() + "\n" +
                                "Offset: " + recordMetadata.offset() + "\n" +
                                "Timestamp: " + recordMetadata.timestamp());
                    } else {
                        log.error("Error while producing", e);
                    }
                }
            });
        }

        //flush and close the producer - sync operation
        producer.flush();

        producer.close();
    }
}

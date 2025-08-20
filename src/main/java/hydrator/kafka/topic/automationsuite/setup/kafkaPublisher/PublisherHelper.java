package hydrator.kafka.topic.automationsuite.setup.kafkaPublisher;

import com.google.protobuf.GeneratedMessageV3;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static hydrator.kafka.topic.automationsuite.setup.generators.TestDataGenerator.generateId;

@Slf4j
public class PublisherHelper {
    private final Producer<String, GeneratedMessageV3> producer;
    //    private final Producer<String, ? super GeneratedMessageV3> producer;
    private final Producer<Integer, ? super GeneratedMessageV3> producerWithIntegerKey;
    private final Properties propNew;

    public PublisherHelper(Properties prop, String bootstrapServer, String schemaRegistryUrl) {
        //Properties props = new Properties();
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, "Security.OutboundMatchingAutomation." + generateId());
        prop.put(ConsumerConfig.CLIENT_ID_CONFIG, "ProducerHelper");
//        prop.put(ConsumerConfig.CLIENT_ID_CONFIG, "ProducerHelper." + generateId());

        if (prop.getProperty("enable.ssl").equals("true")) {
            prop.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            prop.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
            prop.put(SaslConfigs.SASL_JAAS_CONFIG,
                    "software.amazon.msk.auth.iam.IAMLoginModule required;");
            prop.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS,
                    "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
        }

        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaProtobufSerde.class);
        prop.put(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
                schemaRegistryUrl);
        prop.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer",
                "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer");
        this.producer = new KafkaProducer<>(prop);
        propNew = prop;
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.IntegerSerde.class);
        prop.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        this.producerWithIntegerKey = new KafkaProducer<>(prop);
    }

    private RecordMetadata publishDummyMessage(GeneratedMessageV3 data, String key, String headerKey, String headerValue, String topic)
            throws ExecutionException, InterruptedException {

        ProducerRecord<String, GeneratedMessageV3> record = new ProducerRecord<>(topic, key, data);
        record.headers().add(headerKey, headerValue.getBytes());

        RecordMetadata metadata = producer.send(record).get();

        log.info("Data details are: {}", data);
        log.info("Message sent to topic {} with offset {} and partition {}",
                metadata.topic(), metadata.offset(), metadata.partition());

        return metadata;
    }

    public RecordMetadata publishMessageToAgentSkillTopic(GeneratedMessageV3 data, String key, String headerKey, String headerValue)
            throws ExecutionException, InterruptedException {
        String topic = propNew.getProperty("agent.skill.association");
        return publishDummyMessage(data, key, headerKey, headerValue, topic);
    }

    private RecordMetadata publishEmptyMessage(String key, String headerKey, String headerValue, String topic)
            throws ExecutionException, InterruptedException {
        ProducerRecord<String, GeneratedMessageV3> record = new ProducerRecord<>(topic, key, null);
        record.headers().add(headerKey, headerValue.getBytes());
        RecordMetadata metadata = producer.send(record).get();
        log.info("Sent empty message to topic {} with offset {} and partition {}",
                metadata.topic(), metadata.offset(), metadata.partition());
        return metadata;
    }

    public RecordMetadata publishEmptyMessageToAgentSkillTopic(String key, String headerKey, String headerValue)
            throws ExecutionException, InterruptedException {
        String topic = propNew.getProperty("agent.skill.association");
        return publishEmptyMessage(key, headerKey, headerValue, topic);
    }

}

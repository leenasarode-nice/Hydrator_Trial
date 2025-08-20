
package hydrator.kafka.topic.automationsuite.setup.kafkalisteners;

import com.nice.acdsettings.AgentSkill;
import hydrator.kafka.topic.automationsuite.setup.kafkahelper.SerdeFactory;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.StreamsConfig;

import static hydrator.kafka.topic.automationsuite.setup.generators.TestDataGenerator.generateId;

@Slf4j
public class AgentSkillAssociationListener {

    private static String topic;
    private final Consumer<String, AgentSkill> consumer;
    @Getter
    private ConsumerRecord<String, AgentSkill> latestMessage;
    private final Map<Integer, ConsumerRecord<String, AgentSkill>> latestPartitionRecords = new HashMap<>();


    public AgentSkillAssociationListener(String bootstrapServers, boolean enableSSL, Properties prop) {
        topic = prop.getProperty("agent.skill.association");
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "AgentSkillAssociation.OutboundMatchingAutomation." + generateId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "Agent.Skill.Association");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        if (enableSSL) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            props.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "software.amazon.msk.auth.iam.IAMLoginModule required;");
            props.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
        }
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaProtobufSerde.class);
        props.put(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, prop.getProperty("schema.registry.url"));
        props.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, AgentSkill.class.getName());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer");
        SerdeFactory serdeFactory = new SerdeFactory(prop.getProperty("schema.registry.url"));
        this.consumer = new KafkaConsumer<>(props, new StringDeserializer(), serdeFactory.serde(AgentSkill.class).deserializer());
    }

    public void consumeAgentSkillAssociation(Properties prop) throws InterruptedException {
        topic = prop.getProperty("agent.skill.association");
        latestPartitionRecords.clear();

        List<TopicPartition> partitions = consumer.partitionsFor(topic)
                .stream()
                .map(p -> new TopicPartition(p.topic(), p.partition()))
                .collect(Collectors.toList());

        if (partitions.isEmpty()) {
            log.warn("No partitions found for topic: {}", topic);
            return;
        }

        consumer.assign(partitions);
        consumer.seekToEnd(partitions);  // Get end offsets

        // Move to last message of each partition
        for (TopicPartition tp : partitions) {
            long endOffset = consumer.position(tp);
            long fetchOffset = Math.max(endOffset - 1, 0);
            consumer.seek(tp, fetchOffset);
        }

        Set<Integer> expectedPartitions = partitions.stream()
                .map(TopicPartition::partition)
                .collect(Collectors.toSet());

        int retries = 5;
        int receivedPartitions = 0;

        while (retries-- > 0 && receivedPartitions < expectedPartitions.size()) {
            ConsumerRecords<String, AgentSkill> records = consumer.poll(Duration.ofSeconds(2));

            for (ConsumerRecord<String, AgentSkill> record : records) {
                if (!latestPartitionRecords.containsKey(record.partition())) {
                    latestPartitionRecords.put(record.partition(), record);
                    log.info("Partition: {}, Offset: {}, Timestamp: {}, Key: {}",
                            record.partition(),
                            record.offset(),
                            Instant.ofEpochMilli(record.timestamp()),
                            record.key());
                }
            }

            receivedPartitions = latestPartitionRecords.size();
            if (receivedPartitions < expectedPartitions.size()) {
                log.info("Waiting for missing partitions... got {}/{}", receivedPartitions, expectedPartitions.size());
                Thread.sleep(1000); // Wait before next poll
            }
        }

        if (receivedPartitions < expectedPartitions.size()) {
            Set<Integer> missing = new HashSet<>(expectedPartitions);
            missing.removeAll(latestPartitionRecords.keySet());
            log.warn("Missing records from partitions: {}", missing);
        }
    }

    public ConsumerRecord<String, AgentSkill> getLatestRecordByEpoch() {
        return latestPartitionRecords.values().stream()
                .max(Comparator.comparingLong(ConsumerRecord::timestamp))
                .orElse(null);
    }

    public AgentSkill getLatestMessageValue() {
        ConsumerRecord<String, AgentSkill> latestRecord = getLatestRecordByEpoch();
        return latestRecord != null ? latestRecord.value() : null;
    }

    public Map<Integer, Long> getPartitionTimestampsInEpoch() {
        Map<Integer, Long> partitionTimestamps = new HashMap<>();

        for (Map.Entry<Integer, ConsumerRecord<String, AgentSkill>> entry : latestPartitionRecords.entrySet()) {
            int partition = entry.getKey();
            long epoch = entry.getValue().timestamp();
            partitionTimestamps.put(partition, epoch);

            // Print directly inside the method
            log.info("Partition: " + partition + ", Epoch Timestamp: " + epoch);
        }

        return partitionTimestamps;
    }

    public Map.Entry<Integer, Long> getLatestPartitionEpoch() {
        Map<Integer, Long> epochTimestamps = new HashMap<>();

        for (Map.Entry<Integer, ConsumerRecord<String, AgentSkill>> entry : latestPartitionRecords.entrySet()) {
            long epoch = entry.getValue().timestamp();
            epochTimestamps.put(entry.getKey(), epoch);
        }

        Map.Entry<Integer, Long> latestEntry = epochTimestamps.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (latestEntry != null) {
            log.info("Latest Timestamp Found:");
            log.info("Partition: " + latestEntry.getKey() + ", Epoch: " + latestEntry.getValue());
        }

        return latestEntry;
    }

    public void clearAgentSkillAssociationMap() {
        latestMessage = null;
    }

}
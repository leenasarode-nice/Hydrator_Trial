package hydrator.kafka.topic.automationsuite.setup.kafkahelper;

import com.google.protobuf.Message;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.StreamsConfig;

public class SerdeFactory {

  private final String schemaRegistryUrl;
  private final SchemaRegistryClient schemaRegistryClient;

  public SerdeFactory(String schemaRegistryUrl) {
    this(schemaRegistryUrl, null);
  }

  public SerdeFactory(String schemaRegistryUrl, SchemaRegistryClient schemaRegistryClient) {
    this.schemaRegistryUrl = schemaRegistryUrl;
    this.schemaRegistryClient = schemaRegistryClient;
  }

  public <T extends Message> KafkaProtobufSerde<T> serde(Class<T> clazz) {
    final KafkaProtobufSerde<T> serde =
        schemaRegistryClient == null ? new KafkaProtobufSerde<>(clazz)
            : new KafkaProtobufSerde<>(schemaRegistryClient, clazz);
    Map<String, String> serdeConfig = new HashMap<>();
    serdeConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
        KafkaProtobufSerde.class.getName());
    serdeConfig.put(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    serdeConfig.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, clazz.getName());
    serde.configure(serdeConfig, false);
    return serde;
  }

}

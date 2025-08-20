package hydrator.kafka.topic.automationsuite.setup.kafkahelper;

import java.util.Properties;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import hydrator.kafka.topic.automationsuite.setup.kafkalisteners.*;

@Getter
public class ConsumerHelper {

    private final AgentSkillAssociationListener agentSkillAssociationListener;

    public ConsumerHelper(String bootstrapServer, boolean enableSSL, Properties prop) {
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.info("Consumer Helper launched");

        agentSkillAssociationListener = new AgentSkillAssociationListener(bootstrapServer, enableSSL, prop);

    }

    public void consumeAgentSkillAssociation(Properties prop) throws InterruptedException {
        agentSkillAssociationListener.consumeAgentSkillAssociation(prop);
    }

    public void clearConsumerMaps() {
        agentSkillAssociationListener.clearAgentSkillAssociationMap();
    }

}

package hydrator.kafka.topic.automationsuite.setup.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentSkills {

    private final int busNo;
    private final int agentNo;
    private final int skillNo;
    private final String sourceCluster;

}
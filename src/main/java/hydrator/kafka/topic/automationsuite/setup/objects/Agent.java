package hydrator.kafka.topic.automationsuite.setup.objects;


import hydrator.kafka.topic.automationsuite.setup.generators.AttributesGenerator;
import hydrator.kafka.topic.automationsuite.setup.generators.SkillsGenerator;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;
import static hydrator.kafka.topic.automationsuite.setup.generators.TestDataGenerator.generateFiveDigitsId;
import static hydrator.kafka.topic.automationsuite.setup.generators.TestDataGenerator.generateId;

@Data
@Builder
public class Agent {

    public static final int DEFAULT_NUM_SKILLS = 5;
    public static final int DEFAULT_NUM_ATTRIBUTES = 5;
    private static final int ONE_MINUTE_MS = 60000;
    private final String buId;
    private final String agentNo;
    private final String agentUuid;
    private final String agentSessionId;
    private AgentState state;
    private List<Integer> attributes;
    private List<Integer> oldAttributes;
    private List<Skill> skills;
    private List<Skill> oldSkills;
    private Long queueTime;
    private Skill skill;

    public static Agent buildAgentData(Tenant tenant, MediaType mediaType, int priority, int proficiency) {
        return Agent.builder()
                .buId(tenant.getTenantId())
                .agentNo(generateFiveDigitsId())
                .agentSessionId(generateId())
                .agentUuid(UUID.randomUUID().toString())
                .attributes(AttributesGenerator.generateAttributes(
                        DEFAULT_NUM_ATTRIBUTES  ))
                .skills(SkillsGenerator.generateSkills(DEFAULT_NUM_SKILLS,mediaType.ordinal(),priority,proficiency ))
                .queueTime(System.currentTimeMillis())
                .build();
    }

}

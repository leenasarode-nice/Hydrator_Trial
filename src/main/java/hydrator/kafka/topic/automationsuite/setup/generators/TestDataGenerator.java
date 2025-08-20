package hydrator.kafka.topic.automationsuite.setup.generators;

import com.google.protobuf.Timestamp;
import com.nice.acdsettings.AgentSkill;
import com.nice.acdsettings.AgentSkillStatus;
import hydrator.kafka.topic.automationsuite.setup.objects.Agent;
import hydrator.kafka.topic.automationsuite.setup.objects.Tenant;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

public class TestDataGenerator {

    private static final Random random = new Random();

    public static String generateId() {
        return Integer.toString(Math.abs(random.nextInt()));
    }

    public static String generateFiveDigitsId() {
        return UUID.randomUUID().toString().replaceAll("\\D", "").substring(0, 5);
    }

    public static Timestamp getCurrentTimestamp() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }

    public static AgentSkill getAgentSkillData(Tenant tenant, Agent agent, String agentSkillStatus, int proficiency, String clusterId, boolean deleteFlag) {
        return AgentSkill.newBuilder()
                .setAgentNo(Integer.parseInt(agent.getAgentNo()))
                .setUserId(agent.getAgentUuid())
                .setSkillNo(Integer.parseInt(agent.getSkills().get(0).getSkillId()))
                .setBusNo(Integer.parseInt(tenant.getTenantId()))
                .setTenantId(tenant.getTenantGuid())
                .setAgentSkillStatus(AgentSkillStatus.valueOf(agentSkillStatus))
                .setAgentSkillProficiency(proficiency)
                .setCreatedTimestamp(getCurrentTimestamp())     // Timestamp
//                .setCreatedTimestamp(getCurrentTimestamp())   // Timestamp with Builder
                .setCreatedByAgentNo(Integer.parseInt(agent.getAgentNo()))
                .setCreatedByUserId(agent.getAgentUuid())
                .setModifiedTimestamp(getCurrentTimestamp())
                .setModifiedByAgentNo(Integer.parseInt(agent.getAgentNo()))
                .setModifiedByUserId(agent.getAgentUuid())
                .setSourceCluster(clusterId)
                .setIsDelete(deleteFlag)
                .build();
    }

    public static AgentSkill getStaticAgentSkillData(Tenant tenant, Agent agent, String agentSkillStatus, int proficiency, String clusterId, boolean deleteFlag, int agentNo, int skillNo) {
        return AgentSkill.newBuilder()
                .setAgentNo(agentNo)
                .setUserId(agent.getAgentUuid())
                .setSkillNo(skillNo)
                .setBusNo(Integer.parseInt(tenant.getTenantId()))
                .setTenantId(tenant.getTenantGuid())
                .setAgentSkillStatus(AgentSkillStatus.valueOf(agentSkillStatus))
                .setAgentSkillProficiency(proficiency)
                .setCreatedTimestamp(getCurrentTimestamp())     // Timestamp
//                .setCreatedTimestamp(getCurrentTimestamp())   // Timestamp with Builder
                .setCreatedByAgentNo(Integer.parseInt(agent.getAgentNo()))
                .setCreatedByUserId(agent.getAgentUuid())
                .setModifiedTimestamp(getCurrentTimestamp())
                .setModifiedByAgentNo(Integer.parseInt(agent.getAgentNo()))
                .setModifiedByUserId(agent.getAgentUuid())
                .setSourceCluster(clusterId)
                .setIsDelete(deleteFlag)
                .build();
    }

    public static AgentSkill getStaticAgentSkillDataWithNoAgentAndSkillNo(Tenant tenant, Agent agent, String agentSkillStatus, int proficiency, String clusterId, boolean deleteFlag) {
        return AgentSkill.newBuilder()
                .setUserId(agent.getAgentUuid())
                .setBusNo(Integer.parseInt(tenant.getTenantId()))
                .setTenantId(tenant.getTenantGuid())
                .setAgentSkillStatus(AgentSkillStatus.valueOf(agentSkillStatus))
                .setAgentSkillProficiency(proficiency)
                .setCreatedTimestamp(getCurrentTimestamp())     // Timestamp
//                .setCreatedTimestamp(getCurrentTimestamp())   // Timestamp with Builder
                .setCreatedByAgentNo(Integer.parseInt(agent.getAgentNo()))
                .setCreatedByUserId(agent.getAgentUuid())
                .setModifiedTimestamp(getCurrentTimestamp())
                .setModifiedByAgentNo(Integer.parseInt(agent.getAgentNo()))
                .setModifiedByUserId(agent.getAgentUuid())
                .setSourceCluster(clusterId)
                .setIsDelete(deleteFlag)
                .build();
    }

}

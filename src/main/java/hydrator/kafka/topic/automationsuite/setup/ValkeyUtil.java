package hydrator.kafka.topic.automationsuite.setup;

import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ValkeyUtil {

    private final Jedis jedis;

    public ValkeyUtil(Jedis jedis) {
        this.jedis = jedis;
    }

    public Map<String, Integer> fetchDataFromValkey() {
        String agent = jedis.get("agentNo");
        String skill = jedis.get("skillNo");

        if (agent != null && skill != null) {
            Map<String, Integer> result = new HashMap<>();
            result.put("agentNo", Integer.parseInt(agent));
            result.put("skillNo", Integer.parseInt(skill));
            return result;
        }

        return Collections.emptyMap();
    }

    public void setAgentSkillInValkey(int agentNo, int skillNo) {
        jedis.set("agentNo", String.valueOf(agentNo));
        jedis.set("skillNo", String.valueOf(skillNo));
    }

}

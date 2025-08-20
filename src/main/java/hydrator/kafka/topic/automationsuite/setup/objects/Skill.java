package hydrator.kafka.topic.automationsuite.setup.objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Skill {
    private String skillId;
    private Integer channel;
    private Integer priority;
    private Integer proficiency;
}

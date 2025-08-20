package hydrator.kafka.topic.automationsuite.setup.objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Tenant {

    private String tenantId;
    private String tenantGuid;

}


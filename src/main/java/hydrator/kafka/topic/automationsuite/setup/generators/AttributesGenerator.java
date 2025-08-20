package hydrator.kafka.topic.automationsuite.setup.generators;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AttributesGenerator {

    private static final Random rng = new Random();

    public static Integer getIntId() {
        return Math.abs(rng.nextInt());
    }

    public static List<Integer> generateAttributes(int numAttributes) {
        return IntStream.range(0, numAttributes)
                .mapToObj(i -> getIntId())
                .collect(Collectors.toList());
    }

    public static Integer randomAttribute(List<Integer> attributes) {
        return attributes.get(rng.nextInt(attributes.size()));
    }

}

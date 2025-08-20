package hydrator.kafka.topic.automationsuite.setup.generators;

import hydrator.kafka.topic.automationsuite.setup.objects.MediaType;
import hydrator.kafka.topic.automationsuite.setup.objects.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SkillsGenerator {

    private static final Random rng = new Random();

    public static String genId() {
        return Integer.toString(Math.abs(rng.nextInt()));
    }

    public static List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> generateSkills(int numSkills, int mediaType, int priority, int proficiency) {
        return IntStream.range(0, numSkills).mapToObj(
                i -> new hydrator.kafka.topic.automationsuite.setup.objects.Skill(genId(), mediaType, priority, proficiency)).collect(Collectors.toList());
    }

    /**
     * Generates a list of `Skill` objects based on the provided skill details.
     *
     * <p>This method creates a list of `Skill` objects using the provided map of skill details.
     * Each entry in the map represents a skill, where the key is the skill ID and the value is the proficiency.
     * The method sets the media type to `CALL` for all generated skills.
     *
     * @param skillDetails a map where the key is the skill ID and the value is the proficiency.
     * @return a list of `Skill` objects generated from the provided skill details.
     */
    public static List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> generateSkills(Map<String, Integer> skillDetails, List<Integer> skillPriority) {
        List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> listOfSkills = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : skillDetails.entrySet()) {
            listOfSkills.add(new hydrator.kafka.topic.automationsuite.setup.objects.Skill(entry.getKey(), MediaType.CALL.ordinal(), skillPriority.get(i), entry.getValue()));
            i++;
        }

        return listOfSkills;
    }

    public static List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> generateSkillsForDigital(Map<String, Integer> skillDetails, List<Integer> skillPriority) {
        List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> listOfSkills = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : skillDetails.entrySet()) {
            listOfSkills.add(new hydrator.kafka.topic.automationsuite.setup.objects.Skill(entry.getKey(), MediaType.DIGITAL.ordinal(), skillPriority.get(i), entry.getValue()));
            i++;
        }

        return listOfSkills;
    }

    public static List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> generateSkills(List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> skillDetails) {
        List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> listOfSkills = new ArrayList<>();

        for (hydrator.kafka.topic.automationsuite.setup.objects.Skill entry : skillDetails) {
            listOfSkills.add(new hydrator.kafka.topic.automationsuite.setup.objects.Skill(entry.getSkillId(), entry.getChannel(), entry.getPriority(), entry.getProficiency()));
        }

        return listOfSkills;
    }

    public static hydrator.kafka.topic.automationsuite.setup.objects.Skill randomSkill(List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> skills) {
        if (skills.isEmpty()) {
            return null;
        }
        return skills.get(rng.nextInt(skills.size()));
    }


    private static boolean hasUniqueSkillId(List<hydrator.kafka.topic.automationsuite.setup.objects.Skill> skills, hydrator.kafka.topic.automationsuite.setup.objects.Skill newSkill) {
        for (Skill s : skills) {
            if (s.getSkillId().equals(newSkill.getSkillId())) {
                return false;
            }
        }
        return true;
    }

    private static Integer genProficiency() {
        return rng.nextInt(20) + 1;
    }

}


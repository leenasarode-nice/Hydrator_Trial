package hydrator.kafka.topic.automationsuite.tests;

import com.nice.acdsettings.AgentSkill;
import hydrator.kafka.topic.automationsuite.setup.generators.TestDataGenerator;
import hydrator.kafka.topic.automationsuite.setup.objects.Agent;
import hydrator.kafka.topic.automationsuite.setup.objects.AgentSkills;
import hydrator.kafka.topic.automationsuite.setup.objects.MediaType;
import hydrator.kafka.topic.automationsuite.setup.objects.Tenant;
import hydrator.kafka.topic.automationsuite.setup.reports.Log;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static hydrator.kafka.topic.automationsuite.setup.TestConstant.AGENT_SKILL_STATUS_ACTIVE;
import static hydrator.kafka.topic.automationsuite.setup.TestConstant.AGENT_SKILL_STATUS_DELETED;
import static org.testng.Assert.*;

public class TestAgentSkillMinimalCacheHydrator extends TestBaseClass {

    /**
     * This Test case is for data validation, whether the record got stored or not into agent_skill_minimal table in Dimension DB
     */
//    @Test(priority = 1)
    public void validateRecordGetStoredInDb() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Verify the record get stored in Dimension DB");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message
        AgentSkill initialMessage = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + initialMessage.getSkillNo();
        publisherHelper.publishMessageToAgentSkillTopic(initialMessage, key, headerKey, headerValue);

        delay();

        int kafkaAgentNo = initialMessage.getAgentNo();
        int kafkaSkillNo = initialMessage.getSkillNo();

        String fetchTheCountOfEntry = "SELECT COUNT(*) as record_count FROM test_connection.agent_skill_minimal s WHERE agent_no = " + kafkaAgentNo + " AND JSON_CONTAINS(skills, '" + kafkaSkillNo + "', '$');";

        int count = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchTheCountOfDataFromDB(fetchTheCountOfEntry, "record_count");
            return databaseUtil.getCountOfData();
        });

        assertEquals(count, 1, "Record is not populated");

        System.out.println("---> " + initialMessage);
        System.out.println();
        System.out.println("1st Test case is passed");
    }

    /**
     * This Test case is for data validation, whether the Kafka values are populated or not into agent_skill_minimal table in Dimension DB
     */
//    @Test(priority = 2)
    public void validateDbDataAndKafkaTopicData() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate the agent_skill_minimal table fields in Dimension DB");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message
        AgentSkill initialMessage = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + initialMessage.getSkillNo();
        RecordMetadata metadata = publisherHelper.publishMessageToAgentSkillTopic(initialMessage, key, headerKey, headerValue);

        delay();

        int kafkaAgentNo = initialMessage.getAgentNo();
        String kafkaSourceCluster = initialMessage.getSourceCluster();
        int kafkaSkillNo = initialMessage.getSkillNo();
        int kafkaBusNo = initialMessage.getBusNo();

        String fetchTheEntryFields = "SELECT s.bus_no, s.agent_no, " + kafkaSkillNo + " AS skill_no, s.source_cluster FROM test_connection.agent_skill_minimal s WHERE agent_no = " + kafkaAgentNo + " AND JSON_CONTAINS(skills, '" + kafkaSkillNo + "', '$');";
        List<AgentSkills> dbRecords = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchDataFromDB(fetchTheEntryFields);
            return databaseUtil.getDataFromDBList();
        });

        AgentSkills kafkaData = new AgentSkills(kafkaBusNo, kafkaAgentNo, kafkaSkillNo, kafkaSourceCluster);
        AgentSkills dbData = dbRecords.get(0);

        assertEquals(kafkaData, dbData, "Kafka data does not match DB record");

    }

    /**
     * This Test case is for data validation, whether the new Skill values are populated or not into skills column, agent_skill_minimal table in Dimension DB
     */
//    @Test(priority = 3)
    public void validateSkillIsAddedInSkillsColumn() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate new skill gets add in skills column");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message
        AgentSkill initialMessage = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + initialMessage.getSkillNo();
        publisherHelper.publishMessageToAgentSkillTopic(initialMessage, key, headerKey, headerValue);

        delay();

        int kafkaAgentNo = initialMessage.getAgentNo();
        String kafkaSourceCluster = initialMessage.getSourceCluster();
        int kafkaSkillNo = initialMessage.getSkillNo();

        String fetchTheEntryBySkill = "SELECT s.bus_no, s.agent_no, " + kafkaSkillNo + " AS skill_no, s.source_cluster FROM test_connection.agent_skill_minimal s WHERE source_cluster = '" + kafkaSourceCluster + "' AND agent_no = " + kafkaAgentNo + " AND JSON_CONTAINS(skills, '" + kafkaSkillNo + "', '$');";
        List<AgentSkills> dbRecords = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchDataFromDB(fetchTheEntryBySkill);
            return databaseUtil.getDataFromDBList();
        });

        int skillNo = dbRecords.get(0).getSkillNo();
        assertEquals(skillNo, kafkaSkillNo, "Skill number is not present in skills column");
    }

    /**
     * This test case validates, whether the skill value has been removed from the skills column,
     * while ensuring that the entry remains in the agent_skill_minimal table in the Dimension DB
     */
//    @Test(priority = 4)
    public void validateSkillIsRemovedFromSkillsColumn() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate that the skill_no gets removed when the isDelete flag is false");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message with the agent's skill status set to ACTIVE
        AgentSkill initialMessageActiveAgentSkill = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessageActiveAgentSkill.getSourceCluster() + "_" + initialMessageActiveAgentSkill.getAgentNo() + "_" + initialMessageActiveAgentSkill.getSkillNo();
        publisherHelper.publishMessageToAgentSkillTopic(initialMessageActiveAgentSkill, key, headerKey, headerValue);

//        Publish a second message with the agent's skill status set to ACTIVE and a different skill number
        int tempSkillNo = Integer.parseInt(agent.getSkills().get(1).getSkillId());
        String tempKey = initialMessageActiveAgentSkill.getSourceCluster() + "_" + initialMessageActiveAgentSkill.getAgentNo() + "_" + tempSkillNo;
        AgentSkill activeAgentSkillSecondary = TestDataGenerator.getStaticAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false, initialMessageActiveAgentSkill.getAgentNo(), tempSkillNo);
        publisherHelper.publishMessageToAgentSkillTopic(activeAgentSkillSecondary, tempKey, headerKey, headerValue);

//        Publish a third message with the agent's skill status set to DELETED, using the same agent and skill number as in the first message
        AgentSkill deletedAgentSkill = TestDataGenerator.getStaticAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_DELETED, 3, clusterId, false, initialMessageActiveAgentSkill.getAgentNo(), initialMessageActiveAgentSkill.getSkillNo());
        publisherHelper.publishMessageToAgentSkillTopic(deletedAgentSkill, key, headerKey, headerValue);

        delay();

        int kafkaAgentNo = deletedAgentSkill.getAgentNo();
        String kafkaSourceCluster = deletedAgentSkill.getSourceCluster();
        int kafkaSkillNo = deletedAgentSkill.getSkillNo();

        String fetchSkillCountWhenAgentSkillStatusIsDeleted = "SELECT COUNT(*) as record_count FROM test_connection.agent_skill_minimal s WHERE agent_no = " + kafkaAgentNo + " AND source_cluster = '" + kafkaSourceCluster + "' AND JSON_CONTAINS(skills, '" + kafkaSkillNo + "', '$');";

        int skillCount = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchTheCountOfDataFromDB(fetchSkillCountWhenAgentSkillStatusIsDeleted, "record_count");
            return databaseUtil.getCountOfData();
        }, result -> result == 0);

//        Verify whether the skill has been removed
        assertEquals(skillCount, 0, "Skill is not removed");

        String fetchCountOfEntry = "SELECT COUNT(*) as record_count FROM test_connection.agent_skill_minimal s WHERE agent_no = " + kafkaAgentNo + " AND source_cluster = '" + kafkaSourceCluster + "' AND JSON_CONTAINS(skills, '" + tempSkillNo + "', '$');";

        int count = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchTheCountOfDataFromDB(fetchCountOfEntry, "record_count");
            return databaseUtil.getCountOfData();
        });

//        Verify that the entire entry is not deleted when removing the skill
        assertEquals(count, 1, "Whole Entry is deleted from Table");
    }

    /**
     * This test case validates whether a connection to the Aurora DB can be established and data can be fetched from the agent_skill_minimal table
     */
//    @Test(priority = 5)
    public void validateDimensionDBConnection() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate the DB connection (Aurora DB)");

        String fetchTheCountOfRecord = "SELECT COUNT(*) as record_count FROM test_connection.agent_skill_minimal s;";

        int count = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchTheCountOfDataFromDB(fetchTheCountOfRecord, "record_count");
            return databaseUtil.getCountOfData();
        });

        assertTrue(count >= 0, "Unable to connect to Aurora DB or count is less than zero");
    }

    /**
     * This Test case is for timestamp validation, whether the timestamp is stored as millisecond or not into agent_skill_minimal table in Dimension DB
     */
//    @Test(priority = 6)
    public void validateTimestampConversion() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate timestamp conversion to milliseconds");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message
        AgentSkill initialMessage = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + initialMessage.getSkillNo();
        publisherHelper.publishMessageToAgentSkillTopic(initialMessage, key, headerKey, headerValue);

        delay();

        int kafkaAgentNo = initialMessage.getAgentNo();
        String kafkaSourceCluster = initialMessage.getSourceCluster();
        int kafkaSkillNo = initialMessage.getSkillNo();
        long kafkaModifySeconds = initialMessage.getModifiedTimestamp().getSeconds();
        int kafkaModifyNanos = initialMessage.getModifiedTimestamp().getNanos();

        String fetchTheLastUpdateTimeQuery = "SELECT last_update_time_ms FROM test_connection.agent_skill_minimal WHERE source_cluster = '" + kafkaSourceCluster + "' AND JSON_CONTAINS(skills, '" + kafkaSkillNo + "', '$') AND agent_no = " + kafkaAgentNo + ";";

        long actualTime = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchLastUpdateTimeFromDB(fetchTheLastUpdateTimeQuery);
            return databaseUtil.getLastUpdateTime();
        });

        long timeInMilliscon = convertToMillis(kafkaModifySeconds, kafkaModifyNanos);

//        System.out.println("timeInMilliscon" + timeInMilliscon);
//        System.out.println("actualTime" + actualTime);

        assertEquals(actualTime, timeInMilliscon, "Mismatch in timestamp");
    }

    /**
     * This test case validates whether the last_update_time_ms column gets updated
     * when a duplicate message (i.e., a message with the same key) arrives
     */
//    @Test(priority = 7)
    public void validateLastUpdateTime() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate handling of duplicate messages");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message
        AgentSkill initialMessage = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + initialMessage.getSkillNo();
        publisherHelper.publishMessageToAgentSkillTopic(initialMessage, key, headerKey, headerValue);

        delay();

        int kafkaAgentNo = initialMessage.getAgentNo();
        String kafkaSourceCluster = initialMessage.getSourceCluster();
        int kafkaSkillNo = initialMessage.getSkillNo();

        String fetchInitialLastUpdateTimeQuery = "SELECT last_update_time_ms FROM test_connection.agent_skill_minimal WHERE source_cluster = '" + kafkaSourceCluster + "' AND JSON_CONTAINS(skills, '" + kafkaSkillNo + "', '$') AND agent_no = " + kafkaAgentNo + ";";

        long initialTime = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchLastUpdateTimeFromDB(fetchInitialLastUpdateTimeQuery);
            return databaseUtil.getLastUpdateTime();
        });

//        Publish a duplicate message identical to the initial one, with the same Agent, Skill, and Bus number
        AgentSkill secondDuplicateMessage = TestDataGenerator.getStaticAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false, initialMessage.getAgentNo(), initialMessage.getSkillNo());
        publisherHelper.publishMessageToAgentSkillTopic(secondDuplicateMessage, key, headerKey, headerValue);

        delay();

        String fetchTheLastUpdateTimeQuery = "SELECT last_update_time_ms FROM test_connection.agent_skill_minimal WHERE source_cluster = '" + kafkaSourceCluster + "' AND JSON_CONTAINS(skills, '" + kafkaSkillNo + "', '$') AND agent_no = " + kafkaAgentNo + ";";

        long lastUpdatedTime = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchLastUpdateTimeFromDB(fetchTheLastUpdateTimeQuery);
            return databaseUtil.getLastUpdateTime();
        });

        assertNotEquals(lastUpdatedTime, initialTime, "The timestamp is not updated");
    }

    /**
     * This test case validates whether a new entry for a non-existing agent number is added to the agent_skill_minimal table in the Dimension DB
     */
//    @Test(priority = 8)
    public void validateNewEntryForNewAgentNo() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate the new entry gets populated for non-existing agent_no");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message
        AgentSkill initialMessage = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + initialMessage.getSkillNo();
        publisherHelper.publishMessageToAgentSkillTopic(initialMessage, key, headerKey, headerValue);

        delay();

        int kafkaAgentNo = initialMessage.getAgentNo();
        String kafkaSourceCluster = initialMessage.getSourceCluster();
        int kafkaBusNo = initialMessage.getBusNo();

        String fetchTheEntryFields = "SELECT COUNT(*) as record_count FROM test_connection.agent_skill_minimal s WHERE bus_no = " + kafkaBusNo + " AND agent_no = " + kafkaAgentNo + " AND source_cluster = '" + kafkaSourceCluster + "';";

        int count = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchTheCountOfDataFromDB(fetchTheEntryFields, "record_count");
            return databaseUtil.getCountOfData();
        });

        assertEquals(count, 1, "Entry is not populated for new Agent number");
    }

    /**
     * This test case validates whether the entire entry is deleted from the agent_skill_minimal table in the Dimension DB
     * when the agent's skill status is set to DELETED and the isDeleted flag is true for the given key in the table
     */
//    @Test(priority = 9)
    public void validateEntryIsDeleted() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate that the skill_no gets removed when the isDelete flag is true");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message with isDelete flag is false
        AgentSkill initialMessageWithIsDeletedSetToFalse = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessageWithIsDeletedSetToFalse.getSourceCluster() + "_" + initialMessageWithIsDeletedSetToFalse.getAgentNo() + "_" + initialMessageWithIsDeletedSetToFalse.getSkillNo();
        publisherHelper.publishMessageToAgentSkillTopic(initialMessageWithIsDeletedSetToFalse, key, headerKey, headerValue);

//        Publish a second message with the same key as the first message, setting the agent's skill status to DELETED and the Is_Deleted flag to true
        AgentSkill secondMessageWithIsDeletedSetToTrue = TestDataGenerator.getStaticAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_DELETED, 3, clusterId, true, initialMessageWithIsDeletedSetToFalse.getAgentNo(), initialMessageWithIsDeletedSetToFalse.getSkillNo());
        publisherHelper.publishMessageToAgentSkillTopic(secondMessageWithIsDeletedSetToTrue, key, headerKey, headerValue);

        delay();

        int kafkaAgentNo = secondMessageWithIsDeletedSetToTrue.getAgentNo();
        String kafkaSourceCluster = secondMessageWithIsDeletedSetToTrue.getSourceCluster();
        int kafkaSkillNo = secondMessageWithIsDeletedSetToTrue.getSkillNo();

        String fetchEntryCountWhenIsDeletedSetToTrue = "SELECT COUNT(*) as record_count FROM test_connection.agent_skill_minimal s WHERE agent_no = " + kafkaAgentNo + " AND source_cluster = '" + kafkaSourceCluster + "' AND JSON_CONTAINS(skills, '" + kafkaSkillNo + "', '$');";

        int skillCount = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchTheCountOfDataFromDB(fetchEntryCountWhenIsDeletedSetToTrue, "record_count");
            return databaseUtil.getCountOfData();
        }, result -> result == 0);

        assertEquals(skillCount, 0, "Entry is not deleted from the table");
    }

    /**
     * This test case validates that the entire entry is not deleted from the agent_skill_minimal table in the Dimension DB
     * when the skills array contains at least one skill, even after the agent's skill status is set to DELETED and
     * the isDeleted flag is set to true for the given key in the table
     */
//    @Test(priority = 10)
    public void validateEntryIsNotDeleted() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate that the isDelete flag is true is handles when at least one skill is present in array");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message with the agent's skill status set to ACTIVE and the isDeleted flag set to false
        AgentSkill initialMessage = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + initialMessage.getSkillNo();
        publisherHelper.publishMessageToAgentSkillTopic(initialMessage, key, headerKey, headerValue);

//        Publish a second message with the agent's skill status set to ACTIVE and the isDeleted flag set to false, using the same key and agent number, but with a different skill
        int tempSkillNo = Integer.parseInt(agent.getSkills().get(1).getSkillId());
        String tempKey = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + tempSkillNo;
        AgentSkill secondMessageWithDiffSkill = TestDataGenerator.getStaticAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false, initialMessage.getAgentNo(), tempSkillNo);
        publisherHelper.publishMessageToAgentSkillTopic(secondMessageWithDiffSkill, tempKey, headerKey, headerValue);

//        Publish a third message with the same key as the initial message, with the agent's skill status to DELETED and the Is_Deleted flag to true
        AgentSkill thirdMessageWithIsDeletedSetToTrue = TestDataGenerator.getStaticAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_DELETED, 3, clusterId, true, initialMessage.getAgentNo(), initialMessage.getSkillNo());
        publisherHelper.publishMessageToAgentSkillTopic(thirdMessageWithIsDeletedSetToTrue, key, headerKey, headerValue);

        delay();

        int kafkaAgentNo = thirdMessageWithIsDeletedSetToTrue.getAgentNo();
        String kafkaSourceCluster = thirdMessageWithIsDeletedSetToTrue.getSourceCluster();
        int kafkaBusNo = thirdMessageWithIsDeletedSetToTrue.getBusNo();

        String fetchEntryCount = "SELECT COUNT(*) as record_count FROM test_connection.agent_skill_minimal s WHERE source_cluster = '" + kafkaSourceCluster + "' AND bus_no = " + kafkaBusNo + " AND agent_no = " + kafkaAgentNo + ";";

        int count = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchTheCountOfDataFromDB(fetchEntryCount, "record_count");
            return databaseUtil.getCountOfData();
        });

        assertEquals(count, 1, "Entire entry has been removed from the table");
    }

    /**
     * This test case validates that the message is skipped when any one of source_cluster, agent_no, or skill is missing from the key
     */
//    @Test(priority = 11)
    public void validateSkipOperation() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate the behaviour for null values");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message to create a record in the DB with No Agent, Skill number
        AgentSkill initialMessage = TestDataGenerator.getStaticAgentSkillDataWithNoAgentAndSkillNo(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + initialMessage.getSkillNo();
//        RecordMetadata metadata = publisherHelper.publishMessageToAgentSkillTopic(initialMessage, key, headerKey, headerValue);

        delay();

//        int kafkaAgentNo = initialMessage.getAgentNo();
//        String kafkaSourceCluster = initialMessage.getSourceCluster();
//        int kafkaBusNo = initialMessage.getBusNo();
//
//        String fetchEntryCount = "SELECT COUNT(*) as record_count FROM test_connection.agent_skill_minimal s WHERE source_cluster = '" + kafkaSourceCluster + "' AND bus_no = " + kafkaBusNo + " AND agent_no = 0;";
//
//        int count = TestBaseClass.retryUntil(() -> {
//            databaseUtil.fetchTheCountOfDataFromDB(fetchEntryCount, "record_count");
//            return databaseUtil.getCountOfData();
//        });d
//
//        assertEquals(count, 1, "Entire entry has been removed from the table");

        System.out.println(initialMessage);

    }


    /**
     * This test case validates that the skill is removed when a null message is received for the same key
     */
//    @Test(priority = 12)
    public void validateDeleteOperation() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate the deletion operation when a null message is received");

        Tenant tenant = new Tenant(BUID, TENANT_GUID);
        Agent agent = Agent.buildAgentData(tenant, MediaType.CALL, 1, 10);

//        Publish an initial message to create a record in the DB
        AgentSkill initialMessage = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        key = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + initialMessage.getSkillNo();
        publisherHelper.publishMessageToAgentSkillTopic(initialMessage, key, headerKey, headerValue);

        delay();
//        Publish a second message with the agent's skill status set to ACTIVE and the isDeleted flag set to false, using the same key and agent number as initial message, but with a different skill number
        int tempSkillNo = Integer.parseInt(agent.getSkills().get(1).getSkillId());
        String tempKey = initialMessage.getSourceCluster() + "_" + initialMessage.getAgentNo() + "_" + tempSkillNo;
        AgentSkill secondMessageWithDiffSkill = TestDataGenerator.getStaticAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false, initialMessage.getAgentNo(), tempSkillNo);
        publisherHelper.publishMessageToAgentSkillTopic(secondMessageWithDiffSkill, tempKey, headerKey, headerValue);

        delay();
//        Publish a third null message with the same key as in initial message
        AgentSkill nullMessage = TestDataGenerator.getAgentSkillData(tenant, agent, AGENT_SKILL_STATUS_ACTIVE, 3, clusterId, false);
        publisherHelper.publishEmptyMessageToAgentSkillTopic(key, headerKey, headerValue);

        delay();

        int kafkaSkillNo = initialMessage.getSkillNo();
        int kafkaAgentNo = initialMessage.getAgentNo();
        String kafkaSourceCluster = initialMessage.getSourceCluster();
        int kafkaBusNo = initialMessage.getBusNo();

        String fetchTheEntryBySkill = "SELECT COUNT(*) as record_count FROM test_connection.agent_skill_minimal s WHERE source_cluster = '" + kafkaSourceCluster + "' AND agent_no = " + kafkaAgentNo + " AND JSON_CONTAINS(skills, '" + kafkaSkillNo + "', '$');";

        int skillCount = TestBaseClass.retryUntil(() -> {
            databaseUtil.fetchTheCountOfDataFromDB(fetchTheEntryBySkill, "record_count");
            return databaseUtil.getCountOfData();
        }, result -> result == 0);

        assertEquals(skillCount, 0, "Skill number is not removed from the table");
    }

    /**
     * This test case validates that the Aurora DB Connection
     */
    @Test(priority = 13)
    public void validateAuroraDbConnection() throws InterruptedException, ExecutionException {
        Log.info("Started Test Case: Validate the DB connection (Aurora DB)");

//        String fetchTheCountOfRecord = "SELECT COUNT(*) as record_count FROM agent_skill_minimal s;";
//
//        int count = TestBaseClass.retryUntil(() -> {
//            databaseUtil.fetchTheCountOfDataFromDB(fetchTheCountOfRecord, "record_count");
//            return databaseUtil.getCountOfData();
//        });
//
//        System.out.println("Count of all Records-> " + count);

//        assertTrue(count >= 0, "Unable to connect to Aurora DB or count is less than zero");

        System.out.println("Test Case is passed");
    }

}

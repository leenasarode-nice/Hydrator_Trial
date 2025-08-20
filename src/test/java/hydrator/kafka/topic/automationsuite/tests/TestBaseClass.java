package hydrator.kafka.topic.automationsuite.tests;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import hydrator.kafka.topic.automationsuite.setup.DatabaseUtil;
import hydrator.kafka.topic.automationsuite.setup.TestConstant;
import hydrator.kafka.topic.automationsuite.setup.ValkeyUtil;
import hydrator.kafka.topic.automationsuite.setup.kafkaPublisher.PublisherHelper;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import hydrator.kafka.topic.automationsuite.setup.kafkahelper.*;
import hydrator.kafka.topic.automationsuite.setup.kafkalisteners.*;
import hydrator.kafka.topic.automationsuite.setup.reports.*;
import redis.clients.jedis.Jedis;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.RdsUtilities;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class TestBaseClass {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    protected ConsumerHelper consumerHelper;
    protected Properties prop;
    protected String clusterId;
    protected String schemaRegistryUrl;
    protected AgentSkillAssociationListener agentSkillAssociationListener;
    protected DatabaseUtil databaseUtil;
    protected ValkeyUtil valkeyUtil;
    protected String url;
    protected String username;
    protected String password;
    protected Connection connection;
    protected Jedis jedis;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(2);
    protected String BUID;
    protected String TENANT_GUID;
    protected PublisherHelper publisherHelper;
    protected final String headerKey = "_AF";
    protected final String headerValue = "DummyData";
    protected String key;

    @BeforeSuite
    public void beforeSuite() {
        ExtentManager.setExtent();
    }

    @AfterSuite
    public void afterSuite() {
        executorService.shutdown();
        ExtentManager.endReport();
    }

    @BeforeClass
    @Parameters({"KAFKA_BOOTSTRAP_SERVERS", "KAFKA_SSL", "ENV", "CLUSTER_ID"})
    public void init(String kafkaBootstrapServer, boolean kafkaSSL, String env, String clustId)
            throws Exception {
        setBUID_TenantGUID();
        prop = new Properties();
        try (InputStream inputStream = TestBaseClass.class.getResourceAsStream("/config.properties")) {
            prop.load(inputStream);
            schemaRegistryUrl = prop.getProperty("schema.registry.url");
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while reading config.properties file: ", e);
        }

        clusterId = clustId;

//        consumerHelper = new ConsumerHelper(kafkaBootstrapServer, kafkaSSL, prop);
//        publisherHelper = new PublisherHelper(prop, kafkaBootstrapServer, schemaRegistryUrl);
        executorService.submit(() -> {
            try {
                consumerHelper.consumeAgentSkillAssociation(prop);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        delay();

//        agentSkillAssociationListener = consumerHelper.getAgentSkillAssociationListener();

//        setupDbConnection();
        setupAuroraDbConnection();
//        setupValkeyConnection();
    }

//    private void setupDbConnection() {
//        url = prop.getProperty("url");
//        username = prop.getProperty("username");
//        password = prop.getProperty("password");
//
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            connection = DriverManager.getConnection(url, username, password);
//            log.info("DB connection established");
//        } catch (ClassNotFoundException | SQLException e) {
//            log.warn("Error initializing DB connection");
//            e.printStackTrace();
//        }
//
//        databaseUtil = new DatabaseUtil(connection);
//    }

    private void setupValkeyConnection() {
        try {
            String host = prop.getProperty("valkey.host", "localhost");
            int port = Integer.parseInt(prop.getProperty("valkey.port", "6379"));
            boolean ssl = Boolean.parseBoolean(prop.getProperty("valkey.ssl", "false"));

            jedis = new Jedis(host, port, ssl);
            log.info("Valkey (Redis) connection established.");
        } catch (Exception e) {
            throw new RuntimeException("Error setting up Valkey", e);
        }

        valkeyUtil = new ValkeyUtil(jedis);
    }

    protected void delay() throws InterruptedException {
        TimeUnit.SECONDS.sleep(TestConstant.PROCESSING_INTERVAL_IN_SECONDS);
    }

    protected void longDelay() throws InterruptedException {
        TimeUnit.SECONDS.sleep(TestConstant.PROCESSING_INTERVAL_IN_SECONDS * 3);
    }

    /**
     * 1. Core retry method that accepts a custom condition.
     * Keeps calling the given supplier until the provided condition is met or the timeout expires.
     * After each execution, the result is checked against the given condition;
     * if it fails, the method waits for a short interval and retries.
     *
     * @param supplier  the task to execute repeatedly
     * @param condition the condition to check for success
     * @param <T>       the type of result returned by the supplier
     * @return the result that satisfies the given condition
     * @throws RuntimeException if the timeout is reached before the condition is met
     */

    public static <T> T retryUntil(Supplier<T> supplier, Predicate<T> condition) {
        Instant endTime = Instant.now().plus(DEFAULT_TIMEOUT);
        int attempt = 1;

        while (Instant.now().isBefore(endTime)) {
            T result = supplier.get();
            if (condition.test(result)) {
                return result;
            }

            Log.info("No valid result on attempt " + attempt + ". Retrying...");
            try {
                Thread.sleep(DEFAULT_INTERVAL.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Retry interrupted", e);
            }

            attempt++;
        }

        throw new RuntimeException("Retry timeout exceeded after " + DEFAULT_TIMEOUT.getSeconds() + " seconds");
    }

    /**
     * 2. Overloaded version with default conditions for common types
     * Keeps calling the given supplier until a default success condition is met or the timeout expires.
     * The default conditions are:
     * - Non-empty collection
     * - Non-empty map
     * - Non-zero number
     * - Non-null object
     *
     * @param supplier the task to execute repeatedly
     * @param <T>      the type of result returned by the supplier
     * @return the result that satisfies the condition
     * @throws RuntimeException if the timeout is reached before the condition is met
     */
    public static <T> T retryUntil(Supplier<T> supplier) {
        return retryUntil(supplier, result -> {
            if (result instanceof Collection) {
                return !((Collection<?>) result).isEmpty();
            } else if (result instanceof Map) {
                return !((Map<?, ?>) result).isEmpty();
            } else if (result instanceof Number) {
                return ((Number) result).longValue() != 0;
            } else {
                return result != null;
            }
        });
    }

    /**
     * @param seconds the number of seconds from the Kafka message
     * @param nanos   nanos the number of nanoseconds from the Kafka message
     * @return the corresponding time in milliseconds since the epoch
     */
    public static long convertToMillis(long seconds, int nanos) {
        Instant instant = Instant.ofEpochSecond(seconds, nanos);
        return instant.toEpochMilli();
    }

    /**
     * Sets the BUID and TENANT\_GUID properties.
     *
     * <p>This method first checks if the environment variables "BUID" and "TENANT\_GUID" are set.
     * If they are, it assigns their values to the BUID and TENANT\_GUID fields respectively.
     * If the environment variables are not set, it reads the values from the localConfig.properties file.
     *
     * @throws RuntimeException if an error occurs while reading the localConfig.properties file.
     */
    private void setBUID_TenantGUID() {
        if (System.getenv("BUID") != null && System.getenv("TENANT_GUID") != null) {
            BUID = System.getenv("BUID");
            TENANT_GUID = System.getenv("TENANT_GUID");
        } else {
            Properties localProp = new Properties();
            try (InputStream inputStream = TestBaseClass.class.getResourceAsStream("/localConfig.properties")) {
                localProp.load(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Error occurred while reading localConfig.properties file: ", e);
            }
            BUID = localProp.getProperty("BUID");
            TENANT_GUID = localProp.getProperty("TENANT_GUID");
        }
    }

    @AfterClass
    public void tearDown() throws SQLException {
//        consumerHelper.clearConsumerMaps();
        connection.close();
//        jedis.close();
        log.info("All Connections are closed.");
    }


    private String getSecret(String secretName, String region) throws Exception {
        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(region))
                .build()) {

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = client.getSecretValue(request);
            return response.secretString();
        }
    }

    private void setupAuroraDbConnection() throws Exception {
        String jdbcUrl = prop.getProperty("mysql.read.url");
        String secretId = prop.getProperty("mysql.secrets.manager.secret.id");
        String region = prop.getProperty("aws.region");

        String secretJson = getSecret(secretId, region);

        log.info("Fetched secret JSON: {}", secretJson);

        Map<String, String> secretMap = new ObjectMapper().readValue(secretJson, Map.class);

        String username = secretMap.get("username");
        String password = secretMap.get("password");

        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(jdbcUrl, username, password);

        databaseUtil = new DatabaseUtil(connection);

        log.info("Aurora DB connection established successfully.");
    }

//    public Connection getRdsConnection() throws Exception {
//        String host = prop.getProperty("rds.hostname");
//        String port = prop.getProperty("mysql.db.port");
//        String dbName = prop.getProperty("mysql.db.name");
//        String username = prop.getProperty("mysql.username");
//        String secretId = prop.getProperty("mysql.secrets.manager.secret.id");
//        String region = prop.getProperty("aws.region");
//
//        String secretJson = getSecret(secretId, region);
//        Map<String, String> secretMap = new ObjectMapper().readValue(secretJson, Map.class);
//        String password = secretMap.get("password");
//
//        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
//                + "?useSSL=true&requireSSL=false";
//
//        Class.forName("com.mysql.cj.jdbc.Driver");
//        return DriverManager.getConnection(jdbcUrl, username, password);
//    }


//    private void setupAuroraDbConnection() throws Exception {
//        String host = prop.getProperty("rds.hostname");
//        String port = prop.getProperty("mysql.db.port");
//        String dbName = prop.getProperty("mysql.db.name");
//        String username = prop.getProperty("mysql.username");
//        String secretId = prop.getProperty("mysql.secrets.manager.secret.id");
//        String region = prop.getProperty("aws.region");
//
//        String secretJson = getSecret(secretId, region);
//        Map<String, String> secretMap = new ObjectMapper().readValue(secretJson, Map.class);
//        String password = secretMap.get("password");
//
//        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
//                + "?useSSL=true&requireSSL=false";
//
//        Class.forName("com.mysql.cj.jdbc.Driver");
//        connection = DriverManager.getConnection(jdbcUrl, username, password);
//
//        databaseUtil = new DatabaseUtil(connection);
//
//        log.info("Aurora DB connection + DatabaseUtil initialized.");
//    }


}

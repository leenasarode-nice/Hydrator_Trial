package hydrator.kafka.topic.automationsuite.setup;

public class TestConstant {

  public static final long PROCESSING_INTERVAL_IN_SECONDS = 10;
  public static final String DEFAULT_REGION = "us-west-2";
  public static final String MATCH_REQUEST_STREAM = "-findmatch-matchrequests";
  public static final String SQS_MATCHES_QUEUE = "-Matches";
  public static final String SKILL_EXT_SETTING_OB_STRATEGY = "OutboundStrategy";
  public static final String SKILL_EXT_SETTING_EVALUATION_CRITERIA = "EvaluationCriteria";
  public static final long MATCH_WAIT_TIMEOUT_IN_SECONDS = 300;
  public static final int MAX_WAIT_TIME_FOR_SQS_IN_SECONDS = 20;

  public static final String AGENT_SKILL_STATUS_ACTIVE = "AGENT_SKILL_STATUS_ACTIVE";
  public static final String AGENT_SKILL_STATUS_DELETED = "AGENT_SKILL_STATUS_DELETED";
}

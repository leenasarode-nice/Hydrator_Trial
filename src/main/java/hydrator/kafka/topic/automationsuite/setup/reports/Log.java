package hydrator.kafka.topic.automationsuite.setup.reports;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hydrator.kafka.topic.automationsuite.setup.reports.ExtentManager.test;

public class Log {
    private final static Logger log = LoggerFactory.getLogger(Log.class);

    private static ExtentTest getTest() {
        return ExtentReportListener.getTest();
    }

    public static void info(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.INFO, message);
        }
        log.info(message);
    }

    public static void error(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.ERROR, message);
        }
        log.error(message);
    }

    public static void debug(String message) {
        test.log(Status.DEBUG, message);
        log.debug(message);
    }

    public static void pass(String message) {
        ExtentTest test = getTest();
        if (test != null) {
            test.log(Status.PASS, message);
        }
        log.debug(message);
    }
}

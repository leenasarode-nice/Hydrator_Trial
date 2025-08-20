package hydrator.kafka.topic.automationsuite.setup.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportListener extends ExtentManager implements ITestListener {

  private static ExtentReports extent = createInstance();
  private static ConcurrentMap<Long, ExtentTest> testMap = new ConcurrentHashMap<>();

  private static ExtentReports createInstance() {
    ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("test-output/ExtentReport/TestExecutionReport.html");
    ExtentReports extent = new ExtentReports();
    extent.attachReporter(htmlReporter);
    return extent;
  }

  public static ExtentTest getTest() {
    return testMap.get(Thread.currentThread().getId());
  }
  public synchronized void onTestStart(ITestResult result) {
    ExtentTest extentTest = extent.createTest(result.getMethod().getMethodName());
    testMap.put(Thread.currentThread().getId(), extentTest);
  }

  public synchronized void onTestSuccess(ITestResult result) {
    if (result.getStatus() == ITestResult.SUCCESS) {
      testMap.get(Thread.currentThread().getId()).pass("Pass Test case is: " + result.getName());
    }
  }

  public synchronized void onTestFailure(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE) {
      testMap.get(Thread.currentThread().getId()).fail(
              MarkupHelper.createLabel(result.getName() + " - Test Case Failed", ExtentColor.RED));
      testMap.get(Thread.currentThread().getId()).fail(
              MarkupHelper.createLabel(result.getThrowable() + " - Test Case Failed", ExtentColor.RED));
    }
  }

  public synchronized void onTestSkipped(ITestResult result) {
    if (result.getStatus() == ITestResult.SKIP) {
      testMap.get(Thread.currentThread().getId()).skip("Skipped Test case is: " + result.getName());
    }
  }

  public synchronized void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    testMap.get(Thread.currentThread().getId()).info("Test case failed but within success percentage: " + result.getName());
  }

  public synchronized void onStart(ITestContext context) {
    // Code to handle onStart
  }

  public synchronized void onFinish(ITestContext context) {
    extent.flush();
    try {
      Map<String, Object> testResult = new HashMap<>();
      testResult.put("TotalTestCaseCount", context.getAllTestMethods().length);
      testResult.put("PassedTestCaseCount", context.getPassedTests().size());
      testResult.put("FailedTestCaseCount", context.getFailedTests().size());
      testResult.put("SkippedTestCaseCount", context.getSkippedTests().size());

      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      String filePath = "test-output/ExtentReport/TestExecutionReport.json";
      mapper.writeValue(new File(filePath), testResult);
    } catch (IOException e) {
      throw new RuntimeException("Error occurred while writing to TestExecutionReport.json file: ", e);
    }
  }
}
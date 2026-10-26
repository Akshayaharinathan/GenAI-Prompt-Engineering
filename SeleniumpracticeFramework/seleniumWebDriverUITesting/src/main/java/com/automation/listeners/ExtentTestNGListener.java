package com.automation.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.automation.base.DriverFactory;
import com.automation.utils.ExtentReportManager;
import com.automation.utils.PdfReportGenerator;
import com.automation.utils.ScreenshotUtils;
import com.automation.utils.TestResultsCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TestNG listener that:
 *  - Starts/stops an ExtentTest per test method (HTML report)
 *  - Captures screenshots on failure and embeds them in the report
 *  - Feeds a thread-safe collector used to build the PDF execution report at suite end
 */
public class ExtentTestNGListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(ExtentTestNGListener.class);
    private final ThreadLocal<Long> startTimeMillis = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        String browser = extractBrowserParam(result);
        String testName = result.getMethod().getMethodName() + " [" + browser.toUpperCase() + "]";
        ExtentReportManager.startTest(testName, browser);
        startTimeMillis.set(System.currentTimeMillis());
        logger.info("Test started: {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.PASS, "Test passed successfully.");
        }
        recordResult(result, "PASS", null);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        String screenshotPath = null;
        try {
            WebDriver driver = DriverFactory.getDriver();
            if (driver != null) {
                screenshotPath = ScreenshotUtils.capture(driver, result.getMethod().getMethodName() + "_FAILURE");
            }
        } catch (Exception e) {
            logger.error("Could not capture failure screenshot", e);
        }

        if (test != null) {
            test.log(Status.FAIL, "Test failed: " + result.getThrowable());
            if (screenshotPath != null) {
                try {
                    test.addScreenCaptureFromPath(screenshotPath);
                } catch (Exception e) {
                    logger.warn("Could not attach failure screenshot to report", e);
                }
            }
        }
        recordResult(result, "FAIL", screenshotPath);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.SKIP, "Test skipped: " + result.getThrowable());
        }
        recordResult(result, "SKIP", null);
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
    }

    private void recordResult(ITestResult result, String status, String screenshotPath) {
        String browser = extractBrowserParam(result);
        long start = startTimeMillis.get() != null ? startTimeMillis.get() : System.currentTimeMillis();
        long duration = System.currentTimeMillis() - start;
        String startTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        TestResultsCollector.addResult(new PdfReportGenerator.ResultRow(
                result.getMethod().getMethodName(),
                browser,
                status,
                startTimeStr,
                String.valueOf(duration),
                screenshotPath
        ));
    }

    private String extractBrowserParam(ITestResult result) {
        Object[] params = result.getParameters();
        // browser comes from @Parameters injected into BaseTest.setUp, not into the test method itself,
        // so fall back to the test context name if not directly available.
        try {
            String ctxName = result.getTestContext().getCurrentXmlTest().getLocalParameters().get("browser");
            if (ctxName != null) return ctxName;
        } catch (Exception ignored) {
        }
        return "unknown";
    }
}

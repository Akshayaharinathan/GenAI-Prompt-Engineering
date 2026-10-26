package com.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Singleton manager for ExtentReports HTML reporting.
 * Thread-safe ExtentTest map keyed by test (thread) name.
 */
public class ExtentReportManager {

    private static final Logger logger = LogManager.getLogger(ExtentReportManager.class);
    private static ExtentReports extent;
    private static String reportPath;
    private static final Map<Long, ExtentTest> testMap = new ConcurrentHashMap<>();

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            extent = createInstance();
        }
        return extent;
    }

    private static ExtentReports createInstance() {
        String reportDir = ConfigReader.getInstance().get("extentReportDir", "test-output/ExtentReport");
        File dir = new File(reportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        reportPath = reportDir + File.separator + "ExtentReport_" + timestamp + ".html";

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Test Automation Practice - Execution Report");
        sparkReporter.config().setReportName("Selenium + TestNG Automation Results (Microsoft Edge)");

        ExtentReports extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Application URL", "https://testautomationpractice.blogspot.com/");
        extentReports.setSystemInfo("Framework", "Selenium + TestNG + Maven (POM Design)");
        extentReports.setSystemInfo("OS", System.getProperty("os.name"));
        extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));

        logger.info("ExtentReports HTML report initialized at {}", reportPath);
        return extentReports;
    }

    public static String getReportPath() {
        return reportPath;
    }

    public static void startTest(String testName, String browser) {
        ExtentTest test = getInstance().createTest(testName).assignCategory(browser);
        testMap.put(Thread.currentThread().getId(), test);
    }

    public static ExtentTest getTest() {
        return testMap.get(Thread.currentThread().getId());
    }

    public static void flush() {
        if (extent != null) {
            extent.flush();
            logger.info("ExtentReports HTML report flushed to {}", reportPath);
        }
    }
}

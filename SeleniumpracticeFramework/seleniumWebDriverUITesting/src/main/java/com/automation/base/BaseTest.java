package com.automation.base;

import com.automation.utils.ConfigReader;
import com.automation.utils.ExtentReportManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.lang.reflect.Method;

/**
 * Common base class for all TestNG test classes.
 * Handles WebDriver lifecycle (per-method, thread-safe) and exposes
 * the current browser name + base URL for use in test/page classes.
 */
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected static final ConfigReader config = ConfigReader.getInstance();

    protected WebDriver driver;
    protected String browser;

    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("edge") String browserParam, Method method) {
        this.browser = browserParam;
        ExtentReportManager.startTest(method.getName(), browser);
        logger.info("==== Initializing {} browser session ====", browser);
        driver = DriverFactory.initDriver(browser);
        driver.get(config.get("baseURL"));
        logger.info("Navigated to base URL: {}", config.get("baseURL"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        logger.info("==== Closing {} browser session for test [{}] - Status: {} ====",
                browser, result.getMethod().getMethodName(), statusName(result.getStatus()));
        if (driver != null) {
            DriverFactory.quitDriver();
        }
        ExtentReportManager.flush();
    }

    private String statusName(int status) {
        switch (status) {
            case ITestResult.SUCCESS: return "PASS";
            case ITestResult.FAILURE: return "FAIL";
            case ITestResult.SKIP: return "SKIP";
            default: return "UNKNOWN";
        }
    }
}

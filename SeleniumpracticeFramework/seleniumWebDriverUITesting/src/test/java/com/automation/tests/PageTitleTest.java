package com.automation.tests;

import com.aventstack.extentreports.Status;
import com.automation.base.BaseTest;
import com.automation.pages.HomePage;
import com.automation.utils.ExcelUtils;
import com.automation.utils.ExtentReportManager;
import com.automation.utils.ScreenshotUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Test 1: Validates the Home page title.
 * Data driven from the "HomePageTitleData" sheet in TestData.xlsx.
 */
public class PageTitleTest extends BaseTest {

    @DataProvider(name = "titleData")
    public Object[][] titleDataProvider() {
        ExcelUtils.createDefaultTestDataFileIfMissing(config.get("testDataExcelPath"));
        List<Map<String, String>> rows = ExcelUtils.getSheetData(config.get("testDataExcelPath"), "HomePageTitleData");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    @Test(dataProvider = "titleData", description = "Validate the Home page title matches the expected value")
    public void validateHomePageTitle(Map<String, String> rowData) {
        String expectedTitle = rowData.get("ExpectedTitle");
        String testCaseId = rowData.get("TestCaseID");

        ExtentReportManager.getTest().log(Status.INFO,
                "[" + testCaseId + "] Validating home page title. Expected: '" + expectedTitle + "'");

        HomePage homePage = new HomePage(driver);
        String actualTitle = homePage.getPageTitle();

        String screenshotPath = ScreenshotUtils.capture(driver, "HomePageTitle_" + browser);
        if (screenshotPath != null) {
            ExtentReportManager.getTest().addScreenCaptureFromPath(screenshotPath);
        }

        ExtentReportManager.getTest().log(Status.INFO, "Actual title found: '" + actualTitle + "'");
        Assert.assertEquals(actualTitle, expectedTitle,
                "Home page title did not match the expected value for " + testCaseId);

        ExtentReportManager.getTest().log(Status.PASS, "Home page title validated successfully on " + browser);
    }
}

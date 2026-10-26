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
import java.util.Set;

/**
 * Test 2: Validates Alerts (Simple/Confirmation/Prompt) and Popup
 * (New Tab / Popup Window) functionalities on the Home page.
 * Alert scenarios are data driven from the "AlertsPopupData" sheet in TestData.xlsx.
 */
public class AlertsPopupsTest extends BaseTest {

    @DataProvider(name = "alertData")
    public Object[][] alertDataProvider() {
        ExcelUtils.createDefaultTestDataFileIfMissing(config.get("testDataExcelPath"));
        List<Map<String, String>> rows = ExcelUtils.getSheetData(config.get("testDataExcelPath"), "AlertsPopupData");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    @Test(dataProvider = "alertData", description = "Validate Simple, Confirmation and Prompt JS alerts")
    public void validateAlertsFunctionality(Map<String, String> rowData) {
        String testCaseId = rowData.get("TestCaseID");
        String alertType = rowData.get("AlertType");
        String expectedAlertText = rowData.get("ExpectedAlertText");
        String promptInput = rowData.get("PromptInputText");

        ExtentReportManager.getTest().log(Status.INFO, "[" + testCaseId + "] Validating alert type: " + alertType);

        HomePage homePage = new HomePage(driver);

        switch (alertType.trim().toUpperCase()) {

            case "SIMPLEALERT":
                homePage.clickSimpleAlertButton();
                String simpleAlertText = homePage.waitForAlertAndGetText();
                Assert.assertEquals(simpleAlertText, expectedAlertText, "Simple alert text mismatch");
                homePage.acceptAlert();
                captureScreenshot("SimpleAlert_" + browser);
                ExtentReportManager.getTest().log(Status.PASS, "Simple alert validated and accepted: " + simpleAlertText);
                break;

            case "CONFIRMALERT":
                homePage.clickConfirmAlertButton();
                String confirmAlertText = homePage.waitForAlertAndGetText();
                Assert.assertEquals(confirmAlertText, expectedAlertText, "Confirmation alert text mismatch");
                homePage.acceptAlert();
                captureScreenshot("ConfirmationAlert_" + browser);
                ExtentReportManager.getTest().log(Status.PASS, "Confirmation alert validated and accepted: " + confirmAlertText);
                break;

            case "PROMPTALERT":
                homePage.clickPromptAlertButton();
                String promptAlertText = homePage.waitForAlertAndGetText();
                Assert.assertEquals(promptAlertText, expectedAlertText, "Prompt alert text mismatch");
                homePage.typeIntoPromptAndAccept(promptInput);
                captureScreenshot("PromptAlert_AfterInput_" + browser);
                ExtentReportManager.getTest().log(Status.PASS,
                        "Prompt alert validated; entered '" + promptInput + "' and accepted");
                break;

            default:
                Assert.fail("Unknown AlertType in test data: " + alertType);
        }
    }

    @Test(description = "Validate 'Open new Tab' popup functionality")
    public void validateOpenNewTabPopup() {
        HomePage homePage = new HomePage(driver);
        String originalWindow = homePage.getCurrentWindowHandle();

        if (!homePage.isOpenNewTabButtonPresent()) {
            ExtentReportManager.getTest().log(Status.SKIP, "'Open new Tab' control not present on page; skipping.");
            throw new org.testng.SkipException("'Open new Tab' button not found on page");
        }

        homePage.clickOpenNewTabButton();

        Set<String> windows = homePage.getAllWindowHandles();
        Assert.assertEquals(windows.size(), 2, "Expected a new tab/window to open");

        for (String handle : windows) {
            if (!handle.equals(originalWindow)) {
                homePage.switchToWindow(handle);
                break;
            }
        }

        captureScreenshot("NewTabOpened_" + browser);
        ExtentReportManager.getTest().log(Status.PASS, "New tab opened successfully. URL: " + homePage.getCurrentUrl());

        homePage.closeCurrentWindowAndSwitchBack(originalWindow);
        Assert.assertEquals(homePage.getCurrentWindowHandle(), originalWindow, "Did not switch back to original window");
    }

    @Test(description = "Validate 'Popup Window' functionality")
    public void validatePopupWindow() {
        HomePage homePage = new HomePage(driver);
        String originalWindow = homePage.getCurrentWindowHandle();

        if (!homePage.isOpenPopupWindowButtonPresent()) {
            ExtentReportManager.getTest().log(Status.SKIP, "'Popup Window' control not present on page; skipping.");
            throw new org.testng.SkipException("'Open Window' popup button not found on page");
        }

        homePage.clickOpenPopupWindowButton();

        Set<String> windows = homePage.getAllWindowHandles();
        Assert.assertTrue(windows.size() >= 2, "Expected a popup window to open");

        for (String handle : windows) {
            if (!handle.equals(originalWindow)) {
                homePage.switchToWindow(handle);
                break;
            }
        }

        captureScreenshot("PopupWindowOpened_" + browser);
        ExtentReportManager.getTest().log(Status.PASS, "Popup window opened successfully. URL: " + homePage.getCurrentUrl());

        homePage.closeCurrentWindowAndSwitchBack(originalWindow);
        Assert.assertEquals(homePage.getCurrentWindowHandle(), originalWindow, "Did not switch back to original window");
    }

    private void captureScreenshot(String label) {
        String path = ScreenshotUtils.capture(driver, label);
        if (path != null) {
            ExtentReportManager.getTest().addScreenCaptureFromPath(path);
        }
    }
}

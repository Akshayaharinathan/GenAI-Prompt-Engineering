package com.automation.tests;

import com.aventstack.extentreports.Status;
import com.automation.base.BaseTest;
import com.automation.pages.DownloadFilesPage;
import com.automation.pages.HomePage;
import com.automation.utils.ExcelUtils;
import com.automation.utils.ExtentReportManager;
import com.automation.utils.FileValidationUtils;
import com.automation.utils.ScreenshotUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Test 3: Validates navigation to the "Download Files" page via the footer link,
 * generation of TXT/PDF files, and successful download + content validation.
 * Data driven from the "DownloadFilesData" sheet in TestData.xlsx.
 */
public class DownloadFilesTest extends BaseTest {

    private static final String EXPECTED_DOWNLOAD_URL =
            "https://testautomationpractice.blogspot.com/p/download-files_25.html";

    @DataProvider(name = "downloadData")
    public Object[][] downloadDataProvider() {
        ExcelUtils.createDefaultTestDataFileIfMissing(config.get("testDataExcelPath"));
        List<Map<String, String>> rows = ExcelUtils.getSheetData(config.get("testDataExcelPath"), "DownloadFilesData");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    @Test(dataProvider = "downloadData",
          description = "Navigate via footer link, generate & download TXT/PDF file, validate generation & download")
    public void validateGenerateAndDownloadFile(Map<String, String> rowData) throws IOException, InterruptedException {

        String testCaseId = rowData.get("TestCaseID");
        String fileType = rowData.get("FileType").trim().toUpperCase();
        String inputText = rowData.get("InputText");
        String expectedExtension = rowData.get("ExpectedFileExtension").trim().toLowerCase();
        String downloadDir = config.get("downloadDir");

        ExtentReportManager.getTest().log(Status.INFO,
                "[" + testCaseId + "] Validating " + fileType + " file generation & download on " + browser);

        // Step 1: Navigate to Download Files page via footer link
        HomePage homePage = new HomePage(driver);
        DownloadFilesPage downloadFilesPage = homePage.navigateToDownloadFilesPage();

        Assert.assertEquals(downloadFilesPage.getCurrentUrl(), EXPECTED_DOWNLOAD_URL,
                "Did not land on the expected Download Files URL");
        ExtentReportManager.getTest().log(Status.PASS,
                "Navigated to Download Files page: " + downloadFilesPage.getCurrentUrl());

        // Step 2: Clean download directory of this extension for a deterministic check
        FileValidationUtils.cleanDownloadDir(downloadDir, expectedExtension);

        // Step 3: Enter text and generate the requested file type
        downloadFilesPage.enterText(inputText);

        if (fileType.equals("TXT")) {
            downloadFilesPage.clickGenerateTextFile();

            boolean linkVisible = downloadFilesPage.isDownloadTextFileLinkDisplayed();
            String genShot = ScreenshotUtils.capture(driver, "TxtFileGenerated_" + browser);
            if (genShot != null) ExtentReportManager.getTest().addScreenCaptureFromPath(genShot);

            Assert.assertTrue(linkVisible, "'Download Text File' link/button did not appear after generation");
            ExtentReportManager.getTest().log(Status.PASS, "Text file generated successfully - download control visible");

            // Step 4: Download
            downloadFilesPage.clickDownloadTextFileLink();

        } else if (fileType.equals("PDF")) {
            downloadFilesPage.clickGeneratePdfFile();

            boolean linkVisible = downloadFilesPage.isDownloadPdfFileLinkDisplayed();
            String genShot = ScreenshotUtils.capture(driver, "PdfFileGenerated_" + browser);
            if (genShot != null) ExtentReportManager.getTest().addScreenCaptureFromPath(genShot);

            Assert.assertTrue(linkVisible, "'Download PDF File' link/button did not appear after generation");
            ExtentReportManager.getTest().log(Status.PASS, "PDF file generated successfully - download control visible");

            // Step 4: Download
            downloadFilesPage.clickDownloadPdfFileLink();

        } else {
            Assert.fail("Unsupported FileType in test data: " + fileType);
        }

        // Step 5: Wait for and validate the downloaded file on disk
        File downloadedFile = FileValidationUtils.waitForDownload(downloadDir, expectedExtension, 20);

        String downloadShot = ScreenshotUtils.capture(driver, fileType + "_FileDownloaded_" + browser);
        if (downloadShot != null) ExtentReportManager.getTest().addScreenCaptureFromPath(downloadShot);

        Assert.assertNotNull(downloadedFile,
                "No ." + expectedExtension + " file was found in download directory: " + downloadDir);
        Assert.assertTrue(FileValidationUtils.isFileValidAndNonEmpty(downloadedFile),
                "Downloaded file is missing or empty: " + downloadedFile);

        ExtentReportManager.getTest().log(Status.PASS,
                "File downloaded successfully: " + downloadedFile.getAbsolutePath()
                        + " (" + downloadedFile.length() + " bytes)");

        // Step 6: Basic content sanity check
        if (fileType.equals("TXT")) {
            String content = FileValidationUtils.readTextFile(downloadedFile);
            ExtentReportManager.getTest().log(Status.INFO, "TXT file content length: " + content.length() + " characters");
            Assert.assertFalse(content.trim().isEmpty(), "Downloaded TXT file content is empty");
        } else {
            String content = FileValidationUtils.readPdfFile(downloadedFile);
            ExtentReportManager.getTest().log(Status.INFO, "PDF file extracted text length: " + content.length() + " characters");
            Assert.assertFalse(content.trim().isEmpty(), "Downloaded PDF file appears to have no extractable content");
        }

        ExtentReportManager.getTest().log(Status.PASS,
                fileType + " file generation and download validated successfully on " + browser);
    }
}

package com.automation.listeners;

import com.automation.utils.ConfigReader;
import com.automation.utils.PdfReportGenerator;
import com.automation.utils.TestResultsCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Fires once when the entire TestNG suite (all <test> tags / browsers) finishes.
 * Builds the consolidated PDF execution report from all collected results,
 * complementing the ExtentReports HTML report.
 */
public class SuiteListener implements ISuiteListener {

    private static final Logger logger = LogManager.getLogger(SuiteListener.class);

    @Override
    public void onFinish(ISuite suite) {
        try {
            String reportDir = ConfigReader.getInstance().get("extentReportDir", "test-output/ExtentReport");
            String pdfPath = PdfReportGenerator.generatePdfReport(TestResultsCollector.getResults(), reportDir);
            logger.info("Suite finished. Consolidated PDF report generated at: {}", pdfPath);
        } catch (Exception e) {
            logger.error("Failed to generate consolidated PDF report at suite finish", e);
        }
    }
}

package com.automation.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates a PDF summary report of the test execution using iText (OpenPDF-compatible API).
 * This complements the ExtentReports HTML report so results are available in both formats,
 * as required by the project.
 */
public class PdfReportGenerator {

    private static final Logger logger = LogManager.getLogger(PdfReportGenerator.class);

    public static class ResultRow {
        public String testName;
        public String browser;
        public String status;
        public String startTime;
        public String durationMs;
        public String screenshotPath;

        public ResultRow(String testName, String browser, String status, String startTime,
                          String durationMs, String screenshotPath) {
            this.testName = testName;
            this.browser = browser;
            this.status = status;
            this.startTime = startTime;
            this.durationMs = durationMs;
            this.screenshotPath = screenshotPath == null ? "-" : screenshotPath;
        }
    }

    /**
     * Builds a PDF execution report summarizing each test's name, browser, status and timing.
     *
     * @param results  list of result rows collected during execution
     * @param reportDir directory in which to place the generated PDF
     * @return absolute path of the generated PDF file
     */
    public static String generatePdfReport(List<ResultRow> results, String reportDir) {
        File dir = new File(reportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String pdfPath = reportDir + File.separator + "ExtentReport_" + timestamp + ".pdf";

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Test Automation Practice - Execution Report (PDF)", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);
            Paragraph generatedOn = new Paragraph(
                    "Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    subFont);
            generatedOn.setAlignment(Element.ALIGN_CENTER);
            generatedOn.setSpacingAfter(15f);
            document.add(generatedOn);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1.5f, 1.5f, 2.5f, 1.5f, 4f});

            String[] headers = {"Test Name", "Browser", "Status", "Start Time", "Duration (ms)", "Screenshot Path"};
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new BaseColor(54, 96, 146));
                cell.setPadding(6f);
                table.addCell(cell);
            }

            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
            for (ResultRow row : results) {
                BaseColor statusColor = "PASS".equalsIgnoreCase(row.status)
                        ? new BaseColor(198, 239, 206)
                        : "SKIP".equalsIgnoreCase(row.status)
                            ? new BaseColor(255, 235, 156)
                            : new BaseColor(255, 199, 206);

                table.addCell(cellOf(row.testName, bodyFont, null));
                table.addCell(cellOf(row.browser, bodyFont, null));
                table.addCell(cellOf(row.status, bodyFont, statusColor));
                table.addCell(cellOf(row.startTime, bodyFont, null));
                table.addCell(cellOf(row.durationMs, bodyFont, null));
                table.addCell(cellOf(row.screenshotPath, bodyFont, null));
            }

            document.add(table);
            document.close();

            logger.info("PDF execution report generated at {}", pdfPath);
        } catch (Exception e) {
            logger.error("Failed to generate PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }

        return pdfPath;
    }

    private static PdfPCell cellOf(String text, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "-" : text, font));
        cell.setPadding(5f);
        if (bg != null) {
            cell.setBackgroundColor(bg);
        }
        return cell;
    }
}

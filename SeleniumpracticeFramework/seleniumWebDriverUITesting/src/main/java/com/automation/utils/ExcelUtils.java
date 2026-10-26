package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to read tabular test data from an Excel (.xlsx) workbook
 * for Data Driven Testing using Apache POI.
 *
 * Each sheet's first row is treated as the header row; each subsequent row
 * is converted into a Map<columnName, cellValueAsString> consumed by TestNG DataProviders.
 */
public class ExcelUtils {

    private static final Logger logger = LogManager.getLogger(ExcelUtils.class);

    /**
     * Reads all data rows of the given sheet into a list of maps (header -> value).
     *
     * @param filePath  path to the .xlsx file
     * @param sheetName name of the sheet to read
     * @return List of row data, each represented as Map<String, String>
     */
    public static List<Map<String, String>> getSheetData(String filePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("Excel test data file not found at path: {}", filePath);
            throw new RuntimeException("Excel file not found: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in " + filePath);
            }

            Row headerRow = sheet.getRow(0);
            int colCount = headerRow.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < colCount; c++) {
                headers.add(getCellValueAsString(headerRow.getCell(c)));
            }

            int totalRows = sheet.getLastRowNum();
            for (int r = 1; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                for (int c = 0; c < colCount; c++) {
                    rowData.put(headers.get(c), getCellValueAsString(row.getCell(c)));
                }
                data.add(rowData);
            }
            logger.info("Read {} data rows from sheet '{}' in file '{}'", data.size(), sheetName, filePath);

        } catch (IOException e) {
            logger.error("Error reading Excel file: {}", filePath, e);
            throw new RuntimeException("Error reading Excel file: " + filePath, e);
        }

        return data;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * Creates the TestData.xlsx workbook used by the data driven tests, in case
     * it does not already exist. This keeps the project runnable out-of-the-box.
     */
    public static void createDefaultTestDataFileIfMissing(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return;
        }
        try {
            file.getParentFile().mkdirs();
            try (Workbook workbook = new XSSFWorkbook()) {

                // Sheet 1: HomePageTitleData
                Sheet titleSheet = workbook.createSheet("HomePageTitleData");
                String[] titleHeaders = {"TestCaseID", "URL", "ExpectedTitle"};
                createRow(titleSheet, 0, titleHeaders);
                createRow(titleSheet, 1, new String[]{"TC01", "https://testautomationpractice.blogspot.com/", "Automation Testing Practice"});

                // Sheet 2: AlertsPopupData
                Sheet alertSheet = workbook.createSheet("AlertsPopupData");
                String[] alertHeaders = {"TestCaseID", "AlertType", "ExpectedAlertText", "PromptInputText"};
                createRow(alertSheet, 0, alertHeaders);
                createRow(alertSheet, 1, new String[]{"TC02", "SimpleAlert", "I am an alert box!", ""});
                createRow(alertSheet, 2, new String[]{"TC03", "ConfirmAlert", "Press a button!", ""});
                createRow(alertSheet, 3, new String[]{"TC04", "PromptAlert", "Please enter your name:", "Selenium QA"});

                // Sheet 3: DownloadFilesData
                Sheet downloadSheet = workbook.createSheet("DownloadFilesData");
                String[] downloadHeaders = {"TestCaseID", "FileType", "InputText", "ExpectedFileExtension"};
                createRow(downloadSheet, 0, downloadHeaders);
                createRow(downloadSheet, 1, new String[]{"TC05", "TXT", "Selenium Practice Automation - Text File Content", "txt"});
                createRow(downloadSheet, 2, new String[]{"TC06", "PDF", "Selenium Practice Automation - PDF File Content", "pdf"});

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                logger.info("Default TestData.xlsx created at {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to create default test data Excel file", e);
            throw new RuntimeException("Failed to create default test data Excel file", e);
        }
    }

    private static void createRow(Sheet sheet, int rowIndex, String[] values) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }
}

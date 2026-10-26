package com.automation.utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class responsible for capturing screenshots at important
 * checkpoints (popups, alerts, file generation, file download, failures, etc).
 */
public class ScreenshotUtils {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = ConfigReader.getInstance().get("screenshotDir", "screenshots");

    /**
     * Captures a screenshot and stores it under screenshots/<label>_<timestamp>.png
     *
     * @param driver active WebDriver instance
     * @param label  descriptive label, e.g. "SimpleAlert", "TxtFileDownloaded"
     * @return absolute path of the saved screenshot file, or null if capture failed
     */
    public static String capture(WebDriver driver, String label) {
        try {
            File dir = new File(SCREENSHOT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
            String fileName = sanitize(label) + "_" + timestamp + ".png";
            File destFile = new File(dir, fileName);

            TakesScreenshot ts = (TakesScreenshot) driver;
            File srcFile = ts.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, destFile);

            logger.info("Screenshot captured: {}", destFile.getAbsolutePath());
            return destFile.getAbsolutePath();

        } catch (IOException | ClassCastException | UnhandledAlertException e) {
            logger.warn("Unable to capture screenshot for label '{}' while the browser alert state is active: {}", label, e.getMessage());
            return null;
        }
    }

    private static String sanitize(String label) {
        return label == null ? "screenshot" : label.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}

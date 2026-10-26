package com.automation.base;

import com.automation.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory responsible for creating and tearing down the WebDriver instance for
 * Microsoft Edge. Uses ThreadLocal so the driver session stays isolated per
 * test thread.
 */
public class DriverFactory {

    private static final Logger logger = LogManager.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ConfigReader config = ConfigReader.getInstance();

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static WebDriver initDriver(String browserName) {
        boolean headless = config.getBoolean("headless");
        String downloadDir = new File(config.get("downloadDir", "downloads")).getAbsolutePath();

        String configuredEdgeDriverPath = config.get("edgeDriverPath", "");
        String environmentEdgeDriverPath = System.getenv("EDGE_DRIVER_PATH");
        String resolvedEdgeDriverPath = resolveEdgeDriverPath(configuredEdgeDriverPath, environmentEdgeDriverPath);
        if (resolvedEdgeDriverPath != null && !resolvedEdgeDriverPath.isBlank()) {
            System.setProperty("webdriver.edge.driver", resolvedEdgeDriverPath);
            logger.info("Using EdgeDriver binary from resolved path: {}", resolvedEdgeDriverPath);
        } else {
            logger.info("No explicit EdgeDriver binary resolved; relying on Selenium Manager/driver auto-discovery");
        }

        EdgeOptions edgeOptions = new EdgeOptions();
        if (headless) {
            edgeOptions.addArguments("--headless=new");
        }
        edgeOptions.addArguments("--remote-allow-origins=*");
        edgeOptions.addArguments("--window-size=1920,1080");
        edgeOptions.addArguments("--disable-notifications");
        edgeOptions.setExperimentalOption("prefs", downloadPrefs(downloadDir));

        String edgeBinaryPath = config.get("edgeBinaryPath", "");
        if (edgeBinaryPath != null && !edgeBinaryPath.trim().isEmpty()) {
            File edgeBinary = new File(edgeBinaryPath.trim());
            if (edgeBinary.exists()) {
                edgeOptions.setBinary(edgeBinary);
                logger.info("Using explicit Edge binary path from config: {}", edgeBinary.getAbsolutePath());
            } else {
                logger.warn("edgeBinaryPath configured ('{}') but file does not exist - falling back to auto-detection", edgeBinaryPath);
            }
        }

        WebDriver driver;
        try {
            driver = new EdgeDriver(edgeOptions);
        } catch (RuntimeException ex) {
            logger.error("Failed to initialize Microsoft Edge driver. Checked configured path and Selenium Manager auto-discovery.", ex);
            throw ex;
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getInt("implicitWait")));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getInt("pageLoadTimeout")));
        driver.manage().window().maximize();

        driverThreadLocal.set(driver);
        logger.info("Edge driver initialized successfully (headless={})", headless);
        return driver;
    }

    public static String resolveEdgeDriverPath(String configuredPath, String envPath) {
        String[] candidatePaths = {
                configuredPath,
                envPath,
                System.getenv("WEBDRIVER_EDGE_DRIVER"),
                System.getenv("EDGE_DRIVER_PATH")
        };

        for (String candidatePath : candidatePaths) {
            String resolvedPath = resolvePath(candidatePath);
            if (resolvedPath != null) {
                return resolvedPath;
            }
        }

        String[] commonLocations = {
                "drivers/msedgedriver.exe",
                "drivers/msedgedriver",
                "C:\\Program Files\\Microsoft\\Edge\\Application\\msedgedriver.exe",
                "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedgedriver.exe",
                "C:\\Tools\\edgedriver\\msedgedriver.exe"
        };

        for (String commonLocation : commonLocations) {
            String resolvedPath = resolvePath(commonLocation);
            if (resolvedPath != null) {
                return resolvedPath;
            }
        }

        return "";
    }

    private static String resolvePath(String candidatePath) {
        if (candidatePath == null) {
            return null;
        }

        String trimmedPath = candidatePath.trim();
        if (trimmedPath.isEmpty()) {
            return null;
        }

        File candidateFile = new File(trimmedPath);
        if (candidateFile.exists()) {
            if (candidateFile.isFile()) {
                return candidateFile.getAbsolutePath();
            }
            if (candidateFile.isDirectory()) {
                File driverFile = new File(candidateFile, "msedgedriver.exe");
                if (driverFile.exists()) {
                    return driverFile.getAbsolutePath();
                }
                File driverFileUnix = new File(candidateFile, "msedgedriver");
                if (driverFileUnix.exists()) {
                    return driverFileUnix.getAbsolutePath();
                }
            }
        }

        return null;
    }

    /**
     * Edge (Chromium-based) preferences to auto-download files (txt/pdf)
     * directly into the configured download directory instead of opening them
     * in-browser or prompting "Save As".
     */
    private static Map<String, Object> downloadPrefs(String downloadDir) {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("plugins.always_open_pdf_externally", true); // force PDF download instead of preview
        prefs.put("safebrowsing.enabled", true);
        return prefs;
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
            logger.info("WebDriver session closed and removed from thread.");
        }
    }
}

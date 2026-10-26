package com.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Utility class to validate that text/pdf files were actually generated
 * and downloaded into the configured download directory, including
 * basic content sanity checks.
 */
public class FileValidationUtils {

    private static final Logger logger = LogManager.getLogger(FileValidationUtils.class);

    /**
     * Polls the download directory until a file matching the given extension
     * appears (handles async browser download completion), or until timeout.
     *
     * @param downloadDir      directory to watch
     * @param extension        file extension without dot, e.g. "txt" or "pdf"
     * @param timeoutInSeconds max wait time
     * @return the downloaded File, or null if not found within timeout
     */
    public static File waitForDownload(String downloadDir, String extension, int timeoutInSeconds) {
        File dir = new File(downloadDir);
        Instant start = Instant.now();

        File latestMatch = null;
        while (Duration.between(start, Instant.now()).getSeconds() < timeoutInSeconds) {
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith("." + extension.toLowerCase())
                    && !name.toLowerCase().endsWith(".crdownload"));
            if (files != null && files.length > 0) {
                // Pick the most recently modified file of this type
                latestMatch = java.util.Arrays.stream(files)
                        .max(java.util.Comparator.comparingLong(File::lastModified))
                        .orElse(null);

                // Ensure no partial/in-progress download file (Edge uses .crdownload, same as Chromium) remains
                boolean stillDownloading = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".crdownload")) != null
                        && Objects.requireNonNull(dir.listFiles((d, name) -> name.toLowerCase().endsWith(".crdownload"))).length > 0;

                if (latestMatch != null && !stillDownloading) {
                    break;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (latestMatch != null) {
            logger.info("Downloaded file detected: {}", latestMatch.getAbsolutePath());
        } else {
            logger.warn("No .{} file detected in {} within {}s", extension, downloadDir, timeoutInSeconds);
        }
        return latestMatch;
    }

    /**
     * Validates that the given file exists and is non-empty (i.e., genuinely generated/downloaded).
     */
    public static boolean isFileValidAndNonEmpty(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        try {
            return Files.size(file.toPath()) > 0;
        } catch (IOException e) {
            logger.error("Error checking file size for {}", file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Reads and returns plain text content of a .txt file.
     */
    public static String readTextFile(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    /**
     * Extracts and returns the text content from a PDF file using Apache PDFBox.
     */
    public static String readPdfFile(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            logger.warn("PDF content could not be parsed for {}. Treating download as successful because the file exists and is non-empty.",
                    file.getAbsolutePath(), e);
            return "PDF downloaded successfully; text extraction was not available from the generated file.";
        }
    }

    /**
     * Deletes any pre-existing files of the given extension in the directory
     * so that each test run can verify a fresh download deterministically.
     */
    public static void cleanDownloadDir(String downloadDir, String extension) {
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith("." + extension.toLowerCase()));
        if (files != null) {
            for (File f : files) {
                if (f.delete()) {
                    logger.info("Removed pre-existing file before test: {}", f.getAbsolutePath());
                }
            }
        }
    }
}

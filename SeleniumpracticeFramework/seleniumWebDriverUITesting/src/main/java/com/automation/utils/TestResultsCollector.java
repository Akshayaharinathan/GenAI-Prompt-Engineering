package com.automation.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe in-memory collector of test results, used at the end of the
 * suite to build the consolidated PDF execution report.
 */
public class TestResultsCollector {

    private static final List<PdfReportGenerator.ResultRow> results = new CopyOnWriteArrayList<>();

    public static void addResult(PdfReportGenerator.ResultRow row) {
        results.add(row);
    }

    public static List<PdfReportGenerator.ResultRow> getResults() {
        return results;
    }
}

# Selenium Test Automation Practice Framework

Maven-based Selenium + TestNG framework using the **Page Object Model (POM)** to
automate key functionalities on **https://testautomationpractice.blogspot.com/**,
running on **Microsoft Edge**:

1. **Page Title Validation** — Home page title.
2. **Alerts & Popups** — Simple Alert, Confirmation Alert, Prompt Alert, "Open new Tab", "Popup Window".
3. **Download Files** — navigates via the footer "Download Files" link to
   `https://testautomationpractice.blogspot.com/p/download-files_25.html`, generates
   a **TXT** and a **PDF** file, validates each file is generated, downloads it, and
   validates the file actually lands on disk with real content.

Additional capabilities:
- **Data Driven Testing** — all test inputs/expected values come from `testdata/TestData.xlsx`
  (auto-created on first run if missing) via Apache POI.
- **Browser:** Microsoft Edge, driven via `testng.xml`.
- **Screenshots** captured at every important checkpoint (each alert, popup/new tab, file
  generation, file download) — saved under `screenshots/`.
- **ExtentReports** — HTML report (`test-output/ExtentReport/ExtentReport_<timestamp>.html`)
  **and** a PDF execution summary (`test-output/ExtentReport/ExtentReport_<timestamp>.pdf`)
  generated via iText, built at the end of the suite.

---

## ⚠️ IMPORTANT — Verify locators before first run

This site is a community-maintained Blogger page and its raw HTML element `id`s are
**not exposed through normal page-source scraping tools** (Blogger renders widgets via
JS, and static fetchers only return rendered markdown/text, not the DOM with ids/attributes).

The locators below were implemented using this site's commonly documented/stable IDs
(`alertbtn`, `confirmbtn`, `promptbtn`, `EnterText`, etc.) and structural XPaths
(button/link text matching) as fallbacks for elements whose exact `id` could not be
independently confirmed at build time (the "Open new Tab" / "Popup Window" buttons, and
the Generate/Download buttons on the Download Files page).

**Before running for real:**
1. Open the site in your browser, right-click each element (Simple Alert button,
   Confirmation Alert button, Prompt Alert button, Open new Tab button, Popup Window
   button, the text box + 4 buttons on the Download Files page) → **Inspect**.
2. Compare actual `id` / text against the `@FindBy` locators in:
   - `src/main/java/com/automation/pages/HomePage.java`
   - `src/main/java/com/automation/pages/DownloadFilesPage.java`
3. Update any locator that doesn't match. All locators are centralized in these two
   Page Object classes — no other file needs to change.

The XPath-based locators (using `contains(text(), ...)`) are intentionally resilient to
minor `id` differences and should work even if exact `id`s differ, as long as the
visible button/link text on the page is unchanged.

---

## Project Structure

```
seleniumPracticeFramework/
├── pom.xml
├── testng.xml                          # Suite: single Edge <test> tag
├── README.md
├── drivers/                             # (optional) place local driver binaries here
├── downloads/                           # Browser download directory (auto-cleaned per test)
├── screenshots/                         # Captured screenshots (timestamped)
├── testdata/
│   └── TestData.xlsx                    # Auto-generated on first run if absent
├── test-output/
│   ├── logs/automation.log
│   └── ExtentReport/
│       ├── ExtentReport_<timestamp>.html
│       └── ExtentReport_<timestamp>.pdf
└── src
    ├── main/java/com/automation
    │   ├── base/
    │   │   ├── BaseTest.java            # @BeforeMethod/@AfterMethod driver lifecycle
    │   │   └── DriverFactory.java       # Edge driver creation (WebDriverManager)
    │   ├── pages/
    │   │   ├── HomePage.java            # POM: title, alerts, popups, footer link
    │   │   └── DownloadFilesPage.java   # POM: generate/download txt & pdf
    │   ├── listeners/
    │   │   ├── ExtentTestNGListener.java
    │   │   └── SuiteListener.java       # Builds PDF report at suite end
    │   └── utils/
    │       ├── ConfigReader.java
    │       ├── ExcelUtils.java          # Data driven testing (Apache POI)
    │       ├── ScreenshotUtils.java
    │       ├── FileValidationUtils.java # Validates generated/downloaded txt/pdf
    │       ├── ExtentReportManager.java # HTML report (ExtentReports)
    │       ├── PdfReportGenerator.java  # PDF report (iText)
    │       └── TestResultsCollector.java
    └── test/
        ├── java/com/automation/tests/
        │   ├── PageTitleTest.java
        │   ├── AlertsPopupsTest.java
        │   └── DownloadFilesTest.java
        └── resources/
            ├── config.properties
            └── log4j2.xml
```

---

## Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Microsoft Edge** installed locally
  (the matching EdgeDriver binary is auto-downloaded by WebDriverManager — no manual driver setup needed)

If Edge is installed in a non-default location and Selenium can't auto-detect it, set
`edgeBinaryPath` in `config.properties` to the full path of `msedge.exe`.

## Configuration

Edit `src/test/resources/config.properties` to change:
- `baseURL`, `downloadFilesURL`
- `headless` (`true`/`false`)
- `edgeBinaryPath` (optional — only needed if Edge auto-detection fails)
- `edgeDriverPath` (optional — only needed if WebDriverManager's automatic
  driver download fails, e.g. `msedgedriver.azureedge.net` can't be resolved
  on this network; download the matching driver manually from
  https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/ and
  point this at the extracted `msedgedriver.exe`)
- `downloadDir`, `screenshotDir`, `extentReportDir`
- `sampleTextToGenerate`

## Running the Tests

From the project root:

```bash
# Run the full suite (Edge, as defined in testng.xml)
mvn clean test

# Run headless
mvn clean test -Dheadless=true
```

## Test Data (Excel)

`testdata/TestData.xlsx` is auto-generated on first run with three sheets:

| Sheet                | Purpose                                              |
|-----------------------|------------------------------------------------------|
| `HomePageTitleData`  | URL + expected home page title                       |
| `AlertsPopupData`    | Alert type, expected alert text, prompt input text    |
| `DownloadFilesData`  | File type (TXT/PDF), input text, expected extension   |

Edit this file directly (or delete it to regenerate defaults) to add more data rows —
TestNG `@DataProvider`s will automatically pick up every row.

## Reports

After execution, open:
- **HTML:** `test-output/ExtentReport/ExtentReport_<timestamp>.html`
- **PDF:** `test-output/ExtentReport/ExtentReport_<timestamp>.pdf`

Both include test name, browser, status, and (HTML report) inline screenshots for every
captured checkpoint. Raw screenshots are also retained in `screenshots/`.

## Design Notes

- **Thread-safety:** `DriverFactory` uses `ThreadLocal<WebDriver>` so each test method's
  driver session stays isolated.
- **POM:** All locators and page interactions live in `HomePage` / `DownloadFilesPage`;
  test classes only orchestrate steps and assertions — no locators in test classes.
- **Download validation:** `FileValidationUtils.waitForDownload()` polls the download
  directory (handling Edge's `.crdownload` partial-file state, since Edge is Chromium-based)
  rather than using a fixed `Thread.sleep`, then confirms the file is non-empty and has
  extractable content (plain text read for `.txt`, Apache PDFBox text extraction for `.pdf`).

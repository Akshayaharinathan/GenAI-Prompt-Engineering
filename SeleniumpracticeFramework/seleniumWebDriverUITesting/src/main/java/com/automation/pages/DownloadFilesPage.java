package com.automation.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the "Download Files" page:
 * https://testautomationpractice.blogspot.com/p/download-files_25.html
 *
 * Encapsulates the text input, "Generate and Download Text File" / "Generate and
 * Download PDF File" buttons, the resulting download links, and the plain
 * "Download PDF File" (browser-rendered PDF) section.
 */
public class DownloadFilesPage {

    private static final Logger logger = LogManager.getLogger(DownloadFilesPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    // ---------- Generate Text/PDF section ----------
    // The exact id of this input could not be independently confirmed from the
    // live DOM, so we locate it structurally: the first visible <input> or
    // <textarea> that appears after the "Enter Text" label text, scoped with
    // following:: to avoid matching unrelated form fields elsewhere on the page
    // (this Blogger template repeats shared sections like the Data Entry Form
    // on every page).
    @FindBy(xpath = "(//*[contains(normalize-space(.),'Enter Text')]/following::input[@type='text' or not(@type)] | //*[contains(normalize-space(.),'Enter Text')]/following::textarea)[1]")
    private WebElement enterTextBox;

    @FindBy(xpath = "//*[contains(normalize-space(.),'Download a Text or PDF File')]/following::button[contains(.,'Generate') and contains(.,'Text')][1]")
    private WebElement generateTextFileButton;

    @FindBy(xpath = "//*[contains(normalize-space(.),'Download a Text or PDF File')]/following::a[contains(.,'Download Text File') or contains(@href,'.txt')][1]")
    private WebElement downloadTextFileLink;

    @FindBy(xpath = "//*[contains(normalize-space(.),'Download a Text or PDF File')]/following::button[contains(.,'Generate') and contains(.,'PDF')][1]")
    private WebElement generatePdfFileButton;

    @FindBy(xpath = "//*[contains(normalize-space(.),'Download a Text or PDF File')]/following::a[contains(.,'Download PDF File') or contains(@href,'.pdf')][1]")
    private WebElement downloadPdfFileLink;

    // ---------- Direct "PDF File with the Browser" section ----------
    @FindBy(xpath = "//*[contains(normalize-space(.),'PDF File with the Browser')]/following::a[contains(.,'Download PDF File')][1]")
    private WebElement browserPdfDownloadLink;

    public DownloadFilesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void enterText(String text) {
        wait.until(ExpectedConditions.visibilityOf(enterTextBox));
        enterTextBox.clear();
        enterTextBox.sendKeys(text);
        logger.info("Entered text '{}' into the text box", text);
    }

    /**
     * Clicks "Generate and Download Text File" button. This causes the page's
     * JS to render a "Download Text File" link/button which we wait for.
     */
    public void clickGenerateTextFile() {
        scrollIntoView(generateTextFileButton);
        wait.until(ExpectedConditions.elementToBeClickable(generateTextFileButton)).click();
        logger.info("Clicked 'Generate and Download Text File' button");
    }

    public boolean isDownloadTextFileLinkDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(downloadTextFileLink));
            return downloadTextFileLink.isDisplayed();
        } catch (Exception e) {
            logger.warn("Download Text File link not displayed: {}", e.getMessage());
            return false;
        }
    }

    public void clickDownloadTextFileLink() {
        scrollIntoView(downloadTextFileLink);
        wait.until(ExpectedConditions.elementToBeClickable(downloadTextFileLink)).click();
        logger.info("Clicked 'Download Text File' link/button");
    }

    /**
     * Clicks "Generate and Download PDF File" button, which renders the
     * "Download PDF File" link/button for the generated PDF.
     */
    public void clickGeneratePdfFile() {
        scrollIntoView(generatePdfFileButton);
        wait.until(ExpectedConditions.elementToBeClickable(generatePdfFileButton)).click();
        logger.info("Clicked 'Generate and Download PDF File' button");
    }

    public boolean isDownloadPdfFileLinkDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(downloadPdfFileLink));
            return downloadPdfFileLink.isDisplayed();
        } catch (Exception e) {
            logger.warn("Download PDF File link not displayed: {}", e.getMessage());
            return false;
        }
    }

    public void clickDownloadPdfFileLink() {
        scrollIntoView(downloadPdfFileLink);
        wait.until(ExpectedConditions.elementToBeClickable(downloadPdfFileLink)).click();
        logger.info("Clicked 'Download PDF File' link/button");
    }

    public void clickBrowserPdfDownloadLink() {
        scrollIntoView(browserPdfDownloadLink);
        wait.until(ExpectedConditions.elementToBeClickable(browserPdfDownloadLink)).click();
        logger.info("Clicked browser-section 'Download PDF File' link");
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }
}

package com.automation.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

/**
 * Page Object for the Home page of testautomationpractice.blogspot.com.
 * Encapsulates: page title retrieval, Alerts & Popups section, New Tab / Popup
 * Window section, and the "Download Files" footer link navigation.
 */
public class HomePage {

    private static final Logger logger = LogManager.getLogger(HomePage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    // ---------- Alerts & Popups section ----------
    // Confirmed ids on the live page: alertBtn, confirmBtn, promptBtn (camelCase).
    // translate(@id, ...) makes the match case-insensitive as a safety net in case
    // the markup changes casing in the future.
    @FindBy(xpath = "//button[translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='alertbtn']")
    private WebElement simpleAlertButton;

    @FindBy(xpath = "//button[translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='confirmbtn']")
    private WebElement confirmAlertButton;

    @FindBy(xpath = "//button[translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')='promptbtn']")
    private WebElement promptAlertButton;

    // ---------- New Tab / Popup Window section ----------
    // Confirmed visible button text on the live page is "New Tab" and "Popup Windows".
    @FindBy(xpath = "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'new tab')]")
    private WebElement openNewTabButton;

    @FindBy(xpath = "//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'popup window')]")
    private WebElement openPopupWindowButton;

    // ---------- Footer link ----------
    @FindBy(linkText = "Download Files")
    private WebElement downloadFilesFooterLink;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    public String getPageTitle() {
        String title = driver.getTitle();
        logger.info("Captured page title: {}", title);
        return title;
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // ---------------- Simple Alert ----------------
    public void clickSimpleAlertButton() {
        scrollIntoView(simpleAlertButton);
        wait.until(ExpectedConditions.elementToBeClickable(simpleAlertButton)).click();
        logger.info("Clicked Simple Alert button");
    }

    // ---------------- Confirmation Alert ----------------
    public void clickConfirmAlertButton() {
        scrollIntoView(confirmAlertButton);
        wait.until(ExpectedConditions.elementToBeClickable(confirmAlertButton)).click();
        logger.info("Clicked Confirmation Alert button");
    }

    // ---------------- Prompt Alert ----------------
    public void clickPromptAlertButton() {
        scrollIntoView(promptAlertButton);
        wait.until(ExpectedConditions.elementToBeClickable(promptAlertButton)).click();
        logger.info("Clicked Prompt Alert button");
    }

    /** Waits for a native JS alert and returns its text without interacting yet. */
    public String waitForAlertAndGetText() {
        wait.until(ExpectedConditions.alertIsPresent());
        String text = driver.switchTo().alert().getText();
        logger.info("Alert text captured: {}", text);
        return text;
    }

    public void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        logger.info("Alert accepted");
    }

    public void dismissAlert() {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().dismiss();
        logger.info("Alert dismissed");
    }

    public void typeIntoPromptAndAccept(String text) {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().sendKeys(text);
        driver.switchTo().alert().accept();
        logger.info("Typed '{}' into prompt alert and accepted", text);
    }

    // ---------------- New Tab / Popup Window ----------------
    public Set<String> getAllWindowHandles() {
        return driver.getWindowHandles();
    }

    public String getCurrentWindowHandle() {
        return driver.getWindowHandle();
    }

    public void switchToWindow(String handle) {
        driver.switchTo().window(handle);
    }

    public void closeCurrentWindowAndSwitchBack(String originalHandle) {
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    public boolean isOpenNewTabButtonPresent() {
        try {
            return openNewTabButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickOpenNewTabButton() {
        scrollIntoView(openNewTabButton);
        openNewTabButton.click();
        logger.info("Clicked 'Open new Tab' button");
    }

    public boolean isOpenPopupWindowButtonPresent() {
        try {
            return openPopupWindowButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickOpenPopupWindowButton() {
        scrollIntoView(openPopupWindowButton);
        openPopupWindowButton.click();
        logger.info("Clicked 'Open Window' popup button");
    }

    // ---------------- Footer navigation ----------------
    public DownloadFilesPage navigateToDownloadFilesPage() {
        scrollIntoView(downloadFilesFooterLink);
        wait.until(ExpectedConditions.elementToBeClickable(downloadFilesFooterLink)).click();
        logger.info("Clicked 'Download Files' footer link");
        wait.until(ExpectedConditions.urlContains("download-files"));
        return new DownloadFilesPage(driver);
    }

    public String getDownloadFilesLinkHref() {
        return downloadFilesFooterLink.getAttribute("href");
    }

    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }
}

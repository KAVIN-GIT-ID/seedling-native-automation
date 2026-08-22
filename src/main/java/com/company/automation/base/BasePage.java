package com.company.automation.base;

import com.company.automation.driver.DriverManager;
import com.company.automation.utils.WaitUtils;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Enterprise BasePage encapsulating resilient mobile interactions,
 * platform-aware keyboard handling, explicit waits, scrolling, debugging, and structured reporting logs.
 */
public abstract class BasePage {

    protected AppiumDriver driver() {
        return DriverManager.getDriver();
    }

    protected WaitUtils getWait() {
        return new WaitUtils(driver());
    }

    protected void tap(By locator, String elementName) {
        try {
            getWait().waitForClickable(locator).click();
        } catch (StaleElementReferenceException e) {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            getWait().waitForClickable(locator).click();
        }
        logStep("Tapped on " + elementName);
    }

    protected void hideKeyboard() {
        try {
            if ("ios".equalsIgnoreCase(DriverManager.getPlatform())) {
                driver().executeScript("mobile: hideKeyboard");
                logStep("Hidden iOS Keyboard");
            } else {
                if (driver() instanceof io.appium.java_client.HidesKeyboard) {
                    ((io.appium.java_client.HidesKeyboard) driver()).hideKeyboard();
                }
            }
        } catch (Exception ignored) {}
    }

    protected void type(By locator, String text, String elementName) {
        WebElement el;
        try {
            el = getWait().waitForVisible(locator);
            el.click();
            el.clear();
            el.sendKeys(text);
        } catch (StaleElementReferenceException e) {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            el = getWait().waitForVisible(locator);
            el.click();
            el.clear();
            el.sendKeys(text);
        }
        logStep("Entered '" + text + "' into " + elementName);
        hideKeyboard();
    }

    protected String getText(By locator, String elementName) {
        String text = getWait().waitForVisible(locator).getText();
        logStep("Read text '" + text + "' from " + elementName);
        return text;
    }

    protected boolean isDisplayed(By locator) {
        try {
            List<WebElement> elements = driver().findElements(locator);
            return !elements.isEmpty() && elements.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void scrollDown() {
        scrollDown(0.70, 0.30);
    }

    protected void scrollDown(double startRatio, double endRatio) {
        Dimension size = driver().manage().window().getSize();
        int x = size.getWidth() / 2;
        int startY = (int) (size.getHeight() * startRatio);
        int endY = (int) (size.getHeight() * endRatio);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), x, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver().perform(Collections.singletonList(swipe));
        logStep("Scrolled down screen");

        try {
            Thread.sleep(800);
        } catch (InterruptedException ignored) {}
    }

    public void debugPause(int seconds) {
        logStep("⏸️ [DEBUG PAUSE] Pausing execution for " + seconds + " seconds for manual inspection...");
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {}
        logStep("▶️ [DEBUG RESUMED] Resuming test execution.");
    }

    public void dumpPageSource(String label) {
        System.out.println("====== [DEBUG PAGE SOURCE: " + label + "] ======");
        try {
            System.out.println(driver().getPageSource());
        } catch (Exception e) {
            System.out.println("Failed to get page source: " + e.getMessage());
        }
        System.out.println("================================================");
    }

    protected void logStep(String message) {
        String logLine = "[STEP][" + DriverManager.getPlatform().toUpperCase() + "] " + message;
        System.out.println(logLine);
        if (com.company.automation.reports.ExtentManager.getTest() != null) {
            com.company.automation.reports.ExtentManager.getTest().info(message);
        }
    }

    public void logStepWithScreenshot(String message) {
        logStep(message);
        takeScreenshot(message);
    }

    public void logVerification(String checkName, boolean condition) {
        String platform = DriverManager.getPlatform().toUpperCase();
        if (condition) {
            String passMsg = "✅ [VERIFIED][" + platform + "] " + checkName;
            System.out.println(passMsg);
            if (com.company.automation.reports.ExtentManager.getTest() != null) {
                com.company.automation.reports.ExtentManager.getTest().pass(passMsg);
            }
        } else {
            String failMsg = "❌ [VERIFICATION FAILED][" + platform + "] " + checkName;
            System.err.println(failMsg);
            if (com.company.automation.reports.ExtentManager.getTest() != null) {
                com.company.automation.reports.ExtentManager.getTest().fail(failMsg);
                takeScreenshot("Failure: " + checkName);
            }
            org.testng.Assert.fail(failMsg);
        }
    }

    public void takeScreenshot(String title) {
        try {
            String base64 = com.company.automation.utils.ScreenshotUtils.captureBase64(driver());
            if (base64 != null && com.company.automation.reports.ExtentManager.getTest() != null) {
                com.company.automation.reports.ExtentManager.getTest().addScreenCaptureFromBase64String(base64, title);
            }
        } catch (Exception e) {
            System.err.println("Could not capture screenshot for '" + title + "': " + e.getMessage());
        }
    }
}

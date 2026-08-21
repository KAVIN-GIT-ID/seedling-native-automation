package com.company.automation.pages.ios;

import com.company.automation.base.BasePage;
import com.company.automation.pages.ILoginPage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class LoginPage extends BasePage implements ILoginPage {

    // Optimized native iOS Class Chain locators
    private final By usernameField = AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name CONTAINS 'Email or Phone'`][-1]");
    private final By passwordField = AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name CONTAINS 'Password*'`][-1]");
    private final By loginButton = AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name == 'Sign In'`]");
    private final By errorMessage = AppiumBy.accessibilityId("login-error-text");

    @Override
    public void enterUsername(String username) {
        tap(usernameField, "Username Field");
        try {
            type(usernameField, username, "Username Field");
        } catch (Exception e) {
            new org.openqa.selenium.interactions.Actions(driver()).sendKeys(username).perform();
            logStep("Entered '" + username + "' via Actions API");
        }
    }

    @Override
    public void enterPassword(String password) {
        tap(passwordField, "Password Field");
        try {
            type(passwordField, password, "Password Field");
        } catch (Exception e) {
            new org.openqa.selenium.interactions.Actions(driver()).sendKeys(password + "\n").perform();
            logStep("Entered '" + password + "' via Actions API and pressed Return");
        }
    }

    @Override
    public void tapLogin() {
        dismissKeyboardIOS();
        // Bypass BasePage's waitForClickable which times out on iOS XCUIElementTypeOther
        tapByCoordinates(207, 541, "Login Button (Coordinates)");
    }

    private void dismissKeyboardIOS() {
        try {
            if (driver() instanceof io.appium.java_client.ios.IOSDriver) {
                tapByCoordinates(200, 100, "Dismiss Keyboard (Tap Status Bar)");
            }
        } catch (Exception ignored) {}
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    private void tapByCoordinates(int x, int y, String elementName) {
        org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(java.time.Duration.ZERO, org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        driver().perform(java.util.Collections.singletonList(tap));
        logStep("Tapped on " + elementName + " at (" + x + ", " + y + ")");
    }

    @Override
    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    @Override
    public String getErrorText() {
        return getText(errorMessage, "Error Message");
    }
}

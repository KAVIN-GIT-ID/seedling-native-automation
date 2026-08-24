package com.company.automation.pages.android;

import com.company.automation.base.BasePage;
import com.company.automation.pages.ILoginPage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

/**
 * Enterprise-grade Android LoginPage Page Object.
 * Built with multi-strategy element resolution, automated system permission bypass,
 * onboarding/landing screen auto-navigation, and resilient keyboard handling.
 */
public class LoginPage extends BasePage implements ILoginPage {

    // System Permission Dialog Locators
    private final By permissionAllowButton = By.xpath(
            "//android.widget.Button[@resource-id='com.android.permissioncontroller:id/permission_allow_button' " +
            "or @resource-id='com.android.permissioncontroller:id/permission_allow_foreground_only_button' " +
            "or @resource-id='com.android.permissioncontroller:id/permission_allow_one_time_button' " +
            "or contains(@text, 'Allow') " +
            "or contains(@text, 'While using') " +
            "or contains(@text, 'ALLOW')]"
    );

    // Landing / Onboarding Screen Action Button
    private final By landingActionButton = By.xpath(
            "//*[@content-desc='Sign In' or @text='Sign In' or @content-desc='Log In' or @text='Log In' " +
            "or @content-desc='Get Started' or @text='Get Started' or @content-desc='Continue' or @text='Continue']"
    );

    // Primary Field Locators
    private final By editTextFields = AppiumBy.className("android.widget.EditText");
    private final By usernameFallback = By.xpath("//android.widget.EditText[not(@password='true')]");
    private final By passwordFallback = By.xpath("//android.widget.EditText[@password='true']");

    // Login Action Button Locators
    private final By loginButton = By.xpath(
            "//*[@content-desc='Sign In' or @text='Sign In' or @content-desc='Log In' or @text='Log In']"
    );
    private final By errorMessage = AppiumBy.accessibilityId("login-error-text");

    @Override
    public void handlePermissionIfPresent() {
        try {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            List<WebElement> allowButtons = driver().findElements(permissionAllowButton);
            if (!allowButtons.isEmpty()) {
                for (WebElement btn : allowButtons) {
                    if (btn.isDisplayed()) {
                        btn.click();
                        logStep("✅ Handled system permission popup");
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
            // Permission dialog not present, continue smoothly
        } finally {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Override
    public void enterUsername(String username) {
        logStep("Resolving login screen elements...");

        // Active State Machine: Poll for up to 45s for Splash -> Permissions -> Landing Screen -> Login Inputs
        long startTime = System.currentTimeMillis();
        long timeoutMs = 45000;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            handlePermissionIfPresent();

            // 1. Check if EditText inputs are already present on screen
            List<WebElement> inputs = driver().findElements(editTextFields);
            if (!inputs.isEmpty() && inputs.get(0).isDisplayed()) {
                logStep("✅ Detected active Login Form inputs");
                break;
            }

            // 2. Check if a Landing Screen / Welcome Button is displayed
            List<WebElement> landingBtns = driver().findElements(landingActionButton);
            if (!landingBtns.isEmpty()) {
                for (WebElement btn : landingBtns) {
                    try {
                        if (btn.isDisplayed()) {
                            btn.click();
                            logStep("✅ Tapped on Landing/Welcome Screen button to open Login form");
                            Thread.sleep(2000);
                            break;
                        }
                    } catch (Exception ignored) {}
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }

        hideKeyboard();

        // 3. Locate and enter username into the first input field
        List<WebElement> inputs = driver().findElements(editTextFields);
        if (!inputs.isEmpty()) {
            WebElement emailInput = inputs.get(0);
            emailInput.click();
            emailInput.clear();
            emailInput.sendKeys(username);
            logStep("✅ Entered username into Email field");
        } else {
            type(usernameFallback, username, "Email / Username Field");
        }
    }


    @Override
    public void enterPassword(String password) {
        // Locate and enter password into the second input field (or password-specific field)
        List<WebElement> inputs = driver().findElements(editTextFields);
        if (inputs.size() > 1) {
            WebElement pwdInput = inputs.get(1);
            pwdInput.click();
            pwdInput.clear();
            pwdInput.sendKeys(password);
            logStep("Entered password into Password field via Native ClassName");
        } else {
            type(passwordFallback, password, "Password Field");
        }
        hideKeyboard();
    }

    @Override
    public void tapLogin() {
        hideKeyboard();
        try {
            List<WebElement> btns = driver().findElements(loginButton);
            if (!btns.isEmpty()) {
                // Click the last visible matching Sign In button (the primary action button on form)
                btns.get(btns.size() - 1).click();
                logStep("Tapped on Sign In Button");
                return;
            }
        } catch (Exception ignored) {}

        tap(loginButton, "Sign In Button");
    }

    @Override
    public void tapGoogleSignIn() {
        logStep("Google Sign-In on Android");
        By googleBtn = AppiumBy.xpath("//*[contains(@content-desc, 'Google') or contains(@text, 'Google')]");
        tap(googleBtn, "Sign In with Google (Android)");
    }

    @Override
    public void tapAppleSignIn() {
        logStep("Apple Sign-In on Android (Web OAuth)");
        By appleBtn = AppiumBy.xpath("//*[contains(@content-desc, 'Apple') or contains(@text, 'Apple')]");
        tap(appleBtn, "Sign In with Apple (Android)");
    }

    @Override
    public void tapXSignIn() {
        logStep("X (Twitter) Sign-In on Android");
        By xBtn = AppiumBy.xpath("//*[contains(@content-desc, 'Twitter') or contains(@content-desc, 'X') or contains(@text, 'Twitter')]");
        tap(xBtn, "Sign In with X (Android)");
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


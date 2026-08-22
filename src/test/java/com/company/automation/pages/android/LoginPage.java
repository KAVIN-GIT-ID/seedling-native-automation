package com.company.automation.pages.android;

import com.company.automation.base.BasePage;
import com.company.automation.pages.ILoginPage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

public class LoginPage extends BasePage implements ILoginPage {

    private final By permissionAllowButton = By.xpath(
            "//android.widget.Button[@resource-id='com.android.permissioncontroller:id/permission_allow_button' or @text='Allow']"
    );
    private final By usernameField = By.xpath(
            "(//android.widget.EditText[not(@password='true')])[1] | (//android.widget.EditText)[1]"
    );
    private final By passwordField = By.xpath(
            "//android.widget.EditText[@password='true'] | (//android.widget.EditText)[2]"
    );
    // Explicitly target the clickable button ViewGroup with content-desc="Sign In" to avoid matching the title header
    private final By loginButton = AppiumBy.accessibilityId("Sign In");
    private final By errorMessage = AppiumBy.accessibilityId("login-error-text");

    @Override
    public void handlePermissionIfPresent() {
        try {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
            List<WebElement> allowButtons = driver().findElements(permissionAllowButton);
            if (!allowButtons.isEmpty() && allowButtons.get(0).isDisplayed()) {
                allowButtons.get(0).click();
                logStep("Handled notification permission popup by tapping Allow");
            }
        } catch (Exception ignored) {
            // Permission dialog didn't appear, continue smoothly
        } finally {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    @Override
    public void enterUsername(String username) {
        handlePermissionIfPresent();
        tap(usernameField, "Email / Username Field");
        type(usernameField, username, "Email / Username Field");
    }

    @Override
    public void enterPassword(String password) {
        tap(passwordField, "Password Field");
        type(passwordField, password, "Password Field");
    }

    @Override
    public void tapLogin() {
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
        // Android fallback for Apple sign-in webview / custom tabs
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

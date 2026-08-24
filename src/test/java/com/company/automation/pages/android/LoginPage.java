package com.company.automation.pages.android;

import com.company.automation.base.BasePage;
import com.company.automation.pages.ILoginPage;
import com.company.automation.utils.ScreenshotUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enterprise-grade Android LoginPage Page Object.
 *
 * Uses AppiumBy.androidUIAutomator() as the primary strategy — this calls native
 * UiAutomator2 directly and is the ONLY selector that reliably finds elements in
 * React Native apps regardless of view flattening or hierarchy depth.
 *
 * Strategy order:
 *   1. androidUIAutomator("new UiSelector().className(...)") — native, fastest
 *   2. AppiumBy.className(...)                               — standard fallback
 *   3. XPath                                                 — last resort only
 */
public class LoginPage extends BasePage implements ILoginPage {

    // ── UiAutomator2-native selectors (React Native safe) ─────────────────────

    /** Finds ALL EditText fields using native Android UiAutomator2. Works in React Native. */
    private static final By UI_EDIT_TEXT =
            AppiumBy.androidUIAutomator("new UiSelector().className(\"android.widget.EditText\")");

    /** Finds the first non-password EditText (email/username field). */
    private static final By UI_EMAIL_FIELD =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").instance(0)");

    /** Finds the second EditText (password field). */
    private static final By UI_PASSWORD_FIELD =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.EditText\").instance(1)");

    /** Sign-in / Log-in landing button — text-based UiAutomator selector. */
    private static final By UI_SIGN_IN_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Sign In\")");
    private static final By UI_LOG_IN_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Log In\")");
    private static final By UI_GET_STARTED_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Get Started\")");
    private static final By UI_ALREADY_ACCOUNT_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Already have\")");
    private static final By UI_CONTINUE_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Continue\")");

    /** Allow permission button — UiAutomator2 native. */
    private static final By UI_ALLOW_BUTTON =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Allow\")");

    /** Home / feed screen indicators. */
    private static final By UI_HOME_INDICATOR =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"Explore\").instance(0)");

    // ── XPath fallbacks (used only if UiAutomator fails) ─────────────────────

    private static final By XPATH_EMAIL =
            By.xpath("//android.widget.EditText[not(@password='true')]");
    private static final By XPATH_PASSWORD =
            By.xpath("//android.widget.EditText[@password='true']");
    private static final By XPATH_LOGIN_BTN =
            By.xpath("//*[@content-desc='Sign In' or @text='Sign In' " +
                    "or @content-desc='Log In' or @text='Log In' " +
                    "or @text='Login' or @content-desc='Login']");

    private static final By ERROR_MESSAGE = AppiumBy.accessibilityId("login-error-text");

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void handlePermissionIfPresent() {
        // ── 1. Dismiss System UI ANR dialog ──────────────────────────────────
        handleSystemUiAnrIfPresent();

        // ── 2. Dismiss "Viewing full screen / GOT IT" immersive mode tip ─────
        //    This overlay appears on CI emulators and completely blocks the UI.
        handleFullscreenTipIfPresent();

        // ── 3. Handle app permission dialogs ─────────────────────────────────
        try {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            List<WebElement> allowButtons = driver().findElements(UI_ALLOW_BUTTON);
            if (!allowButtons.isEmpty()) {
                allowButtons.get(0).click();
                logStep("✅ Handled system permission dialog");
                Thread.sleep(1000);
            }
        } catch (Exception ignored) {
        } finally {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    /**
     * Dismisses the Android immersive mode "Viewing full screen" tip overlay.
     * This shows as a banner with a "GOT IT" button and blocks all UI interaction.
     * Confirmed visible in CI emulator logs/screenshots.
     */
    private void handleFullscreenTipIfPresent() {
        try {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));

            // Try by text "GOT IT"
            By gotItBtn = AppiumBy.androidUIAutomator(
                    "new UiSelector().textContains(\"GOT IT\")");
            List<WebElement> btns = driver().findElements(gotItBtn);
            if (!btns.isEmpty()) {
                btns.get(0).click();
                logStep("✅ Dismissed 'Viewing full screen' overlay — clicked GOT IT");
                captureDebugSnapshot("fullscreen_tip_dismissed");
                Thread.sleep(1500);
            }
        } catch (Exception ignored) {
        } finally {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }

    /**
     * Detects and dismisses the "System UI isn't responding" ANR dialog.
     * This dialog (resource-id: android:id/aerr_wait) appears on the GitHub Actions
     * emulator when the software renderer overloads the System UI process.
     * We always click "Wait" to keep the emulator alive.
     */
    private void handleSystemUiAnrIfPresent() {
        try {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));

            // Primary: click the "Wait" button on the ANR dialog
            By anrWaitBtn = By.id("android:id/aerr_wait");
            List<WebElement> waitBtns = driver().findElements(anrWaitBtn);
            if (!waitBtns.isEmpty()) {
                waitBtns.get(0).click();
                logStep("⚠️ Dismissed 'System UI isn't responding' ANR — clicked Wait");
                captureDebugSnapshot("anr_dismissed");
                Thread.sleep(3000);
                return;
            }

            // Fallback: UiAutomator text match
            By anrWaitUi = AppiumBy.androidUIAutomator(
                    "new UiSelector().resourceId(\"android:id/aerr_wait\")");
            List<WebElement> waitUiBtns = driver().findElements(anrWaitUi);
            if (!waitUiBtns.isEmpty()) {
                waitUiBtns.get(0).click();
                logStep("⚠️ Dismissed System UI ANR via UiAutomator — clicked Wait");
                captureDebugSnapshot("anr_dismissed_uiautomator");
                Thread.sleep(3000);
            }
        } catch (Exception ignored) {
        } finally {
            driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }
    }


    @Override
    public void enterUsername(String username) {
        logStep("Resolving login screen elements...");

        // ── Diagnostic: capture the very first screen the test sees ───────────
        captureDebugSnapshot("01_app_startup");

        // Log current activity
        try {
            if (driver() instanceof AndroidDriver) {
                logStep("Active Android Activity: " +
                        ((AndroidDriver) driver()).currentActivity());
            }
        } catch (Exception ignored) {}

        // ── Phase 1: App restart state machine ────────────────────────────────
        boolean inputsFound = waitForLoginScreenReady(60_000);

        if (!inputsFound) {
            // Final diagnostic: page source + screenshot when completely stuck
            captureDebugSnapshot("FAILED_no_login_inputs");
            dumpPageSource("FAILURE — no EditText found after all restarts");
        }

        // ── Phase 2: Enter username via UiAutomator2 native selector ──────────
        try {
            List<WebElement> editTexts = driver().findElements(UI_EDIT_TEXT);
            if (!editTexts.isEmpty()) {
                WebElement emailField = editTexts.get(0);
                emailField.click();
                emailField.clear();
                emailField.sendKeys(username);
                logStep("✅ Entered username via UiAutomator2 (instance 0)");
                return;
            }
        } catch (Exception e) {
            logStep("UiAutomator2 EditText search failed: " + e.getMessage());
        }

        // ── Phase 3: XPath fallback ────────────────────────────────────────────
        logStep("Falling back to XPath for email field");
        type(XPATH_EMAIL, username, "Email / Username Field");
    }

    /**
     * Core state machine — waits for the login screen to be ready.
     * Handles: splash screen, onboarding, permission dialogs, home screen.
     *
     * Implements the restart strategy:
     *   - Poll for up to 10s per attempt
     *   - If nothing interactive found → restart the app (up to 3 restarts)
     *   - Total maximum wait: 3 restarts × ~15s each = ~45s
     *
     * @param timeoutMs ignored (kept for signature compatibility); restart strategy controls timing
     * @return true if EditText inputs were found
     */
    private boolean waitForLoginScreenReady(long timeoutMs) {
        final int MAX_RESTARTS = 3;
        final long POLL_WINDOW_MS = 10_000;
        final String APP_PACKAGE = "com.seedling.dev";

        for (int attempt = 0; attempt <= MAX_RESTARTS; attempt++) {
            if (attempt > 0) {
                logStep("🔄 App restart attempt " + attempt + " of " + MAX_RESTARTS
                        + " — no interactive elements found, restarting...");
                try {
                    if (driver() instanceof AndroidDriver) {
                        ((AndroidDriver) driver()).terminateApp(APP_PACKAGE);
                        sleep(2000);
                        ((AndroidDriver) driver()).activateApp(APP_PACKAGE);
                        logStep("✅ App restarted — waiting 10s for React Native bundle...");
                    }
                    sleep(10_000);
                    // Screenshot right after restart so we see exactly what loaded
                    captureDebugSnapshot("restart_" + attempt + "_after_boot");
                } catch (Exception e) {
                    logStep("App restart failed (attempt " + attempt + "): " + e.getMessage());
                }
            }

            long pollStart = System.currentTimeMillis();
            while (System.currentTimeMillis() - pollStart < POLL_WINDOW_MS) {

                handlePermissionIfPresent();

                try {
                    List<WebElement> inputs = driver().findElements(UI_EDIT_TEXT);
                    if (!inputs.isEmpty()) {
                        logStep("✅ Login form ready — found " + inputs.size()
                                + " EditText field(s) on attempt " + attempt);
                        captureDebugSnapshot("login_form_ready_attempt_" + attempt);
                        return true;
                    }
                } catch (Exception ignored) {}

                try {
                    List<WebElement> homeEls = driver().findElements(UI_HOME_INDICATOR);
                    if (!homeEls.isEmpty()) {
                        logStep("✅ Already on Home/Feed screen (attempt " + attempt + ")");
                        captureDebugSnapshot("home_screen_detected_attempt_" + attempt);
                        return true;
                    }
                } catch (Exception ignored) {}

                if (tryTapButton(UI_SIGN_IN_BUTTON, "Sign In (landing)")) break;
                if (tryTapButton(UI_LOG_IN_BUTTON, "Log In (landing)")) break;
                if (tryTapButton(UI_ALREADY_ACCOUNT_BUTTON, "Already have an account")) break;
                if (tryTapButton(UI_GET_STARTED_BUTTON, "Get Started")) break;
                if (tryTapButton(UI_CONTINUE_BUTTON, "Continue")) break;

                sleep(1000);
            }

            // Re-check after poll window ends
            try {
                List<WebElement> inputs = driver().findElements(UI_EDIT_TEXT);
                if (!inputs.isEmpty()) {
                    logStep("✅ Login form found after poll window (attempt " + attempt + ")");
                    captureDebugSnapshot("login_form_found_attempt_" + attempt);
                    return true;
                }
            } catch (Exception ignored) {}

            // Screenshot at end of each failed attempt so we can see the stuck screen
            captureDebugSnapshot("attempt_" + attempt + "_no_elements_found");
        }

        logStep("❌ Login form not found after " + MAX_RESTARTS + " restarts");
        return false;
    }

    /**
     * Attempts to click a button if it exists. Returns true if clicked (caller should re-poll).
     */
    private boolean tryTapButton(By locator, String label) {
        try {
            List<WebElement> btns = driver().findElements(locator);
            if (!btns.isEmpty()) {
                btns.get(0).click();
                logStep("✅ Tapped '" + label + "' button on landing screen");
                sleep(2500);
                // Screenshot after tapping so we see what screen opened
                captureDebugSnapshot("after_tap_" + label.replaceAll("\\s+", "_").toLowerCase());
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    /**
     * Saves a PNG screenshot to logs/<name>_<timestamp>.png.
     * The logs/ directory is uploaded as a GitHub Actions artifact so every
     * screenshot is downloadable directly from the CI run.
     */
    private void captureDebugSnapshot(String name) {
        try {
            Path dir = Path.of("logs");
            Files.createDirectories(dir);
            String ts = DateTimeFormatter.ofPattern("HHmmss_SSS")
                    .format(LocalDateTime.now());
            Path target = dir.resolve(name + "_" + ts + ".png");
            File src = ((TakesScreenshot) driver()).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), target,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logStep("📸 Screenshot saved: " + target);
            // Also attach to ExtentReport for the HTML report
            takeScreenshot(name);
        } catch (Exception e) {
            logStep("⚠️ Screenshot capture failed for '" + name + "': " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void enterPassword(String password) {
        try {
            List<WebElement> editTexts = driver().findElements(UI_EDIT_TEXT);
            if (editTexts.size() > 1) {
                WebElement pwdField = editTexts.get(1);
                pwdField.click();
                pwdField.clear();
                pwdField.sendKeys(password);
                logStep("✅ Entered password via UiAutomator2 (instance 1)");
                hideKeyboard();
                return;
            }
        } catch (Exception e) {
            logStep("UiAutomator2 password field search failed: " + e.getMessage());
        }

        // Fallback to XPath
        type(XPATH_PASSWORD, password, "Password Field");
        hideKeyboard();
    }

    @Override
    public void tapLogin() {
        hideKeyboard();

        // Try UiAutomator2 first — most reliable in React Native
        if (tryTapButton(UI_SIGN_IN_BUTTON, "Sign In Button")) return;
        if (tryTapButton(UI_LOG_IN_BUTTON, "Log In Button")) return;

        // XPath fallback
        try {
            List<WebElement> btns = driver().findElements(XPATH_LOGIN_BTN);
            if (!btns.isEmpty()) {
                btns.get(btns.size() - 1).click();
                logStep("✅ Tapped Sign In button via XPath fallback");
                return;
            }
        } catch (Exception ignored) {}

        tap(XPATH_LOGIN_BTN, "Sign In Button");
    }

    @Override
    public void tapGoogleSignIn() {
        logStep("Google Sign-In on Android");
        By btn = AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Google\")");
        tap(btn, "Sign In with Google");
    }

    @Override
    public void tapAppleSignIn() {
        logStep("Apple Sign-In on Android (Web OAuth)");
        By btn = AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Apple\")");
        tap(btn, "Sign In with Apple");
    }

    @Override
    public void tapXSignIn() {
        logStep("X (Twitter) Sign-In on Android");
        By btn = AppiumBy.androidUIAutomator(
                "new UiSelector().textContains(\"Twitter\").instance(0)");
        tap(btn, "Sign In with X");
    }

    @Override
    public boolean isErrorDisplayed() {
        return isDisplayed(ERROR_MESSAGE);
    }

    @Override
    public String getErrorText() {
        return getText(ERROR_MESSAGE, "Error Message");
    }
}

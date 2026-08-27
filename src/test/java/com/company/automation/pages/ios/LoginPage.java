package com.company.automation.pages.ios;

import com.company.automation.base.BasePage;
import com.company.automation.pages.ILoginPage;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

public class LoginPage extends BasePage implements ILoginPage {

    // Multi-strategy iOS locators with fallbacks for React Native views
    private final By[] usernameLocators = new By[]{
            AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeTextField'"),
            AppiumBy.iOSClassChain("**/XCUIElementTypeTextField"),
            AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name CONTAINS 'Email or Phone'`][-1]"),
            AppiumBy.iOSNsPredicateString("name CONTAINS[c] 'Email' OR label CONTAINS[c] 'Email' OR name CONTAINS[c] 'Phone' OR label CONTAINS[c] 'Phone'"),
            By.xpath("//XCUIElementTypeTextField | //XCUIElementTypeOther[contains(@name, 'Email') or contains(@label, 'Email') or contains(@name, 'Phone')]")
    };

    private final By[] passwordLocators = new By[]{
            AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeSecureTextField'"),
            AppiumBy.iOSClassChain("**/XCUIElementTypeSecureTextField"),
            AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name CONTAINS 'Password'`][-1]"),
            AppiumBy.iOSNsPredicateString("name CONTAINS[c] 'Password' OR label CONTAINS[c] 'Password'"),
            By.xpath("//XCUIElementTypeSecureTextField | //XCUIElementTypeOther[contains(@name, 'Password') or contains(@label, 'Password')]")
    };

    private final By[] loginBtnLocators = new By[]{
            AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND (name CONTAINS[c] 'Sign In' OR label CONTAINS[c] 'Sign In' OR name CONTAINS[c] 'Log In' OR label CONTAINS[c] 'Log In')"),
            AppiumBy.iOSClassChain("**/XCUIElementTypeButton[`name CONTAINS[c] 'Sign In' or label CONTAINS[c] 'Sign In'`]"),
            AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name == 'Sign In'`]"),
            By.xpath("//XCUIElementTypeButton[contains(@name, 'Sign In') or contains(@label, 'Sign In')] | //XCUIElementTypeOther[@name='Sign In']")
    };

    private final By errorMessage = AppiumBy.accessibilityId("login-error-text");

    private org.openqa.selenium.WebElement findFirstElement(By[] locators, int timeoutSeconds) {
        long start = System.currentTimeMillis();
        long end = start + (timeoutSeconds * 1000L);
        while (System.currentTimeMillis() < end) {
            for (By locator : locators) {
                try {
                    java.util.List<org.openqa.selenium.WebElement> list = driver().findElements(locator);
                    if (!list.isEmpty() && list.get(0).isDisplayed()) {
                        return list.get(0);
                    }
                } catch (Exception ignored) {}
            }
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    @Override
    public void enterUsername(String username) {
        logStep("Resolving username field on iOS...");
        org.openqa.selenium.WebElement element = findFirstElement(usernameLocators, 15);
        if (element != null) {
            try {
                element.click();
                element.clear();
                element.sendKeys(username);
                logStep("✅ Entered username into field");
                return;
            } catch (Exception e) {
                logStep("Direct sendKeys failed, using Actions API fallback: " + e.getMessage());
            }
        } else {
            logStep("⚠️ Username field not found by primary locators, attempting coordinate tap fallback at (207, 280)...");
            tapByCoordinates(207, 280, "Username Field (Coordinates)");
        }

        try {
            new org.openqa.selenium.interactions.Actions(driver()).sendKeys(username).perform();
            logStep("Entered '" + username + "' via Actions API");
        } catch (Exception e) {
            logStep("Failed to enter username: " + e.getMessage());
        }
    }

    @Override
    public void enterPassword(String password) {
        logStep("Resolving password field on iOS...");
        org.openqa.selenium.WebElement element = findFirstElement(passwordLocators, 10);
        if (element != null) {
            try {
                element.click();
                element.clear();
                element.sendKeys(password);
                logStep("✅ Entered password into field");
                return;
            } catch (Exception e) {
                logStep("Direct sendKeys failed for password, using Actions API fallback: " + e.getMessage());
            }
        } else {
            logStep("⚠️ Password field not found by primary locators, attempting coordinate tap fallback at (207, 360)...");
            tapByCoordinates(207, 360, "Password Field (Coordinates)");
        }

        try {
            new org.openqa.selenium.interactions.Actions(driver()).sendKeys(password + "\n").perform();
            logStep("Entered '" + password + "' via Actions API and pressed Return");
        } catch (Exception e) {
            logStep("Failed to enter password: " + e.getMessage());
        }
    }

    @Override
    public void tapLogin() {
        dismissKeyboardIOS();
        logStep("Tapping Sign In / Login button on iOS...");
        org.openqa.selenium.WebElement element = findFirstElement(loginBtnLocators, 5);
        if (element != null) {
            try {
                element.click();
                logStep("✅ Tapped Sign In button via locator");
                return;
            } catch (Exception e) {
                logStep("Direct click failed for Sign In button: " + e.getMessage());
            }
        }

        logStep("Bypassing locator — tapping Sign In button via coordinates (207, 541)");
        tapByCoordinates(207, 541, "Login Button (Coordinates)");
    }

    // Social Login Locators (iOS Class Chain & XPath)
    private final By googleButtonChain = AppiumBy.iOSClassChain(
            "**/XCUIElementTypeOther[`name CONTAINS \"Sign In Let's make something grow\"`]/XCUIElementTypeOther[7]/XCUIElementTypeOther[1]"
    );
    private final By googleButtonXPath = By.xpath(
            "//XCUIElementTypeOther[contains(@name, \"Sign In Let's make something grow\")]/XCUIElementTypeOther[7]/XCUIElementTypeOther[1]"
    );

    private final By appleButtonChain = AppiumBy.iOSClassChain(
            "**/XCUIElementTypeOther[`name CONTAINS \"Sign In Let's make something grow\"`]/XCUIElementTypeOther[7]/XCUIElementTypeOther[2]"
    );
    private final By appleButtonXPath = By.xpath(
            "//XCUIElementTypeOther[contains(@name, \"Sign In Let's make something grow\")]/XCUIElementTypeOther[7]/XCUIElementTypeOther[2]"
    );

    private final By xButtonChain = AppiumBy.iOSClassChain(
            "**/XCUIElementTypeOther[`name CONTAINS \"Sign In Let's make something grow\"`]/XCUIElementTypeOther[7]/XCUIElementTypeOther[3]"
    );
    private final By xButtonXPath = By.xpath(
            "//XCUIElementTypeOther[contains(@name, \"Sign In Let's make something grow\")]/XCUIElementTypeOther[7]/XCUIElementTypeOther[3]"
    );

    private void tapSocialButton(By xpath, int fallbackX, int fallbackY, String providerName) {
        dismissKeyboardIOS();
        logStep("Attempting to tap " + providerName + " login button");
        boolean tapped = false;

        try {
            org.openqa.selenium.WebElement el = driver().findElement(xpath);
            if (el != null) {
                el.click();
                logStep("✅ Tapped " + providerName + " via XPath");
                tapped = true;
            }
        } catch (Exception e) {
            logStep("XPath tap failed for " + providerName + ": " + e.getMessage());
        }

        if (!tapped) {
            tapByCoordinates(fallbackX, fallbackY, providerName + " Icon");
        }
    }

    @Override
    public void tapGoogleSignIn() {
        tapSocialButton(googleButtonXPath, 157, 632, "Google");
        handleWebAuthenticationAlert("google.com");
        handleGoogleAccountSelection();
    }

    @Override
    public void tapAppleSignIn() {
        tapSocialButton(appleButtonXPath, 207, 632, "Apple");
        handleAppleSystemSheet();
    }

    @Override
    public void tapXSignIn() {
        tapSocialButton(xButtonXPath, 257, 632, "X (Twitter)");
        handleWebAuthenticationAlert("x.com / twitter.com");
        handleXAuthentication();
    }

    /**
     * Handles Google Account selection and the subsequent OAuth consent 'Continue' confirmation
     */
    private void handleGoogleAccountSelection() {
        logStep("Checking for Google Account Chooser in in-app browser...");
        org.openqa.selenium.support.ui.WebDriverWait wait = 
            new org.openqa.selenium.support.ui.WebDriverWait(driver(), java.time.Duration.ofSeconds(10));

        try {
            By accountLink = AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeLink' AND (label CONTAINS[c] '@' OR name CONTAINS[c] '@')");
            org.openqa.selenium.WebElement account = wait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(accountLink)
            );
            String accountName = account.getAttribute("label");
            account.click();
            logStep("✅ Selected Google Account: " + accountName);
        } catch (Exception e) {
            logStep("ℹ️ Google Account link not found or already chosen.");
        }

        // 2. Handle Google Consent Screen ("Google will allow Seedling to access... -> Scroll down and Tap 'Continue'")
        logStep("Waiting for Google OAuth Consent page to render...");
        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}

        logStep("Scrolling down to reveal the 'Continue' button on Google Consent screen...");
        scrollDown(0.75, 0.25);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        By continueBtn = AppiumBy.iOSClassChain("**/XCUIElementTypeButton[`name == 'Continue' or label == 'Continue'`]");
        By continueBtnNs = AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND (name == 'Continue' OR label == 'Continue')");
        By continueBtnXPath = By.xpath("//XCUIElementTypeButton[@name='Continue' or @label='Continue']");

        boolean continueTapped = false;
        try {
            org.openqa.selenium.WebElement btn = wait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(continueBtn)
            );
            btn.click();
            logStep("✅ Tapped 'Continue' on Google OAuth Consent Screen (ClassChain)");
            continueTapped = true;
        } catch (Exception ignored) {}

        if (!continueTapped) {
            try {
                org.openqa.selenium.WebElement btn = wait.until(
                    org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(continueBtnNs)
                );
                btn.click();
                logStep("✅ Tapped 'Continue' on Google OAuth Consent Screen (NsPredicate)");
                continueTapped = true;
            } catch (Exception ignored) {}
        }

        if (!continueTapped) {
            try {
                org.openqa.selenium.WebElement btn = wait.until(
                    org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(continueBtnXPath)
                );
                btn.click();
                logStep("✅ Tapped 'Continue' on Google OAuth Consent Screen (XPath)");
                continueTapped = true;
            } catch (Exception e) {
                logStep("ℹ️ Tapping 'Continue' via coordinate fallback at (310, 755)...");
                tapByCoordinates(310, 755, "Google Consent 'Continue' Button");
            }
        }

        // Wait for OAuth redirect back to Seedling app
        try { Thread.sleep(6000); } catch (InterruptedException ignored) {}
    }

    /**
     * Handles X (Twitter) OAuth authorization page
     */
    private void handleXAuthentication() {
        logStep("Waiting for X (Twitter) OAuth page / Authorization...");
        org.openqa.selenium.support.ui.WebDriverWait wait = 
            new org.openqa.selenium.support.ui.WebDriverWait(driver(), java.time.Duration.ofSeconds(12));

        try {
            By authorizeBtn = AppiumBy.iOSNsPredicateString("label CONTAINS[c] 'Authorize' OR name CONTAINS[c] 'Authorize'");
            org.openqa.selenium.WebElement btn = wait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(authorizeBtn)
            );
            btn.click();
            logStep("✅ Tapped 'Authorize' on X (Twitter) OAuth page");
        } catch (Exception e) {
            logStep("ℹ️ X Authorize button not found or already authorized.");
        }

        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
    }

    /**
     * Handles standard iOS ASWebAuthenticationSession / Safari system alerts (e.g., 'App wants to use X to Sign In')
     */
    private void handleWebAuthenticationAlert(String domainHint) {
        logStep("Checking for iOS Web Authentication alert for " + domainHint + "...");
        org.openqa.selenium.support.ui.WebDriverWait wait = 
            new org.openqa.selenium.support.ui.WebDriverWait(driver(), java.time.Duration.ofSeconds(6));

        By continueButton = AppiumBy.iOSClassChain(
            "**/XCUIElementTypeButton[`name == 'Continue' or label == 'Continue'`]"
        );

        try {
            org.openqa.selenium.WebElement btn = wait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(continueButton)
            );
            btn.click();
            logStep("✅ Accepted iOS WebAuth dialog (Tapped 'Continue')");
        } catch (Exception e) {
            logStep("ℹ️ No ASWebAuthentication 'Continue' button found for " + domainHint + ".");
        }

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    /**
     * Handles the native iOS Apple Authentication (AuthKitUI) system sheet
     */
    private void handleAppleSystemSheet() {
        logStep("Waiting for Apple System Sheet / AuthKitUI...");
        org.openqa.selenium.support.ui.WebDriverWait wait = 
            new org.openqa.selenium.support.ui.WebDriverWait(driver(), java.time.Duration.ofSeconds(8));

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // 1. Target the Apple Sign-In action button ("Sign In with Passcode" / "Continue" / "Sign in with Apple")
        By appleActionBtn = AppiumBy.accessibilityId("Sign In with Passcode");
        By appleActionNs = AppiumBy.iOSNsPredicateString(
            "type == 'XCUIElementTypeButton' AND (label CONTAINS[c] 'Passcode' OR name CONTAINS[c] 'Passcode' OR label CONTAINS[c] 'Continue' OR name CONTAINS[c] 'Continue' OR label CONTAINS[c] 'Sign In' OR name CONTAINS[c] 'Sign In')"
        );
        By appleActionXPath = By.xpath(
            "//XCUIElementTypeButton[contains(@name, 'Passcode') or contains(@label, 'Passcode') or contains(@name, 'Continue') or contains(@label, 'Continue')]"
        );

        boolean tapped = false;
        try {
            org.openqa.selenium.WebElement btn = wait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(appleActionBtn)
            );
            btn.click();
            logStep("✅ Tapped 'Sign In with Passcode' via AccessibilityId");
            tapped = true;
        } catch (Exception ignored) {}

        if (!tapped) {
            try {
                org.openqa.selenium.WebElement btn = wait.until(
                    org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(appleActionNs)
                );
                String btnLabel = btn.getAttribute("label");
                btn.click();
                logStep("✅ Tapped '" + btnLabel + "' on Apple System Sheet via NsPredicate");
                tapped = true;
            } catch (Exception ignored) {}
        }

        if (!tapped) {
            try {
                org.openqa.selenium.WebElement btn = wait.until(
                    org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(appleActionXPath)
                );
                btn.click();
                logStep("✅ Tapped Apple Action Button via XPath");
                tapped = true;
            } catch (Exception e) {
                logStep("Tapping 'Sign In with Passcode' blue button at (207, 815)...");
                tapByCoordinates(207, 815, "Sign In with Passcode (Blue Button)");
            }
        }

        // 2. Handle Passcode Entry with passcode
        handleDevicePasscodeIfPrompted();

        // Small pause to allow Apple OAuth token handshake & app redirect
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
    }

    /**
     * Automatically enters device passcode on iOS system keypad
     */
    private void handleDevicePasscodeIfPrompted() {
        String passcode = "000000";
        try {
            String conf = com.company.automation.config.ConfigManager.credential("ios.devicePasscode");
            if (conf != null && !conf.isBlank()) passcode = conf;
        } catch (Exception ignored) {}

        logStep("Waiting for Passcode keypad to appear...");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        logStep("Entering device passcode (" + passcode + ") on iOS Keypad...");
        for (int i = 0; i < passcode.length(); i++) {
            char digit = passcode.charAt(i);
            boolean typed = false;

            // Strategy 1: Find system key element
            try {
                By key = AppiumBy.iOSNsPredicateString("name == '" + digit + "' OR label == '" + digit + "'");
                org.openqa.selenium.WebElement keyEl = driver().findElement(key);
                if (keyEl != null) {
                    keyEl.click();
                    typed = true;
                }
            } catch (Exception ignored) {}

            // Strategy 2: Actions API
            if (!typed) {
                try {
                    new org.openqa.selenium.interactions.Actions(driver()).sendKeys(String.valueOf(digit)).perform();
                    typed = true;
                } catch (Exception ignored) {}
            }

            // Strategy 3: Direct Keypad Coordinate Taps
            if (!typed) {
                int keyX = 207;
                int keyY = 745; // Default for '0'
                if (digit == '1') { keyX = 80; keyY = 520; }
                else if (digit == '2') { keyX = 207; keyY = 520; }
                else if (digit == '3') { keyX = 334; keyY = 520; }
                else if (digit == '4') { keyX = 80; keyY = 595; }
                else if (digit == '5') { keyX = 207; keyY = 595; }
                else if (digit == '6') { keyX = 334; keyY = 595; }
                else if (digit == '7') { keyX = 80; keyY = 670; }
                else if (digit == '8') { keyX = 207; keyY = 670; }
                else if (digit == '9') { keyX = 334; keyY = 670; }
                else if (digit == '0') { keyX = 207; keyY = 745; }

                tapByCoordinates(keyX, keyY, "Passcode Key '" + digit + "'");
            }

            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        }
        logStep("✅ Finished entering device passcode");
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
        try {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("x", x);
            params.put("y", y);
            driver().executeScript("mobile: tap", params);
            logStep("Tapped on " + elementName + " at (" + x + ", " + y + ") via mobile: tap");
        } catch (Exception ex) {
            org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
            org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(finger, 1);
            tap.addAction(finger.createPointerMove(java.time.Duration.ZERO, org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
            tap.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            tap.addAction(new org.openqa.selenium.interactions.Pause(finger, java.time.Duration.ofMillis(150)));
            tap.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
            driver().perform(java.util.Collections.singletonList(tap));
            logStep("Tapped on " + elementName + " at (" + x + ", " + y + ") via W3C Pointer");
        }
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

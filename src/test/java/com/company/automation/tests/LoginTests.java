package com.company.automation.tests;

import com.company.automation.base.BaseTest;
import com.company.automation.config.ConfigManager;
import com.company.automation.pages.ILoginPage;
import com.company.automation.pages.LoginPageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTests extends BaseTest {

    @Test
    public void testValidLogin() {
        log.info("Executing valid login test");
        ILoginPage loginPage = LoginPageFactory.create();
        
        loginPage.enterUsername(ConfigManager.credential("qa.username"));
        loginPage.enterPassword(ConfigManager.credential("qa.password"));
        loginPage.tapLogin();

        try {
            Thread.sleep(6000);
        } catch (InterruptedException ignored) {}

        // Validation
        Assert.assertFalse(loginPage.isErrorDisplayed(), "Login failed! Expected success but saw error.");
    }

    @Test
    public void testGoogleOAuthLogin() {
        log.info("Executing Google OAuth login test");
        ILoginPage loginPage = LoginPageFactory.create();

        loginPage.tapGoogleSignIn();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        // Validation: Verify error message is not displayed
        Assert.assertFalse(loginPage.isErrorDisplayed(), "Google Sign-In failed or threw an error.");
    }

    @Test
    public void testAppleOAuthLogin() {
        log.info("Executing Apple OAuth login test");
        ILoginPage loginPage = LoginPageFactory.create();

        loginPage.tapAppleSignIn();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        // Validation: Verify error message is not displayed
        Assert.assertFalse(loginPage.isErrorDisplayed(), "Apple Sign-In failed or threw an error.");
    }

    @Test(enabled = false, description = "Temporarily disabled - X login is currently down/unavailable on this version")
    public void testXOAuthLogin() {
        log.info("Executing X (Twitter) OAuth login test");
        ILoginPage loginPage = LoginPageFactory.create();

        loginPage.tapXSignIn();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        // Validation: Verify error message is not displayed
        Assert.assertFalse(loginPage.isErrorDisplayed(), "X (Twitter) Sign-In failed or threw an error.");
    }
}
